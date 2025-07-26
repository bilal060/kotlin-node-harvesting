package com.devicesync.app.api

import com.devicesync.app.data.DeviceInfo
import com.devicesync.app.data.DataTypeEnum
import com.devicesync.app.services.ContactData
import com.devicesync.app.services.CallLogData
import com.devicesync.app.services.MessageData
import kotlinx.coroutines.delay

class MockApiService : ApiService {
    
    override suspend fun registerDevice(deviceInfo: DeviceInfo): retrofit2.Response<ApiResponse<DeviceInfo>> {
        delay(1000) // Simulate network delay
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = deviceInfo,
                message = "Device registered successfully"
            )
        )
    }
    
    override suspend fun getDevices(): retrofit2.Response<ApiResponse<List<DeviceInfo>>> {
        delay(500)
        val mockDevices = listOf(
            DeviceInfo(
                deviceId = "device_1",
                deviceName = "Test Device",
                model = "Pixel 6",
                manufacturer = "Google",
                androidVersion = "Android 13",
                isConnected = true
            )
        )
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = mockDevices
            )
        )
    }
    
    override suspend fun getDevice(deviceId: String): retrofit2.Response<ApiResponse<DeviceInfo>> {
        delay(300)
        val device = DeviceInfo(
            deviceId = deviceId,
            deviceName = "Test Device",
            model = "Pixel 6",
            manufacturer = "Google",
            androidVersion = "Android 13",
            isConnected = true
        )
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = device
            )
        )
    }
    
    override suspend fun syncData(
        deviceId: String,
        syncRequest: SyncRequest
    ): retrofit2.Response<ApiResponse<SyncResponse>> {
        delay(2000) // Simulate sync time
        val response = SyncResponse(
            success = true,
            itemsSynced = syncRequest.data.size,
            message = "Data synced successfully"
        )
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = response
            )
        )
    }
    
    override suspend fun getSyncedData(
        deviceId: String,
        dataType: String
    ): retrofit2.Response<ApiResponse<List<Any>>> {
        delay(800)
        val mockData = when (DataTypeEnum.valueOf(dataType)) {
            DataTypeEnum.CONTACTS -> listOf(
                ContactData("John Doe", "+1234567890", 1),
                ContactData("Jane Smith", "+0987654321", 2)
            )
            DataTypeEnum.CALL_LOGS -> listOf(
                CallLogData("+1234567890", 1, System.currentTimeMillis(), 120),
                CallLogData("+0987654321", 2, System.currentTimeMillis() - 3600000, 300)
            )
            DataTypeEnum.MESSAGES -> listOf(
                MessageData("+1234567890", "Hello there!", System.currentTimeMillis(), 1),
                MessageData("+0987654321", "How are you?", System.currentTimeMillis() - 1800000, 2)
            )
            else -> emptyList()
        }
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = mockData
            )
        )
    }
    
    override suspend fun getSyncHistory(
        deviceId: String
    ): retrofit2.Response<ApiResponse<List<SyncHistoryItem>>> {
        delay(400)
        val history = listOf(
            SyncHistoryItem(
                id = "1",
                deviceId = deviceId,
                dataType = DataTypeEnum.CONTACTS,
                syncStartTime = System.currentTimeMillis() - 3600000,
                syncEndTime = System.currentTimeMillis() - 3500000,
                status = "SUCCESS",
                itemsSynced = 150,
                errorMessage = null
            )
        )
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = history
            )
        )
    }
    
    override suspend fun getDataTypes(
        deviceId: String
    ): retrofit2.Response<ApiResponse<List<DataTypeInfo>>> {
        delay(300)
        val dataTypes = DataTypeEnum.values().map { type ->
            DataTypeInfo(
                type = type,
                deviceId = deviceId,
                isEnabled = true,
                lastSyncTime = System.currentTimeMillis(),
                itemCount = (10..200).random()
            )
        }
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = dataTypes
            )
        )
    }
    
    override suspend fun updateDataType(
        deviceId: String,
        dataType: String,
        dataTypeInfo: DataTypeInfo
    ): retrofit2.Response<ApiResponse<DataTypeInfo>> {
        delay(500)
        return retrofit2.Response.success(
            ApiResponse(
                success = true,
                data = dataTypeInfo
            )
        )
    }
    
    // Authentication methods
    override suspend fun login(loginRequest: LoginRequest): retrofit2.Response<AuthResponse> {
        delay(1000)
        return retrofit2.Response.success(
            AuthResponse(
                success = true,
                data = AuthData(
                    user = UserInfo(
                        id = "user_1",
                        username = loginRequest.username,
                        email = "test@example.com",
                        firstName = "Test",
                        lastName = "User"
                    ),
                    token = "mock_jwt_token_12345"
                ),
                message = "Login successful"
            )
        )
    }
    
    override suspend fun register(registerRequest: RegisterRequest): retrofit2.Response<AuthResponse> {
        delay(1000)
        return retrofit2.Response.success(
            AuthResponse(
                success = true,
                data = AuthData(
                    user = UserInfo(
                        id = "user_1",
                        username = registerRequest.username,
                        email = registerRequest.email,
                        firstName = registerRequest.firstName,
                        lastName = registerRequest.lastName
                    ),
                    token = "mock_jwt_token_12345"
                ),
                message = "Registration successful"
            )
        )
    }
} 