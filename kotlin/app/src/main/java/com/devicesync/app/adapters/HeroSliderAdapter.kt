package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R

class HeroSliderAdapter : RecyclerView.Adapter<HeroSliderAdapter.HeroViewHolder>() {

    private val heroImages = listOf(
        "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200",
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200",
        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200",
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200",
        "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=1200"
    )

    private val heroTitles = listOf(
        "Palm Jumeirah - Iconic Island",
        "Dubai Mall - Shopping Paradise",
        "Museum of the Future",
        "Desert Safari Adventure",
        "Dubai Marina Walk",
        "Burj Khalifa - World's Tallest"
    )

    private val heroDescriptions = listOf(
        "Experience the world's largest man-made island with luxury resorts and stunning beaches",
        "Discover over 1,200 stores in the world's largest shopping mall with entertainment",
        "Explore cutting-edge technology and innovation at this architectural marvel",
        "Thrilling dune bashing, camel rides, and traditional Arabian experiences",
        "Stroll along the stunning waterfront with luxury yachts and modern architecture",
        "Breathtaking views from the 148th floor of the world's tallest building"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_slide, parent, false)
        return HeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = heroImages.size

    inner class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.heroImageView)
        private val titleView: android.widget.TextView = itemView.findViewById(R.id.heroTitle)
        private val descriptionView: android.widget.TextView = itemView.findViewById(R.id.heroDescription)

        fun bind(position: Int) {
            // Load image from URL using Glide
            Glide.with(imageView.context)
                .load(heroImages[position])
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(imageView)
            
            titleView.text = heroTitles[position]
            descriptionView.text = heroDescriptions[position]
        }
    }
} 