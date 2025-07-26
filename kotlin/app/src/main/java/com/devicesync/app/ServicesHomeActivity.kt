package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.ServiceCardAdapter
import com.devicesync.app.data.Service
import com.devicesync.app.viewmodels.ServicesViewModel

class ServicesHomeActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ServicesViewModel
    private lateinit var adapter: ServiceCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var viewAllButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_home)
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.servicesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        viewAllButton = findViewById(R.id.viewAllButton)
        
        // Setup RecyclerView
        adapter = ServiceCardAdapter(
            onServiceClick = { service ->
                // Navigate to services list
                val intent = Intent(this, ServicesListActivity::class.java)
                startActivity(intent)
            },
            onFavoriteClick = { service ->
                viewModel.toggleFavorite(service)
            }
        )
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        
        // Setup button click
        viewAllButton.setOnClickListener {
            val intent = Intent(this, ServicesListActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ServicesViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.services.observe(this) { services ->
            // Show first 4 services on home screen
            val featuredServices = services.take(4)
            adapter.updateServices(featuredServices)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            if (error != null) {
                errorText.text = error
                errorText.visibility = View.VISIBLE
            } else {
                errorText.visibility = View.GONE
            }
        }
    }
} 