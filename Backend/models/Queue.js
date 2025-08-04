const mongoose = require('mongoose');

const queueSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    dataType: {
        type: String,
        required: true,
        enum: ['contacts', 'calllogs', 'messages', 'notifications', 'emailaccounts', 'whatsapp'],
        index: true
    },
    data: {
        type: mongoose.Schema.Types.Mixed,
        required: true
    },
    dataCount: {
        type: Number,
        required: true,
        default: 0
    },
    batchSize: {
        type: Number,
        default: 100
    },
    status: {
        type: String,
        enum: ['pending', 'processing', 'completed', 'failed', 'partially_completed'],
        default: 'pending',
        index: true
    },
    priority: {
        type: Number,
        default: 0, // 0 = normal, 1 = high, 2 = urgent
        index: true
    },
    attempts: {
        type: Number,
        default: 0
    },
    maxAttempts: {
        type: Number,
        default: 3
    },
    processedCount: {
        type: Number,
        default: 0
    },
    failedCount: {
        type: Number,
        default: 0
    },
    errorMessage: {
        type: String
    },
    processingStartedAt: {
        type: Date
    },
    processingCompletedAt: {
        type: Date
    },
    createdAt: {
        type: Date,
        default: Date.now,
        index: true
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Indexes for better performance
queueSchema.index({ status: 1, priority: -1, createdAt: 1 });
queueSchema.index({ deviceId: 1, dataType: 1, status: 1 });
queueSchema.index({ createdAt: 1 }, { expireAfterSeconds: 7 * 24 * 60 * 60 }); // Auto-delete after 7 days

// Static method to add item to queue
queueSchema.statics.addToQueue = async function(deviceId, dataType, data, priority = 0) {
    const dataCount = Array.isArray(data) ? data.length : 1;
    const batchSize = dataCount > 500 ? 100 : dataCount;
    
    const queueItem = new this({
        deviceId,
        dataType,
        data,
        dataCount,
        batchSize,
        priority
    });
    
    return await queueItem.save();
};

// Static method to get next pending item
queueSchema.statics.getNextPending = async function() {
    return await this.findOneAndUpdate(
        { 
            status: 'pending',
            attempts: { $lt: '$maxAttempts' }
        },
        { 
            $inc: { attempts: 1 },
            $set: { 
                status: 'processing',
                processingStartedAt: new Date()
            }
        },
        { 
            sort: { priority: -1, createdAt: 1 },
            new: true
        }
    );
};

// Static method to mark item as completed
queueSchema.statics.markCompleted = async function(queueId, processedCount, failedCount = 0, errorMessage = null) {
    const status = failedCount > 0 ? 'partially_completed' : 'completed';
    
    return await this.findByIdAndUpdate(queueId, {
        status,
        processedCount,
        failedCount,
        errorMessage,
        processingCompletedAt: new Date()
    }, { new: true });
};

// Static method to mark item as failed
queueSchema.statics.markFailed = async function(queueId, errorMessage) {
    return await this.findByIdAndUpdate(queueId, {
        status: 'failed',
        errorMessage,
        processingCompletedAt: new Date()
    }, { new: true });
};

// Static method to get queue statistics
queueSchema.statics.getStats = async function() {
    const stats = await this.aggregate([
        {
            $group: {
                _id: '$status',
                count: { $sum: 1 },
                totalDataCount: { $sum: '$dataCount' },
                totalProcessedCount: { $sum: '$processedCount' },
                totalFailedCount: { $sum: '$failedCount' }
            }
        }
    ]);
    
    return stats;
};

// Instance method to get processing progress
queueSchema.methods.getProgress = function() {
    if (this.dataCount === 0) return 0;
    return Math.round((this.processedCount / this.dataCount) * 100);
};

// Instance method to get estimated time remaining
queueSchema.methods.getEstimatedTimeRemaining = function() {
    if (!this.processingStartedAt || this.processedCount === 0) return null;
    
    const elapsed = Date.now() - this.processingStartedAt.getTime();
    const rate = this.processedCount / elapsed; // items per millisecond
    const remaining = this.dataCount - this.processedCount;
    
    return Math.round(remaining / rate);
};

module.exports = mongoose.model('Queue', queueSchema); 