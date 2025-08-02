package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class PopularDestinationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popular_destinations)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Discover Dubai's most popular destinations!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Burj Khalifa booking
        findViewById<MaterialButton>(R.id.bookBurjKhalifa)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Attraction")
            intent.putExtra("booking_name", "Burj Khalifa")
            startActivity(intent)
        }

        // Dubai Mall explore
        findViewById<MaterialButton>(R.id.exploreDubaiMall)?.setOnClickListener {
            Toast.makeText(this, "Opening Dubai Mall information", Toast.LENGTH_SHORT).show()
            // In a real app, you would navigate to mall details or open a map
        }

        // Palm Jumeirah booking
        findViewById<MaterialButton>(R.id.bookPalmJumeirah)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Attraction")
            intent.putExtra("booking_name", "Palm Jumeirah")
            startActivity(intent)
        }

        // Desert Safari booking
        findViewById<MaterialButton>(R.id.bookDesertSafari)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Tour")
            intent.putExtra("booking_name", "Desert Safari Adventure")
            startActivity(intent)
        }

        // Contact support
        findViewById<MaterialButton>(R.id.contactSupportButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Popular Destinations Inquiry")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 