package com.devicesync.app

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.devicesync.app.adapters.ImageSlideshowAdapter
import com.devicesync.app.data.Service
import com.devicesync.app.viewmodels.ServicesViewModel

class ServiceSlideshowActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ServicesViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ImageSlideshowAdapter
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var priceText: TextView
    private lateinit var pageIndicator: TextView
    
    private var serviceId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_slideshow)
        
        serviceId = intent.getStringExtra("service_id") ?: ""
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        descriptionText = findViewById(R.id.descriptionText)
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
        viewModel = ViewModelProvider(this)[ServicesViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.selectedService.observe(this) { service ->
            service?.let { updateServiceInfo(it) }
        }
        
        // Load the specific service
        loadServiceById(serviceId)
    }
    
    private fun updateServiceInfo(service: Service) {
        titleText.text = service.name
        descriptionText.text = service.description
        priceText.text = "From ${service.currency} ${service.averageCost.values.minOrNull()} ${service.unit}"
        
        // Update slideshow
        adapter.updateImages(service.images)
        updatePageIndicator(0)
    }
    
    private fun updatePageIndicator(currentPosition: Int) {
        val totalImages = adapter.itemCount
        pageIndicator.text = "${currentPosition + 1} / $totalImages"
    }
    
    private fun loadServiceById(id: String) {
        viewModel.loadServiceById(id)
    }
} 