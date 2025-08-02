package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        setupToolbar()
        
        // Set up navigation to all screens
        setupNavigation()
        setupHeroSlider()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dubai Discoveries"
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
        val tabLayout = findViewById<TabLayout>(R.id.heroTabLayout)
        
        // Set up the tab layout for hero slider
        tabLayout.addTab(tabLayout.newTab())
        tabLayout.addTab(tabLayout.newTab())
        tabLayout.addTab(tabLayout.newTab())
    }
}
