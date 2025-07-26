package com.devicesync.app.data.repository

import android.content.Context
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.dao.AttractionDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttractionsRepository(
    private val attractionDao: AttractionDao,
    private val context: Context
) {
    
    fun getAllAttractions(): Flow<List<Attraction>> = attractionDao.getAllAttractions()
    
    fun getFavoriteAttractions(): Flow<List<Attraction>> = attractionDao.getFavoriteAttractions()
    
    suspend fun getAttractionById(id: Int): Attraction? = attractionDao.getAttractionById(id)
    
    fun searchAttractions(query: String): Flow<List<Attraction>> = attractionDao.searchAttractions(query)
    
    suspend fun insertAttraction(attraction: Attraction) = attractionDao.insertAttraction(attraction)
    
    suspend fun updateAttraction(attraction: Attraction) = attractionDao.updateAttraction(attraction)
    
    suspend fun deleteAttraction(attraction: Attraction) = attractionDao.deleteAttraction(attraction)
    
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) = 
        attractionDao.updateFavoriteStatus(id, isFavorite)
    
    suspend fun updateRating(id: Int, rating: Float) = attractionDao.updateRating(id, rating)
    
    suspend fun loadAttractionsFromJson() = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("dubai_attractions.json").bufferedReader().use { it.readText() }
            val attractions = parseAttractionsFromJson(jsonString)
            attractionDao.insertAttractions(attractions)
            println("✅ Loaded ${attractions.size} attractions from JSON")
        } catch (e: Exception) {
            println("❌ Error loading attractions: ${e.message}")
        }
    }
    
    private fun parseAttractionsFromJson(jsonString: String): List<Attraction> {
        val gson = Gson()
        val listType = object : TypeToken<List<AttractionJson>>() {}.type
        val attractionsJson = gson.fromJson<List<AttractionJson>>(jsonString, listType)
        
        return attractionsJson.map { json ->
            Attraction(
                name = json.name,
                simplePrice = json.simple_price.toDouble(),
                premiumPrice = json.premium_price.toDouble(),
                location = json.location,
                images = json.images
            )
        }
    }
    
    private data class AttractionJson(
        val name: String,
        val simple_price: Int,
        val premium_price: Int,
        val location: String,
        val images: List<String>
    )
} 