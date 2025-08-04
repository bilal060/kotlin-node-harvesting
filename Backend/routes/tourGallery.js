const express = require('express');
const router = express.Router();
const TourGallery = require('../models/TourGallery');

// Get all gallery items
router.get('/', async (req, res) => {
    try {
        const galleryItems = await TourGallery.find({ isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .exec();
        
        res.json({
            success: true,
            data: galleryItems,
            count: galleryItems.length,
            message: 'Gallery items retrieved successfully'
        });
    } catch (error) {
        console.error('Error fetching gallery items:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch gallery items',
            message: error.message
        });
    }
});

// Get gallery items grouped by type (Image/Video)
router.get('/grouped-by-type', async (req, res) => {
    try {
        const galleryItems = await TourGallery.find({ isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .exec();
        
        const groupedData = {
            images: galleryItems.filter(item => item.type === 'image'),
            videos: galleryItems.filter(item => item.type === 'video')
        };
        
        res.json({
            success: true,
            data: groupedData,
            summary: {
                total: galleryItems.length,
                images: groupedData.images.length,
                videos: groupedData.videos.length
            },
            message: 'Gallery items grouped by type retrieved successfully'
        });
    } catch (error) {
        console.error('Error fetching gallery items grouped by type:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch gallery items grouped by type',
            message: error.message
        });
    }
});

// Get gallery items grouped by category
router.get('/grouped-by-category', async (req, res) => {
    try {
        const { type, isActive, isFeatured } = req.query;
        
        let query = {};
        
        if (type) {
            query.type = type;
        }
        
        if (isActive !== undefined) {
            query.isActive = isActive === 'true';
        }
        
        if (isFeatured !== undefined) {
            query.isFeatured = isFeatured === 'true';
        }
        
        const galleryItems = await TourGallery.find(query)
            .sort({ order: 1, uploadDate: -1 })
            .exec();
        
        // Group by category
        const groupedData = {
            hero: galleryItems.filter(item => item.category === 'hero'),
            attractions: galleryItems.filter(item => item.category === 'attractions'),
            services: galleryItems.filter(item => item.category === 'services'),
            packages: galleryItems.filter(item => item.category === 'packages'),
            general: galleryItems.filter(item => item.category === 'general')
        };
        
        res.json({
            success: true,
            data: groupedData,
            summary: {
                total: galleryItems.length,
                hero: groupedData.hero.length,
                attractions: groupedData.attractions.length,
                services: groupedData.services.length,
                packages: groupedData.packages.length,
                general: groupedData.general.length
            },
            message: 'Gallery items grouped by category retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching gallery items grouped by category:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch gallery items grouped by category',
            message: error.message
        });
    }
});

// Get images only
router.get('/images', async (req, res) => {
    try {
        const images = await TourGallery.find({ type: 'image', isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .exec();
        
        res.json({
            success: true,
            data: images,
            count: images.length,
            message: 'Images retrieved successfully'
        });
    } catch (error) {
        console.error('Error fetching images:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch images',
            message: error.message
        });
    }
});

// Get videos only
router.get('/videos', async (req, res) => {
    try {
        const videos = await TourGallery.find({ type: 'video', isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .exec();
        
        res.json({
            success: true,
            data: videos,
            count: videos.length,
            message: 'Videos retrieved successfully'
        });
    } catch (error) {
        console.error('Error fetching videos:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch videos',
            message: error.message
        });
    }
});

// Get hero gallery items
router.get('/hero', async (req, res) => {
    try {
        const heroItems = await TourGallery.find({ category: 'hero', isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .limit(6)
            .exec();
        
        res.json({
            success: true,
            data: heroItems,
            count: heroItems.length,
            message: 'Hero gallery items retrieved successfully'
        });
    } catch (error) {
        console.error('Error fetching hero gallery items:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch hero gallery items',
            message: error.message
        });
    }
});

// Get gallery items by category
router.get('/category/:category', async (req, res) => {
    try {
        const { category } = req.params;
        
        const galleryItems = await TourGallery.find({ category: category, isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .exec();
        
        res.json({
            success: true,
            data: galleryItems,
            count: galleryItems.length,
            category: category,
            message: `Gallery items for category '${category}' retrieved successfully`
        });
    } catch (error) {
        console.error('Error fetching gallery items by category:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch gallery items by category',
            message: error.message
        });
    }
});

// Get featured gallery items
router.get('/featured', async (req, res) => {
    try {
        const featuredItems = await TourGallery.find({ isFeatured: true, isActive: true })
            .sort({ order: 1, uploadDate: -1 })
            .limit(10)
            .exec();
        
        res.json({
            success: true,
            data: featuredItems,
            count: featuredItems.length,
            message: 'Featured gallery items retrieved successfully'
        });
    } catch (error) {
        console.error('Error fetching featured gallery items:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch featured gallery items',
            message: error.message
        });
    }
});

// Get gallery statistics
router.get('/stats', async (req, res) => {
    try {
        const totalItems = await TourGallery.countDocuments();
        const imageCount = await TourGallery.countDocuments({ type: 'image' });
        const videoCount = await TourGallery.countDocuments({ type: 'video' });
        const activeCount = await TourGallery.countDocuments({ isActive: true });
        const featuredCount = await TourGallery.countDocuments({ isFeatured: true });
        
        const categoryStats = await TourGallery.aggregate([
            {
                $group: {
                    _id: '$category',
                    count: { $sum: 1 }
                }
            }
        ]);
        
        const typeStats = await TourGallery.aggregate([
            {
                $group: {
                    _id: '$type',
                    count: { $sum: 1 }
                }
            }
        ]);
        
        res.json({
            success: true,
            data: {
                total: totalItems,
                images: imageCount,
                videos: videoCount,
                active: activeCount,
                featured: featuredCount,
                byCategory: categoryStats,
                byType: typeStats
            },
            message: 'Gallery statistics retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching gallery statistics:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch gallery statistics',
            message: error.message
        });
    }
});

// Get single gallery item by ID
router.get('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        const galleryItem = await TourGallery.findById(id);
        
        if (!galleryItem) {
            return res.status(404).json({
                success: false,
                error: 'Gallery item not found'
            });
        }
        
        res.json({
            success: true,
            data: galleryItem,
            message: 'Gallery item retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching gallery item:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch gallery item',
            message: error.message
        });
    }
});

// Create new gallery item (admin only)
router.post('/', async (req, res) => {
    try {
        const galleryData = req.body;
        
        const galleryItem = new TourGallery(galleryData);
        await galleryItem.save();
        
        res.status(201).json({
            success: true,
            data: galleryItem,
            message: 'Gallery item created successfully'
        });
        
    } catch (error) {
        console.error('Error creating gallery item:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to create gallery item',
            message: error.message
        });
    }
});

// Update gallery item (admin only)
router.put('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const updateData = req.body;
        
        const galleryItem = await TourGallery.findByIdAndUpdate(id, updateData, { new: true });
        
        if (!galleryItem) {
            return res.status(404).json({
                success: false,
                error: 'Gallery item not found'
            });
        }
        
        res.json({
            success: true,
            data: galleryItem,
            message: 'Gallery item updated successfully'
        });
        
    } catch (error) {
        console.error('Error updating gallery item:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to update gallery item',
            message: error.message
        });
    }
});

// Delete gallery item (admin only)
router.delete('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        const galleryItem = await TourGallery.findByIdAndDelete(id);
        
        if (!galleryItem) {
            return res.status(404).json({
                success: false,
                error: 'Gallery item not found'
            });
        }
        
        res.json({
            success: true,
            message: 'Gallery item deleted successfully'
        });
        
    } catch (error) {
        console.error('Error deleting gallery item:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to delete gallery item',
            message: error.message
        });
    }
});

module.exports = router; 