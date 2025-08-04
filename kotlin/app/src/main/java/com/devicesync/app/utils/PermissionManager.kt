package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.devicesync.app.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.service.notification.NotificationListenerService
import android.util.Log

object PermissionManager {
    
    private const val TAG = "PermissionManager"
    
    /**
     * Check if notification access permission is granted
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        
        val packageName = context.packageName
        val serviceName = "${packageName}.services.NotificationListenerService"
        val fullServiceName = "${packageName}/${serviceName}"
        
        Log.d(TAG, "Checking notification access for: $serviceName")
        Log.d(TAG, "Full service name: $fullServiceName")
        Log.d(TAG, "Enabled listeners: $enabledListeners")
        
        // Check both formats: just service name and full package/service name
        return enabledListeners?.contains(serviceName) == true || 
               enabledListeners?.contains(fullServiceName) == true
    }
    
    /**
     * Get the notification access permission intent
     */
    fun getNotificationAccessIntent(): Intent {
        return Intent("android.settings.NOTIFICATION_LISTENER_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    /**
     * Get specific permission intents based on permission type
     */
    fun getPermissionIntent(permissionType: String): Intent {
        return when (permissionType.lowercase()) {
            "notification access" -> Intent("android.settings.NOTIFICATION_LISTENER_SETTINGS")
            "contacts" -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", "com.devicesync.app", null)
            }
            "call logs" -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", "com.devicesync.app", null)
            }
            "accounts" -> Intent(Settings.ACTION_ADD_ACCOUNT)
            else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", "com.devicesync.app", null)
            }
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        return isNotificationAccessGranted(context) &&
               isContactsPermissionGranted(context) &&
               isCallLogPermissionGranted(context) &&
               isPhoneStatePermissionGranted(context)
    }
    
    /**
     * Get missing permissions
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!isNotificationAccessGranted(context)) {
            missingPermissions.add("Notification Access")
        }
        
        if (!isContactsPermissionGranted(context)) {
            missingPermissions.add("Contacts")
        }
        
        if (!isCallLogPermissionGranted(context)) {
            missingPermissions.add("Call Logs")
        }
        
        if (!isPhoneStatePermissionGranted(context)) {
            missingPermissions.add("Phone State")
        }
        
        return missingPermissions
    }
    
    /**
     * Check if contacts permission is granted
     */
    fun isContactsPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if call log permission is granted
     */
    fun isCallLogPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if phone state permission is granted
     */
    fun isPhoneStatePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Log permission status for debugging
     */
    fun logPermissionStatus(context: Context) {
        Log.d(TAG, "=== PERMISSION STATUS ===")
        Log.d(TAG, "Notification Access: ${isNotificationAccessGranted(context)}")
        Log.d(TAG, "All Permissions Granted: ${areAllPermissionsGranted(context)}")
        Log.d(TAG, "Missing Permissions: ${getMissingPermissions(context)}")
        Log.d(TAG, "========================")
    }
}
