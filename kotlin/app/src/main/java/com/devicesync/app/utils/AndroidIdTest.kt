package com.devicesync.app.utils

import android.content.Context
import android.provider.Settings
import android.util.Log

object AndroidIdTest {
    
    private const val TAG = "AndroidIdTest"
    
    /**
     * Test Android ID retrieval and validation
     * Android ID is a 64-bit hex string (16 characters) generated when user first sets up the device
     */
    fun testAndroidIdRetrieval(context: Context) {
        val androidId = getAndroidId(context)
        val isValid = validateAndroidId(androidId)
        
        Log.d(TAG, "=== Android ID Test Results ===")
        Log.d(TAG, "Android ID: $androidId")
        Log.d(TAG, "Length: ${androidId.length} characters")
        Log.d(TAG, "Is Valid Format: $isValid")
        Log.d(TAG, "Is Empty: ${androidId.isEmpty()}")
        Log.d(TAG, "Is Null: ${androidId.isNullOrEmpty()}")
        Log.d(TAG, "Hex Pattern: ${androidId.matches(Regex("^[0-9a-fA-F]{16}$"))}")
        Log.d(TAG, "===============================")
        
        // Additional validation
        if (androidId.isEmpty()) {
            Log.w(TAG, "⚠️ Android ID is empty - this might indicate a factory reset or new device")
        } else if (!isValid) {
            Log.w(TAG, "⚠️ Android ID format is invalid - expected 16-character hex string")
        } else {
            Log.i(TAG, "✅ Android ID is valid and ready for use")
        }
    }
    
    /**
     * Get Android ID using the standard method
     * Code: Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
     */
    fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Android ID: ${e.message}")
            ""
        }
    }
    
    /**
     * Validate Android ID format
     * Should be a 16-character hexadecimal string
     */
    fun validateAndroidId(androidId: String): Boolean {
        return androidId.isNotEmpty() && 
               androidId.length == 16 && 
               androidId.matches(Regex("^[0-9a-fA-F]{16}$"))
    }
    
    /**
     * Get device information including Android ID
     */
    fun getDeviceInfo(context: Context): Map<String, String> {
        val androidId = getAndroidId(context)
        
        return mapOf(
            "androidId" to androidId,
            "androidIdLength" to androidId.length.toString(),
            "androidIdValid" to validateAndroidId(androidId).toString(),
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "version" to android.os.Build.VERSION.RELEASE,
            "sdkVersion" to android.os.Build.VERSION.SDK_INT.toString(),
            "deviceId" to DeviceInfoUtils.getDeviceId(context)
        )
    }
    
    /**
     * Compare two Android IDs
     */
    fun compareAndroidIds(id1: String, id2: String): Boolean {
        return id1.equals(id2, ignoreCase = true)
    }
    
    /**
     * Generate a fallback device ID if Android ID is not available
     */
    fun generateFallbackDeviceId(): String {
        return "fallback_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
} 