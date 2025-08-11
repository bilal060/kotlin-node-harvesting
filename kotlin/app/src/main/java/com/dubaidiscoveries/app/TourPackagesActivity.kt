package com.dubaidiscoveries.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TourPackagesActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this)
        textView.text = "🗺️ Tour Packages\n\nComing Soon!"
        textView.textSize = 24f
        textView.gravity = android.view.Gravity.CENTER
        textView.setPadding(32, 32, 32, 32)
        
        setContentView(textView)
    }
}
