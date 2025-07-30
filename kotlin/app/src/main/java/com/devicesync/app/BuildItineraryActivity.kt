package com.devicesync.app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.ItineraryDayAdapter
import com.devicesync.app.adapters.SelectableAttractionAdapter
import com.devicesync.app.adapters.SelectableServiceAdapter
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.Service
import com.devicesync.app.models.ItineraryDay
import com.devicesync.app.models.ItineraryItem
import com.google.android.material.tabs.TabLayout

class BuildItineraryActivity : AppCompatActivity() {
    
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var attractionsRecyclerView: RecyclerView
    private lateinit var servicesRecyclerView: RecyclerView
    private lateinit var itineraryRecyclerView: RecyclerView
    private lateinit var attractionsContainer: LinearLayout
    private lateinit var servicesContainer: LinearLayout
    private lateinit var itineraryContainer: LinearLayout
    private lateinit var createItineraryButton: Button
    private lateinit var saveItineraryButton: Button
    
    private lateinit var attractionsAdapter: SelectableAttractionAdapter
    private lateinit var servicesAdapter: SelectableServiceAdapter
    private lateinit var itineraryAdapter: ItineraryDayAdapter
    
    private val selectedAttractions = mutableListOf<Attraction>()
    private val selectedServices = mutableListOf<Service>()
    private val itineraryDays = mutableListOf<ItineraryDay>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build_itinerary)
        
        initializeViews()
        setupToolbar()
        setupTabs()
        setupRecyclerViews()
        loadData()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        attractionsRecyclerView = findViewById(R.id.attractionsRecyclerView)
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView)
        itineraryRecyclerView = findViewById(R.id.itineraryRecyclerView)
        attractionsContainer = findViewById(R.id.attractionsContainer)
        servicesContainer = findViewById(R.id.servicesContainer)
        itineraryContainer = findViewById(R.id.itineraryContainer)
        createItineraryButton = findViewById(R.id.createItineraryButton)
        saveItineraryButton = findViewById(R.id.saveItineraryButton)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Build Your Itinerary"
    }
    
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Attractions"))
        tabLayout.addTab(tabLayout.newTab().setText("Services"))
        tabLayout.addTab(tabLayout.newTab().setText("Itinerary"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showAttractionsTab()
                    1 -> showServicesTab()
                    2 -> showItineraryTab()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Show attractions tab by default
        showAttractionsTab()
    }
    
    private fun setupRecyclerViews() {
        // Setup attractions adapter
        attractionsAdapter = SelectableAttractionAdapter(
            onAttractionSelected = { attraction, isSelected ->
                if (isSelected) {
                    selectedAttractions.add(attraction)
                } else {
                    selectedAttractions.remove(attraction)
                }
                updateCreateButton()
            }
        )
        attractionsRecyclerView.layoutManager = LinearLayoutManager(this)
        attractionsRecyclerView.adapter = attractionsAdapter
        
        // Setup services adapter
        servicesAdapter = SelectableServiceAdapter(
            onServiceSelected = { service, isSelected ->
                if (isSelected) {
                    selectedServices.add(service)
                } else {
                    selectedServices.remove(service)
                }
                updateCreateButton()
            }
        )
        servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        servicesRecyclerView.adapter = servicesAdapter
        
        // Setup itinerary adapter
        itineraryAdapter = ItineraryDayAdapter(
            onItemMoved = { fromDay, toDay, item ->
                moveItineraryItem(fromDay, toDay, item)
            },
            onItemRemoved = { day, item ->
                removeItineraryItem(day, item)
            }
        )
        itineraryRecyclerView.layoutManager = LinearLayoutManager(this)
        itineraryRecyclerView.adapter = itineraryAdapter
    }
    
    private fun loadData() {
        // Load sample attractions
        val attractions = listOf(
            Attraction(
                id = 1,
                name = "Burj Khalifa",
                simplePrice = 149.0,
                premiumPrice = 199.0,
                location = "Downtown Dubai",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.8f,
                description = "Iconic skyscraper with observation deck"
            ),
            Attraction(
                id = 2,
                name = "Dubai Mall",
                simplePrice = 0.0,
                premiumPrice = 50.0,
                location = "Downtown Dubai",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.6f,
                description = "World's largest shopping mall"
            ),
            Attraction(
                id = 3,
                name = "Palm Jumeirah",
                simplePrice = 89.0,
                premiumPrice = 150.0,
                location = "Palm Jumeirah",
                images = listOf("https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400"),
                rating = 4.7f,
                description = "Artificial island with luxury resorts"
            ),
            Attraction(
                id = 4,
                name = "Dubai Frame",
                simplePrice = 50.0,
                premiumPrice = 80.0,
                location = "Zabeel Park",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.5f,
                description = "Iconic frame structure with city views"
            ),
            Attraction(
                id = 5,
                name = "Dubai Aquarium",
                simplePrice = 120.0,
                premiumPrice = 180.0,
                location = "Dubai Mall",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.4f,
                description = "Underwater zoo and aquarium"
            ),
            Attraction(
                id = 6,
                name = "Desert Safari",
                simplePrice = 199.0,
                premiumPrice = 299.0,
                location = "Dubai Desert",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.8f,
                description = "Adventure in the Arabian desert"
            )
        )
        attractionsAdapter.updateAttractions(attractions)
        
        // Load sample services
        val services = listOf(
            Service(
                id = "1",
                name = "Private Tour Guide",
                description = "Professional English-speaking guide",
                averageCost = mapOf("per_day" to 250),
                currency = "AED",
                unit = "per day",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.9f,
                category = "Tour Guide"
            ),
            Service(
                id = "2",
                name = "Airport Transfer",
                description = "Comfortable airport pickup and drop-off",
                averageCost = mapOf("one_way" to 150),
                currency = "AED",
                unit = "one way",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.7f,
                category = "Transportation"
            ),
            Service(
                id = "3",
                name = "Photography Service",
                description = "Professional photography during your tour",
                averageCost = mapOf("per_session" to 180),
                currency = "AED",
                unit = "per session",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.8f,
                category = "Photography"
            ),
            Service(
                id = "4",
                name = "Luxury Car Rental",
                description = "Premium car rental with driver",
                averageCost = mapOf("per_day" to 400),
                currency = "AED",
                unit = "per day",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.6f,
                category = "Transportation"
            ),
            Service(
                id = "5",
                name = "Cooking Class",
                description = "Traditional Arabic cooking experience",
                averageCost = mapOf("per_person" to 120),
                currency = "AED",
                unit = "per person",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.7f,
                category = "Experience"
            ),
            Service(
                id = "6",
                name = "Spa & Wellness",
                description = "Relaxing spa treatment",
                averageCost = mapOf("per_session" to 200),
                currency = "AED",
                unit = "per session",
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"),
                rating = 4.8f,
                category = "Wellness"
            )
        )
        servicesAdapter.updateServices(services)
    }
    
    private fun setupClickListeners() {
        createItineraryButton.setOnClickListener {
            createItinerary()
        }
        
        saveItineraryButton.setOnClickListener {
            saveItinerary()
        }
    }
    
    private fun showAttractionsTab() {
        attractionsContainer.visibility = View.VISIBLE
        servicesContainer.visibility = View.GONE
        itineraryContainer.visibility = View.GONE
    }
    
    private fun showServicesTab() {
        attractionsContainer.visibility = View.GONE
        servicesContainer.visibility = View.VISIBLE
        itineraryContainer.visibility = View.GONE
    }
    
    private fun showItineraryTab() {
        attractionsContainer.visibility = View.GONE
        servicesContainer.visibility = View.GONE
        itineraryContainer.visibility = View.VISIBLE
    }
    
    private fun updateCreateButton() {
        val totalSelected = selectedAttractions.size + selectedServices.size
        createItineraryButton.isEnabled = totalSelected > 0
        createItineraryButton.text = "Create Itinerary (${totalSelected} items)"
    }
    
    private fun createItinerary() {
        val totalItems = selectedAttractions.size + selectedServices.size
        if (totalItems == 0) {
            Toast.makeText(this, "Please select at least one attraction or service", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Calculate number of days needed (assuming 3-4 items per day)
        val daysNeeded = (totalItems + 2) / 3 // Round up
        
        itineraryDays.clear()
        
        // Create days
        for (dayNumber in 1..daysNeeded) {
            val day = ItineraryDay(
                dayNumber = dayNumber,
                title = "Day $dayNumber",
                items = mutableListOf()
            )
            itineraryDays.add(day)
        }
        
        // Distribute attractions and services across days
        var currentDayIndex = 0
        
        // Add attractions first
        selectedAttractions.forEach { attraction ->
            val day = itineraryDays[currentDayIndex]
            day.items.add(ItineraryItem(
                id = attraction.id.toString(),
                name = attraction.name,
                type = "Attraction",
                duration = 3, // Default duration for attractions
                price = attraction.simplePrice,
                imageUrl = attraction.images.firstOrNull() ?: ""
            ))
            
            currentDayIndex = (currentDayIndex + 1) % daysNeeded
        }
        
        // Add services
        selectedServices.forEach { service ->
            val day = itineraryDays[currentDayIndex]
            day.items.add(ItineraryItem(
                id = service.id,
                name = service.name,
                type = "Service",
                duration = 2, // Default duration for services
                price = service.averageCost.values.firstOrNull()?.toDouble() ?: 0.0,
                imageUrl = service.images.firstOrNull() ?: ""
            ))
            
            currentDayIndex = (currentDayIndex + 1) % daysNeeded
        }
        
        // Update adapter and switch to itinerary tab
        itineraryAdapter.updateItinerary(itineraryDays)
        tabLayout.getTabAt(2)?.select()
        
        Toast.makeText(this, "Itinerary created with $daysNeeded days!", Toast.LENGTH_SHORT).show()
    }
    
    private fun moveItineraryItem(fromDay: Int, toDay: Int, item: ItineraryItem) {
        if (fromDay < itineraryDays.size && toDay < itineraryDays.size) {
            val sourceDay = itineraryDays[fromDay]
            val targetDay = itineraryDays[toDay]
            
            sourceDay.items.remove(item)
            targetDay.items.add(item)
            
            itineraryAdapter.updateItinerary(itineraryDays)
        }
    }
    
    private fun removeItineraryItem(day: Int, item: ItineraryItem) {
        if (day < itineraryDays.size) {
            itineraryDays[day].items.remove(item)
            itineraryAdapter.updateItinerary(itineraryDays)
        }
    }
    
    private fun saveItinerary() {
        if (itineraryDays.isEmpty()) {
            Toast.makeText(this, "Please create an itinerary first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val totalPrice = itineraryDays.sumOf { day ->
            day.items.sumOf { it.price.toInt() }
        }
        
        val message = buildString {
            appendLine("Your Custom Itinerary")
            appendLine("Total Days: ${itineraryDays.size}")
            appendLine("Total Price: AED $totalPrice")
            appendLine()
            
            itineraryDays.forEach { day ->
                appendLine("${day.title}:")
                day.items.forEach { item ->
                    appendLine("• ${item.name} (${item.type}) - ${item.duration}h - AED ${item.price}")
                }
                appendLine()
            }
        }
        
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Save Itinerary")
            .setMessage(message)
            .setPositiveButton("Save") { _, _ ->
                Toast.makeText(this, "Itinerary saved successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 