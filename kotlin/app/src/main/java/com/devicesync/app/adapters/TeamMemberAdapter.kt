package com.devicesync.app.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.R
import com.devicesync.app.data.models.TeamMember

class TeamMemberAdapter(
    private val teamMembers: List<TeamMember>
) : RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder>() {

    class TeamMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.teamMemberImage)
        val nameText: TextView = itemView.findViewById(R.id.teamMemberName)
        val positionText: TextView = itemView.findViewById(R.id.teamMemberPosition)
        val descriptionText: TextView = itemView.findViewById(R.id.teamMemberDescription)
        val facebookButton: ImageView = itemView.findViewById(R.id.facebookButton)
        val instagramButton: ImageView = itemView.findViewById(R.id.instagramButton)
        val emailButton: ImageView = itemView.findViewById(R.id.emailButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member, parent, false)
        return TeamMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
        val member = teamMembers[position]
        
        holder.nameText.text = member.name
        holder.positionText.text = member.position
        holder.descriptionText.text = member.description
        
        // Load image
        Glide.with(holder.imageView.context)
            .load(member.imageUrl)
            .placeholder(R.drawable.placeholder_attraction)
            .into(holder.imageView)
        
        // Setup social media buttons
        holder.facebookButton.visibility = if (member.facebookUrl != null) View.VISIBLE else View.GONE
        holder.instagramButton.visibility = if (member.instagramUrl != null) View.VISIBLE else View.GONE
        holder.emailButton.visibility = if (member.email != null) View.VISIBLE else View.GONE
        
        holder.facebookButton.setOnClickListener {
            member.facebookUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                holder.itemView.context.startActivity(intent)
            }
        }
        
        holder.instagramButton.setOnClickListener {
            member.instagramUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                holder.itemView.context.startActivity(intent)
            }
        }
        
        holder.emailButton.setOnClickListener {
            member.email?.let { email ->
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = teamMembers.size
} 