const express = require('express');
const router = express.Router();
const UserProfile = require('../models/UserProfile');
const auth = require('../middleware/auth');

// Get user profile
router.get('/', auth, async (req, res) => {
    try {
        console.log('🔍 Fetching profile for user:', req.user.id);
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            console.log('📝 Creating default profile for user:', req.user.id);
            profile = await UserProfile.createDefaultProfile(req.user.id, {
                firstName: req.user.fullName?.split(' ')[0] || '',
                lastName: req.user.fullName?.split(' ').slice(1).join(' ') || '',
                email: req.user.email
            });
        }
        
        console.log('✅ Profile retrieved successfully for user:', req.user.id);
        res.json({
            success: true,
            data: profile
        });
        
    } catch (error) {
        console.error('❌ Error fetching user profile:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch user profile',
            error: error.message
        });
    }
});

// Update user profile
router.put('/', auth, async (req, res) => {
    try {
        console.log('🔄 Updating profile for user:', req.user.id);
        console.log('📝 Update data:', JSON.stringify(req.body, null, 2));
        
        const updateData = req.body;
        
        // Remove sensitive fields that shouldn't be updated directly
        delete updateData.userId;
        delete updateData._id;
        delete updateData.createdAt;
        delete updateData.updatedAt;
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            console.log('📝 Creating new profile for user:', req.user.id);
            profile = new UserProfile({ userId: req.user.id });
        }
        
        // Update profile with new data
        Object.keys(updateData).forEach(key => {
            if (updateData[key] !== undefined) {
                profile[key] = updateData[key];
            }
        });
        
        await profile.save();
        
        console.log('✅ Profile updated successfully for user:', req.user.id);
        res.json({
            success: true,
            message: 'Profile updated successfully',
            data: profile
        });
        
    } catch (error) {
        console.error('❌ Error updating user profile:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to update user profile',
            error: error.message
        });
    }
});

// Update specific profile section
router.patch('/:section', auth, async (req, res) => {
    try {
        const { section } = req.params;
        const updateData = req.body;
        
        console.log(`🔄 Updating ${section} for user:`, req.user.id);
        console.log('📝 Update data:', JSON.stringify(updateData, null, 2));
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            console.log('📝 Creating new profile for user:', req.user.id);
            profile = new UserProfile({ userId: req.user.id });
        }
        
        // Update only the specified section
        if (profile[section]) {
            Object.keys(updateData).forEach(key => {
                if (updateData[key] !== undefined) {
                    profile[section][key] = updateData[key];
                }
            });
        } else {
            // If section doesn't exist, create it
            profile[section] = updateData;
        }
        
        await profile.save();
        
        console.log(`✅ ${section} updated successfully for user:`, req.user.id);
        res.json({
            success: true,
            message: `${section} updated successfully`,
            data: profile
        });
        
    } catch (error) {
        console.error('❌ Error updating profile section:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to update profile section',
            error: error.message
        });
    }
});

// Upload profile picture
router.post('/profile-picture', auth, async (req, res) => {
    try {
        console.log('📸 Uploading profile picture for user:', req.user.id);
        
        const { imageUrl } = req.body;
        
        if (!imageUrl) {
            return res.status(400).json({
                success: false,
                message: 'Image URL is required'
            });
        }
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            profile = new UserProfile({ userId: req.user.id });
        }
        
        profile.profilePicture = {
            url: imageUrl,
            uploadedAt: new Date()
        };
        
        await profile.save();
        
        console.log('✅ Profile picture uploaded successfully for user:', req.user.id);
        res.json({
            success: true,
            message: 'Profile picture uploaded successfully',
            data: profile.profilePicture
        });
        
    } catch (error) {
        console.error('❌ Error uploading profile picture:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to upload profile picture',
            error: error.message
        });
    }
});

// Add tour to history
router.post('/tour-history', auth, async (req, res) => {
    try {
        console.log('📝 Adding tour to history for user:', req.user.id);
        
        const tourData = req.body;
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            profile = new UserProfile({ userId: req.user.id });
        }
        
        profile.addTour(tourData);
        await profile.save();
        
        console.log('✅ Tour added to history for user:', req.user.id);
        res.json({
            success: true,
            message: 'Tour added to history successfully',
            data: profile.tourHistory
        });
        
    } catch (error) {
        console.error('❌ Error adding tour to history:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to add tour to history',
            error: error.message
        });
    }
});

// Add upcoming tour
router.post('/upcoming-tours', auth, async (req, res) => {
    try {
        console.log('📅 Adding upcoming tour for user:', req.user.id);
        
        const tourData = req.body;
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            profile = new UserProfile({ userId: req.user.id });
        }
        
        profile.addUpcomingTour(tourData);
        await profile.save();
        
        console.log('✅ Upcoming tour added for user:', req.user.id);
        res.json({
            success: true,
            message: 'Upcoming tour added successfully',
            data: profile.tourHistory.upcomingTours
        });
        
    } catch (error) {
        console.error('❌ Error adding upcoming tour:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to add upcoming tour',
            error: error.message
        });
    }
});

// Remove upcoming tour
router.delete('/upcoming-tours/:tourId', auth, async (req, res) => {
    try {
        const { tourId } = req.params;
        console.log('🗑️ Removing upcoming tour for user:', req.user.id, 'Tour ID:', tourId);
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            return res.status(404).json({
                success: false,
                message: 'Profile not found'
            });
        }
        
        profile.removeUpcomingTour(tourId);
        await profile.save();
        
        console.log('✅ Upcoming tour removed for user:', req.user.id);
        res.json({
            success: true,
            message: 'Upcoming tour removed successfully',
            data: profile.tourHistory.upcomingTours
        });
        
    } catch (error) {
        console.error('❌ Error removing upcoming tour:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to remove upcoming tour',
            error: error.message
        });
    }
});

// Get profile completeness
router.get('/completeness', auth, async (req, res) => {
    try {
        console.log('📊 Getting profile completeness for user:', req.user.id);
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            profile = await UserProfile.createDefaultProfile(req.user.id);
        }
        
        const completeness = profile.getProfileCompleteness();
        
        console.log('✅ Profile completeness calculated for user:', req.user.id, completeness + '%');
        res.json({
            success: true,
            data: {
                completeness: completeness,
                profile: profile
            }
        });
        
    } catch (error) {
        console.error('❌ Error calculating profile completeness:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to calculate profile completeness',
            error: error.message
        });
    }
});

// Export profile
router.get('/export', auth, async (req, res) => {
    try {
        console.log('📤 Exporting profile for user:', req.user.id);
        
        let profile = await UserProfile.findOne({ userId: req.user.id });
        
        if (!profile) {
            profile = await UserProfile.createDefaultProfile(req.user.id);
        }
        
        console.log('✅ Profile exported successfully for user:', req.user.id);
        res.json({
            success: true,
            data: profile,
            exportedAt: new Date().toISOString()
        });
        
    } catch (error) {
        console.error('❌ Error exporting user profile:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to export user profile',
            error: error.message
        });
    }
});

module.exports = router; 