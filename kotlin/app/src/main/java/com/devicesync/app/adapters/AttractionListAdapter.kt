package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Attraction

class AttractionListAdapter(
    private val onAttractionClick: (Attraction) -> Unit,
    private val onFavoriteClick: (Attraction) -> Unit
) : RecyclerView.Adapter<AttractionListAdapter.AttractionViewHolder>() {
    
    private var attractions = listOf<Attraction>()
    
    fun updateAttractions(newAttractions: List<Attraction>) {
        attractions = newAttractions
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attraction_list, parent, false)
        return AttractionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        holder.bind(attractions[position])
    }
    
    override fun getItemCount(): Int = attractions.size
    
    inner class AttractionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.attractionImage)
        private val nameText: TextView = itemView.findViewById(R.id.attractionName)
        private val locationText: TextView = itemView.findViewById(R.id.attractionLocation)
        private val priceText: TextView = itemView.findViewById(R.id.attractionPrice)
        private val ratingText: TextView = itemView.findViewById(R.id.attractionRating)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        
        fun bind(attraction: Attraction) {
            nameText.text = attraction.name
            locationText.text = attraction.location
            priceText.text = "From AED ${attraction.simplePrice.toInt()}"
            
            // Show rating if available
            if (attraction.rating > 0) {
                ratingText.text = "★ ${attraction.rating}"
                ratingText.visibility = View.VISIBLE
            } else {
                ratingText.visibility = View.GONE
            }
            
            // Load image using Glide
            if (attraction.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(attraction.images[0])
                    .placeholder(R.drawable.placeholder_attraction)
                    .error(R.drawable.placeholder_attraction)
                    .centerCrop()
                    .into(imageView)
            }
            
            // Update favorite button
            favoriteButton.setImageResource(
                if (attraction.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )
            
            // Setup click listeners
            itemView.setOnClickListener {
                onAttractionClick(attraction)
            }
            
            favoriteButton.setOnClickListener {
                onFavoriteClick(attraction)
            }
        }
    }
} 