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
import com.google.android.material.textview.MaterialTextView
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.AuthManager
import com.devicesync.app.data.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var forgotPasswordButton: MaterialTextView
    private lateinit var loadingView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        setupViews()
        setupToolbar()
        setupClickListeners()
        checkAuthStatus()
    }

    private fun setupViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        loadingView = findViewById(R.id.loadingView)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Login"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }
        
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun checkAuthStatus() {
        if (AuthManager.isLoggedIn(this)) {
            Log.d("LoginActivity", "✅ User already logged in, redirecting to main")
            navigateToMain()
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("LoginActivity", "🔄 Attempting login for: $email")
                
                val loginRequest = LoginRequest(
                    email = email,
                    password = password,
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
                
                val response = RetrofitClient.authApiService.login(loginRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    
                    // Save authentication data
                    AuthManager.saveAuthData(
                        context = this@LoginActivity,
                        accessToken = loginData?.tokens?.accessToken ?: "",
                        refreshToken = loginData?.tokens?.refreshToken ?: "",
                        userData = loginData?.user
                    )
                    
                    Log.d("LoginActivity", "✅ Login successful for: ${loginData?.user?.email}")
                    
                    navigateToMain()
                    
                } else {
                    val errorMessage = response.body()?.message ?: "Login failed"
                    Log.e("LoginActivity", "❌ Login failed: $errorMessage")
                }
                
            } catch (e: Exception) {
                Log.e("LoginActivity", "❌ Login error: ${e.message}", e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        loginButton.isEnabled = !show
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