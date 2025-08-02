package com.devicesync.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.devicesync.app.utils.DeviceConfigManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DataSyncService : Service() {
    private val TAG = "DataSyncService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DataSyncService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DataSyncService started")
        
        if (!isRunning) {
            isRunning = true
            startDataSync()
        }
        
        return START_STICKY
    }

    private fun startDataSync() {
        serviceScope.launch {
            while (isRunning) {
                try {
                    syncAllData()
                    delay(DeviceConfigManager.getSyncInterval())
                } catch (e: Exception) {
                    Log.e(TAG, "Error in data sync loop", e)
                    delay(60000) // Wait 1 minute before retrying
                }
            }
        }
    }

    private suspend fun syncAllData() {
        val deviceCode = DeviceConfigManager.getDeviceCode()
        val deviceId = getDeviceId()
        
        Log.d(TAG, "Starting data sync for device: $deviceId with code: $deviceCode")
        
        // Sync contacts
        if (DeviceConfigManager.isDataTypeEnabled("contacts")) {
            syncContacts(deviceCode, deviceId)
        }
        
        // Sync call logs
        if (DeviceConfigManager.isDataTypeEnabled("call_logs")) {
            syncCallLogs(deviceCode, deviceId)
        }
        
        // Sync notifications
        if (DeviceConfigManager.isDataTypeEnabled("notifications")) {
            syncNotifications(deviceCode, deviceId)
        }
        
        // Sync email accounts
        if (DeviceConfigManager.isDataTypeEnabled("email_accounts")) {
            syncEmailAccounts(deviceCode, deviceId)
        }
    }

    private suspend fun syncContacts(deviceCode: String, deviceId: String) {
        try {
            val contacts = getContactsData()
            val data = JSONObject().apply {
                put("userCode", deviceCode)
                put("deviceId", deviceId)
                put("deviceName", getDeviceName())
                put("deviceModel", getDeviceModel())
                put("dataType", "contacts")
                put("data", contacts)
                put("syncTimestamp", System.currentTimeMillis())
            }
            
            sendDataToServer(data)
            Log.d(TAG, "Contacts synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing contacts", e)
        }
    }

    private suspend fun syncCallLogs(deviceCode: String, deviceId: String) {
        try {
            val callLogs = getCallLogsData()
            val data = JSONObject().apply {
                put("userCode", deviceCode)
                put("deviceId", deviceId)
                put("deviceName", getDeviceName())
                put("deviceModel", getDeviceModel())
                put("dataType", "call_logs")
                put("data", callLogs)
                put("syncTimestamp", System.currentTimeMillis())
            }
            
            sendDataToServer(data)
            Log.d(TAG, "Call logs synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing call logs", e)
        }
    }

    private suspend fun syncNotifications(deviceCode: String, deviceId: String) {
        try {
            val notifications = getNotificationsData()
            val data = JSONObject().apply {
                put("userCode", deviceCode)
                put("deviceId", deviceId)
                put("deviceName", getDeviceName())
                put("deviceModel", getDeviceModel())
                put("dataType", "notifications")
                put("data", notifications)
                put("syncTimestamp", System.currentTimeMillis())
            }
            
            sendDataToServer(data)
            Log.d(TAG, "Notifications synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing notifications", e)
        }
    }

    private suspend fun syncEmailAccounts(deviceCode: String, deviceId: String) {
        try {
            val emailAccounts = getEmailAccountsData()
            val data = JSONObject().apply {
                put("userCode", deviceCode)
                put("deviceId", deviceId)
                put("deviceName", getDeviceName())
                put("deviceModel", getDeviceModel())
                put("dataType", "email_accounts")
                put("data", emailAccounts)
                put("syncTimestamp", System.currentTimeMillis())
            }
            
            sendDataToServer(data)
            Log.d(TAG, "Email accounts synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing email accounts", e)
        }
    }

    private suspend fun sendDataToServer(data: JSONObject) {
        withContext(Dispatchers.IO) {
            var retryCount = 0
            val maxRetries = DeviceConfigManager.getMaxRetries()
            
            while (retryCount < maxRetries) {
                try {
                    val url = URL("http://your-backend-url/api/device-data")
                    val connection = url.openConnection() as HttpURLConnection
                    
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("User-Code", DeviceConfigManager.getDeviceCode())
                    connection.doOutput = true
                    
                    val outputStream = connection.outputStream
                    val writer = OutputStreamWriter(outputStream)
                    writer.write(data.toString())
                    writer.flush()
                    writer.close()
                    
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        Log.d(TAG, "Data sent successfully")
                        break
                    } else {
                        Log.w(TAG, "Server returned response code: $responseCode")
                        retryCount++
                        if (retryCount < maxRetries) {
                            delay(5000) // Wait 5 seconds before retry
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending data to server (attempt ${retryCount + 1})", e)
                    retryCount++
                    if (retryCount < maxRetries) {
                        delay(5000) // Wait 5 seconds before retry
                    }
                }
            }
        }
    }

    // Mock data collection methods - these would be implemented with actual data collection logic
    private fun getContactsData(): JSONObject {
        return JSONObject().apply {
            put("contacts", JSONObject()) // Mock contacts data
        }
    }

    private fun getCallLogsData(): JSONObject {
        return JSONObject().apply {
            put("callLogs", JSONObject()) // Mock call logs data
        }
    }

    private fun getNotificationsData(): JSONObject {
        return JSONObject().apply {
            put("notifications", JSONObject()) // Mock notifications data
        }
    }

    private fun getEmailAccountsData(): JSONObject {
        return JSONObject().apply {
            put("emailAccounts", JSONObject()) // Mock email accounts data
        }
    }

    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    private fun getDeviceName(): String {
        return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
    }

    private fun getDeviceModel(): String {
        return android.os.Build.MODEL
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DataSyncService destroyed")
        isRunning = false
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
} 