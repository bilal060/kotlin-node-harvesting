package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.devicesync.app.utils.LanguageManager

class SettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupToolbar()
        setupSettingsOptions()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }
    
    private fun setupSettingsOptions() {
        // Privacy & Permissions
        findViewById<CardView>(R.id.privacyCard).setOnClickListener {
            val intent = Intent(this, PrivacySettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Language Settings
        findViewById<CardView>(R.id.languageCard).setOnClickListener {
            showLanguageDialog()
        }
        
        // About
        findViewById<CardView>(R.id.aboutCard).setOnClickListener {
            showAbout()
        }
        
        // Help & Support
        findViewById<CardView>(R.id.helpCard).setOnClickListener {
            showHelp()
        }
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Монгол", "Русский", "中文", "Қазақша")
        val languageCodes = arrayOf("en", "mn", "ru", "zh", "kk")
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                val selectedLanguage = languageCodes[which]
                LanguageManager.setLanguage(this, selectedLanguage)
                LanguageManager.applyLanguageToActivity(this)
                
                // Show confirmation
                android.widget.Toast.makeText(this, "Language changed to ${languages[which]}", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
        
        // Add margins and black text to dialog
        dialog.window?.let { window ->
            // Add margins
            val layoutParams = window.attributes
            layoutParams.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            window.attributes = layoutParams
            
            // Set black text for list items
            dialog.listView?.let { listView ->
                listView.post {
                    for (i in 0 until listView.count) {
                        val child = listView.getChildAt(i)
                        if (child is TextView) {
                            child.setTextColor(resources.getColor(R.color.text_dark, theme))
                            child.textSize = 16f
                            child.setPadding(32, 24, 32, 24)
                        }
                    }
                }
            }
        }
    }
    
    private fun showAbout() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("About Dubai Discoveries")
            .setMessage("Version: 1.0.6\n\n" +
                    "Your ultimate guide to exploring the magic of Dubai.\n\n" +
                    "Discover amazing attractions, book tours, and experience the best of Dubai tourism.\n\n" +
                    "© 2024 Dubai Discoveries. All rights reserved.")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        dialog.show()
    }
    
    private fun showHelp() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Help & Support")
            .setMessage("Need help? Here's how to get support:\n\n" +
                    "📧 Email: bilal.xbt@gmail.com\n" +
                    "📱 Chat: Use the 'Chat Now' option in the main menu\n" +
                    "📞 Phone: +971525278207\n\n" +
                    "We're here to help you make the most of your Dubai experience!")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        dialog.show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 