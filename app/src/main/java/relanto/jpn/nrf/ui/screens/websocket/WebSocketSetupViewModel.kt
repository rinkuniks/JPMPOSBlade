package relanto.jpn.nrf.ui.screens.websocket

import dagger.hilt.android.lifecycle.HiltViewModel
import relanto.jpn.nrf.base.BaseViewModel
import relanto.jpn.nrf.websocket.UnifiedWebSocketService
import javax.inject.Inject
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.collectLatest
import dagger.hilt.android.qualifiers.ApplicationContext

data class WebSocketSetupState(
    val serverUrl: String = "",
    val serverIp: String = "",
    val isConnecting: Boolean = false,
    val isServerRunning: Boolean = false,
    val connectedClients: Int = 0,
    val enableServerMode: Boolean = false,
    val enableClientMode: Boolean = false
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
}

@HiltViewModel
class WebSocketSetupViewModel @Inject constructor(
    private val unifiedWebSocketService: UnifiedWebSocketService,
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
                    }
                    is UnifiedWebSocketService.ServerState.Stopped -> {
                        setState { 
                            copy(
                                isServerRunning = false,
                                serverIp = "",
                                connectedClients = 0
                            )
                        }
                    }
                    is UnifiedWebSocketService.ServerState.Error -> {
                        setState { 
                            copy(
                                isServerRunning = false,
                                serverIp = ""
                            )
                        }
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
            }
            is WebSocketSetupEvent.ToggleClientMode -> {
                setState { copy(enableClientMode = !uiState.value.enableClientMode) }
            }
        }
    }
    
    private fun updateServerIp(ip: String) {
        val formattedUrl = formatServerUrl(ip)
        setState { 
            copy(
                serverIp = ip,
                serverUrl = formattedUrl
            )
        }
        // Save the IP address
        saveServerIp(ip)
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
                if (url.isNotBlank()) {
                    unifiedWebSocketService.connectToServer(url)
                } else {
                    Log.e("WebSocketSetupViewModel", "No server URL available")
                }
            } catch (e: Exception) {
                Log.e("WebSocketSetupViewModel", "Connection failed", e)
            } finally {
                setState { copy(isConnecting = false) }
            }
        }
    }
    
    private fun disconnectFromServer() {
        launchCoroutine {
            unifiedWebSocketService.disconnectFromServer()
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
            unifiedWebSocketService.stopServer()
        } catch (e: Exception) {
            Log.e("WebSocketSetupViewModel", "Failed to stop server", e)
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
}
