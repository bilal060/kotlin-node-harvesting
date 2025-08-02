package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class TeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup button click listeners
        setupButtonListeners()

        // Show welcome message
        Toast.makeText(this, "Meet our amazing Dubai Discoveries team!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        // Contact team button
        findViewById<MaterialButton>(R.id.contactTeamButton)?.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("subject", "Team Contact Request")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 