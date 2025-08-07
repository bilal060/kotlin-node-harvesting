package com.devicesync.app.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devicesync.app.data.Service
import com.devicesync.app.data.StaticDataRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServicesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services
    
    private val _favoriteServices = MutableLiveData<List<Service>>()
    val favoriteServices: LiveData<List<Service>> = _favoriteServices
    
    private val _selectedService = MutableLiveData<Service>()
    val selectedService: LiveData<Service> = _selectedService
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadServices()
    }
    
    fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("ServicesViewModel", "🔄 Loading services from production API...")
                
                // Fetch data from production API via StaticDataRepository
                val result = StaticDataRepository.fetchAllStaticData(getApplication())
                
                if (result is StaticDataRepository.FetchResult.Success<*>) {
                    val apiServices = StaticDataRepository.services
                    android.util.Log.d("ServicesViewModel", "✅ Successfully loaded ${apiServices.size} services from API")
                    _services.value = apiServices
                    _error.value = null
                } else {
                    android.util.Log.e("ServicesViewModel", "❌ Failed to load services from API: $result")
                    _error.value = "Failed to load services from API"
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ServicesViewModel", "❌ Error loading services: ${e.message}", e)
                _error.value = "Failed to load services: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectService(service: Service) {
        _selectedService.value = service
    }
    
    fun toggleFavorite(service: Service) {
        viewModelScope.launch {
            try {
                // For now, just update the local list since we're not using Room database
                val currentServices = _services.value?.toMutableList() ?: mutableListOf()
                val index = currentServices.indexOfFirst { it.id == service.id }
                if (index != -1) {
                    currentServices[index] = currentServices[index].copy(isFavorite = !service.isFavorite)
                    _services.value = currentServices
                }
            } catch (e: Exception) {
                _error.value = "Failed to update favorite: ${e.message}"
            }
        }
    }
    
    fun updateRating(service: Service, rating: Float) {
        viewModelScope.launch {
            try {
                // For now, just update the local list since we're not using Room database
                val currentServices = _services.value?.toMutableList() ?: mutableListOf()
                val index = currentServices.indexOfFirst { it.id == service.id }
                if (index != -1) {
                    currentServices[index] = currentServices[index].copy(rating = rating)
                    _services.value = currentServices
                }
            } catch (e: Exception) {
                _error.value = "Failed to update rating: ${e.message}"
            }
        }
    }
    
    fun searchServices(query: String) {
        viewModelScope.launch {
            try {
                val currentServices = _services.value ?: emptyList()
                val filteredServices = currentServices.filter { service ->
                    service.name.contains(query, ignoreCase = true) ||
                    service.description.contains(query, ignoreCase = true) ||
                    service.category.contains(query, ignoreCase = true)
                }
                _services.value = filteredServices
            } catch (e: Exception) {
                _error.value = "Failed to search services: ${e.message}"
            }
        }
    }
    
    fun loadServiceById(id: String) {
        viewModelScope.launch {
            try {
                val currentServices = _services.value ?: emptyList()
                val service = currentServices.find { it.id == id }
                service?.let { selectService(it) }
            } catch (e: Exception) {
                _error.value = "Failed to load service: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun refreshServices() {
        loadServices()
    }
} 