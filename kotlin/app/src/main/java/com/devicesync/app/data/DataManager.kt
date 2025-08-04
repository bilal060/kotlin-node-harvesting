package com.devicesync.app.data

import android.content.Context
import android.content.SharedPreferences
import com.devicesync.app.api.Slider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "dubai_discoveries_data", 
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_SLIDERS = "sliders"
        private const val KEY_ATTRACTIONS = "attractions"
        private const val KEY_SERVICES = "services"
        private const val KEY_TOUR_PACKAGES = "tour_packages"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_DATA_VERSION = "data_version"
    }
    
    // Slider data management
    fun saveSliders(sliders: List<Slider>) {
        val json = gson.toJson(sliders)
        sharedPreferences.edit()
            .putString(KEY_SLIDERS, json)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }
    
    fun getSliders(): List<Slider> {
        val json = sharedPreferences.getString(KEY_SLIDERS, "[]")
        val type = object : TypeToken<List<Slider>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getHeroSliders(): List<Slider> {
        return getSliders().filter { it.category == "hero" && it.isActive }
            .sortedBy { it.order }
    }
    
    fun getAttractionSliders(): List<Slider> {
        return getSliders().filter { it.category == "attractions" && it.isActive }
            .sortedBy { it.order }
    }
    
    fun getServiceSliders(): List<Slider> {
        return getSliders().filter { it.category == "services" && it.isActive }
            .sortedBy { it.order }
    }
    
    // Generic data management for attractions, services, tour packages
    fun <T> saveData(key: String, data: List<T>) {
        val json = gson.toJson(data)
        sharedPreferences.edit()
            .putString(key, json)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }
    
    internal inline fun <reified T> getData(key: String): List<T> {
        val json = getSharedPreferences().getString(key, "[]")
        val type = object : TypeToken<List<T>>() {}.type
        return try {
            getGson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    internal fun getSharedPreferences() = sharedPreferences
    internal fun getGson() = gson
    
    // Attractions
    fun saveAttractions(attractions: List<Any>) {
        saveData(KEY_ATTRACTIONS, attractions)
    }
    
    fun getAttractions(): List<Any> {
        return getData(KEY_ATTRACTIONS)
    }
    
    // Services
    fun saveServices(services: List<Any>) {
        saveData(KEY_SERVICES, services)
    }
    
    fun getServices(): List<Any> {
        return getData(KEY_SERVICES)
    }
    
    // Tour Packages
    fun saveTourPackages(tourPackages: List<Any>) {
        saveData(KEY_TOUR_PACKAGES, tourPackages)
    }
    
    fun getTourPackages(): List<Any> {
        return getData(KEY_TOUR_PACKAGES)
    }
    
    // Sync management
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0)
    }
    
    fun isDataStale(maxAgeHours: Int = 24): Boolean {
        val lastSync = getLastSyncTime()
        val currentTime = System.currentTimeMillis()
        val maxAgeMs = maxAgeHours * 60 * 60 * 1000L
        return (currentTime - lastSync) > maxAgeMs
    }
    
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun hasData(): Boolean {
        return getSliders().isNotEmpty() || 
               getAttractions().isNotEmpty() || 
               getServices().isNotEmpty() || 
               getTourPackages().isNotEmpty()
    }
    
    // Data version management
    fun saveDataVersion(version: String) {
        sharedPreferences.edit()
            .putString(KEY_DATA_VERSION, version)
            .apply()
    }
    
    fun getDataVersion(): String {
        return sharedPreferences.getString(KEY_DATA_VERSION, "1.0.0") ?: "1.0.0"
    }
} 