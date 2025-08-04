const QueueProcessor = require('../services/QueueProcessor');
const colors = require('colors');

// Initialize queue processor
const queueProcessor = new QueueProcessor();

// Start the queue processor when middleware is loaded
queueProcessor.start();

/**
 * Middleware to automatically queue large data sets
 * If data count > 500, it gets queued for batch processing
 * If data count <= 500, it gets processed immediately
 */
const queueMiddleware = (dataType) => {
    return async (req, res, next) => {
        try {
            const { deviceId, data } = req.body;
            
            if (!deviceId || !data) {
                return next(); // Continue with normal processing
            }

            const dataArray = Array.isArray(data) ? data : [data];
            const dataCount = dataArray.length;

            console.log(colors.blue(`📊 Received ${dataCount} ${dataType} items for device ${deviceId}`));

            // Check if data should be queued (> 500 items)
            if (queueProcessor.shouldQueueData(dataCount)) {
                console.log(colors.yellow(`📦 Data count (${dataCount}) exceeds threshold (${queueProcessor.dataThreshold}). Queuing for batch processing...`));
                
                try {
                    // Add to queue
                    const queueItem = await queueProcessor.addToQueue(deviceId, dataType, dataArray);
                    
                    // Return immediate response indicating data was queued
                    return res.status(202).json({
                        success: true,
                        message: `Data queued for processing`,
                        queueId: queueItem._id,
                        dataCount: dataCount,
                        batchSize: queueProcessor.batchSize,
                        estimatedBatches: Math.ceil(dataCount / queueProcessor.batchSize),
                        status: 'queued',
                        timestamp: new Date().toISOString()
                    });
                    
                } catch (queueError) {
                    console.error(colors.red(`❌ Failed to queue data:`, queueError.message));
                    // Fall back to immediate processing if queuing fails
                    console.log(colors.yellow(`⚠️ Falling back to immediate processing...`));
                    return next();
                }
            } else {
                console.log(colors.green(`✅ Data count (${dataCount}) within threshold. Processing immediately...`));
                // Continue with normal processing for smaller data sets
                return next();
            }
            
        } catch (error) {
            console.error(colors.red(`❌ Error in queue middleware:`, error.message));
            // Continue with normal processing on error
            return next();
        }
    };
};

/**
 * Middleware to get queue status
 */
const getQueueStatus = async (req, res) => {
    try {
        const stats = await queueProcessor.getStats();
        const status = queueProcessor.getStatus();
        
        res.json({
            success: true,
            queueStatus: status,
            statistics: stats,
            timestamp: new Date().toISOString()
        });
    } catch (error) {
        console.error(colors.red(`❌ Error getting queue status:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to get queue status',
            error: error.message
        });
    }
};

/**
 * Middleware to get specific queue item status
 */
const getQueueItemStatus = async (req, res) => {
    try {
        const { queueId } = req.params;
        const Queue = require('../models/Queue');
        
        const queueItem = await Queue.findById(queueId);
        
        if (!queueItem) {
            return res.status(404).json({
                success: false,
                message: 'Queue item not found'
            });
        }
        
        const progress = queueItem.getProgress();
        const estimatedTimeRemaining = queueItem.getEstimatedTimeRemaining();
        
        res.json({
            success: true,
            queueItem: {
                id: queueItem._id,
                deviceId: queueItem.deviceId,
                dataType: queueItem.dataType,
                status: queueItem.status,
                dataCount: queueItem.dataCount,
                processedCount: queueItem.processedCount,
                failedCount: queueItem.failedCount,
                progress: progress,
                estimatedTimeRemaining: estimatedTimeRemaining,
                attempts: queueItem.attempts,
                maxAttempts: queueItem.maxAttempts,
                errorMessage: queueItem.errorMessage,
                createdAt: queueItem.createdAt,
                processingStartedAt: queueItem.processingStartedAt,
                processingCompletedAt: queueItem.processingCompletedAt
            },
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        console.error(colors.red(`❌ Error getting queue item status:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to get queue item status',
            error: error.message
        });
    }
};

/**
 * Middleware to manually trigger queue processing
 */
const triggerQueueProcessing = async (req, res) => {
    try {
        if (!queueProcessor.isProcessing) {
            queueProcessor.start();
            res.json({
                success: true,
                message: 'Queue processor started',
                timestamp: new Date().toISOString()
            });
        } else {
            res.json({
                success: true,
                message: 'Queue processor is already running',
                timestamp: new Date().toISOString()
            });
        }
    } catch (error) {
        console.error(colors.red(`❌ Error triggering queue processing:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to trigger queue processing',
            error: error.message
        });
    }
};

/**
 * Middleware to stop queue processing
 */
const stopQueueProcessing = async (req, res) => {
    try {
        if (queueProcessor.isProcessing) {
            queueProcessor.stop();
            res.json({
                success: true,
                message: 'Queue processor stopped',
                timestamp: new Date().toISOString()
            });
        } else {
            res.json({
                success: true,
                message: 'Queue processor is not running',
                timestamp: new Date().toISOString()
            });
        }
    } catch (error) {
        console.error(colors.red(`❌ Error stopping queue processing:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to stop queue processing',
            error: error.message
        });
    }
};

/**
 * Middleware to clear failed queue items
 */
const clearFailedQueueItems = async (req, res) => {
    try {
        const Queue = require('../models/Queue');
        
        const result = await Queue.deleteMany({ status: 'failed' });
        
        res.json({
            success: true,
            message: `Cleared ${result.deletedCount} failed queue items`,
            deletedCount: result.deletedCount,
            timestamp: new Date().toISOString()
        });
        
    } catch (error) {
        console.error(colors.red(`❌ Error clearing failed queue items:`, error.message));
        res.status(500).json({
            success: false,
            message: 'Failed to clear failed queue items',
            error: error.message
        });
    }
};

module.exports = {
    queueMiddleware,
    getQueueStatus,
    getQueueItemStatus,
    triggerQueueProcessing,
    stopQueueProcessing,
    clearFailedQueueItems,
    queueProcessor
}; 