const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    // Basic user info
    username: {
        type: String,
        required: true,
        unique: true
    },
    email: {
        type: String,
        required: true,
        unique: true
    },
    fullName: {
        type: String,
        required: true
    },
    
    // Internal code for APK builds
    user_internal_code: {
        type: String,
        required: true,
        unique: true,
        length: 5,
        uppercase: true
    },
    
    // Device management
    maxDevices: {
        type: Number,
        required: true,
        default: 10,
        min: 1
    },
    currentDevices: {
        type: Number,
        default: 0
    },
    
    // Billing and subscription
    subscriptionStatus: {
        type: String,
        enum: ['active', 'inactive', 'suspended', 'expired'],
        default: 'active'
    },
    subscriptionPlan: {
        type: String,
        enum: ['basic', 'premium', 'enterprise'],
        default: 'basic'
    },
    billingCycle: {
        type: String,
        enum: ['monthly', 'quarterly', 'yearly'],
        default: 'monthly'
    },
    nextBillingDate: {
        type: Date,
        default: Date.now
    },
    
    // APK and build info
    apkVersion: {
        type: String,
        default: '1.0.0'
    },
    lastBuildDate: {
        type: Date
    },
    buildStatus: {
        type: String,
        enum: ['pending', 'building', 'completed', 'failed'],
        default: 'pending'
    },
    
    // Data tracking
    totalDataRecords: {
        type: Number,
        default: 0
    },
    lastDataSync: {
        type: Date
    },
    
    // Admin notes
    adminNotes: {
        type: String
    },
    
    // Status
    isActive: {
        type: Boolean,
        default: true
    },
    
    // Timestamps
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

// Generate unique 5-digit alphanumeric code
userSchema.methods.generateInternalCode = function() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < 5; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
};

// Check if user can add more devices
userSchema.methods.canAddDevice = function() {
    return this.currentDevices < this.maxDevices && this.subscriptionStatus === 'active';
};

// Increment device count
userSchema.methods.addDevice = function() {
    if (this.canAddDevice()) {
        this.currentDevices += 1;
        return true;
    }
    return false;
};

// Decrement device count
userSchema.methods.removeDevice = function() {
    if (this.currentDevices > 0) {
        this.currentDevices -= 1;
        return true;
    }
    return false;
};

// Update data statistics
userSchema.methods.updateDataStats = function(recordCount) {
    this.totalDataRecords += recordCount;
    this.lastDataSync = new Date();
};

module.exports = mongoose.model('User', userSchema); 