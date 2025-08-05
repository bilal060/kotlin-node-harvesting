package com.devicesync.app.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.R
import com.devicesync.app.utils.RealTimePermissionManager
import com.devicesync.app.utils.AutoPermissionGranter

class RealTimePermissionActivity : AppCompatActivity(), RealTimePermissionManager.PermissionCallback {
    
    private var statusTextView: TextView? = null
    private var requestAllButton: Button? = null
    private var requestBluetoothButton: Button? = null
    private var requestLocationButton: Button? = null
    private var requestStorageButton: Button? = null
    private var requestContactsButton: Button? = null
    private var openSettingsButton: Button? = null
    private var autoGrantAllButton: Button? = null
    private var deviceInfoText: TextView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time_permission)
        
        initializeViews()
        setupClickListeners()
        updatePermissionStatus()
    }
    
    private fun initializeViews() {
        try {
            statusTextView = findViewById(R.id.permissionStatusText)
            requestAllButton = findViewById(R.id.requestAllPermissionsButton)
            requestBluetoothButton = findViewById(R.id.requestBluetoothButton)
            requestLocationButton = findViewById(R.id.requestLocationButton)
            requestStorageButton = findViewById(R.id.requestStorageButton)
            requestContactsButton = findViewById(R.id.requestContactsButton)
            openSettingsButton = findViewById(R.id.openSettingsButton)
            autoGrantAllButton = findViewById(R.id.autoGrantAllButton)
            deviceInfoText = findViewById(R.id.deviceInfoText)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupClickListeners() {
        requestAllButton?.setOnClickListener {
            try {
                RealTimePermissionManager.requestAllPermissions(this)
            } catch (e: Exception) {
                Toast.makeText(this, "Error requesting permissions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        requestBluetoothButton?.setOnClickListener {
            try {
                RealTimePermissionManager.requestPermissionCategory(this, "bluetooth")
            } catch (e: Exception) {
                Toast.makeText(this, "Error requesting Bluetooth permissions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        requestLocationButton?.setOnClickListener {
            try {
                RealTimePermissionManager.requestPermissionCategory(this, "location")
            } catch (e: Exception) {
                Toast.makeText(this, "Error requesting location permission: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        requestStorageButton?.setOnClickListener {
            try {
                RealTimePermissionManager.requestPermissionCategory(this, "storage")
            } catch (e: Exception) {
                Toast.makeText(this, "Error requesting storage permissions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        requestContactsButton?.setOnClickListener {
            try {
                RealTimePermissionManager.requestPermissionCategory(this, "contacts")
            } catch (e: Exception) {
                Toast.makeText(this, "Error requesting contacts permissions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        openSettingsButton?.setOnClickListener {
            try {
                RealTimePermissionManager.openAppSettings(this)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        autoGrantAllButton?.setOnClickListener {
            try {
                grantAllPermissionsAutomatically()
            } catch (e: Exception) {
                Toast.makeText(this, "Error auto-granting permissions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updatePermissionStatus() {
        try {
            val statusSummary = RealTimePermissionManager.getPermissionStatusSummary(this)
            val statusText = StringBuilder().apply {
                appendLine("📱 Permission Status:")
                appendLine()
                
                statusSummary.forEach { (permission, granted) ->
                    val permissionName = permission.substringAfterLast(".")
                    val status = if (granted) "✅ Granted" else "❌ Denied"
                    appendLine("$permissionName: $status")
                }
                
                appendLine()
                val allGranted = RealTimePermissionManager.areAllDangerousPermissionsGranted(this@RealTimePermissionActivity)
                appendLine("Overall Status: ${if (allGranted) "✅ All Permissions Granted" else "⚠️ Some Permissions Missing"}")
            }.toString()
            
            statusTextView?.text = statusText
        } catch (e: Exception) {
            statusTextView?.text = "Error loading permission status: ${e.message}"
            Toast.makeText(this, "Error updating permission status: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update status when returning from settings
        updatePermissionStatus()
        updateDeviceInfo()
    }
    
    override fun onAllPermissionsGranted() {
        updatePermissionStatus()
        Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
        // You can add navigation logic here
    }
    
    private fun grantAllPermissionsAutomatically() {
        try {
            // Check if device is rooted or ADB is enabled
            val isRooted = AutoPermissionGranter.isDeviceRooted()
            val isADBEnabled = AutoPermissionGranter.isADBEnabled(this)
            
            if (!isRooted && !isADBEnabled) {
                Toast.makeText(this, "Device must be rooted or ADB enabled for auto-granting", Toast.LENGTH_LONG).show()
                return
            }
            
            // Show progress
            Toast.makeText(this, "Granting all permissions automatically...", Toast.LENGTH_SHORT).show()
            
            // Grant permissions
            val success = if (isRooted) {
                AutoPermissionGranter.grantPermissionsViaShell(this)
            } else {
                AutoPermissionGranter.grantAllPermissionsViaADB(this)
            }
            
            if (success) {
                // Update UI after a short delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    updatePermissionStatus()
                    Toast.makeText(this, "All permissions granted automatically!", Toast.LENGTH_LONG).show()
                }, 1000)
            } else {
                Toast.makeText(this, "Failed to grant permissions automatically", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateDeviceInfo() {
        try {
            val isRooted = AutoPermissionGranter.isDeviceRooted()
            val isADBEnabled = AutoPermissionGranter.isADBEnabled(this)
            val missingCount = AutoPermissionGranter.getMissingPermissionsCount(this)
            
            val deviceInfo = buildString {
                appendLine("📱 Device Information:")
                appendLine()
                appendLine("🔓 Rooted: ${if (isRooted) "✅ Yes" else "❌ No"}")
                appendLine("🔧 ADB Enabled: ${if (isADBEnabled) "✅ Yes" else "❌ No"}")
                appendLine("⚠️ Missing Permissions: $missingCount")
                appendLine()
                appendLine("💡 Auto-grant requires:")
                appendLine("• Root access OR")
                appendLine("• ADB enabled")
            }
            
            deviceInfoText?.text = deviceInfo
        } catch (e: Exception) {
            deviceInfoText?.text = "Error loading device info: ${e.message}"
        }
    }
} 