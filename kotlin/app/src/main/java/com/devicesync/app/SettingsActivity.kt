package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.LinearLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.devicesync.app.utils.SettingsManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup navigation
        setupNavigation()
        
        // Show welcome message
        Toast.makeText(this, "Welcome to Settings!", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigation() {
        // Language setting
        findViewById<LinearLayout>(R.id.languageSetting)?.setOnClickListener {
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
        }
        
        // Theme setting
        findViewById<LinearLayout>(R.id.themeSetting)?.setOnClickListener {
            startActivity(Intent(this, ThemeSelectionActivity::class.java))
        }
        
        // Profile setting
        findViewById<LinearLayout>(R.id.profileSetting)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
        
        // Security setting
        findViewById<LinearLayout>(R.id.securitySetting)?.setOnClickListener {
            startActivity(Intent(this, PrivacySettingsActivity::class.java))
        }
        
        // Help setting
        findViewById<LinearLayout>(R.id.helpSetting)?.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }
        
        // About setting
        findViewById<LinearLayout>(R.id.aboutSetting)?.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
        
        // Logout button
        findViewById<MaterialButton>(R.id.logoutButton)?.setOnClickListener {
            performLogout()
        }
    }
    
    private fun performLogout() {
        val settingsManager = SettingsManager(this)
        settingsManager.clearAuthData()
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate back to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 