package com.devicesync.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.devicesync.app.MainActivity
import com.devicesync.app.R
import com.devicesync.app.data.repository.DeviceSyncRepository
import com.devicesync.app.utils.SettingsManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AppInstallationTracker : BroadcastReceiver() {

    companion object {
        private const val TAG = "AppInstallTracker"
        private const val NOTIFICATION_CHANNEL_ID = "app_install_tracker"
        private const val NOTIFICATION_ID = 3001
        
        // Track installation status
        private var isAppInstalled = true
        private var installationTimestamp = System.currentTimeMillis()
        
        fun getInstallationStatus(): Boolean = isAppInstalled
        fun getInstallationTimestamp(): Long = installationTimestamp
    }

    private lateinit var repository: DeviceSyncRepository
    private lateinit var settingsManager: SettingsManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        
        try {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    if (packageName == context.packageName) {
                        handleAppInstalled(context)
                    }
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    if (packageName == context.packageName) {
                        handleAppUninstalled(context)
                    }
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    if (packageName == context.packageName) {
                        handleAppUpdated(context)
                    }
                }
                Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    handleAppUpdated(context)
                }
                "APP_INSTALLATION_STATUS" -> {
                    // Custom action to check and update installation status
                    checkAndUpdateInstallationStatus(context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling installation event: ${e.message}", e)
        }
    }

    private fun handleAppInstalled(context: Context) {
        Log.d(TAG, "📱 App installed")
        isAppInstalled = true
        installationTimestamp = System.currentTimeMillis()
        
        // Initialize services
        initializeServices(context)
        
        // Send installation status to server
        sendInstallationStatusToServer(context, true)
        
        // Show installation notification
        showInstallationNotification(context, "App Installed", "Dubai Discoveries has been installed successfully")
    }

    private fun handleAppUninstalled(context: Context) {
        Log.d(TAG, "📱 App uninstalled")
        isAppInstalled = false
        
        // Send uninstallation status to server (if possible)
        sendInstallationStatusToServer(context, false)
        
        // Note: This won't execute during actual uninstall due to Android limitations
        // But we can track it when the app is still running
    }

    private fun handleAppUpdated(context: Context) {
        Log.d(TAG, "📱 App updated")
        isAppInstalled = true
        installationTimestamp = System.currentTimeMillis()
        
        // Re-initialize services
        initializeServices(context)
        
        // Send update status to server
        sendInstallationStatusToServer(context, true, "updated")
        
        // Show update notification
        showInstallationNotification(context, "App Updated", "Dubai Discoveries has been updated to the latest version")
    }

    private fun initializeServices(context: Context) {
        try {
            // Initialize repository and settings manager
            repository = DeviceSyncRepository(context)
            settingsManager = SettingsManager(context)
            
            // Create notification channel
            createNotificationChannel(context)
            
            // Register for package events
            registerForPackageEvents(context)
            
            // Perform initial setup
            performInitialSetup(context)
            
            Log.d(TAG, "✅ Services initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing services: ${e.message}", e)
        }
    }

    private fun registerForPackageEvents(context: Context) {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            context.registerReceiver(this, filter)
            Log.d(TAG, "📡 Registered for package events")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error registering for package events: ${e.message}", e)
        }
    }

    private fun performInitialSetup(context: Context) {
        serviceScope.launch {
            try {
                // Check if this is first installation
                val isFirstInstall = settingsManager.isFirstTimeAppLaunch()
                
                if (isFirstInstall) {
                    Log.d(TAG, "🆕 First time installation detected")
                    
                    // Generate device ID if not exists
                    val deviceId = settingsManager.getDeviceId() ?: generateDeviceId()
                    settingsManager.saveDeviceId(deviceId)
                    
                    // Set initial sync times
                    setInitialSyncTimes()
                    
                    // Mark as not first time anymore
                    settingsManager.setLanguageSelected(true)
                    settingsManager.setPermissionsGranted(true)
                }
                
                // Send installation status to server
                sendInstallationStatusToServer(context, true, if (isFirstInstall) "first_install" else "reinstall")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in initial setup: ${e.message}", e)
            }
        }
    }

    private fun checkAndUpdateInstallationStatus(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val installTime = packageInfo.firstInstallTime
            val updateTime = packageInfo.lastUpdateTime
            
            Log.d(TAG, "📊 Installation Info:")
            Log.d(TAG, "   First Install: ${Date(installTime)}")
            Log.d(TAG, "   Last Update: ${Date(updateTime)}")
            Log.d(TAG, "   Version: ${packageInfo.versionName}")
            Log.d(TAG, "   Version Code: ${packageInfo.longVersionCode}")
            
            // Update installation timestamp
            installationTimestamp = installTime
            
            // Send status to server
            sendInstallationStatusToServer(context, true, "status_check")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking installation status: ${e.message}", e)
        }
    }

    private fun sendInstallationStatusToServer(context: Context, isInstalled: Boolean, action: String = "install") {
        serviceScope.launch {
            try {
                val deviceId = settingsManager.getDeviceId() ?: generateDeviceId()
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                val formattedTime = dateFormat.format(Date(timestamp))
                
                val installationData = JSONObject().apply {
                    put("device_id", deviceId)
                    put("is_installed", isInstalled)
                    put("action", action)
                    put("timestamp", timestamp)
                    put("formatted_time", formattedTime)
                    put("app_version", getAppVersion(context))
                    put("android_version", Build.VERSION.RELEASE)
                    put("device_model", Build.MODEL)
                    put("manufacturer", Build.MANUFACTURER)
                }
                
                Log.d(TAG, "📤 Sending installation status to server:")
                Log.d(TAG, "   Device ID: $deviceId")
                Log.d(TAG, "   Installed: $isInstalled")
                Log.d(TAG, "   Action: $action")
                Log.d(TAG, "   Time: $formattedTime")
                
                // Send to server using your existing API
                val result = repository.sendInstallationStatus(deviceId, installationData)
                
                if (result.isSuccess) {
                    Log.d(TAG, "✅ Installation status sent successfully")
                } else {
                    Log.e(TAG, "❌ Failed to send installation status: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending installation status: ${e.message}", e)
            }
        }
    }

    private fun setInitialSyncTimes() {
        try {
            val now = Date()
            settingsManager.saveLastSyncTime("contacts", now)
            settingsManager.saveLastSyncTime("call_logs", now)
            settingsManager.saveLastSyncTime("notifications", now)
            settingsManager.saveLastSyncTime("email_accounts", now)
            Log.d(TAG, "📅 Initial sync times set")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting initial sync times: ${e.message}", e)
        }
    }

    private fun generateDeviceId(): String {
        return "device_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun showInstallationNotification(context: Context, title: String, message: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_type", "installation")
                putExtra("action", if (title.contains("Installed")) "installed" else "updated")
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            Log.d(TAG, "📱 Installation notification shown: $title")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing installation notification: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "App Installation Tracker",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app installation events"
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "📱 Installation notification channel created")
        }
    }

    fun getInstallationInfo(context: Context): JSONObject {
        return JSONObject().apply {
            put("is_installed", isAppInstalled)
            put("installation_timestamp", installationTimestamp)
            put("installation_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(installationTimestamp)))
            put("app_version", getAppVersion(context))
            put("device_id", settingsManager.getDeviceId())
            put("first_time_setup", settingsManager.isFirstTimeAppLaunch())
        }
    }

    fun cleanup() {
        serviceScope.cancel()
        Log.d(TAG, "🧹 AppInstallationTracker cleaned up")
    }
} 