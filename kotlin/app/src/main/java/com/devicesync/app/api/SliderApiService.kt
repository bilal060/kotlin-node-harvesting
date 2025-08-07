package com.devicesync.app.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Data classes for API responses
data class Attraction(
    val _id: String,
    val name: String,
    val description: String,
    val shortDescription: String,
    val location: Location,
    val images: List<Image>,
    val tags: List<String>,
    val isActive: Boolean,
    val isPopular: Boolean,
    val isFeatured: Boolean
)

data class Service(
    val _id: String,
    val name: String,
    val description: String,
    val shortDescription: String,
    val category: String,
    val subcategory: String,
    val location: Location,
    val images: List<Image>,
    val tags: List<String>,
    val isActive: Boolean,
    val isPopular: Boolean,
    val isFeatured: Boolean
)

data class Location(
    val address: String,
    val area: String
)

data class Image(
    val url: String,
    val caption: String,
    val isPrimary: Boolean
)

data class AttractionResponse(
    val success: Boolean,
    val data: List<Attraction>,
    val count: Int,
    val message: String
)

data class ServiceResponse(
    val success: Boolean,
    val data: List<Service>,
    val count: Int,
    val message: String
)

interface SliderApiService {
    // Get attractions from production API
    @GET("dubai/attractions")
    suspend fun getAttractions(
        @Query("limit") limit: Int = 20
    ): Response<AttractionResponse>
    
    // Get services from production API
    @GET("dubai/services")
    suspend fun getServices(
        @Query("limit") limit: Int = 20
    ): Response<ServiceResponse>
} 