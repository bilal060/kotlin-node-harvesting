package com.devicesync.app.services

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.devicesync.app.api.ApiService
import com.devicesync.app.api.RetrofitClient
import com.devicesync.app.utils.DeviceInfoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive error reporting service for DeviceSync mobile app
 * Captures all errors including crashes and sends them to the backend error API
 */
class ErrorReportingService(
    private val context: Context,
    private val apiService: ApiService = RetrofitClient.apiService
) {
    
    companion object {
        private const val TAG = "ErrorReportingService"
        private const val MAX_ERROR_QUEUE_SIZE = 100
        private const val ERROR_SYNC_INTERVAL = 5 * 60 * 1000L // 5 minutes
        
        // Singleton instance
        @Volatile
        private var INSTANCE: ErrorReportingService? = null
        
        fun getInstance(context: Context): ErrorReportingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ErrorReportingService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Error queue for offline storage
    private val errorQueue = mutableListOf<ErrorReport>()
    private var lastSyncTime = 0L
    
    // Error levels
    enum class ErrorLevel(val value: String) {
        INFO("info"),
        WARNING("warning"),
        ERROR("error"),
        CRITICAL("critical")
    }
    
    // Error sources
    enum class ErrorSource(val value: String) {
        APP_CRASH("app_crash"),
        UNCAUGHT_EXCEPTION("uncaught_exception"),
        API_ERROR("api_error"),
        PERMISSION_ERROR("permission_error"),
        DATA_SYNC_ERROR("data_sync_error"),
        NETWORK_ERROR("network_error"),
        DATABASE_ERROR("database_error"),
        UI_ERROR("ui_error"),
        SERVICE_ERROR("service_error"),
        BACKGROUND_TASK_ERROR("background_task_error"),
        ACCESSIBILITY_ERROR("accessibility_error"),
        NOTIFICATION_ERROR("notification_error"),
        FILE_ERROR("file_error"),
        MEMORY_ERROR("memory_error"),
        BATTERY_ERROR("battery_error"),
        LOCATION_ERROR("location_error"),
        BLUETOOTH_ERROR("bluetooth_error"),
        CAMERA_ERROR("camera_error"),
        AUDIO_ERROR("audio_error"),
        GENERAL_ERROR("general_error")
    }
    
    /**
     * Data class for error reports
     */
    data class ErrorReport(
        val reason: String,
        val trace: String,
        val level: ErrorLevel = ErrorLevel.ERROR,
        val source: ErrorSource = ErrorSource.GENERAL_ERROR,
        val userId: String? = null,
        val deviceId: String? = null,
        val additionalData: Map<String, Any> = emptyMap(),
        val timestamp: Long = System.currentTimeMillis(),
        val isOffline: Boolean = false
    )
    
    /**
     * Initialize error reporting service
     */
    fun initialize() {
        try {
            // Set up global uncaught exception handler
            setupGlobalExceptionHandler()
            
            // Set up error queue monitoring
            startErrorQueueMonitor()
            
            Log.i(TAG, "✅ Error reporting service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize error reporting service", e)
        }
    }
    
    /**
     * Set up global uncaught exception handler to catch crashes
     */
    private fun setupGlobalExceptionHandler() {
        try {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    // Log the crash
                    Log.e(TAG, "🚨 UNCAUGHT EXCEPTION in thread: ${thread.name}", throwable)
                    
                    // Create crash report
                    val crashReport = ErrorReport(
                        reason = throwable.message ?: "Unknown crash",
                        trace = getStackTrace(throwable),
                        level = ErrorLevel.CRITICAL,
                        source = ErrorSource.APP_CRASH,
                        additionalData = mapOf(
                            "threadName" to thread.name,
                            "threadId" to thread.id,
                            "isMainThread" to (thread == context.mainLooper.thread)
                        )
                    )
                    
                    // Report the crash immediately
                    reportError(crashReport, immediate = true)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to handle uncaught exception", e)
                } finally {
                    // Call the default handler
                    defaultHandler?.uncaughtException(thread, throwable)
                }
            }
            
            Log.i(TAG, "✅ Global exception handler set up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to set up global exception handler", e)
        }
    }
    
    /**
     * Report an error (can be called from anywhere in the app)
     */
    fun reportError(
        reason: String,
        throwable: Throwable? = null,
        level: ErrorLevel = ErrorLevel.ERROR,
        source: ErrorSource = ErrorSource.GENERAL_ERROR,
        userId: String? = null,
        deviceId: String? = null,
        additionalData: Map<String, Any> = emptyMap(),
        immediate: Boolean = false
    ) {
        try {
            val trace = throwable?.let { getStackTrace(it) } ?: "No stack trace available"
            
            val errorReport = ErrorReport(
                reason = reason,
                trace = trace,
                level = level,
                source = source,
                userId = userId,
                deviceId = deviceId,
                additionalData = additionalData
            )
            
            reportError(errorReport, immediate)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create error report", e)
        }
    }
    
    /**
     * Report an error using ErrorReport object
     */
    fun reportError(errorReport: ErrorReport, immediate: Boolean = false) {
        try {
            // Add to queue
            synchronized(errorQueue) {
                if (errorQueue.size >= MAX_ERROR_QUEUE_SIZE) {
                    // Remove oldest error if queue is full
                    errorQueue.removeAt(0)
                }
                errorQueue.add(errorReport)
            }
            
            Log.d(TAG, "📝 Error queued: ${errorReport.reason} (${errorReport.source.value})")
            
            // Send immediately if requested or if it's a critical error
            if (immediate || errorReport.level == ErrorLevel.CRITICAL) {
                sendErrorToBackend(errorReport)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to queue error report", e)
        }
    }
    
    /**
     * Send error to backend API
     */
    private fun sendErrorToBackend(errorReport: ErrorReport) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = errorReport.deviceId ?: DeviceInfoUtils.getDeviceId(context)
                
                val errorData = JSONObject().apply {
                    put("reason", errorReport.reason)
                    put("trace", errorReport.trace)
                    put("level", errorReport.level.value)
                    put("source", errorReport.source.value)
                    put("userId", errorReport.userId ?: JSONObject.NULL)
                    put("deviceId", deviceId)
                    put("platform", "android")
                    put("appVersion", getAppVersion())
                    put("osVersion", Build.VERSION.RELEASE)
                    put("additionalData", JSONObject(errorReport.additionalData))
                }
                
                val response = apiService.logMobileError(errorData.toString())
                
                if (response.isSuccessful) {
                    Log.i(TAG, "✅ Error sent to backend successfully")
                    
                    // Remove from queue if successful
                    synchronized(errorQueue) {
                        errorQueue.remove(errorReport)
                    }
                    
                } else {
                    Log.w(TAG, "⚠️ Failed to send error to backend: ${response.code()}")
                    // Keep in queue for retry
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send error to backend", e)
                // Keep in queue for retry
            }
        }
    }
    
    /**
     * Start error queue monitor for periodic syncing
     */
    private fun startErrorQueueMonitor() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    // Wait for sync interval
                    kotlinx.coroutines.delay(ERROR_SYNC_INTERVAL)
                    
                    // Check if it's time to sync
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastSyncTime >= ERROR_SYNC_INTERVAL) {
                        syncErrorQueue()
                        lastSyncTime = currentTime
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in error queue monitor", e)
                }
            }
        }
    }
    
    /**
     * Sync all queued errors to backend
     */
    private suspend fun syncErrorQueue() {
        try {
            val errorsToSync = synchronized(errorQueue) {
                if (errorQueue.isEmpty()) return
                errorQueue.toList()
            }
            
            if (errorsToSync.isEmpty()) return
            
            Log.i(TAG, "🔄 Syncing ${errorsToSync.size} errors to backend...")
            
            // Send errors in batches
            val batchSize = 10
            for (i in errorsToSync.indices step batchSize) {
                val batch = errorsToSync.subList(i, minOf(i + batchSize, errorsToSync.size))
                
                try {
                    val batchData = JSONObject().apply {
                        put("errors", batch.map { error ->
                            JSONObject().apply {
                                put("reason", error.reason)
                                put("trace", error.trace)
                                put("level", error.level.value)
                                put("source", error.source.value)
                                put("userId", error.userId ?: JSONObject.NULL)
                                put("deviceId", error.deviceId ?: DeviceInfoUtils.getDeviceId(context))
                                put("platform", "android")
                                put("appVersion", getAppVersion())
                                put("osVersion", Build.VERSION.RELEASE)
                                put("additionalData", JSONObject(error.additionalData))
                            }
                        })
                    }
                    
                    val response = apiService.logMobileErrorBatch(batchData.toString())
                    
                    if (response.isSuccessful) {
                        // Remove successfully synced errors from queue
                        synchronized(errorQueue) {
                            errorQueue.removeAll(batch.toSet())
                        }
                        Log.i(TAG, "✅ Batch of ${batch.size} errors synced successfully")
                    } else {
                        Log.w(TAG, "⚠️ Failed to sync batch: ${response.code()}")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to sync error batch", e)
                }
                
                // Small delay between batches
                kotlinx.coroutines.delay(1000)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sync error queue", e)
        }
    }
    
    /**
     * Get stack trace as string
     */
    private fun getStackTrace(throwable: Throwable): String {
        return try {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            throwable.printStackTrace(printWriter)
            stringWriter.toString()
        } catch (e: Exception) {
            "Failed to get stack trace: ${e.message}"
        }
    }
    
    /**
     * Get app version
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }
    
    /**
     * Get error queue statistics
     */
    fun getErrorQueueStats(): Map<String, Any> {
        return synchronized(errorQueue) {
            mapOf(
                "queueSize" to errorQueue.size,
                "lastSyncTime" to lastSyncTime,
                "errorsByLevel" to errorQueue.groupBy { it.level.value }.mapValues { it.value.size },
                "errorsBySource" to errorQueue.groupBy { it.source.value }.mapValues { it.value.size }
            )
        }
    }
    
    /**
     * Clear error queue (use with caution)
     */
    fun clearErrorQueue() {
        synchronized(errorQueue) {
            errorQueue.clear()
        }
        Log.i(TAG, "🗑️ Error queue cleared")
    }
    
    /**
     * Force sync error queue immediately
     */
    fun forceSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncErrorQueue()
        }
    }
} 