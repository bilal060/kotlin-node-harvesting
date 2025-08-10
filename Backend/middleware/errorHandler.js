const ErrorLogger = require('../services/errorLogger');

/**
 * Express error handling middleware
 * Automatically logs all errors to the error_logs collection
 */
const errorHandler = async (error, req, res, next) => {
  try {
    // Log the error to our error_logs collection
    await ErrorLogger.logApiError(error, req, res, {
      source: 'express_middleware',
      metadata: {
        middleware: 'errorHandler',
        timestamp: new Date().toISOString()
      }
    });

    // Determine error type and send appropriate response
    let statusCode = 500;
    let message = 'Internal Server Error';

    // Handle specific error types
    if (error.name === 'ValidationError') {
      statusCode = 400;
      message = 'Validation Error';
    } else if (error.name === 'CastError') {
      statusCode = 400;
      message = 'Invalid ID format';
    } else if (error.name === 'MongoError' && error.code === 11000) {
      statusCode = 409;
      message = 'Duplicate key error';
    } else if (error.name === 'UnauthorizedError') {
      statusCode = 401;
      message = 'Unauthorized';
    } else if (error.name === 'ForbiddenError') {
      statusCode = 403;
      message = 'Forbidden';
    } else if (error.name === 'NotFoundError') {
      statusCode = 404;
      message = 'Resource not found';
    } else if (error.status) {
      statusCode = error.status;
      message = error.message || message;
    } else if (error.message) {
      message = error.message;
    }

    // Log additional error details for debugging
    console.error('Error details:', {
      name: error.name,
      message: error.message,
      stack: error.stack,
      url: req.originalUrl,
      method: req.method,
      ip: req.ip,
      userAgent: req.get('User-Agent')
    });

    // Send error response
    res.status(statusCode).json({
      success: false,
      error: {
        message: message,
        statusCode: statusCode,
        timestamp: new Date().toISOString(),
        path: req.originalUrl,
        method: req.method
      }
    });

  } catch (loggingError) {
    // If error logging fails, fall back to console logging
    console.error('Failed to log error:', loggingError);
    console.error('Original error:', error);
    
    // Still send error response to client
    res.status(500).json({
      success: false,
      error: {
        message: 'Internal Server Error',
        statusCode: 500,
        timestamp: new Date().toISOString()
      }
    });
  }
};

/**
 * Async error wrapper middleware
 * Wraps async route handlers to automatically catch and log errors
 */
const asyncErrorHandler = (fn) => {
  return (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
};

/**
 * 404 handler for unmatched routes
 */
const notFoundHandler = async (req, res, next) => {
  try {
    // Log 404 errors
    await ErrorLogger.logError(
      'Route not found',
      `Requested route: ${req.method} ${req.originalUrl}`,
      {
        app: 'backend',
        level: 'warning',
        source: 'route_handler',
        ipAddress: req.ip,
        userAgent: req.get('User-Agent'),
        endpoint: req.originalUrl,
        method: req.method,
        statusCode: 404
      }
    );

    res.status(404).json({
      success: false,
      error: {
        message: 'Route not found',
        statusCode: 404,
        timestamp: new Date().toISOString(),
        path: req.originalUrl,
        method: req.method
      }
    });
  } catch (error) {
    // Fallback response if logging fails
    res.status(404).json({
      success: false,
      error: {
        message: 'Route not found',
        statusCode: 404,
        timestamp: new Date().toISOString()
      }
    });
  }
};

/**
 * Request logging middleware
 * Logs all incoming requests for debugging purposes
 */
const requestLogger = async (req, res, next) => {
  const start = Date.now();
  
  // Log request start
  await ErrorLogger.logInfo(
    'Request started',
    `${req.method} ${req.originalUrl}`,
    {
      app: 'backend',
      source: 'request_logger',
      ipAddress: req.ip,
      userAgent: req.get('User-Agent'),
      endpoint: req.originalUrl,
      method: req.method,
      metadata: {
        requestId: req.id || 'unknown',
        timestamp: new Date().toISOString()
      }
    }
  );

  // Override res.end to log response
  const originalEnd = res.end;
  res.end = function(chunk, encoding) {
    const duration = Date.now() - start;
    
    // Log request completion
    ErrorLogger.logInfo(
      'Request completed',
      `${req.method} ${req.originalUrl} - ${res.statusCode} (${duration}ms)`,
      {
        app: 'backend',
        source: 'request_logger',
        ipAddress: req.ip,
        userAgent: req.get('User-Agent'),
        endpoint: req.originalUrl,
        method: req.method,
        statusCode: res.statusCode,
        metadata: {
          requestId: req.id || 'unknown',
          duration: duration,
          timestamp: new Date().toISOString()
        }
      }
    );

    originalEnd.call(this, chunk, encoding);
  };

  next();
};

module.exports = {
  errorHandler,
  asyncErrorHandler,
  notFoundHandler,
  requestLogger
}; 