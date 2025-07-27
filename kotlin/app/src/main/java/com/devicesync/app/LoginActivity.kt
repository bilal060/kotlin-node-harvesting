package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

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
            // For now, just proceed to main activity (no actual login functionality)
            proceedToMainActivity()
        }
        
        continueAsGuestButton.setOnClickListener {
            proceedToMainActivity()
        }
    }
    
    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
} 