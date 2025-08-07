package com.devicesync.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.StaticDataRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttractionsViewModel(application: Application) : AndroidViewModel(application) {
    
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
                android.util.Log.d("AttractionsViewModel", "🔄 Loading attractions from production API...")
                
                // Fetch data from production API via StaticDataRepository
                val result = StaticDataRepository.fetchAllStaticData(getApplication())
                
                if (result is StaticDataRepository.FetchResult.Success<*>) {
                    val apiAttractions = StaticDataRepository.attractions
                    android.util.Log.d("AttractionsViewModel", "✅ Successfully loaded ${apiAttractions.size} attractions from API")
                    _attractions.value = apiAttractions
                    _error.value = null
                } else {
                    android.util.Log.e("AttractionsViewModel", "❌ Failed to load attractions from API: $result")
                    _error.value = "Failed to load attractions from API"
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AttractionsViewModel", "❌ Error loading attractions: ${e.message}", e)
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
                // For now, just update the local list since we're not using Room database
                val currentAttractions = _attractions.value?.toMutableList() ?: mutableListOf()
                val index = currentAttractions.indexOfFirst { it.id == attraction.id }
                if (index != -1) {
                    currentAttractions[index] = currentAttractions[index].copy(isFavorite = !attraction.isFavorite)
                    _attractions.value = currentAttractions
                }
            } catch (e: Exception) {
                _error.value = "Failed to update favorite: ${e.message}"
            }
        }
    }
    
    fun updateRating(attraction: Attraction, rating: Float) {
        viewModelScope.launch {
            try {
                // For now, just update the local list since we're not using Room database
                val currentAttractions = _attractions.value?.toMutableList() ?: mutableListOf()
                val index = currentAttractions.indexOfFirst { it.id == attraction.id }
                if (index != -1) {
                    currentAttractions[index] = currentAttractions[index].copy(rating = rating)
                    _attractions.value = currentAttractions
                }
            } catch (e: Exception) {
                _error.value = "Failed to update rating: ${e.message}"
            }
        }
    }
    
    fun searchAttractions(query: String) {
        viewModelScope.launch {
            try {
                val currentAttractions = _attractions.value ?: emptyList()
                val filteredAttractions = currentAttractions.filter { attraction ->
                    attraction.name.contains(query, ignoreCase = true) ||
                    attraction.description.contains(query, ignoreCase = true) ||
                    attraction.location.contains(query, ignoreCase = true)
                }
                _attractions.value = filteredAttractions
            } catch (e: Exception) {
                _error.value = "Failed to search attractions: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun refreshAttractions() {
        loadAttractions()
    }
    
    fun loadAttractionById(id: Int) {
        viewModelScope.launch {
            try {
                val currentAttractions = _attractions.value ?: emptyList()
                val attraction = currentAttractions.find { it.id == id }
                attraction?.let { selectAttraction(it) }
            } catch (e: Exception) {
                _error.value = "Failed to load attraction: ${e.message}"
            }
        }
    }
} 