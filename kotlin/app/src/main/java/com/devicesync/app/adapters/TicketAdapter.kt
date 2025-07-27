package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.Ticket

class TicketAdapter(
    private var tickets: List<Ticket>,
    private val onTicketClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ticketAttractionName: TextView = itemView.findViewById(R.id.ticketAttractionName)
        val ticketPrice: TextView = itemView.findViewById(R.id.ticketPrice)
        val ticketNumber: TextView = itemView.findViewById(R.id.ticketNumber)
        val ticketValidDate: TextView = itemView.findViewById(R.id.ticketValidDate)
        val ticketStatus: TextView = itemView.findViewById(R.id.ticketStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        
        holder.ticketAttractionName.text = ticket.attractionName
        holder.ticketPrice.text = ticket.price
        holder.ticketNumber.text = "Ticket: ${ticket.ticketNumber}"
        holder.ticketValidDate.text = "Valid: ${ticket.validDate}"
        holder.ticketStatus.text = ticket.status
        
        // Set status color based on status
        when {
            ticket.status.contains("✓") -> {
                holder.ticketStatus.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            }
            ticket.status.contains("⏳") -> {
                holder.ticketStatus.setTextColor(holder.itemView.context.getColor(R.color.accent_gold))
            }
            else -> {
                holder.ticketStatus.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
            }
        }
        
        holder.itemView.setOnClickListener {
            onTicketClick(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
} 