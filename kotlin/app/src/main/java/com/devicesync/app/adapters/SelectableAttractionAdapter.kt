package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Attraction

class SelectableAttractionAdapter(
    private val onAttractionSelected: (Attraction, Boolean) -> Unit
) : RecyclerView.Adapter<SelectableAttractionAdapter.AttractionViewHolder>() {

    private var attractions = listOf<Attraction>()
    private val selectedAttractions = mutableSetOf<String>()

    class AttractionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.attractionImage)
        val nameText: TextView = itemView.findViewById(R.id.attractionName)
        val locationText: TextView = itemView.findViewById(R.id.attractionLocation)
        val ratingText: TextView = itemView.findViewById(R.id.attractionRating)
        val priceText: TextView = itemView.findViewById(R.id.attractionPrice)
        val durationText: TextView = itemView.findViewById(R.id.attractionDuration)
        val checkBox: CheckBox = itemView.findViewById(R.id.attractionCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_attraction, parent, false)
        return AttractionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        val attraction = attractions[position]
        
        holder.nameText.text = attraction.name
        holder.locationText.text = attraction.location
        holder.ratingText.text = "★ ${attraction.rating}"
        holder.priceText.text = if (attraction.simplePrice > 0) "AED ${attraction.simplePrice.toInt()}" else "Free"
        holder.durationText.text = "3h" // Default duration
        
        // Load image
        Glide.with(holder.imageView.context)
            .load(attraction.images.firstOrNull() ?: "")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(holder.imageView)
        
        // Set checkbox state
        holder.checkBox.isChecked = selectedAttractions.contains(attraction.id.toString())
        
        // Handle checkbox click
        holder.checkBox.setOnClickListener {
            val isSelected = holder.checkBox.isChecked
            if (isSelected) {
                selectedAttractions.add(attraction.id.toString())
            } else {
                selectedAttractions.remove(attraction.id.toString())
            }
            onAttractionSelected(attraction, isSelected)
        }
        
        // Handle item click
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            val isSelected = holder.checkBox.isChecked
            if (isSelected) {
                selectedAttractions.add(attraction.id.toString())
            } else {
                selectedAttractions.remove(attraction.id.toString())
            }
            onAttractionSelected(attraction, isSelected)
        }
    }

    override fun getItemCount(): Int = attractions.size

    fun updateAttractions(newAttractions: List<Attraction>) {
        attractions = newAttractions
        notifyDataSetChanged()
    }
} 