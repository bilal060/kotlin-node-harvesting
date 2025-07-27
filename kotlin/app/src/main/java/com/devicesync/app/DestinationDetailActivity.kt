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
import com.devicesync.app.data.Destination
import com.devicesync.app.data.TimeSlot
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.NumberFormat
import java.util.*

class DestinationDetailActivity : AppCompatActivity() {
    
    private lateinit var imageViewPager: ViewPager2
    private lateinit var titleText: TextView
    private lateinit var locationText: TextView
    private lateinit var ratingText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var priceText: TextView
    private lateinit var timeSlotRecyclerView: RecyclerView
    private lateinit var amenitiesChipGroup: ChipGroup
    private lateinit var bookButton: Button
    private lateinit var backButton: ImageButton
    
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_detail)
        
        // Initialize views
        imageViewPager = findViewById(R.id.imageViewPager)
        titleText = findViewById(R.id.titleText)
        locationText = findViewById(R.id.locationText)
        ratingText = findViewById(R.id.ratingText)
        descriptionText = findViewById(R.id.descriptionText)
        priceText = findViewById(R.id.priceText)
        timeSlotRecyclerView = findViewById(R.id.timeSlotRecyclerView)
        amenitiesChipGroup = findViewById(R.id.amenitiesChipGroup)
        bookButton = findViewById(R.id.bookButton)
        backButton = findViewById(R.id.backButton)
        
        // Get destination data from intent
        val destination = intent.getParcelableExtra<Destination>("destination")
        
        if (destination != null) {
            setupDestinationDetails(destination)
        } else {
            // Fallback to sample data
            setupSampleDestination()
        }
        
        setupBackButton()
        setupBookButton()
    }
    
    private fun setupDestinationDetails(destination: Destination) {
        titleText.text = destination.name
        locationText.text = destination.location
        ratingText.text = "★ ${destination.rating}"
        descriptionText.text = destination.description
        
        // Setup image slideshow
        val imageAdapter = ImageSlideshowAdapter(destination.images)
        imageViewPager.adapter = imageAdapter
        
        // Setup time slots
        setupTimeSlots(destination.timeSlots)
        
        // Setup amenities
        setupAmenities(destination.amenities)
        
        // Setup pricing
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        priceText.text = "From ${formatter.format(destination.basePrice)}"
    }
    
    private fun setupSampleDestination() {
        val sampleDestination = Destination(
            id = "1",
            name = "Burj Khalifa",
            location = "Downtown Dubai",
            description = "The Burj Khalifa is the tallest building in the world, standing at 828 meters. This architectural marvel offers breathtaking views of Dubai from its observation decks on the 124th and 148th floors. Experience the city from new heights with our guided tours.",
            rating = 4.8f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop"
            ),
            basePrice = 149.0,
            timeSlots = listOf(
                TimeSlot("09:00 AM", "11:00 AM", 149.0, "Morning Tour"),
                TimeSlot("02:00 PM", "04:00 PM", 169.0, "Afternoon Tour"),
                TimeSlot("06:00 PM", "08:00 PM", 199.0, "Sunset Tour"),
                TimeSlot("08:00 PM", "10:00 PM", 179.0, "Evening Tour")
            ),
            amenities = listOf("Guided Tour", "Skip-the-Line", "Audio Guide", "Photo Service", "Refreshments")
        )
        
        setupDestinationDetails(sampleDestination)
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
    
    private fun setupAmenities(amenities: List<String>) {
        amenitiesChipGroup.removeAllViews()
        
        amenities.forEach { amenity ->
            val chip = Chip(this)
            chip.text = amenity
            chip.isCheckable = false
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(resources.getColor(R.color.chip_text, null))
            amenitiesChipGroup.addView(chip)
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