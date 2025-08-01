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
import com.devicesync.app.adapters.HeroSliderAdapter
import com.devicesync.app.adapters.HeroImageAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply current theme and language
        ThemeManager.applyCurrentTheme(this)
        LanguageManager.applyLanguageToActivity(this)
        
        // Initialize device configuration
        DeviceConfigManager.initialize(this)
        
        setContentView(R.layout.activity_main)

        // Set up toolbar and drawer
        setupToolbar()
        setupDrawer()
        
        // Set up navigation to all screens
        setupNavigation()
        setupHeroSlider()
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
        // Build Itinerary Button
        findViewById<MaterialButton>(R.id.buildItineraryButton)?.setOnClickListener {
            val intent = Intent(this, BuildItineraryActivity::class.java)
            startActivity(intent)
        }

        // Attractions Button
        findViewById<CardView>(R.id.attractionsButton)?.setOnClickListener {
            val intent = Intent(this, AttractionsHomeActivity::class.java)
            startActivity(intent)
        }

        // Services Button
        findViewById<CardView>(R.id.servicesButton)?.setOnClickListener {
            val intent = Intent(this, ServicesHomeActivity::class.java)
            startActivity(intent)
        }

        // Tour Packages Button
        findViewById<CardView>(R.id.tourPackagesButton)?.setOnClickListener {
            val intent = Intent(this, TourPackagesActivity::class.java)
            startActivity(intent)
        }

        // Live Chat Button
        findViewById<CardView>(R.id.chatNowButton)?.setOnClickListener {
            val intent = Intent(this, LiveChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupHeroSlider() {
        val viewPager = findViewById<ViewPager2>(R.id.heroViewPager)
        
        // Set up the hero slider adapter
        val heroAdapter = HeroSliderAdapter()
        viewPager.adapter = heroAdapter
        
        // Auto-scroll hero images every 3 seconds
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (viewPager.currentItem == heroAdapter.itemCount - 1) {
                    viewPager.currentItem = 0
                } else {
                    viewPager.currentItem = viewPager.currentItem + 1
                }
                handler.postDelayed(this, 3000) // Auto-slide every 3 seconds
            }
        }
        
        // Start auto-scrolling
        handler.postDelayed(runnable, 3000)
    }
}
