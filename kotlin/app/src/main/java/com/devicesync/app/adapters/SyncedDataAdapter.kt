package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.services.ContactData
import com.devicesync.app.services.CallLogData
import com.devicesync.app.services.MessageData
import java.text.SimpleDateFormat
import java.util.*

class SyncedDataAdapter(private val data: List<Any>) : RecyclerView.Adapter<SyncedDataAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.dataTitle)
        val detailText: TextView = view.findViewById(R.id.dataDetail)
        val timestampText: TextView = view.findViewById(R.id.dataTimestamp)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_synced_data, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        
        when (item) {
            is ContactData -> {
                holder.titleText.text = item.name
                holder.detailText.text = item.number
                holder.timestampText.text = "Contact"
            }
            is CallLogData -> {
                holder.titleText.text = item.number
                holder.detailText.text = "Duration: ${item.duration}s"
                holder.timestampText.text = formatDate(item.date)
            }
            is MessageData -> {
                holder.titleText.text = item.address
                holder.detailText.text = item.body
                holder.timestampText.text = formatDate(item.date)
            }
            else -> {
                holder.titleText.text = "Data Item"
                holder.detailText.text = item.toString()
                holder.timestampText.text = "Unknown"
            }
        }
    }
    
    override fun getItemCount() = data.size
    
    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
} 