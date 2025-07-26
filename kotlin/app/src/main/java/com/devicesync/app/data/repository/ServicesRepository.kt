package com.devicesync.app.data.repository

import android.content.Context
import com.devicesync.app.data.Service
import com.devicesync.app.data.dao.ServiceDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServicesRepository(
    private val serviceDao: ServiceDao,
    private val context: Context
) {
    
    fun getAllServices(): Flow<List<Service>> = serviceDao.getAllServices()
    
    fun getFavoriteServices(): Flow<List<Service>> = serviceDao.getFavoriteServices()
    
    suspend fun getServiceById(id: String): Service? = serviceDao.getServiceById(id)
    
    fun searchServices(query: String): Flow<List<Service>> = serviceDao.searchServices(query)
    
    suspend fun insertService(service: Service) = serviceDao.insertService(service)
    
    suspend fun updateService(service: Service) = serviceDao.updateService(service)
    
    suspend fun deleteService(service: Service) = serviceDao.deleteService(service)
    
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) = 
        serviceDao.updateFavoriteStatus(id, isFavorite)
    
    suspend fun updateRating(id: String, rating: Float) = serviceDao.updateRating(id, rating)
    
    suspend fun loadServicesFromJson() = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("service.json").bufferedReader().use { it.readText() }
            val services = parseServicesFromJson(jsonString)
            serviceDao.insertServices(services)
            println("✅ Loaded ${services.size} services from JSON")
        } catch (e: Exception) {
            println("❌ Error loading services: ${e.message}")
        }
    }
    
    private fun parseServicesFromJson(jsonString: String): List<Service> {
        val gson = Gson()
        val servicesJson = gson.fromJson<ServicesJson>(jsonString, ServicesJson::class.java)
        
        return servicesJson.services.map { json ->
            Service(
                id = json.id,
                name = json.name,
                description = json.description,
                averageCost = json.average_cost,
                currency = json.currency,
                unit = json.unit,
                images = json.images
            )
        }
    }
    
    private data class ServicesJson(
        val services: List<ServiceJson>
    )
    
    private data class ServiceJson(
        val id: String,
        val name: String,
        val description: String,
        val average_cost: Map<String, Int>,
        val currency: String,
        val unit: String,
        val images: List<String>
    )
} 