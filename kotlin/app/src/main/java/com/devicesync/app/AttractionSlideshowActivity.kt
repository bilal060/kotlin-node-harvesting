package com.devicesync.app

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.devicesync.app.adapters.ImageSlideshowAdapter
import com.devicesync.app.data.Attraction
import com.devicesync.app.viewmodels.AttractionsViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AttractionSlideshowActivity : AppCompatActivity() {
    
    private lateinit var viewModel: AttractionsViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ImageSlideshowAdapter
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var locationText: TextView
    private lateinit var priceText: TextView
    private lateinit var pageIndicator: TextView
    
    private var attractionId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attraction_slideshow)
        
        attractionId = intent.getIntExtra("attraction_id", 0)
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        locationText = findViewById(R.id.locationText)
        priceText = findViewById(R.id.priceText)
        pageIndicator = findViewById(R.id.pageIndicator)
        
        // Setup ViewPager
        adapter = ImageSlideshowAdapter()
        viewPager.adapter = adapter
        
        // Setup page change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
            }
        })
        
        // Setup back button
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AttractionsViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.selectedAttraction.observe(this) { attraction ->
            attraction?.let { updateAttractionInfo(it) }
        }
        
        // Load the specific attraction
        loadAttractionById(attractionId)
    }
    
    private fun updateAttractionInfo(attraction: Attraction) {
        titleText.text = attraction.name
        locationText.text = attraction.location
        priceText.text = "From AED ${attraction.simplePrice.toInt()}"
        
        // Update slideshow
        adapter.updateImages(attraction.images)
        updatePageIndicator(0)
    }
    
    private fun updatePageIndicator(currentPosition: Int) {
        val totalImages = adapter.itemCount
        pageIndicator.text = "${currentPosition + 1} / $totalImages"
    }
    
    private fun loadAttractionById(id: Int) {
        viewModel.loadAttractionById(id)
    }
} 