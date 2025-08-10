package com.devicesync.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.devicesync.app.services.ErrorReportingService
import com.devicesync.app.utils.DeviceInfo

/**
 * Custom Application class for DeviceSync app
 * Initializes error reporting service and other global components
 */
class DeviceSyncApplication : Application() {
    
    companion object {
        private const val TAG = "DeviceSyncApplication"
        
        // Global context access
        lateinit var appContext: Context
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Set global context
            appContext = applicationContext
            
            Log.i(TAG, "🚀 DeviceSync Application starting...")
            
            // Initialize device info
            DeviceInfo.initialize(this)
            
            // Initialize error reporting service
            initializeErrorReporting()
            
            // Initialize other services here if needed
            
            Log.i(TAG, "✅ DeviceSync Application initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize DeviceSync Application", e)
        }
    }
    
    /**
     * Initialize error reporting service
     */
    private fun initializeErrorReporting() {
        try {
            Log.i(TAG, "🔧 Initializing error reporting service...")
            
            // Get error reporting service instance and initialize
            val errorService = ErrorReportingService.getInstance(this)
            errorService.initialize()
            
            // Test error reporting
            errorService.reportError(
                reason = "Application started successfully",
                level = ErrorReportingService.ErrorLevel.INFO,
                source = ErrorReportingService.ErrorSource.GENERAL_ERROR,
                additionalData = mapOf(
                    "initialization" to "successful",
                    "timestamp" to System.currentTimeMillis()
                )
            )
            
            Log.i(TAG, "✅ Error reporting service initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize error reporting service", e)
        }
    }
    
    override fun onTerminate() {
        try {
            Log.i(TAG, "🛑 DeviceSync Application terminating...")
            
            // Cleanup operations here if needed
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during application termination", e)
        }
        
        super.onTerminate()
    }
    
    override fun onLowMemory() {
        try {
            Log.w(TAG, "⚠️ Low memory detected")
            
            // Report low memory error
            ErrorReportingService.getInstance(this).reportError(
                reason = "Low memory detected",
                level = ErrorReportingService.ErrorLevel.WARNING,
                source = ErrorReportingService.ErrorSource.MEMORY_ERROR,
                additionalData = mapOf(
                    "availableMemory" to Runtime.getRuntime().freeMemory(),
                    "totalMemory" to Runtime.getRuntime().totalMemory(),
                    "maxMemory" to Runtime.getRuntime().maxMemory()
                )
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to handle low memory", e)
        }
        
        super.onLowMemory()
    }
    
    override fun onTrimMemory(level: Int) {
        try {
            Log.d(TAG, "🧹 Memory trim requested: level $level")
            
            // Report memory trim
            ErrorReportingService.getInstance(this).reportError(
                reason = "Memory trim requested",
                level = ErrorReportingService.ErrorLevel.INFO,
                source = ErrorReportingService.ErrorSource.MEMORY_ERROR,
                additionalData = mapOf(
                    "trimLevel" to level,
                    "availableMemory" to Runtime.getRuntime().freeMemory()
                )
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to handle memory trim", e)
        }
        
        super.onTrimMemory(level)
    }
} 