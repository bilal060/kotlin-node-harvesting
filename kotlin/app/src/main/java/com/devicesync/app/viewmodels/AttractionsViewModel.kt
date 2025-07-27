package com.devicesync.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.AttractionsDatabase
import com.devicesync.app.data.repository.AttractionsRepository
import kotlinx.coroutines.launch

class AttractionsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AttractionsRepository = AttractionsRepository(
        AttractionsDatabase.getDatabase(application).attractionDao(),
        application
    )
    
    private val _attractions = MutableLiveData<List<Attraction>>()
    val attractions: LiveData<List<Attraction>> = _attractions
    
    private val _favoriteAttractions = MutableLiveData<List<Attraction>>()
    val favoriteAttractions: LiveData<List<Attraction>> = _favoriteAttractions
    
    private val _selectedAttraction = MutableLiveData<Attraction>()
    val selectedAttraction: LiveData<Attraction> = _selectedAttraction
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadAttractions()
    }
    
    fun loadAttractions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load from JSON if database is empty
                repository.loadAttractionsFromJson()
                
                // Observe attractions from database
                repository.getAllAttractions().collect { attractions ->
                    _attractions.value = attractions
                }
                
                repository.getFavoriteAttractions().collect { favorites ->
                    _favoriteAttractions.value = favorites
                }
                
            } catch (e: Exception) {
                _error.value = "Failed to load attractions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectAttraction(attraction: Attraction) {
        _selectedAttraction.value = attraction
    }
    
    fun toggleFavorite(attraction: Attraction) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStatus(attraction.id, !attraction.isFavorite)
            } catch (e: Exception) {
                _error.value = "Failed to update favorite: ${e.message}"
            }
        }
    }
    
    fun updateRating(attraction: Attraction, rating: Float) {
        viewModelScope.launch {
            try {
                repository.updateRating(attraction.id, rating)
            } catch (e: Exception) {
                _error.value = "Failed to update rating: ${e.message}"
            }
        }
    }
    
    fun searchAttractions(query: String) {
        viewModelScope.launch {
            try {
                repository.searchAttractions(query).collect { results ->
                    _attractions.value = results
                }
            } catch (e: Exception) {
                _error.value = "Failed to search attractions: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun loadAttractionById(id: Int) {
        viewModelScope.launch {
            try {
                val attraction = repository.getAttractionById(id)
                attraction?.let { selectAttraction(it) }
            } catch (e: Exception) {
                _error.value = "Failed to load attraction: ${e.message}"
            }
        }
    }
} 