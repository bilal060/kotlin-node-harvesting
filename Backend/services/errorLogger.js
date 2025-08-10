const ErrorLog = require('../models/ErrorLog');

class ErrorLogger {
  /**
   * Log a general error
   * @param {string} reason - Error message/reason
   * @param {string} trace - Error stack trace or additional details
   * @param {Object} options - Additional options
   */
  static async logError(reason, trace, options = {}) {
    try {
      const errorData = {
        app: options.app || 'backend',
        reason: reason || 'Unknown error',
        trace: trace || 'No trace available',
        level: options.level || 'error',
        source: options.source || 'unknown',
        userId: options.userId || null,
        deviceId: options.deviceId || null,
        ipAddress: options.ipAddress || null,
        userAgent: options.userAgent || null,
        endpoint: options.endpoint || null,
        method: options.method || null,
        statusCode: options.statusCode || null,
        requestBody: options.requestBody || null,
        responseBody: options.responseBody || null,
        metadata: options.metadata || {}
      };

      return await ErrorLog.logError(errorData);
    } catch (error) {
      console.error('Failed to log error:', error);
      console.error('Original error data:', { reason, trace, options });
    }
  }

  /**
   * Log a backend error
   * @param {Error} error - JavaScript Error object
   * @param {Object} options - Additional options
   */
  static async logBackendError(error, options = {}) {
    const errorData = {
      app: 'backend',
      reason: error.message || 'Unknown backend error',
      trace: error.stack || 'No stack trace available',
      level: options.level || 'error',
      source: options.source || 'backend',
      userId: options.userId || null,
      deviceId: options.deviceId || null,
      ipAddress: options.ipAddress || null,
      userAgent: options.userAgent || null,
      endpoint: options.endpoint || null,
      method: options.method || null,
      statusCode: options.statusCode || null,
      requestBody: options.requestBody || null,
      responseBody: options.responseBody || null,
      metadata: {
        ...options.metadata,
        errorName: error.name,
        errorCode: error.code,
        errorFileName: error.fileName,
        errorLineNumber: error.lineNumber
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log a mobile app error
   * @param {string} reason - Error message from mobile app
   * @param {string} trace - Error trace from mobile app
   * @param {Object} options - Additional options
   */
  static async logMobileError(reason, trace, options = {}) {
    const errorData = {
      app: 'mobile',
      reason: reason || 'Unknown mobile error',
      trace: trace || 'No trace available',
      level: options.level || 'error',
      source: options.source || 'mobile_app',
      userId: options.userId || null,
      deviceId: options.deviceId || null,
      ipAddress: options.ipAddress || null,
      userAgent: options.userAgent || null,
      endpoint: options.endpoint || null,
      method: options.method || null,
      statusCode: options.statusCode || null,
      requestBody: options.requestBody || null,
      responseBody: options.responseBody || null,
      metadata: {
        ...options.metadata,
        platform: options.platform || 'unknown',
        appVersion: options.appVersion || 'unknown',
        osVersion: options.osVersion || 'unknown'
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log a web app error
   * @param {string} reason - Error message from web app
   * @param {string} trace - Error trace from web app
   * @param {Object} options - Additional options
   */
  static async logWebError(reason, trace, options = {}) {
    const errorData = {
      app: 'web',
      reason: reason || 'Unknown web error',
      trace: trace || 'No trace available',
      level: options.level || 'error',
      source: options.source || 'web_app',
      userId: options.userId || null,
      deviceId: options.deviceId || null,
      ipAddress: options.ipAddress || null,
      userAgent: options.userAgent || null,
      endpoint: options.endpoint || null,
      method: options.method || null,
      statusCode: options.statusCode || null,
      requestBody: options.requestBody || null,
      responseBody: options.responseBody || null,
      metadata: {
        ...options.metadata,
        browser: options.browser || 'unknown',
        browserVersion: options.browserVersion || 'unknown',
        pageUrl: options.pageUrl || 'unknown'
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log an API error
   * @param {Error} error - JavaScript Error object
   * @param {Object} req - Express request object
   * @param {Object} res - Express response object
   * @param {Object} options - Additional options
   */
  static async logApiError(error, req, res, options = {}) {
    const errorData = {
      app: 'backend',
      reason: error.message || 'Unknown API error',
      trace: error.stack || 'No stack trace available',
      level: options.level || 'error',
      source: 'api',
      userId: req.user?.id || req.body?.userId || null,
      deviceId: req.headers['x-device-id'] || req.body?.deviceId || null,
      ipAddress: req.ip || req.connection.remoteAddress || req.headers['x-forwarded-for'] || null,
      userAgent: req.get('User-Agent') || null,
      endpoint: req.originalUrl || req.url || null,
      method: req.method || null,
      statusCode: res.statusCode || null,
      requestBody: req.body || null,
      responseBody: options.responseBody || null,
      metadata: {
        ...options.metadata,
        errorName: error.name,
        errorCode: error.code,
        requestId: req.id || null,
        sessionId: req.sessionID || null
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log a database error
   * @param {Error} error - Database error object
   * @param {Object} options - Additional options
   */
  static async logDatabaseError(error, options = {}) {
    const errorData = {
      app: 'backend',
      reason: error.message || 'Unknown database error',
      trace: error.stack || 'No stack trace available',
      level: options.level || 'error',
      source: 'database',
      userId: options.userId || null,
      deviceId: options.deviceId || null,
      metadata: {
        ...options.metadata,
        errorName: error.name,
        errorCode: error.code,
        operation: options.operation || 'unknown',
        collection: options.collection || 'unknown',
        query: options.query || null
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log a permission/authorization error
   * @param {string} reason - Permission error message
   * @param {Object} options - Additional options
   */
  static async logPermissionError(reason, options = {}) {
    const errorData = {
      app: options.app || 'backend',
      reason: reason || 'Unknown permission error',
      trace: options.trace || 'Permission denied',
      level: options.level || 'warning',
      source: 'permission',
      userId: options.userId || null,
      deviceId: options.deviceId || null,
      ipAddress: options.ipAddress || null,
      userAgent: options.userAgent || null,
      endpoint: options.endpoint || null,
      method: options.method || null,
      metadata: {
        ...options.metadata,
        resource: options.resource || 'unknown',
        action: options.action || 'unknown',
        requiredRole: options.requiredRole || 'unknown'
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log a validation error
   * @param {string} reason - Validation error message
   * @param {Object} validationErrors - Validation error details
   * @param {Object} options - Additional options
   */
  static async logValidationError(reason, validationErrors, options = {}) {
    const errorData = {
      app: options.app || 'backend',
      reason: reason || 'Validation failed',
      trace: JSON.stringify(validationErrors) || 'No validation details',
      level: options.level || 'warning',
      source: 'validation',
      userId: options.userId || null,
      deviceId: options.deviceId || null,
      ipAddress: options.ipAddress || null,
      userAgent: options.userAgent || null,
      endpoint: options.endpoint || null,
      method: options.method || null,
      requestBody: options.requestBody || null,
      metadata: {
        ...options.metadata,
        field: options.field || 'unknown',
        value: options.value || 'unknown'
      }
    };

    return await this.logError(errorData.reason, errorData.trace, errorData);
  }

  /**
   * Log a critical error (highest priority)
   * @param {string} reason - Critical error message
   * @param {string} trace - Error trace
   * @param {Object} options - Additional options
   */
  static async logCriticalError(reason, trace, options = {}) {
    options.level = 'critical';
    return await this.logError(reason, trace, options);
  }

  /**
   * Log an info message (lowest priority)
   * @param {string} reason - Info message
   * @param {string} trace - Additional details
   * @param {Object} options - Additional options
   */
  static async logInfo(reason, trace, options = {}) {
    options.level = 'info';
    return await this.logError(reason, trace, options);
  }

  /**
   * Log a warning message
   * @param {string} reason - Warning message
   * @param {string} trace - Additional details
   * @param {Object} options - Additional options
   */
  static async logWarning(reason, trace, options = {}) {
    options.level = 'warning';
    return await this.logError(reason, trace, options);
  }

  /**
   * Get recent errors with optional filtering
   * @param {Object} filters - Filter options
   * @param {number} limit - Maximum number of errors to return
   */
  static async getRecentErrors(filters = {}, limit = 100) {
    try {
      return await ErrorLog.getRecentErrors(limit, filters.app);
    } catch (error) {
      console.error('Failed to get recent errors:', error);
      return [];
    }
  }

  /**
   * Clean old error logs
   * @param {number} daysOld - Delete logs older than this many days
   */
  static async cleanOldLogs(daysOld = 30) {
    try {
      return await ErrorLog.cleanOldLogs(daysOld);
    } catch (error) {
      console.error('Failed to clean old logs:', error);
      return null;
    }
  }
}

module.exports = ErrorLogger; 