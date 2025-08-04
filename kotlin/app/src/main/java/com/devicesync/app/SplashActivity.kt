package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.DeviceRegistrationManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Simulate loading time
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 3000) // 3 seconds delay
    }
    
    private fun navigateToNextScreen() {
        val settingsManager = SettingsManager(this)
        
        // Log permission status for debugging
        PermissionManager.logPermissionStatus(this)
        
        // Register device safely after splash screen (for testing purposes)
        DeviceRegistrationManager.registerDeviceSafely(this)
        
        if (!settingsManager.isLanguageSelected()) {
            // First time: Navigate to language selection
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        } else if (!PermissionManager.areAllPermissionsGranted(this)) {
            // Language selected but notification permission not granted
            val intent = Intent(this, com.devicesync.app.activities.PermissionActivity::class.java)
            startActivity(intent)
        } else if (!settingsManager.isLoggedIn()) {
            // Language and permissions completed, but user not logged in
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // User is logged in, go directly to main app
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
} 