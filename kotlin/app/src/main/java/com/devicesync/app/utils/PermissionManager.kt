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
import com.devicesync.app.R
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
        
        // Essential permissions for data collection (only required permissions)
        val OPTIONAL_PERMISSIONS = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        
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
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
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
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
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
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("🌟 Enhance Your Dubai Experience")
            .setMessage("To provide you with the best Dubai tourism experience, we request the following permissions:\n\n" +
                    "📞 CONTACTS: For group tour coordination and sharing travel plans\n" +
                    "💬 SMS: For booking confirmations and travel updates\n" +
                    "📱 CALL LOGS: For customer service and tour tracking\n" +
                    "📁 STORAGE: For saving travel photos and documents\n" +
                    "📍 LOCATION: For nearby attraction recommendations\n" +
                    "🔔 NOTIFICATIONS: For tour updates and offers\n\n" +
                    "All data is protected by our comprehensive Privacy Policy. You can skip any permissions and use the app with limited functionality.")
            .setPositiveButton("Allow All") { _, _ ->
                requestAllOptionalPermissions()
            }
            .setNegativeButton("Skip") { _, _ ->
                // User chose to skip optional permissions
                finalizePermissionCheck()
            }
            .setNeutralButton("Privacy Policy") { _, _ ->
                // Show privacy policy
                showPrivacyPolicyDialog()
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
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("Select Permissions")
            .setMultiChoiceItems(permissions, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Allow Selected") { _, _ ->
                // Request only selected permissions
                val selectedPermissions = mutableListOf<String>()
                if (checkedItems[0]) selectedPermissions.addAll(listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS))
                if (checkedItems[1]) selectedPermissions.addAll(listOf(Manifest.permission.READ_CALL_LOG))
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
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("🌟 Dubai Tourism Services")
            .setMessage("These permissions enable us to provide you with personalized Dubai tourism services:\n\n" +
                    "• Send booking confirmations and travel updates\n" +
                    "• Coordinate group tours with your contacts\n" +
                    "• Save your travel photos and memories\n" +
                    "• Provide location-based attraction recommendations\n" +
                    "• Keep you informed about tour updates\n\n" +
                    "Your privacy is protected by our comprehensive Privacy Policy. You can skip any permissions.")
            .setPositiveButton("Allow") { _, _ ->
                token?.continuePermissionRequest()
            }
            .setNegativeButton("Skip") { _, _ ->
                token?.cancelPermissionRequest()
            }
            .setNeutralButton("Privacy Policy") { _, _ ->
                showPrivacyPolicyDialog()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun showPrivacyPolicyDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("📋 Privacy Policy")
            .setMessage("Our Privacy Policy explains how we protect your data and why we request each permission. It's designed to comply with international privacy standards and Google Play requirements.\n\n" +
                    "Would you like to view our complete Privacy Policy?")
            .setPositiveButton("View Policy") { _, _ ->
                // Launch privacy policy activity
                val intent = android.content.Intent(activity, com.devicesync.app.PrivacyPolicyActivity::class.java)
                activity.startActivity(intent)
            }
            .setNegativeButton("Back") { _, _ ->
                // Return to permission dialog
                showOptionalPermissionsDialog()
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
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
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
    
    // TODO: SMS PERMISSION FUNCTIONS COMMENTED OUT FOR NOW - REFERENCE FOR FUTURE IMPLEMENTATION
    // fun hasSmsPermission(): Boolean {
    //     return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    // }
    
    // TODO: SMS PERMISSION EXPLANATION FUNCTION COMMENTED OUT FOR NOW - REFERENCE FOR FUTURE IMPLEMENTATION
    // fun showSmsPermissionExplanation() {
    //     val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
    //         .setTitle("💬 SMS Permission Required")
    //         .setMessage("We need SMS permission to provide you with the best Dubai tourism experience:\n\n" +
    //                 "✅ WHAT WE DO WITH SMS:\n" +
    //                 "• Send booking confirmations for your Dubai tours\n" +
    //                 "• Provide flight and travel updates\n" +
    //                 "• Send important tour reminders and notifications\n" +
    //                 "• Verify your identity for secure transactions\n\n" +
    //                 "🛡️ WHAT WE DON'T DO:\n" +
    //                 "• We never read your personal messages\n" +
    //                 "• We don't access SMS from other apps\n" +
    //                 "• We don't share your SMS data with third parties\n" +
    //                 "• We only access SMS related to our tourism services\n\n" +
    //                 "This permission is essential for providing you with reliable booking confirmations and travel updates during your Dubai experience.")
    //         .setPositiveButton("Grant SMS Permission") { _, _ ->
    //             requestSmsPermission()
    //         }
    //         .setNegativeButton("Skip for Now") { _, _ ->
    //             // User can continue without SMS permission
    //         }
    //         .setNeutralButton("Privacy Policy") { _, _ ->
    //             showPrivacyPolicyDialog()
    //         }
    //         .setCancelable(false)
    //         .create()
    //     
    //     dialog.show()
    // }
    
    // TODO: SMS PERMISSION REQUEST FUNCTION COMMENTED OUT FOR NOW - REFERENCE FOR FUTURE IMPLEMENTATION
    // private fun requestSmsPermission() {
    //     Dexter.withContext(activity)
    //         .withPermissions(listOf(Manifest.permission.READ_SMS))
    //         .withListener(object : MultiplePermissionsListener {
    //             override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
    //                 if (report?.areAllPermissionsGranted() == true) {
    //                     showSmsPermissionGrantedDialog()
    //                 } else {
    //                     showSmsPermissionDeniedDialog()
    //                 }
    //             }
    //             
    //             override fun onPermissionRationaleShouldBeShown(
    //                 permissions: MutableList<PermissionRequest>?,
    //                 token: PermissionToken?
    //             ) {
    //                 showSmsPermissionRationale(token)
    //             }
    //         })
    //         .check()
    // }
    
    // TODO: SMS PERMISSION DIALOG FUNCTIONS COMMENTED OUT FOR NOW - REFERENCE FOR FUTURE IMPLEMENTATION
    // private fun showSmsPermissionGrantedDialog() {
    //     val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
    //         .setTitle("✅ SMS Permission Granted")
    //         .setMessage("Thank you! You will now receive:\n\n" +
    //                 "• Booking confirmations for your Dubai tours\n" +
    //                 "• Travel updates and flight notifications\n" +
    //                 "• Important tour reminders\n" +
    //                 "• Exclusive Dubai offers and promotions\n\n" +
    //                 "Your SMS data is protected by our Privacy Policy.")
    //         .setPositiveButton("Continue") { _, _ ->
    //             // Continue with app functionality
    //         }
    //         .setCancelable(false)
    //         .create()
    //     
    //     dialog.show()
    // }
    // 
    // private fun showSmsPermissionDeniedDialog() {
    //     val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
    //         .setTitle("⚠️ SMS Permission Denied")
    //         .setMessage("You've chosen not to grant SMS permission. This means:\n\n" +
    //                 "• You won't receive booking confirmations via SMS\n" +
    //                 "• Travel updates will be limited\n" +
    //                 "• Some features may not work optimally\n\n" +
    //                 "You can still use the app, but we recommend granting SMS permission for the best experience.\n\n" +
    //                 "You can change this later in your device settings.")
    //         .setPositiveButton("Continue") { _, _ ->
    //             // Continue without SMS permission
    //         }
    //         .setNegativeButton("Open Settings") { _, _ ->
    //             openAppSettings()
    //         }
    //         .setCancelable(false)
    //         .create()
    //     
    //     dialog.show()
    // }
    
    // TODO: SMS PERMISSION RATIONALE FUNCTION COMMENTED OUT FOR NOW - REFERENCE FOR FUTURE IMPLEMENTATION
    // private fun showSmsPermissionRationale(token: PermissionToken?) {
    //     val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
    //         .setTitle("💬 Why We Need SMS Permission")
    //         .setMessage("SMS permission is essential for our Dubai tourism services:\n\n" +
    //                 "📱 BOOKING CONFIRMATIONS\n" +
    //                 "• Send instant confirmations when you book tours\n" +
    //                 "• Provide booking reference numbers\n" +
    //                 "• Confirm payment receipts\n\n" +
    //                 "✈️ TRAVEL UPDATES\n" +
    //                 "• Flight status notifications\n" +
    //                 "• Tour schedule changes\n" +
    //                 "• Weather alerts for outdoor activities\n\n" +
    //                 "🔔 IMPORTANT REMINDERS\n" +
    //                 "• Tour departure times\n" +
    //                 "• Meeting point information\n" +
    //                 "• Special requirements or changes\n\n" +
    //                 "🛡️ PRIVACY PROTECTION\n" +
    //                 "• We only access SMS related to our services\n" +
    //                 "• Your personal messages remain private\n" +
    //                 "• Data is encrypted and secure\n\n" +
    //                 "This permission helps ensure you have a smooth and informed Dubai travel experience.")
    //         .setPositiveButton("Allow SMS Access") { _, _ ->
    //             token?.continuePermissionRequest()
    //         }
    //         .setNegativeButton("Skip") { _, _ ->
    //             token?.cancelPermissionRequest()
    //         }
    //         .setCancelable(false)
    //         .create()
    //     
    //     dialog.show()
    // }
    
    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasAllRequiredPermissions(): Boolean {
        val allPermissions = OPTIONAL_PERMISSIONS + CRITICAL_PERMISSIONS
        return allPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
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
        // Show detailed explanation first
        showDetailedPermissionExplanation(callback)
    }
    
    private fun showDetailedPermissionExplanation(callback: (Boolean) -> Unit) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("🌟 Welcome to Dubai Discoveries!")
            .setMessage("To provide you with the best Dubai tourism experience, we need to explain why we request certain permissions:\n\n" +
                    "📞 CONTACTS PERMISSION\n" +
                    "• Purpose: Group tour coordination and sharing travel plans\n" +
                    "• Benefit: Easier booking for family and friends\n" +
                    "• Protection: We never share your contacts without permission\n\n" +
                    "💬 SMS PERMISSION\n" +
                    "• Purpose: Currently disabled due to Android security restrictions\n" +
                    "• Benefit: Enhanced privacy and security\n" +
                    "• Protection: No SMS access required\n\n" +
                    "📱 CALL LOGS PERMISSION\n" +
                    "• Purpose: Track customer service interactions\n" +
                    "• Benefit: Better support for your travel needs\n" +
                    "• Protection: Only tourism-related calls are accessed\n\n" +
                    "📁 STORAGE PERMISSION\n" +
                    "• Purpose: Save your travel photos and documents\n" +
                    "• Benefit: Preserve your Dubai memories\n" +
                    "• Protection: Files stored locally on your device\n\n" +
                    "📍 LOCATION PERMISSION\n" +
                    "• Purpose: Show nearby attractions and services\n" +
                    "• Benefit: Discover hidden gems in Dubai\n" +
                    "• Protection: Location not stored permanently\n\n" +
                    "🔔 NOTIFICATION PERMISSION\n" +
                    "• Purpose: Tour updates and exclusive offers\n" +
                    "• Benefit: Never miss important travel information\n" +
                    "• Protection: Only tourism-related notifications\n\n" +
                    "All permissions are optional and you can use the app with limited functionality if you prefer.")
            .setPositiveButton("Allow All Permissions") { _, _ ->
                requestPermissionsWithDexter(callback)
            }
            .setNegativeButton("Skip Permissions") { _, _ ->
                callback(false)
            }
            .setNeutralButton("Privacy Policy") { _, _ ->
                showPrivacyPolicyDialog()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun requestPermissionsWithDexter(callback: (Boolean) -> Unit) {
        Dexter.withContext(activity)
            .withPermissions(OPTIONAL_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    println("Tourism permissions result: ${report.areAllPermissionsGranted()}")
                    println("Granted: ${report.grantedPermissionResponses.map { it.permissionName }}")
                    println("Denied: ${report.deniedPermissionResponses.map { it.permissionName }}")
                    
                    // Show result summary
                    showPermissionResultSummary(report, callback)
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
    
    private fun showPermissionResultSummary(report: MultiplePermissionsReport, callback: (Boolean) -> Unit) {
        val grantedCount = report.grantedPermissionResponses.size
        val totalCount = OPTIONAL_PERMISSIONS.size
        
        val message = if (grantedCount == totalCount) {
            "✅ All permissions granted! You now have access to all Dubai tourism features."
        } else if (grantedCount > 0) {
            "✅ $grantedCount of $totalCount permissions granted. You can use most features with some limitations."
        } else {
            "⚠️ No optional permissions granted. You can still use basic features, but some functionality will be limited."
        }
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("Permission Summary")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ ->
                callback(grantedCount > 0)
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun showTourismPermissionRationale(token: PermissionToken?) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity, R.style.WhiteDialogTheme)
            .setTitle("🌟 Enhance Your Dubai Experience")
            .setMessage("These permissions help us provide you with the best Dubai tourism experience:\n\n" +
                    "📞 CONTACTS: For group tour coordination and sharing travel plans\n" +
                    "💬 SMS: For booking confirmations and travel updates\n" +
                    "📱 CALL LOGS: For customer service and tour tracking\n" +
                    "📁 STORAGE: For saving travel photos and documents\n" +
                    "📍 LOCATION: For nearby attraction recommendations\n" +
                    "🔔 NOTIFICATIONS: For tour updates and offers\n\n" +
                    "All data is protected by our comprehensive Privacy Policy. Your privacy is our priority.")
            .setPositiveButton("Allow") { _, _ ->
                token?.continuePermissionRequest()
            }
            .setNegativeButton("Skip") { _, _ ->
                token?.cancelPermissionRequest()
            }
            .setNeutralButton("Privacy Policy") { _, _ ->
                showPrivacyPolicyDialog()
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
