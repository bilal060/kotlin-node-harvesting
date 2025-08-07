package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.models.TourPackage

class PackagesAdapter(
    private var packages: List<TourPackage> = emptyList(),
    private val onPackageClick: (TourPackage) -> Unit
) : RecyclerView.Adapter<PackagesAdapter.PackageViewHolder>() {

    inner class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.packageImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.packageName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.packageDescription)
        private val priceTextView: TextView = itemView.findViewById(R.id.packagePrice)
        private val durationTextView: TextView = itemView.findViewById(R.id.packageDuration)
        private val highlightsTextView: TextView = itemView.findViewById(R.id.packageHighlights)

        fun bind(packageItem: TourPackage) {
            titleTextView.text = packageItem.name
            descriptionTextView.text = packageItem.description
            priceTextView.text = packageItem.price
            durationTextView.text = packageItem.duration
            highlightsTextView.text = packageItem.highlights.take(3).joinToString(" • ")

            // Load image using Glide
            if (packageItem.imageUrls.isNotEmpty()) {
                Glide.with(imageView.context)
                    .load(packageItem.imageUrls.first())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageView)
            }

            itemView.setOnClickListener {
                onPackageClick(packageItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)
        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.bind(packages[position])
    }

    override fun getItemCount(): Int = packages.size

    fun updatePackages(newPackages: List<TourPackage>) {
        packages = newPackages
        notifyDataSetChanged()
    }
} 