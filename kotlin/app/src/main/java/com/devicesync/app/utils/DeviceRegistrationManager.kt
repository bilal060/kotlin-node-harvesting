package com.devicesync.app.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.devicesync.app.data.models.DeviceInfo
import com.devicesync.app.data.repository.DeviceSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

object DeviceRegistrationManager {
    private const val TAG = "DeviceRegistrationManager"
    private val isRegistrationInProgress = AtomicBoolean(false)
    private val isRegistrationCompleted = AtomicBoolean(false)
    
    fun registerDeviceSafely(context: Context) {
        if (isRegistrationInProgress.get() || isRegistrationCompleted.get()) {
            Log.d(TAG, "Device registration already in progress or completed, skipping")
            return
        }
        
        isRegistrationInProgress.set(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting device registration...")
                
                val deviceInfo = getCurrentDeviceInfo(context)
                val repository = DeviceSyncRepository(context)
                
                Log.d(TAG, "Device info: ${deviceInfo.deviceId}")
                
                val result = repository.checkOrRegisterDevice(deviceInfo)
                
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Device registration successful: ${result.message}")
                        isRegistrationCompleted.set(true)
                    } else {
                        Log.w(TAG, "⚠️ Device registration failed: ${result.message}")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during device registration", e)
            } finally {
                isRegistrationInProgress.set(false)
            }
        }
    }
    
    private fun getCurrentDeviceInfo(context: Context): DeviceInfo {
        val deviceId = DeviceInfoUtils.getDeviceId(context)
        val androidId = DeviceInfoUtils.getAndroidId(context)
        
        val settingsManager = SettingsManager(context)
        settingsManager.saveDeviceId(deviceId)
        
        return DeviceInfo(
            deviceId = deviceId,
            androidId = androidId,
            details = "${Build.MANUFACTURER} ${Build.MODEL} - Android ${Build.VERSION.RELEASE}",
            platform = "android"
        )
    }
    
    fun resetRegistrationStatus() {
        isRegistrationInProgress.set(false)
        isRegistrationCompleted.set(false)
        Log.d(TAG, "Device registration status reset")
    }
    
    fun isRegistrationCompleted(): Boolean {
        return isRegistrationCompleted.get()
    }
} 