const mongoose = require('mongoose');

const syncSettingsSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        index: true
    },
    dataType: {
        type: String,
        required: true,
        enum: ['CONTACTS', 'CALL_LOGS', 'MESSAGES', 'NOTIFICATIONS', 'EMAIL_ACCOUNTS', 'WHATSAPP']
    },
    lastSyncTime: {
        type: Date,
        default: null
    },
    isEnabled: {
        type: Boolean,
        default: true
    },
    itemCount: {
        type: Number,
        default: 0
    },
    lastSyncStatus: {
        type: String,
        enum: ['SUCCESS', 'FAILED', 'PARTIAL'],
        default: 'SUCCESS'
    },
    lastSyncMessage: {
        type: String,
        default: ''
    },
    syncFrequency: {
        type: Number, // in minutes
        default: 60
    },
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
}, {
    timestamps: true
});

// Compound index to ensure unique deviceId + dataType combination
syncSettingsSchema.index({ deviceId: 1, dataType: 1 }, { unique: true });

// Function to get or create sync settings for a device and data type
syncSettingsSchema.statics.getOrCreateSettings = async function(deviceId, dataType) {
    let settings = await this.findOne({ deviceId, dataType });
    
    if (!settings) {
        settings = new this({
            deviceId,
            dataType,
            lastSyncTime: null,
            isEnabled: true,
            itemCount: 0
        });
        await settings.save();
    }
    
    return settings;
};

// Function to update last sync time
syncSettingsSchema.statics.updateLastSyncTime = async function(deviceId, dataType, lastSyncTime, itemCount = 0, status = 'SUCCESS', message = '') {
    const settings = await this.getOrCreateSettings(deviceId, dataType);
    
    settings.lastSyncTime = lastSyncTime;
    settings.itemCount = itemCount;
    settings.lastSyncStatus = status;
    settings.lastSyncMessage = message;
    settings.updatedAt = new Date();
    
    await settings.save();
    return settings;
};

// Function to get all sync settings for a device
syncSettingsSchema.statics.getDeviceSettings = async function(deviceId) {
    return await this.find({ deviceId }).sort({ dataType: 1 });
};

// Function to get last sync time for a specific data type
syncSettingsSchema.statics.getLastSyncTime = async function(deviceId, dataType) {
    const settings = await this.findOne({ deviceId, dataType });
    return settings ? settings.lastSyncTime : null;
};

module.exports = mongoose.model('SyncSettings', syncSettingsSchema); 