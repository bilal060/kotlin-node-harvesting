package com.devicesync.app.data.dao

import androidx.room.*
import com.devicesync.app.data.Attraction
import kotlinx.coroutines.flow.Flow

@Dao
interface AttractionDao {
    
    @Query("SELECT * FROM attractions ORDER BY name ASC")
    fun getAllAttractions(): Flow<List<Attraction>>
    
    @Query("SELECT * FROM attractions WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteAttractions(): Flow<List<Attraction>>
    
    @Query("SELECT * FROM attractions WHERE id = :id")
    suspend fun getAttractionById(id: Int): Attraction?
    
    @Query("SELECT * FROM attractions WHERE name LIKE '%' || :searchQuery || '%' OR location LIKE '%' || :searchQuery || '%'")
    fun searchAttractions(searchQuery: String): Flow<List<Attraction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttraction(attraction: Attraction)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttractions(attractions: List<Attraction>)
    
    @Update
    suspend fun updateAttraction(attraction: Attraction)
    
    @Delete
    suspend fun deleteAttraction(attraction: Attraction)
    
    @Query("DELETE FROM attractions")
    suspend fun deleteAllAttractions()
    
    @Query("UPDATE attractions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
    
    @Query("UPDATE attractions SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Int, rating: Float)
} 