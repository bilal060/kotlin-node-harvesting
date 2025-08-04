const express = require('express');
const router = express.Router();
const {
    getQueueStatus,
    getQueueItemStatus,
    triggerQueueProcessing,
    stopQueueProcessing,
    clearFailedQueueItems
} = require('../middleware/queueMiddleware');
const Queue = require('../models/Queue');
const colors = require('colors');

/**
 * @route   GET /api/queue/status
 * @desc    Get queue status and statistics
 * @access  Public
 */
router.get('/status', getQueueStatus);

/**
 * @route   GET /api/queue/items
 * @desc    Get all queue items with pagination
 * @access  Public
 */
router.get('/items', async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const status = req.query.status;
        const deviceId = req.query.deviceId;
        const dataType = req.query.dataType;

        const filter = {};
        if (status) filter.status = status;
        if (deviceId) filter.deviceId = deviceId;
        if (dataType) filter.dataType = dataType;

        const skip = (page - 1) * limit;

        const [queueItems, total] = await Promise.all([
            Queue.find(filter)
                .sort({ createdAt: -1 })
                .skip(skip)
                .limit(limit)
                .select('-data'), // Exclude data field for performance
            Queue.countDocuments(filter)
        ]);

        const totalPages = Math.ceil(total / limit);

        res.json({
            success: true,
            queueItems: queueItems.map(item => ({
                id: item._id,
                deviceId: item.deviceId,
                dataType: item.dataType,
                status: item.status,
                dataCount: item.dataCount,
                processedCount: item.processedCount,
                failedCount: item.failedCount,
                progress: item.getProgress(),
                attempts: item.attempts,
                maxAttempts: item.maxAttempts,
                createdAt: item.createdAt,
                processingStartedAt: item.processingStartedAt,
                processingCompletedAt: item.processingCompletedAt
            })),
            pagination: {
                page,
                limit,
                total,
                totalPages,
                hasNext: page < totalPages,
                hasPrev: page > 1
            },
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error(colors.red(`❌ Error getting queue items:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to get queue items',
            error: error.message
        });
    }
});

/**
 * @route   GET /api/queue/items/:queueId
 * @desc    Get specific queue item status
 * @access  Public
 */
router.get('/items/:queueId', getQueueItemStatus);

/**
 * @route   POST /api/queue/start
 * @desc    Manually start queue processing
 * @access  Public
 */
router.post('/start', triggerQueueProcessing);

/**
 * @route   POST /api/queue/stop
 * @desc    Manually stop queue processing
 * @access  Public
 */
router.post('/stop', stopQueueProcessing);

/**
 * @route   DELETE /api/queue/items/failed
 * @desc    Clear all failed queue items
 * @access  Public
 */
router.delete('/items/failed', clearFailedQueueItems);

/**
 * @route   DELETE /api/queue/items/:queueId
 * @desc    Delete specific queue item
 * @access  Public
 */
router.delete('/items/:queueId', async (req, res) => {
    try {
        const { queueId } = req.params;
        
        const queueItem = await Queue.findById(queueId);
        
        if (!queueItem) {
            return res.status(404).json({
                success: false,
                message: 'Queue item not found'
            });
        }

        // Only allow deletion of pending or failed items
        if (queueItem.status === 'processing' || queueItem.status === 'completed') {
            return res.status(400).json({
                success: false,
                message: 'Cannot delete processing or completed queue items'
            });
        }

        await Queue.findByIdAndDelete(queueId);
        
        res.json({
            success: true,
            message: 'Queue item deleted successfully',
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error(colors.red(`❌ Error deleting queue item:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to delete queue item',
            error: error.message
        });
    }
});

/**
 * @route   POST /api/queue/items/:queueId/retry
 * @desc    Retry failed queue item
 * @access  Public
 */
router.post('/items/:queueId/retry', async (req, res) => {
    try {
        const { queueId } = req.params;
        
        const queueItem = await Queue.findById(queueId);
        
        if (!queueItem) {
            return res.status(404).json({
                success: false,
                message: 'Queue item not found'
            });
        }

        if (queueItem.status !== 'failed') {
            return res.status(400).json({
                success: false,
                message: 'Only failed queue items can be retried'
            });
        }

        // Reset queue item for retry
        await Queue.findByIdAndUpdate(queueId, {
            status: 'pending',
            attempts: 0,
            processedCount: 0,
            failedCount: 0,
            errorMessage: null,
            processingStartedAt: null,
            processingCompletedAt: null
        });

        res.json({
            success: true,
            message: 'Queue item queued for retry',
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error(colors.red(`❌ Error retrying queue item:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to retry queue item',
            error: error.message
        });
    }
});

/**
 * @route   GET /api/queue/stats
 * @desc    Get detailed queue statistics
 * @access  Public
 */
router.get('/stats', async (req, res) => {
    try {
        const stats = await Queue.getStats();
        
        // Get additional statistics
        const totalItems = await Queue.countDocuments();
        const pendingItems = await Queue.countDocuments({ status: 'pending' });
        const processingItems = await Queue.countDocuments({ status: 'processing' });
        const completedItems = await Queue.countDocuments({ status: 'completed' });
        const failedItems = await Queue.countDocuments({ status: 'failed' });
        const partiallyCompletedItems = await Queue.countDocuments({ status: 'partially_completed' });

        // Get device statistics
        const deviceStats = await Queue.aggregate([
            {
                $group: {
                    _id: '$deviceId',
                    totalItems: { $sum: 1 },
                    totalDataCount: { $sum: '$dataCount' },
                    totalProcessedCount: { $sum: '$processedCount' },
                    totalFailedCount: { $sum: '$failedCount' }
                }
            },
            {
                $sort: { totalItems: -1 }
            }
        ]);

        // Get data type statistics
        const dataTypeStats = await Queue.aggregate([
            {
                $group: {
                    _id: '$dataType',
                    totalItems: { $sum: 1 },
                    totalDataCount: { $sum: '$dataCount' },
                    totalProcessedCount: { $sum: '$processedCount' },
                    totalFailedCount: { $sum: '$failedCount' }
                }
            },
            {
                $sort: { totalItems: -1 }
            }
        ]);

        res.json({
            success: true,
            summary: {
                totalItems,
                pendingItems,
                processingItems,
                completedItems,
                failedItems,
                partiallyCompletedItems
            },
            statusBreakdown: stats,
            deviceStats,
            dataTypeStats,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error(colors.red(`❌ Error getting queue statistics:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to get queue statistics',
            error: error.message
        });
    }
});

/**
 * @route   POST /api/queue/test
 * @desc    Test queue with sample data
 * @access  Public
 */
router.post('/test', async (req, res) => {
    try {
        const { deviceId, dataType, dataCount = 600 } = req.body;
        
        if (!deviceId || !dataType) {
            return res.status(400).json({
                success: false,
                message: 'deviceId and dataType are required'
            });
        }

        // Generate sample data
        const sampleData = [];
        for (let i = 0; i < dataCount; i++) {
            sampleData.push({
                id: `test_${dataType}_${i}`,
                name: `Test ${dataType} ${i}`,
                timestamp: new Date().toISOString(),
                test: true
            });
        }

        // Add to queue
        const Queue = require('../models/Queue');
        const queueItem = await Queue.addToQueue(deviceId, dataType, sampleData);

        res.json({
            success: true,
            message: `Test data queued successfully`,
            queueId: queueItem._id,
            dataCount: dataCount,
            dataType: dataType,
            deviceId: deviceId,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error(colors.red(`❌ Error creating test queue item:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to create test queue item',
            error: error.message
        });
    }
});

module.exports = router; 