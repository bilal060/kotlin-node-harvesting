package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.devicesync.app.adapters.AudioToursAdapter
import com.devicesync.app.data.Priority2DataProvider
import com.devicesync.app.data.AudioTour
import com.devicesync.app.services.AudioTourService

class AudioToursActivity : AppCompatActivity() {
    
    private lateinit var audioToursRecyclerView: RecyclerView
    private lateinit var audioToursAdapter: AudioToursAdapter
    private lateinit var audioTourService: AudioTourService
    private var audioTours = mutableListOf<AudioTour>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_tours)
        
        setupViews()
        setupAudioTourService()
        loadAudioTours()
        setupRecyclerView()
    }
    
    private fun setupViews() {
        audioToursRecyclerView = findViewById(R.id.audioToursRecyclerView)
        
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
        
        findViewById<Button>(R.id.filterButton).setOnClickListener {
            showFilterDialog()
        }
    }
    
    private fun setupAudioTourService() {
        audioTourService = AudioTourService(this)
    }
    
    private fun loadAudioTours() {
        audioTours = Priority2DataProvider.getSampleAudioTours().toMutableList()
    }
    
    private fun setupRecyclerView() {
        audioToursAdapter = AudioToursAdapter(audioTours) { audioTour ->
            showAudioTourDetails(audioTour)
        }
        
        audioToursRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AudioToursActivity)
            adapter = audioToursAdapter
        }
    }
    
    private fun showFilterDialog() {
        val options = arrayOf("All Tours", "Free Tours", "English", "Arabic", "Chinese", "Mongolian")
        
        AlertDialog.Builder(this)
            .setTitle("Filter Audio Tours")
            .setItems(options) { _, which ->
                val filterType = when (which) {
                    0 -> "all"
                    1 -> "free"
                    2 -> "en"
                    3 -> "ar"
                    4 -> "zh"
                    5 -> "mn"
                    else -> "all"
                }
                
                filterTours(filterType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun filterTours(filterType: String) {
        val filteredTours = when (filterType) {
            "free" -> audioTours.filter { it.isFree }
            "en" -> audioTours.filter { it.language == "English" }
            "ar" -> audioTours.filter { it.language == "Arabic" }
            "zh" -> audioTours.filter { it.language == "Chinese" }
            "mn" -> audioTours.filter { it.language == "Mongolian" }
            else -> audioTours
        }
        
        audioToursAdapter.updateAudioTours(filteredTours)
        Toast.makeText(this, "Showing ${filteredTours.size} tours", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAudioTourDetails(audioTour: AudioTour) {
        val message = """
            ${audioTour.title}
            
            Duration: ${audioTour.duration} minutes
            Language: ${audioTour.language}
            Stops: ${audioTour.stops.size}
            ${if (audioTour.isFree) "🎁 FREE" else "Price: ${audioTour.price}"}
            
            ${audioTour.description}
            
            Stops:
            ${audioTour.stops.joinToString("\n• ", "• ") { it.title }}
            
            Rating: ${audioTour.rating}/5.0 (${audioTour.downloadCount} downloads)
            Downloads: ${audioTour.downloadCount}
        """.trimIndent()
        
        val options = if (audioTour.isFree) {
            arrayOf("Play Tour", "Download", "View Stops", "Cancel")
        } else {
            arrayOf("Purchase & Play", "View Stops", "Cancel")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Audio Tour Details")
            .setMessage(message)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (audioTour.isFree) {
                            playAudioTour(audioTour)
                        } else {
                            purchaseAndPlay(audioTour)
                        }
                    }
                    1 -> {
                        if (audioTour.isFree) {
                            downloadAudioTour(audioTour)
                        } else {
                            showStops(audioTour)
                        }
                    }
                    2 -> showStops(audioTour)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun playAudioTour(audioTour: AudioTour) {
        lifecycleScope.launch {
            val result = audioTourService.playAudioTour(audioTour.id)
            if (result.isSuccess) {
                Toast.makeText(this@AudioToursActivity, "Playing ${audioTour.title}...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AudioToursActivity, "Failed to play tour", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun purchaseAndPlay(audioTour: AudioTour) {
        AlertDialog.Builder(this)
            .setTitle("Purchase Audio Tour")
            .setMessage("Purchase '${audioTour.title}' for ${audioTour.price}?")
            .setPositiveButton("Purchase") { _, _ ->
                Toast.makeText(this, "Purchase successful! Playing tour...", Toast.LENGTH_SHORT).show()
                playAudioTour(audioTour)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun downloadAudioTour(audioTour: AudioTour) {
        Toast.makeText(this, "Downloading ${audioTour.title}...", Toast.LENGTH_SHORT).show()
        // Simulate download
        lifecycleScope.launch {
            audioTourService.downloadAudioTour(audioTour.id)
        }
    }
    
    private fun showStops(audioTour: AudioTour) {
        val stopsMessage = audioTour.stops.joinToString("\n\n") { stop ->
            """
            🛑 ${stop.title}
            Duration: ${stop.duration} seconds
            ${stop.description}
            """.trimIndent()
        }
        
        AlertDialog.Builder(this)
            .setTitle("Tour Stops")
            .setMessage(stopsMessage)
            .setPositiveButton("Close", null)
            .show()
    }
} 