package com.devicesync.app.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.utils.ComprehensivePermissionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Comprehensive Permission Activity
 * Shows all permissions with individual accept/reject buttons
 * Uses default Android permission popups
 */
class ComprehensivePermissionActivity : AppCompatActivity(), ComprehensivePermissionManager.PermissionCallback {
    
    private var statusTextView: TextView? = null
    private var summaryCardView: MaterialCardView? = null
    private var grantedCountText: TextView? = null
    private var deniedCountText: TextView? = null
    private var totalCountText: TextView? = null
    private var permissionRecyclerView: RecyclerView? = null
    private var requestAllButton: MaterialButton? = null
    private var requestByCategoryButton: MaterialButton? = null
    private var openSettingsButton: MaterialButton? = null
    private var proceedButton: MaterialButton? = null
    private var deviceInfoText: TextView? = null
    
    private lateinit var permissionAdapter: PermissionAdapter
    
    companion object {
        private const val TAG = "ComprehensivePermissionActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprehensive_permission)
        
        try {
            initializeViews()
            setupRecyclerView()
            setupClickListeners()
            updatePermissionStatus()
            updateDeviceInfo()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initializeViews() {
        try {
            statusTextView = findViewById(R.id.statusTextView)
            summaryCardView = findViewById(R.id.summaryCardView)
            grantedCountText = findViewById(R.id.grantedCountText)
            deniedCountText = findViewById(R.id.deniedCountText)
            totalCountText = findViewById(R.id.totalCountText)
            permissionRecyclerView = findViewById(R.id.permissionRecyclerView)
            requestAllButton = findViewById(R.id.requestAllButton)
            requestByCategoryButton = findViewById(R.id.requestByCategoryButton)
            openSettingsButton = findViewById(R.id.openSettingsButton)
            proceedButton = findViewById(R.id.proceedButton)
            deviceInfoText = findViewById(R.id.deviceInfoText)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        try {
            permissionAdapter = PermissionAdapter { permission ->
                requestSinglePermission(permission)
            }
            
            permissionRecyclerView?.apply {
                layoutManager = LinearLayoutManager(this@ComprehensivePermissionActivity)
                adapter = permissionAdapter
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }
    
    private fun setupClickListeners() {
        try {
            requestAllButton?.setOnClickListener {
                try {
                    ComprehensivePermissionManager.requestAllPermissions(this, this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error requesting all permissions", e)
                    Toast.makeText(this, "Error requesting permissions", Toast.LENGTH_SHORT).show()
                }
            }
            
            requestByCategoryButton?.setOnClickListener {
                try {
                    showCategorySelectionDialog()
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing category dialog", e)
                    Toast.makeText(this, "Error showing categories", Toast.LENGTH_SHORT).show()
                }
            }
            
            openSettingsButton?.setOnClickListener {
                try {
                    openAppSettings()
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening settings", e)
                    Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show()
                }
            }
            
            proceedButton?.setOnClickListener {
                try {
                    proceedToMainApp()
                } catch (e: Exception) {
                    Log.e(TAG, "Error proceeding to main app", e)
                    Toast.makeText(this, "Error proceeding to main app", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }
    
    private fun updatePermissionStatus() {
        try {
            val permissionStatus = ComprehensivePermissionManager.getPermissionStatus(this)
            val grantedCount = permissionStatus.values.count { it }
            val deniedCount = permissionStatus.values.count { !it }
            val totalCount = permissionStatus.size
            
            // Update summary
            grantedCountText?.text = "✅ Granted: $grantedCount"
            deniedCountText?.text = "❌ Denied: $deniedCount"
            totalCountText?.text = "📱 Total: $totalCount"
            
            // Update adapter
            permissionAdapter.updatePermissions(permissionStatus)
            
            // Update status text
            val statusText = when {
                grantedCount == totalCount -> "🎉 All permissions granted!"
                grantedCount > totalCount / 2 -> "⚠️ Most permissions granted"
                else -> "⚠️ Many permissions denied"
            }
            statusTextView?.text = statusText
            
            // Show/hide proceed button
            proceedButton?.visibility = if (grantedCount > 0) View.VISIBLE else View.GONE
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating permission status", e)
        }
    }
    
    private fun updateDeviceInfo() {
        try {
            val deviceInfo = StringBuilder().apply {
                append("📱 Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
                append("🤖 Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n")
                append("📦 App Version: ${packageManager.getPackageInfo(packageName, 0).versionName}\n")
                append("🔧 Build: ${android.os.Build.VERSION.INCREMENTAL}")
            }.toString()
            
            deviceInfoText?.text = deviceInfo
        } catch (e: Exception) {
            Log.e(TAG, "Error updating device info", e)
            deviceInfoText?.text = "Error loading device information"
        }
    }
    
    private fun requestSinglePermission(permission: String) {
        try {
            ComprehensivePermissionManager.requestSinglePermission(this, permission, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting single permission: $permission", e)
            Toast.makeText(this, "Error requesting permission", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showCategorySelectionDialog() {
        try {
            val categories = ComprehensivePermissionManager.PERMISSION_CATEGORIES.keys.toTypedArray()
            
            android.app.AlertDialog.Builder(this)
                .setTitle("Select Permission Category")
                .setItems(categories) { _, which ->
                    val selectedCategory = categories[which]
                    ComprehensivePermissionManager.requestPermissionsByCategory(this, selectedCategory, this)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing category dialog", e)
        }
    }
    
    private fun openAppSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings", e)
            Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun proceedToMainApp() {
        try {
            val intent = Intent(this, com.devicesync.app.MainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error proceeding to main app", e)
            Toast.makeText(this, "Error opening main app", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            ComprehensivePermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling permission result", e)
        }
    }
    
    // PermissionCallback implementations
    override fun onPermissionGranted(permission: String) {
        try {
            Log.d(TAG, "Permission granted: $permission")
            Toast.makeText(this, "✅ ${ComprehensivePermissionManager.getPermissionDisplayName(permission)} granted", Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling permission granted", e)
        }
    }
    
    override fun onPermissionDenied(permission: String) {
        try {
            Log.d(TAG, "Permission denied: $permission")
            Toast.makeText(this, "❌ ${ComprehensivePermissionManager.getPermissionDisplayName(permission)} denied", Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling permission denied", e)
        }
    }
    
    override fun onPermissionPermanentlyDenied(permission: String) {
        try {
            Log.d(TAG, "Permission permanently denied: $permission")
            Toast.makeText(this, "🚫 ${ComprehensivePermissionManager.getPermissionDisplayName(permission)} permanently denied. Please enable in settings.", Toast.LENGTH_LONG).show()
            updatePermissionStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling permission permanently denied", e)
        }
    }
    
    override fun onAllPermissionsGranted() {
        try {
            Log.d(TAG, "All permissions granted")
            Toast.makeText(this, "🎉 All permissions granted!", Toast.LENGTH_LONG).show()
            updatePermissionStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling all permissions granted", e)
        }
    }
    
    override fun onSomePermissionsDenied(deniedPermissions: List<String>) {
        try {
            Log.d(TAG, "Some permissions denied: $deniedPermissions")
            Toast.makeText(this, "⚠️ ${deniedPermissions.size} permissions denied", Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling some permissions denied", e)
        }
    }
    
    /**
     * RecyclerView Adapter for permissions
     */
    private inner class PermissionAdapter(
        private val onPermissionClick: (String) -> Unit
    ) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {
        
        private var permissions: Map<String, Boolean> = emptyMap()
        
        fun updatePermissions(newPermissions: Map<String, Boolean>) {
            permissions = newPermissions
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_permission, parent, false)
            return PermissionViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
            val permission = ComprehensivePermissionManager.ALL_DANGEROUS_PERMISSIONS[position]
            val isGranted = permissions[permission] ?: false
            holder.bind(permission, isGranted)
        }
        
        override fun getItemCount(): Int = ComprehensivePermissionManager.ALL_DANGEROUS_PERMISSIONS.size
        
        inner class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val permissionNameText: TextView = itemView.findViewById(R.id.permissionNameText)
            private val permissionCategoryText: TextView = itemView.findViewById(R.id.permissionCategoryText)
            private val permissionStatusText: TextView = itemView.findViewById(R.id.permissionStatusText)
            private val requestButton: MaterialButton = itemView.findViewById(R.id.requestButton)
            private val permissionCard: MaterialCardView = itemView.findViewById(R.id.permissionCard)
            
            fun bind(permission: String, isGranted: Boolean) {
                try {
                    val displayName = ComprehensivePermissionManager.getPermissionDisplayName(permission)
                    val category = ComprehensivePermissionManager.getPermissionCategory(permission)
                    
                    permissionNameText.text = displayName
                    permissionCategoryText.text = category
                    
                    if (isGranted) {
                        permissionStatusText.text = "✅ Granted"
                        permissionStatusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                        requestButton.text = "Granted"
                        requestButton.isEnabled = false
                        permissionCard.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_light))
                    } else {
                        permissionStatusText.text = "❌ Denied"
                        permissionStatusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                        requestButton.text = "Request"
                        requestButton.isEnabled = true
                        permissionCard.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_light))
                    }
                    
                    requestButton.setOnClickListener {
                        onPermissionClick(permission)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error binding permission item", e)
                }
            }
        }
    }
} 