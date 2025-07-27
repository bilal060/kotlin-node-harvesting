const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
    deviceId: {
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
    syncTime: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Compound index to prevent duplicates based on device, package, title and timestamp
notificationSchema.index({ deviceId: 1, packageName: 1, title: 1, timestamp: 1 }, { unique: true });

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
