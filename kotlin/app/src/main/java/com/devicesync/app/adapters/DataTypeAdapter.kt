package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.DataType

class DataTypeAdapter(
    private var dataTypes: List<DataType>,
    private val onItemClick: (DataType) -> Unit,
    private val onViewDataClick: (DataType) -> Unit
) : RecyclerView.Adapter<DataTypeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.dataTypeTitle)
        val statusText: TextView = view.findViewById(R.id.dataTypeStatus)
        val countText: TextView = view.findViewById(R.id.dataTypeCount)
        val viewDataButton: android.widget.Button = view.findViewById(R.id.viewDataButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_type, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataType = dataTypes[position]
        holder.titleText.text = dataType.type.name.replace("_", " ")
        holder.statusText.text = if (dataType.isEnabled) "Enabled" else "Disabled"
        holder.countText.text = "${dataType.itemCount} items"
        
        // Show/hide view data button based on whether there's data
        holder.viewDataButton.visibility = if (dataType.itemCount > 0) android.view.View.VISIBLE else android.view.View.GONE
        
        holder.itemView.setOnClickListener {
            onItemClick(dataType)
        }
        
        holder.viewDataButton.setOnClickListener {
            onViewDataClick(dataType)
        }
    }

    override fun getItemCount() = dataTypes.size

    fun updateData(newDataTypes: List<DataType>) {
        dataTypes = newDataTypes
        notifyDataSetChanged()
    }
} 