package com.devicesync.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.utils.SettingsManager
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val apiService = RetrofitClient.apiService
    private lateinit var settingsManager: SettingsManager
    
    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult
    
    fun setSettingsManager(settingsManager: SettingsManager) {
        this.settingsManager = settingsManager
    }
    
    fun login(username: String, password: String) {
        _authResult.value = AuthResult.Loading(true)
        
        viewModelScope.launch {
            try {
                val loginRequest = com.devicesync.app.api.LoginRequest(username, password)
                val response = apiService.login(loginRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val token = data?.token
                    val user = data?.user
                    
                    if (token != null && user != null) {
                        // Save token and user info
                        settingsManager.saveAuthToken(token)
                        settingsManager.saveUserInfo(user)
                        
                        _authResult.value = AuthResult.Success(UserInfo(
                            user.id,
                            user.username,
                            user.email,
                            user.firstName,
                            user.lastName
                        ))
                    } else {
                        _authResult.value = AuthResult.Error("Invalid response from server")
                    }
                } else {
                    val errorMessage = response.body()?.error ?: "Login failed"
                    _authResult.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error("Network error: ${e.message}")
            } finally {
                _authResult.value = AuthResult.Loading(false)
            }
        }
    }
    
    fun signup(username: String, email: String, password: String, firstName: String, lastName: String) {
        _authResult.value = AuthResult.Loading(true)
        
        viewModelScope.launch {
            try {
                val registerRequest = com.devicesync.app.api.RegisterRequest(username, email, password, firstName, lastName)
                val response = apiService.register(registerRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val token = data?.token
                    val user = data?.user
                    
                    if (token != null && user != null) {
                        // Save token and user info
                        settingsManager.saveAuthToken(token)
                        settingsManager.saveUserInfo(user)
                        
                        _authResult.value = AuthResult.Success(UserInfo(
                            user.id,
                            user.username,
                            user.email,
                            user.firstName,
                            user.lastName
                        ))
                    } else {
                        _authResult.value = AuthResult.Error("Invalid response from server")
                    }
                } else {
                    val errorMessage = response.body()?.error ?: "Registration failed"
                    _authResult.value = AuthResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error("Network error: ${e.message}")
            } finally {
                _authResult.value = AuthResult.Loading(false)
            }
        }
    }
    
    sealed class AuthResult {
        data class Success(val user: UserInfo) : AuthResult()
        data class Error(val message: String) : AuthResult()
        data class Loading(val isLoading: Boolean) : AuthResult()
    }
    
    data class UserInfo(
        val id: String,
        val username: String,
        val email: String,
        val firstName: String,
        val lastName: String
    )
} 