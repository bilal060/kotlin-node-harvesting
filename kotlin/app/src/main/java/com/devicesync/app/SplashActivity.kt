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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("SplashActivity", "onCreate started")
        
        try {
            setContentView(R.layout.activity_splash)
            android.util.Log.d("SplashActivity", "setContentView successful")
            
            // Dismiss any existing dialogs first
            permissionDialog?.dismiss()
            permissionDialog = null
            
            // Fetch static data and then navigate to MainActivity
            fetchStaticDataAndNavigate()
            
            android.util.Log.d("SplashActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error in onCreate", e)
            // Emergency fallback
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e2: Exception) {
                    android.util.Log.e("SplashActivity", "Emergency fallback failed", e2)
                    finish()
                }
            }, 1000)
        }
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
        val settingsManager = SettingsManager(this)
        
        if (!settingsManager.isLoggedIn()) {
            // Permissions completed, but user not logged in
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // User is logged in, go directly to main app
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        try {
            // Use comprehensive permission manager to handle results
            ComprehensivePermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults, this)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission results", e)
            // Fallback to proceeding
            proceedToNextScreenAfterPermissions()
        }
    }
    
    // Implement the RealTimePermissionManager.PermissionCallback interface
    override fun onAllPermissionsGranted() {
        try {
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
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission granted", e)
        }
    }
    
    override fun onPermissionDenied(permission: String) {
        try {
            android.util.Log.d("SplashActivity", "Permission denied: $permission")
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission denied", e)
        }
    }
    
    override fun onPermissionPermanentlyDenied(permission: String) {
        try {
            android.util.Log.d("SplashActivity", "Permission permanently denied: $permission")
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission permanently denied", e)
        }
    }
    
    override fun onSomePermissionsDenied(deniedPermissions: List<String>) {
        try {
            android.util.Log.d("SplashActivity", "Some permissions denied: $deniedPermissions")
            // Still proceed to next screen even if some permissions are denied
            proceedToNextScreenAfterPermissions()
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling some permissions denied", e)
            proceedToNextScreenAfterPermissions()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check if we're returning from settings and permissions are now granted
        if (PermissionManager.areAllPermissionsGranted(this)) {
            proceedToNextScreenAfterPermissions()
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
    }
    
    /**
     * Fetch static data from API and then navigate to MainActivity
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
                
                // Navigate to MainActivity regardless of fetch result
                navigateToMainActivity()
                
            } catch (e: Exception) {
                android.util.Log.e("SplashActivity", "❌ Error in fetchStaticDataAndNavigate: ${e.message}", e)
                // Navigate to MainActivity even if data fetch fails
                navigateToMainActivity()
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
                
                // Request only the permissions needed for allowed data types
                withContext(Dispatchers.Main) {
                    DynamicPermissionManager.requestRequiredPermissions(this@SplashActivity)
                }
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
} 