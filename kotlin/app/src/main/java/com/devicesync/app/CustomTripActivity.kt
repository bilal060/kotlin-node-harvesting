package com.devicesync.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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
        // TODO: Implement adapter and fragments
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
        val totalDays = 5 // Placeholder
        val totalGuests = numberOfAdults + numberOfKids
        val attractionsCost = selectedAttractionsByDay.values.flatten().size * 50.0
        val servicesCost = selectedServicesByDay.values.flatten().size * 75.0
        val subtotal = attractionsCost + servicesCost + TOUR_GUIDE_FEE
        val serviceCharge = subtotal * (SERVICE_CHARGE_PERCENTAGE / 100.0)
        val totalCost = subtotal + serviceCharge
        
        return mapOf(
            "totalDays" to totalDays,
            "totalGuests" to totalGuests,
            "attractionsCost" to attractionsCost,
            "servicesCost" to servicesCost,
            "tourGuideFee" to TOUR_GUIDE_FEE,
            "serviceCharge" to serviceCharge,
            "subtotal" to subtotal,
            "totalCost" to totalCost
        )
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