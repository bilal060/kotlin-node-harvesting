package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.api.ApiService
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.LoginRequest
import com.devicesync.app.data.LoginResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var skipLoginButton: Button
    private lateinit var apiService: ApiService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize API service
        apiService = RetrofitClient.apiService
        
        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        skipLoginButton = findViewById(R.id.skipLoginButton)
        
        // Set click listeners
        loginButton.setOnClickListener {
            performLogin()
        }
        
        registerButton.setOnClickListener {
            // Navigate to registration screen (can be implemented later)
            Toast.makeText(this, "Registration feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        forgotPasswordText.setOnClickListener {
            // Navigate to forgot password screen (can be implemented later)
            Toast.makeText(this, "Password reset feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        skipLoginButton.setOnClickListener {
            skipLogin()
        }
        
        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity()
        }
    }
    
    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        // Validate input
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }
        
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            return
        }
        
        // Show loading state
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."
        
        // Perform login API call
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = withContext(Dispatchers.IO) {
                    apiService.login(loginRequest)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Login successful
                    val loginResponse = response.body()
                    saveUserSession(loginResponse)
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // Login failed
                    val errorMessage = response.body()?.message ?: "Login failed. Please try again."
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Network or other error
                Toast.makeText(this@LoginActivity, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
                println("❌ Login error: ${e.message}")
            } finally {
                // Reset button state
                loginButton.isEnabled = true
                loginButton.text = "Login"
            }
        }
    }
    
    private fun skipLogin() {
        // Save guest session
        val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putBoolean("is_guest", true)
            putBoolean("is_logged_in", false)
            putLong("login_timestamp", System.currentTimeMillis())
        }.apply()
        
        println("✅ Guest session saved")
        Toast.makeText(this, "Continuing as guest", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }
    
    private fun saveUserSession(loginResponse: LoginResponse?) {
        val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("user_id", loginResponse?.userId)
            putString("user_email", loginResponse?.email)
            putString("access_token", loginResponse?.accessToken)
            putBoolean("is_logged_in", true)
            putLong("login_timestamp", System.currentTimeMillis())
        }.apply()
        
        println("✅ User session saved: ${loginResponse?.email}")
    }
    
    private fun isUserLoggedIn(): Boolean {
        val sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        val isGuest = sharedPrefs.getBoolean("is_guest", false)
        val loginTimestamp = sharedPrefs.getLong("login_timestamp", 0)
        
        // Check if login is still valid (24 hours)
        val currentTime = System.currentTimeMillis()
        val loginValid = (currentTime - loginTimestamp) < (24 * 60 * 60 * 1000)
        
        return (isLoggedIn || isGuest) && loginValid
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    companion object {
        fun logout(context: android.content.Context) {
            val sharedPrefs = context.getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
} 