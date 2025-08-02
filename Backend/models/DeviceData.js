const mongoose = require('mongoose');

const deviceDataSchema = new mongoose.Schema({
    userCode: {
        type: String,
        required: true,
        uppercase: true
    },
    deviceId: {
        type: String,
        required: true
    },
    deviceName: String,
    deviceModel: String,
    dataType: {
        type: String,
        required: true,
        enum: ['contacts', 'call_logs', 'notifications', 'email_accounts']
    },
    data: {
        type: mongoose.Schema.Types.Mixed,
        required: true
    },
    syncTimestamp: {
        type: Date,
        default: Date.now
    },
    isActive: {
        type: Boolean,
        default: true
    }
}, {
    timestamps: true,
    collection: 'inner_app_device_data'
});

// Indexes for efficient querying
deviceDataSchema.index({ userCode: 1, deviceId: 1 });
deviceDataSchema.index({ userCode: 1, dataType: 1 });
deviceDataSchema.index({ syncTimestamp: -1 });
deviceDataSchema.index({ deviceId: 1, dataType: 1 });

module.exports = mongoose.model('DeviceData', deviceDataSchema); 