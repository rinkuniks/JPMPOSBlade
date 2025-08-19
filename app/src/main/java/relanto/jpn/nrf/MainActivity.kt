package relanto.jpn.nrf

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import relanto.jpn.nrf.navigation.NavGraph
import relanto.jpn.nrf.ui.theme.JPMPOSBladeTheme
import relanto.jpn.nrf.websocket.WebSocketSessionManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	
	@Inject
	lateinit var sessionManager: WebSocketSessionManager
	
	private val requestPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if (isGranted) {
			// Permission granted, notification service can run
		} else {
			// Permission denied, show message or handle accordingly
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		
		// Request notification permission for Android 13+
		requestNotificationPermission()
		
		// Restore WebSocket session if app was reopened
		restoreWebSocketSession()
		
		setContent {
			JPMPOSBladeTheme {
				val navController = rememberNavController()
				NavGraph(navController = navController)
			}
		}
	}
	
	override fun onResume() {
		super.onResume()
		// Check if session should be restored when app comes to foreground
		if (sessionManager.shouldMaintainSession()) {
			// Session will be restored by the ViewModel
		}
	}
	
	override fun onDestroy() {
		super.onDestroy()
		// Note: We don't clear the session here as we want to maintain it
		// The session will be cleared only when explicitly stopped by the user
	}
	
	private fun requestNotificationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			when {
				ContextCompat.checkSelfPermission(
					this,
					Manifest.permission.POST_NOTIFICATIONS
				) == PackageManager.PERMISSION_GRANTED -> {
					// Permission already granted
				}
				shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
					// Show rationale if needed
				}
				else -> {
					// Request permission
					requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
				}
			}
		}
	}
	
	private fun restoreWebSocketSession() {
		// The session restoration will be handled by the ViewModel
		// This is just a placeholder for any additional setup if needed
	}
}
