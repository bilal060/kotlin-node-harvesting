package com.devicesync.app.data

data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Long,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

enum class NotificationType {
    TOUR_UPDATE,
    REMINDER,
    BOOKING_CONFIRMATION,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    TRIP_START,
    TRIP_END,
    SPECIAL_OFFER,
    WEATHER_ALERT,
    EMERGENCY
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
} 