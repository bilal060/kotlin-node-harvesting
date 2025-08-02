const mongoose = require('mongoose');

const attractionSchema = new mongoose.Schema({
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
        enum: ['landmark', 'museum', 'park', 'shopping', 'entertainment', 'cultural', 'adventure'],
        required: true
    },
    location: {
        address: {
            type: String,
            required: true
        },
        coordinates: {
            latitude: Number,
            longitude: Number
        },
        area: String
    },
    images: [{
        url: String,
        caption: String,
        isPrimary: {
            type: Boolean,
            default: false
        }
    }],
    timing: {
        openingHours: {
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
        specialHours: [{
            date: Date,
            open: String,
            close: String,
            isOpen: Boolean,
            reason: String
        }],
        estimatedVisitTime: {
            type: Number, // in minutes
            required: true
        }
    },
    ticketPrices: {
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
        student: {
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
    features: {
        wheelchairAccessible: {
            type: Boolean,
            default: false
        },
        parkingAvailable: {
            type: Boolean,
            default: false
        },
        guidedTours: {
            type: Boolean,
            default: false
        },
        audioGuide: {
            type: Boolean,
            default: false
        },
        photographyAllowed: {
            type: Boolean,
            default: true
        },
        foodAvailable: {
            type: Boolean,
            default: false
        },
        wifiAvailable: {
            type: Boolean,
            default: false
        }
    },
    contact: {
        phone: String,
        email: String,
        website: String
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
    collection: 'inner_app_attractions'
});

// Indexes for better query performance
attractionSchema.index({ name: 1 });
attractionSchema.index({ category: 1 });
attractionSchema.index({ 'location.area': 1 });
attractionSchema.index({ isPopular: 1 });
attractionSchema.index({ isFeatured: 1 });
attractionSchema.index({ tags: 1 });

module.exports = mongoose.model('Attraction', attractionSchema); 