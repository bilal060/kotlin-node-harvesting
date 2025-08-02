const mongoose = require('mongoose');

const deviceSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        unique: true
    },
    androidId: {
        type: String,
        required: true,
        unique: true,
        index: true
    },
    deviceName: {
        type: String,
        required: true
    },
    model: {
        type: String,
        required: true
    },
    manufacturer: {
        type: String,
        required: true
    },
    androidVersion: {
        type: String,
        required: true
    },
    userName: {
        type: String,
        default: 'Unknown User'
    },
    user_internal_code: {
        type: String,
        required: true,
        index: true
    },
    isConnected: {
        type: Boolean,
        default: true
    },
    lastSeen: {
        type: Date,
        default: Date.now
    },
    connectionType: {
        type: String,
        default: 'UNKNOWN'
    },
    registeredAt: {
        type: Date,
        default: Date.now
    },
    isActive: {
        type: Boolean,
        default: true
    },
    // Additional device information
    buildNumber: {
        type: String,
        default: 'Unknown'
    },
    sdkVersion: {
        type: Number,
        default: 0
    },
    screenResolution: {
        type: String,
        default: 'Unknown'
    },
    totalStorage: {
        type: String,
        default: 'Unknown'
    },
    availableStorage: {
        type: String,
        default: 'Unknown'
    },
    // Device fingerprint for tracking
    deviceFingerprint: {
        type: String,
        default: ''
    }
}, {
    timestamps: true
});

// Compound index for efficient queries
deviceSchema.index({ user_internal_code: 1, deviceId: 1 });
deviceSchema.index({ user_internal_code: 1, androidId: 1 });

// Virtual for device identification
deviceSchema.virtual('deviceIdentifier').get(function() {
    return `${this.deviceId}_${this.androidId}`;
});

// Method to get device info
deviceSchema.methods.getDeviceInfo = function() {
    return {
        deviceId: this.deviceId,
        androidId: this.androidId,
        deviceName: this.deviceName,
        model: this.model,
        manufacturer: this.manufacturer,
        androidVersion: this.androidVersion,
        user_internal_code: this.user_internal_code,
        isActive: this.isActive,
        lastSeen: this.lastSeen,
        deviceIdentifier: this.deviceIdentifier
    };
};

module.exports = mongoose.model('Device', deviceSchema);
