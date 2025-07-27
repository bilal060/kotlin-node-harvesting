package com.devicesync.app.data

data class Destination(
    val id: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val badge: String? = null,
    val rating: Float = 0f
) 