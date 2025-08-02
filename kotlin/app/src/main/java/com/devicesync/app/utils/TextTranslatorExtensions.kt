package com.devicesync.app.utils

import android.content.Context
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Extension function to translate any string to current user language
 */
fun String.translate(context: Context): String {
    val currentLanguage = LanguageManager.getCurrentLanguage(context)
    
    // If current language is English, return original text
    if (currentLanguage == "en") {
        return this
    }
    
    // Use TextTranslator to translate
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val translatedText = TextTranslator.translateText(this@translate, context)
            // Note: This is async, so the original text is returned immediately
            // The translated text will be available in cache for next use
        } catch (e: Exception) {
            // Translation failed, keep original text
        }
    }
    
    return this
}

/**
 * Extension function to set translated text on TextView
 */
fun TextView.setTextTranslated(text: String, context: Context) {
    TextTranslator.setTranslatedText(this, text, context)
}

/**
 * Extension function to set translated text on Button
 */
fun Button.setTextTranslated(text: String, context: Context) {
    TextTranslator.setTranslatedText(this, text, context)
}

/**
 * Extension function to set translated hint on EditText
 */
fun EditText.setHintTranslated(hint: String, context: Context) {
    TextTranslator.setTranslatedHint(this, hint, context)
}

/**
 * Extension function to translate text and return it asynchronously
 */
suspend fun String.translateAsync(context: Context): String {
    return TextTranslator.translateText(this, context)
}

/**
 * Extension function to translate multiple texts at once
 */
suspend fun Map<String, String>.translateAll(context: Context): Map<String, String> {
    return TextTranslator.translateMultipleTexts(this, context)
}

/**
 * Extension function to update all text elements in a layout
 */
fun Context.updateAllTexts(textElements: Map<Int, String>) {
    TextTranslator.updateLayoutTexts(this, textElements)
}

/**
 * Extension function to get translation statistics
 */
fun Context.getTranslationStats(): Map<String, Any> {
    return TextTranslator.getTranslationStats()
}

/**
 * Extension function to clear translation cache
 */
fun Context.clearTranslationCache() {
    TextTranslator.clearCache()
}

/**
 * Extension function to preload common translations
 */
suspend fun Context.preloadCommonTranslations() {
    TextTranslator.preloadCommonTranslations(this)
} 