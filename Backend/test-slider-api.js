const express = require('express');
const mongoose = require('mongoose');
const config = require('./config/environment');
const Slider = require('./models/Slider');

const app = express();
const PORT = 5003;

// Connect to MongoDB
mongoose.connect(config.mongodb.uri, config.mongodb.options);

// Simple slider routes for testing
app.get('/api/sliders/hero', async (req, res) => {
    try {
        const sliders = await Slider.find({ 
            category: 'hero', 
            isActive: true 
        })
        .sort({ order: 1, createdAt: -1 })
        .limit(6)
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

app.get('/api/sliders/mobile/bundle', async (req, res) => {
    try {
        console.log('📱 Mobile app requesting data bundle...');
        
        const sliders = await Slider.find({ isActive: true })
            .sort({ order: 1, createdAt: -1 })
            .exec();
        
        const bundle = {
            sliders: sliders,
            metadata: {
                timestamp: new Date().toISOString(),
                version: '1.0.0',
                totalSliders: sliders.length
            }
        };
        
        console.log(`✅ Mobile bundle sent: ${sliders.length} sliders`);
        
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

app.listen(PORT, () => {
    console.log(`🧪 Test server running on http://localhost:${PORT}`);
    console.log(`📱 Test API: http://localhost:${PORT}/api/sliders/hero`);
    console.log(`📦 Test Bundle: http://localhost:${PORT}/api/sliders/mobile/bundle`);
}); 