package com.devicesync.app.data

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: Map<String, String> = emptyMap()
) 