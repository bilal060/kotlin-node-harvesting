package com.devicesync.app.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Handler
import android.os.Looper
import com.devicesync.app.data.ConnectionType
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.DiscoveryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class DeviceDiscoveryService(private val context: Context) {
    
    private val _discoveryResult = MutableStateFlow(DiscoveryResult(emptyList()))
    val discoveryResult: Flow<DiscoveryResult> = _discoveryResult.asStateFlow()
    
    private val discoveredDevices = mutableListOf<DeviceInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false
    
    // Bluetooth discovery
    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }
    
    // WiFi P2P discovery
    private val wifiP2pManager: WifiP2pManager? by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    }
    
    private val wifiP2pChannel: WifiP2pManager.Channel? by lazy {
        wifiP2pManager?.initialize(context, Looper.getMainLooper(), null)
    }
    
    fun startDiscovery() {
        if (isScanning) return
        
        isScanning = true
        discoveredDevices.clear()
        updateDiscoveryResult()
        
        // Start Bluetooth discovery
        startBluetoothDiscovery()
        
        // Start WiFi P2P discovery
        startWifiP2pDiscovery()
        
        // Stop discovery after 30 seconds
        handler.postDelayed({
            stopDiscovery()
        }, 30000)
    }
    
    fun stopDiscovery() {
        if (!isScanning) return
        
        isScanning = false
        
        // Stop Bluetooth discovery
        bluetoothAdapter?.cancelDiscovery()
        
        // Stop WiFi P2P discovery
        wifiP2pManager?.stopPeerDiscovery(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {}
        })
        
        updateDiscoveryResult()
    }
    
    private fun startBluetoothDiscovery() {
        bluetoothAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                adapter.startDiscovery()
            }
        }
    }
    
    private fun startWifiP2pDiscovery() {
        wifiP2pManager?.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery started successfully
            }
            
            override fun onFailure(reason: Int) {
                // Discovery failed
            }
        })
    }
    
    fun onBluetoothDeviceFound(device: BluetoothDevice) {
        val deviceInfo = DeviceInfo(
            deviceId = device.address,
            deviceName = device.name ?: "Unknown Device",
            model = "Bluetooth Device",
            manufacturer = "Unknown",
            androidVersion = "Unknown",
            isConnected = false,
            connectionType = ConnectionType.BLUETOOTH,
            lastSeen = System.currentTimeMillis()
        )
        
        addDiscoveredDevice(deviceInfo)
    }
    
    fun onWifiP2pDeviceFound(device: WifiP2pDevice) {
        val deviceInfo = DeviceInfo(
            deviceId = device.deviceAddress,
            deviceName = device.deviceName,
            model = "WiFi P2P Device",
            manufacturer = "Unknown",
            androidVersion = "Unknown",
            isConnected = false,
            connectionType = ConnectionType.WIFI_DIRECT,
            lastSeen = System.currentTimeMillis()
        )
        
        addDiscoveredDevice(deviceInfo)
    }
    
    private fun addDiscoveredDevice(deviceInfo: DeviceInfo) {
        val existingIndex = discoveredDevices.indexOfFirst { it.deviceId == deviceInfo.deviceId }
        
        if (existingIndex >= 0) {
            discoveredDevices[existingIndex] = deviceInfo
        } else {
            discoveredDevices.add(deviceInfo)
        }
        
        updateDiscoveryResult()
    }
    
    private fun updateDiscoveryResult() {
        _discoveryResult.value = DiscoveryResult(
            devices = discoveredDevices.toList(),
            isScanning = isScanning
        )
    }
    
    fun connectToDevice(deviceInfo: DeviceInfo): Boolean {
        return when (deviceInfo.connectionType) {
            ConnectionType.BLUETOOTH -> connectViaBluetooth(deviceInfo)
            ConnectionType.WIFI_DIRECT -> connectViaWifiP2p(deviceInfo)
            else -> false
        }
    }
    
    private fun connectViaBluetooth(deviceInfo: DeviceInfo): Boolean {
        // Implement Bluetooth connection logic
        return false
    }
    
    private fun connectViaWifiP2p(deviceInfo: DeviceInfo): Boolean {
        // Implement WiFi P2P connection logic
        return false
    }
} 