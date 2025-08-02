package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class TravelTipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_tips)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Essential tips for your Dubai adventure!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Contact support button
        findViewById<MaterialButton>(R.id.contactSupportButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Travel Tips Inquiry")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 