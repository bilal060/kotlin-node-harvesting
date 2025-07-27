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
                    // Critical permissions granted, check optional permissions
                    val permissionStatus = permissionManager.getPermissionStatus()
                    val hasSms = permissionManager.hasSmsPermission()
                    val hasContacts = permissionManager.hasContactsPermission()
                    val hasStorage = permissionManager.hasStoragePermission()
                    
                    if (hasSms && hasContacts && hasStorage) {
                        updateStatus("All permissions granted!", 100)
                        loadingText.text = "Starting app with full functionality..."
                    } else {
                        updateStatus("Limited functionality mode", 100)
                        loadingText.text = "Some features will be limited..."
                    }
                    
                    // Always proceed to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // Critical permissions denied, show error
                    updateStatus("Critical permissions required", 100)
                    loadingText.text = "Cannot start app without essential permissions"
                    // The permission manager will handle showing the error dialog
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
        updateStatus("Checking essential permissions...", 25)
        loadingText.text = "Setting up your Dubai tourism experience"
        
        // Simulate progress
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Requesting optional permissions...", 50)
            loadingText.text = "Enhancing your travel experience"
        }, 1000)
        
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Finalizing setup...", 75)
            loadingText.text = "Almost ready..."
        }, 2000)
        
        permissionManager.requestAllPermissions()
    }
    
    private fun updateStatus(status: String, progress: Int) {
        permissionStatusText.text = status
        progressBar.progress = progress
    }
} 