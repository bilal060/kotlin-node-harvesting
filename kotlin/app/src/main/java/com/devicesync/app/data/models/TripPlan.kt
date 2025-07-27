package com.devicesync.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TripPlan(
    val id: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val status: TripStatus,
    val guide: Guide,
    val dailyPlans: List<DailyPlan>,
    val totalCost: String,
    val bookedTickets: List<BookedTicket>,
    val pendingTickets: List<PendingTicket>
) : Parcelable

@Parcelize
data class DailyPlan(
    val id: String,
    val date: String,
    val dayNumber: Int,
    val attractions: List<PlannedAttraction>,
    val services: List<PlannedService>,
    val meals: List<Meal>,
    val transportation: List<Transportation>,
    val status: DayStatus,
    val progress: Int, // 0-100
    val notes: String = ""
) : Parcelable

@Parcelize
data class PlannedAttraction(
    val id: String,
    val name: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val ticketStatus: TicketStatus,
    val imageUrl: String,
    val description: String,
    val isCompleted: Boolean = false
) : Parcelable

@Parcelize
data class PlannedService(
    val id: String,
    val name: String,
    val type: ServiceType,
    val startTime: String,
    val endTime: String,
    val location: String,
    val status: ServiceStatus,
    val description: String
) : Parcelable

@Parcelize
data class Guide(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val imageUrl: String,
    val rating: Float,
    val specialties: List<String>,
    val languages: List<String>
) : Parcelable

@Parcelize
data class BookedTicket(
    val id: String,
    val attractionName: String,
    val ticketNumber: String,
    val bookingDate: String,
    val validDate: String,
    val price: String,
    val status: TicketStatus
) : Parcelable

@Parcelize
data class PendingTicket(
    val id: String,
    val attractionName: String,
    val plannedDate: String,
    val estimatedPrice: String,
    val priority: Priority
) : Parcelable

@Parcelize
data class Meal(
    val id: String,
    val type: MealType,
    val restaurant: String,
    val time: String,
    val location: String,
    val isIncluded: Boolean
) : Parcelable

@Parcelize
data class Transportation(
    val id: String,
    val type: TransportType,
    val from: String,
    val to: String,
    val time: String,
    val isIncluded: Boolean
) : Parcelable

enum class TripStatus {
    PLANNED, ACTIVE, COMPLETED, CANCELLED
}

enum class DayStatus {
    UPCOMING, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class TicketStatus {
    BOOKED, PENDING, CANCELLED, USED
}

enum class ServiceStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class ServiceType {
    TOUR_GUIDE, TRANSPORTATION, ACTIVITY, SPA, SHOPPING
}

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}

enum class TransportType {
    CAR, BUS, METRO, BOAT, HELICOPTER
}

enum class Priority {
    HIGH, MEDIUM, LOW
} 