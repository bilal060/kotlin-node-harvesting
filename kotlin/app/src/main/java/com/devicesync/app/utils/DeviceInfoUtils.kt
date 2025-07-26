package com.devicesync.app.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.devicesync.app.data.models.DeviceInfo
import java.security.MessageDigest

object DeviceInfoUtils {
    
    fun getDeviceInfo(context: Context): DeviceInfo {
        val deviceId = generateDeviceId(context)
        val details = buildDeviceDetails()
        
        return DeviceInfo(
            deviceId = deviceId,
            details = details,
            platform = "android"
        )
    }
    
    private fun generateDeviceId(context: Context): String {
        return try {
            // Try to get Android ID first
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            
            if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
                // Hash the Android ID for privacy
                hashString(androidId).substring(0, 12)
            } else {
                // Fallback to device characteristics
                val deviceInfo = "${Build.MANUFACTURER}-${Build.MODEL}-${Build.SERIAL}"
                hashString(deviceInfo).substring(0, 12)
            }
        } catch (e: Exception) {
            // Final fallback
            hashString("${Build.MANUFACTURER}-${Build.MODEL}-${System.currentTimeMillis()}").substring(0, 12)
        }
    }
    
    private fun buildDeviceDetails(): String {
        return buildString {
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Build: ${Build.DISPLAY}")
            appendLine("Hardware: ${Build.HARDWARE}")
            appendLine("Product: ${Build.PRODUCT}")
            appendLine("Brand: ${Build.BRAND}")
            appendLine("Board: ${Build.BOARD}")
            appendLine("CPU ABI: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine("Fingerprint: ${Build.FINGERPRINT}")
        }.trim()
    }
    
    private fun hashString(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }
    
    fun getDetailedDeviceInfo(context: Context): Map<String, Any> {
        return mapOf(
            "deviceId" to generateDeviceId(context),
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "product" to Build.PRODUCT,
            "device" to Build.DEVICE,
            "board" to Build.BOARD,
            "hardware" to Build.HARDWARE,
            "serial" to Build.SERIAL,
            "androidVersion" to Build.VERSION.RELEASE,
            "apiLevel" to Build.VERSION.SDK_INT,
            "buildNumber" to Build.DISPLAY,
            "fingerprint" to Build.FINGERPRINT,
            "bootloader" to Build.BOOTLOADER,
            "radioVersion" to Build.getRadioVersion(),
            "supportedAbis" to Build.SUPPORTED_ABIS.toList(),
            "tags" to Build.TAGS,
            "type" to Build.TYPE,
            "user" to Build.USER,
            "time" to Build.TIME,
            "host" to Build.HOST,
            "isEmulator" to isEmulator(),
            "isRooted" to isRooted(),
            "hasGooglePlayServices" to hasGooglePlayServices(context),
            "screenDensity" to context.resources.displayMetrics.density,
            "screenWidthPx" to context.resources.displayMetrics.widthPixels,
            "screenHeightPx" to context.resources.displayMetrics.heightPixels
        )
    }
    
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.PRODUCT.contains("sdk_google") ||
                Build.PRODUCT.contains("google_sdk") ||
                Build.PRODUCT.contains("sdk") ||
                Build.PRODUCT.contains("sdk_x86") ||
                Build.PRODUCT.contains("sdk_gphone64_arm64") ||
                Build.PRODUCT.contains("vbox86p") ||
                Build.PRODUCT.contains("emulator") ||
                Build.PRODUCT.contains("simulator")
    }
    
    private fun isRooted(): Boolean {
        return try {
            // Check for common root binaries
            val rootPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
            
            rootPaths.any { java.io.File(it).exists() }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasGooglePlayServices(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.gms", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
