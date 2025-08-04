const mongoose = require('mongoose');

const tourGallerySchema = new mongoose.Schema({
    title: {
        type: String,
        required: true,
        trim: true
    },
    description: {
        type: String,
        trim: true
    },
    type: {
        type: String,
        enum: ['image', 'video'],
        required: true
    },
    filePath: {
        type: String,
        required: true,
        trim: true
    },
    fileName: {
        type: String,
        required: true,
        trim: true
    },
    fileSize: {
        type: Number,
        default: 0
    },
    mimeType: {
        type: String,
        trim: true
    },
    dimensions: {
        width: Number,
        height: Number
    },
    duration: {
        type: Number, // for videos in seconds
        default: 0
    },
    thumbnail: {
        type: String,
        trim: true
    },
    category: {
        type: String,
        enum: ['attractions', 'services', 'packages', 'general', 'hero'],
        default: 'general'
    },
    tags: [String],
    isActive: {
        type: Boolean,
        default: true
    },
    isFeatured: {
        type: Boolean,
        default: false
    },
    order: {
        type: Number,
        default: 0
    },
    uploadDate: {
        type: Date,
        default: Date.now
    },
    metadata: {
        originalName: String,
        uploadPath: String,
        processed: {
            type: Boolean,
            default: false
        },
        compression: {
            quality: Number,
            format: String
        }
    }
}, {
    timestamps: true,
    collection: 'inner_app_tour_gallary'
});

// Indexes for better query performance
tourGallerySchema.index({ type: 1 });
tourGallerySchema.index({ category: 1 });
tourGallerySchema.index({ isActive: 1 });
tourGallerySchema.index({ isFeatured: 1 });
tourGallerySchema.index({ tags: 1 });
tourGallerySchema.index({ order: 1 });

module.exports = mongoose.model('TourGallery', tourGallerySchema); 