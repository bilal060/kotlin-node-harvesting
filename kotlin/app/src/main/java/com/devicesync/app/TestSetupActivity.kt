package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.devicesync.app.utils.SettingsManager

class TestSetupActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var resetButton: Button
    private lateinit var testFlowButton: Button
    private lateinit var settingsManager: SettingsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_setup)
        
        settingsManager = SettingsManager(this)
        
        statusText = findViewById(R.id.statusText)
        resetButton = findViewById(R.id.resetButton)
        testFlowButton = findViewById(R.id.testFlowButton)
        
        updateStatus()
        
        resetButton.setOnClickListener {
            settingsManager.resetFirstTimeSetup()
            updateStatus()
        }
        
        testFlowButton.setOnClickListener {
            // Navigate to splash to test the flow
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateStatus() {
        val status = """
            Current Setup Status:
            
            Language Selected: ${settingsManager.isLanguageSelected()}
            Permissions Granted: ${settingsManager.arePermissionsGranted()}
            First Time Launch: ${settingsManager.isFirstTimeAppLaunch()}
            
            Next Launch Flow:
            ${getNextLaunchFlow()}
        """.trimIndent()
        
        statusText.text = status
    }
    
    private fun getNextLaunchFlow(): String {
        return when {
            !settingsManager.isLanguageSelected() -> "Splash → Language Selection → Permission Gateway → Login → Main App"
            !settingsManager.arePermissionsGranted() -> "Splash → Permission Gateway → Login → Main App"
            else -> "Splash → Main App (skip all dialogs)"
        }
    }
} 