package com.devicesync.app.services

// Data models for sync operations
data class ContactData(
    val name: String,
    val number: String,
    val type: Int
)

data class CallLogData(
    val number: String,
    val type: Int,
    val date: Long,
    val duration: Long
)

data class MessageData(
    val address: String,
    val body: String,
    val date: Long,
    val type: Int
)

data class NotificationData(
    val notificationId: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)

data class WhatsAppMessageData(
    val messageId: String,
    val chatId: String,
    val chatName: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val messageType: String, // text, image, video, audio, document, etc.
    val timestamp: Long,
    val isIncoming: Boolean,
    val mediaPath: String?,
    val mediaSize: Long?
)

data class EmailAccountData(
    val accountId: String,
    val emailAddress: String,
    val accountName: String,
    val provider: String, // Gmail, Outlook, Yahoo, etc.
    val accountType: String, // IMAP, POP3, Exchange
    val serverIncoming: String?,
    val serverOutgoing: String?,
    val portIncoming: Int?,
    val portOutgoing: Int?,
    val isActive: Boolean,
    val isDefault: Boolean,
    val syncEnabled: Boolean,
    val lastSyncTime: Long?,
    val totalEmails: Int?,
    val unreadEmails: Int?
)

// Sync result sealed class
sealed class SyncResult {
    data class Success(val itemsSynced: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
} 