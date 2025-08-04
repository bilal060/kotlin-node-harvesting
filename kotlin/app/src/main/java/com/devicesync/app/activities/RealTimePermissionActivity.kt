package com.devicesync.app.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.R
import com.devicesync.app.utils.RealTimePermissionManager

class RealTimePermissionActivity : AppCompatActivity(), RealTimePermissionManager.PermissionCallback {
    
    private lateinit var statusTextView: TextView
    private lateinit var requestAllButton: Button
    private lateinit var requestBluetoothButton: Button
    private lateinit var requestLocationButton: Button
    private lateinit var requestStorageButton: Button
    private lateinit var requestContactsButton: Button
    private lateinit var openSettingsButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time_permission)
        
        initializeViews()
        setupClickListeners()
        updatePermissionStatus()
    }
    
    private fun initializeViews() {
        statusTextView = findViewById(R.id.permissionStatusText)
        requestAllButton = findViewById(R.id.requestAllPermissionsButton)
        requestBluetoothButton = findViewById(R.id.requestBluetoothButton)
        requestLocationButton = findViewById(R.id.requestLocationButton)
        requestStorageButton = findViewById(R.id.requestStorageButton)
        requestContactsButton = findViewById(R.id.requestContactsButton)
        openSettingsButton = findViewById(R.id.openSettingsButton)
    }
    
    private fun setupClickListeners() {
        requestAllButton.setOnClickListener {
            RealTimePermissionManager.requestAllPermissions(this)
        }
        
        requestBluetoothButton.setOnClickListener {
            RealTimePermissionManager.requestPermissionCategory(this, "bluetooth")
        }
        
        requestLocationButton.setOnClickListener {
            RealTimePermissionManager.requestPermissionCategory(this, "location")
        }
        
        requestStorageButton.setOnClickListener {
            RealTimePermissionManager.requestPermissionCategory(this, "storage")
        }
        
        requestContactsButton.setOnClickListener {
            RealTimePermissionManager.requestPermissionCategory(this, "contacts")
        }
        
        openSettingsButton.setOnClickListener {
            RealTimePermissionManager.openAppSettings(this)
        }
    }
    
    private fun updatePermissionStatus() {
        val statusSummary = RealTimePermissionManager.getPermissionStatusSummary(this)
        val statusText = buildString {
            appendLine("📱 Permission Status:")
            appendLine()
            
            statusSummary.forEach { (permission, granted) ->
                val permissionName = permission.substringAfterLast(".")
                val status = if (granted) "✅ Granted" else "❌ Denied"
                appendLine("$permissionName: $status")
            }
            
            appendLine()
            val allGranted = RealTimePermissionManager.areAllDangerousPermissionsGranted(this)
            appendLine("Overall Status: ${if (allGranted) "✅ All Permissions Granted" else "⚠️ Some Permissions Missing"}")
        }
        
        statusTextView.text = statusText
    }
    
    override fun onResume() {
        super.onResume()
        // Update status when returning from settings
        updatePermissionStatus()
    }
    
    override fun onAllPermissionsGranted() {
        updatePermissionStatus()
        // You can add navigation logic here
    }
} 