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
import com.devicesync.app.adapters.AttractionListAdapter
import com.devicesync.app.data.Attraction
import com.devicesync.app.viewmodels.AttractionsViewModel

class AttractionsListActivity : AppCompatActivity() {
    
    private lateinit var viewModel: AttractionsViewModel
    private lateinit var adapter: AttractionListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions_list)
        
        setupViews()
        setupViewModel()
        setupObservers()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.attractionsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        searchEditText = findViewById(R.id.searchEditText)
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        
        titleText.text = "Dubai Attractions"
        
        // Setup RecyclerView
        adapter = AttractionListAdapter(
            onAttractionClick = { attraction ->
                // Navigate to image slideshow
                val intent = Intent(this, AttractionSlideshowActivity::class.java)
                intent.putExtra("attraction_id", attraction.id)
                startActivity(intent)
            },
            onFavoriteClick = { attraction ->
                viewModel.toggleFavorite(attraction)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Setup search
        searchEditText.setOnEditorActionListener { _, _, _ ->
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchAttractions(query)
            } else {
                viewModel.loadAttractions()
            }
            true
        }
        
        // Setup back button
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AttractionsViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.attractions.observe(this) { attractions ->
            adapter.updateAttractions(attractions)
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