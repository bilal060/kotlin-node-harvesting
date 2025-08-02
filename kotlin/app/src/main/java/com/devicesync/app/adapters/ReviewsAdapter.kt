package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R

class ReviewsAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)
        val reviewDate: TextView = itemView.findViewById(R.id.reviewDate)
        val reviewTitle: TextView = itemView.findViewById(R.id.reviewTitle)
        val reviewContent: TextView = itemView.findViewById(R.id.reviewContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        
        holder.userName.text = review.userName
        holder.reviewDate.text = review.date
        holder.reviewTitle.text = review.title
        holder.reviewContent.text = review.content
    }

    override fun getItemCount(): Int = reviews.size

    data class Review(
        val userName: String,
        val date: String,
        val title: String,
        val content: String
    )
} 