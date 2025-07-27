package com.devicesync.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Destination(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val rating: Float = 0f,
    val images: List<String> = emptyList(),
    val basePrice: Double = 0.0,
    val timeSlots: List<TimeSlot> = emptyList(),
    val amenities: List<String> = emptyList(),
    val badge: String? = null
) : Parcelable 