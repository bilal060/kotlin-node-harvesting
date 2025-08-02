package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Welcome to your Trip Calendar!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Add new trip button
        findViewById<MaterialButton>(R.id.addTripButton)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "New Trip")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 