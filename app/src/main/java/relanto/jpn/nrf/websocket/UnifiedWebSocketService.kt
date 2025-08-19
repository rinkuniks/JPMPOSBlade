package relanto.jpn.nrf.websocket

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import org.java_websocket.WebSocket as JavaWebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedWebSocketService @Inject constructor(
    private val context: Context
) {
    
    private val TAG = "UnifiedWebSocketService"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Server state
    private var webSocketServer: WebSocketServerImpl? = null
    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()
    
    // Client state
    private var webSocketClient: WebSocket? = null
    private var currentUrl: String? = null
    private val _clientState = MutableStateFlow<ClientState>(ClientState.Disconnected)
    val clientState: StateFlow<ClientState> = _clientState.asStateFlow()
    
    // Shared state
    private val _connectedClients = MutableStateFlow(0)
    val connectedClients: StateFlow<Int> = _connectedClients.asStateFlow()
    
    private val _serverIp = MutableStateFlow<String?>(null)
    val serverIp: StateFlow<String?> = _serverIp.asStateFlow()
    
    private val _localIpAddress = MutableStateFlow<String?>(null)
    val localIpAddress: StateFlow<String?> = _localIpAddress.asStateFlow()
    
    sealed class ServerState {
        object Stopped : ServerState()
        object Starting : ServerState()
        object Running : ServerState()
        data class Error(val message: String) : ServerState()
    }
    
    sealed class ClientState {
        object Disconnected : ClientState()
        object Connecting : ClientState()
        object Connected : ClientState()
        data class Error(val message: String) : ClientState()
    }
    
    init {
        // Initialize local IP address
        _localIpAddress.value = getLocalIpAddress()
    }
    
    // ==================== SERVER FUNCTIONALITY ====================
    
    fun startServer(port: Int = 8080) {
        try {
            if (webSocketServer != null && _serverState.value is ServerState.Running) {
                Log.w(TAG, "Server is already running")
                return
            }
            
            Log.d(TAG, "Starting WebSocket server on port $port")
            _serverState.value = ServerState.Starting
            
            val ipAddress = _localIpAddress.value ?: getLocalIpAddress()
            if (ipAddress == null) {
                val errorMsg = "Failed to get local IP address"
                Log.e(TAG, errorMsg)
                _serverState.value = ServerState.Error(errorMsg)
                return
            }
            
            Log.d(TAG, "Local IP address: $ipAddress")
            _serverIp.value = ipAddress
            
            // Check if port is available, if not find an available one
            var actualPort = port
            if (!isPortAvailable(port)) {
                Log.w(TAG, "Port $port is not available, searching for available port")
                actualPort = findAvailablePort(port)
                if (actualPort == -1) {
                    val errorMsg = "No available ports found in range $port-${port + 100}"
                    Log.e(TAG, errorMsg)
                    _serverState.value = ServerState.Error(errorMsg)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                Log.d(TAG, "Using available port: $actualPort")
            }
            
            webSocketServer = WebSocketServerImpl(InetSocketAddress(ipAddress, actualPort))
            webSocketServer!!.start()
            
            Log.d(TAG, "WebSocket server started on $ipAddress:$actualPort")
            _serverState.value = ServerState.Running
            
            // Start notification service
            startNotificationService()
            
            onServerStarted()
            
        } catch (e: Exception) {
            val errorMsg = "Failed to start server: ${e.message}"
            Log.e(TAG, errorMsg, e)
            _serverState.value = ServerState.Error(errorMsg)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun stopServer() {
        try {
            Log.d(TAG, "Stopping WebSocket server")
            
            // Disconnect all clients first
            disconnectAllClients()
            
            webSocketServer?.stop()
            webSocketServer = null
            _connectedClients.value = 0
            _serverState.value = ServerState.Stopped
            
            // Stop notification service
            stopNotificationService()
            
            onServerStopped()
            Log.d(TAG, "WebSocket server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }
    
    // ==================== CLIENT FUNCTIONALITY ====================
    
    fun connectToServer(url: String? = null) {
        val targetUrl = url ?: getLocalServerUrl()
        if (targetUrl == null) {
            Log.e(TAG, "No server URL available")
            _clientState.value = ClientState.Error("No server URL available")
            return
        }
        
        // Don't try to connect if already connected
        if (_clientState.value is ClientState.Connected) {
            Log.d(TAG, "Already connected to server")
            return
        }
        
        scope.launch {
            try {
                _clientState.value = ClientState.Connecting
                currentUrl = targetUrl
                
                Log.d(TAG, "Attempting to connect to: $targetUrl")
                
                val client = OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .pingInterval(30, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url(targetUrl)
                    .build()
                
                webSocketClient = client.newWebSocket(request, createWebSocketListener())
                
                // The connection result will be handled by the WebSocketListener callbacks
                // No need for artificial delays
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect", e)
                _clientState.value = ClientState.Error(e.message ?: "Connection failed")
            }
        }
    }
    
    fun disconnectFromServer() {
        try {
            Log.d(TAG, "Disconnecting WebSocket client")
            webSocketClient?.close(1000, "Manual disconnect")
            webSocketClient = null
            currentUrl = null
            _clientState.value = ClientState.Disconnected
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting client", e)
        }
    }
    
    fun sendMessage(message: String) {
        if (_clientState.value is ClientState.Connected) {
            webSocketClient?.send(message)
            Log.d(TAG, "Sent message: $message")
            
            // Show toast when client sends message to server
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "📤 Sent: $message", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Cannot send message: WebSocket not connected")
        }
    }
    
    // ==================== UTILITY FUNCTIONS ====================
    
    private fun getLocalIpAddress(): String? {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val inetAddresses = networkInterface.inetAddresses
                
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress.hostAddress.indexOf(':') < 0) {
                        val hostAddress = inetAddress.hostAddress
                        Log.d(TAG, "Found network interface: $hostAddress")
                        if (hostAddress.startsWith("192.168.") || 
                            hostAddress.startsWith("10.") || 
                            hostAddress.startsWith("172.")) {
                            Log.d(TAG, "Selected IP address: $hostAddress")
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP address", e)
        }
        return null
    }
    
    private fun getLocalServerUrl(): String? {
        val ip = _serverIp.value
        return if (ip != null) "ws://$ip:8080" else null
    }
    
    private fun onServerStarted() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "WebSocket server started", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun onServerStopped() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "WebSocket server stopped", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun isServerRunning(): Boolean {
        return _serverState.value is ServerState.Running
    }
    
    fun isClientConnected(): Boolean {
        return _clientState.value is ClientState.Connected
    }
    
    // Check if server has any active client connections
    fun hasActiveClientConnections(): Boolean {
        return _connectedClients.value > 0
    }
    
    // Check if current device is connected as a client to any server
    fun isCurrentDeviceConnectedAsClient(): Boolean {
        return _clientState.value is ClientState.Connected
    }
    
    // Get detailed connection status for debugging
    fun getConnectionStatus(): String {
        return buildString {
            append("Server State: ${_serverState.value}")
            append(", Client State: ${_clientState.value}")
            append(", Connected Clients: ${_connectedClients.value}")
            append(", Server IP: ${_serverIp.value ?: "null"}")
            append(", Current URL: ${currentUrl ?: "null"}")
        }
    }
    
    fun broadcastMessage(message: String) {
        webSocketServer?.broadcast(message)
        
        // Show toast when server broadcasts message to clients
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "📢 Broadcasted: $message", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Check if port is available
    fun isPortAvailable(port: Int): Boolean {
        return try {
            val socket = java.net.ServerSocket(port)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Find available port starting from given port
    fun findAvailablePort(startPort: Int): Int {
        var port = startPort
        while (!isPortAvailable(port)) {
            port++
            if (port > startPort + 100) { // Limit search to 100 ports
                return -1 // No available port found
            }
        }
        return port
    }
    
    // Disconnect all clients when server stops
    private fun disconnectAllClients() {
        // This will be handled by the WebSocket server implementation
        // When server stops, all client connections are automatically closed
        Log.d(TAG, "All clients will be disconnected as server is stopping")
    }
    
    // ==================== WEB SOCKET SERVER IMPLEMENTATION ====================
    
    private inner class WebSocketServerImpl(address: InetSocketAddress) : WebSocketServer(address) {
        
        override fun onOpen(conn: JavaWebSocket, handshake: ClientHandshake) {
            Log.d(TAG, "New client connection from: ${conn.remoteSocketAddress}")
            Log.d(TAG, "Handshake: ${handshake.resourceDescriptor}")
            _connectedClients.value = _connectedClients.value + 1
            Log.d(TAG, "Connected clients count updated to: ${_connectedClients.value}")
            
            // Don't update client state here - that represents the current device's client connection
            // _clientState.value = ClientState.Connected  // REMOVED THIS LINE
            
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Client connected", Toast.LENGTH_SHORT).show()
            }
            
            // Send welcome message
            conn.send("Welcome to WebSocket Server!")
            Log.d(TAG, "Welcome message sent to client")
            
            // Show toast for welcome message
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "👋 Sent welcome message", Toast.LENGTH_SHORT).show()
            }
        }
        
        override fun onClose(conn: JavaWebSocket, code: Int, reason: String, remote: Boolean) {
            Log.d(TAG, "Client disconnected: ${conn.remoteSocketAddress}, code: $code, reason: $reason, remote: $remote")
            _connectedClients.value = _connectedClients.value - 1
            Log.d(TAG, "Connected clients count updated to: ${_connectedClients.value}")
            
            // Don't update client state here - that represents the current device's client connection
            // If no more clients, just ensure connectedClients is not negative
            if (_connectedClients.value < 0) {
                _connectedClients.value = 0
                Log.w(TAG, "Connected clients count was negative, reset to 0")
            }
        }
        
        override fun onMessage(conn: JavaWebSocket, message: String) {
            Log.d(TAG, "Received message from ${conn.remoteSocketAddress}: $message")
            
            // Show toast message when server receives a message from client
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "📨 Client: $message", Toast.LENGTH_LONG).show()
            }
            
            // Handle different message types
            when {
                message.startsWith("PING") -> {
                    Log.d(TAG, "Responding to PING from ${conn.remoteSocketAddress}")
                    conn.send("PONG")
                    // Show toast for PING response
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "🏓 Sent PONG response", Toast.LENGTH_SHORT).show()
                    }
                }
                message.startsWith("STATUS") -> {
                    Log.d(TAG, "Status request from ${conn.remoteSocketAddress}")
                    conn.send("STATUS: Server running, ${_connectedClients.value} clients connected")
                    // Show toast for STATUS response
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "📊 Sent status response", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // Echo message back
                    conn.send("Echo: $message")
                    // Show toast for echo response
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "🔄 Echoed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        override fun onError(conn: JavaWebSocket?, ex: Exception) {
            Log.e(TAG, "WebSocket server error", ex)
            _serverState.value = ServerState.Error(ex.message ?: "Unknown error")
        }
        
        override fun onStart() {
            Log.d(TAG, "WebSocket server started successfully")
        }
    }
    
    // ==================== WEB SOCKET CLIENT LISTENER ====================
    
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket client connected successfully to: ${webSocket.request().url}")
                Log.d(TAG, "Response code: ${response.code}, message: ${response.message}")
                _clientState.value = ClientState.Connected
                Log.d(TAG, "Client state updated to: Connected")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                // Handle incoming messages here
                handleIncomingMessage(text)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket client closed: $code - $reason")
                _clientState.value = ClientState.Disconnected
                Log.d(TAG, "Client state updated to: Disconnected")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket client failure", t)
                Log.e(TAG, "Response: ${response?.code} - ${response?.message}")
                
                val errorMessage = when (t) {
                    is java.net.SocketTimeoutException -> "Connection timeout. Server may be unreachable."
                    is java.net.ConnectException -> "Connection refused. Please ensure the WebSocket server is running and accessible."
                    is java.net.UnknownHostException -> "Unknown host. Please check the server address and try again."
                    is javax.net.ssl.SSLException -> "SSL/TLS error. Try using ws:// instead of wss:// for local connections."
                    is java.net.NoRouteToHostException -> "No route to host. Check network connectivity and firewall settings."
                    else -> "Connection failed: ${t.message ?: t.javaClass.simpleName}"
                }
                
                _clientState.value = ClientState.Error(errorMessage)
                Log.d(TAG, "Client state updated to: Error - $errorMessage")
            }
        }
    }
    
    private fun handleIncomingMessage(message: String) {
        // Show toast message when client receives a message from server
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "📨 Server: $message", Toast.LENGTH_LONG).show()
        }
        
        // Handle different types of messages here
        when {
            message.startsWith("PING") -> {
                scope.launch {
                    sendMessage("PONG")
                    // Show toast for PONG response
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "🏓 Sent PONG response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            message.startsWith("STATUS") -> {
                // Handle status updates
            }
            else -> {
                // Handle other message types
            }
        }
    }
    
    // ==================== NOTIFICATION SERVICE MANAGEMENT ====================
    
    private fun startNotificationService() {
        try {
            val intent = Intent(context, WebSocketNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Notification service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start notification service", e)
        }
    }
    
    private fun stopNotificationService() {
        try {
            val intent = Intent(context, WebSocketNotificationService::class.java)
            context.stopService(intent)
            Log.d(TAG, "Notification service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop notification service", e)
        }
    }
}
