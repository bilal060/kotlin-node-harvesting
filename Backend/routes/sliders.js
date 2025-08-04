const express = require('express');
const router = express.Router();
const Slider = require('../models/Slider');
const Attraction = require('../models/Attraction');
const Service = require('../models/Service');
const TourPackage = require('../models/TourPackage');

// Get all sliders with optional filtering
router.get('/', async (req, res) => {
    try {
        const { category, isActive, limit, offset } = req.query;
        
        let query = {};
        
        if (category) {
            query.category = category;
        }
        
        if (isActive !== undefined) {
            query.isActive = isActive === 'true';
        }
        
        let sliderQuery = Slider.find(query).sort({ order: 1, createdAt: -1 });
        
        if (limit) {
            sliderQuery = sliderQuery.limit(parseInt(limit));
        }
        
        if (offset) {
            sliderQuery = sliderQuery.skip(parseInt(offset));
        }
        
        const sliders = await sliderQuery.exec();
        
        res.json({
            success: true,
            data: sliders,
            count: sliders.length,
            message: 'Sliders retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching sliders:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch sliders',
            message: error.message
        });
    }
});

// Get sliders by category
router.get('/category/:category', async (req, res) => {
    try {
        const { category } = req.params;
        const { limit, offset } = req.query;
        
        let query = Slider.find({ 
            category: category, 
            isActive: true 
        }).sort({ order: 1, createdAt: -1 });
        
        if (limit) {
            query = query.limit(parseInt(limit));
        }
        
        if (offset) {
            query = query.skip(parseInt(offset));
        }
        
        const sliders = await query.exec();
        
        res.json({
            success: true,
            data: sliders,
            count: sliders.length,
            category: category,
            message: `Sliders for category '${category}' retrieved successfully`
        });
        
    } catch (error) {
        console.error('Error fetching sliders by category:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch sliders by category',
            message: error.message
        });
    }
});

// Get hero sliders (main slider for home screen)
router.get('/hero', async (req, res) => {
    try {
        const { limit = 6 } = req.query;
        
        const sliders = await Slider.find({ 
            category: 'hero', 
            isActive: true 
        })
        .sort({ order: 1, createdAt: -1 })
        .limit(parseInt(limit))
        .exec();
        
        res.json({
            success: true,
            data: sliders,
            count: sliders.length,
            message: 'Hero sliders retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching hero sliders:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch hero sliders',
            message: error.message
        });
    }
});

// Get attraction sliders
router.get('/attractions', async (req, res) => {
    try {
        const { limit = 10 } = req.query;
        
        const sliders = await Slider.find({ 
            category: 'attractions', 
            isActive: true 
        })
        .sort({ order: 1, createdAt: -1 })
        .limit(parseInt(limit))
        .exec();
        
        res.json({
            success: true,
            data: sliders,
            count: sliders.length,
            message: 'Attraction sliders retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching attraction sliders:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch attraction sliders',
            message: error.message
        });
    }
});

// Get service sliders
router.get('/services', async (req, res) => {
    try {
        const { limit = 10 } = req.query;
        
        const sliders = await Slider.find({ 
            category: 'services', 
            isActive: true 
        })
        .sort({ order: 1, createdAt: -1 })
        .limit(parseInt(limit))
        .exec();
        
        res.json({
            success: true,
            data: sliders,
            count: sliders.length,
            message: 'Service sliders retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching service sliders:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch service sliders',
            message: error.message
        });
    }
});

// Get slider with related data (for mobile app)
router.get('/:id/with-data', async (req, res) => {
    try {
        const { id } = req.params;
        
        const slider = await Slider.findById(id);
        
        if (!slider) {
            return res.status(404).json({
                success: false,
                error: 'Slider not found'
            });
        }
        
        let relatedData = null;
        
        // Fetch related data based on action type
        if (slider.actionType === 'attraction' && slider.actionData.attractionId) {
            relatedData = await Attraction.findById(slider.actionData.attractionId);
        } else if (slider.actionType === 'service' && slider.actionData.serviceId) {
            relatedData = await Service.findById(slider.actionData.serviceId);
        } else if (slider.actionType === 'package' && slider.actionData.packageId) {
            relatedData = await TourPackage.findById(slider.actionData.packageId);
        }
        
        res.json({
            success: true,
            data: {
                slider: slider,
                relatedData: relatedData
            },
            message: 'Slider with related data retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching slider with data:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch slider with data',
            message: error.message
        });
    }
});

