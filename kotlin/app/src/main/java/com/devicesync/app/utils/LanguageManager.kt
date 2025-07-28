package com.devicesync.app.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

class LanguageManager(private val context: Context) {
    
    companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ARABIC = "ar"
        const val LANGUAGE_CHINESE = "zh"
        const val LANGUAGE_MONGOLIAN = "mn"
        
        val SUPPORTED_LANGUAGES = listOf(
            LANGUAGE_ENGLISH,
            LANGUAGE_ARABIC,
            LANGUAGE_CHINESE,
            LANGUAGE_MONGOLIAN
        )
    }
    
    private val sharedPreferences = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    
    fun setLanguage(languageCode: String) {
        val locale = when (languageCode) {
            LANGUAGE_ARABIC -> Locale("ar")
            LANGUAGE_CHINESE -> Locale("zh")
            LANGUAGE_MONGOLIAN -> Locale("mn")
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        context.createConfigurationContext(config)
        
        // Save language preference
        sharedPreferences.edit().putString("selected_language", languageCode).apply()
    }
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString("selected_language", LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_ARABIC -> "العربية"
            LANGUAGE_CHINESE -> "中文"
            LANGUAGE_MONGOLIAN -> "Монгол"
            else -> "English"
        }
    }
    
    fun getLanguageFlag(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "🇺🇸"
            LANGUAGE_ARABIC -> "🇸🇦"
            LANGUAGE_CHINESE -> "🇨🇳"
            LANGUAGE_MONGOLIAN -> "🇲🇳"
            else -> "🇺🇸"
        }
    }
    
    fun isRTL(languageCode: String): Boolean {
        return languageCode == LANGUAGE_ARABIC
    }
    
    fun getLocalizedString(key: String, languageCode: String = getCurrentLanguage()): String {
        return when (languageCode) {
            LANGUAGE_ARABIC -> getArabicString(key)
            LANGUAGE_CHINESE -> getChineseString(key)
            LANGUAGE_MONGOLIAN -> getMongolianString(key)
            else -> getEnglishString(key)
        }
    }
    
    private fun getEnglishString(key: String): String {
        return when (key) {
            "welcome" -> "Welcome to Dubai Discoveries"
            "popular_destinations" -> "Popular Destinations"
            "things_to_do" -> "Things to Do"
            "travel_services" -> "Travel Services"
            "ready_made_packages" -> "Ready-Made Packages"
            "what_travelers_say" -> "What Travelers Say"
            "uae_travel_tips" -> "UAE Travel Tips"
            "build_itinerary" -> "Build Your Own Itinerary"
            "book_now" -> "Book Now"
            "view_details" -> "View Details"
            "continue_planning" -> "Continue Planning"
            else -> key
        }
    }
    
    private fun getArabicString(key: String): String {
        return when (key) {
            "welcome" -> "مرحباً بكم في اكتشافات دبي"
            "popular_destinations" -> "الوجهات الشائعة"
            "things_to_do" -> "أشياء للقيام بها"
            "travel_services" -> "خدمات السفر"
            "ready_made_packages" -> "الحزم الجاهزة"
            "what_travelers_say" -> "ماذا يقول المسافرون"
            "uae_travel_tips" -> "نصائح السفر في الإمارات"
            "build_itinerary" -> "بناء رحلتك الخاصة"
            "book_now" -> "احجز الآن"
            "view_details" -> "عرض التفاصيل"
            "continue_planning" -> "استمر في التخطيط"
            else -> key
        }
    }
    
    private fun getChineseString(key: String): String {
        return when (key) {
            "welcome" -> "欢迎来到迪拜探索"
            "popular_destinations" -> "热门目的地"
            "things_to_do" -> "可做之事"
            "travel_services" -> "旅游服务"
            "ready_made_packages" -> "现成套餐"
            "what_travelers_say" -> "旅行者说"
            "uae_travel_tips" -> "阿联酋旅游贴士"
            "build_itinerary" -> "制定您的行程"
            "book_now" -> "立即预订"
            "view_details" -> "查看详情"
            "continue_planning" -> "继续规划"
            else -> key
        }
    }
    
    private fun getMongolianString(key: String): String {
        return when (key) {
            "welcome" -> "Дубай нээлтийн тавтай морил"
            "popular_destinations" -> "Түгээмэл хөтөч"
            "things_to_do" -> "Хийх зүйлс"
            "travel_services" -> "Аяллын үйлчилгээ"
            "ready_made_packages" -> "Бэлэн багцууд"
            "what_travelers_say" -> "Аялагчид юу гэж хэлэв"
            "uae_travel_tips" -> "Арабын Нэгдсэн Эмиратын аяллын зөвлөгөө"
            "build_itinerary" -> "Өөрийн аяллын төлөвлөгөөг бүтээх"
            "book_now" -> "Одоо захиалах"
            "view_details" -> "Дэлгэрэнгүй харах"
            "continue_planning" -> "Төлөвлөлтийг үргэлжлүүлэх"
            else -> key
        }
    }
} 