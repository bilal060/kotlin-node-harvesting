package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class PackagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packages)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Discover amazing Dubai tour packages!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Essential Package booking
        findViewById<MaterialButton>(R.id.bookEssentialPackage)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Package")
            intent.putExtra("booking_name", "Essential Dubai Package")
            startActivity(intent)
        }

        // Luxury Package booking
        findViewById<MaterialButton>(R.id.bookLuxuryPackage)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Package")
            intent.putExtra("booking_name", "Luxury Dubai Package")
            startActivity(intent)
        }

        // Family Package booking
        findViewById<MaterialButton>(R.id.bookFamilyPackage)?.setOnClickListener {
            val intent = Intent(this, BookingFormActivity::class.java)
            intent.putExtra("booking_type", "Package")
            intent.putExtra("booking_name", "Family Dubai Package")
            startActivity(intent)
        }

        // Contact support
        findViewById<MaterialButton>(R.id.contactSupportButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Custom Package Request")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 