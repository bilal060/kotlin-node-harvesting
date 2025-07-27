package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Review

class ReviewsAdapter(
    private var reviews: List<Review>,
    private val onReviewClick: (Review) -> Unit
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val reviewText: TextView = itemView.findViewById(R.id.reviewText)
        val reviewDate: TextView = itemView.findViewById(R.id.reviewDate)
        val helpfulButton: TextView = itemView.findViewById(R.id.helpfulButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        
        holder.userName.text = review.userName
        holder.ratingText.text = review.rating.toString()
        holder.reviewText.text = review.comment
        holder.reviewDate.text = review.date
        
        // Load user avatar (using placeholder for now)
        Glide.with(holder.userAvatar.context)
            .load("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop&crop=face")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .circleCrop()
            .into(holder.userAvatar)
        
        holder.itemView.setOnClickListener {
            onReviewClick(review)
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
} 