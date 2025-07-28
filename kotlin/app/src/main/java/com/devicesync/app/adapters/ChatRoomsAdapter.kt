package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.ChatRoom

class ChatRoomsAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onChatRoomClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomsAdapter.ChatRoomViewHolder>() {

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatIcon: ImageView = itemView.findViewById(R.id.chatIcon)
        val chatName: TextView = itemView.findViewById(R.id.chatName)
        val chatType: TextView = itemView.findViewById(R.id.chatType)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val lastMessageTime: TextView = itemView.findViewById(R.id.lastMessageTime)
        val unreadCount: TextView = itemView.findViewById(R.id.unreadCount)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        
        holder.chatName.text = chatRoom.title
        holder.chatType.text = chatRoom.participants.firstOrNull()?.type?.name ?: "Support"
        holder.lastMessage.text = chatRoom.lastMessage?.message ?: "No messages yet"
        
        // Format last message time
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val time = java.util.Date(chatRoom.lastMessage?.timestamp ?: chatRoom.createdAt)
        holder.lastMessageTime.text = timeFormat.format(time)
        
        // Show unread count
        if (chatRoom.unreadCount > 0) {
            holder.unreadCount.text = chatRoom.unreadCount.toString()
            holder.unreadCount.visibility = View.VISIBLE
        } else {
            holder.unreadCount.visibility = View.GONE
        }
        
        // Show status indicator
        holder.statusIndicator.visibility = if (chatRoom.isActive) View.VISIBLE else View.GONE
        
        // Set chat icon based on type
        val iconRes = when (chatRoom.participants.firstOrNull()?.type?.name?.lowercase()) {
            "support" -> R.drawable.ic_guide
            "guide" -> R.drawable.ic_guide
            "booking" -> R.drawable.ic_car
            "emergency" -> R.drawable.ic_disconnected
            else -> R.drawable.ic_guide
        }
        holder.chatIcon.setImageResource(iconRes)
        
        holder.itemView.setOnClickListener {
            onChatRoomClick(chatRoom)
        }
    }

    override fun getItemCount(): Int = chatRooms.size

    fun updateChatRooms(newChatRooms: List<ChatRoom>) {
        chatRooms = newChatRooms
        notifyDataSetChanged()
    }
} 