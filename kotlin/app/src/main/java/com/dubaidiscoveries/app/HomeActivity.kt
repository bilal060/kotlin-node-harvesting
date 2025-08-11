package com.dubaidiscoveries.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dubaidiscoveries.app.models.PermissionData
import com.dubaidiscoveries.app.utils.PreferenceManager

class HomeActivity : AppCompatActivity() {
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var welcomeText: TextView
    private lateinit var permissionStatusText: TextView
    private lateinit var attractionsButton: Button
    private lateinit var servicesButton: Button
    private lateinit var tourPackagesButton: Button
    private lateinit var profileButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        initializeViews()
        loadPermissions()
        setupButtonListeners()
    }
    
    private fun initializeViews() {
        welcomeText = findViewById(R.id.welcomeText)
        permissionStatusText = findViewById(R.id.permissionStatusText)
        attractionsButton = findViewById(R.id.attractionsButton)
        servicesButton = findViewById(R.id.servicesButton)
        tourPackagesButton = findViewById(R.id.tourPackagesButton)
        profileButton = findViewById(R.id.profileButton)
    }
    
    private fun loadPermissions() {
        preferenceManager = PreferenceManager(this)
        val permissions = preferenceManager.getPermissions()
        
        if (permissions != null) {
            updateButtonStates(permissions)
            displayPermissionStatus(permissions)
        }
    }
    
    private fun updateButtonStates(permissions: PermissionData) {
        // Enable/disable buttons based on granted permissions
        attractionsButton.isEnabled = permissions.permissions.attractions
        servicesButton.isEnabled = permissions.permissions.services
        tourPackagesButton.isEnabled = permissions.permissions.tourPackages
        profileButton.isEnabled = permissions.permissions.profile
    }
    
    private fun displayPermissionStatus(permissions: PermissionData) {
        val statusText = buildString {
            appendLine("✅ Permissions Status:")
            appendLine("• Attractions: ${if (permissions.permissions.attractions) "✅" else "❌"}")
            appendLine("• Services: ${if (permissions.permissions.services) "✅" else "❌"}")
            appendLine("• Tour Packages: ${if (permissions.permissions.tourPackages) "✅" else "❌"}")
            appendLine("• Profile: ${if (permissions.permissions.profile) "✅" else "❌"}")
        }
        permissionStatusText.text = statusText
    }
    
    private fun setupButtonListeners() {
        attractionsButton.setOnClickListener {
            if (attractionsButton.isEnabled) {
                startActivity(Intent(this, AttractionsActivity::class.java))
            } else {
                showPermissionDeniedMessage("Attractions")
            }
        }
        
        servicesButton.setOnClickListener {
            if (servicesButton.isEnabled) {
                startActivity(Intent(this, ServicesActivity::class.java))
            } else {
                showPermissionDeniedMessage("Services")
            }
        }
        
        tourPackagesButton.setOnClickListener {
            if (tourPackagesButton.isEnabled) {
                startActivity(Intent(this, TourPackagesActivity::class.java))
            } else {
                showPermissionDeniedMessage("Tour Packages")
            }
        }
        
        profileButton.setOnClickListener {
            if (profileButton.isEnabled) {
                // Profile functionality
                showComingSoonMessage("Profile")
            } else {
                showPermissionDeniedMessage("Profile")
            }
        }
    }
    
    private fun showPermissionDeniedMessage(feature: String) {
        android.widget.Toast.makeText(
            this,
            "Permission denied for $feature feature",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun showComingSoonMessage(feature: String) {
        android.widget.Toast.makeText(
            this,
            "$feature feature coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    override fun onBackPressed() {
        // Prevent going back to permission screen
        moveTaskToBack(true)
    }
}
