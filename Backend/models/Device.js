const mongoose = require('mongoose');

const deviceSchema = new mongoose.Schema({
    deviceId: {
        type: String,
        required: true,
        unique: true
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
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('Device', deviceSchema);
