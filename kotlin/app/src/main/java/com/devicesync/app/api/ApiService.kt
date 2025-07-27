package com.devicesync.app.api

import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.DataTypeEnum
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Device management
    @POST("devices/register")
    suspend fun registerDevice(@Body deviceInfo: DeviceInfo): Response<ApiResponse<DeviceInfo>>
    
    @GET("devices")
    suspend fun getDevices(): Response<ApiResponse<List<DeviceInfo>>>
    
    @GET("devices/{deviceId}")
    suspend fun getDevice(@Path("deviceId") deviceId: String): Response<ApiResponse<DeviceInfo>>
    
    // Data synchronization (no authentication required)
    @POST("devices/{deviceId}/sync")
    suspend fun syncData(
        @Path("deviceId") deviceId: String,
        @Body syncRequest: SyncRequest
    ): Response<ApiResponse<SyncResponse>>
    
    @GET("data/{dataType}")
    suspend fun getSyncedData(
        @Path("deviceId") deviceId: String,
        @Path("dataType") dataType: String
    ): Response<ApiResponse<List<Any>>>
    
    @GET("devices/{deviceId}/sync-history")
    suspend fun getSyncHistory(
        @Path("deviceId") deviceId: String
    ): Response<ApiResponse<List<SyncHistoryItem>>>
    
    // Data type management
    @GET("devices/{deviceId}/data-types")
    suspend fun getDataTypes(
        @Path("deviceId") deviceId: String
    ): Response<ApiResponse<List<DataTypeInfo>>>
    
    @PUT("devices/{deviceId}/data-types/{dataType}")
    suspend fun updateDataType(
        @Path("deviceId") deviceId: String,
        @Path("dataType") dataType: String,
        @Body dataTypeInfo: DataTypeInfo
    ): Response<ApiResponse<DataTypeInfo>>
    
    // Authentication
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<AuthResponse>
}

// API Models
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

data class SyncRequest(
    val dataType: String,
    val data: List<Map<String, Any>>,
    val timestamp: String
)

data class SyncResponse(
    val success: Boolean,
    val itemsSynced: Int,
    val message: String
)

data class SyncHistoryItem(
    val id: String,
    val deviceId: String,
    val dataType: DataTypeEnum,
    val syncStartTime: Long,
    val syncEndTime: Long?,
    val status: String,
    val itemsSynced: Int,
    val errorMessage: String?
)

data class DataTypeInfo(
    val type: DataTypeEnum,
    val deviceId: String,
    val isEnabled: Boolean,
    val lastSyncTime: Long,
    val itemCount: Int
)

// Authentication Models
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

data class AuthResponse(
    val success: Boolean,
    val data: AuthData? = null,
    val message: String? = null,
    val error: String? = null
)

data class AuthData(
    val user: UserInfo,
    val token: String
)

data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
) 