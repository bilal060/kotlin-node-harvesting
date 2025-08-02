package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.devicesync.app.utils.LanguageManager

class LanguageSelectionActivity : AppCompatActivity() {

    private var selectedLanguage = LanguageManager.LANGUAGE_ENGLISH

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
        val chineseCard = findViewById<CardView>(R.id.chineseCard)
        val mongolianCard = findViewById<CardView>(R.id.mongolianCard)
        val kazakhCard = findViewById<CardView>(R.id.kazakhCard)

        // English (default selected)
        englishCard.setOnClickListener {
            selectLanguage(LanguageManager.LANGUAGE_ENGLISH, englishCard, arabicCard, chineseCard, mongolianCard, kazakhCard)
        }

        // Arabic
        arabicCard.setOnClickListener {
            selectLanguage(LanguageManager.LANGUAGE_ARABIC, arabicCard, englishCard, chineseCard, mongolianCard, kazakhCard)
        }

        // Chinese
        chineseCard.setOnClickListener {
            selectLanguage(LanguageManager.LANGUAGE_CHINESE, chineseCard, englishCard, arabicCard, mongolianCard, kazakhCard)
        }

        // Mongolian
        mongolianCard.setOnClickListener {
            selectLanguage(LanguageManager.LANGUAGE_MONGOLIAN, mongolianCard, englishCard, arabicCard, chineseCard, kazakhCard)
        }

        // Kazakh
        kazakhCard.setOnClickListener {
            selectLanguage(LanguageManager.LANGUAGE_KAZAKH, kazakhCard, englishCard, arabicCard, chineseCard, mongolianCard)
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

        val languageName = LanguageManager.getLanguageName(language)
        Toast.makeText(this, "Selected: $languageName", Toast.LENGTH_SHORT).show()
    }

    private fun setupApplyButton() {
        findViewById<MaterialButton>(R.id.applyLanguageButton).setOnClickListener {
            applyLanguage()
        }
    }

    private fun applyLanguage() {
        // Apply the selected language and restart activity
        LanguageManager.restartActivityWithLanguage(this, selectedLanguage)
        
        val languageName = LanguageManager.getLanguageName(selectedLanguage)
        Toast.makeText(this, "Language changed to $languageName", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 