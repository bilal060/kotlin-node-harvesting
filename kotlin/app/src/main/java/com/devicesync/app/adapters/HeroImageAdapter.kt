package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R

class HeroImageAdapter : RecyclerView.Adapter<HeroImageAdapter.HeroViewHolder>() {

    private val heroImages = listOf(
        R.drawable.dubai_burj_khalifa,
        R.drawable.dubai_desert,
        R.drawable.dubai_marina,
        R.drawable.dubai_palm
    )

    private val heroTitles = listOf(
        "Burj Khalifa - Touch the Sky",
        "Desert Safari - Golden Adventures",
        "Dubai Marina - Modern Elegance",
        "Palm Jumeirah - Island Paradise"
    )

    private val heroDescriptions = listOf(
        "Experience the world's tallest building",
        "Explore the magical desert landscape",
        "Discover luxury waterfront living",
        "Visit the iconic palm-shaped island"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_image, parent, false)
        return HeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = heroImages.size

    inner class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.heroImageView)

        fun bind(position: Int) {
            imageView.setImageResource(heroImages[position])
        }
    }
} 