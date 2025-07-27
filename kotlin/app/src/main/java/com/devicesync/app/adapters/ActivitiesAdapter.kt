package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Activity

class ActivitiesAdapter(
    private var activities: List<Activity>,
    private val onActivityClick: (Activity) -> Unit
) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityImage: ImageView = itemView.findViewById(R.id.activityImage)
        val activityTitle: TextView = itemView.findViewById(R.id.activityTitle)
        val activityRating: TextView = itemView.findViewById(R.id.activityRating)
        val activityDuration: TextView = itemView.findViewById(R.id.activityReviews)
        val activityPrice: TextView = itemView.findViewById(R.id.activityPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.activityTitle.text = activity.name
        holder.activityRating.text = "★ ${activity.rating}"
        holder.activityDuration.text = activity.duration
        
        // Handle free activities
        if (activity.basePrice == 0.0) {
            holder.activityPrice.text = "Free Entry"
        } else {
            holder.activityPrice.text = "From AED ${activity.basePrice.toInt()}"
        }
        
        // Load activity image using Glide
        if (activity.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(activity.images.first())
                .placeholder(R.drawable.ic_airport)
                .error(R.drawable.ic_airport)
                .centerCrop()
                .into(holder.activityImage)
        }
        
        holder.itemView.setOnClickListener {
            onActivityClick(activity)
        }
    }

    override fun getItemCount(): Int = activities.size
    
    fun updateActivities(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }
} 