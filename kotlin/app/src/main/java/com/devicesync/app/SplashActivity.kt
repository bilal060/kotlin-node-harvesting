package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.utils.PermissionManager

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
    
    private lateinit var permissionManager: PermissionManager
    private lateinit var permissionStatusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Initialize UI elements
        permissionStatusText = findViewById(R.id.permissionStatus)
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)
        
        permissionManager = PermissionManager(this) { allGranted ->
            Handler(Looper.getMainLooper()).postDelayed({
                if (allGranted) {
                    // All permissions granted, go to main activity
                    updateStatus("All permissions granted!", 100)
                    loadingText.text = "Starting app..."
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // Some permissions missing, go to login/signup
                    updateStatus("Some permissions missing", 100)
                    loadingText.text = "Continuing with limited functionality..."
                    startActivity(Intent(this, AuthActivity::class.java))
                }
                finish()
            }, SPLASH_DELAY)
        }
        
        // Start permission check
        checkPermissions()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle result from settings page
        permissionManager.handleActivityResult(requestCode, resultCode, data)
    }
    
    private fun checkPermissions() {
        updateStatus("Requesting permissions...", 25)
        loadingText.text = "Please grant the required permissions"
        
        // Simulate progress
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Checking system permissions...", 50)
        }, 1000)
        
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Finalizing setup...", 75)
        }, 2000)
        
        permissionManager.requestAllPermissions()
    }
    
    private fun updateStatus(status: String, progress: Int) {
        permissionStatusText.text = status
        progressBar.progress = progress
    }
} 