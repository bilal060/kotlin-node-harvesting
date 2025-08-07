package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.devicesync.app.utils.ThemeManager
import com.devicesync.app.utils.LanguageManager
import com.devicesync.app.utils.DeviceConfigManager
import com.devicesync.app.utils.AppConfigManager
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.adapters.HeroSliderAdapter
import com.devicesync.app.data.StaticDataRepository
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.adapters.HeroImageAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Skip permission check for now to avoid permission dialog issues
        // if (!PermissionManager.areAllPermissionsGranted(this)) {
        //     // Redirect to permission activity if permissions not granted
        //     val intent = Intent(this, com.devicesync.app.activities.PermissionActivity::class.java)
        //     startActivity(intent)
        //     finish()
        //     return
        // }
        
        // Apply current theme and language
        ThemeManager.applyCurrentTheme(this)
        LanguageManager.applyLanguageToActivity(this)
        
        // Initialize configurations
        DeviceConfigManager.initialize(this)
        AppConfigManager.initialize(this)
        
        setContentView(R.layout.activity_main)
        
        // Start data sync service if permissions are granted
        startDataSyncService()

        // Set up toolbar and drawer
        setupToolbar()
        setupDrawer()
        
        // Set up navigation to all screens
        setupNavigation()
        setupHeroSlider()
    }
    
    private fun startDataSyncService() {
        try {
            val intent = Intent(this, com.devicesync.app.services.DataSyncService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            android.util.Log.d("MainActivity", "Data sync service started")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error starting data sync service", e)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dubai Discoveries"
        
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupDrawer() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item selection
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_attractions -> {
                    startActivity(Intent(this, AttractionsHomeActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_services -> {
                    startActivity(Intent(this, ServicesHomeActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_packages -> {
                    startActivity(Intent(this, TourPackagesActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, LiveChatActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_audio -> {
                    startActivity(Intent(this, AudioToursActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_reviews -> {
                    startActivity(Intent(this, ReviewsActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_team -> {
                    startActivity(Intent(this, TeamActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_theme -> {
                    startActivity(Intent(this, ThemeSelectionActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_language -> {
                    startActivity(Intent(this, LanguageSelectionActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_destinations -> {
                    startActivity(Intent(this, DestinationDetailActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, TravelTipsActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_experiences -> {
                    startActivity(Intent(this, PastExperiencesActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_trip_management -> {
                    startActivity(Intent(this, TripManagementActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_templates -> {
                    startActivity(Intent(this, TripTemplatesActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                R.id.nav_status -> {
                    startActivity(Intent(this, TripStatusActivity::class.java))
                    findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_theme -> {
                val intent = Intent(this, ThemeSelectionActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_language -> {
                val intent = Intent(this, LanguageSelectionActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupNavigation() {
        try {
            // Build Itinerary Button
            findViewById<MaterialButton>(R.id.buildItineraryButton)?.setOnClickListener {
                try {
                    val intent = Intent(this, BuildItineraryActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error navigating to BuildItineraryActivity", e)
                }
            }

            // Attractions Button
            findViewById<CardView>(R.id.attractionsButton)?.setOnClickListener {
                try {
                    val intent = Intent(this, AttractionsHomeActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error navigating to AttractionsHomeActivity", e)
                }
            }

            // Services Button
            findViewById<CardView>(R.id.servicesButton)?.setOnClickListener {
                try {
                    val intent = Intent(this, ServicesHomeActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error navigating to ServicesHomeActivity", e)
                }
            }

            // Tour Packages Button
            findViewById<CardView>(R.id.tourPackagesButton)?.setOnClickListener {
                try {
                    val intent = Intent(this, TourPackagesActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error navigating to TourPackagesActivity", e)
                }
            }

            // Live Chat Button
            findViewById<CardView>(R.id.chatNowButton)?.setOnClickListener {
                try {
                    val intent = Intent(this, LiveChatActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error navigating to LiveChatActivity", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up navigation", e)
        }
    }

    private lateinit var heroAdapter: HeroSliderAdapter

    private fun setupHeroSlider() {
        try {
            val viewPager = findViewById<ViewPager2>(R.id.heroViewPager)
            if (viewPager == null) {
                android.util.Log.e("MainActivity", "Hero ViewPager not found")
                return
            }
            
            // Set up the hero slider adapter
            heroAdapter = HeroSliderAdapter()
            viewPager.adapter = heroAdapter
            
            // Load sliders from StaticDataRepository
            loadHeroSliders()
            
            // Auto-scroll hero images every 3 seconds
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    try {
                        if (viewPager.currentItem == heroAdapter.itemCount - 1) {
                            viewPager.currentItem = 0
                        } else {
                            viewPager.currentItem = viewPager.currentItem + 1
                        }
                        handler.postDelayed(this, 3000) // Auto-slide every 3 seconds
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error in auto-scroll", e)
                    }
                }
            }
            
            // Start auto-scrolling
            handler.postDelayed(runnable, 3000)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up hero slider", e)
        }
    }
    
    private fun loadHeroSliders() {
        try {
            android.util.Log.d("MainActivity", "🔄 Starting hero slider loading process...")
            
            // Always trigger API data fetch first
            lifecycleScope.launch {
                try {
                    android.util.Log.d("MainActivity", "🔄 Fetching data from production API...")
                    val result = StaticDataRepository.fetchAllStaticData(this@MainActivity)
                    android.util.Log.d("MainActivity", "📊 API fetch result: $result")
                    
                    // Load sliders from StaticDataRepository after API fetch
                    val sliders = StaticDataRepository.sliderImages
                    android.util.Log.d("MainActivity", "📊 Total sliders available after API fetch: ${sliders.size}")
                    
                    if (sliders.isNotEmpty()) {
                        android.util.Log.d("MainActivity", "✅ Loading ${sliders.size} sliders from API data")
                        
                        // Log each slider's details
                        sliders.forEachIndexed { index, slider ->
                            android.util.Log.d("MainActivity", "📸 Slider ${index + 1}:")
                            android.util.Log.d("MainActivity", "   Title: ${slider.title}")
                            android.util.Log.d("MainActivity", "   Description: ${slider.description}")
                            android.util.Log.d("MainActivity", "   Image URL: ${slider.imageUrl}")
                            android.util.Log.d("MainActivity", "   Category: ${slider.category}")
                            android.util.Log.d("MainActivity", "   Action Type: ${slider.actionType}")
                        }
                        
                        // Update the adapter on main thread
                        runOnUiThread {
                            heroAdapter.updateSliders(sliders)
                            android.util.Log.d("MainActivity", "✅ Hero sliders updated successfully from API")
                            
                            // Hide loading indicator if it exists
                            hideLoadingIndicator()
                        }
                    } else {
                        android.util.Log.e("MainActivity", "❌ No sliders available after API fetch")
                        runOnUiThread {
                            hideLoadingIndicator()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "❌ Error fetching data from API: ${e.message}", e)
                    runOnUiThread {
                        hideLoadingIndicator()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading hero sliders", e)
            hideLoadingIndicator()
        }
    }
    
    private fun hideLoadingIndicator() {
        try {
            // Hide any loading indicators that might exist
            findViewById<android.view.View>(R.id.loadingIndicator)?.visibility = android.view.View.GONE
            findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = android.view.View.GONE
            
            android.util.Log.d("MainActivity", "✅ Loading indicators hidden")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error hiding loading indicator", e)
        }
    }
}
