package com.devicesync.app.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val isConnected: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.UNKNOWN,
    val lastSeen: Long = System.currentTimeMillis()
) : Parcelable

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}

enum class ConnectionType {
    BLUETOOTH,
    WIFI_DIRECT,
    USB,
    NETWORK,
    UNKNOWN
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    COMPLETED,
    FAILED
}

@Parcelize
data class DataType(
    val type: DataTypeEnum,
    val deviceId: String,
    val isEnabled: Boolean = true,
    val lastSyncTime: Long = 0L,
    val itemCount: Int = 0
) : Parcelable

enum class DataTypeEnum {
    CONTACTS,
    CALL_LOGS,
    MESSAGES,
    WHATSAPP,
    NOTIFICATIONS,
    EMAIL_ACCOUNTS
}

@Parcelize
data class PermissionInfo(
    val permission: String,
    val isGranted: Boolean,
    val isRequired: Boolean = true
) : Parcelable

@Parcelize
data class DiscoveryResult(
    val devices: List<DeviceInfo>,
    val isScanning: Boolean = false,
    val error: String? = null
) : Parcelable 

data class EmailAccountData(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val emailAddress: String,
    val isActive: Boolean
)

 