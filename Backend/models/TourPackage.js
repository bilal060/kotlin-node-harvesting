const mongoose = require('mongoose');

const tourPackageSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    description: {
        type: String,
        required: true
    },
    shortDescription: {
        type: String,
        required: true
    },
    category: {
        type: String,
        enum: ['essential', 'luxury', 'adventure', 'cultural', 'family', 'romantic', 'budget'],
        required: true
    },
    duration: {
        days: {
            type: Number,
            required: true
        },
        nights: {
            type: Number,
            required: true
        }
    },
    images: [{
        url: String,
        caption: String,
        isPrimary: {
            type: Boolean,
            default: false
        }
    }],
    pricing: {
        adult: {
            type: Number,
            required: true
        },
        child: {
            type: Number,
            default: 0
        },
        senior: {
            type: Number,
            default: 0
        },
        family: {
            type: Number,
            default: 0
        },
        currency: {
            type: String,
            default: 'AED'
        },
        includes: [String],
        exclusions: [String],
        specialOffers: [{
            name: String,
            description: String,
            discount: Number,
            validFrom: Date,
            validTo: Date
        }]
    },
    itinerary: [{
        day: {
            type: Number,
            required: true
        },
        title: String,
        description: String,
        activities: [{
            time: String,
            activity: String,
            location: String,
            duration: Number, // in minutes
            type: {
                type: String,
                enum: ['attraction', 'meal', 'transport', 'free_time', 'hotel']
            }
        }],
        meals: {
            breakfast: {
                type: Boolean,
                default: false
            },
            lunch: {
                type: Boolean,
                default: false
            },
            dinner: {
                type: Boolean,
                default: false
            }
        },
        accommodation: {
            hotel: String,
            roomType: String,
            checkIn: String,
            checkOut: String
        },
        transport: {
            type: String,
            details: String
        }
    }],
    highlights: [{
        type: String,
        trim: true
    }],
    attractions: [{
        attractionId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Attraction'
        },
        visitOrder: Number,
        timeSlot: String,
        duration: Number // in minutes
    }],
    services: [{
        serviceId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Service'
        },
        day: Number,
        timeSlot: String,
        duration: Number // in minutes
    }],
    accommodations: {
        hotelCategory: {
            type: String,
            enum: ['budget', 'standard', 'premium', 'luxury'],
            default: 'standard'
        },
        roomType: {
            type: String,
            default: 'Standard Room'
        },
        mealPlan: {
            type: String,
            enum: ['room_only', 'bed_breakfast', 'half_board', 'full_board', 'all_inclusive'],
            default: 'bed_breakfast'
        }
    },
    transport: {
        airportTransfer: {
            type: Boolean,
            default: true
        },
        interCityTransport: {
            type: Boolean,
            default: true
        },
        localTransport: {
            type: Boolean,
            default: true
        },
        transportType: [String]
    },
    groupSize: {
        min: {
            type: Number,
            default: 1
        },
        max: {
            type: Number,
            default: 20
        }
    },
    availability: {
        isAvailable: {
            type: Boolean,
            default: true
        },
        startDate: Date,
        endDate: Date,
        maxBookings: Number,
        currentBookings: {
            type: Number,
            default: 0
        },
        requiresAdvanceBooking: {
            type: Boolean,
            default: true
        },
        advanceBookingDays: {
            type: Number,
            default: 7
        }
    },
    provider: {
        name: {
            type: String,
            required: true
        },
        contact: {
            phone: String,
            email: String,
            website: String
        },
        rating: {
            type: Number,
            default: 0,
            min: 0,
            max: 5
        },
        totalReviews: {
            type: Number,
            default: 0
        }
    },
    ratings: {
        average: {
            type: Number,
            default: 0,
            min: 0,
            max: 5
        },
        totalReviews: {
            type: Number,
            default: 0
        },
        reviews: [{
            userId: {
                type: mongoose.Schema.Types.ObjectId,
                ref: 'User'
            },
            rating: {
                type: Number,
                required: true,
                min: 1,
                max: 5
            },
            comment: String,
            date: {
                type: Date,
                default: Date.now
            }
        }]
    },
    tags: [String],
    isActive: {
        type: Boolean,
        default: true
    },
    isPopular: {
        type: Boolean,
        default: false
    },
    isFeatured: {
        type: Boolean,
        default: false
    }
}, {
    timestamps: true,
    collection: 'inner_app_tour_packages'
});

// Indexes for better query performance
tourPackageSchema.index({ name: 1 });
tourPackageSchema.index({ category: 1 });
tourPackageSchema.index({ 'duration.days': 1 });
tourPackageSchema.index({ isPopular: 1 });
tourPackageSchema.index({ isFeatured: 1 });
tourPackageSchema.index({ tags: 1 });

module.exports = mongoose.model('TourPackage', tourPackageSchema); 