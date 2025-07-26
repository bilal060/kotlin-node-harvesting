package com.devicesync.app.viewmodels

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devicesync.app.data.*
import com.devicesync.app.services.SyncResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = DeviceSyncRepository(application)
    
    private val _dataTypes = MutableLiveData<List<DataType>>()
    val dataTypes: LiveData<List<DataType>> = _dataTypes
    
    private val _syncStatus = MutableLiveData<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: LiveData<SyncStatus> = _syncStatus
    
    private val _deviceInfo = MutableLiveData<DeviceInfo>()
    val deviceInfo: LiveData<DeviceInfo> = _deviceInfo
    
    private val _connectionStatus = MutableLiveData<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Use current device directly - no detection needed
                val currentDevice = getCurrentDeviceInfo()
                _deviceInfo.value = currentDevice
                _connectionStatus.value = ConnectionStatus.CONNECTED
                
                Log.d("MainViewModel", "Using current device: ${currentDevice.deviceId}")
                
                // Register device with backend
                repository.registerDevice(currentDevice)
                
                val dataTypes = repository.getDataTypes(currentDevice.deviceId)
                _dataTypes.value = dataTypes
                
                // Automatically start sync after 2 seconds
                delay(2000)
                startSync()
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading data: ${e.message}", e)
                _connectionStatus.value = ConnectionStatus.ERROR
            }
        }
    }
    
    private fun getCurrentDeviceInfo(): DeviceInfo {
        val context = getApplication<Application>()
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val deviceId = "current_device_${androidId}"
        
        // Save device ID to SettingsManager so NotificationListenerService can access it
        val settingsManager = com.devicesync.app.utils.SettingsManager(context)
        settingsManager.saveDeviceId(deviceId)
        
        return DeviceInfo(
            deviceId = deviceId,
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            isConnected = true
        )
    }
    
    fun startSync() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Starting sync...")
            _syncStatus.value = SyncStatus.SYNCING
            
            try {
                val deviceId = _deviceInfo.value?.deviceId
                if (deviceId == null) {
                    Log.e("MainViewModel", "No device available for sync")
                    _syncStatus.value = SyncStatus.FAILED
                    return@launch
                }
                
                Log.d("MainViewModel", "Syncing for device: $deviceId")
                
                // Media sync service removed - not supported in current backend
                
                val dataTypes = _dataTypes.value ?: return@launch
                
                var totalSynced = 0
                var hasErrors = false
                
                // Sync all data types at once
                Log.d("MainViewModel", "Syncing all data types")
                val result = repository.syncData(deviceId)
                if (result.isSuccess) {
                    val results = result.getOrNull() ?: emptyMap()
                    var allSuccessful = true
                    
                    results.forEach { (dataType, count) ->
                        if (count > 0) {
                            Log.d("MainViewModel", "✅ Sync successful for $dataType: $count items")
                            totalSynced += count
                        } else if (count == -1) {
                            Log.e("MainViewModel", "❌ Sync failed for $dataType")
                            allSuccessful = false
                            hasErrors = true
                        }
                    }
                    
                    // Check if all data types synced successfully
                    if (allSuccessful && results.size >= 6) { // 6 data types: contacts, call logs, messages, notifications, whatsapp, email accounts
                        Log.d("MainViewModel", "🎉 All data types synced successfully!")
                    } else {
                        Log.e("MainViewModel", "⚠️ Some data types failed to sync")
                        hasErrors = true
                    }
                } else {
                    Log.e("MainViewModel", "❌ Sync failed: ${result.exceptionOrNull()?.message}")
                    hasErrors = true
                }
                
                if (hasErrors) {
                    _syncStatus.value = SyncStatus.FAILED
                    Log.e("MainViewModel", "Sync completed with errors. Total items synced: $totalSynced")
                } else {
                    _syncStatus.value = SyncStatus.COMPLETED
                    Log.d("MainViewModel", "Sync completed successfully. Total items synced: $totalSynced")
                }
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error during sync: ${e.message}", e)
                _syncStatus.value = SyncStatus.FAILED
            }
        }
    }
    
    // Media sync service removed - not supported in current backend
}
