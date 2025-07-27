package com.devicesync.app.data.repository

import android.content.Context
import com.devicesync.app.data.api.ApiService
import com.devicesync.app.data.models.*
import com.devicesync.app.utils.PermissionManager
import java.util.*

class DeviceSyncRepository(private val context: Context) {
    
    private val apiService = ApiService()
    
    // Step 1 & 2: Check if device exists or register it, then get/create settings
    suspend fun checkOrRegisterDevice(deviceInfo: DeviceInfo): RegistrationResult {
        return try {
            val response = apiService.registerDevice(deviceInfo)
            
            if (response.isSuccessful) {
                val body = response.body()
                RegistrationResult(
                    isSuccess = true,
                    isNewDevice = body?.isNewDevice ?: false,
                    message = body?.message ?: "Success",
                    data = body
                )
            } else {
                RegistrationResult(
                    isSuccess = false,
                    isNewDevice = false,
                    message = "Registration failed: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            RegistrationResult(
                isSuccess = false,
                isNewDevice = false,
                message = "Network error: ${e.message}",
                data = null
            )
        }
    }
    
    // Step 3: Get device settings (fetched every 2 minutes)
    suspend fun getDeviceSettings(deviceId: String): ApiResult {
        return try {
            val response = apiService.getDeviceSettings(deviceId)
            
            if (response.isSuccessful) {
                val body = response.body()
                ApiResult(
                    isSuccess = true,
                    message = "Settings fetched successfully",
                    data = DeviceSettings.fromApiResponse(body)
                )
            } else {
                ApiResult(
                    isSuccess = false,
                    message = "Failed to get settings: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            ApiResult(
                isSuccess = false,
                message = "Settings fetch error: ${e.message}",
                data = null
            )
        }
    }
    
    // Update sync timestamp after successful sync
    suspend fun updateSyncTimestamp(deviceId: String, dataType: String, timestamp: Date): ApiResult {
        return try {
            val response = apiService.updateSyncTimestamp(deviceId, dataType, timestamp)
            
            if (response.isSuccessful) {
                ApiResult(
                    isSuccess = true,
                    message = "Sync timestamp updated",
                    data = null
                )
            } else {
                ApiResult(
                    isSuccess = false,
                    message = "Failed to update timestamp: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            ApiResult(
                isSuccess = false,
                message = "Timestamp update error: ${e.message}",
                data = null
            )
        }
    }
    
    // Step 5: Sync contacts (incremental based on lastContactsSync)
    suspend fun syncContacts(deviceId: String, contacts: List<ContactModel>): ApiResult {
        return try {
            val data = contacts.map { contact ->
                mapOf(
                    "name" to contact.name,
                    "phoneNumber" to contact.phoneNumber,
                    "phoneType" to contact.phoneType,
                    "emails" to contact.emails,
                    "organization" to (contact.organization ?: "")
                )
            }
            val response = apiService.syncData(deviceId, "CONTACTS", data)
            
            if (response.isSuccessful) {
                val body = response.body()
                ApiResult(
                    isSuccess = true,
                    message = body?.data?.message ?: "Contacts synced successfully",
                    data = body
                )
            } else {
                ApiResult(
                    isSuccess = false,
                    message = "Contacts sync failed: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            ApiResult(
                isSuccess = false,
                message = "Contacts sync error: ${e.message}",
                data = null
            )
        }
    }
    
    // Step 6: Sync call logs (incremental based on lastCallLogsSync)
    suspend fun syncCallLogs(deviceId: String, callLogs: List<CallLogModel>): ApiResult {
        return try {
            val data = callLogs.map { callLog ->
                mapOf(
                    "phoneNumber" to callLog.phoneNumber,
                    "contactName" to (callLog.contactName ?: ""),
                    "callType" to callLog.callType,
                    "duration" to callLog.duration,
                    "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(callLog.timestamp)
                )
            }
            val response = apiService.syncData(deviceId, "CALL_LOGS", data)
            
            if (response.isSuccessful) {
                val body = response.body()
                ApiResult(
                    isSuccess = true,
                    message = body?.data?.message ?: "Call logs synced successfully",
                    data = body
                )
            } else {
                ApiResult(
                    isSuccess = false,
                    message = "Call logs sync failed: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            ApiResult(
                isSuccess = false,
                message = "Call logs sync error: ${e.message}",
                data = null
            )
        }
    }
    
    // Step 7: Sync notifications (real-time, called by NotificationListenerService)
    suspend fun syncNotifications(deviceId: String, notifications: List<NotificationModel>): ApiResult {
        return try {
            val data = notifications.map { notification ->
                mapOf(
                    "packageName" to notification.packageName,
                    "title" to (notification.title ?: ""),
                    "text" to (notification.text ?: ""),
                    "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(notification.timestamp)
                )
            }
            val response = apiService.syncData(deviceId, "NOTIFICATIONS", data)
            
            if (response.isSuccessful) {
                val body = response.body()
                ApiResult(
                    isSuccess = true,
                    message = body?.data?.message ?: "Notifications synced successfully",
                    data = body
                )
            } else {
                ApiResult(
                    isSuccess = false,
                    message = "Notifications sync failed: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            ApiResult(
                isSuccess = false,
                message = "Notifications sync error: ${e.message}",
                data = null
            )
        }
    }
    
    // Step 8: Sync email accounts (once daily)
    suspend fun syncEmailAccounts(deviceId: String, emailAccounts: List<EmailAccountModel>): ApiResult {
        return try {
            val data = emailAccounts.map { emailAccount ->
                mapOf(
                    "email" to emailAccount.emailAddress,
                    "type" to emailAccount.accountType,
                    "name" to (emailAccount.displayName ?: emailAccount.accountName)
                )
            }
            val response = apiService.syncData(deviceId, "EMAIL_ACCOUNTS", data)
            
            if (response.isSuccessful) {
                val body = response.body()
                ApiResult(
                    isSuccess = true,
                    message = body?.message ?: "Email accounts synced successfully",
                    data = body
                )
            } else {
                ApiResult(
                    isSuccess = false,
                    message = "Email accounts sync failed: ${response.code()}",
                    data = null
                )
            }
        } catch (e: Exception) {
            ApiResult(
                isSuccess = false,
                message = "Email accounts sync error: ${e.message}",
                data = null
            )
        }
    }
    
    // Test connection to backend
    suspend fun testConnection(): Boolean {
        return try {
            val response = apiService.testConnection()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    // Check permissions status
    suspend fun checkPermissions(): List<PermissionInfo> {
        val permissionManager = PermissionManager(context as android.app.Activity) { /* callback */ }
        val permissionStatus = permissionManager.getPermissionStatus()
        
        return permissionStatus.map { (permission, isGranted) ->
            PermissionInfo(
                name = permission,
                isGranted = isGranted
            )
        }
    }
}

// Data classes for API responses
data class RegistrationResult(
    val isSuccess: Boolean,
    val isNewDevice: Boolean,
    val message: String,
    val data: Any?
)

data class DeviceSettings(
    val enabled: Boolean,
    val settingsUpdateFrequency: Int, // minutes
    val contacts: SyncSettings,
    val callLogs: SyncSettings,
    val notifications: SyncSettings,
    val emails: SyncSettings
) {
    companion object {
        fun fromApiResponse(response: Any?): DeviceSettings {
            // Parse the API response and create DeviceSettings
            // This would be based on your actual API response structure
            return DeviceSettings(
                enabled = true,
                settingsUpdateFrequency = 2,
                contacts = SyncSettings(enabled = true, frequency = 1440), // daily
                callLogs = SyncSettings(enabled = true, frequency = 1440), // daily
                notifications = SyncSettings(enabled = true, frequency = 1), // real-time
                emails = SyncSettings(enabled = true, frequency = 1440) // daily
            )
        }
    }
}

data class SyncSettings(
    val enabled: Boolean,
    val frequency: Int // minutes for most, seconds for notifications
)
