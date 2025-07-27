package com.devicesync.app.utils

import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceInfoUtils {
    
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        
        return androidId.ifEmpty { 
            "device_${UUID.randomUUID().toString().substring(0, 8)}" 
        }
    }
    
    fun getUserName(context: Context): String {
        // Try to get user name from system
        val userName = android.os.Build.USER
        
        return if (userName.isNotEmpty() && userName != "root") {
            userName
        } else {
            // Generate a unique user name
            "guest_${UUID.randomUUID().toString().substring(0, 6)}"
        }
    }
}
