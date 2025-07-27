package com.devicesync.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.DataTypeEnum
import com.devicesync.app.services.BackendSyncService
import kotlinx.coroutines.launch

class SyncedDataViewModel(application: Application) : AndroidViewModel(application) {
    
    private val apiService = RetrofitClient.apiService
    private val backendSyncService = BackendSyncService(application, apiService)
    
    private val _syncedData = MutableLiveData<List<Any>>()
    val syncedData: LiveData<List<Any>> = _syncedData
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadSyncedData(deviceId: String, dataType: DataTypeEnum) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = backendSyncService.getSyncedData(deviceId, dataType)
                if (result.isSuccess) {
                    val data = result.getOrNull() ?: emptyList()
                    _syncedData.value = data
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load data"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 