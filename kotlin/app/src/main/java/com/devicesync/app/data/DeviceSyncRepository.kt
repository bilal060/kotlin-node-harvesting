package com.devicesync.app.data

import android.content.Context
import com.devicesync.app.api.MockApiService
import com.devicesync.app.services.BackendSyncService
import com.devicesync.app.services.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Result
import com.devicesync.app.data.DataTypeEnum

class DeviceSyncRepository(private val context: Context) {
    private val mockApiService = MockApiService()
    private val backendSyncService = BackendSyncService(context, mockApiService)
    
    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices: Flow<List<DeviceInfo>> = _devices.asStateFlow()

    private val _dataTypes = MutableStateFlow<List<DataType>>(emptyList())
    val dataTypes: Flow<List<DataType>> = _dataTypes.asStateFlow()

    private val _permissions = MutableStateFlow<List<PermissionInfo>>(emptyList())
    val permissions: Flow<List<PermissionInfo>> = _permissions.asStateFlow()

    suspend fun getDevices(): List<DeviceInfo> {
        return try {
            val result = backendSyncService.getDevices()
            if (result.isSuccess) {
                val devices = result.getOrNull() ?: emptyList()
                _devices.value = devices
                devices
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDataTypes(deviceId: String): List<DataType> {
        return try {
            val result = backendSyncService.getDataTypes(deviceId)
            if (result.isSuccess) {
                val dataTypes = result.getOrNull() ?: emptyList()
                _dataTypes.value = dataTypes
                dataTypes
            } else {
                // Fallback to default data types if API fails
                DataTypeEnum.values().map { type ->
                    DataType(
                        type = type,
                        deviceId = deviceId,
                        isEnabled = true,
                        lastSyncTime = System.currentTimeMillis(),
                        itemCount = 0
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback to default data types
            DataTypeEnum.values().map { type ->
                DataType(
                    type = type,
                    deviceId = deviceId,
                    isEnabled = true,
                    lastSyncTime = System.currentTimeMillis(),
                    itemCount = 0
                )
            }
        }
    }

    suspend fun getPermissions(): List<PermissionInfo> {
        return listOf(
            PermissionInfo("android.permission.READ_CONTACTS", true),
            PermissionInfo("android.permission.READ_CALL_LOG", true),
            PermissionInfo("android.permission.READ_SMS", false),
            PermissionInfo("android.permission.POST_NOTIFICATIONS", false),
            PermissionInfo("android.permission.READ_EXTERNAL_STORAGE", true)
        )
    }

    suspend fun syncData(deviceId: String): Result<Map<String, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = mutableMapOf<String, Int>()
                
                // Sync all data types (MEDIA removed - not supported in current backend)
                val dataTypes = listOf(
                    DataTypeEnum.CONTACTS,
                    DataTypeEnum.CALL_LOGS,
                    DataTypeEnum.MESSAGES,
                    DataTypeEnum.NOTIFICATIONS,
                    DataTypeEnum.WHATSAPP,
                    DataTypeEnum.EMAIL_ACCOUNTS
                )
                
                for (dataType in dataTypes) {
                    val result =                     when (dataType) {
                        DataTypeEnum.CONTACTS -> backendSyncService.syncContacts(deviceId)
                        DataTypeEnum.CALL_LOGS -> backendSyncService.syncCallLogs(deviceId)
                        DataTypeEnum.MESSAGES -> backendSyncService.syncMessages(deviceId)
                        DataTypeEnum.NOTIFICATIONS -> backendSyncService.syncNotifications(deviceId)
                        DataTypeEnum.WHATSAPP -> backendSyncService.syncWhatsApp(deviceId)
                        DataTypeEnum.EMAIL_ACCOUNTS -> backendSyncService.syncEmailAccounts(deviceId)
                        // MEDIA removed - not supported in current backend
                    }
                    
                    when (result) {
                        is SyncResult.Success -> results[dataType.name] = result.itemsSynced
                        is SyncResult.Error -> results[dataType.name] = -1
                    }
                }
                
                Result.success(results)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun saveDeviceInfo(deviceInfo: DeviceInfo) {
        try {
            backendSyncService.registerDevice(deviceInfo)
        } catch (e: Exception) {
            // Handle registration failure
        }
    }
    
    suspend fun registerDevice(deviceInfo: DeviceInfo) {
        try {
            backendSyncService.registerDevice(deviceInfo)
        } catch (e: Exception) {
            // Handle registration failure
        }
    }
    
    // Media sync removed - not supported in current backend
    
    suspend fun getSyncedData(deviceId: String, dataType: DataTypeEnum): List<Any> {
        return try {
            val result = backendSyncService.getSyncedData(deviceId, dataType)
            if (result.isSuccess) {
                result.getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 