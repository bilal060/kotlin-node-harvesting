const mongoose = require('mongoose');

const userProfileSchema = new mongoose.Schema({
    // Reference to user
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true
    },
    
    // Personal Information
    personalInfo: {
        firstName: {
            type: String,
            required: true,
            trim: true
        },
        lastName: {
            type: String,
            required: true,
            trim: true
        },
        middleName: {
            type: String,
            trim: true
        },
        dateOfBirth: {
            type: Date
        },
        gender: {
            type: String,
            enum: ['male', 'female', 'other', 'prefer-not-to-say'],
            default: 'prefer-not-to-say'
        },
        nationality: {
            type: String,
            default: 'UAE'
        },
        passportNumber: {
            type: String,
            trim: true
        },
        passportExpiry: {
            type: Date
        }
    },
    
    // Contact Information
    contactInfo: {
        email: {
            type: String,
            required: true,
            lowercase: true,
            trim: true
        },
        phone: {
            type: String,
            trim: true
        },
        alternatePhone: {
            type: String,
            trim: true
        },
        address: {
            street: String,
            city: String,
            state: String,
            country: String,
            postalCode: String
        },
        emergencyContact: {
            name: String,
            relationship: String,
            phone: String,
            email: String
        }
    },
    
    // Travel Preferences
    travelPreferences: {
        preferredAirlines: [String],
        seatPreference: {
            type: String,
            enum: ['window', 'aisle', 'middle', 'no-preference'],
            default: 'no-preference'
        },
        mealPreference: {
            type: String,
            enum: ['regular', 'vegetarian', 'vegan', 'halal', 'kosher', 'gluten-free'],
            default: 'regular'
        },
        specialAssistance: [{
            type: String,
            enum: ['wheelchair', 'mobility-assistance', 'visual-assistance', 'hearing-assistance', 'medical-assistance']
        }],
        travelInsurance: {
            type: Boolean,
            default: false
        }
    },
    
    // Tour History
    tourHistory: {
        totalTours: {
            type: Number,
            default: 0
        },
        totalSpent: {
            type: Number,
            default: 0
        },
        favoriteDestinations: [String],
        lastTourDate: Date,
        upcomingTours: [{
            tourId: String,
            tourName: String,
            startDate: Date,
            endDate: Date,
            status: {
                type: String,
                enum: ['confirmed', 'pending', 'cancelled'],
                default: 'pending'
            }
        }]
    },
    
    // Preferences
    preferences: {
        interests: [{
            type: String,
            enum: ['culture', 'adventure', 'relaxation', 'food', 'shopping', 'nature', 'history', 'art', 'sports', 'nightlife']
        }],
        activityLevel: {
            type: String,
            enum: ['low', 'moderate', 'high', 'extreme'],
            default: 'moderate'
        },
        groupSize: {
            type: String,
            enum: ['solo', 'couple', 'family', 'group', 'no-preference'],
            default: 'no-preference'
        },
        accommodationType: {
            type: String,
            enum: ['budget', 'standard', 'luxury', 'boutique', 'resort'],
            default: 'standard'
        }
    },
    
    // Social Media
    socialMedia: {
        facebook: String,
        instagram: String,
        twitter: String,
        linkedin: String
    },
    
    // Profile Picture
    profilePicture: {
        url: String,
        uploadedAt: Date
    },
    
    // Verification Status
    verification: {
        emailVerified: {
            type: Boolean,
            default: false
        },
        phoneVerified: {
            type: Boolean,
            default: false
        },
        identityVerified: {
            type: Boolean,
            default: false
        },
        verificationDocuments: [{
            type: String,
            url: String,
            uploadedAt: Date,
            status: {
                type: String,
                enum: ['pending', 'approved', 'rejected'],
                default: 'pending'
            }
        }]
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
userProfileSchema.pre('save', function(next) {
    this.updatedAt = new Date();
    next();
});

// Update timestamp on update
userProfileSchema.pre('findOneAndUpdate', function() {
    this.set({ updatedAt: new Date() });
});

// Create default profile for a user
userProfileSchema.statics.createDefaultProfile = function(userId, basicInfo = {}) {
    return this.create({
        userId: userId,
        personalInfo: {
            firstName: basicInfo.firstName || '',
            lastName: basicInfo.lastName || '',
            email: basicInfo.email || ''
        },
        contactInfo: {
            email: basicInfo.email || ''
        }
    });
};

// Get full name
userProfileSchema.methods.getFullName = function() {
    const { firstName, lastName, middleName } = this.personalInfo;
    return [firstName, middleName, lastName].filter(Boolean).join(' ');
};

// Get display name
userProfileSchema.methods.getDisplayName = function() {
    const { firstName, lastName } = this.personalInfo;
    return [firstName, lastName].filter(Boolean).join(' ');
};

// Update tour history
userProfileSchema.methods.addTour = function(tourData) {
    this.tourHistory.totalTours += 1;
    this.tourHistory.totalSpent += tourData.cost || 0;
    this.tourHistory.lastTourDate = new Date();
    
    if (tourData.destination && !this.tourHistory.favoriteDestinations.includes(tourData.destination)) {
        this.tourHistory.favoriteDestinations.push(tourData.destination);
    }
};

// Add upcoming tour
userProfileSchema.methods.addUpcomingTour = function(tourData) {
    this.tourHistory.upcomingTours.push({
        tourId: tourData.tourId,
        tourName: tourData.tourName,
        startDate: tourData.startDate,
        endDate: tourData.endDate,
        status: tourData.status || 'pending'
    });
};

// Remove upcoming tour
userProfileSchema.methods.removeUpcomingTour = function(tourId) {
    this.tourHistory.upcomingTours = this.tourHistory.upcomingTours.filter(
        tour => tour.tourId !== tourId
    );
};

// Get profile completeness percentage
userProfileSchema.methods.getProfileCompleteness = function() {
    const fields = [
        'personalInfo.firstName',
        'personalInfo.lastName',
        'contactInfo.email',
        'contactInfo.phone',
        'personalInfo.dateOfBirth',
        'personalInfo.gender',
        'contactInfo.address.city',
        'preferences.interests'
    ];
    
    let completedFields = 0;
    
    fields.forEach(field => {
        const value = field.split('.').reduce((obj, key) => obj?.[key], this);
        if (value && (typeof value === 'string' ? value.trim() !== '' : true)) {
            completedFields++;
        }
    });
    
    return Math.round((completedFields / fields.length) * 100);
};

module.exports = mongoose.model('UserProfile', userProfileSchema); 