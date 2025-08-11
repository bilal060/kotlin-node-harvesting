package com.dubaidiscoveries.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.dubaidiscoveries.app.models.PermissionData
import com.google.gson.Gson

class PreferenceManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "DubaiDiscoveriesPrefs"
        private const val KEY_PERMISSIONS_VERIFIED = "permissions_verified"
        private const val KEY_PERMISSIONS_DATA = "permissions_data"
        private const val KEY_DEVICE_CODE = "device_code"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_APP_VERSION = "app_version"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun isPermissionsVerified(): Boolean {
        return sharedPreferences.getBoolean(KEY_PERMISSIONS_VERIFIED, false)
    }
    
    fun setPermissionsVerified(verified: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PERMISSIONS_VERIFIED, verified).apply()
    }
    
    fun savePermissions(permissions: PermissionData) {
        val permissionsJson = gson.toJson(permissions)
        sharedPreferences.edit()
            .putString(KEY_PERMISSIONS_DATA, permissionsJson)
            .putBoolean(KEY_PERMISSIONS_VERIFIED, true)
            .apply()
    }
    
    fun getPermissions(): PermissionData? {
        val permissionsJson = sharedPreferences.getString(KEY_PERMISSIONS_DATA, null)
        return if (permissionsJson != null) {
            try {
                gson.fromJson(permissionsJson, PermissionData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    fun saveDeviceCode(deviceCode: String) {
        sharedPreferences.edit().putString(KEY_DEVICE_CODE, deviceCode).apply()
    }
    
    fun getDeviceCode(): String {
        return sharedPreferences.getString(KEY_DEVICE_CODE, "61250") ?: "61250"
    }
    
    fun setLastSyncTime(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }
    
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0L)
    }
    
    fun setAppVersion(version: String) {
        sharedPreferences.edit().putString(KEY_APP_VERSION, version).apply()
    }
    
    fun getAppVersion(): String {
        return sharedPreferences.getString(KEY_APP_VERSION, "1.0.0") ?: "1.0.0"
    }
    
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun checkPermissionExpiry(): Boolean {
        val permissions = getPermissions()
        if (permissions == null) return true
        
        // Check if permissions have expired (24 hours)
        val lastSync = getLastSyncTime()
        val currentTime = System.currentTimeMillis()
        val expiryTime = 24 * 60 * 60 * 1000 // 24 hours in milliseconds
        
        return (currentTime - lastSync) > expiryTime
    }
    
    fun needsPermissionRenewal(): Boolean {
        return !isPermissionsVerified() || checkPermissionExpiry()
    }
}
