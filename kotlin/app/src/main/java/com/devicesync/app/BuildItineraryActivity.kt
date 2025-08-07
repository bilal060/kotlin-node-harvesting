package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.devicesync.app.data.StaticDataRepository
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.Service
import kotlinx.coroutines.launch

class BuildItineraryActivity : AppCompatActivity() {

    private var selectedAttractions = mutableListOf<Attraction>()
    private var selectedServices = mutableListOf<Service>()
    private var totalCost = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build_itinerary)
        
        setupToolbar()
        setupClickListeners()
        loadAttractionsAndServices()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Select Attractions button
        val selectAttractionsButton = findViewById<MaterialButton>(R.id.selectAttractionsButton)
        selectAttractionsButton?.setOnClickListener {
            showAttractionsSelectionDialog()
        }

        // Select Services button
        val selectServicesButton = findViewById<MaterialButton>(R.id.selectServicesButton)
        selectServicesButton?.setOnClickListener {
            showServicesSelectionDialog()
        }

        // Plan Your Days button
        val planYourDaysButton = findViewById<MaterialButton>(R.id.planYourDaysButton)
        planYourDaysButton?.setOnClickListener {
            planYourDays()
        }

        // Build My Itinerary button
        val buildItineraryButton = findViewById<MaterialButton>(R.id.buildItineraryButton)
        buildItineraryButton?.setOnClickListener {
            buildMyItinerary()
        }
    }

    private fun loadAttractionsAndServices() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("BuildItineraryActivity", "🔄 Loading attractions and services from API...")
                
                // Fetch data from production API
                val result = StaticDataRepository.fetchAllStaticData(this@BuildItineraryActivity)
                
                if (result is StaticDataRepository.FetchResult.Success<*>) {
                    android.util.Log.d("BuildItineraryActivity", "✅ Successfully fetched data from API")
                    
                    val attractions = StaticDataRepository.attractions
                    val services = StaticDataRepository.services
                    
                    android.util.Log.d("BuildItineraryActivity", "📊 Loaded ${attractions.size} attractions and ${services.size} services")
                    
                    // Update trip summary
                    updateTripSummary()
                    
                } else {
                    android.util.Log.e("BuildItineraryActivity", "❌ Failed to fetch data from API: $result")
                    Toast.makeText(this@BuildItineraryActivity, "Failed to load attractions and services", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("BuildItineraryActivity", "❌ Error loading data: ${e.message}", e)
                Toast.makeText(this@BuildItineraryActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAttractionsSelectionDialog() {
        val attractions = StaticDataRepository.attractions
        if (attractions.isEmpty()) {
            Toast.makeText(this, "No attractions available", Toast.LENGTH_SHORT).show()
            return
        }

        val attractionNames = attractions.map { "${it.name} (One-time visit)" }.toTypedArray()
        val checkedItems = BooleanArray(attractions.size) { false }
        
        // Pre-check already selected attractions
        attractions.forEachIndexed { index, attraction ->
            checkedItems[index] = selectedAttractions.contains(attraction)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Attractions (One-time visits)")
            .setMultiChoiceItems(attractionNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Done") { _, _ ->
                // Update selected attractions
                selectedAttractions.clear()
                attractions.forEachIndexed { index, attraction ->
                    if (checkedItems[index]) {
                        selectedAttractions.add(attraction)
                    }
                }
                updateTripSummary()
                updateSelectedItemsDisplay()
                android.util.Log.d("BuildItineraryActivity", "✅ Selected ${selectedAttractions.size} attractions")
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showServicesSelectionDialog() {
        val services = StaticDataRepository.services
        if (services.isEmpty()) {
            Toast.makeText(this, "No services available", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceNames = services.map { "${it.name} (Can repeat daily)" }.toTypedArray()
        val checkedItems = BooleanArray(services.size) { false }
        
        // Pre-check already selected services
        services.forEachIndexed { index, service ->
            checkedItems[index] = selectedServices.contains(service)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Services (Can be repeated daily)")
            .setMultiChoiceItems(serviceNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Done") { _, _ ->
                // Update selected services
                selectedServices.clear()
                services.forEachIndexed { index, service ->
                    if (checkedItems[index]) {
                        selectedServices.add(service)
                    }
                }
                updateTripSummary()
                updateSelectedItemsDisplay()
                android.util.Log.d("BuildItineraryActivity", "✅ Selected ${selectedServices.size} services")
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateSelectedItemsDisplay() {
        val selectedItemsText = findViewById<TextView>(R.id.selectedItemsText)
        val totalItems = selectedAttractions.size + selectedServices.size
        
        val itemsList = mutableListOf<String>()
        itemsList.addAll(selectedAttractions.map { it.name })
        itemsList.addAll(selectedServices.map { it.name })
        
        val displayText = if (totalItems > 0) {
            "Selected: ${itemsList.take(3).joinToString(", ")}${if (totalItems > 3) " +${totalItems - 3} more" else ""}"
        } else {
            "No items selected"
        }
        
        selectedItemsText?.text = displayText
    }

    private fun updateTripSummary() {
        val totalCostText = findViewById<TextView>(R.id.totalCostText)
        val selectedAttractionsText = findViewById<TextView>(R.id.selectedAttractionsText)
        
        // Calculate total cost
        val attractionsCost = selectedAttractions.sumOf { (it.rating * 50).toDouble() }
        val servicesCost = selectedServices.sumOf { (it.rating * 75).toDouble() }
        totalCost = attractionsCost + servicesCost
        
        // Update UI
        totalCostText?.text = "AED ${totalCost.toInt()}"
        selectedAttractionsText?.text = "${selectedAttractions.size + selectedServices.size} Selected"
        
        android.util.Log.d("BuildItineraryActivity", "💰 Updated trip summary: ${selectedAttractions.size} attractions, ${selectedServices.size} services, AED ${totalCost.toInt()}")
    }

    private fun planYourDays() {
        // Navigate to the new custom trip planning flow
        val intent = Intent(this, CustomTripActivity::class.java)
        startActivity(intent)
    }

    private fun buildMyItinerary() {
        if (selectedAttractions.isEmpty() && selectedServices.isEmpty()) {
            Toast.makeText(this, "Please select at least one attraction or service", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Navigate to day selection screen
        val intent = Intent(this, ItineraryDaySelectionActivity::class.java)
        intent.putExtra("selected_attractions_count", selectedAttractions.size)
        intent.putExtra("selected_services_count", selectedServices.size)
        intent.putExtra("total_cost", totalCost)
        startActivity(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 