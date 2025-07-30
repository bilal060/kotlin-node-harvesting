package com.devicesync.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.ReviewsAdapter
import com.devicesync.app.data.DummyDataProvider
import com.devicesync.app.data.Review

class ReviewsActivity : AppCompatActivity() {
    
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var reviews = mutableListOf<Review>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)
        
        setupViews()
        loadReviews()
        setupRecyclerView()
    }
    
    private fun setupViews() {
        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
    }
    
    private fun loadReviews() {
        reviews = DummyDataProvider.reviews.toMutableList()
    }
    
    private fun setupRecyclerView() {
        reviewsAdapter = ReviewsAdapter(reviews) { review ->
            // Handle review click - show review details
            showReviewDetails(review)
        }
        
        reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReviewsActivity)
            adapter = reviewsAdapter
        }
    }
    
    private fun showAddReviewDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val commentEditText = dialogView.findViewById<EditText>(R.id.commentEditText)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val locationEditText = dialogView.findViewById<EditText>(R.id.locationEditText)
        
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Add Your Review")
            .setView(dialogView)
            .setPositiveButton("Post Review") { _, _ ->
                val title = titleEditText.text.toString()
                val comment = commentEditText.text.toString()
                val rating = ratingBar.rating
                val location = locationEditText.text.toString()
                
                if (title.isNotEmpty() && comment.isNotEmpty() && rating > 0) {
                    addNewReview(title, comment, rating, location)
                } else {
                    Toast.makeText(this, "Please fill all fields and add a rating", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addNewReview(title: String, comment: String, rating: Float, location: String) {
        val newReview = Review(
            id = (reviews.size + 1).toString(),
            userId = "current_user",
            userName = "You",
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&h=150&fit=crop&crop=face",
            rating = rating,
            title = title,
            comment = comment,
            date = System.currentTimeMillis(),
            location = location,
            helpfulCount = 0,
            isVerified = false,
            images = emptyList(),
            tags = listOf("New Review")
        )
        
        reviews.add(0, newReview)
        reviewsAdapter.updateReviews(reviews)
        Toast.makeText(this, "Review posted successfully!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showReviewDetails(review: Review) {
        val message = """
            ${review.title}
            
            Rating: ${review.rating}/5.0
            Location: ${review.location}
            Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(review.date))}
            
            ${review.comment}
            
            Helpful: ${review.helpfulCount} people
            ${if (review.isVerified) "✓ Verified Review" else ""}
        """.trimIndent()
        
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Review Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 