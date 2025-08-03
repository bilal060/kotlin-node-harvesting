package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.api.UserInfo
import com.devicesync.app.utils.AppConfigManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var continueAsGuestButton: MaterialButton
    private lateinit var welcomeText: TextView
    private lateinit var loginCard: CardView
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        continueAsGuestButton = findViewById(R.id.continueAsGuestButton)
        welcomeText = findViewById(R.id.welcomeText)
        loginCard = findViewById(R.id.loginCard)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        
        // Set tourism-themed welcome message
        welcomeText.text = "Welcome to Dubai Discoveries!\nSign in to access your personalized experience"
        
        // Apply entrance animations
        applyEntranceAnimations()
        
        // Set up click listeners
        loginButton.setOnClickListener {
            performLogin()
        }
        
        continueAsGuestButton.setOnClickListener {
            proceedAsGuest()
        }
        
        // Set up input field listeners for real-time validation
        setupInputValidation()
    }
    
    private fun applyEntranceAnimations() {
        // Fade in animation for the welcome section
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        welcomeText.startAnimation(fadeIn)
        
        // Slide up animation for the login card
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        loginCard.startAnimation(slideUp)
        
        // Scale in animation for social login buttons
        val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        findViewById<View>(R.id.socialLoginContainer)?.startAnimation(scaleIn)
    }
    
    private fun setupInputValidation() {
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && emailEditText.text.isNotEmpty()) {
                validateEmail()
            }
        }
        
        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && passwordEditText.text.isNotEmpty()) {
                validatePassword()
            }
        }
    }
    
    private fun validateEmail(): Boolean {
        val email = emailEditText.text.toString().trim()
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = null
            true
        } else {
            emailLayout.error = "Please enter a valid email address"
            false
        }
    }
    
    private fun validatePassword(): Boolean {
        val password = passwordEditText.text.toString().trim()
        return if (password.length >= 6) {
            passwordLayout.error = null
            true
        } else {
            passwordLayout.error = "Password must be at least 6 characters"
            false
        }
    }
    
    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!validateEmail() || !validatePassword()) {
            return
        }
        
        // Show loading state
        setLoadingState(true)
        
        // Simulate network delay for better UX
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500) // Simulate API call
            
            // Simple login for regular app users
            // In production, this would validate against user authentication API
            val settingsManager = SettingsManager(this@LoginActivity)
            settingsManager.saveAuthToken("user_token_${System.currentTimeMillis()}")
            
            // Save user info for regular app user
            val userInfo = UserInfo(
                id = "user_${System.currentTimeMillis()}",
                username = email.split("@").firstOrNull() ?: "User",
                email = email,
                firstName = email.split("@").firstOrNull() ?: "User",
                lastName = ""
            )
            settingsManager.saveUserInfo(userInfo)
            
            setLoadingState(false)
            Toast.makeText(this@LoginActivity, "Welcome to Dubai Discoveries!", Toast.LENGTH_SHORT).show()
            proceedToMainActivity()
        }
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        continueAsGuestButton.isEnabled = !isLoading
        emailEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading
        
        if (isLoading) {
            loginButton.text = "Signing In..."
            loginButton.icon = null
        } else {
            loginButton.text = "Sign In"
            loginButton.setIconResource(R.drawable.ic_arrow_forward)
        }
    }
    
    private fun proceedAsGuest() {
        // Show loading state
        setLoadingState(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            delay(800) // Simulate processing
            
            val settingsManager = SettingsManager(this@LoginActivity)
            // Clear any existing auth data for guest mode
            settingsManager.clearAuthData()
            
            setLoadingState(false)
            Toast.makeText(this@LoginActivity, "Continuing as guest", Toast.LENGTH_SHORT).show()
            proceedToMainActivity()
        }
    }
    
    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        
        // Apply exit animation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
    
    override fun onBackPressed() {
        // Apply exit animation when back is pressed
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        super.onBackPressed()
    }
} 