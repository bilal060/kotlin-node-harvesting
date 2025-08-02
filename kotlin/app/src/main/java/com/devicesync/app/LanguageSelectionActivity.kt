package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class LanguageSelectionActivity : AppCompatActivity() {

    private var selectedLanguage = "English"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup language selection
        setupLanguageSelection()

        // Setup apply button
        setupApplyButton()

        // Show welcome message
        Toast.makeText(this, "Select your preferred language", Toast.LENGTH_SHORT).show()
    }

    private fun setupLanguageSelection() {
        val englishCard = findViewById<CardView>(R.id.englishCard)
        val arabicCard = findViewById<CardView>(R.id.arabicCard)
        val frenchCard = findViewById<CardView>(R.id.frenchCard)
        val germanCard = findViewById<CardView>(R.id.germanCard)

        // English (default selected)
        englishCard.setOnClickListener {
            selectLanguage("English", englishCard, arabicCard, frenchCard, germanCard)
        }

        // Arabic
        arabicCard.setOnClickListener {
            selectLanguage("Arabic", arabicCard, englishCard, frenchCard, germanCard)
        }

        // French
        frenchCard.setOnClickListener {
            selectLanguage("French", frenchCard, englishCard, arabicCard, germanCard)
        }

        // German
        germanCard.setOnClickListener {
            selectLanguage("German", germanCard, englishCard, arabicCard, frenchCard)
        }
    }

    private fun selectLanguage(language: String, selectedCard: CardView, vararg otherCards: CardView) {
        selectedLanguage = language
        
        // Update selected card appearance - use elevation instead of stroke
        selectedCard.elevation = 8f
        selectedCard.alpha = 1.0f
        
        // Reset other cards
        otherCards.forEach { card ->
            card.elevation = 2f
            card.alpha = 0.8f
        }

        Toast.makeText(this, "Selected: $language", Toast.LENGTH_SHORT).show()
    }

    private fun setupApplyButton() {
        findViewById<MaterialButton>(R.id.applyLanguageButton).setOnClickListener {
            applyLanguage()
        }
    }

    private fun applyLanguage() {
        // In a real app, you would save the language preference and restart the app
        Toast.makeText(this, "Language changed to $selectedLanguage. Please restart the app for changes to take effect.", Toast.LENGTH_LONG).show()
        
        // Navigate back
        onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 