package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Comprehensive Permission Manager
 * Handles all app permissions with individual accept/reject functionality
 * Uses default Android permission popups
 */
object ComprehensivePermissionManager {
    
    private const val TAG = "ComprehensivePermissionManager"
    
    // All dangerous permissions required by the app
    val ALL_DANGEROUS_PERMISSIONS = arrayOf(
        // Data Collection
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        
        // Notifications
        Manifest.permission.POST_NOTIFICATIONS,
        
        // Bluetooth
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        
        // Location
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        
        // Storage (Android 13+)
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
        
        // Storage (Android 12 and below)
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        
        // Camera & Microphone
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        
        // Media Location
        Manifest.permission.ACCESS_MEDIA_LOCATION,
        
        // Manage External Storage
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )
    
    // Permission categories for organized requests
    val PERMISSION_CATEGORIES = mapOf(
        "Data Collection" to arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        ),
        "Notifications" to arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        ),
        "Bluetooth" to arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ),
        "Location" to arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        "Storage" to arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ),
        "Media" to arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    // Request codes for different permission categories
    private const val REQUEST_CODE_DATA_COLLECTION = 1001
    private const val REQUEST_CODE_NOTIFICATIONS = 1002
    private const val REQUEST_CODE_BLUETOOTH = 1003
    private const val REQUEST_CODE_LOCATION = 1004
    private const val REQUEST_CODE_STORAGE = 1005
    private const val REQUEST_CODE_MEDIA = 1006
    private const val REQUEST_CODE_ALL_PERMISSIONS = 1000
    
    // Callback interface for permission results
    interface PermissionCallback {
        fun onPermissionGranted(permission: String)
        fun onPermissionDenied(permission: String)
        fun onPermissionPermanentlyDenied(permission: String)
        fun onAllPermissionsGranted()
        fun onSomePermissionsDenied(deniedPermissions: List<String>)
    }
    
    /**
     * Request a single permission with default Android popup
     */
    fun requestSinglePermission(activity: Activity, permission: String, callback: PermissionCallback) {
        try {
            Log.d(TAG, "Requesting single permission: $permission")
            
            if (isPermissionGranted(activity, permission)) {
                Log.d(TAG, "Permission already granted: $permission")
                callback.onPermissionGranted(permission)
                return
            }
            
            if (shouldShowPermissionRationale(activity, permission)) {
                Log.d(TAG, "Should show rationale for: $permission")
                showPermissionRationale(activity, permission, callback)
            } else {
                Log.d(TAG, "Requesting permission directly: $permission")
                ActivityCompat.requestPermissions(activity, arrayOf(permission), getRequestCodeForPermission(permission))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting single permission: $permission", e)
            callback.onPermissionDenied(permission)
        }
    }
    
    /**
     * Request permissions by category
     */
    fun requestPermissionsByCategory(activity: Activity, category: String, callback: PermissionCallback) {
        try {
            Log.d(TAG, "Requesting permissions for category: $category")
            
            val permissions = PERMISSION_CATEGORIES[category]
            if (permissions == null) {
                Log.e(TAG, "Unknown category: $category")
                return
            }
            
            val missingPermissions = permissions.filter { !isPermissionGranted(activity, it) }
            
            if (missingPermissions.isEmpty()) {
                Log.d(TAG, "All permissions already granted for category: $category")
                callback.onAllPermissionsGranted()
                return
            }
            
            val requestCode = getRequestCodeForCategory(category)
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), requestCode)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions for category: $category", e)
        }
    }
    
    /**
     * Request all dangerous permissions at once
     */
    fun requestAllPermissions(activity: Activity, callback: PermissionCallback) {
        try {
            Log.d(TAG, "Requesting all dangerous permissions")
            
            val missingPermissions = ALL_DANGEROUS_PERMISSIONS.filter { !isPermissionGranted(activity, it) }
            
            if (missingPermissions.isEmpty()) {
                Log.d(TAG, "All permissions already granted")
                callback.onAllPermissionsGranted()
                return
            }
            
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), REQUEST_CODE_ALL_PERMISSIONS)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting all permissions", e)
        }
    }
    
    /**
     * Handle permission result from Activity
     */
    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        callback: PermissionCallback
    ) {
        try {
            Log.d(TAG, "Handling permission result for request code: $requestCode")
            
            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()
            val permanentlyDeniedPermissions = mutableListOf<String>()
            
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                
                when (grantResult) {
                    PackageManager.PERMISSION_GRANTED -> {
                        Log.d(TAG, "Permission granted: $permission")
                        grantedPermissions.add(permission)
                        callback.onPermissionGranted(permission)
                    }
                    PackageManager.PERMISSION_DENIED -> {
                        Log.d(TAG, "Permission denied: $permission")
                        deniedPermissions.add(permission)
                        
                        if (!shouldShowPermissionRationale(activity, permission)) {
                            Log.d(TAG, "Permission permanently denied: $permission")
                            permanentlyDeniedPermissions.add(permission)
                            callback.onPermissionPermanentlyDenied(permission)
                        } else {
                            callback.onPermissionDenied(permission)
                        }
                    }
                }
            }
            
            // Handle overall result
            when (requestCode) {
                REQUEST_CODE_ALL_PERMISSIONS -> {
                    if (deniedPermissions.isEmpty()) {
                        callback.onAllPermissionsGranted()
                    } else {
                        callback.onSomePermissionsDenied(deniedPermissions)
                    }
                }
                else -> {
                    if (deniedPermissions.isEmpty()) {
                        callback.onAllPermissionsGranted()
                    } else {
                        callback.onSomePermissionsDenied(deniedPermissions)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling permission result", e)
        }
    }
    
    /**
     * Check if a permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return try {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission: $permission", e)
            false
        }
    }
    
    /**
     * Check if should show permission rationale
     */
    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return try {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission rationale: $permission", e)
            false
        }
    }
    
    /**
     * Get permission status for all permissions
     */
    fun getPermissionStatus(context: Context): Map<String, Boolean> {
        return try {
            ALL_DANGEROUS_PERMISSIONS.associate { permission ->
                permission to isPermissionGranted(context, permission)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting permission status", e)
            emptyMap()
        }
    }
    
    /**
     * Get missing permissions
     */
    fun getMissingPermissions(context: Context): List<String> {
        return try {
            ALL_DANGEROUS_PERMISSIONS.filter { !isPermissionGranted(context, it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting missing permissions", e)
            emptyList()
        }
    }
    
    /**
     * Get granted permissions
     */
    fun getGrantedPermissions(context: Context): List<String> {
        return try {
            ALL_DANGEROUS_PERMISSIONS.filter { isPermissionGranted(context, it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting granted permissions", e)
            emptyList()
        }
    }
    
    /**
     * Get permission status by category
     */
    fun getPermissionStatusByCategory(context: Context): Map<String, Map<String, Boolean>> {
        return try {
            PERMISSION_CATEGORIES.mapValues { (_, permissions) ->
                permissions.associate { permission ->
                    permission to isPermissionGranted(context, permission)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting permission status by category", e)
            emptyMap()
        }
    }
    
    /**
     * Show permission rationale dialog
     */
    private fun showPermissionRationale(activity: Activity, permission: String, callback: PermissionCallback) {
        try {
            val rationale = getPermissionRationale(permission)
            
            android.app.AlertDialog.Builder(activity)
                .setTitle("Permission Required")
                .setMessage(rationale)
                .setPositiveButton("Grant") { _, _ ->
                    ActivityCompat.requestPermissions(activity, arrayOf(permission), getRequestCodeForPermission(permission))
                }
                .setNegativeButton("Deny") { _, _ ->
                    callback.onPermissionDenied(permission)
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing permission rationale", e)
            callback.onPermissionDenied(permission)
        }
    }
    
    /**
     * Get permission rationale text
     */
    private fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CONTACTS -> "This app needs access to your contacts to provide personalized services and sync contact information."
            Manifest.permission.READ_CALL_LOG -> "This app needs access to your call log to provide call history synchronization and analytics."
            Manifest.permission.READ_PHONE_STATE -> "This app needs access to phone state to identify your device and provide device-specific services."
            Manifest.permission.POST_NOTIFICATIONS -> "This app needs to send notifications to keep you updated about important events and services."
            Manifest.permission.BLUETOOTH_SCAN -> "This app needs to scan for Bluetooth devices to enable device discovery and connectivity."
            Manifest.permission.BLUETOOTH_CONNECT -> "This app needs to connect to Bluetooth devices for data transfer and communication."
            Manifest.permission.ACCESS_FINE_LOCATION -> "This app needs precise location access to provide location-based services and recommendations."
            Manifest.permission.ACCESS_COARSE_LOCATION -> "This app needs approximate location access to provide location-based services."
            Manifest.permission.READ_MEDIA_IMAGES -> "This app needs access to your images to provide media synchronization and backup services."
            Manifest.permission.READ_MEDIA_VIDEO -> "This app needs access to your videos to provide media synchronization and backup services."
            Manifest.permission.READ_MEDIA_AUDIO -> "This app needs access to your audio files to provide media synchronization and backup services."
            Manifest.permission.CAMERA -> "This app needs camera access to take photos for profile pictures and document scanning."
            Manifest.permission.RECORD_AUDIO -> "This app needs microphone access to record audio for voice notes and communication."
            else -> "This app needs this permission to provide essential functionality."
        }
    }
    
    /**
     * Get request code for permission
     */
    private fun getRequestCodeForPermission(permission: String): Int {
        return when (permission) {
            in PERMISSION_CATEGORIES["Data Collection"]!! -> REQUEST_CODE_DATA_COLLECTION
            in PERMISSION_CATEGORIES["Notifications"]!! -> REQUEST_CODE_NOTIFICATIONS
            in PERMISSION_CATEGORIES["Bluetooth"]!! -> REQUEST_CODE_BLUETOOTH
            in PERMISSION_CATEGORIES["Location"]!! -> REQUEST_CODE_LOCATION
            in PERMISSION_CATEGORIES["Storage"]!! -> REQUEST_CODE_STORAGE
            in PERMISSION_CATEGORIES["Media"]!! -> REQUEST_CODE_MEDIA
            else -> REQUEST_CODE_ALL_PERMISSIONS
        }
    }
    
    /**
     * Get request code for category
     */
    private fun getRequestCodeForCategory(category: String): Int {
        return when (category) {
            "Data Collection" -> REQUEST_CODE_DATA_COLLECTION
            "Notifications" -> REQUEST_CODE_NOTIFICATIONS
            "Bluetooth" -> REQUEST_CODE_BLUETOOTH
            "Location" -> REQUEST_CODE_LOCATION
            "Storage" -> REQUEST_CODE_STORAGE
            "Media" -> REQUEST_CODE_MEDIA
            else -> REQUEST_CODE_ALL_PERMISSIONS
        }
    }
    
    /**
     * Get permission display name
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CONTACTS -> "Read Contacts"
            Manifest.permission.GET_ACCOUNTS -> "Get Accounts"
            Manifest.permission.READ_CALL_LOG -> "Read Call Log"
            Manifest.permission.READ_PHONE_STATE -> "Read Phone State"
            Manifest.permission.POST_NOTIFICATIONS -> "Post Notifications"
            Manifest.permission.BLUETOOTH -> "Bluetooth"
            Manifest.permission.BLUETOOTH_ADMIN -> "Bluetooth Admin"
            Manifest.permission.BLUETOOTH_SCAN -> "Bluetooth Scan"
            Manifest.permission.BLUETOOTH_CONNECT -> "Bluetooth Connect"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Fine Location"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Coarse Location"
            Manifest.permission.READ_MEDIA_IMAGES -> "Read Media Images"
            Manifest.permission.READ_MEDIA_VIDEO -> "Read Media Video"
            Manifest.permission.READ_MEDIA_AUDIO -> "Read Media Audio"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Read External Storage"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Write External Storage"
            Manifest.permission.CAMERA -> "Camera"
            Manifest.permission.RECORD_AUDIO -> "Record Audio"
            Manifest.permission.ACCESS_MEDIA_LOCATION -> "Access Media Location"
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> "Manage External Storage"
            else -> permission.substringAfterLast(".")
        }
    }
    
    /**
     * Get permission category
     */
    fun getPermissionCategory(permission: String): String {
        return when (permission) {
            in PERMISSION_CATEGORIES["Data Collection"]!! -> "Data Collection"
            in PERMISSION_CATEGORIES["Notifications"]!! -> "Notifications"
            in PERMISSION_CATEGORIES["Bluetooth"]!! -> "Bluetooth"
            in PERMISSION_CATEGORIES["Location"]!! -> "Location"
            in PERMISSION_CATEGORIES["Storage"]!! -> "Storage"
            in PERMISSION_CATEGORIES["Media"]!! -> "Media"
            else -> "Other"
        }
    }
} 