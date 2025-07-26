package com.devicesync.app.data.dao

import androidx.room.*
import com.devicesync.app.data.Service
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    
    @Query("SELECT * FROM services ORDER BY name ASC")
    fun getAllServices(): Flow<List<Service>>
    
    @Query("SELECT * FROM services WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteServices(): Flow<List<Service>>
    
    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getServiceById(id: String): Service?
    
    @Query("SELECT * FROM services WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'")
    fun searchServices(searchQuery: String): Flow<List<Service>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: Service)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<Service>)
    
    @Update
    suspend fun updateService(service: Service)
    
    @Delete
    suspend fun deleteService(service: Service)
    
    @Query("DELETE FROM services")
    suspend fun deleteAllServices()
    
    @Query("UPDATE services SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)
    
    @Query("UPDATE services SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: String, rating: Float)
} 