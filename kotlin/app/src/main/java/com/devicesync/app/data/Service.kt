package com.devicesync.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.devicesync.app.data.converters.ServiceConverters

@Entity(tableName = "services")
@TypeConverters(ServiceConverters::class)
data class Service(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val averageCost: Map<String, Int>,
    val currency: String,
    val unit: String,
    val images: List<String>,
    val isFavorite: Boolean = false,
    val isFeatured: Boolean = false,
    val rating: Float = 0.0f,
    val category: String = "Travel Service"
) {
    val simplePrice: Double
        get() = averageCost["simple"]?.toDouble() ?: 0.0
} 