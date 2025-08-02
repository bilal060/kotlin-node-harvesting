const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const config = require('../config/environment');

// Import MongoDB models (we'll create these)
const User = require('../models/User');
const Attraction = require('../models/Attraction');
const Service = require('../models/Service');
const TourPackage = require('../models/TourPackage');
const DeviceData = require('../models/DeviceData');

// Authentication middleware
const auth = async (req, res, next) => {
    try {
        const token = req.header('Authorization')?.replace('Bearer ', '');
        if (!token) {
            return res.status(401).json({ success: false, message: 'Access denied. No token provided.' });
        }
        
        const decoded = jwt.verify(token, config.jwt.secret);
        const user = await User.findById(decoded.userId);
        if (!user) {
            return res.status(401).json({ success: false, message: 'Invalid token.' });
        }
        
        req.user = user;
        next();
    } catch (error) {
        res.status(401).json({ success: false, message: 'Invalid token.' });
    }
};

// 1. User Authentication APIs
// Register new user
router.post('/auth/register', async (req, res) => {
    try {
        const { email, password, name } = req.body;
        
        // Check if user already exists
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({ 
                success: false, 
                message: 'User with this email already exists' 
            });
        }
        
        // Hash password
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);
        
        // Create new user
        const user = new User({
            email,
            password: hashedPassword,
            name,
            language: 'en', // default language
            theme: 'light'  // default theme
        });
        
        await user.save();
        
        // Generate JWT token
        const token = jwt.sign({ userId: user._id }, config.jwt.secret, { expiresIn: '7d' });
        
        res.status(201).json({
            success: true,
            message: 'User registered successfully',
            data: {
                user: {
                    id: user._id,
                    email: user.email,
                    name: user.name,
                    language: user.language,
                    theme: user.theme
                },
                token
            }
        });
        
    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error during registration' 
        });
    }
});

// Login user
router.post('/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        
        // Find user by email
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(400).json({ 
                success: false, 
                message: 'Invalid email or password' 
            });
        }
        
        // Check password
        const isValidPassword = await bcrypt.compare(password, user.password);
        if (!isValidPassword) {
            return res.status(400).json({ 
                success: false, 
                message: 'Invalid email or password' 
            });
        }
        
        // Generate JWT token
        const token = jwt.sign({ userId: user._id }, config.jwt.secret, { expiresIn: '7d' });
        
        res.json({
            success: true,
            message: 'Login successful',
            data: {
                user: {
                    id: user._id,
                    email: user.email,
                    name: user.name,
                    language: user.language,
                    theme: user.theme
                },
                token
            }
        });
        
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Server error during login' 
        });
    }
});

// 2. Attractions APIs
// Get all attractions
router.get('/attractions', async (req, res) => {
    try {
        const attractions = await Attraction.find().sort({ name: 1 });
        res.json({
            success: true,
            data: attractions
        });
    } catch (error) {
        console.error('Error fetching attractions:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching attractions' 
        });
    }
});

// Get attraction by ID
router.get('/attractions/:id', async (req, res) => {
    try {
        const attraction = await Attraction.findById(req.params.id);
        if (!attraction) {
            return res.status(404).json({ 
                success: false, 
                message: 'Attraction not found' 
            });
        }
        res.json({
            success: true,
            data: attraction
        });
    } catch (error) {
        console.error('Error fetching attraction:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching attraction' 
        });
    }
});

// 3. Services APIs
// Get all services
router.get('/services', async (req, res) => {
    try {
        const services = await Service.find().sort({ name: 1 });
        res.json({
            success: true,
            data: services
        });
    } catch (error) {
        console.error('Error fetching services:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching services' 
        });
    }
});

// Get service by ID
router.get('/services/:id', async (req, res) => {
    try {
        const service = await Service.findById(req.params.id);
        if (!service) {
            return res.status(404).json({ 
                success: false, 
                message: 'Service not found' 
            });
        }
        res.json({
            success: true,
            data: service
        });
    } catch (error) {
        console.error('Error fetching service:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching service' 
        });
    }
});

// 4. Tour Packages APIs
// Get all tour packages
router.get('/tour-packages', async (req, res) => {
    try {
        const packages = await TourPackage.find().sort({ name: 1 });
        res.json({
            success: true,
            data: packages
        });
    } catch (error) {
        console.error('Error fetching tour packages:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching tour packages' 
        });
    }
});

