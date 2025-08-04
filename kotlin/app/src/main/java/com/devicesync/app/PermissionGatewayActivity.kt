package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.SettingsManager

class PermissionGatewayActivity : AppCompatActivity() {
    
    private lateinit var acceptButton: Button
    private lateinit var denyButton: Button
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var settingsManager: SettingsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_gateway)
        
        settingsManager = SettingsManager(this)
        
        // Check if permissions are already granted using PermissionManager
        if (PermissionManager.areAllPermissionsGranted(this) && settingsManager.arePermissionsGranted()) {
            // Permissions already granted, skip to main app
            navigateToMainApp()
            return
        }
        
        setupViews()
        setupClickListeners()
    }
    
    private fun setupViews() {
        acceptButton = findViewById(R.id.acceptButton)
        denyButton = findViewById(R.id.denyButton)
        titleText = findViewById(R.id.titleText)
        descriptionText = findViewById(R.id.descriptionText)
    }
    
    private fun setupClickListeners() {
        acceptButton.setOnClickListener {
            // Open notification settings to grant permission
            try {
                val intent = PermissionManager.getNotificationAccessIntent()
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general settings
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
        }
        
        denyButton.setOnClickListener {
            // User denied permissions, exit app
            finish()
        }
    }
    
    private fun showPermissionError() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Permissions Required")
            .setMessage("This app requires notification access to function properly. Please grant notification access to continue.")
            .setPositiveButton("Try Again") { _, _ ->
                // Open notification settings again
                try {
                    val intent = PermissionManager.getNotificationAccessIntent()
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                    startActivity(intent)
                }
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
} 