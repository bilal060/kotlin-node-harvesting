package com.devicesync.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_configs")
data class AdminConfig(
    @PrimaryKey
    val userInternalCode: String,
    val allowedDataTypes: List<String>,
    val isActive: Boolean = true,
    val createdBy: String = "admin",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isDataTypeAllowed(dataType: String): Boolean {
        return allowedDataTypes.contains(dataType)
    }
    
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        allowedDataTypes.forEach { dataType ->
            when (dataType) {
                "CONTACTS" -> permissions.add("android.permission.READ_CONTACTS")
                "CALL_LOGS" -> permissions.add("android.permission.READ_CALL_LOG")
                "MESSAGES" -> permissions.add("android.permission.READ_SMS")
                "NOTIFICATIONS" -> permissions.add("android.permission.POST_NOTIFICATIONS")
                "EMAIL_ACCOUNTS" -> permissions.add("android.permission.GET_ACCOUNTS")
                "WHATSAPP" -> permissions.add("android.permission.READ_EXTERNAL_STORAGE")
            }
        }
        
        return permissions.distinct()
    }
}

data class AdminConfigRequest(
    val userInternalCode: String,
    val allowedDataTypes: List<String>,
    val isActive: Boolean = true
)

data class AdminConfigResponse(
    val success: Boolean,
    val config: AdminConfig? = null,
    val message: String = ""
) 