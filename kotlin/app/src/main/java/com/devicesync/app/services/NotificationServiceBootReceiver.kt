package com.devicesync.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class NotificationServiceBootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "NotificationServiceBootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device boot completed or app updated, starting notification service")
                startNotificationService(context)
            }
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Quick boot completed, starting notification service")
                startNotificationService(context)
            }
        }
    }
    
    private fun startNotificationService(context: Context) {
        try {
            val serviceIntent = Intent(context, NotificationListenerService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d(TAG, "Notification service start intent sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting notification service: ${e.message}")
        }
    }
} 