package com.devicesync.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.DeviceAdapter
import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.services.DeviceDiscoveryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceDiscoveryActivity : AppCompatActivity() {
    
    private lateinit var discoveryService: DeviceDiscoveryService
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanButton: Button
    private lateinit var statusText: TextView
    
    private val BLUETOOTH_PERMISSION_REQUEST = 1001
    private val LOCATION_PERMISSION_REQUEST = 1002
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)
        
        discoveryService = DeviceDiscoveryService(this)
        setupViews()
        setupObservers()
        checkPermissions()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.deviceRecyclerView)
        scanButton = findViewById(R.id.scanButton)
        statusText = findViewById(R.id.statusText)
        
        deviceAdapter = DeviceAdapter(emptyList()) { device ->
            onDeviceConnectClick(device)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceAdapter
        
        scanButton.setOnClickListener {
            if (checkPermissions()) {
                startDiscovery()
            }
        }
    }
    
    private fun setupObservers() {
        // Observe discovery results
        CoroutineScope(Dispatchers.Main).launch {
            discoveryService.discoveryResult.collect { result ->
                deviceAdapter.updateDevices(result.devices)
                
                statusText.text = when {
                    result.isScanning -> "Scanning for devices..."
                    result.devices.isEmpty() -> "No devices found"
                    else -> "Found ${result.devices.size} device(s)"
                }
                
                scanButton.text = if (result.isScanning) "Stop Scan" else "Start Scan"
            }
        }
    }
    
    private fun startDiscovery() {
        discoveryService.startDiscovery()
    }
    
    private fun onDeviceConnectClick(device: DeviceInfo) {
                        Toast.makeText(this, "Connecting to travel companion device...", Toast.LENGTH_SHORT).show()
        
        // Attempt to connect to the device
        val success = discoveryService.connectToDevice(device)
        
        if (success) {
                            Toast.makeText(this, "Connected! Ready to explore Dubai together!", Toast.LENGTH_SHORT).show()
            // Return to main activity with selected device
            val intent = Intent()
            intent.putExtra("selected_device", device)
            setResult(RESULT_OK, intent)
            finish()
        } else {
                            Toast.makeText(this, "Connection failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        
        // Check Bluetooth permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        // Check location permission (required for Bluetooth scanning)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), BLUETOOTH_PERMISSION_REQUEST)
            return false
        }
        
        return true
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startDiscovery()
                } else {
                    Toast.makeText(this, "Permissions needed to enhance your Dubai travel experience", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        registerBluetoothReceiver()
    }
    
    override fun onPause() {
        super.onPause()
        unregisterBluetoothReceiver()
    }
    
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let {
                        discoveryService.onBluetoothDeviceFound(it)
                    }
                }
            }
        }
    }
    
    private fun registerBluetoothReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
    }
    
    private fun unregisterBluetoothReceiver() {
        try {
            unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }
} 