// Get tour package by ID
router.get('/tour-packages/:id', async (req, res) => {
    try {
        const package = await TourPackage.findById(req.params.id);
        if (!package) {
            return res.status(404).json({ 
                success: false, 
                message: 'Tour package not found' 
            });
        }
        res.json({
            success: true,
            data: package
        });
    } catch (error) {
        console.error('Error fetching tour package:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching tour package' 
        });
    }
});

// 5. User Profile APIs
// Get user profile
router.get('/profile', auth, async (req, res) => {
    try {
        const user = await User.findById(req.user._id).select('-password');
        res.json({
            success: true,
            data: user
        });
    } catch (error) {
        console.error('Error fetching profile:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error fetching profile' 
        });
    }
});

// Update user profile
router.put('/profile', auth, async (req, res) => {
    try {
        const { name, email, phone, preferences } = req.body;
        
        const user = await User.findById(req.user._id);
        if (!user) {
            return res.status(404).json({ 
                success: false, 
                message: 'User not found' 
            });
        }
        
        // Update fields
        if (name) user.name = name;
        if (email) user.email = email;
        if (phone) user.phone = phone;
        if (preferences) user.preferences = preferences;
        
        await user.save();
        
        res.json({
            success: true,
            message: 'Profile updated successfully',
            data: {
                id: user._id,
                email: user.email,
                name: user.name,
                phone: user.phone,
                preferences: user.preferences,
                language: user.language,
                theme: user.theme
            }
        });
        
    } catch (error) {
        console.error('Error updating profile:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error updating profile' 
        });
    }
});

// 6. Settings APIs
// Update language preference
router.put('/settings/language', auth, async (req, res) => {
    try {
        const { language } = req.body;
        
        const user = await User.findById(req.user._id);
        if (!user) {
            return res.status(404).json({ 
                success: false, 
                message: 'User not found' 
            });
        }
        
        user.language = language;
        await user.save();
        
        res.json({
            success: true,
            message: 'Language updated successfully',
            data: { language: user.language }
        });
        
    } catch (error) {
        console.error('Error updating language:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error updating language' 
        });
    }
});

// Update theme preference
router.put('/settings/theme', auth, async (req, res) => {
    try {
        const { theme } = req.body;
        
        const user = await User.findById(req.user._id);
        if (!user) {
            return res.status(404).json({ 
                success: false, 
                message: 'User not found' 
            });
        }
        
        user.theme = theme;
        await user.save();
        
        res.json({
            success: true,
            message: 'Theme updated successfully',
            data: { theme: user.theme }
        });
        
    } catch (error) {
        console.error('Error updating theme:', error);
        res.status(500).json({ 
            success: false, 
            message: 'Error updating theme' 
        });
    }
});

// 7. Data Sync API
// Sync device data to backend
router.post('/sync-data', async (req, res) => {
    try {
        const { deviceId, androidId, deviceCode, dataType, data, timestamp } = req.body;
        
        console.log(`📱 Data sync request: ${dataType} from device ${deviceId} (androidId: ${androidId}, deviceCode: ${deviceCode})`);
        
        // Validate required fields
        if (!deviceId || !androidId || !deviceCode || !dataType || !data) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields: deviceId, androidId, deviceCode, dataType, data'
            });
        }
        
        // Create device data record
        const deviceData = new DeviceData({
            userCode: deviceCode.toUpperCase(),
            deviceId,
            androidId,
            deviceName: `${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}` || 'Unknown Device',
            deviceModel: android.os.Build.MODEL || 'Unknown Model',
            dataType,
            data,
            syncTimestamp: timestamp ? new Date(timestamp) : new Date()
        });
        
        await deviceData.save();
        
        console.log(`✅ Successfully synced ${dataType} data for device ${deviceId} (${androidId})`);
        
        res.json({
            success: true,
            message: 'Data synced successfully',
            data: {
                deviceId: deviceData.deviceId,
                androidId: deviceData.androidId,
                deviceCode: deviceData.userCode,
                dataType: deviceData.dataType,
                syncTimestamp: deviceData.syncTimestamp,
                recordCount: Array.isArray(data) ? data.length : 1
            }
        });
        
    } catch (error) {
        console.error('❌ Error syncing data:', error);
        res.status(500).json({
            success: false,
            message: 'Error syncing data',
            error: error.message
        });
    }
});

module.exports = router; 