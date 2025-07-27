package com.devicesync.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.DestinationsAdapter
import com.devicesync.app.data.Destination
import com.devicesync.app.data.Activity
import com.devicesync.app.data.Package
import com.devicesync.app.data.Review
import com.devicesync.app.data.TravelTip
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
        // TODO: Add ActivitiesAdapter
        
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
        // Sample destinations
        val destinations = listOf(
            Destination("1", "Dubai", "", "Experience the magic of Dubai", "Most Booked", 4.8f),
            Destination("2", "Abu Dhabi", "", "Discover the capital's wonders", "Trending", 4.7f),
            Destination("3", "Sharjah", "", "Cultural heritage and museums", null, 4.5f),
            Destination("4", "Fujairah", "", "Beaches and mountains", null, 4.6f)
        )
        
        destinationsAdapter.updateDestinations(destinations)
    }
    
    private fun setupServiceButtons() {
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
