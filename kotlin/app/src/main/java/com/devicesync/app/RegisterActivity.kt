package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.AuthManager
import com.devicesync.app.data.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loadingView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        setupViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun setupViews() {
        usernameInput = findViewById(R.id.usernameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        fullNameInput = findViewById(R.id.fullNameInput)
        registerButton = findViewById(R.id.registerButton)
        loadingView = findViewById(R.id.loadingView)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        val fullName = fullNameInput.text.toString().trim()
        
        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return
        }
        
        if (password.length < 6) {
            return
        }
        
        if (password != confirmPassword) {
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("RegisterActivity", "🔄 Attempting registration for: $email")
                
                val registerRequest = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    fullName = fullName,
                    deviceInfo = mapOf(
                        "deviceId" to android.provider.Settings.Secure.getString(
                            contentResolver, 
                            android.provider.Settings.Secure.ANDROID_ID
                        ),
                        "deviceName" to android.os.Build.MODEL,
                        "platform" to "Android",
                        "appVersion" to "1.0.0"
                    )
                )
                
                val response = RetrofitClient.authApiService.register(registerRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val registerData = response.body()?.data
                    
                    // Save authentication data
                    AuthManager.saveAuthData(
                        context = this@RegisterActivity,
                        accessToken = registerData?.tokens?.accessToken ?: "",
                        refreshToken = registerData?.tokens?.refreshToken ?: "",
                        userData = registerData?.user
                    )
                    
                    Log.d("RegisterActivity", "✅ Registration successful for: ${registerData?.user?.email}")
                    
                    navigateToMain()
                    
                } else {
                    val errorMessage = response.body()?.message ?: "Registration failed"
                    Log.e("RegisterActivity", "❌ Registration failed: $errorMessage")
                }
                
            } catch (e: Exception) {
                Log.e("RegisterActivity", "❌ Registration error: ${e.message}", e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        registerButton.isEnabled = !show
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 