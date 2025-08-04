package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast

object RealTimePermissionManager {
    
    private const val TAG = "RealTimePermissionManager"
    
    // All required permissions for the app
    private val ALL_REQUIRED_PERMISSIONS = arrayOf(
        // Essential permissions
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        
        // Data collection permissions
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_PHONE_STATE,
        
        // Background service permissions
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC,
        
        // Bluetooth permissions (if available in manifest)
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        
        // Location permission
        Manifest.permission.ACCESS_FINE_LOCATION,
        
        // Storage permissions
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    
    // Dangerous permissions that require runtime request
    private val DANGEROUS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    
    // Permission request codes
    const val PERMISSION_REQUEST_CODE = 1001
    const val BLUETOOTH_PERMISSION_REQUEST = 1002
    const val LOCATION_PERMISSION_REQUEST = 1003
    const val STORAGE_PERMISSION_REQUEST = 1004
    
    /**
     * Request all permissions with default Android popups
     */
    fun requestAllPermissions(activity: Activity) {
        Log.d(TAG, "Requesting all permissions")
        
        val missingPermissions = getMissingDangerousPermissions(activity)
        
        if (missingPermissions.isEmpty()) {
            Log.d(TAG, "All dangerous permissions already granted")
            onAllPermissionsGranted(activity)
            return
        }
        
        // Show default Android permission popup
        ActivityCompat.requestPermissions(
            activity,
            missingPermissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Request specific permission category
     */
    fun requestPermissionCategory(activity: Activity, category: String) {
        when (category.lowercase()) {
            "bluetooth" -> requestBluetoothPermissions(activity)
            "location" -> requestLocationPermission(activity)
            "storage" -> requestStoragePermissions(activity)
            "contacts" -> requestContactsPermissions(activity)
            "notifications" -> requestNotificationPermission(activity)
            else -> requestAllPermissions(activity)
        }
    }
    
    /**
     * Request Bluetooth permissions
     */
    fun requestBluetoothPermissions(activity: Activity) {
        val bluetoothPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ).filter { isPermissionDeclared(activity, it) }
        
        if (bluetoothPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                bluetoothPermissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST
            )
        }
    }
    
    /**
     * Request Location permission
     */
    fun requestLocationPermission(activity: Activity) {
        if (isPermissionDeclared(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }
    
    /**
     * Request Storage permissions
     */
    fun requestStoragePermissions(activity: Activity) {
        val storagePermissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isPermissionDeclared(activity, Manifest.permission.READ_MEDIA_IMAGES)) {
                storagePermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (isPermissionDeclared(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                storagePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (storagePermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                storagePermissions.toTypedArray(),
                STORAGE_PERMISSION_REQUEST
            )
        }
    }
    
    /**
     * Request Contacts permissions
     */
    fun requestContactsPermissions(activity: Activity) {
        val contactsPermissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS
        ).filter { isPermissionDeclared(activity, it) }
        
        if (contactsPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                contactsPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Request Notification permission
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isPermissionDeclared(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * Check if permission is declared in manifest
     */
    private fun isPermissionDeclared(context: Context, permission: String): Boolean {
        return try {
            context.packageManager.getPermissionInfo(permission, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get missing dangerous permissions
     */
    fun getMissingDangerousPermissions(context: Context): List<String> {
        return DANGEROUS_PERMISSIONS.filter { permission ->
            isPermissionDeclared(context, permission) &&
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if all dangerous permissions are granted
     */
    fun areAllDangerousPermissionsGranted(context: Context): Boolean {
        return getMissingDangerousPermissions(context).isEmpty()
    }
    
    /**
     * Handle permission result
     */
    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                handleGeneralPermissionResult(activity, permissions, grantResults)
            }
            BLUETOOTH_PERMISSION_REQUEST -> {
                handleBluetoothPermissionResult(activity, permissions, grantResults)
            }
            LOCATION_PERMISSION_REQUEST -> {
                handleLocationPermissionResult(activity, permissions, grantResults)
            }
            STORAGE_PERMISSION_REQUEST -> {
                handleStoragePermissionResult(activity, permissions, grantResults)
            }
        }
    }
    
    private fun handleGeneralPermissionResult(
        activity: Activity,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        
        if (allGranted) {
            Log.d(TAG, "All general permissions granted")
            onAllPermissionsGranted(activity)
        } else {
            Log.w(TAG, "Some permissions were denied")
            showPermissionDeniedMessage(activity, "Some permissions were denied. App functionality may be limited.")
        }
    }
    
    private fun handleBluetoothPermissionResult(
        activity: Activity,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        
        if (allGranted) {
            Log.d(TAG, "Bluetooth permissions granted")
            Toast.makeText(activity, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "Bluetooth permissions denied")
            showPermissionDeniedMessage(activity, "Bluetooth permissions denied. Device discovery will not work.")
        }
    }
    
    private fun handleLocationPermissionResult(
        activity: Activity,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        
        if (allGranted) {
            Log.d(TAG, "Location permission granted")
            Toast.makeText(activity, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "Location permission denied")
            showPermissionDeniedMessage(activity, "Location permission denied. Some features may not work.")
        }
    }
    
    private fun handleStoragePermissionResult(
        activity: Activity,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        
        if (allGranted) {
            Log.d(TAG, "Storage permissions granted")
            Toast.makeText(activity, "Storage permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "Storage permissions denied")
            showPermissionDeniedMessage(activity, "Storage permissions denied. Media sync will not work.")
        }
    }
    
    private fun showPermissionDeniedMessage(activity: Activity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
    
    private fun onAllPermissionsGranted(activity: Activity) {
        Log.d(TAG, "All permissions granted")
        Toast.makeText(activity, "All permissions granted!", Toast.LENGTH_SHORT).show()
        
        // You can add a callback here to notify the calling activity
        if (activity is PermissionCallback) {
            activity.onAllPermissionsGranted()
        }
    }
    
    /**
     * Open app settings for manual permission granting
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
    
    /**
     * Check specific permission status
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get permission status summary
     */
    fun getPermissionStatusSummary(context: Context): Map<String, Boolean> {
        return DANGEROUS_PERMISSIONS.associate { permission ->
            permission to isPermissionGranted(context, permission)
        }
    }
    
    /**
     * Interface for permission callbacks
     */
    interface PermissionCallback {
        fun onAllPermissionsGranted()
    }
} 