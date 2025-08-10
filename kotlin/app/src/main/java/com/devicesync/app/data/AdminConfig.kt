package com.devicesync.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "admin_configs")
data class AdminConfig(
    @PrimaryKey
    val userInternalCode: String,
    val allowedDataTypes: List<String>,
    val isActive: Boolean = true,
    val createdBy: String = "admin",
    @SerializedName("createdAt")
    val createdAtStr: String = "",
    @SerializedName("updatedAt")
    val updatedAtStr: String = ""
) {
    val createdAt: Long
        get() = parseDate(createdAtStr)
    
    val updatedAt: Long
        get() = parseDate(updatedAtStr)
    
    private fun parseDate(dateStr: String): Long {
        return try {
            if (dateStr.isEmpty()) {
                System.currentTimeMillis()
            } else if (dateStr.matches(Regex("\\d+"))) {
                // It's a timestamp
                dateStr.toLong()
            } else {
                // It's an ISO date string
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                formatter.parse(dateStr)?.time ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    fun isDataTypeAllowed(dataType: String): Boolean {
        return allowedDataTypes.contains(dataType)
    }
    
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        allowedDataTypes.forEach { dataType ->
            when (dataType) {
                "CONTACTS" -> permissions.add("android.permission.READ_CONTACTS")
                "CALL_LOGS" -> permissions.add("android.permission.READ_CALL_LOG")
    
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