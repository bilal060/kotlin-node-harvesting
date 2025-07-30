package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class UserProfileActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        
        setupToolbar()
        setupProfileContent()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "User Profile"
    }
    
    private fun setupProfileContent() {
        // For now, show guest status
        val profileStatus = findViewById<TextView>(R.id.profileStatus)
        val profileDetails = findViewById<TextView>(R.id.profileDetails)
        val loginButton = findViewById<Button>(R.id.loginButton)
        
        profileStatus.text = "Guest User"
        profileDetails.text = "You are currently browsing as a guest.\n\n" +
                "To access full features:\n" +
                "• Save your favorite attractions\n" +
                "• Book tours and services\n" +
                "• Track your bookings\n" +
                "• Get personalized recommendations\n\n" +
                "Sign up or log in to unlock all features!"
        
        loginButton.setOnClickListener {
            // Show login/signup dialog
            showLoginDialog()
        }
    }
    
    private fun showLoginDialog() {
        val options = arrayOf("Sign Up", "Log In")
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Account Access")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSignUpDialog()
                    1 -> showLogInDialog()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun showSignUpDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Sign Up")
            .setMessage("Create your Dubai Discoveries account to:\n\n" +
                    "• Save favorite attractions\n" +
                    "• Book tours and services\n" +
                    "• Get personalized recommendations\n" +
                    "• Track your travel history\n\n" +
                    "Coming soon!")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun showLogInDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Log In")
            .setMessage("Welcome back! Log in to access your account.\n\n" +
                    "Coming soon!")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 