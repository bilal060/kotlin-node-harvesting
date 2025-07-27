package com.devicesync.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PastExperience(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val imageUrls: List<String>,
    val videoUrls: List<String>? = null,
    val highlights: List<String> = emptyList(),
    val rating: Float = 0f,
    val participants: Int = 0
) : Parcelable 