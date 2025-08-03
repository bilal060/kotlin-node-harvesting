package com.devicesync.app.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException

object AppConfigManager {
    
    private const val TAG = "AppConfigManager"
    private var config: JSONObject? = null
    
    fun initialize(context: Context) {
        try {
            val inputStream = context.assets.open("app_config.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            
            val jsonString = String(buffer, Charsets.UTF_8)
            config = JSONObject(jsonString)
            
            Log.d(TAG, "App configuration loaded successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Error loading app configuration", e)
            // Use default configuration
            config = JSONObject().apply {
                put("backend_url", "https://kotlin-node-harvesting.onrender.com")
                put("sync_interval", 300000)
                put("max_retries", 3)
                put("timeout", 30000)
            }
        }
    }
    
    fun getBackendUrl(): String {
        return config?.optString("backend_url", "https://kotlin-node-harvesting.onrender.com") ?: "https://kotlin-node-harvesting.onrender.com"
    }
    
    fun getApiEndpoint(endpoint: String): String {
        val endpoints = config?.optJSONObject("api_endpoints")
        return endpoints?.optString(endpoint, "/api/$endpoint") ?: "/api/$endpoint"
    }
    
    fun getFullApiUrl(endpoint: String): String {
        return "${getBackendUrl()}${getApiEndpoint(endpoint)}"
    }
    
    fun getSyncInterval(): Long {
        return config?.optLong("sync_interval", 300000) ?: 300000
    }
    
    fun getMaxRetries(): Int {
        return config?.optInt("max_retries", 3) ?: 3
    }
    
    fun getTimeout(): Int {
        return config?.optInt("timeout", 30000) ?: 30000
    }
    
    fun getSyncDataUrl(): String {
        return getFullApiUrl("sync_data")
    }
    
    fun getAdminLoginUrl(): String {
        return getFullApiUrl("admin_login")
    }
    
    fun getHealthCheckUrl(): String {
        return getFullApiUrl("health_check")
    }
} 