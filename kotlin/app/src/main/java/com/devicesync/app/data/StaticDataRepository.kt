package com.devicesync.app.data

import android.content.Context
import android.util.Log
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.models.TourPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

object StaticDataRepository {
    private const val TAG = "StaticDataRepository"
    
    // Static data lists
    var sliderImages: List<SliderImage> = emptyList()
    var attractions: List<Attraction> = emptyList()
    var services: List<Service> = emptyList()
    var tourPackages: List<TourPackage> = emptyList()
    
    // Caching and state management
    private var isDataLoaded = AtomicBoolean(false)
    private var isLoading = AtomicBoolean(false)
    private var lastFetchTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000 // 5 minutes cache
    
    // Sample data for fallback
    private val sampleAttractions = listOf(
        Attraction(
            id = 1,
            name = "Burj Khalifa",
            simplePrice = 149.0,
            premiumPrice = 299.0,
            location = "Downtown Dubai",
            images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200&h=600&fit=crop"),
            description = "World's tallest building with stunning views",
            category = "Landmark"
        ),
        Attraction(
            id = 2,
            name = "Palm Jumeirah",
            simplePrice = 89.0,
            premiumPrice = 199.0,
            location = "Palm Jumeirah",
            images = listOf("https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=1200&h=600&fit=crop"),
            description = "Iconic palm-shaped island",
            category = "Island"
        )
    )
    
    private val sampleServices = listOf(
        Service(
            id = "1",
            name = "Desert Safari",
            description = "Experience the thrill of desert adventure",
            averageCost = mapOf("basic" to 200, "premium" to 400),
            currency = "AED",
            unit = "per person",
            images = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&h=600&fit=crop"),
            category = "Adventure"
        ),
        Service(
            id = "2",
            name = "City Tour",
            description = "Explore Dubai's iconic landmarks",
            averageCost = mapOf("basic" to 150, "premium" to 300),
            currency = "AED",
            unit = "per person",
            images = listOf("https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=1200&h=600&fit=crop"),
            category = "Sightseeing"
        )
    )

    suspend fun fetchAllStaticData(context: Context, forceRefresh: Boolean = false): FetchResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if data is already loaded and cache is still valid
                if (!forceRefresh && isDataLoaded.get() && !isCacheExpired()) {
                    Log.d(TAG, "✅ Using cached data (loaded ${System.currentTimeMillis() - lastFetchTime}ms ago)")
                    return@withContext FetchResult.Success("Using cached data")
                }
                
                // Check if already loading
                if (isLoading.get()) {
                    Log.d(TAG, "⏳ Data is already being loaded, waiting...")
                    // Wait for current loading to complete
                    while (isLoading.get()) {
                        kotlinx.coroutines.delay(100)
                    }
                    return@withContext FetchResult.Success("Data loaded by another request")
                }
                
                // Set loading flag
                isLoading.set(true)
                Log.d(TAG, "🔄 Starting to fetch all static data from PRODUCTION API...")
                
                // Force fetch attractions from production API
                val attractionsResult = fetchAttractionsFromApi()
                if (attractionsResult is FetchResult.Success<*>) {
                    attractions = attractionsResult.data as List<Attraction>
                    Log.d(TAG, "✅ Successfully fetched ${attractions.size} attractions from PRODUCTION API")
                } else {
                    Log.e(TAG, "❌ Failed to fetch attractions from API: ${attractionsResult}")
                    // Use sample data as fallback
                    attractions = sampleAttractions
                    Log.w(TAG, "⚠️ Using sample attractions data as fallback")
                }
                
                // Force fetch services from production API
                val servicesResult = fetchServicesFromApi()
                if (servicesResult is FetchResult.Success<*>) {
                    services = servicesResult.data as List<Service>
                    Log.d(TAG, "✅ Successfully fetched ${services.size} services from PRODUCTION API")
                } else {
                    Log.e(TAG, "❌ Failed to fetch services from API: ${servicesResult}")
                    // Use sample data as fallback
                    services = sampleServices
                    Log.w(TAG, "⚠️ Using sample services data as fallback")
                }
                
                // Create slider data from API data
                createSliderDataFromApiData()
                
                // Load tour packages from local data
                tourPackages = DummyDataProvider.packages
                
                // Update cache state
                isDataLoaded.set(true)
                lastFetchTime = System.currentTimeMillis()
                
                Log.d(TAG, "🎉 All static data loaded successfully")
                Log.d(TAG, "📊 Summary: ${sliderImages.size} sliders, ${attractions.size} attractions, ${services.size} services, ${tourPackages.size} packages")
                
