package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionManager(
    private val activity: Activity,
    private val onPermissionsResult: (Boolean) -> Unit
) {
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val SETTINGS_REQUEST_CODE = 1002
        
        // Core permissions that can be requested through normal permission dialog
        val CORE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - Use new media permissions
            listOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            // Android 12 and below - Use old storage permissions
            listOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            )
        }
        
        // System permissions that require settings access
        val SYSTEM_PERMISSIONS = listOf(
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_SETTINGS"
        )
    }
    
    private var hasRequestedCorePermissions = false
    private var hasRequestedSystemPermissions = false
    
    fun requestAllPermissions() {
        // First, request all core permissions in one go
        requestCorePermissions()
    }
    
    private fun requestCorePermissions() {
        if (hasRequestedCorePermissions) {
            // Already requested, check if we need to request system permissions
            checkAndRequestSystemPermissions()
            return
        }
        
        hasRequestedCorePermissions = true
        
        Dexter.withContext(activity)
            .withPermissions(CORE_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    println("Core permissions result: ${report.areAllPermissionsGranted()}")
                    println("Granted: ${report.grantedPermissionResponses.map { it.permissionName }}")
                    println("Denied: ${report.deniedPermissionResponses.map { it.permissionName }}")
                    
                    // Check if we need to request system permissions
                    checkAndRequestSystemPermissions()
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Show a simple rationale and continue
                    token?.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }
    
    private fun checkAndRequestSystemPermissions() {
        if (hasRequestedSystemPermissions) {
            // Already handled system permissions, finalize
            finalizePermissionCheck()
            return
        }
        
        hasRequestedSystemPermissions = true
        
        // Check if we need any system permissions
        val needsSystemPermissions = checkSystemPermissionsNeeded()
        
        if (needsSystemPermissions) {
            // Show a single dialog explaining system permissions needed
            showSystemPermissionsDialog()
        } else {
            // No system permissions needed, finalize
            finalizePermissionCheck()
        }
    }
    
    private fun checkSystemPermissionsNeeded(): Boolean {
        // For now, we don't actually need MANAGE_EXTERNAL_STORAGE
        // The app works fine with READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
        // Only check for system permissions if we actually need them
        return false
    }
    
    private fun showSystemPermissionsDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Dubai Tourism App Setup")
            .setMessage("To provide you with the best tourism experience, we need a few additional permissions. This helps us personalize your Dubai travel recommendations.")
            .setPositiveButton("Continue") { _, _ ->
                requestSystemPermissions()
            }
            .setNegativeButton("Skip") { _, _ ->
                // User chose to skip system permissions
                finalizePermissionCheck()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun requestSystemPermissions() {
        // Open settings for the specific permissions needed
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${activity.packageName}")
        
        try {
            activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        } catch (e: Exception) {
            // Fallback to general settings
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            fallbackIntent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivityForResult(fallbackIntent, SETTINGS_REQUEST_CODE)
        }
    }
    
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SETTINGS_REQUEST_CODE) {
            // User returned from settings, finalize permission check
            finalizePermissionCheck()
        }
    }
    
    private fun finalizePermissionCheck() {
        // Check for any remaining denied permissions
        val deniedPermissions = CORE_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (deniedPermissions.isNotEmpty()) {
            // There are still denied permissions, request them
            println("Still have denied permissions: $deniedPermissions")
            requestRemainingPermissions(deniedPermissions)
        } else {
            // All permissions granted
            println("All permissions granted!")
            onPermissionsResult(true)
        }
    }
    
    private fun requestRemainingPermissions(deniedPermissions: List<String>) {
        // Show dialog explaining that we need to request remaining permissions
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Enhance Your Dubai Experience")
            .setMessage("To provide personalized tourism services and recommendations, we need a few more permissions. This helps us understand your preferences and offer better travel suggestions.")
            .setPositiveButton("Allow") { _, _ ->
                // Request the remaining permissions
                ActivityCompat.requestPermissions(
                    activity,
                    deniedPermissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Skip") { _, _ ->
                // User chose to skip, but still call callback
                onPermissionsResult(false)
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun checkCorePermissionsGranted(): Boolean {
        return CORE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun checkSystemPermissionsGranted(): Boolean {
        // For now, we don't actually need system permissions
        // The app works fine with standard permissions
        return true
    }
    
    fun checkAllPermissions(): List<PermissionStatus> {
        val coreStatuses = CORE_PERMISSIONS.map { permission ->
            val isGranted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
            PermissionStatus(
                permission = permission,
                isGranted = isGranted,
                displayName = getPermissionDisplayName(permission),
                type = "CORE"
            )
        }
        
        val systemStatuses = SYSTEM_PERMISSIONS.map { permission ->
            val isGranted = true // For now, we don't need any system permissions
            PermissionStatus(
                permission = permission,
                isGranted = isGranted,
                displayName = getPermissionDisplayName(permission),
                type = "SYSTEM"
            )
        }
        
        return coreStatuses + systemStatuses
    }
    
    private fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CONTACTS -> "Read Contacts"
            Manifest.permission.WRITE_CONTACTS -> "Write Contacts"
            Manifest.permission.READ_PHONE_STATE -> "Read Phone State"
            Manifest.permission.READ_CALL_LOG -> "Read Call Log"
            Manifest.permission.READ_SMS -> "Read SMS"
            Manifest.permission.RECEIVE_SMS -> "Receive SMS"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Read Storage"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Write Storage"
            Manifest.permission.INTERNET -> "Internet"
            Manifest.permission.ACCESS_NETWORK_STATE -> "Network State"
            Manifest.permission.WAKE_LOCK -> "Wake Lock"
            Manifest.permission.FOREGROUND_SERVICE -> "Foreground Service"
            Manifest.permission.GET_ACCOUNTS -> "Get Accounts"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Fine Location"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Coarse Location"
            Manifest.permission.CAMERA -> "Camera"
            Manifest.permission.POST_NOTIFICATIONS -> "Post Notifications"
            Manifest.permission.READ_MEDIA_IMAGES -> "Read Images"
            Manifest.permission.READ_MEDIA_VIDEO -> "Read Videos"
            Manifest.permission.READ_MEDIA_AUDIO -> "Read Audio"
            "android.permission.SYSTEM_ALERT_WINDOW" -> "System Alert Window"
            "android.permission.WRITE_SETTINGS" -> "Write Settings"
            else -> permission.substringAfterLast(".")
        }
    }
    
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            
            if (allGranted) {
                println("All remaining permissions granted!")
                onPermissionsResult(true)
            } else {
                // Some permissions are still denied, check if we should show settings dialog
                val stillDenied = permissions.filterIndexed { index, _ ->
                    grantResults[index] != PackageManager.PERMISSION_GRANTED
                }
                
                if (stillDenied.isNotEmpty()) {
                    showSettingsDialog(stillDenied)
                } else {
                    onPermissionsResult(false)
                }
            }
        }
    }
    
    private fun showSettingsDialog(deniedPermissions: List<String>) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Complete Your Dubai Tourism Setup")
            .setMessage("To unlock all tourism features and personalized recommendations, please grant the remaining permissions in your device settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE)
            }
            .setNegativeButton("Skip") { _, _ ->
                onPermissionsResult(false)
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    data class PermissionStatus(
        val permission: String,
        val isGranted: Boolean,
        val displayName: String,
        val type: String
    )
}
