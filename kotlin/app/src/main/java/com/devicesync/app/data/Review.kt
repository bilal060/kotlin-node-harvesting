package com.devicesync.app.data

data class Review(
    val id: String,
    val userName: String,
    val userImageUrl: String,
    val rating: Float,
    val comment: String,
    val destination: String,
    val date: String
) 