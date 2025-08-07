package com.devicesync.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.devicesync.app.data.converters.AttractionConverters

@Entity(tableName = "attractions")
@TypeConverters(AttractionConverters::class)
data class Attraction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val simplePrice: Double,
    val premiumPrice: Double,
    val location: String,
    val images: List<String>,
    val isFavorite: Boolean = false,
    val isFeatured: Boolean = false,
    val rating: Float = 0.0f,
    val description: String = "",
    val category: String = "Tourist Attraction"
) 