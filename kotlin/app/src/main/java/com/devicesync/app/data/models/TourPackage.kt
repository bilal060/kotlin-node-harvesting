package com.devicesync.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TourPackage(
    val id: String,
    val name: String,
    val description: String,
    val duration: String,
    val price: String,
    val imageUrls: List<String>,
    val highlights: List<String>,
    val itinerary: List<String>,
    val includes: List<String>,
    val excludes: List<String> = emptyList(),
    val rating: Float = 0f,
    val reviews: Int = 0,
    val isPopular: Boolean = false,
    val category: String = ""
) : Parcelable 