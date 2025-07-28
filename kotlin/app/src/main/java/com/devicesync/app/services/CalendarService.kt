package com.devicesync.app.services

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.devicesync.app.data.Activity
import com.devicesync.app.data.TripTemplate
import java.util.*

class CalendarService(private val context: Context) {
    
    suspend fun addTripToCalendar(tripTemplate: TripTemplate, startDate: Date): Result<Long> {
        return try {
            val contentValues = ContentValues().apply {
                put(CalendarContract.Events.TITLE, tripTemplate.name)
                put(CalendarContract.Events.DESCRIPTION, tripTemplate.description)
                put(CalendarContract.Events.DTSTART, startDate.time)
                put(CalendarContract.Events.DTEND, startDate.time + (tripTemplate.duration * 24 * 60 * 60 * 1000L))
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.CALENDAR_ID, 1) // Default calendar
                put(CalendarContract.Events.EVENT_LOCATION, "Dubai, UAE")
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            if (eventId != null) {
                // Add reminder
                addReminder(eventId, 60) // 1 hour before
                Result.success(eventId)
            } else {
                Result.failure(Exception("Failed to create calendar event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addActivityToCalendar(activity: Activity, date: Date): Result<Long> {
        return try {
            val contentValues = ContentValues().apply {
                put(CalendarContract.Events.TITLE, activity.name)
                put(CalendarContract.Events.DESCRIPTION, activity.description)
                put(CalendarContract.Events.DTSTART, date.time)
                put(CalendarContract.Events.DTEND, date.time + (2 * 60 * 60 * 1000L)) // 2 hours duration
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.CALENDAR_ID, 1) // Default calendar
                put(CalendarContract.Events.EVENT_LOCATION, activity.category)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            if (eventId != null) {
                // Add reminder
                addReminder(eventId, 30) // 30 minutes before
                Result.success(eventId)
            } else {
                Result.failure(Exception("Failed to create calendar event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun addReminder(eventId: Long, minutesBefore: Int) {
        try {
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
            }
            
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        } catch (e: Exception) {
            // Handle reminder creation failure
        }
    }
    
    suspend fun removeEventFromCalendar(eventId: Long): Result<Boolean> {
        return try {
            val deletedRows = context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "${CalendarContract.Events._ID} = ?",
                arrayOf(eventId.toString())
            )
            
            Result.success(deletedRows > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpcomingEvents(days: Int = 7): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        
        try {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (days * 24 * 60 * 60 * 1000L)
            
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION
            )
            
            val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
            val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
            
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val event = CalendarEvent(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)),
                        startTime = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)),
                        endTime = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)),
                        location = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
                    )
                    events.add(event)
                }
            }
        } catch (e: Exception) {
            // Handle query failure
        }
        
        return events
    }
    
    fun hasCalendarPermission(): Boolean {
        // Check if app has calendar permission
        return true // Simplified for demo
    }
}

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val location: String
) 