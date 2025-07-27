package com.devicesync.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.DestinationsAdapter
import com.devicesync.app.adapters.ActivitiesAdapter
import com.devicesync.app.data.Destination
import com.devicesync.app.data.Activity
import com.devicesync.app.data.Package
import com.devicesync.app.data.Review
import com.devicesync.app.data.TravelTip
import com.devicesync.app.data.DummyDataProvider
import com.devicesync.app.services.NotificationListenerService
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var startDateText: TextView
    private lateinit var endDateText: TextView
    private lateinit var startPlanningButton: Button
    private lateinit var continuePlanningButton: Button
    private lateinit var dateRangeText: TextView
    private lateinit var progressText: TextView
    
    private lateinit var destinationsRecyclerView: RecyclerView
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var packagesRecyclerView: RecyclerView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var tipsRecyclerView: RecyclerView
    
    private lateinit var destinationsAdapter: DestinationsAdapter
    private lateinit var activitiesAdapter: ActivitiesAdapter
    
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        setupDatePickers()
        setupRecyclerViews()
        loadSampleData()
        setupServiceButtons()
        
        // Request notification access permission and start notification service
        requestNotificationPermission()
    }
    
    private fun setupViews() {
        startDateText = findViewById(R.id.startDateText)
        endDateText = findViewById(R.id.endDateText)
        startPlanningButton = findViewById(R.id.startPlanningButton)
        continuePlanningButton = findViewById(R.id.continuePlanningButton)
        dateRangeText = findViewById(R.id.dateRangeText)
        progressText = findViewById(R.id.progressText)
        
        destinationsRecyclerView = findViewById(R.id.destinationsRecyclerView)
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView)
        packagesRecyclerView = findViewById(R.id.packagesRecyclerView)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        
        startPlanningButton.setOnClickListener {
            if (startDate != null && endDate != null) {
                Toast.makeText(this, "Starting your UAE trip planning!", Toast.LENGTH_LONG).show()
                // TODO: Navigate to planning screen
            } else {
                Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show()
            }
        }
        
        continuePlanningButton.setOnClickListener {
            Toast.makeText(this, "Continue planning your itinerary", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to itinerary builder
        }
        
        // Load online Dubai image in header
        val headerImageView = findViewById<android.widget.ImageView>(R.id.headerImageView)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=200&h=200&fit=crop&crop=center")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(headerImageView)
    }
    
    private fun setupDatePickers() {
        startDateText.setOnClickListener {
            showDatePicker(true)
        }
        
        endDateText.setOnClickListener {
            showDatePicker(false)
        }
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                
                if (isStartDate) {
                    startDate = selectedDate
                    startDateText.text = formatDate(selectedDate)
                } else {
                    endDate = selectedDate
                    endDateText.text = formatDate(selectedDate)
                }
                
                updateDateRange()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun formatDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    private fun updateDateRange() {
        if (startDate != null && endDate != null) {
            val startFormatted = SimpleDateFormat("MMM dd", Locale.getDefault()).format(startDate!!.time)
            val endFormatted = SimpleDateFormat("MMM dd", Locale.getDefault()).format(endDate!!.time)
            dateRangeText.text = "$startFormatted – $endFormatted"
        }
    }
    
    private fun setupRecyclerViews() {
        // Destinations RecyclerView
        destinationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        destinationsAdapter = DestinationsAdapter(emptyList()) { destination ->
            Toast.makeText(this, "Exploring ${destination.name}", Toast.LENGTH_SHORT).show()
        }
        destinationsRecyclerView.adapter = destinationsAdapter
        
        // Activities RecyclerView
        activitiesRecyclerView.layoutManager = LinearLayoutManager(this)
        activitiesAdapter = ActivitiesAdapter(emptyList()) { activity ->
            Toast.makeText(this, "Added ${activity.name} to itinerary", Toast.LENGTH_SHORT).show()
        }
        activitiesRecyclerView.adapter = activitiesAdapter
        
        // Packages RecyclerView
        packagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // TODO: Add PackagesAdapter
        
        // Reviews RecyclerView
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // TODO: Add ReviewsAdapter
        
        // Tips RecyclerView
        tipsRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Add TipsAdapter
    }
    
    private fun loadSampleData() {
        // Load destinations from dummy data
        destinationsAdapter.updateDestinations(DummyDataProvider.destinations)
        
        // Load activities from dummy data
        activitiesAdapter.updateActivities(DummyDataProvider.activities)
    }
    
    private fun requestNotificationPermission() {
        // Check if notification access is enabled
        val enabledNotificationListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        
        val packageName = packageName
        val isEnabled = enabledNotificationListeners?.contains(packageName) == true
        
        if (!isEnabled) {
            // Show dialog to guide user to enable notification access
            Toast.makeText(
                this,
                "Please enable notification access for real notifications",
                Toast.LENGTH_LONG
            ).show()
            
            // Open notification access settings
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        } else {
            // Start the notification listener service
            startNotificationService()
        }
    }
    
    private fun startNotificationService() {
        try {
            val intent = Intent(this, NotificationListenerService::class.java)
            startService(intent)
            println("🔔 NotificationListenerService started")
        } catch (e: Exception) {
            println("❌ Error starting NotificationListenerService: ${e.message}")
        }
    }
    
    private fun setupServiceButtons() {
        // Load online Dubai image in header
        val headerImageView = findViewById<android.widget.ImageView>(R.id.headerImageView)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=200&h=200&fit=crop&crop=center")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(headerImageView)
            
        // Load real images for services
        loadServiceImages()
    }
    
    private fun loadServiceImages() {
        // Airport Transfer Image
        val airportImageView = findViewById<android.widget.ImageView>(R.id.airportTransferButton)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=100&h=100&fit=crop&crop=center")
            .placeholder(R.drawable.ic_airport)
            .error(R.drawable.ic_airport)
            .centerCrop()
            .into(airportImageView)
            
        // Private Guide Image
        val guideImageView = findViewById<android.widget.ImageView>(R.id.privateGuideButton)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=center")
            .placeholder(R.drawable.ic_guide)
            .error(R.drawable.ic_guide)
            .centerCrop()
            .into(guideImageView)
            
        // Car with Driver Image
        val carImageView = findViewById<android.widget.ImageView>(R.id.carWithDriverButton)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=100&h=100&fit=crop&crop=center")
            .placeholder(R.drawable.ic_car)
            .error(R.drawable.ic_car)
            .centerCrop()
            .into(carImageView)
            
        // SIM Card Image
        val simImageView = findViewById<android.widget.ImageView>(R.id.simCardButton)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=100&h=100&fit=crop&crop=center")
            .placeholder(R.drawable.ic_sim)
            .error(R.drawable.ic_sim)
            .centerCrop()
            .into(simImageView)
            
        // Set up button click handlers
        findViewById<Button>(R.id.airportTransferButton).setOnClickListener {
            Toast.makeText(this, "Airport Transfer service added!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.privateGuideButton).setOnClickListener {
            Toast.makeText(this, "Private Guide service added!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.carWithDriverButton).setOnClickListener {
            Toast.makeText(this, "Car with Driver service added!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.simCardButton).setOnClickListener {
            Toast.makeText(this, "SIM Card service added!", Toast.LENGTH_SHORT).show()
        }
    }
}
