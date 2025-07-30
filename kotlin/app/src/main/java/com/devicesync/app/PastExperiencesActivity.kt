package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.PastExperienceAdapter
import com.devicesync.app.data.models.PastExperience

class PastExperiencesActivity : AppCompatActivity() {
    
    private lateinit var experiencesRecyclerView: RecyclerView
    private lateinit var experiencesAdapter: PastExperienceAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_experiences)
        
        setupViews()
        loadExperiencesData()
    }
    
    private fun setupViews() {
        experiencesRecyclerView = findViewById(R.id.experiencesRecyclerView)
        experiencesRecyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadExperiencesData() {
        val experiences = listOf(
            PastExperience(
                id = "1",
                title = "Luxury Desert Safari Experience",
                description = "An unforgettable evening in the Dubai desert with traditional entertainment, gourmet dinner, and stargazing.",
                date = "March 2024",
                location = "Dubai Desert",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
                    "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400",
                    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400"
                ),
                videoUrls = listOf("https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4"),
                highlights = listOf("Dune bashing", "Camel riding", "Traditional dance", "BBQ dinner"),
                rating = 4.8f,
                participants = 45
            ),
            PastExperience(
                id = "2",
                title = "Dubai City Tour & Burj Khalifa",
                description = "Comprehensive city tour including the world's tallest building with breathtaking views from the 124th floor.",
                date = "February 2024",
                location = "Dubai City",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                highlights = listOf("Burj Khalifa", "Dubai Mall", "Palm Jumeirah", "Dubai Frame"),
                rating = 4.9f,
                participants = 78
            ),
            PastExperience(
                id = "3",
                title = "Abu Dhabi Day Trip",
                description = "Explore the capital city with visits to Sheikh Zayed Grand Mosque, Ferrari World, and Emirates Palace.",
                date = "January 2024",
                location = "Abu Dhabi",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                videoUrls = listOf("https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4"),
                highlights = listOf("Sheikh Zayed Mosque", "Ferrari World", "Emirates Palace", "Louvre Abu Dhabi"),
                rating = 4.7f,
                participants = 32
            ),
            PastExperience(
                id = "4",
                title = "Dubai Food Tour",
                description = "Culinary journey through Dubai's diverse food scene, from traditional Emirati cuisine to international flavors.",
                date = "December 2023",
                location = "Dubai Various Locations",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1504674900240-9c9c0c1d0b1a?w=400",
                    "https://images.unsplash.com/photo-1504674900240-9c9c0c1d0b1a?w=400"
                ),
                highlights = listOf("Traditional Emirati food", "Street food", "Fine dining", "Spice souk"),
                rating = 4.6f,
                participants = 28
            ),
            PastExperience(
                id = "5",
                title = "Dubai Marina & Palm Jumeirah",
                description = "Luxury yacht cruise around Dubai Marina and Palm Jumeirah with stunning skyline views.",
                date = "November 2023",
                location = "Dubai Marina",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400"
                ),
                videoUrls = listOf("https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4"),
                highlights = listOf("Yacht cruise", "Marina views", "Palm Jumeirah", "Sunset photography"),
                rating = 4.8f,
                participants = 15
            )
        )
        
        experiencesAdapter = PastExperienceAdapter(experiences) { _ ->
            // Handle experience click - show detailed view
            Toast.makeText(this, "Experience details coming soon!", Toast.LENGTH_SHORT).show()
        }
        experiencesRecyclerView.adapter = experiencesAdapter
    }
} 