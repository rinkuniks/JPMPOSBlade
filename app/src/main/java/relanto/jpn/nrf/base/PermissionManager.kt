package relanto.jpn.nrf.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Comprehensive permission manager for handling all runtime permissions
 * in the JPMPOSBlade application.
 */
class PermissionManager(private val activity: FragmentActivity) {
    
    // Permission states
    private val _permissionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionStates: StateFlow<Map<String, Boolean>> = _permissionStates.asStateFlow()
    
    // Permission launchers
    private val notificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updatePermissionState(Manifest.permission.POST_NOTIFICATIONS, isGranted)
        onPermissionResult(Manifest.permission.POST_NOTIFICATIONS, isGranted)
    }
    
    private val multiplePermissionsLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            updatePermissionState(permission, isGranted)
            onPermissionResult(permission, isGranted)
        }
    }
    
    // Permission rationale dialog launcher
    private val rationaleDialogLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User clicked OK on rationale dialog, request permission again
            requestNotificationPermission()
        }
    }
    
    init {
        initializePermissionStates()
    }
    
    /**
     * Initialize permission states for all required permissions
     */
    private fun initializePermissionStates() {
        val permissions = getRequiredPermissions()
        val states = permissions.associateWith { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
        _permissionStates.value = states
    }
    
    /**
     * Get all required permissions for the app
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        // Network permissions (always required)
        permissions.add(Manifest.permission.INTERNET)
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE)
        
        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Optional permissions that enhance functionality
        permissions.add(Manifest.permission.CHANGE_WIFI_STATE)
        permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        permissions.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
        
        return permissions
    }
    
    /**
     * Check if a specific permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if all critical permissions are granted
     */
    fun areCriticalPermissionsGranted(): Boolean {
        val criticalPermissions = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )
        
        return criticalPermissions.all { isPermissionGranted(it) }
    }
    
    /**
     * Check if all permissions are granted
     */
    fun areAllPermissionsGranted(): Boolean {
        return getRequiredPermissions().all { isPermissionGranted(it) }
    }
    
    /**
     * Request notification permission with proper handling
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Permission not required for this Android version
            updatePermissionState(Manifest.permission.POST_NOTIFICATIONS, true)
            return
        }
        
        when {
            isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Permission already granted
                updatePermissionState(Manifest.permission.POST_NOTIFICATIONS, true)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale dialog
                showNotificationPermissionRationale()
            }
            else -> {
                // Request permission directly
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    /**
     * Request multiple permissions at once
     */
    fun requestMultiplePermissions(permissions: List<String>) {
        val permissionsToRequest = permissions.filter { !isPermissionGranted(it) }
        if (permissionsToRequest.isNotEmpty()) {
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    /**
     * Request all missing permissions
     */
    fun requestAllMissingPermissions() {
        val missingPermissions = getRequiredPermissions().filter { !isPermissionGranted(it) }
        if (missingPermissions.isNotEmpty()) {
            multiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    /**
     * Show notification permission rationale dialog
     */
    private fun showNotificationPermissionRationale() {
        val intent = Intent(activity, PermissionRationaleActivity::class.java).apply {
            putExtra("permission", Manifest.permission.POST_NOTIFICATIONS)
            putExtra("title", "Notification Permission Required")
            putExtra("message", "This app needs notification permission to show WebSocket connection status and important updates. Please grant this permission to ensure the best user experience.")
        }
        rationaleDialogLauncher.launch(intent)
    }
    
    /**
     * Check if permission rationale should be shown
     */
    private fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.shouldShowRequestPermissionRationale(permission)
        } else {
            false
        }
    }
    
    /**
     * Update permission state
     */
    private fun updatePermissionState(permission: String, isGranted: Boolean) {
        val currentStates = _permissionStates.value.toMutableMap()
        currentStates[permission] = isGranted
        _permissionStates.value = currentStates
    }
    
    /**
     * Handle permission result
     */
    private fun onPermissionResult(permission: String, isGranted: Boolean) {
        when (permission) {
            Manifest.permission.POST_NOTIFICATIONS -> {
                if (isGranted) {
                    // Notification permission granted, can start notification service
                    onNotificationPermissionGranted()
                } else {
                    // Notification permission denied, show explanation
                    onNotificationPermissionDenied()
                }
            }
            // Handle other permissions as needed
        }
    }
    
    /**
     * Called when notification permission is granted
     */
    private fun onNotificationPermissionGranted() {
        // Start notification service or update UI
        // This will be handled by the WebSocket service
    }
    
    /**
     * Called when notification permission is denied
     */
    private fun onNotificationPermissionDenied() {
        // Show explanation and guide user to settings
        showPermissionDeniedDialog()
    }
    
    /**
     * Show dialog when permission is denied
     */
    private fun showPermissionDeniedDialog() {
        val intent = Intent(activity, PermissionDeniedActivity::class.java).apply {
            putExtra("permission", Manifest.permission.POST_NOTIFICATIONS)
            putExtra("title", "Permission Denied")
            putExtra("message", "Notification permission is required for this app to function properly. Please enable it in Settings.")
        }
        activity.startActivity(intent)
    }
    
    /**
     * Open app settings for manual permission granting
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }
    
    /**
     * Get permission display name
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.INTERNET -> "Internet Access"
            Manifest.permission.ACCESS_NETWORK_STATE -> "Network State"
            Manifest.permission.ACCESS_WIFI_STATE -> "WiFi State"
            Manifest.permission.CHANGE_WIFI_STATE -> "WiFi Control"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.FOREGROUND_SERVICE -> "Foreground Service"
            Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC -> "Data Sync Service"
            else -> permission
        }
    }
    
    /**
     * Get permission description
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.INTERNET -> "Required for WebSocket communication"
            Manifest.permission.ACCESS_NETWORK_STATE -> "Required to check network connectivity"
            Manifest.permission.ACCESS_WIFI_STATE -> "Required to detect WiFi network status"
            Manifest.permission.CHANGE_WIFI_STATE -> "Optional: Allows WiFi network management"
            Manifest.permission.POST_NOTIFICATIONS -> "Required for connection status notifications"
            Manifest.permission.FOREGROUND_SERVICE -> "Required for WebSocket service"
            Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC -> "Required for data synchronization"
            else -> "Required for app functionality"
        }
    }
}
