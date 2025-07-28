package com.devicesync.app.services

import android.content.Context
import com.devicesync.app.data.*
import kotlinx.coroutines.delay
import java.util.*

class LiveChatService(private val context: Context) {
    
    private val notificationService = NotificationService(context)
    private val chatRooms = mutableMapOf<String, ChatRoom>()
    private val messages = mutableMapOf<String, MutableList<ChatMessage>>()
    
    init {
        initializeDefaultChatRooms()
    }
    
    private fun initializeDefaultChatRooms() {
        // Create default chat rooms
        val supportRoom = ChatRoom(
            id = "support_room",
            title = "Customer Support",
            participants = listOf(
                ChatParticipant("user_1", "You", SenderType.USER),
                ChatParticipant("support_1", "Ahmed - Support", SenderType.SUPPORT, isOnline = true)
            ),
            lastMessage = null,
            createdAt = System.currentTimeMillis()
        )
        
        val guideRoom = ChatRoom(
            id = "guide_room",
            title = "Tour Guide - Sarah",
            participants = listOf(
                ChatParticipant("user_1", "You", SenderType.USER),
                ChatParticipant("guide_1", "Sarah - Tour Guide", SenderType.GUIDE, isOnline = true)
            ),
            lastMessage = null,
            createdAt = System.currentTimeMillis()
        )
        
        chatRooms["support_room"] = supportRoom
        chatRooms["guide_room"] = guideRoom
        
        // Initialize empty message lists
        messages["support_room"] = mutableListOf()
        messages["guide_room"] = mutableListOf()
    }
    
    suspend fun sendMessage(roomId: String, message: String, senderId: String = "user_1"): Result<ChatMessage> {
        return try {
            val chatMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                senderName = "You",
                senderType = SenderType.USER,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            
            // Add message to room
            messages[roomId]?.add(chatMessage)
            
            // Update last message in room
            chatRooms[roomId]?.let { room ->
                chatRooms[roomId] = room.copy(lastMessage = chatMessage)
            }
            
            // Simulate response delay
            delay(1000 + (Math.random() * 2000).toLong())
            
            // Generate auto-response based on message content
            val autoResponse = generateAutoResponse(message, roomId)
            if (autoResponse != null) {
                val responseMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = if (roomId == "support_room") "support_1" else "guide_1",
                    senderName = if (roomId == "support_room") "Ahmed - Support" else "Sarah - Tour Guide",
                    senderType = if (roomId == "support_room") SenderType.SUPPORT else SenderType.GUIDE,
                    message = autoResponse,
                    timestamp = System.currentTimeMillis()
                )
                
                messages[roomId]?.add(responseMessage)
                chatRooms[roomId]?.let { room ->
                    chatRooms[roomId] = room.copy(lastMessage = responseMessage)
                }
                
                // Show notification for new message
                val notification = PushNotification(
                    id = UUID.randomUUID().toString(),
                    title = "New Message",
                    message = autoResponse,
                    type = NotificationType.TOUR_UPDATE,
                    data = mapOf("room_id" to roomId),
                    timestamp = System.currentTimeMillis(),
                    priority = NotificationPriority.NORMAL
                )
                
                notificationService.showNotification(notification)
            }
            
            Result.success(chatMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateAutoResponse(message: String, roomId: String): String? {
        val lowerMessage = message.lowercase()
        
        return when {
            roomId == "support_room" -> {
                when {
                    lowerMessage.contains("booking") || lowerMessage.contains("reservation") -> {
                        "I can help you with your booking! What specific information do you need?"
                    }
                    lowerMessage.contains("payment") || lowerMessage.contains("pay") -> {
                        "For payment issues, please provide your booking reference number and I'll assist you."
                    }
                    lowerMessage.contains("cancel") || lowerMessage.contains("refund") -> {
                        "I understand you want to cancel. Please share your booking details and I'll help you with the cancellation process."
                    }
                    lowerMessage.contains("hello") || lowerMessage.contains("hi") -> {
                        "Hello! Welcome to Dubai Discoveries. How can I assist you today?"
                    }
                    else -> {
                        "Thank you for your message. A support representative will respond shortly. In the meantime, you can check our FAQ section for quick answers."
                    }
                }
            }
            roomId == "guide_room" -> {
                when {
                    lowerMessage.contains("weather") -> {
                        "The weather in Dubai is currently sunny and warm, perfect for outdoor activities! Temperature is around 28°C."
                    }
                    lowerMessage.contains("meeting") || lowerMessage.contains("pickup") -> {
                        "I'll meet you at the designated pickup point 15 minutes before the tour starts. Look for the Dubai Discoveries sign!"
                    }
                    lowerMessage.contains("what to wear") || lowerMessage.contains("clothing") -> {
                        "For Dubai tours, I recommend comfortable clothing, walking shoes, and don't forget sunscreen and a hat!"
                    }
                    lowerMessage.contains("hello") || lowerMessage.contains("hi") -> {
                        "Hi! I'm Sarah, your tour guide. I'm excited to show you around Dubai! Any questions about our upcoming tour?"
                    }
                    else -> {
                        "Great question! I'm here to make your Dubai experience amazing. Feel free to ask anything about the tour or local recommendations."
                    }
                }
            }
            else -> null
        }
    }
    
    fun getChatRooms(): List<ChatRoom> {
        return chatRooms.values.toList()
    }
    
    fun getMessages(roomId: String): List<ChatMessage> {
        return messages[roomId] ?: emptyList()
    }
    
    fun getChatRoom(roomId: String): ChatRoom? {
        return chatRooms[roomId]
    }
    
    suspend fun createChatRoom(title: String, participants: List<ChatParticipant>): Result<ChatRoom> {
        return try {
            val roomId = UUID.randomUUID().toString()
            val chatRoom = ChatRoom(
                id = roomId,
                title = title,
                participants = participants,
                lastMessage = null,
                createdAt = System.currentTimeMillis()
            )
            
            chatRooms[roomId] = chatRoom
            messages[roomId] = mutableListOf()
            
            Result.success(chatRoom)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun markMessageAsRead(roomId: String, messageId: String) {
        messages[roomId]?.find { it.id == messageId }?.let { message ->
            val index = messages[roomId]?.indexOf(message)
            if (index != null && index >= 0) {
                messages[roomId]?.set(index, message.copy(isRead = true))
            }
        }
    }
    
    fun getUnreadCount(roomId: String): Int {
        return messages[roomId]?.count { !it.isRead && it.senderType != SenderType.USER } ?: 0
    }
    
    fun isParticipantOnline(participantId: String): Boolean {
        return chatRooms.values.any { room ->
            room.participants.any { it.id == participantId && it.isOnline }
        }
    }
} 