package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.devicesync.app.utils.ThemeManager

class ThemeSelectionActivity : AppCompatActivity() {

    private var selectedTheme = ThemeManager.THEME_LIGHT

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
            selectTheme(ThemeManager.THEME_LIGHT, lightThemeCard, darkThemeCard, autoThemeCard)
        }

        // Dark Theme
        darkThemeCard.setOnClickListener {
            selectTheme(ThemeManager.THEME_DARK, darkThemeCard, lightThemeCard, autoThemeCard)
        }

        // Auto Theme
        autoThemeCard.setOnClickListener {
            selectTheme(ThemeManager.THEME_SYSTEM, autoThemeCard, lightThemeCard, darkThemeCard)
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

        val themeName = when (theme) {
            ThemeManager.THEME_LIGHT -> "Light"
            ThemeManager.THEME_DARK -> "Dark"
            ThemeManager.THEME_SYSTEM -> "System"
            else -> theme
        }
        Toast.makeText(this, "Selected: $themeName Theme", Toast.LENGTH_SHORT).show()
    }

    private fun setupApplyButton() {
        findViewById<MaterialButton>(R.id.applyThemeButton).setOnClickListener {
            applyTheme()
        }
    }

    private fun applyTheme() {
        // Apply the selected theme
        ThemeManager.setTheme(this, selectedTheme)
        
        val themeName = when (selectedTheme) {
            ThemeManager.THEME_LIGHT -> "Light"
            ThemeManager.THEME_DARK -> "Dark"
            ThemeManager.THEME_SYSTEM -> "System"
            else -> selectedTheme
        }
        Toast.makeText(this, "Theme changed to $themeName", Toast.LENGTH_SHORT).show()
        
        // Restart activity to apply theme changes
        recreate()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 