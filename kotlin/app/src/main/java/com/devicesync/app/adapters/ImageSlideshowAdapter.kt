package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R

class ImageSlideshowAdapter(private val images: List<String> = emptyList()) : RecyclerView.Adapter<ImageSlideshowAdapter.ImageViewHolder>() {
    
    fun updateImages(newImages: List<String>) {
        (this as? MutableList<String>)?.clear()?.let { addAll(newImages) }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slide, parent, false)
        return ImageViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }
    
    override fun getItemCount(): Int = images.size
    
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.slideImage)
        
        fun bind(imageUrl: String) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_attraction)
                .error(R.drawable.placeholder_attraction)
                .centerCrop()
                .into(imageView)
        }
    }
} 