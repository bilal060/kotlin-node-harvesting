package com.devicesync.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "language"
    
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ARABIC = "ar"
    const val LANGUAGE_CHINESE = "zh"
    const val LANGUAGE_MONGOLIAN = "mn"
    const val LANGUAGE_KAZAKH = "kk"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getCurrentLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun setLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
        updateResources(context, languageCode)
    }
    
    fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_ARABIC -> "العربية"
            LANGUAGE_CHINESE -> "中文"
            LANGUAGE_MONGOLIAN -> "Монгол"
            LANGUAGE_KAZAKH -> "Қазақша"
            else -> "English"
        }
    }
    
    fun getLanguageFlag(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "🇺🇸"
            LANGUAGE_ARABIC -> "🇸🇦"
            LANGUAGE_CHINESE -> "🇨🇳"
            LANGUAGE_MONGOLIAN -> "🇲🇳"
            LANGUAGE_KAZAKH -> "🇰🇿"
            else -> "🇺🇸"
        }
    }
    
    fun applyLanguageToActivity(activity: android.app.Activity) {
        val currentLanguage = getCurrentLanguage(activity)
        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)
        
        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
        
        activity.createConfigurationContext(config)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
    
    fun restartActivityWithLanguage(activity: android.app.Activity, languageCode: String) {
        setLanguage(activity, languageCode)
        activity.recreate()
    }
} 