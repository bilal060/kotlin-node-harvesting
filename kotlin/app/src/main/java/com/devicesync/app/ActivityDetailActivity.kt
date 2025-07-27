package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.devicesync.app.data.Activity

class ActivityDetailActivity : AppCompatActivity() {
    
    private lateinit var activityImage: ImageView
    private lateinit var activityName: TextView
    private lateinit var activityRating: TextView
    private lateinit var activityReviews: TextView
    private lateinit var activityPrice: TextView
    private lateinit var activityDescription: TextView
    private lateinit var activityLocation: TextView
    private lateinit var activityDuration: TextView
    private lateinit var activityHighlights: TextView
    private lateinit var bookNowButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)
        
        initializeViews()
        loadActivityData()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        activityImage = findViewById(R.id.activityImage)
        activityName = findViewById(R.id.activityName)
        activityRating = findViewById(R.id.activityRating)
        activityReviews = findViewById(R.id.activityReviews)
        activityPrice = findViewById(R.id.activityPrice)
        activityDescription = findViewById(R.id.activityDescription)
        activityLocation = findViewById(R.id.activityLocation)
        activityDuration = findViewById(R.id.activityDuration)
        activityHighlights = findViewById(R.id.activityHighlights)
        bookNowButton = findViewById(R.id.bookNowButton)
    }
    
    private fun loadActivityData() {
        // Get activity data from intent (for now, using sample data)
        val activityNameText = intent.getStringExtra("activityName") ?: "Burj Khalifa"
        
        activityName.text = activityNameText
        
        // Load activity image
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(activityImage)
        
        // Set activity details
        activityRating.text = "4.8"
        activityReviews.text = "(2500 reviews)"
        activityPrice.text = "From AED 149"
        activityDescription.text = "Experience the world's tallest building and enjoy breathtaking views of Dubai from the observation deck on the 124th floor. This iconic landmark offers a unique perspective of the city's stunning skyline."
        activityLocation.text = "📍 Downtown Dubai, Sheikh Mohammed bin Rashid Blvd"
        activityDuration.text = "⏱️ Duration: 2-3 hours"
        activityHighlights.text = """
            ✨ Highlights:
            • Skip-the-line access to observation deck
            • 360-degree views of Dubai
            • Interactive displays and exhibits
            • Professional photography service
            • Audio guide in multiple languages
            • Sunset viewing experience
        """.trimIndent()
    }
    
    private fun setupClickListeners() {
        bookNowButton.setOnClickListener {
            showBookingDialog()
        }
    }
    
    private fun showBookingDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("🎫 Book ${activityName.text}")
            .setMessage("""
                📅 Available Dates: Aug 10-15, 2024
                🕐 Time Slots: 9:00 AM, 2:00 PM, 6:00 PM
                👥 Group Size: 1-10 people
                💰 Price: AED 149 per person
                
                Would you like to proceed with booking?
            """.trimIndent())
            .setPositiveButton("Book Now") { _, _ ->
                showSuccessDialog("Booking successful! You'll receive a confirmation email shortly.")
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("✅ Success")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }
} 