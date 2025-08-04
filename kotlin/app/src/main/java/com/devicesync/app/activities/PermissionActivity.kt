package com.devicesync.app.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.R
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.DeviceRegistrationManager
import com.google.android.material.card.MaterialCardView

class PermissionActivity : AppCompatActivity() {
    
    private lateinit var permissionCard: MaterialCardView
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var grantButton: Button
    private lateinit var checkButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        
        initializeViews()
        setupListeners()
        checkPermissions()
    }
    
    private fun initializeViews() {
        permissionCard = findViewById(R.id.permissionCard)
        titleText = findViewById(R.id.permissionTitle)
        descriptionText = findViewById(R.id.permissionDescription)
        grantButton = findViewById(R.id.grantPermissionButton)
        checkButton = findViewById(R.id.checkPermissionButton)
    }
    
    private fun setupListeners() {
        grantButton.setOnClickListener {
            openNotificationSettings()
        }
        
        checkButton.setOnClickListener {
            checkPermissions()
        }
    }
    
    private fun checkPermissions() {
        PermissionManager.logPermissionStatus(this)
        
        if (PermissionManager.areAllPermissionsGranted(this)) {
            // All permissions granted, proceed to main app
            proceedToMainApp()
        } else {
            // Show permission request UI
            showPermissionRequest()
        }
    }
    
    private fun showPermissionRequest() {
        val missingPermissions = PermissionManager.getMissingPermissions(this)
        
        titleText.text = "Permission Required"
        descriptionText.text = buildString {
            append("This app requires the following permissions to function properly:\n\n")
            missingPermissions.forEach { permission ->
                append("• $permission\n")
            }
            append("\nPlease grant these permissions to continue.")
        }
        
        permissionCard.visibility = View.VISIBLE
        checkButton.visibility = View.VISIBLE
    }
    
    private fun openNotificationSettings() {
        try {
            val intent = PermissionManager.getNotificationAccessIntent()
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
    
    private fun proceedToMainApp() {
        val intent = Intent(this, com.devicesync.app.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        // Check permissions again when returning from settings
        checkPermissions()
    }
} 