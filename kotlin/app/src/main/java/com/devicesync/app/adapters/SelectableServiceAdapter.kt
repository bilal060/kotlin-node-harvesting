package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.Service

class SelectableServiceAdapter(
    private val onServiceSelected: (Service, Boolean) -> Unit
) : RecyclerView.Adapter<SelectableServiceAdapter.ServiceViewHolder>() {

    private var services = listOf<Service>()
    private val selectedServices = mutableSetOf<String>()

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.serviceImage)
        val nameText: TextView = itemView.findViewById(R.id.serviceName)
        val descriptionText: TextView = itemView.findViewById(R.id.serviceDescription)
        val ratingText: TextView = itemView.findViewById(R.id.serviceRating)
        val priceText: TextView = itemView.findViewById(R.id.servicePrice)
        val categoryText: TextView = itemView.findViewById(R.id.serviceCategory)
        val checkBox: CheckBox = itemView.findViewById(R.id.serviceCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        
        holder.nameText.text = service.name
        holder.descriptionText.text = service.description
        holder.ratingText.text = "★ ${service.rating}"
        holder.priceText.text = "AED ${service.averageCost.values.firstOrNull() ?: 0}"
        holder.categoryText.text = service.category
        
        // Load image
        Glide.with(holder.imageView.context)
            .load(service.images.firstOrNull() ?: "")
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(holder.imageView)
        
        // Set checkbox state
        holder.checkBox.isChecked = selectedServices.contains(service.id)
        
        // Handle checkbox click
        holder.checkBox.setOnClickListener {
            val isSelected = holder.checkBox.isChecked
            if (isSelected) {
                selectedServices.add(service.id)
            } else {
                selectedServices.remove(service.id)
            }
            onServiceSelected(service, isSelected)
        }
        
        // Handle item click
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            val isSelected = holder.checkBox.isChecked
            if (isSelected) {
                selectedServices.add(service.id)
            } else {
                selectedServices.remove(service.id)
            }
            onServiceSelected(service, isSelected)
        }
    }

    override fun getItemCount(): Int = services.size

    fun updateServices(newServices: List<Service>) {
        services = newServices
        notifyDataSetChanged()
    }
} 