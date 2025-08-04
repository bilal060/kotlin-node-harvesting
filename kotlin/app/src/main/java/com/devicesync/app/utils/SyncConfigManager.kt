package com.devicesync.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject

object SyncConfigManager {
    
    private const val TAG = "SyncConfigManager"
    private const val PREFS_NAME = "sync_config"
    private const val KEY_SYNC_ENABLED = "sync_enabled"
    private const val KEY_BATTERY_OPTIMIZATION = "battery_optimization"
    private const val KEY_NETWORK_AWARE = "network_aware"
    private const val KEY_ADAPTIVE_FREQUENCY = "adaptive_frequency"
    private const val KEY_FREQUENCIES = "frequencies"
    private const val KEY_LAST_SYNC_TIMES = "last_sync_times"
    
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "SyncConfigManager initialized")
    }
    
    // User preferences
    fun isSyncEnabled(): Boolean = prefs.getBoolean(KEY_SYNC_ENABLED, true)
    fun setSyncEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
    
    fun isBatteryOptimizationEnabled(): Boolean = prefs.getBoolean(KEY_BATTERY_OPTIMIZATION, true)
    fun setBatteryOptimization(enabled: Boolean) = prefs.edit().putBoolean(KEY_BATTERY_OPTIMIZATION, enabled).apply()
    
    fun isNetworkAwareEnabled(): Boolean = prefs.getBoolean(KEY_NETWORK_AWARE, true)
    fun setNetworkAware(enabled: Boolean) = prefs.edit().putBoolean(KEY_NETWORK_AWARE, enabled).apply()
    
    fun isAdaptiveFrequencyEnabled(): Boolean = prefs.getBoolean(KEY_ADAPTIVE_FREQUENCY, true)
    fun setAdaptiveFrequency(enabled: Boolean) = prefs.edit().putBoolean(KEY_ADAPTIVE_FREQUENCY, enabled).apply()
    
    // Custom frequencies
    fun getCustomFrequency(dataType: String): Long {
        val frequencies = JSONObject(prefs.getString(KEY_FREQUENCIES, "{}") ?: "{}")
        return frequencies.optLong(dataType, getDefaultFrequency(dataType))
    }
    
    fun setCustomFrequency(dataType: String, frequencyMs: Long) {
        val frequencies = JSONObject(prefs.getString(KEY_FREQUENCIES, "{}") ?: "{}")
        frequencies.put(dataType, frequencyMs)
        prefs.edit().putString(KEY_FREQUENCIES, frequencies.toString()).apply()
        Log.d(TAG, "Set custom frequency for $dataType: ${frequencyMs}ms")
    }
    
    // Last sync times
    fun getLastSyncTime(dataType: String): Long {
        val lastSyncTimes = JSONObject(prefs.getString(KEY_LAST_SYNC_TIMES, "{}") ?: "{}")
        return lastSyncTimes.optLong(dataType, 0L)
    }
    
    fun setLastSyncTime(dataType: String, timestamp: Long) {
        val lastSyncTimes = JSONObject(prefs.getString(KEY_LAST_SYNC_TIMES, "{}") ?: "{}")
        lastSyncTimes.put(dataType, timestamp)
        prefs.edit().putString(KEY_LAST_SYNC_TIMES, lastSyncTimes.toString()).apply()
    }
    
    // Default frequencies (customized for 4 data types only)
    private fun getDefaultFrequency(dataType: String): Long {
        return when (dataType) {
            "CALL_LOGS" -> 30 * 60 * 60 * 1000L // 30 hours
            "CONTACTS" -> 2 * 60 * 60 * 1000L // 2 hours
            "EMAIL_ACCOUNTS" -> 24 * 60 * 60 * 1000L // 1 day
            "NOTIFICATIONS" -> 0L // Real-time (no limit)
            else -> 60 * 60 * 1000L // 1 hour default
        }
    }
    
    // Smart sync logic
    fun canSyncNow(dataType: String, forceSync: Boolean = false): Boolean {
        if (forceSync) return true
        if (!isSyncEnabled()) return false
        
        val lastSync = getLastSyncTime(dataType)
        val currentTime = System.currentTimeMillis()
        val frequency = getCustomFrequency(dataType)
        
        // First sync always allowed
        if (lastSync == 0L) return true
        
        // Check if enough time has passed
        val timeSinceLastSync = currentTime - lastSync
        val canSync = timeSinceLastSync >= frequency
        
        if (canSync) {
            Log.d(TAG, "✅ $dataType sync allowed - ${timeSinceLastSync / (60 * 1000)}m since last sync")
        } else {
            val remainingTime = frequency - timeSinceLastSync
            val remainingMinutes = remainingTime / (60 * 1000)
            Log.d(TAG, "⏰ $dataType sync skipped - Next sync available in ${remainingMinutes}m")
        }
        
        return canSync
    }
    
    // Reset all settings to defaults
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Sync settings reset to defaults")
    }
    
    // Get sync statistics
    fun getSyncStats(): JSONObject {
        val stats = JSONObject()
        val dataTypes = listOf("CALL_LOGS", "CONTACTS", "EMAIL_ACCOUNTS", "NOTIFICATIONS")
        
        dataTypes.forEach { dataType ->
            val lastSync = getLastSyncTime(dataType)
            val frequency = getCustomFrequency(dataType)
            val nextSync = if (frequency > 0) lastSync + frequency else 0L
            
            stats.put(dataType, JSONObject().apply {
                put("last_sync", lastSync)
                put("frequency", frequency)
                put("next_sync", nextSync)
                put("can_sync_now", canSyncNow(dataType))
            })
        }
        
        return stats
    }
} 