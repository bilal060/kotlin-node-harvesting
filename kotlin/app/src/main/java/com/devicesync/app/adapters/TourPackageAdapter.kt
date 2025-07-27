package com.devicesync.app.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.models.TourPackage

class TourPackageAdapter(
    private val packages: List<TourPackage>,
    private val onPackageClick: (TourPackage) -> Unit
) : RecyclerView.Adapter<TourPackageAdapter.PackageViewHolder>() {

    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainImage: ImageView = itemView.findViewById(R.id.packageMainImage)
        val nameText: TextView = itemView.findViewById(R.id.packageName)
        val descriptionText: TextView = itemView.findViewById(R.id.packageDescription)
        val durationText: TextView = itemView.findViewById(R.id.packageDuration)
        val priceText: TextView = itemView.findViewById(R.id.packagePrice)
        val ratingText: TextView = itemView.findViewById(R.id.packageRating)
        val reviewsText: TextView = itemView.findViewById(R.id.packageReviews)
        val popularBadge: ImageView = itemView.findViewById(R.id.popularBadge)
        val categoryText: TextView = itemView.findViewById(R.id.packageCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tour_package, parent, false)
        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val tourPackage = packages[position]
        
        holder.nameText.text = tourPackage.name
        holder.descriptionText.text = tourPackage.description
        holder.durationText.text = tourPackage.duration
        holder.priceText.text = tourPackage.price
        holder.ratingText.text = "★ ${tourPackage.rating}"
        holder.reviewsText.text = "(${tourPackage.reviews} reviews)"
        holder.categoryText.text = tourPackage.category
        
        // Show popular badge if applicable
        holder.popularBadge.visibility = if (tourPackage.isPopular) View.VISIBLE else View.GONE
        
        // Load main image
        if (tourPackage.imageUrls.isNotEmpty()) {
            Glide.with(holder.mainImage.context)
                .load(tourPackage.imageUrls[0])
                .placeholder(R.drawable.placeholder_attraction)
                .into(holder.mainImage)
        }
        
        holder.itemView.setOnClickListener {
            onPackageClick(tourPackage)
        }
    }

    override fun getItemCount(): Int = packages.size
} 