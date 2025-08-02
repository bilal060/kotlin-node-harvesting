package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LiveChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_chat)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)

        // Setup RecyclerView
        setupChatRecyclerView()

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        showWelcomeMessage()
    }

    private fun setupChatRecyclerView() {
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        // In a real app, you would use a proper adapter for chat messages
        // For now, we'll just show a welcome message
    }

    private fun setupButtonListeners() {
        // Send button
        findViewById<MaterialButton>(R.id.sendButton)?.setOnClickListener {
            sendMessage()
        }

        // Quick booking button
        findViewById<MaterialButton>(R.id.quickBookingButton)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Tour")
            intent.putExtra("booking_name", "Dubai Tour")
            startActivity(intent)
        }

        // Quick support button
        findViewById<MaterialButton>(R.id.quickSupportButton)?.setOnClickListener {
            Toast.makeText(this, "Connecting you to a support agent...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWelcomeMessage() {
        Toast.makeText(this, "Welcome to Dubai Discoveries Live Chat!", Toast.LENGTH_SHORT).show()
    }

    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        
        if (message.isNotEmpty()) {
            // In a real app, you would send this message to a server
            Toast.makeText(this, "Message sent: $message", Toast.LENGTH_SHORT).show()
            
            // Clear input
            messageInput.text?.clear()
            
            // Show auto-reply (simulating agent response)
            showAutoReply()
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAutoReply() {
        // Simulate agent response
        val responses = listOf(
            "Thank you for your message! Our team will get back to you shortly.",
            "I understand your inquiry. Let me help you with that.",
            "Great question! Here's what I can tell you about that.",
            "I'm here to help you plan your perfect Dubai experience.",
            "Let me connect you with our booking specialist."
        )
        
        val randomResponse = responses.random()
        Toast.makeText(this, "Agent: $randomResponse", Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 