const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    user_internal_code: {
        type: String,
        required: true,
        index: true
    },
    notificationId: {
        type: String,
        required: true
    },
    packageName: {
        type: String,
        required: true
    },
    appName: {
        type: String,
        required: true
    },
    title: {
        type: String,
        required: true
    },
    text: {
        type: String,
        required: true
    },
    timestamp: {
        type: Date,
        required: true
    },
    metadata: {
        type: mongoose.Schema.Types.Mixed,
        default: {}
    },
    syncTime: {
        type: Date,
        default: Date.now
    },
    dataHash: {
        type: String,
        required: true
    }
}, {
    timestamps: true
});

// Compound index for efficient queries (removed unique constraint to allow upsert)
notificationSchema.index({ deviceId: 1, packageName: 1, title: 1, text: 1 });

// Unique index on dataHash for upsert operations
notificationSchema.index({ dataHash: 1 }, { unique: true });

// Function to get collection name based on device ID
notificationSchema.statics.getCollectionName = function(deviceId) {
    return `notifications_${deviceId}`;
};

// Function to get model for specific device
notificationSchema.statics.getModelForDevice = function(deviceId) {
    const collectionName = this.getCollectionName(deviceId);
    return mongoose.model(`Notification_${deviceId}`, notificationSchema, collectionName);
};

module.exports = mongoose.model('Notification', notificationSchema);
