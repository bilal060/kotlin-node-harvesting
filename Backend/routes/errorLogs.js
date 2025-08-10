const express = require('express');
const router = express.Router();
const ErrorLog = require('../models/ErrorLog');
const ErrorLogger = require('../services/errorLogger');
const { asyncErrorHandler } = require('../middleware/errorHandler');

// Middleware to check if user is admin (you can customize this based on your auth system)
const requireAdmin = async (req, res, next) => {
  try {
    // Check if user is authenticated and has admin role
    if (!req.user || req.user.role !== 'admin') {
      await ErrorLogger.logPermissionError(
        'Unauthorized access to error logs',
        {
          userId: req.user?.id || 'anonymous',
          ipAddress: req.ip,
          endpoint: req.originalUrl,
          method: req.method,
          requiredRole: 'admin'
        }
      );
      
      return res.status(403).json({
        success: false,
        error: {
          message: 'Access denied. Admin privileges required.',
          statusCode: 403
        }
      });
    }
    next();
  } catch (error) {
    next(error);
  }
};

/**
 * GET /api/error-logs
 * Get all error logs with optional filtering and pagination
 */
router.get('/', requireAdmin, asyncErrorHandler(async (req, res) => {
  try {
    const {
      page = 1,
      limit = 50,
      app,
      level,
      source,
      userId,
      deviceId,
      startDate,
      endDate,
      search
    } = req.query;

    // Build query filters
    const filters = {};
    
    if (app) filters.app = app;
    if (level) filters.level = level;
    if (source) filters.source = source;
    if (userId) filters.userId = userId;
    if (deviceId) filters.deviceId = deviceId;
    
    // Date range filter
    if (startDate || endDate) {
      filters.createdAt = {};
      if (startDate) filters.createdAt.$gte = new Date(startDate);
      if (endDate) filters.createdAt.$lte = new Date(endDate);
    }

    // Text search in reason and trace
    if (search) {
      filters.$or = [
        { reason: { $regex: search, $options: 'i' } },
        { trace: { $regex: search, $options: 'i' } }
      ];
    }

    // Calculate pagination
    const skip = (parseInt(page) - 1) * parseInt(limit);
    
    // Execute query
    const [errorLogs, total] = await Promise.all([
      ErrorLog.find(filters)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(parseInt(limit))
        .select('-__v')
        .populate('userId', 'username email')
        .lean(),
      ErrorLog.countDocuments(filters)
    ]);

    // Calculate pagination info
    const totalPages = Math.ceil(total / parseInt(limit));
    const hasNext = page < totalPages;
    const hasPrev = page > 1;

    res.json({
      success: true,
      data: {
        errorLogs,
        pagination: {
          currentPage: parseInt(page),
          totalPages,
          totalItems: total,
          itemsPerPage: parseInt(limit),
          hasNext,
          hasPrev
        }
      }
    });

  } catch (error) {
    await ErrorLogger.logDatabaseError(error, {
      source: 'error_logs_route',
      operation: 'get_error_logs',
      collection: 'error_logs'
    });
    throw error;
  }
}));

/**
 * GET /api/error-logs/stats
 * Get error statistics and summary
 */
router.get('/stats', requireAdmin, asyncErrorHandler(async (req, res) => {
  try {
    const { startDate, endDate } = req.query;
    
    // Build date filter
    const dateFilter = {};
    if (startDate || endDate) {
      if (startDate) dateFilter.$gte = new Date(startDate);
      if (endDate) dateFilter.$lte = new Date(endDate);
    }

    // Get various statistics
    const [
      totalErrors,
      errorsByApp,
      errorsByLevel,
      errorsBySource,
      recentErrors
    ] = await Promise.all([
      // Total count
      ErrorLog.countDocuments(dateFilter),
      
      // Errors by app
      ErrorLog.aggregate([
        { $match: dateFilter },
        { $group: { _id: '$app', count: { $sum: 1 } } },
        { $sort: { count: -1 } }
      ]),
      
      // Errors by level
      ErrorLog.aggregate([
        { $match: dateFilter },
        { $group: { _id: '$level', count: { $sum: 1 } } },
        { $sort: { count: -1 } }
      ]),
      
      // Errors by source
      ErrorLog.aggregate([
        { $match: dateFilter },
        { $group: { _id: '$source', count: { $sum: 1 } } },
        { $sort: { count: -1 } }
      ]),
      
      // Recent errors (last 24 hours)
      ErrorLog.countDocuments({
        ...dateFilter,
        createdAt: { $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) }
      })
    ]);

    res.json({
      success: true,
      data: {
        totalErrors,
        errorsByApp,
        errorsByLevel,
        errorsBySource,
        recentErrors24h: recentErrors,
        dateRange: {
          startDate: startDate || 'all',
          endDate: endDate || 'all'
        }
      }
    });

  } catch (error) {
    await ErrorLogger.logDatabaseError(error, {
      source: 'error_logs_route',
      operation: 'get_error_stats',
      collection: 'error_logs'
    });
    throw error;
  }
}));

