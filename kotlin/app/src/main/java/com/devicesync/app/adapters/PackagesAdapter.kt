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
        val packageDescription: TextView = itemView.findViewById(R.id.packageDescription)
        val packagePrice: TextView = itemView.findViewById(R.id.packagePrice)
        val packageHighlights: TextView = itemView.findViewById(R.id.packageHighlights)
        val bookPackageButton: TextView = itemView.findViewById(R.id.bookPackageButton)
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
        holder.packageDescription.text = packageItem.description ?: "Experience the best of Dubai with our comprehensive tour package including iconic landmarks and desert adventures."
        holder.packagePrice.text = packageItem.price
        holder.packageHighlights.text = packageItem.highlights.take(3).joinToString(" • ") { "• $it" }
        
        // Load package image
        Glide.with(holder.packageImage.context)
            .load(packageItem.imageUrl)
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(holder.packageImage)
        
        holder.itemView.setOnClickListener {
            onPackageClick(packageItem)
        }
        
        holder.bookPackageButton.setOnClickListener {
            onPackageClick(packageItem)
        }
    }

    override fun getItemCount(): Int = packages.size

    fun updatePackages(newPackages: List<Package>) {
        packages = newPackages
        notifyDataSetChanged()
    }
} 