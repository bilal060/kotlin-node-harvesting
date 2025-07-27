package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        
        // Set placeholder image for now
        holder.imageView.setImageResource(R.drawable.original_logo)
        
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
} 