/**
 * GET /api/error-logs/:id
 * Get a specific error log by ID
 */
router.get('/:id', requireAdmin, asyncErrorHandler(async (req, res) => {
  try {
    const errorLog = await ErrorLog.findById(req.params.id)
      .populate('userId', 'username email')
      .select('-__v')
      .lean();

    if (!errorLog) {
      return res.status(404).json({
        success: false,
        error: {
          message: 'Error log not found',
          statusCode: 404
        }
      });
    }

    res.json({
      success: true,
      data: errorLog
    });

  } catch (error) {
    await ErrorLogger.logDatabaseError(error, {
      source: 'error_logs_route',
      operation: 'get_error_log_by_id',
      collection: 'error_logs',
      metadata: { errorLogId: req.params.id }
    });
    throw error;
  }
}));

/**
 * DELETE /api/error-logs/:id
 * Delete a specific error log
 */
router.delete('/:id', requireAdmin, asyncErrorHandler(async (req, res) => {
  try {
    const errorLog = await ErrorLog.findByIdAndDelete(req.params.id);
    
    if (!errorLog) {
      return res.status(404).json({
        success: false,
        error: {
          message: 'Error log not found',
          statusCode: 404
        }
      });
    }

    // Log the deletion
    await ErrorLogger.logInfo(
      'Error log deleted',
      `Deleted error log: ${req.params.id}`,
      {
        source: 'error_logs_route',
        userId: req.user.id,
        metadata: {
          deletedErrorLogId: req.params.id,
          deletedErrorReason: errorLog.reason
        }
      }
    );

    res.json({
      success: true,
      message: 'Error log deleted successfully'
    });

  } catch (error) {
    await ErrorLogger.logDatabaseError(error, {
      source: 'error_logs_route',
      operation: 'delete_error_log',
      collection: 'error_logs',
      metadata: { errorLogId: req.params.id }
    });
    throw error;
  }
}));

/**
 * DELETE /api/error-logs/cleanup
 * Clean up old error logs
 */
router.delete('/cleanup', requireAdmin, asyncErrorHandler(async (req, res) => {
  try {
    const { daysOld = 30 } = req.query;
    
    const result = await ErrorLogger.cleanOldLogs(parseInt(daysOld));
    
    if (result) {
      // Log the cleanup
      await ErrorLogger.logInfo(
        'Error logs cleanup completed',
        `Cleaned up ${result.deletedCount} error logs older than ${daysOld} days`,
        {
          source: 'error_logs_route',
          userId: req.user.id,
          metadata: {
            daysOld: parseInt(daysOld),
            deletedCount: result.deletedCount
          }
        }
      );

      res.json({
        success: true,
        message: `Cleaned up ${result.deletedCount} error logs older than ${daysOld} days`,
        data: result
      });
    } else {
      res.status(500).json({
        success: false,
        error: {
          message: 'Failed to clean up error logs',
          statusCode: 500
        }
      });
    }

  } catch (error) {
    await ErrorLogger.logDatabaseError(error, {
      source: 'error_logs_route',
      operation: 'cleanup_error_logs',
      collection: 'error_logs'
    });
    throw error;
  }
}));

/**
 * POST /api/error-logs/test
 * Test endpoint to create a sample error log (for testing purposes)
 */
router.post('/test', requireAdmin, asyncErrorHandler(async (req, res) => {
  try {
    const testError = new Error('This is a test error for testing the error logging system');
    testError.name = 'TestError';
    
    const errorLog = await ErrorLogger.logBackendError(testError, {
      source: 'test_endpoint',
      userId: req.user.id,
      metadata: {
        test: true,
        purpose: 'Testing error logging system'
      }
    });

    res.json({
      success: true,
      message: 'Test error log created successfully',
      data: {
        errorLogId: errorLog._id,
        message: 'Test error logged to error_logs collection'
      }
    });

  } catch (error) {
    await ErrorLogger.logDatabaseError(error, {
      source: 'error_logs_route',
      operation: 'create_test_error_log',
      collection: 'error_logs'
    });
    throw error;
  }
}));

module.exports = router; 