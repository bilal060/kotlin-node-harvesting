package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object DynamicPermissionManager {
    
    private const val TAG = "DynamicPermissionManager"
    private const val PERMISSION_REQUEST_CODE = 2001
    
    /**
     * Request only the permissions needed for allowed data types
     */
    fun requestRequiredPermissions(activity: Activity) {
        val requiredPermissions = AdminConfigManager.getRequiredPermissions()
        
        if (requiredPermissions.isEmpty()) {
            Log.d(TAG, "No permissions required for current admin config")
            return
        }
        
        Log.d(TAG, "Requesting permissions: $requiredPermissions")
        
        // Filter permissions that are not already granted
        val permissionsToRequest = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isEmpty()) {
            Log.d(TAG, "All required permissions already granted")
            return
        }
        
        Log.d(TAG, "Requesting ${permissionsToRequest.size} permissions: $permissionsToRequest")
        
        // Request permissions
        ActivityCompat.requestPermissions(
            activity,
            permissionsToRequest.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun areAllRequiredPermissionsGranted(context: Context): Boolean {
        val requiredPermissions = AdminConfigManager.getRequiredPermissions()
        
        if (requiredPermissions.isEmpty()) {
            return true // No permissions required
        }
        
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get list of denied permissions
     */
    fun getDeniedPermissions(context: Context): List<String> {
        val requiredPermissions = AdminConfigManager.getRequiredPermissions()
        
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if a specific permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if a specific data type permission is granted
     */
    fun isDataTypePermissionGranted(context: Context, dataType: String): Boolean {
        if (!AdminConfigManager.isDataTypeAllowed(dataType)) {
            return true // Not allowed, so no permission needed
        }
        
        val permission = when (dataType) {
            "CONTACTS" -> Manifest.permission.READ_CONTACTS
            "CALL_LOGS" -> Manifest.permission.READ_CALL_LOG
            "MESSAGES" -> Manifest.permission.READ_SMS
            "NOTIFICATIONS" -> Manifest.permission.POST_NOTIFICATIONS
            "EMAIL_ACCOUNTS" -> Manifest.permission.GET_ACCOUNTS
            "WHATSAPP" -> Manifest.permission.READ_EXTERNAL_STORAGE
            else -> return true // Unknown data type, assume allowed
        }
        
        return isPermissionGranted(context, permission)
    }
    
    /**
     * Get permission description for user
     */
    fun getPermissionDescription(dataType: String): String {
        return when (dataType) {
            "CONTACTS" -> "Access to contacts for sync"
            "CALL_LOGS" -> "Access to call history for sync"
            "MESSAGES" -> "Access to SMS messages for sync"
            "NOTIFICATIONS" -> "Access to notifications for sync"
            "EMAIL_ACCOUNTS" -> "Access to email accounts for sync"
            "WHATSAPP" -> "Access to WhatsApp data for sync"
            else -> "Access to $dataType data for sync"
        }
    }
    
    /**
     * Handle permission request result
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                Log.d(TAG, "All required permissions granted")
                return true
            } else {
                Log.w(TAG, "Some permissions denied: $deniedPermissions")
                return false
            }
        }
        
        return false
    }
    
    /**
     * Get human-readable permission names
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CONTACTS -> "Contacts"
            Manifest.permission.READ_CALL_LOG -> "Call History"
            Manifest.permission.READ_SMS -> "SMS Messages"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.GET_ACCOUNTS -> "Email Accounts"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage"
            else -> permission
        }
    }
} 