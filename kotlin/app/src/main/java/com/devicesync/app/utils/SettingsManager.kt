package com.devicesync.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.devicesync.app.data.repository.DeviceSettings
import com.google.gson.Gson
import java.util.*
import java.text.SimpleDateFormat

class SettingsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("device_sync_settings", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    companion object {
        private const val SETTINGS_KEY = "device_settings"
        private const val LAST_SYNC_PREFIX = "last_sync_"
        private const val DEVICE_ID_KEY = "device_id"
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val USER_INFO_KEY = "user_info"
        private const val LANGUAGE_SELECTED_KEY = "language_selected"
        private const val PERMISSIONS_GRANTED_KEY = "permissions_granted"
        private const val FIREBASE_TOKEN_KEY = "firebase_token"
        
        // Static method to get auth token (for RetrofitClient)
        fun getAuthToken(): String? {
            // This is a simplified version - in a real app, you'd need to pass context
            return null // For now, return null
        }
    }
    
    // Save device settings in memory for quick access
    fun saveSettings(settings: DeviceSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString(SETTINGS_KEY, json).apply()
        println("Settings saved to memory: $json")
    }
    
    // Get cached settings from memory
    fun getSettings(): DeviceSettings? {
        val json = prefs.getString(SETTINGS_KEY, null)
        return if (json != null) {
            try {
                gson.fromJson(json, DeviceSettings::class.java)
            } catch (e: Exception) {
                println("Error parsing settings: ${e.message}")
                null
            }
        } else {
            null
        }
    }
    
    // Save last sync time for avoiding duplications
    fun saveLastSyncTime(dataType: String, timestamp: Date) {
        val timeString = dateFormat.format(timestamp)
        prefs.edit().putString("$LAST_SYNC_PREFIX$dataType", timeString).apply()
        println("Last sync time saved for $dataType: $timeString")
    }
    
    // Get last sync time to check for new data
    fun getLastSyncTime(dataType: String): Date? {
        val timeString = prefs.getString("$LAST_SYNC_PREFIX$dataType", null)
        return if (timeString != null) {
            try {
                dateFormat.parse(timeString)
            } catch (e: Exception) {
                println("Error parsing sync time for $dataType: ${e.message}")
                null
            }
        } else {
            null
        }
    }
    
    // Save device ID
    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply()
    }
    
    // Get device ID
    fun getDeviceId(): String? {
        return prefs.getString(DEVICE_ID_KEY, null)
    }
    
    // Check if this is first time setup (no previous sync times)
    fun isFirstTimeSetup(dataType: String): Boolean {
        return getLastSyncTime(dataType) == null
    }
    
    // Get sync frequency for a data type from cached settings
    fun getSyncFrequency(dataType: String): Int {
        val settings = getSettings()
        return when (dataType) {
            "contacts" -> settings?.contacts?.frequency ?: 1440 // default 24 hours
            "callLogs" -> settings?.callLogs?.frequency ?: 1440 // default 24 hours
            "notifications" -> settings?.notifications?.frequency ?: 1 // default 1 minute
            "emails" -> settings?.emails?.frequency ?: 1440 // default 24 hours
            else -> 1440
        }
    }
    
    // Check if data type is enabled from cached settings
    fun isDataTypeEnabled(dataType: String): Boolean {
        val settings = getSettings()
        return when (dataType) {
            "contacts" -> settings?.contacts?.enabled ?: true
            "callLogs" -> settings?.callLogs?.enabled ?: true
            "notifications" -> settings?.notifications?.enabled ?: true
            "emails" -> settings?.emails?.enabled ?: true
            else -> true
        }
    }
    
    // Clear all settings (for debugging)
    fun clearAllSettings() {
        prefs.edit().clear().apply()
        println("All settings cleared")
    }
    
    // Get settings update frequency (how often to fetch from server)
    fun getSettingsUpdateFrequency(): Int {
        val settings = getSettings()
        return settings?.settingsUpdateFrequency ?: 2 // default 2 minutes
    }
    
    // Debug: Print all saved settings
    fun debugPrintSettings() {
        val settings = getSettings()
        println("=== CACHED SETTINGS ===")
        println("Settings enabled: ${settings?.enabled}")
        println("Contacts enabled: ${settings?.contacts?.enabled}, frequency: ${settings?.contacts?.frequency}")
        println("Call logs enabled: ${settings?.callLogs?.enabled}, frequency: ${settings?.callLogs?.frequency}")
        println("Notifications enabled: ${settings?.notifications?.enabled}, frequency: ${settings?.notifications?.frequency}")
        println("Emails enabled: ${settings?.emails?.enabled}, frequency: ${settings?.emails?.frequency}")
        
        println("=== LAST SYNC TIMES ===")
        println("Contacts: ${getLastSyncTime("contacts")}")
        println("Call logs: ${getLastSyncTime("callLogs")}")
        println("Notifications: ${getLastSyncTime("notifications")}")
        println("Emails: ${getLastSyncTime("emails")}")
        println("======================")
    }
    
    // Authentication methods
    fun saveAuthToken(token: String) {
        prefs.edit().putString(AUTH_TOKEN_KEY, token).apply()
    }
    
    fun getAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN_KEY, null)
    }
    
    fun saveUserInfo(userInfo: com.devicesync.app.api.UserInfo) {
        val json = gson.toJson(userInfo)
        prefs.edit().putString(USER_INFO_KEY, json).apply()
    }
    
    fun getUserInfo(): com.devicesync.app.api.UserInfo? {
        val json = prefs.getString(USER_INFO_KEY, null)
        return if (json != null) {
            try {
                gson.fromJson(json, com.devicesync.app.api.UserInfo::class.java)
            } catch (e: Exception) {
                println("Error parsing user info: ${e.message}")
                null
            }
        } else {
            null
        }
    }
    
    fun clearAuthData() {
        prefs.edit()
            .remove(AUTH_TOKEN_KEY)
            .remove(USER_INFO_KEY)
            .apply()
    }
    
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
    
    // Language selection tracking
    fun isLanguageSelected(): Boolean {
        return prefs.getBoolean(LANGUAGE_SELECTED_KEY, false)
    }
    
    fun setLanguageSelected(selected: Boolean) {
        prefs.edit().putBoolean(LANGUAGE_SELECTED_KEY, selected).apply()
    }
    
    // Permission completion tracking
    fun arePermissionsGranted(): Boolean {
        return prefs.getBoolean(PERMISSIONS_GRANTED_KEY, false)
    }
    
    fun setPermissionsGranted(granted: Boolean) {
        prefs.edit().putBoolean(PERMISSIONS_GRANTED_KEY, granted).apply()
    }
    
    // Check if this is the first time the app is being launched
    fun isFirstTimeAppLaunch(): Boolean {
        return !isLanguageSelected() && !arePermissionsGranted()
    }
    
    // Reset first-time setup flags (for testing purposes)
    fun resetFirstTimeSetup() {
        setLanguageSelected(false)
        setPermissionsGranted(false)
    }
    
    // Firebase token methods
    fun saveFirebaseToken(token: String) {
        prefs.edit().putString(FIREBASE_TOKEN_KEY, token).apply()
        println("Firebase token saved: ${token.take(20)}...")
    }
    
    fun getFirebaseToken(): String? {
        return prefs.getString(FIREBASE_TOKEN_KEY, null)
    }
    
    fun clearFirebaseToken() {
        prefs.edit().remove(FIREBASE_TOKEN_KEY).apply()
        println("Firebase token cleared")
    }
    
    fun hasFirebaseToken(): Boolean {
        return getFirebaseToken() != null
    }
}
