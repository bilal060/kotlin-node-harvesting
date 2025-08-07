package com.devicesync.app.repository

import android.content.Context
import android.util.Log
import com.devicesync.app.api.SliderApiService
import com.devicesync.app.data.DataManager
import com.devicesync.app.data.SliderImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class DataSyncRepository(
    private val context: Context,
    private val apiService: SliderApiService
) {
    
    private val dataManager = DataManager(context)
    private val sliderApiService = apiService
    
    companion object {
        private const val TAG = "DataSyncRepository"
        private const val SYNC_INTERVAL_HOURS = 24
    }
    
    /**
     * Sync all data from backend to local storage
     */
    suspend fun syncAllData(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Starting data synchronization...")
            
            // Check if data is fresh
            if (!dataManager.isDataStale(SYNC_INTERVAL_HOURS) && dataManager.hasData()) {
                Log.d(TAG, "✅ Data is fresh, skipping sync")
                return@withContext SyncResult.Success("Data is fresh")
            }
            
            // Fetch attractions from API
            val attractionsResponse = sliderApiService.getAttractions(20)
            if (attractionsResponse.isSuccessful && attractionsResponse.body()?.success == true) {
                val attractions = attractionsResponse.body()?.data ?: emptyList()
                dataManager.saveAttractions(attractions)
                Log.d(TAG, "✅ Saved ${attractions.size} attractions")
            }
            
            // Fetch services from API
            val servicesResponse = sliderApiService.getServices(20)
            if (servicesResponse.isSuccessful && servicesResponse.body()?.success == true) {
                val services = servicesResponse.body()?.data ?: emptyList()
                dataManager.saveServices(services)
                Log.d(TAG, "✅ Saved ${services.size} services")
            }
            
            // Create slider data from attractions and services
            val sliders = createSliderDataFromApiData()
            dataManager.saveSliders(sliders)
            Log.d(TAG, "✅ Saved ${sliders.size} sliders")
            
            Log.d(TAG, "🎉 Data synchronization completed successfully")
            return@withContext SyncResult.Success("Data synchronized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync error: ${e.message}", e)
            return@withContext SyncResult.Error("Sync error: ${e.message}")
        }
    }
    
    /**
     * Sync only sliders
     */
    suspend fun syncSliders(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🖼️ Syncing sliders...")
            
            // Create slider data from attractions and services
            val sliders = createSliderDataFromApiData()
            dataManager.saveSliders(sliders)
            
            Log.d(TAG, "✅ Synced ${sliders.size} sliders")
            return@withContext SyncResult.Success("Sliders synced successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Slider sync error: ${e.message}", e)
            return@withContext SyncResult.Error("Slider sync error: ${e.message}")
        }
    }
    
    /**
     * Sync attractions
     */
    suspend fun syncAttractions(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🏛️ Syncing attractions...")
            
            val response = sliderApiService.getAttractions(50)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val attractions = response.body()?.data ?: emptyList()
                dataManager.saveAttractions(attractions)
                Log.d(TAG, "✅ Synced ${attractions.size} attractions")
                return@withContext SyncResult.Success("Attractions synced successfully")
            } else {
                Log.e(TAG, "❌ Attraction sync failed: ${response.code()}")
                return@withContext SyncResult.Error("Attraction sync failed")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Attraction sync error: ${e.message}", e)
            return@withContext SyncResult.Error("Attraction sync error: ${e.message}")
        }
    }
    
    /**
     * Sync services
     */
    suspend fun syncServices(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔧 Syncing services...")
            
            val response = sliderApiService.getServices(50)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val services = response.body()?.data ?: emptyList()
                dataManager.saveServices(services)
                Log.d(TAG, "✅ Synced ${services.size} services")
                return@withContext SyncResult.Success("Services synced successfully")
            } else {
                Log.e(TAG, "❌ Service sync failed: ${response.code()}")
                return@withContext SyncResult.Error("Service sync failed")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Service sync error: ${e.message}", e)
            return@withContext SyncResult.Error("Service sync error: ${e.message}")
        }
    }
    
    /**
     * Create slider data from API data
     */
    private suspend fun createSliderDataFromApiData(): List<SliderImage> {
        val sliders = mutableListOf<SliderImage>()
        
        // Reliable Unsplash URLs for fallback
        val reliableImageUrls = listOf(
            "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200&h=600&fit=crop", // Burj Khalifa
            "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=1200&h=600&fit=crop", // Palm Jumeirah
            "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=1200&h=600&fit=crop", // Dubai Marina
            "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&h=600&fit=crop", // Desert
            "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&h=600&fit=crop"  // Mall
        )
        
        try {
            // Get attractions for sliders
            val attractionsResponse = sliderApiService.getAttractions(5)
            if (attractionsResponse.isSuccessful && attractionsResponse.body()?.success == true) {
                val attractions = attractionsResponse.body()?.data ?: emptyList()
                attractions.take(3).forEachIndexed { index, attraction ->
                    // Use reliable Unsplash URL instead of potentially failing external URLs
                    val imageUrl = reliableImageUrls.getOrNull(index) ?: reliableImageUrls.first()
                    sliders.add(
                        SliderImage(
                            id = "attraction_${attraction._id}",
                            title = attraction.name,
                            description = attraction.description,
                            imageUrl = imageUrl,
                            imageType = "url",
                            order = index + 1,
                            isActive = true,
                            category = "hero",
                            actionType = "attraction",
                            actionData = attraction._id
                        )
                    )
                }
            }
            
            // Get services for sliders
            val servicesResponse = sliderApiService.getServices(5)
            if (servicesResponse.isSuccessful && servicesResponse.body()?.success == true) {
                val services = servicesResponse.body()?.data ?: emptyList()
                services.take(2).forEachIndexed { index, service ->
                    // Use reliable Unsplash URL instead of potentially failing external URLs
                    val imageUrl = reliableImageUrls.getOrNull(index + 3) ?: reliableImageUrls.last()
                    sliders.add(
                        SliderImage(
                            id = "service_${service._id}",
                            title = service.name,
                            description = service.description,
                            imageUrl = imageUrl,
                            imageType = "url",
                            order = index + 4,
                            isActive = true,
                            category = "hero",
                            actionType = "service",
                            actionData = service._id
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating slider data: ${e.message}", e)
        }
        
        return sliders
    }
    
    /**
     * Get cached data
     */
    fun getCachedSliders(): List<SliderImage> = dataManager.getSliders()
    fun getCachedAttractions(): List<Any> = dataManager.getAttractions()
    fun getCachedServices(): List<Any> = dataManager.getServices()
    fun getCachedTourPackages(): List<Any> = dataManager.getTourPackages()
    
    /**
     * Check if data needs sync
     */
    fun needsSync(): Boolean = dataManager.isDataStale(SYNC_INTERVAL_HOURS) || !dataManager.hasData()
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        dataManager.clearAllData()
        Log.d(TAG, "🗑️ Cache cleared")
    }
    
    sealed class SyncResult {
        data class Success(val message: String) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }
} 