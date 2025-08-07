const express = require('express');
const router = express.Router();
const UserSettings = require('../models/UserSettings');
const auth = require('../middleware/auth');

// Get user settings
router.get('/', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching settings for user:', req.user.id);
        
        let settings = await UserSettings.findOne({ userId: req.user.id });
        
        if (!settings) {
            console.log('📝 Creating default settings for user:', req.user.id);
            settings = await UserSettings.createDefaultSettings(req.user.id);
        }
        
        const settingsWithDefaults = settings.getSettingsWithDefaults();
        
        console.log('✅ Settings retrieved successfully for user:', req.user.id);
        res.json({
            success: true,
            data: settingsWithDefaults
        });
        
    } catch (error) {
        console.error('❌ Error fetching user settings:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch user settings',
            error: error.message
        });
    }
});

// Update user settings
router.put('/', auth, async (req, res) => {
    try {
        console.log('🔄 Updating settings for user:', req.user.id);
        console.log('📝 Update data:', JSON.stringify(req.body, null, 2));
        
        const updateData = req.body;
        
        // Remove sensitive fields that shouldn't be updated directly
        delete updateData.userId;
        delete updateData._id;
        delete updateData.createdAt;
        delete updateData.updatedAt;
        
        let settings = await UserSettings.findOne({ userId: req.user.id });
        
        if (!settings) {
            console.log('📝 Creating new settings for user:', req.user.id);
            settings = new UserSettings({ userId: req.user.id });
        }
        
        // Update settings with new data
        Object.keys(updateData).forEach(key => {
            if (updateData[key] !== undefined) {
                settings[key] = updateData[key];
            }
        });
        
        await settings.save();
        
        const settingsWithDefaults = settings.getSettingsWithDefaults();
        
        console.log('✅ Settings updated successfully for user:', req.user.id);
        res.json({
            success: true,
            message: 'Settings updated successfully',
            data: settingsWithDefaults
        });
        
    } catch (error) {
        console.error('❌ Error updating user settings:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to update user settings',
            error: error.message
        });
    }
});

// Update specific setting category
router.patch('/:category', auth, async (req, res) => {
    try {
        const { category } = req.params;
        const updateData = req.body;
        
        console.log(`🔄 Updating ${category} settings for user:`, req.user.id);
        console.log('📝 Update data:', JSON.stringify(updateData, null, 2));
        
        let settings = await UserSettings.findOne({ userId: req.user.id });
        
        if (!settings) {
            console.log('📝 Creating new settings for user:', req.user.id);
            settings = new UserSettings({ userId: req.user.id });
        }
        
        // Update only the specified category
        if (settings[category]) {
            Object.keys(updateData).forEach(key => {
                if (updateData[key] !== undefined) {
                    settings[category][key] = updateData[key];
                }
            });
        } else {
            // If category doesn't exist, create it
            settings[category] = updateData;
        }
        
        await settings.save();
        
        const settingsWithDefaults = settings.getSettingsWithDefaults();
        
        console.log(`✅ ${category} settings updated successfully for user:`, req.user.id);
        res.json({
            success: true,
            message: `${category} settings updated successfully`,
            data: settingsWithDefaults
        });
        
    } catch (error) {
        console.error('❌ Error updating user settings category:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to update user settings',
            error: error.message
        });
    }
});

// Reset settings to defaults
router.post('/reset', auth, async (req, res) => {
    try {
        console.log('🔄 Resetting settings to defaults for user:', req.user.id);
        
        let settings = await UserSettings.findOne({ userId: req.user.id });
        
        if (!settings) {
            console.log('📝 Creating default settings for user:', req.user.id);
            settings = await UserSettings.createDefaultSettings(req.user.id);
        } else {
            // Reset to defaults
            settings.language = 'en';
            settings.theme = 'light';
            settings.notifications = {
                pushEnabled: true,
                emailEnabled: true,
                smsEnabled: false,
                tourReminders: true,
                bookingUpdates: true,
                promotionalOffers: false
            };
            settings.privacy = {
                shareLocation: true,
                shareProfile: false,
                allowAnalytics: true,
                allowMarketing: false
            };
            settings.tourPreferences = {
                preferredTransport: 'car',
                maxGroupSize: 10,
                preferredDuration: 'full-day',
                budgetRange: 'moderate',
                dietaryRestrictions: [],
                accessibilityNeeds: []
            };
            settings.location = {
                homeCity: 'Dubai',
                preferredRegions: [],
                timezone: 'Asia/Dubai'
            };
            settings.payment = {
                preferredCurrency: 'AED',
                savePaymentInfo: false,
                autoRenewal: false
            };
            settings.appBehavior = {
                autoSync: true,
                syncFrequency: 'daily',
                cacheSize: 200,
                autoUpdate: true
            };
            
            await settings.save();
        }
        
        const settingsWithDefaults = settings.getSettingsWithDefaults();
        
        console.log('✅ Settings reset successfully for user:', req.user.id);
        res.json({
            success: true,
            message: 'Settings reset to defaults successfully',
            data: settingsWithDefaults
        });
        
    } catch (error) {
        console.error('❌ Error resetting user settings:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to reset user settings',
            error: error.message
        });
    }
});

// Export settings
router.get('/export', auth, async (req, res) => {
    try {
        console.log('📤 Exporting settings for user:', req.user.id);
        
        let settings = await UserSettings.findOne({ userId: req.user.id });
        
        if (!settings) {
            settings = await UserSettings.createDefaultSettings(req.user.id);
        }
        
        const settingsWithDefaults = settings.getSettingsWithDefaults();
        
        console.log('✅ Settings exported successfully for user:', req.user.id);
        res.json({
            success: true,
            data: settingsWithDefaults,
            exportedAt: new Date().toISOString()
        });
        
    } catch (error) {
        console.error('❌ Error exporting user settings:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to export user settings',
            error: error.message
        });
    }
});

// Import settings
router.post('/import', auth, async (req, res) => {
    try {
        console.log('📥 Importing settings for user:', req.user.id);
        
        const importData = req.body;
        
        if (!importData || typeof importData !== 'object') {
            return res.status(400).json({
                success: false,
                message: 'Invalid import data'
            });
        }
        
        let settings = await UserSettings.findOne({ userId: req.user.id });
        
        if (!settings) {
            settings = new UserSettings({ userId: req.user.id });
        }
        
        // Import settings data
        Object.keys(importData).forEach(key => {
            if (key !== 'userId' && key !== '_id' && key !== 'createdAt' && key !== 'updatedAt') {
                settings[key] = importData[key];
            }
        });
        
        await settings.save();
        
        const settingsWithDefaults = settings.getSettingsWithDefaults();
        
        console.log('✅ Settings imported successfully for user:', req.user.id);
        res.json({
            success: true,
            message: 'Settings imported successfully',
            data: settingsWithDefaults
        });
        
    } catch (error) {
        console.error('❌ Error importing user settings:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to import user settings',
            error: error.message
        });
    }
});

module.exports = router; 