package com.devicesync.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.R
import com.devicesync.app.data.ConnectionType
import com.devicesync.app.data.DeviceInfo
import java.text.SimpleDateFormat
import java.util.*

class DeviceAdapter(
    private var devices: List<DeviceInfo>,
    private val onConnectClick: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceModel: TextView = view.findViewById(R.id.deviceModel)
        val connectionType: TextView = view.findViewById(R.id.connectionType)
        val lastSeen: TextView = view.findViewById(R.id.lastSeen)
        val connectButton: Button = view.findViewById(R.id.connectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        
        holder.deviceName.text = device.deviceName
        holder.deviceModel.text = "${device.manufacturer} ${device.model}"
        holder.connectionType.text = getConnectionTypeText(device.connectionType)
        holder.lastSeen.text = "Last seen: ${formatTime(device.lastSeen)}"
        
        holder.connectButton.setOnClickListener {
            onConnectClick(device)
        }
        
        // Update button text based on connection status
        holder.connectButton.text = if (device.isConnected) "Connected" else "Connect"
        holder.connectButton.isEnabled = !device.isConnected
    }

    override fun getItemCount() = devices.size

    fun updateDevices(newDevices: List<DeviceInfo>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    private fun getConnectionTypeText(connectionType: ConnectionType): String {
        return when (connectionType) {
            ConnectionType.BLUETOOTH -> "Bluetooth"
            ConnectionType.WIFI_DIRECT -> "WiFi Direct"
            ConnectionType.USB -> "USB"
            ConnectionType.NETWORK -> "Network"
            ConnectionType.UNKNOWN -> "Unknown"
        }
    }

    private fun formatTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
} 