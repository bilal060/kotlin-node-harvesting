const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    address: {
        type: String,
        required: true
    },
    body: {
        type: String,
        required: true
    },
    type: {
        type: String,
        enum: ['SMS', 'MMS'],
        default: 'SMS'
    },
    isIncoming: {
        type: Boolean,
        default: true
    },
    timestamp: {
        type: Date,
        required: true
    },
    isRead: {
        type: Boolean,
        default: false
    },
    syncTime: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Compound index for efficient queries (removed unique constraint to allow upsert)
messageSchema.index({ deviceId: 1, address: 1, timestamp: 1, body: 1 });

// Unique index on dataHash for upsert operations
messageSchema.index({ dataHash: 1 }, { unique: true });

// Function to get collection name based on device ID
messageSchema.statics.getCollectionName = function(deviceId) {
    return `messages_${deviceId}`;
};

// Function to get model for specific device
messageSchema.statics.getModelForDevice = function(deviceId) {
    const collectionName = this.getCollectionName(deviceId);
    return mongoose.model(`Message_${deviceId}`, messageSchema, collectionName);
};

module.exports = mongoose.model('Message', messageSchema);
