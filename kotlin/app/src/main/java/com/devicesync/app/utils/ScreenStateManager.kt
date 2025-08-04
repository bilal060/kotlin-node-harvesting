package com.devicesync.app.utils

import android.content.Context
import android.util.Log

object ScreenStateManager {
    
    private const val TAG = "ScreenStateManager"
    private val activeScreens = mutableSetOf<String>()
    
    /**
     * Set a screen as active/inactive
     */
    fun setScreenActive(screenId: String, isActive: Boolean) {
        if (isActive) {
            activeScreens.add(screenId)
        } else {
            activeScreens.remove(screenId)
        }
        Log.d(TAG, "📱 Screen state changed: $screenId = $isActive")
    }
    
    /**
     * Check if a screen is active
     */
    fun isScreenActive(screenId: String): Boolean {
        return activeScreens.contains(screenId)
    }
    
    /**
     * Set sync screen as active
     */
    fun setSyncScreenActive(isActive: Boolean) {
        setScreenActive("sync_screen", isActive)
    }
    
    /**
     * Check if sync screen is active
     */
    fun isSyncScreenActive(): Boolean {
        return isScreenActive("sync_screen")
    }
    
    /**
     * Set data viewer screen as active
     */
    fun setDataViewerScreenActive(dataType: String, isActive: Boolean) {
        setScreenActive("data_viewer_$dataType", isActive)
    }
    
    /**
     * Check if data viewer screen is active
     */
    fun isDataViewerScreenActive(dataType: String): Boolean {
        return isScreenActive("data_viewer_$dataType")
    }
    
    /**
     * Set chat screen as active
     */
    fun setChatScreenActive(chatId: String, isActive: Boolean) {
        setScreenActive("chat_$chatId", isActive)
    }
    
    /**
     * Check if chat screen is active
     */
    fun isChatScreenActive(chatId: String): Boolean {
        return isScreenActive("chat_$chatId")
    }
    
    /**
     * Set main activity as active
     */
    fun setMainActivityActive(isActive: Boolean) {
        setScreenActive("main_activity", isActive)
    }
    
    /**
     * Check if main activity is active
     */
    fun isMainActivityActive(): Boolean {
        return isScreenActive("main_activity")
    }
    
    /**
     * Set login screen as active
     */
    fun setLoginScreenActive(isActive: Boolean) {
        setScreenActive("login_screen", isActive)
    }
    
    /**
     * Check if login screen is active
     */
    fun isLoginScreenActive(): Boolean {
        return isScreenActive("login_screen")
    }
    
    /**
     * Get all active screens
     */
    fun getActiveScreens(): List<String> {
        return activeScreens.toList()
    }
    
    /**
     * Clear all screen states
     */
    fun clearAllScreenStates() {
        activeScreens.clear()
        Log.d(TAG, "🗑️ All screen states cleared")
    }
    
    /**
     * Get screen state summary
     */
    fun getScreenStateSummary(): String {
        val activeScreens = getActiveScreens()
        return if (activeScreens.isEmpty()) {
            "No active screens"
        } else {
            "Active screens: ${activeScreens.joinToString(", ")}"
        }
    }
} 