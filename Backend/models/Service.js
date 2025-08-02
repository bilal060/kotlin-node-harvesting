const mongoose = require('mongoose');

const serviceSchema = new mongoose.Schema({
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
        enum: ['transport', 'accommodation', 'food', 'entertainment', 'wellness', 'shopping', 'guide', 'photography'],
        required: true
    },
    subcategory: {
        type: String,
        trim: true
    },
    location: {
        address: String,
        coordinates: {
            latitude: Number,
            longitude: Number
        },
        area: String,
        isMobile: {
            type: Boolean,
            default: false
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
        basePrice: {
            type: Number,
            required: true
        },
        currency: {
            type: String,
            default: 'AED'
        },
        pricingType: {
            type: String,
            enum: ['per_person', 'per_hour', 'per_day', 'fixed', 'negotiable'],
            default: 'per_person'
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
    availability: {
        isAvailable: {
            type: Boolean,
            default: true
        },
        operatingHours: {
            monday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            },
            tuesday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            },
            wednesday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            },
            thursday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            },
            friday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            },
            saturday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            },
            sunday: {
                open: String,
                close: String,
                isOpen: {
                    type: Boolean,
                    default: true
                }
            }
        },
        duration: {
            type: Number, // in minutes
            required: true
        },
        maxCapacity: Number,
        requiresBooking: {
            type: Boolean,
            default: true
        },
        advanceBookingDays: {
            type: Number,
            default: 1
        }
    },
    features: {
        languages: [String],
        accessibility: {
            wheelchairAccessible: {
                type: Boolean,
                default: false
            },
            childFriendly: {
                type: Boolean,
                default: false
            },
            petFriendly: {
                type: Boolean,
                default: false
            }
        },
        amenities: [String],
        requirements: [String]
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
    collection: 'inner_app_services'
});

// Indexes for better query performance
serviceSchema.index({ name: 1 });
serviceSchema.index({ category: 1 });
serviceSchema.index({ subcategory: 1 });
serviceSchema.index({ 'location.area': 1 });
serviceSchema.index({ isPopular: 1 });
serviceSchema.index({ isFeatured: 1 });
serviceSchema.index({ tags: 1 });

module.exports = mongoose.model('Service', serviceSchema); 