package com.devicesync.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_DATA = "user_data"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private val gson = Gson()
    
    fun saveAuthData(context: Context, accessToken: String, refreshToken: String, userData: UserData?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        editor.putString(KEY_USER_DATA, userData?.let { gson.toJson(it) })
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        
        editor.apply()
        
        Log.d("AuthManager", "✅ Auth data saved successfully")
    }
    
    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun getUserData(context: Context): UserData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userDataJson = prefs.getString(KEY_USER_DATA, null)
        
        return userDataJson?.let {
            try {
                gson.fromJson(it, UserData::class.java)
            } catch (e: Exception) {
                Log.e("AuthManager", "❌ Error parsing user data: ${e.message}")
                null
            }
        }
    }
    
    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAccessToken(context) != null
    }
    
    fun clearAuthData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.clear()
        editor.apply()
        
        Log.d("AuthManager", "🗑️ Auth data cleared successfully")
    }
    
    fun updateAccessToken(context: Context, newAccessToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.putString(KEY_ACCESS_TOKEN, newAccessToken)
        editor.apply()
        
        Log.d("AuthManager", "🔄 Access token updated successfully")
    }
}

data class UserData(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val deviceInfo: Map<String, String>
)

data class LoginData(
    val user: UserData,
    val tokens: Tokens
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
) 