package com.devicesync.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.ReviewsAdapter
import com.google.android.material.appbar.MaterialToolbar

class ReviewsActivity : AppCompatActivity() {
    
    private lateinit var reviewsAdapter: ReviewsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)
        
        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Set up navigation
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        setupReviewsList()
    }
    
    private fun setupReviewsList() {
        val recyclerView = findViewById<RecyclerView>(R.id.reviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Create sample reviews
        val sampleReviews = listOf(
            ReviewsAdapter.Review(
                userName = "Sarah Johnson",
                date = "2 days ago",
                title = "Amazing Experience!",
                content = "The tour was absolutely incredible! Our guide was knowledgeable and friendly. The views from the Burj Khalifa were breathtaking. Highly recommend this experience to anyone visiting Dubai."
            ),
            ReviewsAdapter.Review(
                userName = "Michael Chen",
                date = "1 week ago",
                title = "Excellent Service",
                content = "Professional service from start to finish. The desert safari was the highlight of our trip. The staff was very accommodating and the experience was worth every penny."
            ),
            ReviewsAdapter.Review(
                userName = "Emma Wilson",
                date = "3 days ago",
                title = "Wonderful Time",
                content = "We had a fantastic time exploring Dubai with this service. The booking process was smooth and the tour exceeded our expectations. Will definitely use again!"
            )
        )
        
        reviewsAdapter = ReviewsAdapter(sampleReviews)
        recyclerView.adapter = reviewsAdapter
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 