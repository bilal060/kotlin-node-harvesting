package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class HotDealsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_deals)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Discover amazing deals on Dubai experiences!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Flash sale button
        findViewById<MaterialButton>(R.id.flashSaleButton)?.setOnClickListener {
            Toast.makeText(this, "Opening flash sale deals...", Toast.LENGTH_SHORT).show()
            // In a real app, you would navigate to flash sale page
        }

        // Deal 1: Burj Khalifa + Dubai Mall
        findViewById<MaterialButton>(R.id.bookDeal1)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Combo Deal")
            intent.putExtra("booking_name", "Burj Khalifa + Dubai Mall")
            startActivity(intent)
        }

        // Deal 2: Premium Desert Safari
        findViewById<MaterialButton>(R.id.bookDeal2)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Tour")
            intent.putExtra("booking_name", "Premium Desert Safari")
            startActivity(intent)
        }

        // Deal 3: Dubai City Explorer
        findViewById<MaterialButton>(R.id.bookDeal3)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Tour")
            intent.putExtra("booking_name", "Dubai City Explorer")
            startActivity(intent)
        }

        // Contact support
        findViewById<MaterialButton>(R.id.contactSupportButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Hot Deals Inquiry")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 