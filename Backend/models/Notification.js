const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
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
        type: String
    },
    text: {
        type: String
    },
    timestamp: {
        type: Date,
        required: true
    },
    syncTime: {
        type: Date,
        default: Date.now
    },
    // Hash for duplicate detection
    dataHash: {
        type: String,
        required: true,
        index: true
    }
}, {
    timestamps: true
});

// Compound index to prevent duplicates
notificationSchema.index({ notificationId: 1, timestamp: 1, dataHash: 1 }, { unique: true });

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
