package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Service

class ServiceCardAdapter(
    private val onServiceClick: (Service) -> Unit,
    private val onFavoriteClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceCardAdapter.ServiceViewHolder>() {
    
    private var services = listOf<Service>()
    
    fun updateServices(newServices: List<Service>) {
        services = newServices
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)
        return ServiceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }
    
    override fun getItemCount(): Int = services.size
    
    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.serviceImage)
        private val nameText: TextView = itemView.findViewById(R.id.serviceName)
        private val descriptionText: TextView = itemView.findViewById(R.id.serviceDescription)
        private val priceText: TextView = itemView.findViewById(R.id.servicePrice)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        private val featuredBadge: TextView = itemView.findViewById(R.id.featuredBadge)
        
        fun bind(service: Service) {
            nameText.text = service.name
            descriptionText.text = service.description
            priceText.text = "From ${service.currency} ${service.averageCost.values.minOrNull()}"
            
            // Load image using Glide
            if (service.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(service.images[0])
                    .placeholder(R.drawable.placeholder_service)
                    .error(R.drawable.placeholder_service)
                    .centerCrop()
                    .into(imageView)
            }
            
            // Update favorite button
            favoriteButton.setImageResource(
                if (service.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )
            
            // Show/hide featured badge
            featuredBadge.visibility = if (service.isFeatured) View.VISIBLE else View.GONE
            
            // Setup click listeners
            itemView.setOnClickListener {
                onServiceClick(service)
            }
            
            favoriteButton.setOnClickListener {
                onFavoriteClick(service)
            }
        }
    }
} 