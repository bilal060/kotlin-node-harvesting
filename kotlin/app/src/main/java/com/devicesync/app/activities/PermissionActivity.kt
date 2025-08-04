package com.devicesync.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.R
import com.devicesync.app.utils.PermissionManager
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
        
        // Automatically open notification settings after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (!PermissionManager.areAllPermissionsGranted(this)) {
                showPermissionDialog()
            }
        }, 2000) // 2 seconds delay to let user read the screen
        
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
            showPermissionDialog()
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
            append("Dubai Discoveries needs the following permissions to provide you with the best experience:\n\n")
            missingPermissions.forEach { permission ->
                append("• $permission\n")
            }
            append("\nTap 'Grant Permissions' to open the settings page for each permission.")
        }
        
        permissionCard.visibility = View.VISIBLE
        checkButton.visibility = View.VISIBLE
    }
    
    private fun showPermissionDialog() {
        val missingPermissions = PermissionManager.getMissingPermissions(this)
        
        if (missingPermissions.isEmpty()) {
            proceedToMainApp()
            return
        }
        
        val dialogBuilder = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
        dialogBuilder.setTitle("Permissions Required")
        
        val message = buildString {
            append("Dubai Discoveries needs the following permissions:\n\n")
            missingPermissions.forEach { permission ->
                append("• $permission\n")
            }
            append("\nWould you like to grant all permissions at once?")
        }
        
        dialogBuilder.setMessage(message)
        
        dialogBuilder.setPositiveButton("Grant All Permissions") { _, _ ->
            openAllPermissionSettings()
        }
        
        dialogBuilder.setNegativeButton("Grant Individually") { _, _ ->
            showIndividualPermissionDialogs(missingPermissions)
        }
        
        dialogBuilder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        dialogBuilder.setCancelable(false)
        dialogBuilder.show()
    }
    
    private fun showIndividualPermissionDialogs(permissions: List<String>) {
        if (permissions.isEmpty()) {
            proceedToMainApp()
            return
        }
        
        val currentPermission = permissions[0]
        val remainingPermissions = permissions.drop(1)
        
        val dialogBuilder = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
        dialogBuilder.setTitle("Permission Required: $currentPermission")
        
        val message = when (currentPermission.lowercase()) {
            "notification access" -> "This permission allows Dubai Discoveries to show you relevant notifications and updates."
            "contacts" -> "This permission allows Dubai Discoveries to sync your contacts for better connectivity features."
            "call logs" -> "This permission allows Dubai Discoveries to provide call-related features and analytics."
            "phone state" -> "This permission allows Dubai Discoveries to detect phone state changes for better app functionality."
            else -> "This permission is required for Dubai Discoveries to function properly."
        }
        
        dialogBuilder.setMessage(message)
        
        dialogBuilder.setPositiveButton("Grant Permission") { _, _ ->
            openSpecificPermissionSettings(currentPermission)
            // Show next permission dialog after a delay
            Handler(Looper.getMainLooper()).postDelayed({
                showIndividualPermissionDialogs(remainingPermissions)
            }, 1000)
        }
        
        dialogBuilder.setNegativeButton("Skip") { _, _ ->
            // Show next permission dialog
            showIndividualPermissionDialogs(remainingPermissions)
        }
        
        dialogBuilder.setCancelable(false)
        dialogBuilder.show()
    }
    
    private fun openAllPermissionSettings() {
        try {
            // Open app settings where user can grant all permissions
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
    
    private fun openSpecificPermissionSettings(permissionType: String) {
        try {
            val intent = PermissionManager.getPermissionIntent(permissionType)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }
    
    private fun openNotificationSettings() {
        try {
            val intent = PermissionManager.getNotificationAccessIntent()
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app settings
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(fallbackIntent)
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