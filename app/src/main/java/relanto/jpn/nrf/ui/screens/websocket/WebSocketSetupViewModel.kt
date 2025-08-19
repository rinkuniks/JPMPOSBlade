package relanto.jpn.nrf.ui.screens.websocket

import dagger.hilt.android.lifecycle.HiltViewModel
import relanto.jpn.nrf.base.BaseViewModel
import relanto.jpn.nrf.websocket.UnifiedWebSocketService
import relanto.jpn.nrf.websocket.WebSocketSessionManager
import javax.inject.Inject
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import dagger.hilt.android.qualifiers.ApplicationContext

data class WebSocketSetupState(
    val serverUrl: String = "",
    val serverIp: String = "",
    val isConnecting: Boolean = false,
    val isServerRunning: Boolean = false,
    val connectedClients: Int = 0,
    val enableServerMode: Boolean = false,
    val enableClientMode: Boolean = false,
    val messageText: String = "",
    val isClientConnected: Boolean = false
)

sealed class WebSocketSetupEvent {
    object Connect : WebSocketSetupEvent()
    object Disconnect : WebSocketSetupEvent()
    object Reconnect : WebSocketSetupEvent()
    object RefreshConnection : WebSocketSetupEvent()
    object StartServer : WebSocketSetupEvent()
    object StopServer : WebSocketSetupEvent()
    data class UpdateServerIp(val ip: String) : WebSocketSetupEvent()
    object ToggleServerMode : WebSocketSetupEvent()
    object ToggleClientMode : WebSocketSetupEvent()
    object DisconnectAll : WebSocketSetupEvent()
    data class UpdateMessageText(val text: String) : WebSocketSetupEvent()
    object SendMessage : WebSocketSetupEvent()
}

