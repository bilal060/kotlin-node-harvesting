const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    email: {
        type: String,
        required: true,
        unique: true,
        trim: true,
        lowercase: true
    },
    password: {
        type: String,
        required: true
    },
    phone: {
        type: String,
        trim: true
    },
    language: {
        type: String,
        enum: ['en', 'ar', 'zh', 'mn', 'kk'],
        default: 'en'
    },
    theme: {
        type: String,
        enum: ['light', 'dark', 'system'],
        default: 'light'
    },
    preferences: {
        notifications: {
            type: Boolean,
            default: true
        },
        emailUpdates: {
            type: Boolean,
            default: true
        },
        pushNotifications: {
            type: Boolean,
            default: true
        }
    },
    favorites: {
        attractions: [{
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Attraction'
        }],
        services: [{
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Service'
        }],
        packages: [{
            type: mongoose.Schema.Types.ObjectId,
            ref: 'TourPackage'
        }]
    },
    itineraries: [{
        name: String,
        startDate: Date,
        endDate: Date,
        attractions: [{
            attractionId: {
                type: mongoose.Schema.Types.ObjectId,
                ref: 'Attraction'
            },
            visitDate: Date,
            timeSlot: String,
            notes: String
        }],
        services: [{
            serviceId: {
                type: mongoose.Schema.Types.ObjectId,
                ref: 'Service'
            },
            date: Date,
            timeSlot: String,
            notes: String
        }],
        accommodations: {
            hotel: String,
            checkIn: Date,
            checkOut: Date,
            roomType: String
        },
        meals: {
            breakfast: Boolean,
            lunch: Boolean,
            dinner: Boolean
        },
        transport: {
            type: String,
            details: String
        },
        createdAt: {
            type: Date,
            default: Date.now
        },
        updatedAt: {
            type: Date,
            default: Date.now
        }
    }],
    isActive: {
        type: Boolean,
        default: true
    },
    lastLogin: {
        type: Date
    }
}, {
    timestamps: true,
    collection: 'inner_app_users'
});

// Index for email queries
userSchema.index({ email: 1 });

// Index for language and theme queries
userSchema.index({ language: 1, theme: 1 });

module.exports = mongoose.model('User', userSchema); 