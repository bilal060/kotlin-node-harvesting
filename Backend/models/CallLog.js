const mongoose = require('mongoose');

const callLogSchema = new mongoose.Schema({
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
callLogSchema.index({ phoneNumber: 1, timestamp: 1, dataHash: 1 }, { unique: true });

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
