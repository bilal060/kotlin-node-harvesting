package com.devicesync.app.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class Slider(
    val _id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val imageType: String,
    val order: Int,
    val isActive: Boolean,
    val category: String,
    val actionType: String,
    val tags: List<String>?,
    val createdAt: String,
    val updatedAt: String
)

data class SliderResponse(
    val success: Boolean,
    val data: List<Slider>,
    val count: Int,
    val message: String
)

data class MobileBundle(
    val sliders: List<Slider>,
    val attractions: List<Any>?, // Will be defined separately
    val services: List<Any>?, // Will be defined separately
    val tourPackages: List<Any>?, // Will be defined separately
    val metadata: BundleMetadata
)

data class BundleMetadata(
    val timestamp: String,
    val version: String,
    val totalSliders: Int,
    val totalAttractions: Int,
    val totalServices: Int,
    val totalTourPackages: Int
)

data class BundleResponse(
    val success: Boolean,
    val data: MobileBundle,
    val message: String
)

interface SliderApiService {
    
    @GET("sliders/hero")
    suspend fun getHeroSliders(
        @Query("limit") limit: Int = 6
    ): Response<SliderResponse>
    
    @GET("sliders/attractions")
    suspend fun getAttractionSliders(
        @Query("limit") limit: Int = 10
    ): Response<SliderResponse>
    
    @GET("sliders/services")
    suspend fun getServiceSliders(
        @Query("limit") limit: Int = 10
    ): Response<SliderResponse>
    
    @GET("sliders/category/{category}")
    suspend fun getSlidersByCategory(
        @retrofit2.http.Path("category") category: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<SliderResponse>
    
    @GET("sliders/mobile/bundle")
    suspend fun getMobileBundle(): Response<BundleResponse>
    
    @GET("sliders/mobile/bundle/{category}")
    suspend fun getBundleByCategory(
        @retrofit2.http.Path("category") category: String
    ): Response<BundleResponse>
} 