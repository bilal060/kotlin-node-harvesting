package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.TripTemplate

class TripTemplatesAdapter(
    private var templates: List<TripTemplate>,
    private val onTemplateClick: (TripTemplate) -> Unit
) : RecyclerView.Adapter<TripTemplatesAdapter.TemplateViewHolder>() {

    class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val templateImage: ImageView = itemView.findViewById(R.id.templateImage)
        val templateName: TextView = itemView.findViewById(R.id.templateName)
        val templateDescription: TextView = itemView.findViewById(R.id.templateDescription)
        val templateDuration: TextView = itemView.findViewById(R.id.templateDuration)
        val templatePrice: TextView = itemView.findViewById(R.id.templatePrice)
        val templateRating: RatingBar = itemView.findViewById(R.id.templateRating)
        val templateReviewCount: TextView = itemView.findViewById(R.id.templateReviewCount)
        val templateDifficulty: TextView = itemView.findViewById(R.id.templateDifficulty)
        val popularBadge: TextView = itemView.findViewById(R.id.popularBadge)
        val customizableBadge: TextView = itemView.findViewById(R.id.customizableBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = templates[position]
        
        holder.templateName.text = template.name
        holder.templateDescription.text = template.description
        holder.templateDuration.text = "${template.duration} days"
        holder.templatePrice.text = "${template.currency} ${template.price}"
        holder.templateRating.rating = template.rating
        holder.templateReviewCount.text = "(${template.reviewCount} reviews)"
        holder.templateDifficulty.text = template.difficulty.toString()
        
        // Show/hide badges
        holder.popularBadge.visibility = if (template.isPopular) View.VISIBLE else View.GONE
        holder.customizableBadge.visibility = if (template.isCustomizable) View.VISIBLE else View.GONE
        
        // Load template image
        Glide.with(holder.templateImage.context)
            .load(template.imageUrl)
            .placeholder(R.drawable.placeholder_attraction)
            .error(R.drawable.placeholder_attraction)
            .into(holder.templateImage)
        
        holder.itemView.setOnClickListener {
            onTemplateClick(template)
        }
    }

    override fun getItemCount(): Int = templates.size

    fun updateTemplates(newTemplates: List<TripTemplate>) {
        templates = newTemplates
        notifyDataSetChanged()
    }
} 