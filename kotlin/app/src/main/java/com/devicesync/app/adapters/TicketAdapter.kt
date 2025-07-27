package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.models.BookedTicket
import com.devicesync.app.data.models.TicketStatus

class TicketAdapter(
    private val tickets: List<BookedTicket>,
    private val onTicketClick: (BookedTicket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attractionNameText: TextView = itemView.findViewById(R.id.ticketAttractionName)
        val ticketNumberText: TextView = itemView.findViewById(R.id.ticketNumber)
        val validDateText: TextView = itemView.findViewById(R.id.ticketValidDate)
        val priceText: TextView = itemView.findViewById(R.id.ticketPrice)
        val statusText: TextView = itemView.findViewById(R.id.ticketStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        
        holder.attractionNameText.text = ticket.attractionName
        holder.ticketNumberText.text = "Ticket: ${ticket.ticketNumber}"
        holder.validDateText.text = "Valid: ${ticket.validDate}"
        holder.priceText.text = ticket.price
        
        // Set status and color
        when (ticket.status) {
            TicketStatus.BOOKED -> {
                holder.statusText.text = "✓ Booked"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            }
            TicketStatus.PENDING -> {
                holder.statusText.text = "⏳ Pending"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.warning_orange))
            }
            TicketStatus.USED -> {
                holder.statusText.text = "✓ Used"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            }
            else -> {
                holder.statusText.text = "❌ Cancelled"
                holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.error_red))
            }
        }
        
        holder.itemView.setOnClickListener {
            onTicketClick(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size
} 