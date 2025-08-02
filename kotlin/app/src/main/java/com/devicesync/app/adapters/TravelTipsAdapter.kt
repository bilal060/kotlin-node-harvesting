package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.TravelTip

class TravelTipsAdapter : ListAdapter<TravelTip, TravelTipsAdapter.TravelTipViewHolder>(TravelTipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelTipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel_tip, parent, false)
        return TravelTipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelTipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TravelTipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconText: TextView = itemView.findViewById(R.id.tipIcon)
        private val titleText: TextView = itemView.findViewById(R.id.tipTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.tipDescription)

        fun bind(travelTip: TravelTip) {
            iconText.text = travelTip.icon
            titleText.text = travelTip.title
            descriptionText.text = travelTip.description
        }
    }

    private class TravelTipDiffCallback : DiffUtil.ItemCallback<TravelTip>() {
        override fun areItemsTheSame(oldItem: TravelTip, newItem: TravelTip): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: TravelTip, newItem: TravelTip): Boolean {
            return oldItem == newItem
        }
    }
} 