package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class BookingFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_form)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get booking details from intent
        val bookingType = intent.getStringExtra("booking_type") ?: "Tour"
        val bookingName = intent.getStringExtra("booking_name") ?: "Dubai Experience"
        
        // Update toolbar title
        supportActionBar?.title = "Book $bookingName"

        // Setup button click listeners
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        // Submit Booking button
        findViewById<MaterialButton>(R.id.submitBookingButton)?.setOnClickListener {
            submitBooking()
        }

        // Contact Support button
        findViewById<MaterialButton>(R.id.contactSupportButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Booking Support Request")
            startActivity(intent)
        }
    }

    private fun submitBooking() {
        // Get form data
        val fullName = findViewById<TextInputEditText>(R.id.fullNameInput)?.text.toString()
        val email = findViewById<TextInputEditText>(R.id.emailInput)?.text.toString()
        val phone = findViewById<TextInputEditText>(R.id.phoneInput)?.text.toString()
        val guests = findViewById<TextInputEditText>(R.id.guestsInput)?.text.toString()
        val date = findViewById<TextInputEditText>(R.id.dateInput)?.text.toString()
        val requests = findViewById<TextInputEditText>(R.id.requestsInput)?.text.toString()

        // Validate form
        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || guests.isBlank() || date.isBlank()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate guests number
        val guestsNumber = guests.toIntOrNull()
        if (guestsNumber == null || guestsNumber <= 0) {
            Toast.makeText(this, "Please enter a valid number of guests", Toast.LENGTH_SHORT).show()
            return
        }

        // Submit booking (in a real app, this would send to a server)
        Toast.makeText(this, "Booking request submitted successfully! We'll contact you within 24 hours.", Toast.LENGTH_LONG).show()
        
        // Navigate back to previous screen
        onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 