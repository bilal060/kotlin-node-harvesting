package com.devicesync.app.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.models.PastExperience

class PastExperienceAdapter(
    private val experiences: List<PastExperience>,
    private val onExperienceClick: (PastExperience) -> Unit
) : RecyclerView.Adapter<PastExperienceAdapter.ExperienceViewHolder>() {

    class ExperienceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainImage: ImageView = itemView.findViewById(R.id.experienceMainImage)
        val titleText: TextView = itemView.findViewById(R.id.experienceTitle)
        val dateText: TextView = itemView.findViewById(R.id.experienceDate)
        val locationText: TextView = itemView.findViewById(R.id.experienceLocation)
        val descriptionText: TextView = itemView.findViewById(R.id.experienceDescription)
        val ratingText: TextView = itemView.findViewById(R.id.experienceRating)
        val participantsText: TextView = itemView.findViewById(R.id.experienceParticipants)
        val galleryButton: ImageView = itemView.findViewById(R.id.galleryButton)
        val videoButton: ImageView = itemView.findViewById(R.id.videoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_past_experience, parent, false)
        return ExperienceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        val experience = experiences[position]
        
        holder.titleText.text = experience.title
        holder.dateText.text = experience.date
        holder.locationText.text = experience.location
        holder.descriptionText.text = experience.description
        holder.ratingText.text = "★ ${experience.rating}"
        holder.participantsText.text = "${experience.participants} participants"
        
        // Load main image
        if (experience.imageUrls.isNotEmpty()) {
            Glide.with(holder.mainImage.context)
                .load(experience.imageUrls[0])
                .placeholder(R.drawable.placeholder_attraction)
                .into(holder.mainImage)
        }
        
        // Setup gallery and video buttons
        holder.galleryButton.visibility = if (experience.imageUrls.isNotEmpty()) View.VISIBLE else View.GONE
        holder.videoButton.visibility = if (experience.videoUrls?.isNotEmpty() == true) View.VISIBLE else View.GONE
        
        holder.itemView.setOnClickListener {
            onExperienceClick(experience)
        }
        
        holder.galleryButton.setOnClickListener {
            // TODO: Launch gallery activity
            Toast.makeText(holder.itemView.context, "Gallery feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        holder.videoButton.setOnClickListener {
            // TODO: Launch video activity
            Toast.makeText(holder.itemView.context, "Video feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = experiences.size
} 