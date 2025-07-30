package com.devicesync.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

object LanguageManager {
    
    private const val LANGUAGE_PREF = "app_language"
    private const val DEFAULT_LANGUAGE = "en"
    
    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString(LANGUAGE_PREF, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    fun restartActivityWithLanguage(activity: Activity, languageCode: String) {
        setAppLanguage(activity, languageCode)
        
        // Restart the activity to apply language change
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0) // No animation for smooth transition
    }
    
    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("en", "English", "English"),
            Language("mn", "Монгол", "Mongolian"),
            Language("ru", "Русский", "Russian"),
            Language("zh", "中文", "Chinese"),
            Language("kk", "Қазақша", "Kazakh")
        )
    }
    
    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            "kk" -> Locale("kk", "KZ") // Kazakh with Kazakhstan country code
            else -> Locale(languageCode)
        }
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Save language preference
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_PREF, languageCode)
            .apply()
        
        // Preload translations for the new language
        if (languageCode != "en") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DynamicStringManager.preloadLanguage(languageCode, context)
                    Log.d("LanguageManager", "Preloaded translations for $languageCode")
                } catch (e: Exception) {
                    Log.e("LanguageManager", "Failed to preload translations for $languageCode", e)
                }
            }
        }
        
        // Debug logging
        Log.d("LanguageManager", "Language set to: $languageCode, Locale: $locale")
    }
    
    fun setLanguage(context: Context, languageCode: String) {
        setAppLanguage(context, languageCode)
    }
    
    fun applyLanguageToActivity(activity: Activity) {
        val currentLanguage = getCurrentLanguage(activity)
        val locale = when (currentLanguage) {
            "kk" -> Locale("kk", "KZ") // Kazakh with Kazakhstan country code
            else -> Locale(currentLanguage)
        }
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        
        activity.createConfigurationContext(config)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
    
    data class Language(
        val code: String,
        val nativeName: String,
        val englishName: String
    )
} 