package com.devicesync.app.repository

import android.content.Context
import android.util.Log
import com.devicesync.app.api.SliderApiService
import com.devicesync.app.data.DataManager
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
            
            // Fetch mobile bundle from backend
            val bundleResponse = sliderApiService.getMobileBundle()
            
            if (bundleResponse.isSuccessful) {
                val bundle = bundleResponse.body()
                if (bundle?.success == true) {
                    // Save sliders
                    bundle.data.sliders?.let { sliders ->
                        dataManager.saveSliders(sliders)
                        Log.d(TAG, "✅ Saved ${sliders.size} sliders")
                    }
                    
                    // Save attractions
                    bundle.data.attractions?.let { attractions ->
                        dataManager.saveAttractions(attractions)
                        Log.d(TAG, "✅ Saved ${attractions.size} attractions")
                    }
                    
                    // Save services
                    bundle.data.services?.let { services ->
                        dataManager.saveServices(services)
                        Log.d(TAG, "✅ Saved ${services.size} services")
                    }
                    
                    // Save tour packages
                    bundle.data.tourPackages?.let { packages ->
                        dataManager.saveTourPackages(packages)
                        Log.d(TAG, "✅ Saved ${packages.size} tour packages")
                    }
                    
                    // Save metadata
                    bundle.data.metadata?.let { metadata ->
                        dataManager.saveDataVersion(metadata.version)
                        Log.d(TAG, "✅ Saved data version: ${metadata.version}")
                    }
                    
                    Log.d(TAG, "🎉 Data synchronization completed successfully")
                    return@withContext SyncResult.Success("Data synchronized successfully")
                } else {
                    Log.e(TAG, "❌ Backend returned error: ${bundle?.message}")
                    return@withContext SyncResult.Error("Backend error: ${bundle?.message}")
                }
            } else {
                Log.e(TAG, "❌ Network error: ${bundleResponse.code()}")
                return@withContext SyncResult.Error("Network error: ${bundleResponse.code()}")
            }
            
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
            
            val response = sliderApiService.getHeroSliders()
            
            if (response.isSuccessful) {
                val sliderResponse = response.body()
                if (sliderResponse?.success == true) {
                    dataManager.saveSliders(sliderResponse.data)
                    Log.d(TAG, "✅ Saved ${sliderResponse.data.size} sliders")
                    return@withContext SyncResult.Success("Sliders synchronized")
                } else {
                    return@withContext SyncResult.Error("Backend error: ${sliderResponse?.message}")
                }
            } else {
                return@withContext SyncResult.Error("Network error: ${response.code()}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Slider sync error: ${e.message}", e)
            return@withContext SyncResult.Error("Slider sync error: ${e.message}")
        }
    }
    
    /**
     * Sync data by category
     */
    suspend fun syncDataByCategory(category: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📦 Syncing $category data...")
            
            val response = sliderApiService.getBundleByCategory(category)
            
            if (response.isSuccessful) {
                val bundleResponse = response.body()
                if (bundleResponse?.success == true) {
                    val bundle = bundleResponse.data
                    
                    when (category) {
                        "sliders" -> {
                            bundle.sliders?.let { sliders ->
                                dataManager.saveSliders(sliders)
                                Log.d(TAG, "✅ Saved ${sliders.size} sliders")
                            }
                        }
                        "attractions" -> {
                            bundle.attractions?.let { attractions ->
                                dataManager.saveAttractions(attractions)
                                Log.d(TAG, "✅ Saved ${attractions.size} attractions")
                            }
                        }
                        "services" -> {
                            bundle.services?.let { services ->
                                dataManager.saveServices(services)
                                Log.d(TAG, "✅ Saved ${services.size} services")
                            }
                        }
                        "packages" -> {
                            bundle.tourPackages?.let { packages ->
                                dataManager.saveTourPackages(packages)
                                Log.d(TAG, "✅ Saved ${packages.size} tour packages")
                            }
                        }
                    }
                    
                    return@withContext SyncResult.Success("$category data synchronized")
                } else {
                    return@withContext SyncResult.Error("Backend error: ${bundleResponse?.message}")
                }
            } else {
                return@withContext SyncResult.Error("Network error: ${response.code()}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ $category sync error: ${e.message}", e)
            return@withContext SyncResult.Error("$category sync error: ${e.message}")
        }
    }
    
    /**
     * Get hero sliders from local storage
     */
    fun getHeroSliders(): List<com.devicesync.app.api.Slider> {
        return dataManager.getHeroSliders()
    }
    
    /**
     * Get attraction sliders from local storage
     */
    fun getAttractionSliders(): List<com.devicesync.app.api.Slider> {
        return dataManager.getAttractionSliders()
    }
    
    /**
     * Get service sliders from local storage
     */
    fun getServiceSliders(): List<com.devicesync.app.api.Slider> {
        return dataManager.getServiceSliders()
    }
    
    /**
     * Get all sliders from local storage
     */
    fun getAllSliders(): List<com.devicesync.app.api.Slider> {
        return dataManager.getSliders()
    }
    
    /**
     * Get attractions from local storage
     */
    fun getAttractions(): List<Any> {
        return dataManager.getAttractions()
    }
    
    /**
     * Get services from local storage
     */
    fun getServices(): List<Any> {
        return dataManager.getServices()
    }
    
    /**
     * Get tour packages from local storage
     */
    fun getTourPackages(): List<Any> {
        return dataManager.getTourPackages()
    }
    
    /**
     * Check if data needs sync
     */
    fun needsSync(): Boolean {
        return dataManager.isDataStale(SYNC_INTERVAL_HOURS) || !dataManager.hasData()
    }
    
    /**
     * Clear all local data
     */
    fun clearLocalData() {
        dataManager.clearAllData()
        Log.d(TAG, "🗑️ Cleared all local data")
    }
}

sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
} 