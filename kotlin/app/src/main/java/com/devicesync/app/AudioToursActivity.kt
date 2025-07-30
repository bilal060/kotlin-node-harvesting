package com.devicesync.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import com.devicesync.app.utils.LanguageManager
import java.util.*

class AudioToursActivity : AppCompatActivity() {
    
    private lateinit var audioToursRecyclerView: RecyclerView
    private lateinit var audioToursAdapter: AudioToursAdapter
    private lateinit var audioTourService: AudioTourService
    private lateinit var searchEditText: EditText
    private lateinit var languageButton: Button
    private var audioTours = mutableListOf<AudioTour>()
    private var filteredTours = mutableListOf<AudioTour>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply current language
        LanguageManager.applyLanguageToActivity(this)
        
        setContentView(R.layout.activity_audio_tours)
        
        setupViews()
        setupAudioTourService()
        loadAudioTours()
        setupRecyclerView()
    }
    
    private fun setupViews() {
        audioToursRecyclerView = findViewById(R.id.audioToursRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Language button removed from layout - using toolbar instead
        
        // Setup search functionality
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterToursBySearch(s.toString())
            }
        })
    }
    
    private fun setupAudioTourService() {
        audioTourService = AudioTourService(this)
    }
    
    private fun loadAudioTours() {
        audioTours = Priority2DataProvider.getSampleAudioTours().toMutableList()
    }
    
    private fun setupRecyclerView() {
        filteredTours = audioTours.toMutableList()
        audioToursAdapter = AudioToursAdapter(filteredTours) { audioTour ->
            showAudioTourDetails(audioTour)
        }
        
        audioToursRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AudioToursActivity)
            adapter = audioToursAdapter
        }
    }
    
    private fun showFilterDialog() {
        val options = arrayOf("All Tours", "Free Tours", "English", "Arabic", "Chinese", "Mongolian")
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
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
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
            listView.post {
                for (i in 0 until listView.count) {
                    val child = listView.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(resources.getColor(R.color.text_dark, theme))
                        child.textSize = 16f
                    }
                }
            }
        }
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
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
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
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
            listView.post {
                for (i in 0 until listView.count) {
                    val child = listView.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(resources.getColor(R.color.text_dark, theme))
                        child.textSize = 16f
                    }
                }
            }
        }
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
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
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
        
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Tour Stops")
            .setMessage(stopsMessage)
            .setPositiveButton("Close", null)
            .show()
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Монгол", "Русский", "中文", "Қазақша")
        val languageCodes = arrayOf("en", "mn", "ru", "zh", "kk")
        
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                setAppLanguage(languageCodes[which])
            }
            .create()
        
        dialog.show()
        
        // Force set text color to black for better visibility
        dialog.listView?.let { listView ->
            listView.post {
                for (i in 0 until listView.count) {
                    val child = listView.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(resources.getColor(R.color.text_dark, theme))
                        child.textSize = 16f
                    }
                }
            }
        }
    }
    
    private fun setAppLanguage(languageCode: String) {
        LanguageManager.restartActivityWithLanguage(this, languageCode)
    }
    
    private fun filterToursBySearch(query: String) {
        if (query.isEmpty()) {
            filteredTours = audioTours.toMutableList()
        } else {
            filteredTours = audioTours.filter { tour ->
                tour.title.contains(query, ignoreCase = true) ||
                tour.description.contains(query, ignoreCase = true) ||
                tour.language.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        audioToursAdapter.updateAudioTours(filteredTours)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 