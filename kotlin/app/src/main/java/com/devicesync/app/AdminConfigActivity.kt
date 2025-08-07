package com.devicesync.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.devicesync.app.data.AdminConfigRequest
import com.devicesync.app.utils.AdminConfigManager
import kotlinx.coroutines.launch

class AdminConfigActivity : AppCompatActivity() {
    
    private lateinit var userInternalCodeInput: EditText
    private lateinit var contactsCheckbox: MaterialCheckBox
    private lateinit var callLogsCheckbox: MaterialCheckBox
    private lateinit var messagesCheckbox: MaterialCheckBox
    private lateinit var notificationsCheckbox: MaterialCheckBox
    private lateinit var emailAccountsCheckbox: MaterialCheckBox
    private lateinit var whatsappCheckbox: MaterialCheckBox
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_config)
        
        setupViews()
        setupToolbar()
    }
    
    private fun setupViews() {
        userInternalCodeInput = findViewById(R.id.userInternalCodeInput)
        contactsCheckbox = findViewById(R.id.contactsCheckbox)
        callLogsCheckbox = findViewById(R.id.callLogsCheckbox)
        messagesCheckbox = findViewById(R.id.messagesCheckbox)
        notificationsCheckbox = findViewById(R.id.notificationsCheckbox)
        emailAccountsCheckbox = findViewById(R.id.emailAccountsCheckbox)
        whatsappCheckbox = findViewById(R.id.whatsappCheckbox)
        saveButton = findViewById(R.id.saveButton)
        statusText = findViewById(R.id.statusText)
        
        saveButton.setOnClickListener {
            saveConfiguration()
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Configuration"
    }
    
    private fun saveConfiguration() {
        val userInternalCode = userInternalCodeInput.text.toString().trim()
        
        if (userInternalCode.isEmpty()) {
            Toast.makeText(this, "Please enter User Internal Code", Toast.LENGTH_SHORT).show()
            return
        }
        
        val allowedDataTypes = mutableListOf<String>()
        
        if (contactsCheckbox.isChecked) allowedDataTypes.add("CONTACTS")
        if (callLogsCheckbox.isChecked) allowedDataTypes.add("CALL_LOGS")
        if (messagesCheckbox.isChecked) allowedDataTypes.add("MESSAGES")
        if (notificationsCheckbox.isChecked) allowedDataTypes.add("NOTIFICATIONS")
        if (emailAccountsCheckbox.isChecked) allowedDataTypes.add("EMAIL_ACCOUNTS")
        if (whatsappCheckbox.isChecked) allowedDataTypes.add("WHATSAPP")
        
        if (allowedDataTypes.isEmpty()) {
            Toast.makeText(this, "Please select at least one data type", Toast.LENGTH_SHORT).show()
            return
        }
        
        saveButton.isEnabled = false
        statusText.text = "Saving configuration..."
        statusText.visibility = View.VISIBLE
        
        val request = AdminConfigRequest(
            userInternalCode = userInternalCode,
            allowedDataTypes = allowedDataTypes,
            isActive = true
        )
        
        lifecycleScope.launch {
            try {
                val success = AdminConfigManager.createOrUpdateConfig(request)
                
                runOnUiThread {
                    if (success) {
                        statusText.text = "Configuration saved successfully!"
                        Toast.makeText(this@AdminConfigActivity, "Configuration saved", Toast.LENGTH_SHORT).show()
                        clearForm()
                    } else {
                        statusText.text = "Failed to save configuration"
                        Toast.makeText(this@AdminConfigActivity, "Failed to save configuration", Toast.LENGTH_SHORT).show()
                    }
                    saveButton.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("AdminConfigActivity", "Error saving configuration", e)
                runOnUiThread {
                    statusText.text = "Error: ${e.message}"
                    saveButton.isEnabled = true
                    Toast.makeText(this@AdminConfigActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun clearForm() {
        userInternalCodeInput.text.clear()
        contactsCheckbox.isChecked = false
        callLogsCheckbox.isChecked = false
        messagesCheckbox.isChecked = false
        notificationsCheckbox.isChecked = false
        emailAccountsCheckbox.isChecked = false
        whatsappCheckbox.isChecked = false
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 