                FetchResult.Success("Data loaded successfully from API")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching static data from API", e)
                // Create sample data as fallback
                createSampleSliderData()
                attractions = sampleAttractions
                services = sampleServices
                tourPackages = DummyDataProvider.packages
                FetchResult.Error("Failed to fetch data from API: ${e.message}")
            } finally {
                // Clear loading flag
                isLoading.set(false)
            }
        }
    }
    
    private fun isCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastFetchTime > CACHE_DURATION
    }
    
    fun clearCache() {
        Log.d(TAG, "🗑️ Clearing data cache")
        isDataLoaded.set(false)
        lastFetchTime = 0
        sliderImages = emptyList()
        attractions = emptyList()
        services = emptyList()
        tourPackages = emptyList()
    }
    
    fun isDataAvailable(): Boolean {
        return isDataLoaded.get() && !isCacheExpired()
    }
    
    fun getCacheStatus(): String {
        return if (isDataLoaded.get()) {
            val age = System.currentTimeMillis() - lastFetchTime
            "Cache age: ${age / 1000}s, Expired: ${isCacheExpired()}"
        } else {
            "No data loaded"
        }
    }
    
    private suspend fun fetchAttractionsFromApi(): FetchResult {
        return try {
            val response = RetrofitClient.sliderApiService.getAttractions(10)
            if (response.isSuccessful && response.body()?.success == true) {
                val apiAttractions = response.body()?.data ?: emptyList()
                val convertedAttractions = apiAttractions.map { apiAttraction ->
                    Attraction(
                        id = apiAttraction._id.hashCode(),
                        name = apiAttraction.name,
                        simplePrice = 149.0,
                        premiumPrice = 299.0,
                        location = apiAttraction.location.address,
                        images = apiAttraction.images.map { it.url },
                        description = apiAttraction.description,
                        category = "Tourist Attraction"
                    )
                }
                FetchResult.Success(convertedAttractions)
            } else {
                Log.w(TAG, "⚠️ API response not successful for attractions: ${response.code()}")
                FetchResult.Error("API response not successful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching attractions from API", e)
            FetchResult.Error("Failed to fetch attractions: ${e.message}")
        }
    }
    
    private suspend fun fetchServicesFromApi(): FetchResult {
        return try {
            val response = RetrofitClient.sliderApiService.getServices(10)
            if (response.isSuccessful && response.body()?.success == true) {
                val apiServices = response.body()?.data ?: emptyList()
                val convertedServices = apiServices.map { apiService ->
                    Service(
                        id = apiService._id,
                        name = apiService.name,
                        description = apiService.description,
                        averageCost = mapOf("basic" to 100),
                        currency = "AED",
                        unit = "per person",
                        images = apiService.images.map { it.url },
                        category = apiService.category
                    )
                }
                FetchResult.Success(convertedServices)
            } else {
                Log.w(TAG, "⚠️ API response not successful for services: ${response.code()}")
                FetchResult.Error("API response not successful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching services from API", e)
            FetchResult.Error("Failed to fetch services: ${e.message}")
        }
    }
    
    private fun createSliderDataFromApiData() {
        Log.d(TAG, "🖼️ Creating slider data from API data...")
        val newSliders = mutableListOf<SliderImage>()
        
        // Reliable Unsplash URLs for fallback
        val reliableImageUrls = listOf(
            "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200&h=600&fit=crop", // Burj Khalifa
            "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=1200&h=600&fit=crop", // Palm Jumeirah
            "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=1200&h=600&fit=crop", // Dubai Marina
            "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&h=600&fit=crop", // Desert
            "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&h=600&fit=crop"  // Mall
        )
        
        // Add top attractions as sliders
        attractions.take(3).forEachIndexed { index, attraction ->
            // Use reliable Unsplash URL instead of potentially failing external URLs
            val imageUrl = reliableImageUrls.getOrNull(index) ?: reliableImageUrls.first()
            newSliders.add(
                SliderImage(
                    id = "attraction_${attraction.id}",
                    title = attraction.name,
                    description = attraction.description,
                    imageUrl = imageUrl,
                    imageType = "url",
                    order = index + 1,
                    isActive = true,
                    category = "hero",
                    actionType = "attraction",
                    actionData = attraction.id.toString()
                )
            )
            Log.d(TAG, "📸 Added attraction slider: ${attraction.name} - URL: $imageUrl")
        }
        
        // Add top services as sliders
        services.take(2).forEachIndexed { index, service ->
            val imageUrl = reliableImageUrls.getOrNull(index + 3) ?: reliableImageUrls.first()
            newSliders.add(
                SliderImage(
                    id = "service_${service.id}",
                    title = service.name,
                    description = service.description,
                    imageUrl = imageUrl,
                    imageType = "url",
                    order = index + 4,
                    isActive = true,
                    category = "hero",
                    actionType = "service",
                    actionData = service.id
                )
            )
            Log.d(TAG, "📸 Added service slider: ${service.name} - URL: $imageUrl")
        }
        
        sliderImages = newSliders
        Log.d(TAG, "✅ Created ${sliderImages.size} sliders from API data")
    }
    
    private fun createSampleSliderData() {
        Log.d(TAG, "🖼️ Creating sample slider data...")
        sliderImages = listOf(
            SliderImage(
                id = "sample_1",
                title = "Burj Khalifa",
                description = "World's tallest building",
                imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200&h=600&fit=crop",
                imageType = "url",
                order = 1,
                isActive = true,
                category = "hero",
                actionType = "attraction",
                actionData = "1"
            ),
            SliderImage(
                id = "sample_2",
                title = "Palm Jumeirah",
                description = "Iconic palm-shaped island",
                imageUrl = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=1200&h=600&fit=crop",
                imageType = "url",
                order = 2,
                isActive = true,
                category = "hero",
                actionType = "attraction",
                actionData = "2"
            )
        )
        Log.d(TAG, "✅ Created ${sliderImages.size} sample sliders")
    }
    
    sealed class FetchResult {
        data class Success<T>(val data: T) : FetchResult()
        data class Error(val message: String) : FetchResult()
    }
}

// Data class for slider images
data class SliderImage(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val imageType: String,
    val order: Int,
    val isActive: Boolean,
    val category: String,
    val actionType: String,
    val actionData: String
) 