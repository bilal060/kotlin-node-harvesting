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
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val comment: TextView = itemView.findViewById(R.id.comment)
        val destination: TextView = itemView.findViewById(R.id.destination)
        val date: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        
        holder.userName.text = review.userName
        holder.ratingBar.rating = review.rating
        holder.comment.text = review.comment
        holder.destination.text = review.destination
        holder.date.text = review.date
        
        // Load user image (using placeholder for now)
        Glide.with(holder.userImage.context)
            .load("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop&crop=face")
            .placeholder(R.drawable.placeholder_attraction)
            .error(R.drawable.placeholder_attraction)
            .circleCrop()
            .into(holder.userImage)
        
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