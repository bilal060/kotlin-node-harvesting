package com.devicesync.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.SyncedDataAdapter
import com.devicesync.app.data.DataTypeEnum
import com.devicesync.app.viewmodels.SyncedDataViewModel

class SyncedDataActivity : AppCompatActivity() {
    
    private lateinit var viewModel: SyncedDataViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var statusText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synced_data)
        
        viewModel = ViewModelProvider(this)[SyncedDataViewModel::class.java]
        setupViews()
        setupObservers()
        
        // Get data type from intent
        val dataTypeString = intent.getStringExtra("data_type")
        val deviceId = intent.getStringExtra("device_id") ?: "unknown"
        
        if (dataTypeString != null) {
            val dataType = DataTypeEnum.valueOf(dataTypeString)
            viewModel.loadSyncedData(deviceId, dataType)
        }
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.syncedDataRecyclerView)
        statusText = findViewById(R.id.statusText)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupObservers() {
        viewModel.syncedData.observe(this) { data ->
            val adapter = SyncedDataAdapter(data)
            recyclerView.adapter = adapter
            
            statusText.text = "Found ${data.size} synced items"
        }
    }
} 