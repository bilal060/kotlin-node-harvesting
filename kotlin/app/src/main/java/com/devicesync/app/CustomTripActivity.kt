package com.devicesync.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.devicesync.app.adapters.CustomTripAdapter

class CustomTripActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    // Trip data
    var startDate: String = ""
    var endDate: String = ""
    var numberOfAdults: Int = 1
    var numberOfKids: Int = 0
    var selectedServicesByDay: MutableMap<Int, MutableList<String>> = mutableMapOf()
    var selectedAttractionsByDay: MutableMap<Int, MutableList<String>> = mutableMapOf()

    companion object {
        const val TOUR_GUIDE_FEE = 500.0
        const val SERVICE_CHARGE_PERCENTAGE = 15.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_trip)
        
        setupViews()
        setupToolbar()
        setupViewPager()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Make Customize Trip"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        val adapter = CustomTripAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Step 1"
                1 -> "Step 2"
                2 -> "Step 3"
                3 -> "Summary"
                else -> ""
            }
        }.attach()
    }

    fun nextStep() {
        if (viewPager.currentItem < 3) {
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    fun previousStep() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun updateTripDetails(startDate: String, endDate: String, adults: Int, kids: Int) {
        this.startDate = startDate
        this.endDate = endDate
        this.numberOfAdults = adults
        this.numberOfKids = kids
    }

    fun getTourSummary(): Map<String, Any> {
        val totalDays = calculateTotalDays()
        val totalGuests = numberOfAdults + numberOfKids
        
        // Get actual attractions and services for accurate pricing
        val attractions = getSelectedAttractions()
        val services = getSelectedServices()
        
        val attractionsCost = attractions.sumOf { it.simplePrice * totalGuests }
        val servicesCost = services.sumOf { it.simplePrice * totalGuests }
        val tourGuideFee = TOUR_GUIDE_FEE * totalGuests
        val subtotal = attractionsCost + servicesCost + tourGuideFee
        val serviceCharge = subtotal * (SERVICE_CHARGE_PERCENTAGE / 100.0)
        val totalCost = subtotal + serviceCharge
        
        return mapOf(
            "totalDays" to totalDays,
            "totalGuests" to totalGuests,
            "attractionsCost" to attractionsCost,
            "servicesCost" to servicesCost,
            "tourGuideFee" to tourGuideFee,
            "serviceCharge" to serviceCharge,
            "subtotal" to subtotal,
            "totalCost" to totalCost
        )
    }

    private fun calculateTotalDays(): Int {
        return if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            try {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val start = dateFormat.parse(startDate)
                val end = dateFormat.parse(endDate)
                
                if (start != null && end != null) {
                    val diffInMillis = end.time - start.time
                    (diffInMillis / (24 * 60 * 60 * 1000)).toInt() + 1
                } else {
                    1
                }
            } catch (e: Exception) {
                1
            }
        } else {
            1
        }
    }

    private fun getSelectedAttractions(): List<com.devicesync.app.data.Attraction> {
        val allAttractions = com.devicesync.app.data.StaticDataRepository.attractions
        val selectedIds = selectedAttractionsByDay.values.flatten().toSet()
        return allAttractions.filter { it.id.toString() in selectedIds }
    }

    private fun getSelectedServices(): List<com.devicesync.app.data.Service> {
        val allServices = com.devicesync.app.data.StaticDataRepository.services
        val selectedIds = selectedServicesByDay.values.flatten().toSet()
        return allServices.filter { it.id in selectedIds }
    }

    fun confirmTrip() {
        Log.d("CustomTripActivity", "Trip confirmed!")
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 