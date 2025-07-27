package com.devicesync.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val price: Double,
    val name: String
) : Parcelable 