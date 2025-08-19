package relanto.jpn.nrf

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import relanto.jpn.nrf.base.PermissionManager
import relanto.jpn.nrf.navigation.NavGraph
import relanto.jpn.nrf.ui.theme.JPMPOSBladeTheme
import relanto.jpn.nrf.websocket.WebSocketSessionManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
	
	@Inject
	lateinit var sessionManager: WebSocketSessionManager
	
	private lateinit var permissionManager: PermissionManager
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		
		// Restore WebSocket session if app was reopened
		restoreWebSocketSession()
		
		setContent {
			JPMPOSBladeTheme {
				val navController = rememberNavController()
				NavGraph(navController = navController)
			}
		}
		
		// Initialize permission manager after UI is set up
		permissionManager = PermissionManager(this)
		
		// Request all required permissions
		requestAllRequiredPermissions()
	}
	
	override fun onResume() {
		super.onResume()
		
		// Check permissions when app comes to foreground
		checkPermissionsOnResume()
		
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
	
	/**
	 * Request all required permissions for the app
	 */
	private fun requestAllRequiredPermissions() {
		// Check if critical permissions are granted
		if (!permissionManager.areCriticalPermissionsGranted()) {
			// Request critical permissions first
			permissionManager.requestMultiplePermissions(
				listOf(
					Manifest.permission.INTERNET,
					Manifest.permission.ACCESS_NETWORK_STATE,
					Manifest.permission.ACCESS_WIFI_STATE
				)
			)
		}
		
		// Request notification permission for Android 13+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			permissionManager.requestNotificationPermission()
		}
		
		// Request optional permissions that enhance functionality
		val optionalPermissions = listOf(
			Manifest.permission.CHANGE_WIFI_STATE,
			Manifest.permission.FOREGROUND_SERVICE,
			Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
		)
		
		val missingOptionalPermissions = optionalPermissions.filter { 
			!permissionManager.isPermissionGranted(it) 
		}
		
		if (missingOptionalPermissions.isNotEmpty()) {
			permissionManager.requestMultiplePermissions(missingOptionalPermissions)
		}
	}
	
	/**
	 * Check permissions when app resumes
	 */
	private fun checkPermissionsOnResume() {
		// Check if any permissions were revoked while app was in background
		val currentPermissions = permissionManager.getRequiredPermissions()
		val revokedPermissions = currentPermissions.filter { 
			!permissionManager.isPermissionGranted(it) 
		}
		
		if (revokedPermissions.isNotEmpty()) {
			// Some permissions were revoked, show warning
			showPermissionRevokedWarning(revokedPermissions)
		}
	}
	
	/**
	 * Show warning when permissions are revoked
	 */
	private fun showPermissionRevokedWarning(revokedPermissions: List<String>) {
		// This will be handled by the UI layer showing a dialog
		// For now, we'll just log the warning
		val permissionNames = revokedPermissions.joinToString(", ") { permission ->
			permissionManager.getPermissionDisplayName(permission)
		}
		
		android.util.Log.w("MainActivity", "Permissions revoked: $permissionNames")
	}
	
	/**
	 * Check if specific permission is granted
	 */
	fun isPermissionGranted(permission: String): Boolean {
		return permissionManager.isPermissionGranted(permission)
	}
	
	/**
	 * Check if all critical permissions are granted
	 */
	fun areCriticalPermissionsGranted(): Boolean {
		return permissionManager.areCriticalPermissionsGranted()
	}
	
	/**
	 * Check if all permissions are granted
	 */
	fun areAllPermissionsGranted(): Boolean {
		return permissionManager.areAllPermissionsGranted()
	}
	
	/**
	 * Get permission manager instance
	 */
	fun getPermissionManager(): PermissionManager {
		return permissionManager
	}
	
	private fun restoreWebSocketSession() {
		// The session restoration will be handled by the ViewModel
		// This is just a placeholder for any additional setup if needed
	}
}
