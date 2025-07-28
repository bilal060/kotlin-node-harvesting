package com.devicesync.app.data

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val attachments: List<ChatAttachment> = emptyList()
)

enum class SenderType {
    USER,
    GUIDE,
    SUPPORT,
    SYSTEM
}

data class ChatAttachment(
    val id: String,
    val type: AttachmentType,
    val url: String,
    val name: String,
    val size: Long? = null
)

enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    LOCATION,
    AUDIO
}

data class ChatRoom(
    val id: String,
    val title: String,
    val participants: List<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val unreadCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long
)

data class ChatParticipant(
    val id: String,
    val name: String,
    val type: SenderType,
    val avatar: String? = null,
    val isOnline: Boolean = false
) 