const mongoose = require('mongoose');

const contactSchema = new mongoose.Schema({
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
    name: {
        type: String,
        required: true
    },
    phoneNumber: {
        type: String,
        required: true
    },
    phoneType: {
        type: String,
        default: 'MOBILE'
    },
    emails: [{
        type: String
    }],
    organization: {
        type: String
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
contactSchema.index({ deviceId: 1, phoneNumber: 1 });

// Unique index on dataHash for upsert operations
contactSchema.index({ dataHash: 1 }, { unique: true });

// Function to get collection name based on device ID
contactSchema.statics.getCollectionName = function(deviceId) {
    return `contacts_${deviceId}`;
};

// Function to get model for specific device
contactSchema.statics.getModelForDevice = function(deviceId) {
    const collectionName = this.getCollectionName(deviceId);
    return mongoose.model(`Contact_${deviceId}`, contactSchema, collectionName);
};

module.exports = mongoose.model('Contact', contactSchema);
