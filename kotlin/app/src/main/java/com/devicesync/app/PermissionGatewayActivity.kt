package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.utils.PermissionManager

class PermissionGatewayActivity : AppCompatActivity() {
    
    private lateinit var acceptButton: Button
    private lateinit var denyButton: Button
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_gateway)
        
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
            // Request all permissions
            val permissionManager = PermissionManager(this) { allGranted ->
                if (allGranted) {
                    // All permissions granted, proceed to login
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Some permissions denied, show error and exit
                    showPermissionError()
                }
            }
            permissionManager.requestAllPermissions()
        }
        
        denyButton.setOnClickListener {
            // User denied permissions, exit app
            finish()
        }
    }
    
    private fun showPermissionError() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Permissions Required")
            .setMessage("This app requires all permissions to function properly. Please grant all permissions to continue.")
            .setPositiveButton("Try Again") { _, _ ->
                // Restart permission request
                val permissionManager = PermissionManager(this) { allGranted ->
                    if (allGranted) {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showPermissionError()
                    }
                }
                permissionManager.requestAllPermissions()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
} 