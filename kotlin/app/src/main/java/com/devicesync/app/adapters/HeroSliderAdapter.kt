package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devicesync.app.R
import com.devicesync.app.data.SliderImage

class HeroSliderAdapter : RecyclerView.Adapter<HeroSliderAdapter.HeroViewHolder>() {

    private var sliders: List<SliderImage> = emptyList()

    fun updateSliders(newSliders: List<SliderImage>) {
        android.util.Log.d("HeroSliderAdapter", "🔄 Updating sliders: ${newSliders.size} items")
        newSliders.forEachIndexed { index, slider ->
            android.util.Log.d("HeroSliderAdapter", "📸 Slider ${index + 1}: ${slider.title} - URL: ${slider.imageUrl}")
        }
        sliders = newSliders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_slide, parent, false)
        return HeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = sliders.size

    inner class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.heroImageView)
        private val titleView: android.widget.TextView = itemView.findViewById(R.id.heroTitle)
        private val descriptionView: android.widget.TextView = itemView.findViewById(R.id.heroDescription)

        fun bind(position: Int) {
            val slider = sliders[position]
            
            android.util.Log.d("HeroSliderAdapter", "🖼️ Loading image for slider ${position + 1}: ${slider.title}")
            android.util.Log.d("HeroSliderAdapter", "🔗 Image URL: ${slider.imageUrl}")
            
            // Load image from URL using Glide with detailed logging
            Glide.with(imageView.context)
                .load(slider.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        android.util.Log.e("HeroSliderAdapter", "❌ Failed to load image for slider ${position + 1}: ${slider.title}")
                        android.util.Log.e("HeroSliderAdapter", "🔗 Failed URL: ${slider.imageUrl}")
                        android.util.Log.e("HeroSliderAdapter", "❌ Error: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: Target<android.graphics.drawable.Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        android.util.Log.d("HeroSliderAdapter", "✅ Successfully loaded image for slider ${position + 1}: ${slider.title}")
                        android.util.Log.d("HeroSliderAdapter", "🔗 Success URL: ${slider.imageUrl}")
                        return false
                    }
                })
                .into(imageView)
            
            titleView.text = slider.title
            descriptionView.text = slider.description
            
            android.util.Log.d("HeroSliderAdapter", "📝 Set title: ${slider.title}")
            android.util.Log.d("HeroSliderAdapter", "📝 Set description: ${slider.description}")
        }
    }
} 