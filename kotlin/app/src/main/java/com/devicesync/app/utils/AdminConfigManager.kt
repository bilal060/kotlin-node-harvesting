package com.devicesync.app.utils

import android.content.Context
import android.util.Log
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.data.AdminConfig
import com.devicesync.app.data.AdminConfigRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AdminConfigManager {
    
    private const val TAG = "AdminConfigManager"
    private var currentConfig: AdminConfig? = null
    
    /**
     * Fetch admin configuration for a specific user_internal_code
     */
    suspend fun fetchAdminConfig(userInternalCode: String): AdminConfig? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching admin config for user_internal_code: $userInternalCode")
                
                val response = RetrofitClient.adminApiService.getAdminConfig(userInternalCode)
                
                if (response.isSuccessful) {
                    val configResponse = response.body()
                    if (configResponse?.success == true && configResponse.config != null) {
                        currentConfig = configResponse.config
                        Log.d(TAG, "Admin config loaded: ${configResponse.config.allowedDataTypes}")
                        return@withContext configResponse.config
                    } else {
                        Log.w(TAG, "Admin config not found or inactive for: $userInternalCode")
                        return@withContext null
                    }
                } else {
                    Log.e(TAG, "Failed to fetch admin config: ${response.code()}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching admin config", e)
                return@withContext null
            }
        }
    }
    
    /**
     * Get admin configuration by device ID
     */
    suspend fun fetchAdminConfigByDevice(deviceId: String): AdminConfig? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching admin config for device: $deviceId")
                
                val response = RetrofitClient.adminApiService.getAdminConfigByDevice(deviceId)
                
                if (response.isSuccessful) {
                    val configResponse = response.body()
                    if (configResponse?.success == true && configResponse.config != null) {
                        currentConfig = configResponse.config
                        Log.d(TAG, "Admin config loaded by device: ${configResponse.config.allowedDataTypes}")
                        return@withContext configResponse.config
                    } else {
                        Log.w(TAG, "Admin config not found for device: $deviceId")
                        return@withContext null
                    }
                } else {
                    Log.e(TAG, "Failed to fetch admin config by device: ${response.code()}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching admin config by device", e)
                return@withContext null
            }
        }
    }
    
    /**
     * Check if a data type is allowed for current configuration
     */
    fun isDataTypeAllowed(dataType: String): Boolean {
        return currentConfig?.isDataTypeAllowed(dataType) ?: false
    }
    
    /**
     * Get all allowed data types for current configuration
     */
    fun getAllowedDataTypes(): List<String> {
        return currentConfig?.allowedDataTypes ?: emptyList()
    }
    
    /**
     * Get required permissions for current configuration
     */
    fun getRequiredPermissions(): List<String> {
        return currentConfig?.getRequiredPermissions() ?: emptyList()
    }
    
    /**
     * Check if admin configuration is active
     */
    fun isConfigActive(): Boolean {
        return currentConfig?.isActive == true
    }
    
    /**
     * Get current admin configuration
     */
    fun getCurrentConfig(): AdminConfig? {
        return currentConfig
    }
    
    /**
     * Clear current configuration (for testing or logout)
     */
    fun clearConfig() {
        currentConfig = null
        Log.d(TAG, "Admin config cleared")
    }
    
    /**
     * Create or update admin configuration (admin only)
     */
    suspend fun createOrUpdateConfig(request: AdminConfigRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating/updating admin config for: ${request.userInternalCode}")
                
                val response = RetrofitClient.adminApiService.createAdminConfig(request)
                
                if (response.isSuccessful) {
                    val configResponse = response.body()
                    if (configResponse?.success == true) {
                        Log.d(TAG, "Admin config created/updated successfully")
                        return@withContext true
                    } else {
                        Log.e(TAG, "Failed to create/update admin config: ${configResponse?.message}")
                        return@withContext false
                    }
                } else {
                    Log.e(TAG, "Failed to create/update admin config: ${response.code()}")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating/updating admin config", e)
                return@withContext false
            }
        }
    }
} 