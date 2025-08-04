package com.devicesync.app.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.devicesync.app.services.AppInstallationTracker
import org.json.JSONObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object InstallationManager {
    
    private const val TAG = "InstallationManager"
    
    /**
     * Check if app is currently installed
     */
    fun isAppInstalled(): Boolean {
        return AppInstallationTracker.getInstallationStatus()
    }
    
    /**
     * Get installation timestamp
     */
    fun getInstallationTimestamp(): Long {
        return AppInstallationTracker.getInstallationTimestamp()
    }
    
    /**
     * Get installation date as formatted string
     */
    fun getInstallationDate(): String {
        val timestamp = getInstallationTimestamp()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }
    
    /**
     * Get complete installation information
     */
    fun getInstallationInfo(context: Context): JSONObject {
        return AppInstallationTracker().getInstallationInfo(context)
    }
    
    /**
     * Manually trigger installation status check
     */
    fun checkInstallationStatus(context: Context) {
        try {
            val intent = Intent("APP_INSTALLATION_STATUS")
            context.sendBroadcast(intent)
            Log.d(TAG, "📋 Installation status check triggered")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error triggering installation status check: ${e.message}", e)
        }
    }
    
    /**
     * Get installation status summary
     */
    fun getInstallationSummary(context: Context): String {
        val isInstalled = isAppInstalled()
        val installDate = getInstallationDate()
        val deviceId = SettingsManager(context).getDeviceId() ?: "unknown"
        
        return """
            📱 Installation Status: ${if (isInstalled) "✅ Installed" else "❌ Not Installed"}
            📅 Installation Date: $installDate
            🆔 Device ID: $deviceId
        """.trimIndent()
    }
    
    /**
     * Check if this is a fresh installation
     */
    fun isFreshInstallation(context: Context): Boolean {
        val settingsManager = SettingsManager(context)
        return settingsManager.isFirstTimeAppLaunch()
    }
    
    /**
     * Get installation statistics
     */
    fun getInstallationStats(context: Context): JSONObject {
        val settingsManager = SettingsManager(context)
        
        return JSONObject().apply {
            put("is_installed", isAppInstalled())
            put("installation_timestamp", getInstallationTimestamp())
            put("installation_date", getInstallationDate())
            put("is_fresh_installation", isFreshInstallation(context))
            put("device_id", settingsManager.getDeviceId())
            put("has_firebase_token", settingsManager.hasFirebaseToken())
            put("language_selected", settingsManager.isLanguageSelected())
            put("permissions_granted", settingsManager.arePermissionsGranted())
            put("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Force update installation status to server
     */
    fun forceUpdateInstallationStatus(context: Context) {
        try {
            val settingsManager = SettingsManager(context)
            val deviceId = settingsManager.getDeviceId() ?: return
            
            // Create installation data
            val installationData = JSONObject().apply {
                put("device_id", deviceId)
                put("is_installed", true)
                put("action", "force_update")
                put("timestamp", System.currentTimeMillis())
                put("formatted_time", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
                    .format(java.util.Date()))
            }
            
            // Send to server
            GlobalScope.launch {
                try {
                    val repository = com.devicesync.app.data.repository.DeviceSyncRepository(context)
                    val result = repository.sendInstallationStatus(deviceId, installationData)
                    
                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Installation status force updated successfully")
                    } else {
                        Log.e(TAG, "❌ Failed to force update installation status: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error force updating installation status: ${e.message}", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in force update installation status: ${e.message}", e)
        }
    }
    
    /**
     * Get installation history (if available)
     */
    fun getInstallationHistory(context: Context): List<JSONObject> {
        // This would typically come from a local database or server
        // For now, return current installation info
        return listOf(getInstallationInfo(context))
    }
    
    /**
     * Check if app was recently installed (within last 24 hours)
     */
    fun isRecentlyInstalled(): Boolean {
        val installTime = getInstallationTimestamp()
        val currentTime = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        
        return (currentTime - installTime) < twentyFourHours
    }
    
    /**
     * Get time since installation
     */
    fun getTimeSinceInstallation(): String {
        val installTime = getInstallationTimestamp()
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - installTime
        
        val days = diff / (24 * 60 * 60 * 1000L)
        val hours = (diff % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
        val minutes = (diff % (60 * 60 * 1000L)) / (60 * 1000L)
        
        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
} 