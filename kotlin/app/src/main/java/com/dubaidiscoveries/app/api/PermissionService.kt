package com.dubaidiscoveries.app.api

import android.util.Log
import com.dubaidiscoveries.app.models.DeviceInfo
import com.dubaidiscoveries.app.models.PermissionResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class PermissionService {
    
    companion object {
        private const val TAG = "PermissionService"
        private const val BASE_URL = "http://10.0.2.2:5001" // For emulator testing
        private const val PERMISSION_GATEWAY_ENDPOINT = "/api/permissions/gateway/verify"
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    suspend fun verifyPermissions(deviceInfo: DeviceInfo): PermissionResponse? {
        return try {
            val requestBody = gson.toJson(deviceInfo).toRequestBody(jsonMediaType)
            
            val request = Request.Builder()
                .url("$BASE_URL$PERMISSION_GATEWAY_ENDPOINT")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            Log.d(TAG, "Sending permission verification request to: ${request.url}")
            Log.d(TAG, "Request body: ${gson.toJson(deviceInfo)}")
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            Log.d(TAG, "Response code: ${response.code}")
            Log.d(TAG, "Response body: $responseBody")
            
            if (response.isSuccessful && responseBody != null) {
                try {
                    gson.fromJson(responseBody, PermissionResponse::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response", e)
                    PermissionResponse(
                        success = false,
                        message = "Failed to parse response: ${e.message}",
                        data = null
                    )
                }
            } else {
                Log.w(TAG, "Request failed with code: ${response.code}")
                PermissionResponse(
                    success = false,
                    message = "Request failed with code: ${response.code}",
                    data = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error making request", e)
            PermissionResponse(
                success = false,
                message = "Network error: ${e.message}",
                data = null
            )
        }
    }
    
    suspend fun checkPermission(permissionName: String, deviceCode: String): Boolean {
        return try {
            val requestBody = gson.toJson(mapOf(
                "deviceCode" to deviceCode,
                "permissionName" to permissionName
            )).toRequestBody(jsonMediaType)
            
            val request = Request.Builder()
                .url("$BASE_URL/api/permissions/check/$permissionName")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission: $permissionName", e)
            false
        }
    }
    
    suspend fun getPermissions(deviceCode: String): PermissionResponse? {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/api/permissions/device/$deviceCode")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                gson.fromJson(responseBody, PermissionResponse::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting permissions", e)
            null
        }
    }
    
    suspend fun getPermissionAnalytics(deviceCode: String): String? {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/api/permissions/analytics/$deviceCode")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            response.body?.string()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting permission analytics", e)
            null
        }
    }
    
    suspend fun testConnectivity(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/api/health")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Connectivity test failed", e)
            false
        }
    }
}
