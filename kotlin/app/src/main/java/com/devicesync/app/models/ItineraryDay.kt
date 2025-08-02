package com.devicesync.app.models

data class ItineraryDay(
    val dayNumber: Int,
    val title: String,
    val items: MutableList<ItineraryItem>
) 