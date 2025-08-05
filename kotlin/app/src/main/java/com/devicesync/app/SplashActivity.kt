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
import com.devicesync.app.utils.DeviceRegistrationManager

class SplashActivity : AppCompatActivity(), RealTimePermissionManager.PermissionCallback {

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
            
            // Simple test - just navigate immediately without complex logic
            Handler(Looper.getMainLooper()).postDelayed({
                android.util.Log.d("SplashActivity", "Handler delayed task executing")
                // Navigate to LoginActivity directly for testing
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000) // 1 second delay for testing
            
            android.util.Log.d("SplashActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error in onCreate", e)
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
            // Use the new RealTimePermissionManager to show default Android popups
            RealTimePermissionManager.requestAllPermissions(this)
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
            // Use the new RealTimePermissionManager to handle results
            RealTimePermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error handling permission results", e)
            // Fallback to proceeding
            proceedToNextScreenAfterPermissions()
        }
    }
    
    // Implement the PermissionCallback interface
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
    
    override fun onResume() {
        super.onResume()
        // Check if we're returning from settings and permissions are now granted
        if (PermissionManager.areAllPermissionsGranted(this)) {
            proceedToNextScreenAfterPermissions()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Dismiss dialog to prevent window leak
        permissionDialog?.dismiss()
        permissionDialog = null
    }
} 