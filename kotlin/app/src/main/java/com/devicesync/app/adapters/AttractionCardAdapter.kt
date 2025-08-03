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

class AttractionCardAdapter(
    private val onAttractionClick: (Attraction) -> Unit,
    private val onFavoriteClick: (Attraction) -> Unit
) : RecyclerView.Adapter<AttractionCardAdapter.AttractionViewHolder>() {
    
    private var attractions = listOf<Attraction>()
    
    fun updateAttractions(newAttractions: List<Attraction>) {
        attractions = newAttractions
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attraction_card, parent, false)
        return AttractionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        holder.bind(attractions[position])
    }
    
    override fun getItemCount(): Int = attractions.size
    
    inner class AttractionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.attractionImage)
        private val nameText: TextView = itemView.findViewById(R.id.attractionName)
        private val descriptionText: TextView = itemView.findViewById(R.id.attractionDescription)
        private val priceText: TextView = itemView.findViewById(R.id.attractionPrice)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        
        fun bind(attraction: Attraction) {
            nameText.text = attraction.name
            descriptionText.text = attraction.description.ifEmpty { attraction.location }
            priceText.text = "From AED ${attraction.simplePrice.toInt()}"
            
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