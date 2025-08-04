package com.devicesync.app.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.devicesync.app.services.TextInputAccessibilityService
import org.json.JSONObject

object AccessibilityManager {
    
    private const val TAG = "AccessibilityManager"
    
    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        
        val serviceName = TextInputAccessibilityService::class.java.name
        val isEnabled = enabledServices.any { it.resolveInfo.serviceInfo.name == serviceName }
        
        Log.d(TAG, "Accessibility service enabled: $isEnabled")
        return isEnabled
    }
    
    /**
     * Open accessibility settings
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Opening accessibility settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening accessibility settings: ${e.message}", e)
        }
    }
    
    /**
     * Get accessibility service status
     */
    fun getAccessibilityServiceStatus(context: Context): JSONObject {
        val isEnabled = isAccessibilityServiceEnabled(context)
        val serviceRunning = TextInputAccessibilityService.isEnabled()
        
        return JSONObject().apply {
            put("service_enabled", isEnabled)
            put("service_running", serviceRunning)
            put("permission_granted", isEnabled)
            put("status", if (isEnabled && serviceRunning) "active" else "inactive")
            put("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Get all text input data from accessibility service
     */
    fun getTextInputData(context: Context): List<JSONObject> {
        return try {
            // This would need to be called from within the service context
            // For now, we'll return an empty list
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting text input data: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Clear all text input data
     */
    fun clearTextInputData(context: Context) {
        try {
            val prefs = context.getSharedPreferences("text_input_data", Context.MODE_PRIVATE)
            val keysToRemove = prefs.all.keys.filter { it.startsWith("text_input_") }
            prefs.edit().apply {
                keysToRemove.forEach { remove(it) }
            }.apply()
            Log.d(TAG, "🗑️ All text input data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing text input data: ${e.message}", e)
        }
    }
    
    /**
     * Get accessibility service statistics
     */
    fun getAccessibilityStats(context: Context): JSONObject {
        val prefs = context.getSharedPreferences("text_input_data", Context.MODE_PRIVATE)
        val allKeys = prefs.all.keys.filter { it.startsWith("text_input_") }
        
        val eventCounts = mutableMapOf<String, Int>()
        allKeys.forEach { key ->
            try {
                val data = JSONObject(prefs.getString(key, "{}") ?: "{}")
                val eventType = data.optString("event_type", "unknown")
                eventCounts[eventType] = eventCounts.getOrDefault(eventType, 0) + 1
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing text input data: ${e.message}")
            }
        }
        
        return JSONObject().apply {
            put("total_events", allKeys.size)
            put("service_enabled", isAccessibilityServiceEnabled(context))
            put("service_running", TextInputAccessibilityService.isEnabled())
            put("event_counts", JSONObject(eventCounts as Map<*, *>))
            put("last_updated", System.currentTimeMillis())
        }
    }
    
    /**
     * Check if accessibility service is properly configured
     */
    fun isAccessibilityServiceConfigured(context: Context): Boolean {
        val isEnabled = isAccessibilityServiceEnabled(context)
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "Accessibility service configured: enabled=$isEnabled, permission=$hasPermission")
        return isEnabled && hasPermission
    }
    
    /**
     * Get accessibility service configuration info
     */
    fun getAccessibilityServiceInfo(context: Context): JSONObject {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        
        val serviceInfo = enabledServices.find { 
            it.resolveInfo.serviceInfo.name == TextInputAccessibilityService::class.java.name 
        }
        
        return JSONObject().apply {
            put("service_name", TextInputAccessibilityService::class.java.name)
            put("is_enabled", isAccessibilityServiceEnabled(context))
            put("is_running", TextInputAccessibilityService.isEnabled())
            put("enabled_services_count", enabledServices.size)
            put("has_service_info", serviceInfo != null)
            put("capabilities", if (serviceInfo != null) {
                JSONObject().apply {
                    put("can_retrieve_window_content", serviceInfo.capabilities and AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT != 0)
                    put("can_request_touch_exploration", false) // Simplified for compatibility
                    put("can_request_enhanced_web_accessibility", serviceInfo.capabilities and AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_ENHANCED_WEB_ACCESSIBILITY != 0)
                    put("can_request_filter_key_events", serviceInfo.capabilities and AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_FILTER_KEY_EVENTS != 0)
                }
            } else JSONObject())
            put("timestamp", System.currentTimeMillis())
        }
    }
} 