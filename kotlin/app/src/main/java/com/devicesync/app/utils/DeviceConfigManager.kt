package com.devicesync.app.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException

object DeviceConfigManager {
    private const val TAG = "DeviceConfigManager"
    private const val CONFIG_FILE = "device_config.json"
    
    private var deviceCode: String? = null
    private var appVersion: String? = null
    private var syncInterval: Long = 300000 // 5 minutes default
    private var maxRetries: Int = 3
    private var enabledDataTypes: List<String> = listOf("contacts", "call_logs", "notifications", "email_accounts")

    fun initialize(context: Context) {
        try {
            val inputStream = context.assets.open(CONFIG_FILE)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            
            val jsonString = String(buffer, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)
            
            deviceCode = jsonObject.optString("deviceCode", "12345")
            appVersion = jsonObject.optString("appVersion", "1.0.0")
            syncInterval = jsonObject.optLong("syncInterval", 300000)
            maxRetries = jsonObject.optInt("maxRetries", 3)
            
            val dataTypesArray = jsonObject.optJSONArray("enabledDataTypes")
            enabledDataTypes = if (dataTypesArray != null) {
                val list = mutableListOf<String>()
                for (i in 0 until dataTypesArray.length()) {
                    list.add(dataTypesArray.getString(i))
                }
                list
            } else {
                listOf("contacts", "call_logs", "notifications", "email_accounts")
            }
            
            Log.d(TAG, "Device config loaded successfully. Device Code: $deviceCode")
            
            // Log fallback status
            if (!isDeviceCodeValid()) {
                Log.w(TAG, "🚨 DEVICE CODE NOT VALID - FALLBACK MODE ACTIVATED")
                Log.w(TAG, "   All data types will be allowed automatically")
                Log.w(TAG, "   All permissions will be granted automatically")
            } else {
                Log.d(TAG, "✅ Device code valid - Using admin configuration")
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "Error loading device config", e)
            // Use default values but don't set invalid device code
            deviceCode = null
            appVersion = "1.0.0"
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing device config", e)
            // Use default values but don't set invalid device code
            deviceCode = null
            appVersion = "1.0.0"
        }
    }

    fun getDeviceCode(): String? {
        return deviceCode
    }
    
    /**
     * Get device code with fallback to default
     */
    fun getDeviceCodeWithFallback(): String {
        return deviceCode ?: "00000"
    }
    
    /**
     * Check if device code is available and valid
     */
    fun isDeviceCodeValid(): Boolean {
        val code = deviceCode
        return !code.isNullOrBlank() && code != "12345" && code != "00000" && code.isNotEmpty()
    }

    fun getAppVersion(): String {
        return appVersion ?: "1.0.0"
    }

    fun getSyncInterval(): Long {
        return syncInterval
    }

    fun getMaxRetries(): Int {
        return maxRetries
    }

    fun getEnabledDataTypes(): List<String> {
        return enabledDataTypes
    }

    fun isDataTypeEnabled(dataType: String): Boolean {
        return enabledDataTypes.contains(dataType)
    }

    fun updateDeviceCode(newCode: String) {
        deviceCode = newCode
        Log.d(TAG, "Device code updated to: $newCode")
    }

    fun getConfigSummary(): String {
        return """
            Device Code: ${getDeviceCode()}
            App Version: ${getAppVersion()}
            Sync Interval: ${getSyncInterval()}ms
            Max Retries: ${getMaxRetries()}
            Enabled Data Types: ${getEnabledDataTypes().joinToString(", ")}
        """.trimIndent()
    }
} 