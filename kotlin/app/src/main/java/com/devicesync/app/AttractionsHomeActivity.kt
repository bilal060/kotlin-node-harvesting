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
import com.devicesync.app.adapters.AttractionCardAdapter
import com.devicesync.app.data.Attraction
import com.devicesync.app.viewmodels.AttractionsViewModel

class AttractionsHomeActivity : AppCompatActivity() {
    
    private lateinit var viewModel: AttractionsViewModel
    private lateinit var adapter: AttractionCardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var viewAllButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions_home)
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.attractionsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        viewAllButton = findViewById(R.id.viewAllButton)
        
        // Setup RecyclerView
        adapter = AttractionCardAdapter(
            onAttractionClick = { attraction ->
                // Navigate to attractions list
                val intent = Intent(this, AttractionsListActivity::class.java)
                startActivity(intent)
            },
            onFavoriteClick = { attraction ->
                viewModel.toggleFavorite(attraction)
            }
        )
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        
        // Setup button click
        viewAllButton.setOnClickListener {
            val intent = Intent(this, AttractionsListActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupViewModel() {
        // Note: In a real app, you'd use dependency injection
        // For now, we'll create a simple instance
        viewModel = ViewModelProvider(this)[AttractionsViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.attractions.observe(this) { attractions ->
            // Show first 4 attractions on home screen
            val featuredAttractions = attractions.take(4)
            adapter.updateAttractions(featuredAttractions)
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