package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.devicesync.app.data.StaticDataRepository
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.Service
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TourPlanningActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    // Tour planning data
    private var startDate: String = ""
    private var endDate: String = ""
    private var numberOfAdults: Int = 1
    private var numberOfKids: Int = 0
    private var selectedServicesByDay = mutableMapOf<Int, MutableList<Service>>()
    private var selectedAttractionsByDay = mutableMapOf<Int, MutableList<Attraction>>()
    
    // Pricing constants
    private val TOUR_GUIDE_FEE = 500.0
    private val SERVICE_CHARGE_PERCENTAGE = 15.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_planning)
        
        setupToolbar()
        setupViewPager()
        loadData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Plan Your Tour"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        // For now, just show a simple message
        val messageView = TextView(this).apply {
            text = "Tour Planning Coming Soon!\n\nThis will include:\n• Date and guest selection\n• Service planning by day\n• Attraction planning by day\n• Cost calculation with tour guide fee"
            setPadding(32, 32, 32, 32)
            textSize = 16f
        }
        
        // Disable swipe between pages
        viewPager.isUserInputEnabled = false
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("TourPlanningActivity", "🔄 Loading tour planning data...")
                
                val result = StaticDataRepository.fetchAllStaticData(this@TourPlanningActivity)
                
                if (result is StaticDataRepository.FetchResult.Success<*>) {
                    android.util.Log.d("TourPlanningActivity", "✅ Successfully loaded tour planning data")
                    
                    // Initialize data structures
                    val attractions = StaticDataRepository.attractions
                    val services = StaticDataRepository.services
                    
                    // Initialize selected items by day
                    for (day in 1..5) {
                        selectedServicesByDay[day] = mutableListOf()
                        selectedAttractionsByDay[day] = mutableListOf()
                    }
                    
                    android.util.Log.d("TourPlanningActivity", "📊 Loaded ${attractions.size} attractions and ${services.size} services")
                    
                } else {
                    android.util.Log.e("TourPlanningActivity", "❌ Failed to load data: $result")
                    Toast.makeText(this@TourPlanningActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("TourPlanningActivity", "❌ Error loading data: ${e.message}", e)
                Toast.makeText(this@TourPlanningActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateTourDetails(startDate: String, endDate: String, adults: Int, kids: Int) {
        this.startDate = startDate
        this.endDate = endDate
        this.numberOfAdults = adults
        this.numberOfKids = kids
        
        android.util.Log.d("TourPlanningActivity", "📅 Updated tour details: $startDate to $endDate, $adults adults, $kids kids")
    }

    fun updateServicesForDay(day: Int, services: List<Service>) {
        selectedServicesByDay[day] = services.toMutableList()
        android.util.Log.d("TourPlanningActivity", "🔄 Updated services for Day $day: ${services.size} services")
    }

    fun updateAttractionsForDay(day: Int, attractions: List<Attraction>) {
        selectedAttractionsByDay[day] = attractions.toMutableList()
        android.util.Log.d("TourPlanningActivity", "🔄 Updated attractions for Day $day: ${attractions.size} attractions")
    }

    fun getTourSummary(): TourSummary {
        val totalDays = calculateTotalDays()
        val totalGuests = numberOfAdults + numberOfKids
        
        // Calculate costs
        val attractionsCost = selectedAttractionsByDay.values.flatten().sumOf { (it.rating * 50).toDouble() }
        val servicesCost = selectedServicesByDay.values.flatten().sumOf { (it.rating * 75).toDouble() }
        val subtotal = attractionsCost + servicesCost
        val tourGuideFee = TOUR_GUIDE_FEE
        val serviceCharge = (subtotal + tourGuideFee) * (SERVICE_CHARGE_PERCENTAGE / 100.0)
        val totalCost = subtotal + tourGuideFee + serviceCharge
        
        return TourSummary(
            startDate = startDate,
            endDate = endDate,
            totalDays = totalDays,
            numberOfAdults = numberOfAdults,
            numberOfKids = numberOfKids,
            totalGuests = totalGuests,
            attractionsCost = attractionsCost,
            servicesCost = servicesCost,
            tourGuideFee = tourGuideFee,
            serviceCharge = serviceCharge,
            totalCost = totalCost,
            selectedServicesByDay = selectedServicesByDay,
            selectedAttractionsByDay = selectedAttractionsByDay
        )
    }

    private fun calculateTotalDays(): Int {
        if (startDate.isEmpty() || endDate.isEmpty()) return 0
        
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            
            if (start != null && end != null) {
                val diffInMillis = end.time - start.time
                val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                return diffInDays.toInt() + 1 // Include both start and end dates
            }
        } catch (e: Exception) {
            android.util.Log.e("TourPlanningActivity", "❌ Error calculating days: ${e.message}")
        }
        
        return 0
    }

    fun nextStep() {
        val currentItem = viewPager.currentItem
        if (currentItem < 3) {
            viewPager.currentItem = currentItem + 1
        }
    }

    fun previousStep() {
        val currentItem = viewPager.currentItem
        if (currentItem > 0) {
            viewPager.currentItem = currentItem - 1
        }
    }

    fun finishTourPlanning() {
        val summary = getTourSummary()
        android.util.Log.d("TourPlanningActivity", "🎉 Tour planning completed: AED ${summary.totalCost}")
        
        Toast.makeText(this, "Tour planning completed! Total: AED ${summary.totalCost}", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class TourSummary(
    val startDate: String,
    val endDate: String,
    val totalDays: Int,
    val numberOfAdults: Int,
    val numberOfKids: Int,
    val totalGuests: Int,
    val attractionsCost: Double,
    val servicesCost: Double,
    val tourGuideFee: Double,
    val serviceCharge: Double,
    val totalCost: Double,
    val selectedServicesByDay: Map<Int, List<Service>>,
    val selectedAttractionsByDay: Map<Int, List<Attraction>>
) 