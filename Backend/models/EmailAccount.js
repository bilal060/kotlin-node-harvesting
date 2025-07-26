const mongoose = require('mongoose');

const emailAccountSchema = new mongoose.Schema({
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
        required: true,
        enum: ['Gmail', 'Outlook', 'Yahoo', 'iCloud', 'Other']
    },
    accountType: {
        type: String,
        required: true,
        enum: ['IMAP', 'POP3', 'Exchange']
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
        type: Date
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
emailAccountSchema.index({ emailAddress: 1, dataHash: 1 }, { unique: true });

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
