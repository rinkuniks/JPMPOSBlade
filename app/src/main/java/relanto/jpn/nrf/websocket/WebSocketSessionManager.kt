package relanto.jpn.nrf.websocket

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketSessionManager @Inject constructor(
    private val context: Context,
    private val unifiedWebSocketService: UnifiedWebSocketService
) {
    
    private val TAG = "WebSocketSessionManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val PREFS_NAME = "websocket_session_prefs"
        private const val KEY_SERVER_MODE_ENABLED = "server_mode_enabled"
        private const val KEY_CLIENT_MODE_ENABLED = "client_mode_enabled"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_IS_CONNECTED = "is_connected"
        private const val KEY_SESSION_ACTIVE = "session_active"
        private const val DEFAULT_PORT = 8080
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Save current session state
    fun saveSessionState(
        serverModeEnabled: Boolean,
        clientModeEnabled: Boolean,
        serverIp: String = "",
        serverPort: Int = DEFAULT_PORT,
        isConnected: Boolean = false
    ) {
        Log.d(TAG, "Saving session state: server=$serverModeEnabled, client=$clientModeEnabled, ip=$serverIp, connected=$isConnected")
        
        prefs.edit().apply {
            putBoolean(KEY_SERVER_MODE_ENABLED, serverModeEnabled)
            putBoolean(KEY_CLIENT_MODE_ENABLED, clientModeEnabled)
            putString(KEY_SERVER_IP, serverIp)
            putInt(KEY_SERVER_PORT, serverPort)
            putBoolean(KEY_IS_CONNECTED, isConnected)
            putBoolean(KEY_SESSION_ACTIVE, true)
        }.apply()
        
        // Start foreground service if session is active
        if (serverModeEnabled || clientModeEnabled) {
            startForegroundService()
        }
    }
    
    // Load saved session state
    fun loadSessionState(): SessionState {
        val serverModeEnabled = prefs.getBoolean(KEY_SERVER_MODE_ENABLED, false)
        val clientModeEnabled = prefs.getBoolean(KEY_CLIENT_MODE_ENABLED, false)
        val serverIp = prefs.getString(KEY_SERVER_IP, "") ?: ""
        val serverPort = prefs.getInt(KEY_SERVER_PORT, DEFAULT_PORT)
        val isConnected = prefs.getBoolean(KEY_IS_CONNECTED, false)
        val sessionActive = prefs.getBoolean(KEY_SESSION_ACTIVE, false)
        
        Log.d(TAG, "Loaded session state: server=$serverModeEnabled, client=$clientModeEnabled, ip=$serverIp, connected=$isConnected, active=$sessionActive")
        
        return SessionState(
            serverModeEnabled = serverModeEnabled,
            clientModeEnabled = clientModeEnabled,
            serverIp = serverIp,
            serverPort = serverPort,
            isConnected = isConnected,
            sessionActive = sessionActive
        )
    }
    
    // Clear session state
    fun clearSessionState() {
        Log.d(TAG, "Clearing session state")
        
        prefs.edit().apply {
            putBoolean(KEY_SERVER_MODE_ENABLED, false)
            putBoolean(KEY_CLIENT_MODE_ENABLED, false)
            putString(KEY_SERVER_IP, "")
            putInt(KEY_SERVER_PORT, DEFAULT_PORT)
            putBoolean(KEY_IS_CONNECTED, false)
            putBoolean(KEY_SESSION_ACTIVE, false)
        }.apply()
        
        // Stop foreground service
        stopForegroundService()
    }
    
    // Auto-restart session based on saved state
    fun autoRestartSession() {
        val sessionState = loadSessionState()
        
        if (!sessionState.sessionActive) {
            Log.d(TAG, "No active session to restart")
            return
        }
        
        Log.d(TAG, "Auto-restarting session: $sessionState")
        
        scope.launch {
            try {
                // Restart server if it was enabled
                if (sessionState.serverModeEnabled) {
                    Log.d(TAG, "Auto-restarting server on port ${sessionState.serverPort}")
                    unifiedWebSocketService.startServer(sessionState.serverPort)
                }
                
                // Auto-connect client if it was enabled and connected
                if (sessionState.clientModeEnabled && sessionState.isConnected && sessionState.serverIp.isNotBlank()) {
                    Log.d(TAG, "Auto-connecting client to ${sessionState.serverIp}:${sessionState.serverPort}")
                    unifiedWebSocketService.connectToServer("ws://${sessionState.serverIp}:${sessionState.serverPort}")
                }
                
                // Start foreground service to maintain connections
                startForegroundService()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error auto-restarting session", e)
            }
        }
    }
    
    // Start foreground service
    private fun startForegroundService() {
        try {
            val intent = Intent(context, WebSocketNotificationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Foreground service started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }
    
    // Stop foreground service
    private fun stopForegroundService() {
        try {
            val intent = Intent(context, WebSocketNotificationService::class.java)
            context.stopService(intent)
            Log.d(TAG, "Foreground service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground service", e)
        }
    }
    
    // Check if session should be maintained
    fun shouldMaintainSession(): Boolean {
        return prefs.getBoolean(KEY_SESSION_ACTIVE, false)
    }
    
    // Update connection status
    fun updateConnectionStatus(isConnected: Boolean) {
        prefs.edit().putBoolean(KEY_IS_CONNECTED, isConnected).apply()
        Log.d(TAG, "Updated connection status: $isConnected")
    }
    
    data class SessionState(
        val serverModeEnabled: Boolean,
        val clientModeEnabled: Boolean,
        val serverIp: String,
        val serverPort: Int,
        val isConnected: Boolean,
        val sessionActive: Boolean
    )
}
