package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.models.PlannedService
import com.devicesync.app.data.models.ServiceStatus

class ServiceAdapter(
    private val services: List<PlannedService>,
    private val onServiceClick: (PlannedService) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.serviceName)
        val timeText: TextView = itemView.findViewById(R.id.serviceTime)
        val locationText: TextView = itemView.findViewById(R.id.serviceLocation)
        val statusText: TextView = itemView.findViewById(R.id.serviceStatus)
        val descriptionText: TextView = itemView.findViewById(R.id.serviceDescription)
        val typeText: TextView = itemView.findViewById(R.id.serviceType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planned_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        
        holder.nameText.text = service.name
        holder.timeText.text = "${service.startTime} - ${service.endTime}"
        holder.locationText.text = service.location
        holder.descriptionText.text = service.description
        holder.typeText.text = service.type.name.replace("_", " ")
        
        // Set status and color
        when (service.status) {
            ServiceStatus.SCHEDULED -> {
                holder.statusText.text = "📅 Scheduled"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.info_blue))
            }
            ServiceStatus.IN_PROGRESS -> {
                holder.statusText.text = "🔄 In Progress"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.warning_orange))
            }
            ServiceStatus.COMPLETED -> {
                holder.statusText.text = "✓ Completed"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            }
            else -> {
                holder.statusText.text = "❌ Cancelled"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.error_red))
            }
        }
        
        holder.itemView.setOnClickListener {
            onServiceClick(service)
        }
    }

    override fun getItemCount(): Int = services.size
} 