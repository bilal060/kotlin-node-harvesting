const mongoose = require('mongoose');

const syncHistorySchema = new mongoose.Schema({
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
    syncStartTime: {
        type: Date,
        required: true
    },
    syncEndTime: {
        type: Date,
        required: true
    },
    status: {
        type: String,
        required: true,
        enum: ['SUCCESS', 'FAILED', 'PARTIAL']
    },
    itemsSynced: {
        type: Number,
        default: 0
    },
    errorMessage: {
        type: String,
        default: null
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('SyncHistory', syncHistorySchema); 