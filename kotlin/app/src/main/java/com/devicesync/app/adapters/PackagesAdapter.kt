package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Package

class PackagesAdapter(
    private var packages: List<Package>,
    private val onPackageClick: (Package) -> Unit
) : RecyclerView.Adapter<PackagesAdapter.PackageViewHolder>() {

    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val packageImage: ImageView = itemView.findViewById(R.id.packageImage)
        val packageName: TextView = itemView.findViewById(R.id.packageName)
        val packageDuration: TextView = itemView.findViewById(R.id.packageDuration)
        val packagePrice: TextView = itemView.findViewById(R.id.packagePrice)
        val packageDescription: TextView = itemView.findViewById(R.id.packageDescription)
        val highlightsContainer: ViewGroup = itemView.findViewById(R.id.highlightsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)
        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val packageItem = packages[position]
        
        holder.packageName.text = packageItem.name
        holder.packageDuration.text = packageItem.duration
        holder.packagePrice.text = packageItem.price
        holder.packageDescription.text = packageItem.description
        
        // Load package image
        Glide.with(holder.packageImage.context)
            .load(packageItem.imageUrl)
            .placeholder(R.drawable.placeholder_attraction)
            .error(R.drawable.placeholder_attraction)
            .centerCrop()
            .into(holder.packageImage)
        
        // Add highlights
        holder.highlightsContainer.removeAllViews()
        packageItem.highlights.take(3).forEach { highlight ->
            val highlightView = LayoutInflater.from(holder.highlightsContainer.context)
                .inflate(R.layout.item_highlight, holder.highlightsContainer, false)
            highlightView.findViewById<TextView>(R.id.highlightText).text = "• $highlight"
            holder.highlightsContainer.addView(highlightView)
        }
        
        holder.itemView.setOnClickListener {
            onPackageClick(packageItem)
        }
    }

    override fun getItemCount(): Int = packages.size

    fun updatePackages(newPackages: List<Package>) {
        packages = newPackages
        notifyDataSetChanged()
    }
} 