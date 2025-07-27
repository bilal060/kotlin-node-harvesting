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
import com.devicesync.app.data.Activity

class ActivitiesAdapter(
    private var activities: List<Activity>,
    private val onItemClick: (Activity) -> Unit
) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.activityImage)
        val nameText: TextView = itemView.findViewById(R.id.activityName)
        val durationText: TextView = itemView.findViewById(R.id.activityDuration)
        val priceText: TextView = itemView.findViewById(R.id.activityPrice)
        val descriptionText: TextView = itemView.findViewById(R.id.activityDescription)
        val ratingBar: RatingBar = itemView.findViewById(R.id.activityRating)
        val addToItineraryButton: TextView = itemView.findViewById(R.id.addToItineraryButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.nameText.text = activity.name
        holder.durationText.text = activity.duration
        holder.priceText.text = activity.price
        holder.descriptionText.text = activity.description
        holder.ratingBar.rating = activity.rating
        
        // Load real activity images based on activity name
        val imageUrl = getActivityImageUrl(activity.name)
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(holder.imageView)
        
        holder.addToItineraryButton.setOnClickListener {
            onItemClick(activity)
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(activity)
        }
    }

    override fun getItemCount(): Int = activities.size

    fun updateActivities(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }
    
    private fun getActivityImageUrl(activityName: String): String {
        return when {
            activityName.contains("Desert Safari", ignoreCase = true) -> 
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center"
            activityName.contains("Burj Khalifa", ignoreCase = true) -> 
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
            activityName.contains("Marina Cruise", ignoreCase = true) -> 
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=300&fit=crop&crop=center"
            activityName.contains("Skydiving", ignoreCase = true) -> 
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center"
            activityName.contains("Sheikh Zayed", ignoreCase = true) -> 
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=300&fit=crop&crop=center"
            activityName.contains("Dubai Mall", ignoreCase = true) -> 
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
            else -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
        }
    }
} 