package com.devicesync.app.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object AppIdManager {
    
    private const val PREFS_NAME = "app_id_prefs"
    private const val KEY_APP_ID = "unique_app_id"
    private const val KEY_IS_REGISTERED = "is_registered"
    
    /**
     * Generate a unique 8-digit app ID
     */
    fun generateAppId(): String {
        val random = Random()
        val appId = StringBuilder()
        
        // Generate 8 random digits
        for (i in 0 until 8) {
            appId.append(random.nextInt(10))
        }
        
        return appId.toString()
    }
    
    /**
     * Get or create unique app ID
     */
    fun getOrCreateAppId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check if app ID already exists
        val existingAppId = prefs.getString(KEY_APP_ID, null)
        if (existingAppId != null) {
            return existingAppId
        }
        
        // Generate new app ID
        val newAppId = generateAppId()
        prefs.edit()
            .putString(KEY_APP_ID, newAppId)
            .putBoolean(KEY_IS_REGISTERED, true)
            .apply()
        
        return newAppId
    }
    
    /**
     * Get existing app ID (returns null if not exists)
     */
    fun getAppId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_APP_ID, null)
    }
    
    /**
     * Check if app ID is registered
     */
    fun isAppIdRegistered(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_REGISTERED, false)
    }
    
    /**
     * Register app ID with backend (you can implement this)
     */
    suspend fun registerAppIdWithBackend(context: Context, appId: String): Boolean {
        // TODO: Implement backend registration
        // This would typically make an API call to register the app ID
        return true
    }
    
    /**
     * Clear app ID (for testing purposes)
     */
    fun clearAppId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
} 