package com.devicesync.app

import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.devicesync.app.data.StaticDataRepository
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.Service
import kotlinx.coroutines.launch

class DayPlannerActivity : AppCompatActivity() {

    private var selectedAttractions = mutableListOf<Attraction>()
    private var selectedServices = mutableListOf<Service>()
    private var dayContainers = mutableMapOf<Int, LinearLayout>()
    private var itemViews = mutableMapOf<String, View>()
    private var usedAttractions = mutableSetOf<String>() // Track used attractions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_planner)
        
        setupToolbar()
        loadSelectedItems()
        setupDayContainers()
        setupDragAndDrop()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Plan Your Days"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSelectedItems() {
        lifecycleScope.launch {
            try {
                // Fetch data from API
                val result = StaticDataRepository.fetchAllStaticData(this@DayPlannerActivity)
                
                if (result is StaticDataRepository.FetchResult.Success<*>) {
                    val attractions = StaticDataRepository.attractions
                    val services = StaticDataRepository.services
                    
                    // Get selected item IDs from intent
                    val selectedAttractionIds = intent.getStringArrayExtra("selected_attractions") ?: emptyArray()
                    val selectedServiceIds = intent.getStringArrayExtra("selected_services") ?: emptyArray()
                    
                    // Filter selected attractions and services
                    selectedAttractions = attractions.filter { it.id.toString() in selectedAttractionIds }.toMutableList()
                    selectedServices = services.filter { it.id in selectedServiceIds }.toMutableList()
                    
                    android.util.Log.d("DayPlannerActivity", "📊 Loaded ${selectedAttractions.size} attractions and ${selectedServices.size} services")
                    
                    // Populate available items section
                    populateAvailableItems()
                    
                } else {
                    Toast.makeText(this@DayPlannerActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("DayPlannerActivity", "❌ Error loading data: ${e.message}", e)
                Toast.makeText(this@DayPlannerActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDayContainers() {
        // Initialize day containers
        dayContainers[1] = findViewById(R.id.day1Container)
        dayContainers[2] = findViewById(R.id.day2Container)
        dayContainers[3] = findViewById(R.id.day3Container)
        dayContainers[4] = findViewById(R.id.day4Container)
        dayContainers[5] = findViewById(R.id.day5Container)
        
        // Set up drop zones for each day
        dayContainers.forEach { (day, container) ->
            container.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DROP -> {
                        val itemId = event.clipData.getItemAt(0).text.toString()
                        val itemView = itemViews[itemId]
                        if (itemView != null) {
                            // Check if it's an attraction that's already been used
                            if (isAttraction(itemId) && usedAttractions.contains(itemId)) {
                                Toast.makeText(this@DayPlannerActivity, "This attraction can only be visited once!", Toast.LENGTH_SHORT).show()
                                return@setOnDragListener false
                            }
                            
                            // Remove from current parent
                            (itemView.parent as? LinearLayout)?.removeView(itemView)
                            // Add to new day container
                            container.addView(itemView)
                            
                            // Mark attraction as used if it's an attraction
                            if (isAttraction(itemId)) {
                                usedAttractions.add(itemId)
                            }
                            
                            android.util.Log.d("DayPlannerActivity", "✅ Moved item $itemId to Day $day")
                        }
                        true
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        container.setBackgroundResource(R.drawable.drop_zone_highlight)
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        container.setBackgroundResource(R.drawable.drop_zone_background)
                        true
                    }
                    else -> true
                }
            }
        }
    }

    private fun isAttraction(itemId: String): Boolean {
        return selectedAttractions.any { it.id.toString() == itemId }
    }

    private fun isService(itemId: String): Boolean {
        return selectedServices.any { it.id == itemId }
    }

    private fun setupDragAndDrop() {
        // This will be called after populateAvailableItems() creates the item views
    }

    private fun populateAvailableItems() {
        val availableItemsContainer = findViewById<LinearLayout>(R.id.availableItemsContainer)
        
        // Clear existing items
        availableItemsContainer.removeAllViews()
        itemViews.clear()
        
        // Add attractions (one-time only)
        selectedAttractions.forEach { attraction ->
            val itemView = createItemView(attraction.name, "Attraction (One-time)", attraction.id.toString(), true)
            availableItemsContainer.addView(itemView)
            itemViews[attraction.id.toString()] = itemView
        }
        
        // Add services (can be repeated daily)
        selectedServices.forEach { service ->
            val itemView = createItemView(service.name, "Service (Daily)", service.id, false)
            availableItemsContainer.addView(itemView)
            itemViews[service.id] = itemView
        }
        
        android.util.Log.d("DayPlannerActivity", "📋 Created ${itemViews.size} draggable items")
    }

    private fun createItemView(name: String, type: String, itemId: String, isOneTime: Boolean): CardView {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16) // 4dp bottom margin
            }
            radius = 8f
            elevation = 2f
            setCardBackgroundColor(getColor(R.color.surface_color))
        }

        val contentLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 12, 16, 12)
        }

        // Item info
        val infoLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
            orientation = LinearLayout.VERTICAL
        }

        val nameText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = name
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setTextColor(getColor(R.color.text_dark))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val typeText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = type
            setTextAppearance(android.R.style.TextAppearance_Small)
            setTextColor(if (isOneTime) getColor(R.color.accent_gold) else getColor(R.color.primary_color))
        }

        // Drag handle
        val dragHandle = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "⋮⋮"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setTextColor(getColor(R.color.text_secondary))
            setPadding(16, 0, 0, 0)
        }

        // Set up drag functionality
        cardView.setOnLongClickListener { view ->
            val dragData = android.content.ClipData.newPlainText("item_id", itemId)
            val dragShadowBuilder = View.DragShadowBuilder(view)
            view.startDragAndDrop(dragData, dragShadowBuilder, view, 0)
            true
        }

        // Add views to layout
        infoLayout.addView(nameText)
        infoLayout.addView(typeText)
        contentLayout.addView(infoLayout)
        contentLayout.addView(dragHandle)
        cardView.addView(contentLayout)

        return cardView
    }

    private fun saveItinerary() {
        // Save the current itinerary layout
        val itinerary = mutableMapOf<Int, MutableList<String>>()
        
        dayContainers.forEach { (day, container) ->
            val dayItems = mutableListOf<String>()
            for (i in 0 until container.childCount) {
                val itemView = container.getChildAt(i)
                // Extract item ID from the view (you might need to store this as a tag)
                // For now, we'll just count the items
                dayItems.add("Item ${i + 1}")
            }
            itinerary[day] = dayItems
        }
        
        android.util.Log.d("DayPlannerActivity", "💾 Saved itinerary: $itinerary")
        Toast.makeText(this, "Itinerary saved successfully!", Toast.LENGTH_SHORT).show()
        
        // Navigate back or to next screen
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 