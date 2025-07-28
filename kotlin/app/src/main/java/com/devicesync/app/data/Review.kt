package com.devicesync.app.data

data class Review(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val rating: Float,
    val title: String,
    val comment: String,
    val date: Long,
    val location: String,
    val helpfulCount: Int = 0,
    val isVerified: Boolean = false,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) 