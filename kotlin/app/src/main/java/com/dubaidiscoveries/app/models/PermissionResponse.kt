package com.dubaidiscoveries.app.models

import com.google.gson.annotations.SerializedName

data class PermissionResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: PermissionData?
)

data class PermissionData(
    @SerializedName("deviceCode")
    val deviceCode: String,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("androidId")
    val androidId: String,
    
    @SerializedName("permissions")
    val permissions: AppPermissions,
    
    @SerializedName("isActive")
    val isActive: Boolean,
    
    @SerializedName("isVerified")
    val isVerified: Boolean,
    
    @SerializedName("expiresAt")
    val expiresAt: String?,
    
    @SerializedName("lastChecked")
    val lastChecked: String
)

data class AppPermissions(
    @SerializedName("contacts")
    val contacts: Boolean,
    
    @SerializedName("callLogs")
    val callLogs: Boolean,
    
    @SerializedName("sms")
    val sms: Boolean,
    
    @SerializedName("storage")
    val storage: Boolean,
    
    @SerializedName("phoneState")
    val phoneState: Boolean,
    
    @SerializedName("location")
    val location: Boolean,
    
    @SerializedName("camera")
    val camera: Boolean,
    
    @SerializedName("attractions")
    val attractions: Boolean,
    
    @SerializedName("services")
    val services: Boolean,
    
    @SerializedName("tourPackages")
    val tourPackages: Boolean,
    
    @SerializedName("profile")
    val profile: Boolean
) {
    fun isAllPermissionsGranted(): Boolean {
        return contacts && callLogs && sms && storage && phoneState && 
               location && camera && attractions && services && tourPackages && profile
    }
}

data class DeviceInfo(
    @SerializedName("deviceCode")
    val deviceCode: String,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("androidId")
    val androidId: String,
    
    @SerializedName("deviceName")
    val deviceName: String,
    
    @SerializedName("manufacturer")
    val manufacturer: String,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("androidVersion")
    val androidVersion: String,
    
    @SerializedName("sdkVersion")
    val sdkVersion: Int,
    
    @SerializedName("screenResolution")
    val screenResolution: String,
    
    @SerializedName("totalStorage")
    val totalStorage: String,
    
    @SerializedName("availableStorage")
    val availableStorage: String
)
