package com.devicesync.app.data

data class TripTemplate(
    val id: String,
    val name: String,
    val description: String,
    val duration: Int, // in days
    val difficulty: TripDifficulty,
    val price: Double,
    val currency: String = "USD",
    val imageUrl: String,
    val highlights: List<String>,
    val itinerary: List<DayPlan>,
    val included: List<String>,
    val excluded: List<String>,
    val requirements: List<String>,
    val rating: Float,
    val reviewCount: Int,
    val isPopular: Boolean = false,
    val isCustomizable: Boolean = true
)

enum class TripDifficulty {
    EASY,
    MODERATE,
    CHALLENGING,
    EXPERT
}

data class DayPlan(
    val day: Int,
    val title: String,
    val description: String,
    val activities: List<Activity>,
    val meals: List<Meal>,
    val accommodation: String? = null
)

data class Meal(
    val type: MealType,
    val description: String,
    val isIncluded: Boolean = true
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
} 