@HiltViewModel
class WebSocketSetupViewModel @Inject constructor(
    private val unifiedWebSocketService: UnifiedWebSocketService,
    private val sessionManager: WebSocketSessionManager,
    @ApplicationContext private val context: Context
) : BaseViewModel<WebSocketSetupState, WebSocketSetupEvent>() {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("websocket_prefs", Context.MODE_PRIVATE)
    }
    private val SAVED_IP_KEY = "saved_server_ip"
    
    override fun createInitialState(): WebSocketSetupState {
        val savedIp = try {
            sharedPreferences.getString(SAVED_IP_KEY, "") ?: ""
        } catch (e: Exception) {
            Log.e("WebSocketSetupViewModel", "Failed to load saved IP", e)
            ""
        }
        return WebSocketSetupState(serverIp = savedIp)
    }
    
    init {
        // Load and restore previous session state
        restoreSessionState()
        
        // Observe server state changes
        launchCoroutine {
            unifiedWebSocketService.serverState.collectLatest { serverState ->
                when (serverState) {
                    is UnifiedWebSocketService.ServerState.Running -> {
                        setState { 
                            copy(
                                isServerRunning = true,
                                serverIp = unifiedWebSocketService.serverIp.value ?: ""
                            )
                        }
                        // Save session state when server starts
                        saveSessionState()
                    }
                    is UnifiedWebSocketService.ServerState.Stopped -> {
                        setState { 
                            copy(
                                isServerRunning = false,
                                serverIp = "",
                                connectedClients = 0
                            )
                        }
                        // Save session state when server stops
                        saveSessionState()
                    }
                    is UnifiedWebSocketService.ServerState.Error -> {
                        setState { 
                            copy(
                                isServerRunning = false,
                                serverIp = ""
                            )
                        }
                        // Save session state on error
                        saveSessionState()
                    }
                    else -> {}
                }
            }
        }
        
        // Observe connected clients count
        launchCoroutine {
            unifiedWebSocketService.connectedClients.collectLatest { count ->
                setState { copy(connectedClients = count) }
            }
        }
        
        // Observe server IP changes
        launchCoroutine {
            unifiedWebSocketService.serverIp.collectLatest { ip ->
                if (ip != null && uiState.value.enableServerMode) {
                    setState { 
                        copy(
                            serverIp = ip,
                            serverUrl = formatServerUrl(ip)
                        )
                    }
                    // Save the IP address
                    saveServerIp(ip)
                    // Save session state
                    saveSessionState()
                }
            }
        }
        
        // Observe local IP address
        launchCoroutine {
            unifiedWebSocketService.localIpAddress.collectLatest { ip ->
                if (ip != null) {
                    Log.d("WebSocketSetupViewModel", "Local IP address: $ip")
                }
            }
        }
        
        // Observe client state changes
        launchCoroutine {
            unifiedWebSocketService.clientState.collectLatest { clientState ->
                when (clientState) {
                    is UnifiedWebSocketService.ClientState.Connected -> {
                        // Update UI state to show client is connected
                        setState { copy(isConnecting = false, isClientConnected = true) }
                        // Save session state when client connects
                        saveSessionState()
                        Log.d("WebSocketSetupViewModel", "Client connected to server")
                    }
                    is UnifiedWebSocketService.ClientState.Connecting -> {
                        // Update UI state to show client is connecting
                        setState { copy(isConnecting = true, isClientConnected = false) }
                        Log.d("WebSocketSetupViewModel", "Client connecting to server")
                    }
                    is UnifiedWebSocketService.ClientState.Disconnected -> {
                        // Update UI state to show client is disconnected
                        setState { copy(isConnecting = false, isClientConnected = false) }
                        // Save session state when client disconnects
                        saveSessionState()
                        Log.d("WebSocketSetupViewModel", "Client disconnected from server")
                    }
                    is UnifiedWebSocketService.ClientState.Error -> {
                        // Update UI state to show client connection error
                        setState { copy(isConnecting = false, isClientConnected = false) }
                        Log.e("WebSocketSetupViewModel", "Client connection error: ${clientState.message}")
                    }
                }
            }
        }
    }
    
    override fun onEvent(event: WebSocketSetupEvent) {
        when (event) {
            is WebSocketSetupEvent.Connect -> {
                connectToServer()
            }
            is WebSocketSetupEvent.Disconnect -> {
                disconnectFromServer()
            }
            is WebSocketSetupEvent.Reconnect -> {
                reconnectToServer()
            }
            is WebSocketSetupEvent.RefreshConnection -> {
                refreshConnection()
            }
            is WebSocketSetupEvent.StartServer -> {
                startWebSocketServer()
            }
            is WebSocketSetupEvent.StopServer -> {
                stopWebSocketServer()
            }
            is WebSocketSetupEvent.UpdateServerIp -> {
                updateServerIp(event.ip)
            }
            is WebSocketSetupEvent.ToggleServerMode -> {
                setState { copy(enableServerMode = !uiState.value.enableServerMode) }
                saveSessionState()
            }
            is WebSocketSetupEvent.ToggleClientMode -> {
                setState { copy(enableClientMode = !uiState.value.enableClientMode) }
                saveSessionState()
            }
            is WebSocketSetupEvent.DisconnectAll -> {
                disconnectAll()
            }
            is WebSocketSetupEvent.UpdateMessageText -> {
                updateMessageText(event.text)
            }
            is WebSocketSetupEvent.SendMessage -> {
                sendMessage()
            }
        }
    }
    
    /**
     * Test connection with a specific URL (for debugging)
     */
    fun testConnection(testUrl: String) {
        Log.d("WebSocketSetupViewModel", "Testing connection with URL: $testUrl")
        launchCoroutine {
            setState { copy(isConnecting = true) }
            try {
                unifiedWebSocketService.connectToServer(testUrl)
                Log.d("WebSocketSetupViewModel", "Test connection initiated")
            } catch (e: Exception) {
                val errorMsg = "Test connection failed: ${e.message}"
                Log.e("WebSocketSetupViewModel", errorMsg, e)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            } finally {
                setState { copy(isConnecting = false) }
            }
        }
    }
    
    // Restore previous session state
    private fun restoreSessionState() {
        val sessionState = sessionManager.loadSessionState()
        if (sessionState.sessionActive) {
            Log.d("WebSocketSetupViewModel", "Restoring session state: $sessionState")
            
            // Show toast that session is being restored
            Toast.makeText(context, "Restoring previous WebSocket session...", Toast.LENGTH_LONG).show()
            
            setState { 
                copy(
                    enableServerMode = sessionState.serverModeEnabled,
                    enableClientMode = sessionState.clientModeEnabled,
                    serverIp = sessionState.serverIp,
                    serverUrl = formatServerUrl(sessionState.serverIp)
                )
            }
            
            // Auto-restart session in background
            sessionManager.autoRestartSession()
            
            // Wait a bit for connections to establish
            launchCoroutine {
                kotlinx.coroutines.delay(2000) // Wait 2 seconds for connections
            }
        }
    }
    
    // Save current session state
    private fun saveSessionState() {
        val currentState = uiState.value
        sessionManager.saveSessionState(
            serverModeEnabled = currentState.enableServerMode,
            clientModeEnabled = currentState.enableClientMode,
            serverIp = currentState.serverIp,
            serverPort = 8080,
            isConnected = isConnected
        )
        Log.d("WebSocketSetupViewModel", "Session state saved")
    }
    
    private fun updateServerIp(ip: String) {
        Log.d("WebSocketSetupViewModel", "updateServerIp called with IP: '$ip'")
        val formattedUrl = formatServerUrl(ip)
        Log.d("WebSocketSetupViewModel", "Formatted URL: '$formattedUrl'")
        
        setState { 
            copy(
                serverIp = ip,
                serverUrl = formattedUrl
            )
        }
        
        Log.d("WebSocketSetupViewModel", "State updated - serverIp: '${uiState.value.serverIp}', serverUrl: '${uiState.value.serverUrl}'")
        
        // Save the IP address
        saveServerIp(ip)
        // Save session state
        saveSessionState()
    }
    
    private fun formatServerUrl(ip: String): String {
        return if (ip.isNotBlank()) "ws://$ip:8080" else ""
    }
    
    private fun saveServerIp(ip: String) {
        try {
            sharedPreferences.edit().putString(SAVED_IP_KEY, ip).apply()
            Log.d("WebSocketSetupViewModel", "Saved server IP: $ip")
        } catch (e: Exception) {
            Log.e("WebSocketSetupViewModel", "Failed to save server IP", e)
        }
    }
    
    private fun connectToServer() {
        launchCoroutine {
            setState { copy(isConnecting = true) }
            try {
                val url = uiState.value.serverUrl
                Log.d("WebSocketSetupViewModel", "Attempting to connect to URL: '$url'")
                
                if (url.isNotBlank()) {
                    Log.d("WebSocketSetupViewModel", "Calling unifiedWebSocketService.connectToServer with URL: $url")
                    unifiedWebSocketService.connectToServer(url)
                    Log.d("WebSocketSetupViewModel", "connectToServer called successfully")
                } else {
                    val errorMsg = "No server URL available. Server IP: '${uiState.value.serverIp}', Server URL: '$url'"
                    Log.e("WebSocketSetupViewModel", errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                val errorMsg = "Connection failed: ${e.message}"
                Log.e("WebSocketSetupViewModel", errorMsg, e)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            } finally {
                setState { copy(isConnecting = false) }
            }
        }
    }
    
    private fun disconnectFromServer() {
        launchCoroutine {
            try {
                Log.d("WebSocketSetupViewModel", "Disconnecting client from server")
                unifiedWebSocketService.disconnectFromServer()
                
                // Show toast that client disconnected
                Toast.makeText(context, "Client disconnected from server", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Log.e("WebSocketSetupViewModel", "Error disconnecting client", e)
                Toast.makeText(context, "Error disconnecting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun reconnectToServer() {
        launchCoroutine {
            setState { copy(isConnecting = true) }
            try {
                unifiedWebSocketService.disconnectFromServer()
                // Wait a bit before reconnecting
                kotlinx.coroutines.delay(1000)
                val url = uiState.value.serverUrl
                if (url.isNotBlank()) {
                    unifiedWebSocketService.connectToServer(url)
                }
            } catch (e: Exception) {
                Log.e("WebSocketSetupViewModel", "Reconnection failed", e)
            } finally {
                setState { copy(isConnecting = false) }
            }
        }
    }
    
    private fun refreshConnection() {
        launchCoroutine {
            if (unifiedWebSocketService.isClientConnected()) {
                // Send a ping to check connection
                unifiedWebSocketService.sendMessage("PING")
            }
        }
    }
    
    private fun startWebSocketServer() {
        try {
            unifiedWebSocketService.startServer(8080)
        } catch (e: Exception) {
            Log.e("WebSocketSetupViewModel", "Failed to start server", e)
        }
    }
    
    private fun stopWebSocketServer() {
        try {
            Log.d("WebSocketSetupViewModel", "Stopping WebSocket server")
            unifiedWebSocketService.stopServer()
            
            // Show toast that server stopped
            Toast.makeText(context, "Server stopped - all clients disconnected", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Log.e("WebSocketSetupViewModel", "Failed to stop server", e)
            Toast.makeText(context, "Error stopping server: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Enhanced disconnect all with proper cleanup
    private fun disconnectAll() {
        launchCoroutine {
            try {
                Log.d("WebSocketSetupViewModel", "Disconnecting all connections")
                
                // Stop server if running (this will disconnect all clients)
                if (uiState.value.isServerRunning) {
                    Log.d("WebSocketSetupViewModel", "Stopping server to disconnect all clients")
                    unifiedWebSocketService.stopServer()
                }
                
                // Disconnect client if connected
                if (unifiedWebSocketService.isClientConnected()) {
                    Log.d("WebSocketSetupViewModel", "Disconnecting client")
                    unifiedWebSocketService.disconnectFromServer()
                }
                
                // Clear session state
                sessionManager.clearSessionState()
                
                // Reset UI state
                setState { 
                    copy(
                        isServerRunning = false,
                        isConnecting = false,
                        connectedClients = 0,
                        enableServerMode = false,
                        enableClientMode = false
                    )
                }
                
                Toast.makeText(context, "All connections disconnected and session cleared", Toast.LENGTH_LONG).show()
                Log.d("WebSocketSetupViewModel", "All connections disconnected and session cleared")
                
            } catch (e: Exception) {
                Log.e("WebSocketSetupViewModel", "Error disconnecting all", e)
                Toast.makeText(context, "Error disconnecting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Expose unified WebSocket service states
    val serverState = unifiedWebSocketService.serverState
    val clientState = unifiedWebSocketService.clientState
    val serverConnectedClients = unifiedWebSocketService.connectedClients
    val serverIp = unifiedWebSocketService.serverIp
    val localIpAddress = unifiedWebSocketService.localIpAddress
    
    // Helper properties for UI
    val isConnected: Boolean
        get() = unifiedWebSocketService.isClientConnected()
    
    // Check if there are active client connections from server perspective
    val hasActiveClientConnections: Boolean
        get() = unifiedWebSocketService.hasActiveClientConnections()
    
    // Check if current device is connected as a client to any server
    val isCurrentDeviceConnectedAsClient: Boolean
        get() = unifiedWebSocketService.isCurrentDeviceConnectedAsClient()
    
    // Messaging functionality
    private fun updateMessageText(text: String) {
        setState { copy(messageText = text) }
    }
    
    private fun sendMessage() {
        val message = uiState.value.messageText
        if (message.isBlank()) return
        
        try {
            // Send message based on current mode
            if (isConnected) {
                // Client mode: send to server
                unifiedWebSocketService.sendMessage(message)
                Toast.makeText(context, "Message sent to server", Toast.LENGTH_SHORT).show()
            } else if (uiState.value.isServerRunning) {
                // Server mode: broadcast to all clients
                unifiedWebSocketService.broadcastMessage(message)
                Toast.makeText(context, "Message broadcasted to ${uiState.value.connectedClients} client(s)", Toast.LENGTH_SHORT).show()
            }
            
            // Clear message text after sending
            setState { copy(messageText = "") }
            
        } catch (e: Exception) {
            Log.e("WebSocketSetupViewModel", "Failed to send message", e)
            Toast.makeText(context, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
