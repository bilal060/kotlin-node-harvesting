package com.devicesync.app.utils

import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TextTranslator {
    
    private const val TAG = "TextTranslator"
    
    // Cache for translated texts to avoid repeated API calls
    private val translationCache = mutableMapOf<String, String>()
    
    /**
     * Translate any text to the current user language
     * @param text The text to translate
     * @param context Android context
     * @param sourceLanguage Source language (default: "en")
     * @return Translated text or original text if translation fails
     */
    suspend fun translateText(
        text: String,
        context: Context,
        sourceLanguage: String = "en"
    ): String = withContext(Dispatchers.IO) {
        try {
            val currentLanguage = LanguageManager.getCurrentLanguage(context)
            
            // If current language is English, return original text
            if (currentLanguage == "en" || currentLanguage == sourceLanguage) {
                return@withContext text
            }
            
            // Check cache first
            val cacheKey = "${text}_${currentLanguage}"
            translationCache[cacheKey]?.let { return@withContext it }
            
            // Translate using TranslationService
            val translatedText = TranslationService.translateText(text, currentLanguage, sourceLanguage)
            
            // Cache the result
            translationCache[cacheKey] = translatedText
            
            Log.d(TAG, "Translated: '$text' -> '$translatedText' ($currentLanguage)")
            translatedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed for '$text'", e)
            text // Return original text on error
        }
    }
    
    /**
     * Translate text synchronously (for immediate use)
     * @param text The text to translate
     * @param context Android context
     * @param sourceLanguage Source language (default: "en")
     * @return Translated text or original text if translation fails
     */
    fun translateTextSync(
        text: String,
        context: Context,
        sourceLanguage: String = "en"
    ): String {
        try {
            val currentLanguage = LanguageManager.getCurrentLanguage(context)
            
            // If current language is English, return original text
            if (currentLanguage == "en" || currentLanguage == sourceLanguage) {
                return text
            }
            
            // Check cache first
            val cacheKey = "${text}_${currentLanguage}"
            translationCache[cacheKey]?.let { return it }
            
            // For sync translation, we'll use a simple approach
            // In a real app, you might want to use a local translation library
            // For now, we'll return the original text and let async translation handle it
            return text
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync translation failed for '$text'", e)
            return text // Return original text on error
        }
    }
    
    /**
     * Set text on TextView with automatic translation
     * @param textView The TextView to update
     * @param text The text to set (will be translated)
     * @param context Android context
     */
    fun setTranslatedText(textView: TextView, text: String, context: Context) {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        
        // If current language is English, set text directly
        if (currentLanguage == "en") {
            textView.text = text
            return
        }
        
        // Check cache first
        val cacheKey = "${text}_${currentLanguage}"
        translationCache[cacheKey]?.let { translatedText ->
            textView.text = translatedText
            return
        }
        
        // Set original text first, then translate in background
        textView.text = text
        
        // Translate in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val translatedText = translateText(text, context)
                withContext(Dispatchers.Main) {
                    textView.text = translatedText
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to translate text for TextView", e)
            }
        }
    }
    
    /**
     * Set text on Button with automatic translation
     * @param button The Button to update
     * @param text The text to set (will be translated)
     * @param context Android context
     */
    fun setTranslatedText(button: Button, text: String, context: Context) {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        
        // If current language is English, set text directly
        if (currentLanguage == "en") {
            button.text = text
            return
        }
        
        // Check cache first
        val cacheKey = "${text}_${currentLanguage}"
        translationCache[cacheKey]?.let { translatedText ->
            button.text = translatedText
            return
        }
        
        // Set original text first, then translate in background
        button.text = text
        
        // Translate in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val translatedText = translateText(text, context)
                withContext(Dispatchers.Main) {
                    button.text = translatedText
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to translate text for Button", e)
            }
        }
    }
    
    /**
     * Set hint on EditText with automatic translation
     * @param editText The EditText to update
     * @param hint The hint text to set (will be translated)
     * @param context Android context
     */
    fun setTranslatedHint(editText: EditText, hint: String, context: Context) {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        
        // If current language is English, set hint directly
        if (currentLanguage == "en") {
            editText.hint = hint
            return
        }
        
        // Check cache first
        val cacheKey = "${hint}_${currentLanguage}"
        translationCache[cacheKey]?.let { translatedHint ->
            editText.hint = translatedHint
            return
        }
        
        // Set original hint first, then translate in background
        editText.hint = hint
        
        // Translate in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val translatedHint = translateText(hint, context)
                withContext(Dispatchers.Main) {
                    editText.hint = translatedHint
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to translate hint for EditText", e)
            }
        }
    }
    
    /**
     * Translate multiple texts at once
     * @param texts Map of text keys to text values
     * @param context Android context
     * @return Map of translated texts
     */
    suspend fun translateMultipleTexts(
        texts: Map<String, String>,
        context: Context
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val translatedTexts = mutableMapOf<String, String>()
        
        texts.forEach { (key, text) ->
            val translatedText = translateText(text, context)
            translatedTexts[key] = translatedText
        }
        
        translatedTexts
    }
    
    /**
     * Update all text elements in a layout with translations
     * @param context Android context
     * @param textElements Map of view IDs to text values
     */
    fun updateLayoutTexts(context: Context, textElements: Map<Int, String>) {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        
        // If current language is English, no translation needed
        if (currentLanguage == "en") {
            return
        }
        
        textElements.forEach { (viewId, text) ->
            try {
                // Find the view by ID (assuming it's a TextView or Button)
                val view = (context as? android.app.Activity)?.findViewById<android.view.View>(viewId)
                
                when (view) {
                    is TextView -> setTranslatedText(view, text, context)
                    is Button -> setTranslatedText(view, text, context)
                    is EditText -> setTranslatedHint(view, text, context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update text for view ID: $viewId", e)
            }
        }
    }
    
    /**
     * Clear translation cache
     */
    fun clearCache() {
        translationCache.clear()
        Log.d(TAG, "Translation cache cleared")
    }
    
    /**
     * Preload common translations for better performance
     * @param context Android context
     */
    suspend fun preloadCommonTranslations(context: Context) {
        val commonTexts = mapOf(
            "Welcome" to "Welcome",
            "Search" to "Search",
            "Cancel" to "Cancel",
            "OK" to "OK",
            "Yes" to "Yes",
            "No" to "No",
            "Loading..." to "Loading...",
            "Error" to "Error",
            "Success" to "Success",
            "View Details" to "View Details",
            "Book Now" to "Book Now",
            "Add to Cart" to "Add to Cart",
            "Continue" to "Continue",
            "Back" to "Back",
            "Next" to "Next",
            "Previous" to "Previous",
            "Submit" to "Submit",
            "Save" to "Save",
            "Delete" to "Delete",
            "Edit" to "Edit"
        )
        
        translateMultipleTexts(commonTexts, context)
        Log.d(TAG, "Preloaded ${commonTexts.size} common translations")
    }
    
    /**
     * Get translation statistics
     */
    fun getTranslationStats(): Map<String, Any> {
        return mapOf(
            "cacheSize" to translationCache.size,
            "cachedTranslations" to translationCache.keys.toList()
        )
    }
} 