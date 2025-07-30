package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.TimeSlot
import java.text.NumberFormat
import java.util.*

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlot>,
    private val onTimeSlotSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
    
    private var selectedPosition = -1
    
    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val priceText: TextView = itemView.findViewById(R.id.priceText)
        private val container: View = itemView.findViewById(R.id.container)
        
        fun bind(timeSlot: TimeSlot, isSelected: Boolean) {
            timeText.text = "${timeSlot.startTime} - ${timeSlot.endTime}"
            nameText.text = timeSlot.name
            
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            priceText.text = formatter.format(timeSlot.price)
            
            // Update selection state
            container.setBackgroundResource(
                if (isSelected) R.drawable.time_slot_selected_background
                else R.drawable.time_slot_background
            )
            
            // Update price text color based on selection
            priceText.setTextColor(
                if (isSelected) itemView.context.getColor(R.color.accent_gold)
                else itemView.context.getColor(R.color.primary)
            )
            
            itemView.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                
                // Notify previous and current items
                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected)
                }
                notifyItemChanged(selectedPosition)
                
                onTimeSlotSelected(timeSlot)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.bind(timeSlot, position == selectedPosition)
    }
    
    override fun getItemCount(): Int = timeSlots.size
    
    fun getSelectedSlot(): TimeSlot? {
        return if (selectedPosition != -1) timeSlots[selectedPosition] else null
    }
} 