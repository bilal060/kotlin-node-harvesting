const mongoose = require('mongoose');

const emailAccountSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    accountId: {
        type: String,
        required: true
    },
    emailAddress: {
        type: String,
        required: true
    },
    accountName: {
        type: String,
        required: true
    },
    provider: {
        type: String,
        required: true
    },
    accountType: {
        type: String,
        default: 'IMAP'
    },
    serverIncoming: {
        type: String
    },
    serverOutgoing: {
        type: String
    },
    portIncoming: {
        type: Number
    },
    portOutgoing: {
        type: Number
    },
    isActive: {
        type: Boolean,
        default: true
    },
    isDefault: {
        type: Boolean,
        default: false
    },
    syncEnabled: {
        type: Boolean,
        default: true
    },
    lastSyncTime: {
        type: Date,
        default: Date.now
    },
    totalEmails: {
        type: Number
    },
    unreadEmails: {
        type: Number
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
emailAccountSchema.index({ deviceId: 1, emailAddress: 1 });

// Unique index on dataHash for upsert operations
emailAccountSchema.index({ dataHash: 1 }, { unique: true });

// Function to get collection name based on device ID
emailAccountSchema.statics.getCollectionName = function(deviceId) {
    return `emailaccounts_${deviceId}`;
};

// Function to get model for specific device
emailAccountSchema.statics.getModelForDevice = function(deviceId) {
    const collectionName = this.getCollectionName(deviceId);
    return mongoose.model(`EmailAccount_${deviceId}`, emailAccountSchema, collectionName);
};

module.exports = mongoose.model('EmailAccount', emailAccountSchema);
