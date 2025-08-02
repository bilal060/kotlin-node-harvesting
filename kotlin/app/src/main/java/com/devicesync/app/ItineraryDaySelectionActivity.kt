package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class ItineraryDaySelectionActivity : AppCompatActivity() {

    // Day 1 checkboxes
    private lateinit var day1HotelCheckbox: CheckBox
    private lateinit var day1BreakfastCheckbox: CheckBox
    private lateinit var day1LunchCheckbox: CheckBox
    private lateinit var day1TransportCheckbox: CheckBox
    private lateinit var day1OtherCheckbox: CheckBox

    // Day 2 checkboxes
    private lateinit var day2HotelCheckbox: CheckBox
    private lateinit var day2BreakfastCheckbox: CheckBox
    private lateinit var day2LunchCheckbox: CheckBox
    private lateinit var day2TransportCheckbox: CheckBox
    private lateinit var day2OtherCheckbox: CheckBox

    // Day 3 checkboxes
    private lateinit var day3HotelCheckbox: CheckBox
    private lateinit var day3BreakfastCheckbox: CheckBox
    private lateinit var day3LunchCheckbox: CheckBox
    private lateinit var day3TransportCheckbox: CheckBox
    private lateinit var day3OtherCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerary_day_selection)
        
        setupToolbar()
        setupViews()
        setupClickListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViews() {
        // Initialize Day 1 checkboxes
        day1HotelCheckbox = findViewById(R.id.day1HotelCheckbox)
        day1BreakfastCheckbox = findViewById(R.id.day1BreakfastCheckbox)
        day1LunchCheckbox = findViewById(R.id.day1LunchCheckbox)
        day1TransportCheckbox = findViewById(R.id.day1TransportCheckbox)
        day1OtherCheckbox = findViewById(R.id.day1OtherCheckbox)

        // Initialize Day 2 checkboxes
        day2HotelCheckbox = findViewById(R.id.day2HotelCheckbox)
        day2BreakfastCheckbox = findViewById(R.id.day2BreakfastCheckbox)
        day2LunchCheckbox = findViewById(R.id.day2LunchCheckbox)
        day2TransportCheckbox = findViewById(R.id.day2TransportCheckbox)
        day2OtherCheckbox = findViewById(R.id.day2OtherCheckbox)

        // Initialize Day 3 checkboxes
        day3HotelCheckbox = findViewById(R.id.day3HotelCheckbox)
        day3BreakfastCheckbox = findViewById(R.id.day3BreakfastCheckbox)
        day3LunchCheckbox = findViewById(R.id.day3LunchCheckbox)
        day3TransportCheckbox = findViewById(R.id.day3TransportCheckbox)
        day3OtherCheckbox = findViewById(R.id.day3OtherCheckbox)
    }

    private fun setupClickListeners() {
        val createItineraryButton = findViewById<MaterialButton>(R.id.createItineraryButton)
        createItineraryButton.setOnClickListener {
            createItinerary()
        }
    }

    private fun createItinerary() {
        // Collect all selected options
        val day1Options = mutableListOf<String>()
        val day2Options = mutableListOf<String>()
        val day3Options = mutableListOf<String>()

        // Day 1 options
        if (day1HotelCheckbox.isChecked) day1Options.add("Hotel")
        if (day1BreakfastCheckbox.isChecked) day1Options.add("Breakfast")
        if (day1LunchCheckbox.isChecked) day1Options.add("Lunch")
        if (day1TransportCheckbox.isChecked) day1Options.add("Transport")
        if (day1OtherCheckbox.isChecked) day1Options.add("Other Services")

        // Day 2 options
        if (day2HotelCheckbox.isChecked) day2Options.add("Hotel")
        if (day2BreakfastCheckbox.isChecked) day2Options.add("Breakfast")
        if (day2LunchCheckbox.isChecked) day2Options.add("Lunch")
        if (day2TransportCheckbox.isChecked) day2Options.add("Transport")
        if (day2OtherCheckbox.isChecked) day2Options.add("Other Services")

        // Day 3 options
        if (day3HotelCheckbox.isChecked) day3Options.add("Hotel")
        if (day3BreakfastCheckbox.isChecked) day3Options.add("Breakfast")
        if (day3LunchCheckbox.isChecked) day3Options.add("Lunch")
        if (day3TransportCheckbox.isChecked) day3Options.add("Transport")
        if (day3OtherCheckbox.isChecked) day3Options.add("Other Services")

        // Create summary message
        val summary = buildString {
            appendLine("Your Dubai Itinerary has been created!")
            appendLine()
            appendLine("Day 1: ${if (day1Options.isEmpty()) "No services selected" else day1Options.joinToString(", ")}")
            appendLine("Day 2: ${if (day2Options.isEmpty()) "No services selected" else day2Options.joinToString(", ")}")
            appendLine("Day 3: ${if (day3Options.isEmpty()) "No services selected" else day3Options.joinToString(", ")}")
            appendLine()
            appendLine("Our travel experts will contact you soon to finalize your perfect Dubai adventure!")
        }

        // Show success message
        Toast.makeText(this, "Itinerary created successfully!", Toast.LENGTH_LONG).show()

        // Here you would typically save the itinerary to a database
        // and navigate to a confirmation screen or back to main screen
        
        // For now, just go back to the main screen
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 