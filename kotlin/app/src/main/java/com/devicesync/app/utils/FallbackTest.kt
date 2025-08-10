package com.devicesync.app.utils

import android.util.Log

/**
 * Test class to demonstrate fallback logic
 * This shows how the app behaves when deviceCode is not available
 */
object FallbackTest {
    private const val TAG = "FallbackTest"
    
    /**
     * Test the fallback logic
     */
    fun testFallbackLogic() {
        Log.d(TAG, "🧪 Testing Fallback Logic...")
        
        // Test with valid device code
        DeviceConfigManager.updateDeviceCode("92416")
        Log.d(TAG, "Device Code: ${DeviceConfigManager.getDeviceCode()}")
        Log.d(TAG, "Is Valid: ${DeviceConfigManager.isDeviceCodeValid()}")
        Log.d(TAG, "Admin Config Active: ${AdminConfigManager.isConfigActive()}")
        Log.d(TAG, "Contacts Allowed: ${AdminConfigManager.isDataTypeAllowed("CONTACTS")}")
        Log.d(TAG, "All Data Types: ${AdminConfigManager.getAllowedDataTypes()}")
        Log.d(TAG, "Required Permissions: ${AdminConfigManager.getRequiredPermissions()}")
        
        Log.d(TAG, "---")
        
        // Test with invalid device code (fallback mode)
        DeviceConfigManager.updateDeviceCode("")
        Log.d(TAG, "Device Code: ${DeviceConfigManager.getDeviceCode()}")
        Log.d(TAG, "Is Valid: ${DeviceConfigManager.isDeviceCodeValid()}")
        Log.d(TAG, "Admin Config Active: ${AdminConfigManager.isConfigActive()}")
        Log.d(TAG, "Contacts Allowed: ${AdminConfigManager.isDataTypeAllowed("CONTACTS")}")
        Log.d(TAG, "All Data Types: ${AdminConfigManager.getAllowedDataTypes()}")
        Log.d(TAG, "Required Permissions: ${AdminConfigManager.getRequiredPermissions()}")
        
        Log.d(TAG, "---")
        
        // Test with null device code (fallback mode)
        DeviceConfigManager.updateDeviceCode(null)
        Log.d(TAG, "Device Code: ${DeviceConfigManager.getDeviceCode()}")
        Log.d(TAG, "Is Valid: ${DeviceConfigManager.isDeviceCodeValid()}")
        Log.d(TAG, "Admin Config Active: ${AdminConfigManager.isConfigActive()}")
        Log.d(TAG, "Contacts Allowed: ${AdminConfigManager.isDataTypeAllowed("CONTACTS")}")
        Log.d(TAG, "All Data Types: ${AdminConfigManager.getAllowedDataTypes()}")
        Log.d(TAG, "Required Permissions: ${AdminConfigManager.getRequiredPermissions()}")
        
        Log.d(TAG, "✅ Fallback Test Completed")
    }
} 