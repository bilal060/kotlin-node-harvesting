package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devicesync.app.utils.DynamicStringManager
import com.devicesync.app.utils.LanguageManager
import kotlinx.coroutines.launch

class TranslationExampleActivity : AppCompatActivity() {
    
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var searchButton: Button
    private lateinit var bookButton: Button
    private lateinit var languageButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translation_example)
        
        // Apply current language
        LanguageManager.applyLanguageToActivity(this)
        
        initializeViews()
        loadTranslatedStrings()
    }
    
    private fun initializeViews() {
        titleText = findViewById(R.id.titleText)
        descriptionText = findViewById(R.id.descriptionText)
        searchButton = findViewById(R.id.searchButton)
        bookButton = findViewById(R.id.bookButton)
        languageButton = findViewById(R.id.languageButton)
        
        // Set click listeners
        languageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }
    
    private fun loadTranslatedStrings() {
        lifecycleScope.launch {
            try {
                // Load strings asynchronously for better performance
                val title = DynamicStringManager.getString("welcome_to_dubai", this@TranslationExampleActivity)
                val description = DynamicStringManager.getString("setting_up_tourism", this@TranslationExampleActivity)
                val searchText = DynamicStringManager.getString("search", this@TranslationExampleActivity)
                val bookText = DynamicStringManager.getString("book_now", this@TranslationExampleActivity)
                val languageText = DynamicStringManager.getString("language_settings", this@TranslationExampleActivity)
                
                // Update UI on main thread
                titleText.text = title
                descriptionText.text = description
                searchButton.text = searchText
                bookButton.text = bookText
                languageButton.text = languageText
                
            } catch (e: Exception) {
                // Fallback to sync method if async fails
                loadStringsSync()
            }
        }
    }
    
    private fun loadStringsSync() {
        // Synchronous loading for immediate display
        titleText.text = DynamicStringManager.getStringSync("welcome_to_dubai", this)
        descriptionText.text = DynamicStringManager.getStringSync("setting_up_tourism", this)
        searchButton.text = DynamicStringManager.getStringSync("search", this)
        bookButton.text = DynamicStringManager.getStringSync("book_now", this)
        languageButton.text = DynamicStringManager.getStringSync("language_settings", this)
    }
    
    private fun showLanguageSelectionDialog() {
        val languages = mapOf(
            "en" to "English",
            "mn" to "Mongolian",
            "ru" to "Russian", 
            "zh" to "Chinese",
            "kk" to "Kazakh"
        )
        
        val languageNames = languages.values.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(DynamicStringManager.getStringSync("language_settings", this))
            .setItems(languageNames) { _, which ->
                val selectedLanguage = languages.keys.elementAt(which)
                changeLanguage(selectedLanguage)
            }
            .show()
    }
    
    private fun changeLanguage(languageCode: String) {
        // Change language
        LanguageManager.setLanguage(this, languageCode)
        
        // Reload strings with new language
        loadTranslatedStrings()
        
        // Show success message
        val successMessage = DynamicStringManager.getStringSync("success_updated", this)
        android.widget.Toast.makeText(this, successMessage, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload strings when activity resumes (in case language changed)
        loadStringsSync()
    }
}