const mongoose = require('mongoose');

const sliderSchema = new mongoose.Schema({
    title: {
        type: String,
        required: true,
        trim: true
    },
    description: {
        type: String,
        required: true,
        trim: true
    },
    imageUrl: {
        type: String,
        required: true,
        trim: true
    },
    imageType: {
        type: String,
        enum: ['url', 'local'],
        default: 'url'
    },
    localImagePath: {
        type: String,
        trim: true
    },
    order: {
        type: Number,
        default: 0
    },
    isActive: {
        type: Boolean,
        default: true
    },
    category: {
        type: String,
        enum: ['hero', 'attractions', 'services', 'general'],
        default: 'hero'
    },
    actionType: {
        type: String,
        enum: ['none', 'attraction', 'service', 'package', 'url'],
        default: 'none'
    },
    actionData: {
        attractionId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Attraction'
        },
        serviceId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Service'
        },
        packageId: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'TourPackage'
        },
        url: String
    },
    tags: [String],
    metadata: {
        width: Number,
        height: Number,
        fileSize: Number,
        mimeType: String
    }
}, {
    timestamps: true,
    collection: 'inner_app_slider'
});

// Indexes for better query performance
sliderSchema.index({ category: 1 });
sliderSchema.index({ isActive: 1 });
sliderSchema.index({ order: 1 });
sliderSchema.index({ tags: 1 });

module.exports = mongoose.model('Slider', sliderSchema); 