package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Simulate loading time
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to language selection first
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3 seconds delay
    }
} 