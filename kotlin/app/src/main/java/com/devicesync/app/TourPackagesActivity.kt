package com.devicesync.app

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.TourPackageAdapter
import com.devicesync.app.data.models.TourPackage
import com.devicesync.app.utils.LanguageManager
import java.util.Locale

class TourPackagesActivity : AppCompatActivity() {
    
    private lateinit var packagesRecyclerView: RecyclerView
    private lateinit var packagesAdapter: TourPackageAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply current language
        LanguageManager.applyLanguageToActivity(this)
        
        setContentView(R.layout.activity_tour_packages)
        
        setupViews()
        loadPackagesData()
    }
    
    private fun setupViews() {
        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        packagesRecyclerView = findViewById(R.id.packagesRecyclerView)
        packagesRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Initialize adapter with empty list first
        packagesAdapter = TourPackageAdapter(emptyList()) { _ ->
            // Handle package click - show detailed view
            Toast.makeText(this, "Package details coming soon!", Toast.LENGTH_SHORT).show()
        }
        packagesRecyclerView.adapter = packagesAdapter
    }
    
    private fun loadPackagesData() {
        val packages = listOf(
            TourPackage(
                id = "1",
                name = "Dubai Essential Package",
                description = "Perfect introduction to Dubai with all the must-see attractions and experiences.",
                duration = "3 Days / 2 Nights",
                price = "AED 1,999",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = listOf("Burj Khalifa", "Dubai Mall", "Desert Safari", "Dubai Frame"),
                itinerary = listOf(
                    "Day 1: Arrival & Dubai Mall",
                    "Day 2: Burj Khalifa & Desert Safari",
                    "Day 3: Dubai Frame & Departure"
                ),
                includes = listOf("Hotel accommodation", "All transfers", "Guided tours", "Meals"),
                rating = 4.8f,
                reviews = 156,
                isPopular = true,
                category = "Essential"
            ),
            TourPackage(
                id = "2",
                name = "Luxury Dubai Experience",
                description = "Premium Dubai experience with luxury accommodations and exclusive access.",
                duration = "5 Days / 4 Nights",
                price = "AED 4,999",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = listOf("Burj Al Arab", "Palm Jumeirah", "Yacht Cruise", "Fine Dining"),
                itinerary = listOf(
                    "Day 1: Luxury hotel check-in",
                    "Day 2: Burj Al Arab & Palm Jumeirah",
                    "Day 3: Yacht cruise & shopping",
                    "Day 4: Fine dining experience",
                    "Day 5: Departure"
                ),
                includes = listOf("5-star hotel", "Private transfers", "Exclusive access", "Gourmet meals"),
                rating = 4.9f,
                reviews = 89,
                isPopular = true,
                category = "Luxury"
            ),
            TourPackage(
                id = "3",
                name = "Family Adventure Package",
                description = "Perfect for families with activities suitable for all ages.",
                duration = "4 Days / 3 Nights",
                price = "AED 2,799",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = listOf("Dubai Aquarium", "IMG Worlds", "KidZania", "Desert Adventure"),
                itinerary = listOf(
                    "Day 1: Dubai Aquarium & Underwater Zoo",
                    "Day 2: IMG Worlds of Adventure",
                    "Day 3: KidZania & Dubai Frame",
                    "Day 4: Desert Safari & Departure"
                ),
                includes = listOf("Family hotel", "All activities", "Child-friendly meals", "Entertainment"),
                rating = 4.7f,
                reviews = 203,
                category = "Family"
            ),
            TourPackage(
                id = "4",
                name = "Cultural Heritage Tour",
                description = "Immerse yourself in Dubai's rich cultural heritage and traditions.",
                duration = "3 Days / 2 Nights",
                price = "AED 1,899",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = listOf("Dubai Museum", "Al Fahidi Fort", "Traditional Souks", "Heritage Village"),
                itinerary = listOf(
                    "Day 1: Dubai Museum & Al Fahidi",
                    "Day 2: Traditional Souks & Heritage Village",
                    "Day 3: Cultural workshops & Departure"
                ),
                includes = listOf("Heritage hotel", "Cultural guide", "Traditional meals", "Workshops"),
                rating = 4.6f,
                reviews = 67,
                category = "Cultural"
            ),
            TourPackage(
                id = "5",
                name = "Adventure & Sports Package",
                description = "Thrilling adventures and sports activities for adrenaline seekers.",
                duration = "4 Days / 3 Nights",
                price = "AED 3,299",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = listOf("Skydiving", "Dune bashing", "Water sports", "Rock climbing"),
                itinerary = listOf(
                    "Day 1: Skydiving experience",
                    "Day 2: Desert adventure & dune bashing",
                    "Day 3: Water sports & beach activities",
                    "Day 4: Rock climbing & Departure"
                ),
                includes = listOf("Adventure hotel", "Equipment rental", "Expert guides", "Safety gear"),
                rating = 4.8f,
                reviews = 124,
                category = "Adventure"
            )
        )
        
        // Update adapter with packages data
        packagesAdapter.updatePackages(packages)
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Монгол", "Русский", "中文", "Қазақша")
        val languageCodes = arrayOf("en", "mn", "ru", "zh", "kk")
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                setAppLanguage(languageCodes[which])
            }
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
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
    
    private fun setAppLanguage(languageCode: String) {
        LanguageManager.restartActivityWithLanguage(this, languageCode)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 