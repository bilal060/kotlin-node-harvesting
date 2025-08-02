package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class ThemeSelectionActivity : AppCompatActivity() {

    private var selectedTheme = "Light"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_selection)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Setup theme selection
        setupThemeSelection()

        // Setup apply button
        setupApplyButton()

        // Show welcome message
        Toast.makeText(this, "Select your preferred theme", Toast.LENGTH_SHORT).show()
    }

    private fun setupThemeSelection() {
        val lightThemeCard = findViewById<CardView>(R.id.lightThemeCard)
        val darkThemeCard = findViewById<CardView>(R.id.darkThemeCard)
        val autoThemeCard = findViewById<CardView>(R.id.autoThemeCard)

        // Light Theme (default selected)
        lightThemeCard.setOnClickListener {
            selectTheme("Light", lightThemeCard, darkThemeCard, autoThemeCard)
        }

        // Dark Theme
        darkThemeCard.setOnClickListener {
            selectTheme("Dark", darkThemeCard, lightThemeCard, autoThemeCard)
        }

        // Auto Theme
        autoThemeCard.setOnClickListener {
            selectTheme("Auto", autoThemeCard, lightThemeCard, darkThemeCard)
        }
    }

    private fun selectTheme(theme: String, selectedCard: CardView, vararg otherCards: CardView) {
        selectedTheme = theme
        
        // Update selected card appearance - use elevation instead of stroke
        selectedCard.elevation = 8f
        selectedCard.alpha = 1.0f
        
        // Reset other cards
        otherCards.forEach { card ->
            card.elevation = 2f
            card.alpha = 0.8f
        }

        Toast.makeText(this, "Selected: $theme Theme", Toast.LENGTH_SHORT).show()
    }

    private fun setupApplyButton() {
        findViewById<MaterialButton>(R.id.applyThemeButton).setOnClickListener {
            applyTheme()
        }
    }

    private fun applyTheme() {
        // In a real app, you would save the theme preference and restart the app
        Toast.makeText(this, "Theme changed to $selectedTheme. Please restart the app for changes to take effect.", Toast.LENGTH_LONG).show()
        
        // Navigate back
        onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 