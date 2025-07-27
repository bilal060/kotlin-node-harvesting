const mongoose = require('mongoose');

const callLogSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    phoneNumber: {
        type: String,
        required: true
    },
    contactName: {
        type: String
    },
    callType: {
        type: String,
        enum: ['INCOMING', 'OUTGOING', 'MISSED', 'REJECTED', 'BLOCKED'],
        required: true
    },
    timestamp: {
        type: Date,
        required: true
    },
    duration: {
        type: Number,
        default: 0
    },
    syncTime: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Compound index to prevent duplicates based on device, phone, timestamp and duration
callLogSchema.index({ deviceId: 1, phoneNumber: 1, timestamp: 1, duration: 1 }, { unique: true });

// Function to get collection name based on device ID
callLogSchema.statics.getCollectionName = function(deviceId) {
    return `calllogs_${deviceId}`;
};

// Function to get model for specific device
callLogSchema.statics.getModelForDevice = function(deviceId) {
    const collectionName = this.getCollectionName(deviceId);
    return mongoose.model(`CallLog_${deviceId}`, callLogSchema, collectionName);
};

module.exports = mongoose.model('CallLog', callLogSchema);
