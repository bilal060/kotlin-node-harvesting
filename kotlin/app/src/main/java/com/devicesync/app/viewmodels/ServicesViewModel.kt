package com.devicesync.app.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devicesync.app.data.Service
import com.devicesync.app.data.ServicesDatabase
import com.devicesync.app.data.repository.ServicesRepository
import kotlinx.coroutines.launch

class ServicesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ServicesRepository = ServicesRepository(
        ServicesDatabase.getDatabase(application).serviceDao(),
        application
    )
    
    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services
    
    private val _favoriteServices = MutableLiveData<List<Service>>()
    val favoriteServices: LiveData<List<Service>> = _favoriteServices
    
    private val _selectedService = MutableLiveData<Service>()
    val selectedService: LiveData<Service> = _selectedService
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    init {
        loadServices()
    }
    
    fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load from JSON if database is empty
                repository.loadServicesFromJson()
                
                // Observe services from database
                repository.getAllServices().collect { services ->
                    _services.value = services
                }
                
                repository.getFavoriteServices().collect { favorites ->
                    _favoriteServices.value = favorites
                }
                
            } catch (e: Exception) {
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
                repository.updateFavoriteStatus(service.id, !service.isFavorite)
            } catch (e: Exception) {
                _error.value = "Failed to update favorite: ${e.message}"
            }
        }
    }
    
    fun updateRating(service: Service, rating: Float) {
        viewModelScope.launch {
            try {
                repository.updateRating(service.id, rating)
            } catch (e: Exception) {
                _error.value = "Failed to update rating: ${e.message}"
            }
        }
    }
    
    fun searchServices(query: String) {
        viewModelScope.launch {
            try {
                repository.searchServices(query).collect { results ->
                    _services.value = results
                }
            } catch (e: Exception) {
                _error.value = "Failed to search services: ${e.message}"
            }
        }
    }
    
    fun loadServiceById(id: String) {
        viewModelScope.launch {
            try {
                val service = repository.getServiceById(id)
                service?.let { selectService(it) }
            } catch (e: Exception) {
                _error.value = "Failed to load service: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 