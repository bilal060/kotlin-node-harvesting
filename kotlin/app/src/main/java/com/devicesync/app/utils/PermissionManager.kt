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
        
        Log.d(TAG, "Checking notification access for: $serviceName")
        Log.d(TAG, "Enabled listeners: $enabledListeners")
        
        return enabledListeners?.contains(serviceName) == true
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
     * Check if all required permissions are granted
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        return isNotificationAccessGranted(context)
    }
    
    /**
     * Get missing permissions
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!isNotificationAccessGranted(context)) {
            missingPermissions.add("Notification Access")
        }
        
        return missingPermissions
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
