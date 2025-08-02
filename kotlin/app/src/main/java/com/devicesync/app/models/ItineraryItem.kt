package com.devicesync.app.models

data class ItineraryItem(
    val id: String,
    val name: String,
    val type: String, // "Attraction" or "Service"
    val duration: Int, // in hours
    val price: Double,
    val imageUrl: String
) 