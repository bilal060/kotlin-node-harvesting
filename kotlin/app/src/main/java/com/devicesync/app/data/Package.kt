package com.devicesync.app.data

data class Package(
    val id: String,
    val name: String,
    val duration: String,
    val price: String,
    val highlights: List<String>,
    val imageUrl: String,
    val description: String
) 