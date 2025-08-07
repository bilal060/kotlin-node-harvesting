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
import com.devicesync.app.data.StaticDataRepository
import com.devicesync.app.utils.LanguageManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale

class TourPackagesActivity : AppCompatActivity() {
    
    private lateinit var packagesRecyclerView: RecyclerView
    private lateinit var packagesAdapter: TourPackageAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        packagesAdapter = TourPackageAdapter(emptyList()) { packageItem ->
            // Handle package click - show detailed view
            Toast.makeText(this, "Package details coming soon!", Toast.LENGTH_SHORT).show()
        }
        packagesRecyclerView.adapter = packagesAdapter
    }
    
    private fun loadPackagesData() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("TourPackagesActivity", "🔄 Loading tour packages from API...")
                
                // Fetch data from production API
                val result = StaticDataRepository.fetchAllStaticData(this@TourPackagesActivity)
                
                if (result is StaticDataRepository.FetchResult.Success<*>) {
                    android.util.Log.d("TourPackagesActivity", "✅ Successfully fetched data from API")
                    
                    // Create enhanced packages with API data
                    val packages = createEnhancedPackages()
                    packagesAdapter.updatePackages(packages)
                    
                    android.util.Log.d("TourPackagesActivity", "✅ Loaded ${packages.size} tour packages")
                } else {
                    android.util.Log.e("TourPackagesActivity", "❌ Failed to fetch data from API: $result")
                    // Fallback to local data
                    val packages = createLocalPackages()
                    packagesAdapter.updatePackages(packages)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("TourPackagesActivity", "❌ Error loading packages: ${e.message}", e)
                // Fallback to local data
                val packages = createLocalPackages()
                packagesAdapter.updatePackages(packages)
            }
        }
    }
    
    private fun createEnhancedPackages(): List<TourPackage> {
        val attractions = StaticDataRepository.attractions
        val services = StaticDataRepository.services
        
        android.util.Log.d("TourPackagesActivity", "📊 Creating packages with ${attractions.size} attractions and ${services.size} services")
        
        return listOf(
            TourPackage(
                id = "1",
                name = "Dubai Essential Package",
                description = "Perfect introduction to Dubai with all the must-see attractions and experiences.",
                duration = "3 Days / 2 Nights",
                price = "AED 1,999",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400"
                ),
                highlights = attractions.take(4).map { it.name },
                itinerary = listOf(
                    "Day 1: Arrival & ${attractions.getOrNull(0)?.name ?: "Dubai Mall"}",
                    "Day 2: ${attractions.getOrNull(1)?.name ?: "Burj Khalifa"} & ${services.getOrNull(0)?.name ?: "Desert Safari"}",
                    "Day 3: ${attractions.getOrNull(2)?.name ?: "Dubai Frame"} & Departure"
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
                    "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400",
                    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400"
                ),
                highlights = attractions.take(3).map { it.name } + services.take(2).map { it.name },
                itinerary = listOf(
                    "Day 1: Luxury hotel check-in",
                    "Day 2: ${attractions.getOrNull(0)?.name ?: "Burj Al Arab"} & ${attractions.getOrNull(1)?.name ?: "Palm Jumeirah"}",
                    "Day 3: ${services.getOrNull(0)?.name ?: "Yacht cruise"} & shopping",
                    "Day 4: ${services.getOrNull(1)?.name ?: "Fine dining"} experience",
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
                    "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = attractions.take(3).map { it.name } + services.take(1).map { it.name },
                itinerary = listOf(
                    "Day 1: ${attractions.getOrNull(0)?.name ?: "Dubai Aquarium"} & Underwater Zoo",
                    "Day 2: ${attractions.getOrNull(1)?.name ?: "IMG Worlds"} of Adventure",
                    "Day 3: ${attractions.getOrNull(2)?.name ?: "KidZania"} & ${attractions.getOrNull(3)?.name ?: "Dubai Frame"}",
                    "Day 4: ${services.getOrNull(0)?.name ?: "Desert Safari"} & Departure"
                ),
                includes = listOf("Family hotel", "All activities", "Child-friendly meals", "Entertainment"),
                rating = 4.7f,
                reviews = 203,
                category = "Family"
            ),
            TourPackage(
                id = "4",
                name = "Full Day Abu Dhabi City Tour from Dubai with Louvre Museum",
                description = "Experience the capital city's cultural treasures including the iconic Louvre Museum.",
                duration = "1 Day",
                price = "AED 899",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400"
                ),
                highlights = listOf("Louvre Abu Dhabi", "Sheikh Zayed Grand Mosque", "Emirates Palace", "Corniche"),
                itinerary = listOf(
                    "Morning: Pickup from Dubai hotels",
                    "Mid-morning: Sheikh Zayed Grand Mosque",
                    "Afternoon: Louvre Abu Dhabi Museum",
                    "Evening: Emirates Palace & Corniche",
                    "Night: Return to Dubai"
                ),
                includes = listOf("Hotel pickup/drop", "Professional guide", "Louvre Museum entry", "Lunch"),
                rating = 4.8f,
                reviews = 234,
                isPopular = true,
                category = "Day Trip"
            ),
            TourPackage(
                id = "5",
                name = "Cultural Heritage Tour",
                description = "Immerse yourself in Dubai's rich cultural heritage and traditions.",
                duration = "3 Days / 2 Nights",
                price = "AED 1,899",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400",
                    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400"
                ),
                highlights = attractions.take(3).map { it.name },
                itinerary = listOf(
                    "Day 1: ${attractions.getOrNull(0)?.name ?: "Dubai Museum"} & ${attractions.getOrNull(1)?.name ?: "Al Fahidi"}",
                    "Day 2: Traditional Souks & Heritage Village",
                    "Day 3: Cultural workshops & Departure"
                ),
                includes = listOf("Heritage hotel", "Cultural guide", "Traditional meals", "Workshops"),
                rating = 4.6f,
                reviews = 67,
                category = "Cultural"
            ),
            TourPackage(
                id = "6",
                name = "Adventure & Sports Package",
                description = "Thrilling adventures and sports activities for adrenaline seekers.",
                duration = "4 Days / 3 Nights",
                price = "AED 3,299",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = services.take(3).map { it.name } + attractions.take(1).map { it.name },
                itinerary = listOf(
                    "Day 1: ${services.getOrNull(0)?.name ?: "Skydiving"} experience",
                    "Day 2: Desert adventure & dune bashing",
                    "Day 3: ${services.getOrNull(1)?.name ?: "Water sports"} & beach activities",
                    "Day 4: ${attractions.getOrNull(0)?.name ?: "Rock climbing"} & Departure"
                ),
                includes = listOf("Adventure hotel", "Equipment rental", "Expert guides", "Safety gear"),
                rating = 4.8f,
                reviews = 124,
                category = "Adventure"
            )
        )
    }
    
    private fun createLocalPackages(): List<TourPackage> {
        // Fallback local packages if API fails
        return listOf(
            TourPackage(
                id = "1",
                name = "Dubai Essential Package",
                description = "Perfect introduction to Dubai with all the must-see attractions and experiences.",
                duration = "3 Days / 2 Nights",
                price = "AED 1,999",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400"
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
                    "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400",
                    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400"
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
                    "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400",
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
                name = "Full Day Abu Dhabi City Tour from Dubai with Louvre Museum",
                description = "Experience the capital city's cultural treasures including the iconic Louvre Museum.",
                duration = "1 Day",
                price = "AED 899",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400"
                ),
                highlights = listOf("Louvre Abu Dhabi", "Sheikh Zayed Grand Mosque", "Emirates Palace", "Corniche"),
                itinerary = listOf(
                    "Morning: Pickup from Dubai hotels",
                    "Mid-morning: Sheikh Zayed Grand Mosque",
                    "Afternoon: Louvre Abu Dhabi Museum",
                    "Evening: Emirates Palace & Corniche",
                    "Night: Return to Dubai"
                ),
                includes = listOf("Hotel pickup/drop", "Professional guide", "Louvre Museum entry", "Lunch"),
                rating = 4.8f,
                reviews = 234,
                isPopular = true,
                category = "Day Trip"
            )
        )
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
        // Language change functionality will be implemented later
        Toast.makeText(this, "Language changed to $languageCode", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 