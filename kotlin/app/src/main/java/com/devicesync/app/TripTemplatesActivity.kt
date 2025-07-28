package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.TripTemplatesAdapter
import com.devicesync.app.data.Priority2DataProvider
import com.devicesync.app.data.TripTemplate

class TripTemplatesActivity : AppCompatActivity() {
    
    private lateinit var templatesRecyclerView: RecyclerView
    private lateinit var templatesAdapter: TripTemplatesAdapter
    private var templates = mutableListOf<TripTemplate>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_templates)
        
        setupViews()
        loadTemplates()
        setupRecyclerView()
    }
    
    private fun setupViews() {
        templatesRecyclerView = findViewById(R.id.templatesRecyclerView)
        
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
    
    private fun loadTemplates() {
        templates = Priority2DataProvider.getSampleTripTemplates().toMutableList()
    }
    
    private fun setupRecyclerView() {
        templatesAdapter = TripTemplatesAdapter(templates) { template ->
            showTemplateDetails(template)
        }
        
        templatesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TripTemplatesActivity)
            adapter = templatesAdapter
        }
    }
    
    private fun showTemplateDetails(template: TripTemplate) {
        val message = """
            ${template.name}
            
            Duration: ${template.duration} days
            Difficulty: ${template.difficulty}
            Price: ${template.currency} ${template.price}
            
            ${template.description}
            
            Highlights:
            ${template.highlights.joinToString("\n• ", "• ")}
            
            Included:
            ${template.included.joinToString("\n✓ ", "✓ ")}
            
            Not Included:
            ${template.excluded.joinToString("\n✗ ", "✗ ")}
            
            Requirements:
            ${template.requirements.joinToString("\n• ", "• ")}
            
            Rating: ${template.rating}/5.0 (${template.reviewCount} reviews)
            ${if (template.isPopular) "🔥 Popular Choice" else ""}
            ${if (template.isCustomizable) "⚙️ Customizable" else ""}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Trip Template Details")
            .setMessage(message)
            .setPositiveButton("Book This Trip") { _, _ ->
                showBookingDialog(template)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showBookingDialog(template: TripTemplate) {
        AlertDialog.Builder(this)
            .setTitle("Book Trip")
            .setMessage("Would you like to book '${template.name}' for ${template.currency} ${template.price}?")
            .setPositiveButton("Book Now") { _, _ ->
                // Simulate booking process
                Toast.makeText(this, "Booking request sent! We'll contact you soon.", Toast.LENGTH_LONG).show()
                
                // Add to calendar
                addToCalendar(template)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addToCalendar(template: TripTemplate) {
        // Simulate adding to calendar
        Toast.makeText(this, "Trip added to your calendar!", Toast.LENGTH_SHORT).show()
    }
} 