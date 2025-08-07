package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import retrofit2.Response
import com.devicesync.app.api.RetrofitClient

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var emailInput: EditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var resetButton: Button
    private lateinit var backToLoginButton: Button
    private lateinit var loadingView: View
    private lateinit var successView: View
    private lateinit var formView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        
        setupViews()
        setupToolbar()
        setupListeners()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        emailInput = findViewById(R.id.emailInput)
        emailLayout = findViewById(R.id.emailLayout)
        resetButton = findViewById(R.id.resetButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)
        loadingView = findViewById(R.id.loadingView)
        successView = findViewById(R.id.successView)
        formView = findViewById(R.id.formView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Forgot Password"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        resetButton.setOnClickListener {
            performPasswordReset()
        }
        
        backToLoginButton.setOnClickListener {
            finish()
        }
    }

    private fun performPasswordReset() {
        val email = emailInput.text.toString().trim()
        
        if (email.isEmpty()) {
            emailLayout.error = "Please enter your email"
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email"
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("ForgotPasswordActivity", "🔄 Requesting password reset for: $email")
                
                val resetRequest = ForgotPasswordRequest(
                    email = email,
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
                
                val response = RetrofitClient.authApiService.forgotPassword(resetRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("ForgotPasswordActivity", "✅ Password reset email sent successfully")
                    showSuccess()
                } else {
                    val errorMessage = response.body()?.message ?: "Password reset failed"
                    Log.e("ForgotPasswordActivity", "❌ Password reset failed: $errorMessage")
                    showError(errorMessage)
                }
                
            } catch (e: Exception) {
                Log.e("ForgotPasswordActivity", "❌ Password reset error: ${e.message}", e)
                showError("Network error. Please try again.")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            formView.visibility = View.GONE
            loadingView.visibility = View.VISIBLE
            successView.visibility = View.GONE
        } else {
            formView.visibility = View.VISIBLE
            loadingView.visibility = View.GONE
            successView.visibility = View.GONE
        }
    }

    private fun showSuccess() {
        formView.visibility = View.GONE
        loadingView.visibility = View.GONE
        successView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class ForgotPasswordRequest(
    val email: String,
    val deviceInfo: Map<String, String>
) 