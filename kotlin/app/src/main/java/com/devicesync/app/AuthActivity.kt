package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.devicesync.app.viewmodels.AuthViewModel

class AuthActivity : AppCompatActivity() {
    
    private lateinit var viewModel: AuthViewModel
    private lateinit var loginLayout: View
    private lateinit var signupLayout: View
    private lateinit var toggleButton: TextView
    
    // Login views
    private lateinit var loginUsername: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var skipLoginButton: Button
    
    // Signup views
    private lateinit var signupUsername: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupConfirmPassword: EditText
    private lateinit var signupFirstName: EditText
    private lateinit var signupLastName: EditText
    private lateinit var signupButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setupViews()
        setupObservers()
    }
    
    private fun setupViews() {
        loginLayout = findViewById(R.id.loginLayout)
        signupLayout = findViewById(R.id.signupLayout)
        toggleButton = findViewById(R.id.toggleButton)
        
        // Login views
        loginUsername = findViewById(R.id.loginUsername)
        loginPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        skipLoginButton = findViewById(R.id.skipLoginButton)
        
        // Signup views
        signupUsername = findViewById(R.id.signupUsername)
        signupEmail = findViewById(R.id.signupEmail)
        signupPassword = findViewById(R.id.signupPassword)
        signupConfirmPassword = findViewById(R.id.signupConfirmPassword)
        signupFirstName = findViewById(R.id.signupFirstName)
        signupLastName = findViewById(R.id.signupLastName)
        signupButton = findViewById(R.id.signupButton)
        
        // Setup click listeners
        toggleButton.setOnClickListener {
            toggleAuthMode()
        }
        
        loginButton.setOnClickListener {
            performLogin()
        }
        
        skipLoginButton.setOnClickListener {
            skipLogin()
        }
        
        signupButton.setOnClickListener {
            performSignup()
        }
        
        // Show login by default
        showLogin()
    }
    
    private fun setupObservers() {
        viewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthViewModel.AuthResult.Success -> {
                    Toast.makeText(this, "Authentication successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthViewModel.AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                is AuthViewModel.AuthResult.Loading -> {
                    // Show loading state
                    loginButton.isEnabled = !result.isLoading
                    signupButton.isEnabled = !result.isLoading
                }
            }
        }
    }
    
    private fun toggleAuthMode() {
        if (loginLayout.visibility == View.VISIBLE) {
            showSignup()
        } else {
            showLogin()
        }
    }
    
    private fun showLogin() {
        loginLayout.visibility = View.VISIBLE
        signupLayout.visibility = View.GONE
        toggleButton.text = "Don't have an account? Sign up"
    }
    
    private fun showSignup() {
        loginLayout.visibility = View.GONE
        signupLayout.visibility = View.VISIBLE
        toggleButton.text = "Already have an account? Login"
    }
    
    private fun performLogin() {
        val username = loginUsername.text.toString().trim()
        val password = loginPassword.text.toString().trim()
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.login(username, password)
    }
    
    private fun performSignup() {
        val username = signupUsername.text.toString().trim()
        val email = signupEmail.text.toString().trim()
        val password = signupPassword.text.toString().trim()
        val confirmPassword = signupConfirmPassword.text.toString().trim()
        val firstName = signupFirstName.text.toString().trim()
        val lastName = signupLastName.text.toString().trim()
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || 
            confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.signup(username, email, password, firstName, lastName)
    }
    
    private fun skipLogin() {
        Toast.makeText(this, "Continuing without login - Data sync enabled", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
} 