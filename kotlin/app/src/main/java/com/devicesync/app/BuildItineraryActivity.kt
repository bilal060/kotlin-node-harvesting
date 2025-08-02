package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class BuildItineraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build_itinerary)
        
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Add More Attractions button
        val addMoreAttractionsButton = findViewById<MaterialButton>(R.id.addMoreAttractionsButton)
        addMoreAttractionsButton.setOnClickListener {
            addMoreAttractions()
        }

        // Build My Itinerary button
        val buildItineraryButton = findViewById<MaterialButton>(R.id.buildItineraryButton)
        buildItineraryButton.setOnClickListener {
            buildMyItinerary()
        }
    }

    private fun addMoreAttractions() {
        // Navigate to attractions list to add more attractions
        Toast.makeText(this, "Opening attractions list...", Toast.LENGTH_SHORT).show()
        
        // Here you would typically navigate to the attractions list
        // For now, just show a toast message
        // val intent = Intent(this, AttractionsHomeActivity::class.java)
        // startActivity(intent)
    }

    private fun buildMyItinerary() {
        // Navigate to day selection screen
        val intent = Intent(this, ItineraryDaySelectionActivity::class.java)
        startActivity(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 