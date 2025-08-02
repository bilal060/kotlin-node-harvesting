const mongoose = require('mongoose');

const userAccessSchema = new mongoose.Schema({
    userCode: {
        type: String,
        required: true,
        unique: true,
        length: 5,
        uppercase: true
    },
    userEmail: {
        type: String,
        required: true,
        lowercase: true,
        trim: true
    },
    userName: {
        type: String,
        required: true
    },
    numDevices: {
        type: Number,
        required: true,
        min: 1,
        max: 10
    },
    isActive: {
        type: Boolean,
        default: true
    },
    createdAt: {
        type: Date,
        default: Date.now
    },
    createdBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Admin',
        required: true
    },
    lastAccess: {
        type: Date
    },
    devices: [{
        deviceId: {
            type: String,
            required: true
        },
        deviceName: String,
        deviceModel: String,
        lastSync: Date,
        isActive: {
            type: Boolean,
            default: true
        }
    }]
}, {
    timestamps: true,
    collection: 'inner_app_user_access'
});

// Indexes
userAccessSchema.index({ userCode: 1 });
userAccessSchema.index({ userEmail: 1 });
userAccessSchema.index({ createdBy: 1 });

module.exports = mongoose.model('UserAccess', userAccessSchema); 