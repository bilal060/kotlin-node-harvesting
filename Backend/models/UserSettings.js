const mongoose = require('mongoose');

const userSettingsSchema = new mongoose.Schema({
    // Reference to user
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true
    },
    
    // App preferences
    language: {
        type: String,
        enum: ['en', 'ar', 'fr', 'es', 'de', 'zh', 'ja', 'ko'],
        default: 'en'
    },
    
    theme: {
        type: String,
        enum: ['light', 'dark', 'auto'],
        default: 'light'
    },
    
    // Notification settings
    notifications: {
        pushEnabled: {
            type: Boolean,
            default: true
        },
        emailEnabled: {
            type: Boolean,
            default: true
        },
        smsEnabled: {
            type: Boolean,
            default: false
        },
        tourReminders: {
            type: Boolean,
            default: true
        },
        bookingUpdates: {
            type: Boolean,
            default: true
        },
        promotionalOffers: {
            type: Boolean,
            default: false
        }
    },
    
    // Privacy settings
    privacy: {
        shareLocation: {
            type: Boolean,
            default: true
        },
        shareProfile: {
            type: Boolean,
            default: false
        },
        allowAnalytics: {
            type: Boolean,
            default: true
        },
        allowMarketing: {
            type: Boolean,
            default: false
        }
    },
    
    // Tour preferences
    tourPreferences: {
        preferredTransport: {
            type: String,
            enum: ['car', 'bus', 'metro', 'walking', 'bike'],
            default: 'car'
        },
        maxGroupSize: {
            type: Number,
            min: 1,
            max: 50,
            default: 10
        },
        preferredDuration: {
            type: String,
            enum: ['half-day', 'full-day', 'multi-day'],
            default: 'full-day'
        },
        budgetRange: {
            type: String,
            enum: ['budget', 'moderate', 'luxury'],
            default: 'moderate'
        },
        dietaryRestrictions: [{
            type: String,
            enum: ['vegetarian', 'vegan', 'halal', 'kosher', 'gluten-free', 'dairy-free', 'nut-free']
        }],
        accessibilityNeeds: [{
            type: String,
            enum: ['wheelchair', 'hearing-aid', 'visual-aid', 'mobility-assistance']
        }]
    },
    
    // Location settings
    location: {
        homeCity: {
            type: String,
            default: 'Dubai'
        },
        preferredRegions: [{
            type: String
        }],
        timezone: {
            type: String,
            default: 'Asia/Dubai'
        }
    },
    
    // Payment preferences
    payment: {
        preferredCurrency: {
            type: String,
            enum: ['AED', 'USD', 'EUR', 'GBP'],
            default: 'AED'
        },
        savePaymentInfo: {
            type: Boolean,
            default: false
        },
        autoRenewal: {
            type: Boolean,
            default: false
        }
    },
    
    // App behavior
    appBehavior: {
        autoSync: {
            type: Boolean,
            default: true
        },
        syncFrequency: {
            type: String,
            enum: ['realtime', 'hourly', 'daily', 'weekly'],
            default: 'daily'
        },
        cacheSize: {
            type: Number,
            min: 50,
            max: 1000,
            default: 200 // MB
        },
        autoUpdate: {
            type: Boolean,
            default: true
        }
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

// Update timestamp on save
userSettingsSchema.pre('save', function(next) {
    this.updatedAt = new Date();
    next();
});

// Update timestamp on update
userSettingsSchema.pre('findOneAndUpdate', function() {
    this.set({ updatedAt: new Date() });
});

// Create default settings for a user
userSettingsSchema.statics.createDefaultSettings = function(userId) {
    return this.create({
        userId: userId
    });
};

// Get settings with defaults
userSettingsSchema.methods.getSettingsWithDefaults = function() {
    const defaults = {
        language: 'en',
        theme: 'light',
        notifications: {
            pushEnabled: true,
            emailEnabled: true,
            smsEnabled: false,
            tourReminders: true,
            bookingUpdates: true,
            promotionalOffers: false
        },
        privacy: {
            shareLocation: true,
            shareProfile: false,
            allowAnalytics: true,
            allowMarketing: false
        },
        tourPreferences: {
            preferredTransport: 'car',
            maxGroupSize: 10,
            preferredDuration: 'full-day',
            budgetRange: 'moderate',
            dietaryRestrictions: [],
            accessibilityNeeds: []
        },
        location: {
            homeCity: 'Dubai',
            preferredRegions: [],
            timezone: 'Asia/Dubai'
        },
        payment: {
            preferredCurrency: 'AED',
            savePaymentInfo: false,
            autoRenewal: false
        },
        appBehavior: {
            autoSync: true,
            syncFrequency: 'daily',
            cacheSize: 200,
            autoUpdate: true
        }
    };
    
    return {
        ...defaults,
        ...this.toObject()
    };
};

module.exports = mongoose.model('UserSettings', userSettingsSchema); 