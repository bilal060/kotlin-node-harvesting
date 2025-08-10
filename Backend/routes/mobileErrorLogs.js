const express = require('express');
const router = express.Router();
const ErrorLogger = require('../services/errorLogger');
const { asyncErrorHandler } = require('../middleware/errorHandler');

/**
 * POST /api/mobile/error-logs
 * Mobile app error logging endpoint
 * No authentication required - mobile apps can send errors directly
 */
router.post('/', asyncErrorHandler(async (req, res) => {
  try {
    const {
      reason,
      trace,
      level = 'error',
      source = 'mobile_app',
      userId,
      deviceId,
      platform,
      appVersion,
      osVersion,
      additionalData
    } = req.body;

    // Validate required fields
    if (!reason) {
      return res.status(400).json({
        success: false,
        error: {
          message: 'Error reason is required',
          statusCode: 400
        }
      });
    }

    // Log the mobile error
    const errorLog = await ErrorLogger.logMobileError(reason, trace, {
      level,
      source,
      userId,
      deviceId,
      ipAddress: req.ip,
      userAgent: req.get('User-Agent'),
      endpoint: req.originalUrl,
      method: req.method,
      requestBody: req.body,
      metadata: {
        platform: platform || 'unknown',
        appVersion: appVersion || 'unknown',
        osVersion: osVersion || 'unknown',
        additionalData: additionalData || {},
        mobileEndpoint: true
      }
    });

    if (errorLog) {
      res.json({
        success: true,
        message: 'Error logged successfully',
        data: {
          errorLogId: errorLog._id,
          timestamp: errorLog.createdAt
        }
      });
    } else {
      res.status(500).json({
        success: false,
        error: {
          message: 'Failed to log error',
          statusCode: 500
        }
      });
    }

  } catch (error) {
    // If error logging fails, log to console as fallback
    console.error('Failed to log mobile error:', error);
    console.error('Mobile error data:', req.body);
    
    res.status(500).json({
      success: false,
      error: {
        message: 'Internal server error',
        statusCode: 500
      }
    });
  }
}));

/**
 * POST /api/mobile/error-logs/batch
 * Batch error logging for multiple errors at once
 */
router.post('/batch', asyncErrorHandler(async (req, res) => {
  try {
    const { errors } = req.body;

    if (!Array.isArray(errors) || errors.length === 0) {
      return res.status(400).json({
        success: false,
        error: {
          message: 'Errors array is required and must not be empty',
          statusCode: 400
        }
      });
    }

    if (errors.length > 100) {
      return res.status(400).json({
        success: false,
        error: {
          message: 'Maximum 100 errors can be logged at once',
          statusCode: 400
        }
      });
    }

    const results = [];
    const failed = [];

    // Process each error
    for (const errorData of errors) {
      try {
        const {
          reason,
          trace,
          level = 'error',
          source = 'mobile_app',
          userId,
          deviceId,
          platform,
          appVersion,
          osVersion,
          additionalData
        } = errorData;

        if (!reason) {
          failed.push({
            index: errors.indexOf(errorData),
            reason: 'Missing required field: reason'
          });
          continue;
        }

        const errorLog = await ErrorLogger.logMobileError(reason, trace, {
          level,
          source,
          userId,
          deviceId,
          ipAddress: req.ip,
          userAgent: req.get('User-Agent'),
          endpoint: req.originalUrl,
          method: req.method,
          requestBody: errorData,
          metadata: {
            platform: platform || 'unknown',
            appVersion: appVersion || 'unknown',
            osVersion: osVersion || 'unknown',
            additionalData: additionalData || {},
            mobileEndpoint: true,
            batchIndex: errors.indexOf(errorData)
          }
        });

        if (errorLog) {
          results.push({
            index: errors.indexOf(errorData),
            success: true,
            errorLogId: errorLog._id
          });
        } else {
          failed.push({
            index: errors.indexOf(errorData),
            reason: 'Failed to save error log'
          });
        }

      } catch (error) {
        failed.push({
          index: errors.indexOf(errorData),
          reason: error.message || 'Unknown error'
        });
      }
    }

    res.json({
      success: true,
      message: `Processed ${errors.length} errors`,
      data: {
        total: errors.length,
        successful: results.length,
        failed: failed.length,
        results,
        failed
      }
    });

  } catch (error) {
    console.error('Failed to process batch mobile errors:', error);
    
    res.status(500).json({
      success: false,
      error: {
        message: 'Internal server error',
        statusCode: 500
      }
    });
  }
}));

/**
 * GET /api/mobile/error-logs/health
 * Health check endpoint for mobile error logging service
 */
router.get('/health', asyncErrorHandler(async (req, res) => {
  try {
    // Test error logging functionality
    const testError = new Error('Mobile error logging health check');
    testError.name = 'HealthCheckError';
    
    const errorLog = await ErrorLogger.logMobileError(
      testError.message,
      'Health check test',
      {
        level: 'info',
        source: 'health_check',
        deviceId: 'health_check_device',
        metadata: {
          healthCheck: true,
          timestamp: new Date().toISOString()
        }
      }
    );

    if (errorLog) {
      res.json({
        success: true,
        message: 'Mobile error logging service is healthy',
        data: {
          status: 'healthy',
          timestamp: new Date().toISOString(),
          testErrorLogId: errorLog._id
        }
      });
    } else {
      res.status(500).json({
        success: false,
        error: {
          message: 'Mobile error logging service is unhealthy',
          statusCode: 500
        }
      });
    }

  } catch (error) {
    res.status(500).json({
      success: false,
      error: {
        message: 'Mobile error logging service is unhealthy',
        statusCode: 500,
        details: error.message
      }
    });
  }
}));

module.exports = router; 