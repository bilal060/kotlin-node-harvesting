package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.models.ItineraryDay
import com.devicesync.app.models.ItineraryItem

class ItineraryDayAdapter(
    private val onItemMoved: (Int, Int, ItineraryItem) -> Unit,
    private val onItemRemoved: (Int, ItineraryItem) -> Unit
) : RecyclerView.Adapter<ItineraryDayAdapter.DayViewHolder>() {

    private var itineraryDays = listOf<ItineraryDay>()

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTitleText: TextView = itemView.findViewById(R.id.dayTitleText)
        val dayItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.dayItemsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = itineraryDays[position]
        
        holder.dayTitleText.text = day.title
        
        // Setup items adapter for this day
        val itemsAdapter = ItineraryItemsAdapter(
            dayNumber = day.dayNumber,
            onItemMoved = onItemMoved,
            onItemRemoved = onItemRemoved
        )
        
        holder.dayItemsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.itemView.context)
        holder.dayItemsRecyclerView.adapter = itemsAdapter
        itemsAdapter.updateItems(day.items)
    }

    override fun getItemCount(): Int = itineraryDays.size

    fun updateItinerary(newItineraryDays: List<ItineraryDay>) {
        itineraryDays = newItineraryDays
        notifyDataSetChanged()
    }
}

class ItineraryItemsAdapter(
    private val dayNumber: Int,
    private val onItemMoved: (Int, Int, ItineraryItem) -> Unit,
    private val onItemRemoved: (Int, ItineraryItem) -> Unit
) : RecyclerView.Adapter<ItineraryItemsAdapter.ItemViewHolder>() {

    private var items = listOf<ItineraryItem>()

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        val nameText: TextView = itemView.findViewById(R.id.itemName)
        val typeText: TextView = itemView.findViewById(R.id.itemType)
        val durationText: TextView = itemView.findViewById(R.id.itemDuration)
        val priceText: TextView = itemView.findViewById(R.id.itemPrice)
        val moveUpButton: ImageButton = itemView.findViewById(R.id.moveUpButton)
        val moveDownButton: ImageButton = itemView.findViewById(R.id.moveDownButton)
        val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_itinerary_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        
        holder.nameText.text = item.name
        holder.typeText.text = item.type
        holder.durationText.text = "${item.duration}h"
        holder.priceText.text = "AED ${item.price.toInt()}"
        
        // Load image
        Glide.with(holder.imageView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.original_logo)
            .error(R.drawable.original_logo)
            .centerCrop()
            .into(holder.imageView)
        
        // Setup move up button
        holder.moveUpButton.visibility = if (position > 0) View.VISIBLE else View.GONE
        holder.moveUpButton.setOnClickListener {
            if (position > 0) {
                onItemMoved(dayNumber - 1, dayNumber - 1, item)
            }
        }
        
        // Setup move down button
        holder.moveDownButton.visibility = if (position < items.size - 1) View.VISIBLE else View.GONE
        holder.moveDownButton.setOnClickListener {
            if (position < items.size - 1) {
                onItemMoved(dayNumber - 1, dayNumber - 1, item)
            }
        }
        
        // Setup remove button
        holder.removeButton.setOnClickListener {
            onItemRemoved(dayNumber - 1, item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItineraryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 