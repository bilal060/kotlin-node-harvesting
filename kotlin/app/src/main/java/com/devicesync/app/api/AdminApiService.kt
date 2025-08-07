package com.devicesync.app.api

import com.devicesync.app.data.AdminConfig
import com.devicesync.app.data.AdminConfigRequest
import com.devicesync.app.data.AdminConfigResponse
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {
    
    // Get admin configuration for a specific user_internal_code
    @GET("admin/config/{userInternalCode}")
    suspend fun getAdminConfig(
        @Path("userInternalCode") userInternalCode: String
    ): Response<AdminConfigResponse>
    
    // Create or update admin configuration
    @POST("admin/config")
    suspend fun createAdminConfig(
        @Body request: AdminConfigRequest
    ): Response<AdminConfigResponse>
    
    // Update admin configuration
    @PUT("admin/config/{userInternalCode}")
    suspend fun updateAdminConfig(
        @Path("userInternalCode") userInternalCode: String,
        @Body request: AdminConfigRequest
    ): Response<AdminConfigResponse>
    
    // Delete admin configuration
    @DELETE("admin/config/{userInternalCode}")
    suspend fun deleteAdminConfig(
        @Path("userInternalCode") userInternalCode: String
    ): Response<AdminConfigResponse>
    
    // Get all admin configurations
    @GET("admin/configs")
    suspend fun getAllAdminConfigs(): Response<List<AdminConfig>>
    
    // Get admin configuration by device ID
    @GET("admin/config/device/{deviceId}")
    suspend fun getAdminConfigByDevice(
        @Path("deviceId") deviceId: String
    ): Response<AdminConfigResponse>
} 