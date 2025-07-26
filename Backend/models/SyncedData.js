const mongoose = require('mongoose');

const syncedDataSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    dataType: {
        type: String,
        required: true,
        enum: ['CONTACTS', 'CALL_LOGS', 'MESSAGES', 'NOTIFICATIONS', 'EMAIL_ACCOUNTS', 'FILES']
    },
    data: {
        type: mongoose.Schema.Types.Mixed,
        required: true
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
syncedDataSchema.index({ deviceId: 1, dataType: 1, dataHash: 1 }, { unique: true });

module.exports = mongoose.model('SyncedData', syncedDataSchema); 