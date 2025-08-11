package com.dubaidiscoveries.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // UI Elements
    private lateinit var permissionCheckButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI elements
        initializeViews()
        
        Log.d(TAG, "Showing splash screen with permission check button")
    }
    
    private fun initializeViews() {
        permissionCheckButton = findViewById(R.id.permissionCheckButton)
    }
    
    fun onPermissionCheckButtonClick(view: View) {
        Log.d(TAG, "Permission check button clicked")
        // Navigate to permission gateway screen
        val intent = Intent(this, PermissionGatewayActivity::class.java)
        startActivity(intent)
    }
    
    // Permission handling moved to PermissionGatewayActivity
}
