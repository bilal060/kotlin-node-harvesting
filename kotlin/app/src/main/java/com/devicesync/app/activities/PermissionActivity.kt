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
import com.google.android.material.appbar.MaterialToolbar

class PermissionActivity : AppCompatActivity() {
    
    private lateinit var permissionCard: MaterialCardView
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var grantButton: Button
    private lateinit var checkButton: Button
    private lateinit var toolbar: MaterialToolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        
        initializeViews()
        setupListeners()
        checkPermissions()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        permissionCard = findViewById(R.id.permissionCard)
        titleText = findViewById(R.id.permissionTitle)
        descriptionText = findViewById(R.id.permissionDescription)
        grantButton = findViewById(R.id.grantPermissionButton)
        checkButton = findViewById(R.id.checkPermissionButton)
        
        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun setupListeners() {
        grantButton.setOnClickListener {
            openNotificationSettings()
        }
        
        checkButton.setOnClickListener {
            checkPermissions()
        }
        
        // Setup toolbar back button
        toolbar.setNavigationOnClickListener {
            onBackPressed()
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
        
        titleText.text = "Permissions Required"
        descriptionText.text = buildString {
            append("This app needs the following permissions to provide you with the best experience:\n\n")
            missingPermissions.forEach { permission ->
                append("• $permission\n")
            }
            append("\nTap 'Grant Permission' to open the specific settings page for each permission.")
        }
        
        permissionCard.visibility = View.VISIBLE
        checkButton.visibility = View.VISIBLE
    }
    
    private fun openNotificationSettings() {
        try {
            val missingPermissions = PermissionManager.getMissingPermissions(this)
            if (missingPermissions.isNotEmpty()) {
                // Open the first missing permission's specific settings page
                val permissionType = missingPermissions[0]
                val intent = PermissionManager.getPermissionIntent(permissionType)
                startActivity(intent)
            } else {
                // Fallback to notification settings
                val intent = PermissionManager.getNotificationAccessIntent()
                startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback to app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
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