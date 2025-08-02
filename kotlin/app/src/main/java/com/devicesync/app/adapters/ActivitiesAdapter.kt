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
import com.devicesync.app.utils.setTextTranslated

class ActivitiesAdapter(
    private var activities: List<Activity>,
    private val onActivityClick: (Activity) -> Unit
) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityImage: ImageView = itemView.findViewById(R.id.activityImage)
        val activityName: TextView = itemView.findViewById(R.id.activityName)
        val activityRating: TextView = itemView.findViewById(R.id.activityRating)
        val activityReviews: TextView = itemView.findViewById(R.id.activityReviews)
        val activityPrice: TextView = itemView.findViewById(R.id.activityPrice)
        val bookActivityButton: TextView = itemView.findViewById(R.id.bookActivityButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.activityName.setTextTranslated(activity.name, holder.itemView.context)
        holder.activityRating.setTextTranslated(activity.rating.toString(), holder.itemView.context)
        holder.activityReviews.setTextTranslated("(${activity.rating.toInt() * 500} reviews)", holder.itemView.context)
        
        // Handle free activities
        if (activity.basePrice == 0.0) {
            holder.activityPrice.setTextTranslated("Free Entry", holder.itemView.context)
        } else {
            holder.activityPrice.setTextTranslated("From AED ${activity.basePrice.toInt()}", holder.itemView.context)
        }
        
        // Load activity image using Glide
        if (activity.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(activity.images.first())
                .placeholder(R.drawable.original_logo)
                .error(R.drawable.original_logo)
                .centerCrop()
                .into(holder.activityImage)
        }
        
        holder.itemView.setOnClickListener {
            onActivityClick(activity)
        }
        
        holder.bookActivityButton.setOnClickListener {
            onActivityClick(activity)
        }
    }

    override fun getItemCount(): Int = activities.size
    
    fun updateActivities(newActivities: List<Activity>) {
        activities = newActivities
        notifyDataSetChanged()
    }
} 