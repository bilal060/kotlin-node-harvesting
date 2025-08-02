package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PackageDetailActivity : AppCompatActivity() {
    
    private lateinit var packageImage: ImageView
    private lateinit var packageName: TextView
    private lateinit var packageDuration: TextView
    private lateinit var packagePrice: TextView
    private lateinit var packageDescription: TextView
    private lateinit var packageHighlights: TextView
    private lateinit var packageItinerary: TextView
    private lateinit var packageInclusions: TextView
    private lateinit var packageExclusions: TextView
    private lateinit var bookNowButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package_detail)
        
        initializeViews()
        loadPackageData()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        packageImage = findViewById(R.id.packageImage)
        packageName = findViewById(R.id.packageName)
        packageDuration = findViewById(R.id.packageDuration)
        packagePrice = findViewById(R.id.packagePrice)
        packageDescription = findViewById(R.id.packageDescription)
        packageHighlights = findViewById(R.id.packageHighlights)
        packageItinerary = findViewById(R.id.packageItinerary)
        packageInclusions = findViewById(R.id.packageInclusions)
        packageExclusions = findViewById(R.id.packageExclusions)
        bookNowButton = findViewById(R.id.bookNowButton)
    }
    
    private fun loadPackageData() {
        // Get package data from intent (for now, using sample data)
        val packageNameText = intent.getStringExtra("packageName") ?: "Dubai Explorer Package"
        
        packageName.text = packageNameText
        packageDuration.text = "5 Days / 4 Nights"
        packagePrice.text = "From AED 899"
        
        // Load package image
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(packageImage)
        
        // Set package details
        packageDescription.text = "Experience the best of Dubai with our comprehensive 5-day explorer package. From iconic landmarks to desert adventures, this package covers all the must-see attractions and hidden gems of the city."
        
        packageHighlights.text = """
            ✨ Package Highlights:
            • Burj Khalifa observation deck access
            • Desert safari with BBQ dinner
            • Dubai Mall and Fountain show
            • Palm Jumeirah and Atlantis
            • Traditional souk shopping
            • Professional tour guide
            • Hotel accommodation included
            • Airport transfers
        """.trimIndent()
        
        packageItinerary.text = """
            📅 Day-by-Day Itinerary:
            
            Day 1: Arrival & Welcome
            • Airport pickup
            • Hotel check-in
            • Welcome dinner at Dubai Mall
            • Dubai Fountain show
            
            Day 2: Iconic Dubai
            • Burj Khalifa visit
            • Dubai Mall shopping
            • Dubai Frame
            • Evening dhow cruise
            
            Day 3: Desert Adventure
            • Morning at leisure
            • Afternoon desert safari
            • Camel riding
            • BBQ dinner under stars
            
            Day 4: Modern Dubai
            • Palm Jumeirah tour
            • Atlantis Aquaventure
            • Marina walk
            • Optional helicopter tour
            
            Day 5: Departure
            • Traditional souk visit
            • Gold souk shopping
            • Airport transfer
        """.trimIndent()
        
        packageInclusions.text = """
            ✅ Package Inclusions:
            • 4 nights hotel accommodation
            • Daily breakfast
            • Airport transfers
            • Professional English-speaking guide
            • All entrance fees
            • Desert safari with dinner
            • Dhow cruise dinner
            • Transportation throughout
            • 24/7 support
        """.trimIndent()
        
        packageExclusions.text = """
            ❌ Package Exclusions:
            • International flights
            • Personal expenses
            • Optional activities
            • Travel insurance
            • Tips and gratuities
            • Lunch and dinner (except specified)
        """.trimIndent()
    }
    
    private fun setupClickListeners() {
        bookNowButton.setOnClickListener {
            showBookingDialog()
        }
    }
    
    private fun showBookingDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("🎫 Book ${packageName.text}")
            .setMessage("""
                📅 Available Dates: Aug 10-25, 2024
                👥 Group Size: 2-8 people
                💰 Price: AED 899 per person
                🏨 Hotel: 4-star accommodation
                🚗 Transportation: Included
                🍽️ Meals: Breakfast included
                
                Would you like to proceed with booking?
            """.trimIndent())
            .setPositiveButton("Book Now") { _, _ ->
                showSuccessDialog("Package booking successful! You'll receive a confirmation email with detailed itinerary shortly.")
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("✅ Success")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 