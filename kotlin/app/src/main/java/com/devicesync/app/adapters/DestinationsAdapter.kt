package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Destination

class DestinationsAdapter(
    private var destinations: List<Destination>,
    private val onItemClick: (Destination) -> Unit
) : RecyclerView.Adapter<DestinationsAdapter.DestinationViewHolder>() {

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.destinationImage)
        val nameText: TextView = itemView.findViewById(R.id.destinationName)
        val locationText: TextView = itemView.findViewById(R.id.destinationLocation)
        val priceText: TextView = itemView.findViewById(R.id.destinationPrice)
        val badgeText: TextView = itemView.findViewById(R.id.destinationBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position]
        
        holder.nameText.text = destination.name
        holder.locationText.text = destination.location
        holder.priceText.text = "From AED ${destination.basePrice.toInt()}"
        
        holder.badgeText.text = destination.badge ?: ""
        holder.badgeText.visibility = if (destination.badge != null) View.VISIBLE else View.GONE
        
        // Load real destination images based on destination name
        val imageUrl = getDestinationImageUrl(destination.name)
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(holder.imageView)
        
        // Make the whole card clickable
        holder.itemView.setOnClickListener {
            onItemClick(destination)
        }
    }

    override fun getItemCount(): Int = destinations.size

    fun updateDestinations(newDestinations: List<Destination>) {
        destinations = newDestinations
        notifyDataSetChanged()
    }
    
    private fun getDestinationImageUrl(destinationName: String): String {
        return when (destinationName.lowercase()) {
            "burj khalifa" -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
            "sheikh zayed mosque" -> "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=300&fit=crop"
            "palm jumeirah" -> "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=300&fit=crop"
            "dubai frame" -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop"
            "dubai mall" -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop"
            "dubai" -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
            "abu dhabi" -> "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=300&fit=crop&crop=center"
            "sharjah" -> "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=400&h=300&fit=crop&crop=center"
            "fujairah" -> "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center"
            "ras al khaimah" -> "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center"
            else -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
        }
    }
} 