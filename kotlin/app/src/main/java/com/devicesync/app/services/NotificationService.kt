package com.devicesync.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devicesync.app.MainActivity
import com.devicesync.app.R
import com.devicesync.app.data.NotificationPriority
import com.devicesync.app.data.NotificationType
import com.devicesync.app.data.PushNotification
import java.util.*

class NotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID_TOUR_UPDATES = "tour_updates"
        const val CHANNEL_ID_REMINDERS = "reminders"
        const val CHANNEL_ID_PAYMENTS = "payments"
        const val CHANNEL_ID_EMERGENCY = "emergency"
        
        const val NOTIFICATION_ID_TOUR_UPDATE = 1001
        const val NOTIFICATION_ID_REMINDER = 1002
        const val NOTIFICATION_ID_PAYMENT = 1003
        const val NOTIFICATION_ID_EMERGENCY = 1004
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_TOUR_UPDATES,
                    "Tour Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates about your tours and bookings"
                },
                NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Tour reminders and important dates"
                },
                NotificationChannel(
                    CHANNEL_ID_PAYMENTS,
                    "Payments",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Payment confirmations and updates"
                },
                NotificationChannel(
                    CHANNEL_ID_EMERGENCY,
                    "Emergency",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Emergency notifications and alerts"
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    fun showNotification(notification: PushNotification) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_data", notification.data.toString())
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, getChannelId(notification.type))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(getPriority(notification.priority))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
        
        // Add specific styling based on notification type
        when (notification.type) {
            NotificationType.TOUR_UPDATE -> {
                builder.setColor(0xFF2196F3.toInt()) // Blue
            }
            NotificationType.REMINDER -> {
                builder.setColor(0xFFFF9800.toInt()) // Orange
                builder.setVibrate(longArrayOf(0, 500, 200, 500))
            }
            NotificationType.BOOKING_CONFIRMATION -> {
                builder.setColor(0xFF4CAF50.toInt()) // Green
            }
            NotificationType.PAYMENT_SUCCESS -> {
                builder.setColor(0xFF4CAF50.toInt()) // Green
            }
            NotificationType.PAYMENT_FAILED -> {
                builder.setColor(0xFFF44336.toInt()) // Red
            }
            NotificationType.SPECIAL_OFFER -> {
                builder.setColor(0xFFE91E63.toInt()) // Pink
            }
            NotificationType.WEATHER_ALERT -> {
                builder.setColor(0xFF00BCD4.toInt()) // Cyan
            }
            NotificationType.EMERGENCY -> {
                builder.setColor(0xFFF44336.toInt()) // Red
                builder.setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            }
            else -> {
                builder.setColor(0xFF9C27B0.toInt()) // Purple
            }
        }
        
        val notificationId = getNotificationId(notification.type)
        notificationManager.notify(notificationId, builder.build())
    }
    
    private fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.TOUR_UPDATE,
            NotificationType.TRIP_START,
            NotificationType.TRIP_END -> CHANNEL_ID_TOUR_UPDATES
            NotificationType.REMINDER -> CHANNEL_ID_REMINDERS
            NotificationType.BOOKING_CONFIRMATION,
            NotificationType.PAYMENT_SUCCESS,
            NotificationType.PAYMENT_FAILED -> CHANNEL_ID_PAYMENTS
            NotificationType.EMERGENCY -> CHANNEL_ID_EMERGENCY
            else -> CHANNEL_ID_TOUR_UPDATES
        }
    }
    
    private fun getPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.URGENT -> NotificationCompat.PRIORITY_HIGH
        }
    }
    
    private fun getNotificationId(type: NotificationType): Int {
        return when (type) {
            NotificationType.TOUR_UPDATE,
            NotificationType.TRIP_START,
            NotificationType.TRIP_END -> NOTIFICATION_ID_TOUR_UPDATE
            NotificationType.REMINDER -> NOTIFICATION_ID_REMINDER
            NotificationType.BOOKING_CONFIRMATION,
            NotificationType.PAYMENT_SUCCESS,
            NotificationType.PAYMENT_FAILED -> NOTIFICATION_ID_PAYMENT
            NotificationType.EMERGENCY -> NOTIFICATION_ID_EMERGENCY
            else -> NOTIFICATION_ID_TOUR_UPDATE
        }
    }
    
    fun scheduleReminder(notification: PushNotification, delayInMillis: Long) {
        // This would integrate with WorkManager or AlarmManager for scheduled notifications
        // For now, we'll just show the notification immediately
        showNotification(notification)
    }
    
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
} 