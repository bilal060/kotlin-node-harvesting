package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.Destination

class DestinationsAdapter(
    private var destinations: List<Destination>,
    private val onItemClick: (Destination) -> Unit
) : RecyclerView.Adapter<DestinationsAdapter.DestinationViewHolder>() {

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.destinationImage)
        val nameText: TextView = itemView.findViewById(R.id.destinationName)
        val badgeText: TextView = itemView.findViewById(R.id.destinationBadge)
        val exploreButton: TextView = itemView.findViewById(R.id.exploreButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position]
        
        holder.nameText.text = destination.name
        holder.badgeText.text = destination.badge ?: ""
        holder.badgeText.visibility = if (destination.badge != null) View.VISIBLE else View.GONE
        
        // Set placeholder image for now
        holder.imageView.setImageResource(R.drawable.original_logo)
        
        holder.exploreButton.setOnClickListener {
            onItemClick(destination)
        }
    }

    override fun getItemCount(): Int = destinations.size

    fun updateDestinations(newDestinations: List<Destination>) {
        destinations = newDestinations
        notifyDataSetChanged()
    }
} 