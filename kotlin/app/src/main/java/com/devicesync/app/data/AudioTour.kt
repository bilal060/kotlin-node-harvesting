package com.devicesync.app.data

data class AudioTour(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val duration: Int, // in minutes
    val language: String,
    val audioUrl: String,
    val imageUrl: String,
    val stops: List<AudioStop>,
    val rating: Float,
    val downloadCount: Int,
    val isFree: Boolean = false,
    val price: Double? = null,
    val currency: String = "USD"
)

data class AudioStop(
    val id: String,
    val title: String,
    val description: String,
    val coordinates: Coordinates,
    val audioUrl: String,
    val duration: Int, // in seconds
    val imageUrl: String? = null,
    val facts: List<String> = emptyList(),
    val tips: List<String> = emptyList()
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class AudioTourProgress(
    val tourId: String,
    val currentStop: Int,
    val completedStops: List<String>,
    val totalDuration: Int,
    val currentPosition: Int, // in seconds
    val isPlaying: Boolean = false,
    val lastPlayedAt: Long
) 