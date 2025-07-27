package com.devicesync.app.data

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val userId: String?,
    val email: String?,
    val accessToken: String?
) 