// Get complete data bundle for mobile app (attractions, services, sliders)
router.get('/mobile/bundle', async (req, res) => {
    try {
        console.log('📱 Mobile app requesting data bundle...');
        
        // Get all active sliders
        const sliders = await Slider.find({ isActive: true })
            .sort({ order: 1, createdAt: -1 })
            .exec();
        
        // Get all active attractions (limited for mobile)
        const attractions = await Attraction.find({ isActive: true })
            .select('name description shortDescription category location images ticketPrices ratings isPopular isFeatured tags')
            .sort({ isPopular: -1, isFeatured: -1, createdAt: -1 })
            .limit(50)
            .exec();
        
        // Get all active services (limited for mobile)
        const services = await Service.find({ isActive: true })
            .select('name description shortDescription category subcategory location images pricing availability provider ratings isPopular isFeatured tags')
            .sort({ isPopular: -1, isFeatured: -1, createdAt: -1 })
            .limit(50)
            .exec();
        
        // Get all active tour packages (limited for mobile)
        const tourPackages = await TourPackage.find({ isActive: true })
            .select('name description shortDescription category duration images pricing itinerary provider ratings isPopular isFeatured tags')
            .sort({ isPopular: -1, isFeatured: -1, createdAt: -1 })
            .limit(20)
            .exec();
        
        const bundle = {
            sliders: sliders,
            attractions: attractions,
            services: services,
            tourPackages: tourPackages,
            metadata: {
                timestamp: new Date().toISOString(),
                version: '1.0.0',
                totalSliders: sliders.length,
                totalAttractions: attractions.length,
                totalServices: services.length,
                totalTourPackages: tourPackages.length
            }
        };
        
        console.log(`✅ Mobile bundle sent: ${sliders.length} sliders, ${attractions.length} attractions, ${services.length} services, ${tourPackages.length} packages`);
        
        res.json({
            success: true,
            data: bundle,
            message: 'Mobile data bundle retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching mobile bundle:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch mobile data bundle',
            message: error.message
        });
    }
});

// Get data bundle by category for mobile app
router.get('/mobile/bundle/:category', async (req, res) => {
    try {
        const { category } = req.params;
        console.log(`📱 Mobile app requesting ${category} bundle...`);
        
        let data = {};
        
        if (category === 'sliders') {
            data.sliders = await Slider.find({ isActive: true })
                .sort({ order: 1, createdAt: -1 })
                .exec();
        } else if (category === 'attractions') {
            data.attractions = await Attraction.find({ isActive: true })
                .select('name description shortDescription category location images ticketPrices ratings isPopular isFeatured tags')
                .sort({ isPopular: -1, isFeatured: -1, createdAt: -1 })
                .exec();
        } else if (category === 'services') {
            data.services = await Service.find({ isActive: true })
                .select('name description shortDescription category subcategory location images pricing availability provider ratings isPopular isFeatured tags')
                .sort({ isPopular: -1, isFeatured: -1, createdAt: -1 })
                .exec();
        } else if (category === 'packages') {
            data.tourPackages = await TourPackage.find({ isActive: true })
                .select('name description shortDescription category duration images pricing itinerary provider ratings isPopular isFeatured tags')
                .sort({ isPopular: -1, isFeatured: -1, createdAt: -1 })
                .exec();
        } else {
            return res.status(400).json({
                success: false,
                error: 'Invalid category. Use: sliders, attractions, services, or packages'
            });
        }
        
        const itemCount = Object.values(data)[0]?.length || 0;
        console.log(`✅ ${category} bundle sent: ${itemCount} items`);
        
        res.json({
            success: true,
            data: data,
            category: category,
            count: itemCount,
            message: `${category} bundle retrieved successfully`
        });
        
    } catch (error) {
        console.error(`Error fetching ${req.params.category} bundle:`, error);
        res.status(500).json({
            success: false,
            error: `Failed to fetch ${req.params.category} bundle`,
            message: error.message
        });
    }
});

// Get single slider by ID
router.get('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        const slider = await Slider.findById(id);
        
        if (!slider) {
            return res.status(404).json({
                success: false,
                error: 'Slider not found'
            });
        }
        
        res.json({
            success: true,
            data: slider,
            message: 'Slider retrieved successfully'
        });
        
    } catch (error) {
        console.error('Error fetching slider:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fetch slider',
            message: error.message
        });
    }
});

// Create new slider (admin only)
router.post('/', async (req, res) => {
    try {
        const sliderData = req.body;
        
        const slider = new Slider(sliderData);
        await slider.save();
        
        res.status(201).json({
            success: true,
            data: slider,
            message: 'Slider created successfully'
        });
        
    } catch (error) {
        console.error('Error creating slider:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to create slider',
            message: error.message
        });
    }
});

// Update slider (admin only)
router.put('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const updateData = req.body;
        
        const slider = await Slider.findByIdAndUpdate(id, updateData, { new: true });
        
        if (!slider) {
            return res.status(404).json({
                success: false,
                error: 'Slider not found'
            });
        }
        
        res.json({
            success: true,
            data: slider,
            message: 'Slider updated successfully'
        });
        
    } catch (error) {
        console.error('Error updating slider:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to update slider',
            message: error.message
        });
    }
});

// Delete slider (admin only)
router.delete('/:id', async (req, res) => {
    try {
        const { id } = req.params;
        
        const slider = await Slider.findByIdAndDelete(id);
        
        if (!slider) {
            return res.status(404).json({
                success: false,
                error: 'Slider not found'
            });
        }
        
        res.json({
            success: true,
            message: 'Slider deleted successfully'
        });
        
    } catch (error) {
        console.error('Error deleting slider:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to delete slider',
            message: error.message
        });
    }
});

module.exports = router; 