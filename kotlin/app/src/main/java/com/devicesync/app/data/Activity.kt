package com.devicesync.app.data

data class Activity(
    val id: String,
    val name: String,
    val imageUrl: String,
    val duration: String,
    val price: String,
    val description: String,
    val rating: Float = 0f
) 