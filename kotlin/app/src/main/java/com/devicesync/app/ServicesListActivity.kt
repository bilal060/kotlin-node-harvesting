package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.ServiceListAdapter
import com.devicesync.app.data.Service
import com.devicesync.app.viewmodels.ServicesViewModel

class ServicesListActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ServicesViewModel
    private lateinit var adapter: ServiceListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_list)
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.servicesRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        
        titleText.text = "Dubai Services"
        
        // Setup RecyclerView
        adapter = ServiceListAdapter(
            onServiceClick = { service ->
                // Navigate to image slideshow
                val intent = Intent(this, ServiceSlideshowActivity::class.java)
                intent.putExtra("service_id", service.id)
                startActivity(intent)
            },
            onFavoriteClick = { service ->
                viewModel.toggleFavorite(service)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Setup search
        searchEditText.setOnEditorActionListener { _, _, _ ->
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchServices(query)
            } else {
                viewModel.loadServices()
            }
            true
        }
        
        // Setup back button
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ServicesViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.services.observe(this) { services ->
            adapter.updateServices(services)
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