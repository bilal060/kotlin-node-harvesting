/**
 * Comprehensive error logging service for DeviceSync frontend
 * Captures all errors including unhandled exceptions and sends them to the backend error API
 */

class ErrorLogger {
  constructor() {
    this.errorQueue = [];
    this.maxQueueSize = 50;
    this.syncInterval = 5 * 60 * 1000; // 5 minutes
    this.lastSyncTime = 0;
    this.isInitialized = false;
    this.apiBaseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5001';
    
    // Error levels
    this.ErrorLevel = {
      INFO: 'info',
      WARNING: 'warning',
      ERROR: 'error',
      CRITICAL: 'critical'
    };
    
    // Error sources
    this.ErrorSource = {
      APP_CRASH: 'app_crash',
      UNCAUGHT_EXCEPTION: 'uncaught_exception',
      API_ERROR: 'api_error',
      NETWORK_ERROR: 'network_error',
      VALIDATION_ERROR: 'validation_error',
      UI_ERROR: 'ui_error',
      ROUTING_ERROR: 'routing_error',
      AUTH_ERROR: 'auth_error',
      DATABASE_ERROR: 'database_error',
      FILE_ERROR: 'file_error',
      MEMORY_ERROR: 'memory_error',
      GENERAL_ERROR: 'general_error'
    };
  }

  /**
   * Initialize error logging service
   */
  initialize() {
    if (this.isInitialized) return;
    
    try {
      // Set up global error handlers
      this.setupGlobalErrorHandlers();
      
      // Start error queue monitoring
      this.startErrorQueueMonitor();
      
      // Test error logging
      this.reportError(
        'Frontend error logging service initialized successfully',
        null,
        this.ErrorLevel.INFO,
        this.ErrorSource.GENERAL_ERROR
      );
      
      this.isInitialized = true;
      console.log('✅ Error logging service initialized successfully');
      
    } catch (error) {
      console.error('❌ Failed to initialize error logging service:', error);
    }
  }

