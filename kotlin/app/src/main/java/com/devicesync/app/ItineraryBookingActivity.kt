package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ItineraryBookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerary_booking)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Let's create your perfect Dubai itinerary!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Submit itinerary button
        findViewById<MaterialButton>(R.id.submitItineraryButton)?.setOnClickListener {
            createItinerary()
        }

        // Contact expert button
        findViewById<MaterialButton>(R.id.contactExpertButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Custom Itinerary Request")
            startActivity(intent)
        }
    }

    private fun createItinerary() {
        // Get form data
        val duration = findViewById<TextInputEditText>(R.id.durationInput)?.text.toString()
        val travelers = findViewById<TextInputEditText>(R.id.travelersInput)?.text.toString()
        val budget = findViewById<TextInputEditText>(R.id.budgetInput)?.text.toString()
        val requirements = findViewById<TextInputEditText>(R.id.requirementsInput)?.text.toString()

        // Get selected activities
        val activitiesChipGroup = findViewById<ChipGroup>(R.id.activitiesChipGroup)
        val selectedActivities = mutableListOf<String>()
        
        activitiesChipGroup?.let { chipGroup ->
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                if (chip?.isChecked == true) {
                    selectedActivities.add(chip.text.toString())
                }
            }
        }

        // Validate form
        if (duration.isBlank() || travelers.isBlank() || budget.isBlank()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate numbers
        val durationNum = duration.toIntOrNull()
        val travelersNum = travelers.toIntOrNull()
        
        if (durationNum == null || durationNum <= 0) {
            Toast.makeText(this, "Please enter a valid trip duration", Toast.LENGTH_SHORT).show()
            return
        }

        if (travelersNum == null || travelersNum <= 0) {
            Toast.makeText(this, "Please enter a valid number of travelers", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedActivities.isEmpty()) {
            Toast.makeText(this, "Please select at least one preferred activity", Toast.LENGTH_SHORT).show()
            return
        }

        // Create itinerary (in a real app, this would send to a server)
        val itinerarySummary = """
            Trip Duration: $duration days
            Travelers: $travelers
            Budget: $budget AED
            Activities: ${selectedActivities.joinToString(", ")}
            Requirements: ${if (requirements.isNotBlank()) requirements else "None"}
        """.trimIndent()

        Toast.makeText(this, "Itinerary request submitted! Our team will create your custom Dubai experience within 24 hours.", Toast.LENGTH_LONG).show()
        
        // Clear form
        clearForm()
    }

    private fun clearForm() {
        findViewById<TextInputEditText>(R.id.durationInput)?.text?.clear()
        findViewById<TextInputEditText>(R.id.travelersInput)?.text?.clear()
        findViewById<TextInputEditText>(R.id.budgetInput)?.text?.clear()
        findViewById<TextInputEditText>(R.id.requirementsInput)?.text?.clear()
        
        // Clear chip selections
        findViewById<ChipGroup>(R.id.activitiesChipGroup)?.clearCheck()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 