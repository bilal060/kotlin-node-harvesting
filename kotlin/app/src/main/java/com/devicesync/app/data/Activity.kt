package com.devicesync.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Activity(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val duration: String,
    val rating: Float = 0f,
    val images: List<String> = emptyList(),
    val basePrice: Double = 0.0,
    val timeSlots: List<TimeSlot> = emptyList(),
    val features: List<String> = emptyList()
) : Parcelable 