package relanto.jpn.nrf.websocket

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import relanto.jpn.nrf.MainActivity
import relanto.jpn.nrf.R
import relanto.jpn.nrf.websocket.UnifiedWebSocketService.ServerState
import relanto.jpn.nrf.websocket.UnifiedWebSocketService.ClientState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WebSocketNotificationService : Service() {
    
    @Inject
    lateinit var unifiedWebSocketService: UnifiedWebSocketService
    
    @Inject
    lateinit var sessionManager: WebSocketSessionManager
    
    private val TAG = "WebSocketNotificationService"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "websocket_status_channel"
        private const val CHANNEL_NAME = "WebSocket Status"
        private const val CHANNEL_DESCRIPTION = "Shows WebSocket connection status and client count"
        
        // Action to start the service
        const val ACTION_START = "relanto.jpn.nrf.START_WEBSOCKET_SERVICE"
        const val ACTION_STOP = "relanto.jpn.nrf.STOP_WEBSOCKET_SERVICE"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WebSocket Notification Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Initializing...", 0))
        
        // Observe WebSocket states and update notification
        observeWebSocketStates()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WebSocket Notification Service started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Starting WebSocket service")
                // Service is already started, just update notification
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stopping WebSocket service")
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Default action - maintain service
                Log.d(TAG, "Maintaining WebSocket service")
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WebSocket Notification Service destroyed")
        
        // Don't stop WebSocket connections when service is destroyed
        // They should continue running in the background
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String, clientCount: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, WebSocketNotificationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = if (clientCount > 0) {
            "$status • $clientCount client(s) connected"
        } else {
            status
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JPMPOS Blade WebSocket")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop Service",
                stopPendingIntent
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun observeWebSocketStates() {
        // Observe server state
        scope.launch {
            unifiedWebSocketService.serverState.collectLatest { serverState ->
                val status = when (serverState) {
                    is ServerState.Running -> "Server Running"
                    is ServerState.Starting -> "Server Starting..."
                    is ServerState.Error -> "Server Error"
                    else -> "Server Stopped"
                }
                
                val clientCount = unifiedWebSocketService.connectedClients.value
                updateNotification(status, clientCount)
                
                // Update session manager with connection status
                if (serverState is ServerState.Running) {
                    sessionManager.updateConnectionStatus(true)
                } else if (serverState is ServerState.Stopped) {
                    sessionManager.updateConnectionStatus(false)
                }
            }
        }
        
        // Observe client state
        scope.launch {
            unifiedWebSocketService.clientState.collectLatest { clientState ->
                val status = when (clientState) {
                    is ClientState.Connected -> "Client Connected"
                    is ClientState.Connecting -> "Client Connecting..."
                    is ClientState.Error -> "Client Error"
                    else -> "Client Disconnected"
                }
                
                val clientCount = 0 // Client mode doesn't have connected clients
                updateNotification(status, clientCount)
                
                // Update session manager with connection status
                if (clientState is ClientState.Connected) {
                    sessionManager.updateConnectionStatus(true)
                } else if (clientState is ClientState.Disconnected) {
                    sessionManager.updateConnectionStatus(false)
                }
            }
        }
        
        // Observe connected clients count
        scope.launch {
            unifiedWebSocketService.connectedClients.collectLatest { count ->
                val currentStatus = when {
                    unifiedWebSocketService.serverState.value is ServerState.Running -> "Server Running"
                    unifiedWebSocketService.clientState.value is ClientState.Connected -> "Client Connected"
                    else -> "WebSocket Active"
                }
                updateNotification(currentStatus, count)
            }
        }
    }
    
    private fun updateNotification(status: String, clientCount: Int) {
        val notification = createNotification(status, clientCount)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
