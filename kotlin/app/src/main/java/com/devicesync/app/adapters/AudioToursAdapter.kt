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
import com.devicesync.app.data.AudioTour

class AudioToursAdapter(
    private var audioTours: List<AudioTour>,
    private val onAudioTourClick: (AudioTour) -> Unit
) : RecyclerView.Adapter<AudioToursAdapter.AudioTourViewHolder>() {

    class AudioTourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tourImage: ImageView = itemView.findViewById(R.id.tourImage)
        val tourTitle: TextView = itemView.findViewById(R.id.tourTitle)
        val tourDescription: TextView = itemView.findViewById(R.id.tourDescription)
        val tourDuration: TextView = itemView.findViewById(R.id.tourDuration)
        val tourLanguage: TextView = itemView.findViewById(R.id.tourLanguage)
        val tourPrice: TextView = itemView.findViewById(R.id.tourPrice)
        val tourRating: RatingBar = itemView.findViewById(R.id.tourRating)
        val tourReviewCount: TextView = itemView.findViewById(R.id.tourReviewCount)
        val tourStops: TextView = itemView.findViewById(R.id.tourStops)
        val freeBadge: TextView = itemView.findViewById(R.id.freeBadge)
        val playButton: TextView = itemView.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioTourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_tour, parent, false)
        return AudioTourViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioTourViewHolder, position: Int) {
        val audioTour = audioTours[position]
        
        holder.tourTitle.text = audioTour.title
        holder.tourDescription.text = audioTour.description
        holder.tourDuration.text = "${audioTour.duration} min"
        holder.tourLanguage.text = audioTour.language
        holder.tourStops.text = "${audioTour.stops.size} stops"
        holder.tourRating.rating = audioTour.rating
        holder.tourReviewCount.text = "(${audioTour.downloadCount} downloads)"
        
        // Show/hide free badge
        if (audioTour.isFree) {
            holder.freeBadge.visibility = View.VISIBLE
            holder.tourPrice.visibility = View.GONE
            holder.playButton.text = "▶️ Play"
        } else {
            holder.freeBadge.visibility = View.GONE
            holder.tourPrice.visibility = View.VISIBLE
            holder.tourPrice.text = audioTour.price?.toString() ?: ""
            holder.playButton.text = "💰 Purchase"
        }
        
        // Load tour image
        Glide.with(holder.tourImage.context)
            .load(audioTour.imageUrl)
            .placeholder(R.drawable.placeholder_attraction)
            .error(R.drawable.placeholder_attraction)
            .into(holder.tourImage)
        
        holder.itemView.setOnClickListener {
            onAudioTourClick(audioTour)
        }
    }

    override fun getItemCount(): Int = audioTours.size

    fun updateAudioTours(newAudioTours: List<AudioTour>) {
        audioTours = newAudioTours
        notifyDataSetChanged()
    }
} 