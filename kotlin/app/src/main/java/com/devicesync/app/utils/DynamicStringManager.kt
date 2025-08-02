package com.devicesync.app.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DynamicStringManager {
    
    private const val TAG = "DynamicStringManager"
    
    // Cache for all app strings in different languages
    private val stringCache = mutableMapOf<String, MutableMap<String, String>>()
    
    // All app strings in English (source language)
    private val appStrings = mapOf(
        // Splash Screen
        "welcome_to_dubai" to "🌟 Welcome to Dubai Discoveries!",
        "setting_up_tourism" to "Setting up your tourism experience...",
        "registering_device" to "Registering your device...",
        "connecting_services" to "Connecting to Dubai tourism services...",
        "checking_permissions" to "Checking permissions...",
        "preparing_features" to "Preparing personalized features...",
        "almost_ready" to "Almost ready...",
        "adventure_awaits" to "Your Dubai adventure awaits!",
        "complete" to "Complete!",
        
        // Permission Dialog
        "permission_title" to "🌟 Welcome to Dubai Discoveries!",
        "permission_message" to "To enhance your Dubai tourism experience, we need:\n\n• 👥 Contacts - For tour coordination\n• 📱 Notifications - For tour updates\n\nYou can enable these later in settings if needed.",
        "permission_dialog_message" to "To enhance your Dubai tourism experience, we need:\n\n• 👥 Contacts - For tour coordination\n• 📱 Notifications - For tour updates\n\nYou can enable these later in settings if needed.",
        "allow" to "Allow",
        "allow_button" to "Allow",
        "skip" to "Skip",
        "skip_button" to "Skip",
        "complete_setup_title" to "🏛️ Complete Your Dubai Setup",
        "permission_reminder_message" to "To unlock the full Dubai tourism experience, please grant these permissions:\n\n• 👥 Contacts - Connect with fellow travelers and tour groups\n• 📞 Call logs - Receive booking confirmations and guide calls\n• 📧 Email accounts - Access exclusive Dubai deals and offers\n• 📱 Notifications - Stay updated with tour schedules and events\n• 📁 Files & Storage - Save your Dubai memories and travel photos\n• 🎵 Music & Audio - Enjoy audio tours and background music\n• 🎤 Microphone - Record voice notes and audio memories\n• 📹 Video - Capture and share your Dubai moments\n\nWithout these permissions, you'll miss out on:\n• 🎫 Exclusive tour bookings and discounts\n• 📸 Photo and video sharing with travel groups\n• 🎵 Audio tours and guided experiences\n• 🔔 Real-time Dubai event notifications\n• 💬 Travel community features\n\nWould you like to enable the full Dubai experience?",
        "enable_full_experience_button" to "Enable Full Experience",
        "continue_anyway_button" to "Continue Anyway",
        "unlock_adventure_title" to "🌆 Unlock Your Dubai Adventure",
        "final_permission_warning_message" to "To experience the magic of Dubai to its fullest, we need these permissions:\n\n• 👥 Contacts - Connect with your travel companions and tour groups\n• 📞 Call logs - Stay connected with your Dubai tour guides\n• 📧 Email accounts - Get VIP access to Dubai attractions and events\n• 📱 Notifications - Never miss a Dubai experience or special deal\n• 📁 Files & Storage - Preserve your Dubai memories and share with friends\n• 🎵 Music & Audio - Enjoy immersive audio tours and background music\n• 🎤 Microphone - Record your Dubai experiences and voice notes\n• 📹 Video - Capture and share your Dubai adventures\n\nThese permissions unlock:\n• 🏰 VIP access to Burj Khalifa and Palm Jumeirah\n• 🛍️ Exclusive shopping discounts at Dubai Mall\n• 🚁 Helicopter tour bookings and desert safaris\n• 🍽️ Restaurant reservations at top Dubai venues\n• 🎭 Show tickets and entertainment access\n• 🎵 Audio-guided tours and cultural experiences\n• 📸 Professional photo and video sharing\n\nWould you like to unlock your complete Dubai experience?",
        "unlock_dubai_experience_button" to "Unlock Dubai Experience",
        
        // Main Navigation
        "home" to "Home",
        "attractions" to "Attractions",
        "services" to "Services",
        "tours" to "Tours",
        "profile" to "Profile",
        "settings" to "Settings",
        
        // Common Actions
        "search" to "Search",
        "filter" to "Filter",
        "sort" to "Sort",
        "view_details" to "View Details",
        "book_now" to "Book Now",
        "add_to_cart" to "Add to Cart",
        "continue" to "Continue",
        "back" to "Back",
        "next" to "Next",
        "previous" to "Previous",
        "submit" to "Submit",
        "save" to "Save",
        "delete" to "Delete",
        "edit" to "Edit",
        "cancel" to "Cancel",
        "ok" to "OK",
        "yes" to "Yes",
        "no" to "No",
        
        // Status Messages
        "loading" to "Loading...",
        "error" to "Error",
        "success" to "Success",
        "no_results" to "No results found",
        "network_error" to "Network error",
        "try_again" to "Try again",
        
        // Attractions
        "popular_attractions" to "Popular Attractions",
        "all_attractions" to "All Attractions",
        "attraction_details" to "Attraction Details",
        "attraction_description" to "Description",
        "attraction_location" to "Location",
        "attraction_hours" to "Opening Hours",
        "attraction_price" to "Price",
        "attraction_rating" to "Rating",
        "attraction_reviews" to "Reviews",
        
        // Services
        "travel_services" to "Travel Services",
        "transportation" to "Transportation",
        "accommodation" to "Accommodation",
        "restaurants" to "Restaurants",
        "shopping" to "Shopping",
        "entertainment" to "Entertainment",
        
        // Tours
        "guided_tours" to "Guided Tours",
        "self_guided" to "Self-Guided",
        "tour_packages" to "Tour Packages",
        "custom_tours" to "Custom Tours",
        "tour_duration" to "Duration",
        "tour_price" to "Price",
        "tour_includes" to "What's Included",
        "tour_excludes" to "What's Not Included",
        
        // Profile & Settings
        "my_profile" to "My Profile",
        "my_bookings" to "My Bookings",
        "my_favorites" to "My Favorites",
        "language_settings" to "Language Settings",
        "notification_settings" to "Notification Settings",
        "privacy_settings" to "Privacy Settings",
        "about_app" to "About App",
        "help_support" to "Help & Support",
        
        // Authentication
        "login" to "Login",
        "register" to "Register",
        "logout" to "Logout",
        "email" to "Email",
        "password" to "Password",
        "confirm_password" to "Confirm Password",
        "forgot_password" to "Forgot Password?",
        "sign_in" to "Sign In",
        "sign_up" to "Sign Up",
        
        // Permissions
        "permission_contacts" to "Contacts",
        "permission_notifications" to "Notifications",
        "permission_location" to "Location",
        "permission_camera" to "Camera",
        "permission_storage" to "Storage",
        "permission_microphone" to "Microphone",
        
        // Error Messages
        "error_network" to "Network connection error",
        "error_server" to "Server error",
        "error_unknown" to "Unknown error occurred",
        "error_permission_denied" to "Permission denied",
        "error_invalid_input" to "Invalid input",
        
        // Success Messages
        "success_booking" to "Booking successful!",
        "success_saved" to "Saved successfully!",
        "success_updated" to "Updated successfully!",
        "success_deleted" to "Deleted successfully!",
        
        // Time & Date
        "today" to "Today",
        "tomorrow" to "Tomorrow",
        "yesterday" to "Yesterday",
        "morning" to "Morning",
        "afternoon" to "Afternoon",
        "evening" to "Evening",
        "night" to "Night",
        
        // Weather
        "sunny" to "Sunny",
        "cloudy" to "Cloudy",
        "rainy" to "Rainy",
        "hot" to "Hot",
        "warm" to "Warm",
        "cool" to "Cool",
        "cold" to "Cold",
        
        // Layout Strings - Main Activity
        "dubai_discoveries" to "Dubai Discoveries",
        "plan_dubai_adventure" to "Plan Your Dubai Adventure",
        "start_planning_description" to "Discover amazing attractions, book tours, and create unforgettable memories in the city of dreams.",
        "start_planning" to "Start Planning",
        "start_planning_button" to "Start Planning",
        "start_date" to "Start Date",
        "end_date" to "End Date",
        "select_date" to "Select Date",
        "select_date_with_icon" to "📅 Select Date",
        "select_time" to "Select Time",
        "select_time_with_icon" to "🕐 Select Time",
        "number_of_people" to "Number of People",
        "default_people_count" to "2",
        "sample_date_range" to "2024-03-14 - 2024-03-16",
        "days_planned" to "Days Planned",
        "continue_planning" to "Continue Planning",
        "detailed_trip_planning" to "Detailed Trip Planning",
        "ready_made_packages" to "Ready-made Packages",
        "packages" to "Packages",
        "packages_subtitle" to "Choose from our curated packages",
        "tour_packages" to "Tour Packages",
        "things_to_do" to "Things to Do",
        "popular_destinations" to "Popular Destinations",
        "attractions" to "Attractions",
        "services" to "Services",
        "travel_services" to "Travel Services",
        "trip_management" to "Trip Management",
        "schedule_manage_trips" to "Schedule & Manage Trips",
        "past_experiences" to "Past Experiences",
        "gallery_videos_previous_tours" to "Gallery & Videos from Previous Tours",
        "discover_our_previous_tours_and_memorable_adventures" to "Discover our previous tours and memorable adventures",
        "what_travelers_say" to "What Travelers Say",
        "traveler_reviews" to "Traveler Reviews",
        "essential_travel_tips" to "Essential Travel Tips",
        "uae_travel_tips" to "UAE Travel Tips",
        "tips_description" to "Your ultimate guide to exploring the magic of Dubai",
        "search_travel_tips" to "Search Travel Tips",
        "view_all_tips" to "View All Tips",
        "no_tips_found" to "No tips found",
        "try_adjusting_search" to "Try adjusting your search",
        "our_team" to "Our Team",
        "meet_professional_team" to "Meet Our Professional Team",
        "meet_the_passionate_team_behind_your_dubai_adventures" to "Meet the passionate team behind your Dubai adventures",
        "language_english" to "English",
        "choose_your_language" to "Choose Your Language",
        "select_your_preferred_language_to_get_started" to "Select your preferred language to get started",
        
        // UAE Travel Tips
        "uae_travel_tips_title" to "UAE Travel Tips",
        "travel_tips_description" to "Essential tips for your Dubai adventure",
        "tip_1_title" to "Best Time to Visit",
        "tip_1_description" to "Visit between November and March for pleasant weather",
        "tip_2_title" to "Dress Code",
        "tip_2_description" to "Respect local customs, especially in religious sites",
        "tip_3_title" to "Transportation",
        "tip_3_description" to "Use Dubai Metro for efficient city travel",
        "tip_4_title" to "Currency",
        "tip_4_description" to "UAE Dirham (AED) is the local currency",
        "tip_5_title" to "Language",
        "tip_5_description" to "Arabic is official, but English is widely spoken",
        "tip_6_title" to "Safety",
        "tip_6_description" to "Dubai is one of the safest cities in the world",
        
        // Travel Services
        "airport_transfer" to "Airport Transfer",
        "airport_transfer_price" to "From AED 50",
        "private_guide" to "Private Guide",
        "private_guide_price" to "From AED 200",
        "car_with_driver" to "Car with Driver",
        "car_with_driver_price" to "From AED 150",
        "sim_card" to "SIM Card",
        "sim_card_price" to "From AED 25",
        "add_service" to "Add Service",
        
        // Additional Services
        "additional_services" to "Additional Services",
        "tour_packages_button" to "Tour Packages",
        "tour_packages_description" to "Ready-made tour packages",
        "services_button" to "Services",
        "services_description" to "Explore our services",
        "past_experiences_button" to "Past Experiences",
        "past_experiences_description" to "View your travel history",
        "trip_management_button" to "Trip Management",
        "trip_management_description" to "Manage your bookings",
        "device_discovery_button" to "Device Discovery",
        "device_discovery_description" to "Connect nearby devices",
        "chat_now_button" to "Chat Now",
        "audio_guide_button" to "Audio Guide",
        "reviews_button" to "Reviews",
        "team_button" to "Team",
        "additional_services_title" to "Additional Services",
        "build_own_itinerary" to "Build Your Own Itinerary",
        "build_own_itinerary_description" to "Create your personalized Dubai adventure",
        
        // Live Chat
        "live_chat" to "Live Chat",
        "live_chat_description" to "Get instant support and assistance",
        
        // Menu Items
        "profile_settings" to "Profile Settings",
        "payment" to "Payment",
        "settings" to "Settings",
        "sync_status" to "Sync Status",
        "language_settings" to "Language Settings",
        "help_support" to "Help & Support",
        "logout" to "Logout",
        "user_profile" to "User Profile",
        "coming_soon" to "Coming Soon"
    )
    
    /**
     * Get a string in the current language
     * @param key The string key
     * @param context Android context
     * @return Translated string or original if translation fails
     */
    suspend fun getString(key: String, context: Context): String = withContext(Dispatchers.IO) {
        try {
            val currentLanguage = LanguageManager.getCurrentLanguage(context)
            
            // If current language is English, return original
            if (currentLanguage == "en") {
                return@withContext appStrings[key] ?: key
            }
            
            // Check cache first
            val cachedTranslation = stringCache[currentLanguage]?.get(key)
            if (cachedTranslation != null) {
                return@withContext cachedTranslation
            }
            
            // Get original text
            val originalText = appStrings[key] ?: key
            
            // Translate the text
            val translatedText = TextTranslator.translateText(originalText, context)
            
            // Cache the result
            if (!stringCache.containsKey(currentLanguage)) {
                stringCache[currentLanguage] = mutableMapOf()
            }
            stringCache[currentLanguage]!![key] = translatedText
            
            Log.d(TAG, "Translated string: $key -> $translatedText ($currentLanguage)")
            translatedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get string: $key", e)
            appStrings[key] ?: key
        }
    }
    
    /**
     * Get a string synchronously (for immediate use)
     * @param key The string key
     * @param context Android context
     * @return Translated string or original if translation fails
     */
    fun getStringSync(key: String, context: Context): String {
        try {
            val currentLanguage = LanguageManager.getCurrentLanguage(context)
            
            // If current language is English, return original
            if (currentLanguage == "en") {
                return appStrings[key] ?: key
            }
            
            // Check cache first
            val cachedTranslation = stringCache[currentLanguage]?.get(key)
            if (cachedTranslation != null) {
                return cachedTranslation
            }
            
            // Get original text
            val originalText = appStrings[key] ?: key
            
            // Try to get immediate translation
            return try {
                val translatedText = TextTranslator.translateTextSync(originalText, context, "en")
                // Cache the result
                if (!stringCache.containsKey(currentLanguage)) {
                    stringCache[currentLanguage] = mutableMapOf()
                }
                stringCache[currentLanguage]!![key] = translatedText
                translatedText
            } catch (e: Exception) {
                Log.e(TAG, "Sync translation failed for: $key", e)
                originalText
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in getStringSync for key: $key", e)
            return appStrings[key] ?: key
        }
    }
    
    /**
     * Preload all strings for a specific language
     * @param language The target language
     * @param context Android context
     */
    suspend fun preloadLanguage(language: String, context: Context) {
        try {
            Log.d(TAG, "Preloading strings for language: $language")
            
            val translatedStrings = mutableMapOf<String, String>()
            
            appStrings.forEach { (key, value) ->
                val translatedValue = TextTranslator.translateText(value, context, "en")
                translatedStrings[key] = translatedValue
            }
            
            stringCache[language] = translatedStrings
            
            Log.d(TAG, "Preloaded ${translatedStrings.size} strings for $language")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload language: $language", e)
        }
    }
    
    /**
     * Clear cache for a specific language
     * @param language The language to clear
     */
    fun clearLanguageCache(language: String) {
        stringCache.remove(language)
        Log.d(TAG, "Cleared cache for language: $language")
    }
    
    /**
     * Clear all caches
     */
    fun clearAllCaches() {
        stringCache.clear()
        TextTranslator.clearCache()
        Log.d(TAG, "Cleared all string caches")
    }
    
    /**
     * Get all available string keys
     */
    fun getAllStringKeys(): Set<String> = appStrings.keys
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "totalLanguages" to stringCache.size,
            "languages" to stringCache.keys.toList(),
            "totalStrings" to appStrings.size,
            "cachedStrings" to stringCache.values.sumOf { it.size }
        )
    }
    
    /**
     * Check if a string key exists
     */
    fun hasString(key: String): Boolean = appStrings.containsKey(key)
    
    /**
     * Add a custom string (for dynamic content)
     * @param key The string key
     * @param value The string value
     */
    fun addCustomString(key: String, value: String) {
        // Note: This would need to be implemented with a mutable map
        Log.d(TAG, "Custom string added: $key -> $value")
    }
} 