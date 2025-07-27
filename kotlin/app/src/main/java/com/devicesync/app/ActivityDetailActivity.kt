package com.devicesync.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.devicesync.app.adapters.ImageSlideshowAdapter
import com.devicesync.app.adapters.TimeSlotAdapter
import com.devicesync.app.data.Activity
import com.devicesync.app.data.TimeSlot
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.NumberFormat
import java.util.*

class ActivityDetailActivity : AppCompatActivity() {
    
    private lateinit var imageViewPager: ViewPager2
    private lateinit var titleText: TextView
    private lateinit var categoryText: TextView
    private lateinit var ratingText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var durationText: TextView
    private lateinit var priceText: TextView
    private lateinit var timeSlotRecyclerView: RecyclerView
    private lateinit var featuresChipGroup: ChipGroup
    private lateinit var bookButton: Button
    private lateinit var backButton: ImageButton
    
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)
        
        // Initialize views
        imageViewPager = findViewById(R.id.imageViewPager)
        titleText = findViewById(R.id.titleText)
        categoryText = findViewById(R.id.categoryText)
        ratingText = findViewById(R.id.ratingText)
        descriptionText = findViewById(R.id.descriptionText)
        durationText = findViewById(R.id.durationText)
        priceText = findViewById(R.id.priceText)
        timeSlotRecyclerView = findViewById(R.id.timeSlotRecyclerView)
        featuresChipGroup = findViewById(R.id.featuresChipGroup)
        bookButton = findViewById(R.id.bookButton)
        backButton = findViewById(R.id.backButton)
        
        // Get activity data from intent
        val activity = intent.getParcelableExtra<Activity>("activity")
        
        if (activity != null) {
            setupActivityDetails(activity)
        } else {
            // Fallback to sample data
            setupSampleActivity()
        }
        
        setupBackButton()
        setupBookButton()
    }
    
    private fun setupActivityDetails(activity: Activity) {
        titleText.text = activity.name
        categoryText.text = activity.category
        ratingText.text = "★ ${activity.rating}"
        descriptionText.text = activity.description
        durationText.text = "Duration: ${activity.duration}"
        
        // Setup image slideshow
        val imageAdapter = ImageSlideshowAdapter(activity.images)
        imageViewPager.adapter = imageAdapter
        
        // Setup time slots
        setupTimeSlots(activity.timeSlots)
        
        // Setup features
        setupFeatures(activity.features)
        
        // Setup pricing
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        priceText.text = "From ${formatter.format(activity.basePrice)}"
    }
    
    private fun setupSampleActivity() {
        val sampleActivity = Activity(
            id = "1",
            name = "Desert Safari Adventure",
            category = "Adventure",
            description = "Experience the thrill of dune bashing in the Dubai desert, followed by a traditional Bedouin camp experience. Enjoy camel rides, henna painting, traditional dance performances, and a delicious BBQ dinner under the stars.",
            rating = 4.9f,
            duration = "6 hours",
            images = listOf(
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"
            ),
            basePrice = 89.0,
            timeSlots = listOf(
                TimeSlot("02:00 PM", "08:00 PM", 89.0, "Afternoon Safari"),
                TimeSlot("03:00 PM", "09:00 PM", 99.0, "Evening Safari"),
                TimeSlot("04:00 PM", "10:00 PM", 109.0, "Sunset Safari"),
                TimeSlot("05:00 PM", "11:00 PM", 119.0, "Night Safari")
            ),
            features = listOf("Dune Bashing", "Camel Ride", "BBQ Dinner", "Live Entertainment", "Hotel Pickup")
        )
        
        setupActivityDetails(sampleActivity)
    }
    
    private fun setupTimeSlots(timeSlots: List<TimeSlot>) {
        timeSlotAdapter = TimeSlotAdapter(timeSlots) { selectedSlot ->
            // Handle time slot selection
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            priceText.text = formatter.format(selectedSlot.price)
            bookButton.text = "Book ${selectedSlot.name} - ${formatter.format(selectedSlot.price)}"
        }
        
        timeSlotRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        timeSlotRecyclerView.adapter = timeSlotAdapter
    }
    
    private fun setupFeatures(features: List<String>) {
        featuresChipGroup.removeAllViews()
        
        features.forEach { feature ->
            val chip = Chip(this)
            chip.text = feature
            chip.isCheckable = false
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(resources.getColor(R.color.chip_text, null))
            featuresChipGroup.addView(chip)
        }
    }
    
    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupBookButton() {
        bookButton.setOnClickListener {
            val selectedSlot = timeSlotAdapter.getSelectedSlot()
            if (selectedSlot != null) {
                // Show booking confirmation
                showBookingDialog(selectedSlot)
            } else {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showBookingDialog(timeSlot: TimeSlot) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        val message = "Confirm booking for ${timeSlot.name}?\n\n" +
                "Time: ${timeSlot.startTime} - ${timeSlot.endTime}\n" +
                "Price: ${formatter.format(timeSlot.price)}"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Booking")
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                Toast.makeText(this, "Booking confirmed! Check your email for details.", Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 