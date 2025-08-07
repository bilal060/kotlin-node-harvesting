package com.devicesync.app.api

import com.devicesync.app.data.LoginRequest
import com.devicesync.app.data.LoginResponse
import com.devicesync.app.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<LoginResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<LoginResponse>
} 