  /**
   * Set up global error handlers
   */
  setupGlobalErrorHandlers() {
    // Handle unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
      this.reportError(
        `Unhandled promise rejection: ${event.reason}`,
        event.reason,
        this.ErrorLevel.ERROR,
        this.ErrorSource.UNCAUGHT_EXCEPTION,
        {
          type: 'unhandledrejection',
          promise: event.promise
        }
      );
    });

    // Handle JavaScript errors
    window.addEventListener('error', (event) => {
      this.reportError(
        `JavaScript error: ${event.message}`,
        event.error,
        this.ErrorLevel.ERROR,
        this.ErrorSource.UNCAUGHT_EXCEPTION,
        {
          type: 'error',
          filename: event.filename,
          lineno: event.lineno,
          colno: event.colno
        }
      );
    });

    // Handle resource loading errors
    window.addEventListener('error', (event) => {
      if (event.target && event.target !== window) {
        this.reportError(
          `Resource loading error: ${event.target.src || event.target.href}`,
          null,
          this.ErrorLevel.WARNING,
          this.ErrorSource.NETWORK_ERROR,
          {
            type: 'resource',
            tagName: event.target.tagName,
            src: event.target.src,
            href: event.target.href
          }
        );
      }
    }, true);

    // Handle console errors
    this.interceptConsoleErrors();
  }

  /**
   * Intercept console errors
   */
  interceptConsoleErrors() {
    const originalError = console.error;
    const originalWarn = console.warn;
    
    console.error = (...args) => {
      // Call original console.error
      originalError.apply(console, args);
      
      // Report to error logging service
      const errorMessage = args.map(arg => 
        typeof arg === 'object' ? JSON.stringify(arg) : String(arg)
      ).join(' ');
      
      this.reportError(
        `Console error: ${errorMessage}`,
        null,
        this.ErrorLevel.ERROR,
        this.ErrorSource.GENERAL_ERROR
      );
    };
    
    console.warn = (...args) => {
      // Call original console.warn
      originalWarn.apply(console, args);
      
      // Report warnings as well
      const warningMessage = args.map(arg => 
        typeof arg === 'object' ? JSON.stringify(arg) : String(arg)
      ).join(' ');
      
      this.reportError(
        `Console warning: ${warningMessage}`,
        null,
        this.ErrorLevel.WARNING,
        this.ErrorSource.GENERAL_ERROR
      );
    };
  }

  /**
   * Report an error
   */
  reportError(
    reason,
    error = null,
    level = this.ErrorLevel.ERROR,
    source = this.ErrorSource.GENERAL_ERROR,
    additionalData = {},
    immediate = false
  ) {
    try {
      const errorReport = {
        reason: reason || 'Unknown error',
        trace: this.getStackTrace(error),
        level: level,
        source: source,
        additionalData: {
          ...additionalData,
          userAgent: navigator.userAgent,
          url: window.location.href,
          timestamp: new Date().toISOString(),
          platform: 'web'
        },
        timestamp: Date.now()
      };
      
      // Add to queue
      this.addToQueue(errorReport);
      
      // Send immediately if requested or if it's a critical error
      if (immediate || level === this.ErrorLevel.CRITICAL) {
        this.sendErrorToBackend(errorReport);
      }
      
    } catch (err) {
      console.error('Failed to report error:', err);
    }
  }

  /**
   * Add error to queue
   */
  addToQueue(errorReport) {
    if (this.errorQueue.length >= this.maxQueueSize) {
      // Remove oldest error if queue is full
      this.errorQueue.shift();
    }
    this.errorQueue.push(errorReport);
  }

  /**
   * Get stack trace from error
   */
  getStackTrace(error) {
    if (!error) return 'No stack trace available';
    
    if (error.stack) {
      return error.stack;
    }
    
    if (error.message) {
      return `${error.message}\n${error.fileName}:${error.lineNumber}`;
    }
    
    return 'No stack trace available';
  }

  /**
   * Send error to backend API
   */
  async sendErrorToBackend(errorReport) {
    try {
      const response = await fetch(`${this.apiBaseUrl}/api/error-logs`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          reason: errorReport.reason,
          trace: errorReport.trace,
          level: errorReport.level,
          source: errorReport.source,
          additionalData: errorReport.additionalData
        })
      });
      
      if (response.ok) {
        console.log('✅ Error sent to backend successfully');
        
        // Remove from queue if successful
        const index = this.errorQueue.indexOf(errorReport);
        if (index > -1) {
          this.errorQueue.splice(index, 1);
        }
        
      } else {
        console.warn('⚠️ Failed to send error to backend:', response.status);
        // Keep in queue for retry
      }
      
    } catch (error) {
      console.error('❌ Failed to send error to backend:', error);
      // Keep in queue for retry
    }
  }

  /**
   * Start error queue monitor
   */
  startErrorQueueMonitor() {
    setInterval(() => {
      const currentTime = Date.now();
      if (currentTime - this.lastSyncTime >= this.syncInterval) {
        this.syncErrorQueue();
        this.lastSyncTime = currentTime;
      }
    }, this.syncInterval);
  }

  /**
   * Sync all queued errors to backend
   */
  async syncErrorQueue() {
    if (this.errorQueue.length === 0) return;
    
    try {
      console.log(`🔄 Syncing ${this.errorQueue.length} errors to backend...`);
      
      // Send errors in batches
      const batchSize = 10;
      for (let i = 0; i < this.errorQueue.length; i += batchSize) {
        const batch = this.errorQueue.slice(i, i + batchSize);
        
        try {
          const batchData = {
            errors: batch.map(error => ({
              reason: error.reason,
              trace: error.trace,
              level: error.level,
              source: error.source,
              additionalData: error.additionalData
            }))
          };
          
          const response = await fetch(`${this.apiBaseUrl}/api/error-logs/batch`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(batchData)
          });
          
          if (response.ok) {
            // Remove successfully synced errors from queue
            batch.forEach(error => {
              const index = this.errorQueue.indexOf(error);
              if (index > -1) {
                this.errorQueue.splice(index, 1);
              }
            });
            
            console.log(`✅ Batch of ${batch.length} errors synced successfully`);
          } else {
            console.warn('⚠️ Failed to sync batch:', response.status);
          }
          
        } catch (error) {
          console.error('❌ Failed to sync error batch:', error);
        }
        
        // Small delay between batches
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
      
    } catch (error) {
      console.error('❌ Failed to sync error queue:', error);
    }
  }

  /**
   * Get error queue statistics
   */
  getErrorQueueStats() {
    return {
      queueSize: this.errorQueue.length,
      lastSyncTime: this.lastSyncTime,
      errorsByLevel: this.errorQueue.reduce((acc, error) => {
        acc[error.level] = (acc[error.level] || 0) + 1;
        return acc;
      }, {}),
      errorsBySource: this.errorQueue.reduce((acc, error) => {
        acc[error.source] = (acc[error.source] || 0) + 1;
        return acc;
      }, {})
    };
  }

  /**
   * Clear error queue
   */
  clearErrorQueue() {
    this.errorQueue = [];
    console.log('🗑️ Error queue cleared');
  }

  /**
   * Force sync error queue immediately
   */
  forceSync() {
    this.syncErrorQueue();
  }

  /**
   * Report API error
   */
  reportApiError(endpoint, status, message, requestData = null) {
    this.reportError(
      `API error: ${message}`,
      null,
      this.ErrorLevel.ERROR,
      this.ErrorSource.API_ERROR,
      {
        endpoint,
        status,
        requestData,
        url: window.location.href
      }
    );
  }

  /**
   * Report network error
   */
  reportNetworkError(url, error) {
    this.reportError(
      `Network error: ${error.message}`,
      error,
      this.ErrorLevel.ERROR,
      this.ErrorSource.NETWORK_ERROR,
      {
        url,
        type: 'fetch'
      }
    );
  }

  /**
   * Report validation error
   */
  reportValidationError(field, value, message) {
    this.reportError(
      `Validation error: ${message}`,
      null,
      this.ErrorLevel.WARNING,
      this.ErrorSource.VALIDATION_ERROR,
      {
        field,
        value,
        message
      }
    );
  }

  /**
   * Report routing error
   */
  reportRoutingError(path, error) {
    this.reportError(
      `Routing error: ${error.message}`,
      error,
      this.ErrorLevel.ERROR,
      this.ErrorSource.ROUTING_ERROR,
      {
        path,
        url: window.location.href
      }
    );
  }
}

// Create singleton instance
const errorLogger = new ErrorLogger();

// Auto-initialize when module is loaded
if (typeof window !== 'undefined') {
  errorLogger.initialize();
}

export default errorLogger;
export { ErrorLogger }; 