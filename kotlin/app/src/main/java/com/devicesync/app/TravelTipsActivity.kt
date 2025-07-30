package com.devicesync.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.TravelTipsAdapter
import com.devicesync.app.data.TravelTip

class TravelTipsActivity : AppCompatActivity() {
    

    private lateinit var tipsRecyclerView: RecyclerView
    private lateinit var tipsAdapter: TravelTipsAdapter
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, TravelTipsActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_tips)
        
        initializeViews()
        setupRecyclerView()
        loadTravelTips()
    }
    
    private fun initializeViews() {
        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        tipsRecyclerView = findViewById(R.id.tipsRecyclerView)
    }
    
    private fun setupRecyclerView() {
        tipsAdapter = TravelTipsAdapter()
        tipsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TravelTipsActivity)
            adapter = tipsAdapter
        }
    }
    
    private fun loadTravelTips() {
        val travelTips = listOf(
            TravelTip(
                "Best Time to Visit",
                "Visit between November and March for pleasant weather. Summer months (June to September) can be extremely hot with temperatures reaching 45°C (113°F).",
                "🌤️"
            ),
            TravelTip(
                "Dress Code",
                "Respect local customs, especially in religious sites. Women should cover shoulders and knees. Swimwear is acceptable at beaches and pools.",
                "👗"
            ),
            TravelTip(
                "Transportation",
                "Use Dubai Metro for efficient city travel. Taxis are readily available. Consider purchasing a Nol card for public transport.",
                "🚇"
            ),
            TravelTip(
                "Currency",
                "UAE Dirham (AED) is the local currency. Credit cards are widely accepted. ATMs are available throughout the city.",
                "💰"
            ),
            TravelTip(
                "Language",
                "Arabic is the official language, but English is widely spoken. Most signs and menus are in both languages.",
                "🗣️"
            ),
            TravelTip(
                "Safety",
                "Dubai is one of the safest cities in the world. Crime rates are very low. Emergency number is 999.",
                "🛡️"
            ),
            TravelTip(
                "Tipping",
                "Tipping is appreciated but not mandatory. 10-15% in restaurants, AED 5-10 for taxi rides.",
                "💳"
            ),
            TravelTip(
                "WiFi & Internet",
                "Free WiFi is available in most malls, hotels, and public places. Consider getting a local SIM card for data.",
                "📶"
            ),
            TravelTip(
                "Shopping",
                "Dubai is a shopping paradise. Visit during Dubai Shopping Festival (December-January) for great deals.",
                "🛍️"
            ),
            TravelTip(
                "Photography",
                "Ask permission before taking photos of people. Some government buildings and military areas prohibit photography.",
                "📸"
            )
        )
        
        tipsAdapter.submitList(travelTips)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 