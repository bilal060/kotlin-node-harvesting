package com.devicesync.app.models

data class Activity(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Double,
    val reviewCount: Int,
    val price: Int,
    val duration: String,
    val category: String
) 