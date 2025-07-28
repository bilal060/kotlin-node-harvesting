package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.devicesync.app.adapters.ChatRoomsAdapter
import com.devicesync.app.data.Priority2DataProvider
import com.devicesync.app.data.ChatRoom
import com.devicesync.app.services.LiveChatService

class LiveChatActivity : AppCompatActivity() {
    
    private lateinit var chatRoomsRecyclerView: RecyclerView
    private lateinit var chatRoomsAdapter: ChatRoomsAdapter
    private lateinit var liveChatService: LiveChatService
    private var chatRooms = mutableListOf<ChatRoom>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_chat)
        
        setupViews()
        setupLiveChatService()
        loadChatRooms()
        setupRecyclerView()
    }
    
    private fun setupViews() {
        chatRoomsRecyclerView = findViewById(R.id.chatRoomsRecyclerView)
        
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
        
        findViewById<Button>(R.id.newChatButton).setOnClickListener {
            showNewChatDialog()
        }
    }
    
    private fun setupLiveChatService() {
        liveChatService = LiveChatService(this)
    }
    
    private fun loadChatRooms() {
        chatRooms = Priority2DataProvider.getSampleChatRooms().toMutableList()
    }
    
    private fun setupRecyclerView() {
        chatRoomsAdapter = ChatRoomsAdapter(chatRooms) { chatRoom ->
            showChatRoom(chatRoom)
        }
        
        chatRoomsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LiveChatActivity)
            adapter = chatRoomsAdapter
        }
    }
    
    private fun showNewChatDialog() {
        val options = arrayOf("General Support", "Tour Guide", "Booking Help", "Emergency")
        
        AlertDialog.Builder(this)
            .setTitle("Start New Chat")
            .setItems(options) { _, which ->
                val chatType = when (which) {
                    0 -> "support"
                    1 -> "guide"
                    2 -> "booking"
                    3 -> "emergency"
                    else -> "support"
                }
                
                lifecycleScope.launch {
                    val result = liveChatService.createChatRoom(chatType, listOf())
                    if (result.isSuccess) {
                        val newChatRoom = result.getOrNull()
                        if (newChatRoom != null) {
                            chatRooms.add(0, newChatRoom)
                            chatRoomsAdapter.updateChatRooms(chatRooms)
                            showChatRoom(newChatRoom)
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showChatRoom(chatRoom: ChatRoom) {
        val messages = liveChatService.getMessages(chatRoom.id)
        
        val messageBuilder = StringBuilder()
        messageBuilder.append("Chat: ${chatRoom.title}\n")
        messageBuilder.append("Participants: ${chatRoom.participants.size}\n")
        messageBuilder.append("Status: ${if (chatRoom.isActive) "Active" else "Inactive"}\n\n")
        
        messages.forEach { message ->
            val sender = if (message.senderType.name == "USER") "You" else "Guide"
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(message.timestamp))
            messageBuilder.append("$sender ($time): ${message.message}\n\n")
        }
        
        // Add message input
        val inputView = layoutInflater.inflate(R.layout.dialog_chat_input, null)
        val messageInput = inputView.findViewById<EditText>(R.id.messageInput)
        
        AlertDialog.Builder(this)
            .setTitle("Live Chat")
            .setMessage(messageBuilder.toString())
            .setView(inputView)
            .setPositiveButton("Send") { _, _ ->
                val message = messageInput.text.toString()
                if (message.isNotEmpty()) {
                    sendMessage(chatRoom, message)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun sendMessage(chatRoom: ChatRoom, message: String) {
        lifecycleScope.launch {
            val result = liveChatService.sendMessage(chatRoom.id, message)
            if (result.isSuccess) {
                Toast.makeText(this@LiveChatActivity, "Message sent!", Toast.LENGTH_SHORT).show()
                // Refresh chat rooms to show updated last message
                loadChatRooms()
                chatRoomsAdapter.updateChatRooms(chatRooms)
            } else {
                Toast.makeText(this@LiveChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 