package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.api.UserInfo

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var continueAsGuestButton: Button
    private lateinit var welcomeText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        continueAsGuestButton = findViewById(R.id.continueAsGuestButton)
        welcomeText = findViewById(R.id.welcomeText)
        
        // Set tourism-themed welcome message
        welcomeText.text = "Welcome to Dubai Discoveries!\nYour gateway to amazing experiences"
        
        // Set up click listeners
        loginButton.setOnClickListener {
            performLogin()
        }
        
        continueAsGuestButton.setOnClickListener {
            proceedAsGuest()
        }
    }
    
    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }
        
        // For demo purposes, accept any non-empty email/password
        // In a real app, you would validate against your backend
        val settingsManager = SettingsManager(this)
        settingsManager.saveAuthToken("demo_token_${System.currentTimeMillis()}")
        
        // Save user info
        val userInfo = UserInfo(
            id = "user_${System.currentTimeMillis()}",
            username = email.split("@").firstOrNull() ?: "User",
            email = email,
            firstName = email.split("@").firstOrNull() ?: "User",
            lastName = ""
        )
        settingsManager.saveUserInfo(userInfo)
        
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        proceedToMainActivity()
    }
    
    private fun proceedAsGuest() {
        val settingsManager = SettingsManager(this)
        // Clear any existing auth data for guest mode
        settingsManager.clearAuthData()
        
        Toast.makeText(this, "Continuing as guest", Toast.LENGTH_SHORT).show()
        proceedToMainActivity()
    }
    
    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
} 