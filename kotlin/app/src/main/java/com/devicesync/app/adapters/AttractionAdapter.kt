package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.models.PlannedAttraction
import com.devicesync.app.data.models.TicketStatus

class AttractionAdapter(
    private val attractions: List<PlannedAttraction>,
    private val onAttractionClick: (PlannedAttraction) -> Unit
) : RecyclerView.Adapter<AttractionAdapter.AttractionViewHolder>() {

    class AttractionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.attractionImage)
        val nameText: TextView = itemView.findViewById(R.id.attractionName)
        val timeText: TextView = itemView.findViewById(R.id.attractionTime)
        val locationText: TextView = itemView.findViewById(R.id.attractionLocation)
        val statusText: TextView = itemView.findViewById(R.id.attractionStatus)
        val descriptionText: TextView = itemView.findViewById(R.id.attractionDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planned_attraction, parent, false)
        return AttractionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        val attraction = attractions[position]
        
        holder.nameText.text = attraction.name
        holder.timeText.text = "${attraction.startTime} - ${attraction.endTime}"
        holder.locationText.text = attraction.location
        holder.descriptionText.text = attraction.description
        
        // Set status and color
        when (attraction.ticketStatus) {
            TicketStatus.BOOKED -> {
                holder.statusText.text = "✓ Booked"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            }
            TicketStatus.PENDING -> {
                holder.statusText.text = "⏳ Pending"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.warning_orange))
            }
            TicketStatus.USED -> {
                holder.statusText.text = "✓ Completed"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            }
            else -> {
                holder.statusText.text = "❌ Cancelled"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.error_red))
            }
        }
        
        // Load image
        Glide.with(holder.imageView.context)
            .load(attraction.imageUrl)
            .placeholder(R.drawable.placeholder_attraction)
            .into(holder.imageView)
        
        holder.itemView.setOnClickListener {
            onAttractionClick(attraction)
        }
    }

    override fun getItemCount(): Int = attractions.size
} 