package com.devicesync.app.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object TranslationService {
    
    private const val TAG = "TranslationService"
    
    // Google Translate API endpoint (you'll need to set up API key)
    private const val GOOGLE_TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2"
    
    // Free alternative: LibreTranslate API
    private const val LIBRE_TRANSLATE_URL = "https://libretranslate.de/translate"
    
    // Cache for translations to avoid repeated API calls
    private val translationCache = mutableMapOf<String, String>()
    
    // Language codes mapping
    private val languageCodes = mapOf(
        "en" to "English",
        "mn" to "Mongolian", 
        "ru" to "Russian",
        "zh" to "Chinese",
        "kk" to "Kazakh"
    )
    
    /**
     * Translate text to the specified language
     */
    suspend fun translateText(
        text: String, 
        targetLanguage: String,
        sourceLanguage: String = "en"
    ): String = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            val cacheKey = "${text}_${targetLanguage}"
            translationCache[cacheKey]?.let { return@withContext it }
            
            // Use LibreTranslate (free) as primary service
            val translatedText = translateWithLibreTranslate(text, targetLanguage, sourceLanguage)
                ?: translateWithGoogleTranslate(text, targetLanguage, sourceLanguage)
                ?: text // Fallback to original text
            
            // Cache the result
            translationCache[cacheKey] = translatedText
            
            Log.d(TAG, "Translated: '$text' -> '$translatedText' ($targetLanguage)")
            translatedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed for '$text' to $targetLanguage", e)
            text // Return original text on error
        }
    }
    
    /**
     * Translate using LibreTranslate (free service)
     */
    private suspend fun translateWithLibreTranslate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            
            val jsonBody = JSONObject().apply {
                put("q", text)
                put("source", sourceLanguage)
                put("target", targetLanguage)
                put("format", "text")
            }
            
            val request = Request.Builder()
                .url(LIBRE_TRANSLATE_URL)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "")
                jsonResponse.optJSONObject("data")?.optString("translatedText") ?: text
            } else {
                Log.w(TAG, "LibreTranslate failed: ${response.code}")
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "LibreTranslate error", e)
            null
        }
    }
    
    /**
     * Translate using Google Translate API (requires API key)
     */
    private suspend fun translateWithGoogleTranslate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Note: You'll need to add your Google Translate API key
            val apiKey = "YOUR_GOOGLE_TRANSLATE_API_KEY" // Add this to your environment
            
            if (apiKey == "YOUR_GOOGLE_TRANSLATE_API_KEY") {
                Log.w(TAG, "Google Translate API key not configured")
                return@withContext null
            }
            
            val client = OkHttpClient()
            
            val url = "$GOOGLE_TRANSLATE_URL?key=$apiKey"
            
            val jsonBody = JSONObject().apply {
                put("q", text)
                put("source", sourceLanguage)
                put("target", targetLanguage)
                put("format", "text")
            }
            
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "")
                val translations = jsonResponse.optJSONObject("data")?.optJSONArray("translations")
                translations?.optJSONObject(0)?.optString("translatedText") ?: text
            } else {
                Log.w(TAG, "Google Translate failed: ${response.code}")
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Google Translate error", e)
            null
        }
    }
    
    /**
     * Translate all strings in a map to the target language
     */
    suspend fun translateStrings(
        strings: Map<String, String>,
        targetLanguage: String,
        sourceLanguage: String = "en"
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val translatedStrings = mutableMapOf<String, String>()
        
        strings.forEach { (key, value) ->
            val translatedValue = translateText(value, targetLanguage, sourceLanguage)
            translatedStrings[key] = translatedValue
        }
        
        translatedStrings
    }
    
    /**
     * Get available language codes
     */
    fun getAvailableLanguages(): Map<String, String> = languageCodes
    
    /**
     * Clear translation cache
     */
    fun clearCache() {
        translationCache.clear()
    }
    
    /**
     * Preload common translations for better performance
     */
    suspend fun preloadCommonTranslations(targetLanguage: String) {
        val commonStrings = mapOf(
            "welcome_title" to "Welcome to Dubai",
            "popular_destinations" to "Popular Destinations",
            "things_to_do" to "Things to Do",
            "travel_services" to "Travel Services",
            "start_planning" to "Start Planning",
            "view_details" to "View Details",
            "book_now" to "Book Now",
            "search" to "Search",
            "filter" to "Filter",
            "cancel" to "Cancel",
            "ok" to "OK",
            "loading" to "Loading...",
            "error" to "Error",
            "success" to "Success"
        )
        
        translateStrings(commonStrings, targetLanguage)
    }
} 