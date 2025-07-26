const mongoose = require('mongoose');

const contactSchema = new mongoose.Schema({
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
contactSchema.index({ phoneNumber: 1, dataHash: 1 }, { unique: true });

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
