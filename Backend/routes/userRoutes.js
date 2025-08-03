const express = require('express');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const UserAccess = require('../models/UserAccess');
const DeviceData = require('../models/DeviceData');

const router = express.Router();

// User Login
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        // Check if user exists
        const user = await User.findOne({ email: email.toLowerCase() });
        if (!user || !user.isActive) {
            return res.status(401).json({ message: 'Invalid credentials or user inactive.' });
        }

        // Verify password
        const isValidPassword = await user.comparePassword(password);
        if (!isValidPassword) {
            return res.status(401).json({ message: 'Invalid credentials.' });
        }

        // Generate JWT token
        const token = jwt.sign(
            { userId: user._id, email: user.email, userCode: user.user_internal_code },
            process.env.JWT_SECRET || 'devicesync-secret-key-change-in-production',
            { expiresIn: '24h' }
        );

        res.json({
            message: 'User login successful',
            token,
            user: {
                id: user._id,
                username: user.username,
                email: user.email,
                fullName: user.fullName,
                userCode: user.user_internal_code,
                subscriptionStatus: user.subscriptionStatus,
                subscriptionPlan: user.subscriptionPlan,
                maxDevices: user.maxDevices,
                currentDevices: user.currentDevices
            }
        });
    } catch (error) {
        console.error('User login error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// User Registration (only for users created by admin)
router.post('/register', async (req, res) => {
    try {
        const { username, email, password, fullName, userCode } = req.body;

        // Check if user already exists
        const existingUser = await User.findOne({ 
            $or: [{ email: email.toLowerCase() }, { username }] 
        });
        
        if (existingUser) {
            return res.status(400).json({ message: 'User with this email or username already exists.' });
        }

        // Check if userCode is provided (required for admin-created users)
        if (!userCode) {
            return res.status(400).json({ message: 'User code is required for registration.' });
        }

        // Create new user
        const user = new User({
            username,
            email: email.toLowerCase(),
            password,
            fullName,
            user_internal_code: userCode.toUpperCase()
        });

        await user.save();

        // Generate JWT token
        const token = jwt.sign(
            { userId: user._id, email: user.email, userCode: user.user_internal_code },
            process.env.JWT_SECRET || 'devicesync-secret-key-change-in-production',
            { expiresIn: '24h' }
        );

        res.status(201).json({
            message: 'User registered successfully',
            token,
            user: {
                id: user._id,
                username: user.username,
                email: user.email,
                fullName: user.fullName,
                userCode: user.user_internal_code,
                subscriptionStatus: user.subscriptionStatus,
                subscriptionPlan: user.subscriptionPlan,
                maxDevices: user.maxDevices,
                currentDevices: user.currentDevices
            }
        });
    } catch (error) {
        console.error('User registration error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// JWT-based User Authentication Middleware
const userAuthJWT = async (req, res, next) => {
    try {
        const token = req.header('Authorization')?.replace('Bearer ', '');
        if (!token) {
            return res.status(401).json({ message: 'Access denied. No token provided.' });
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'devicesync-secret-key-change-in-production');
        const user = await User.findById(decoded.userId);
        
        if (!user || !user.isActive) {
            return res.status(401).json({ message: 'Invalid token or user inactive.' });
        }

        req.user = user;
        next();
    } catch (error) {
        res.status(401).json({ message: 'Invalid token.' });
    }
};

// User Authentication Middleware
const userAuth = async (req, res, next) => {
    try {
        const userCode = req.header('User-Code');
        if (!userCode) {
            return res.status(401).json({ message: 'Access denied. No user code provided.' });
        }

        const userAccess = await UserAccess.findOne({ 
            userCode: userCode.toUpperCase(),
            isActive: true 
        });
        
        if (!userAccess) {
            return res.status(401).json({ message: 'Invalid user code or user inactive.' });
        }

        req.userAccess = userAccess;
        next();
    } catch (error) {
        res.status(401).json({ message: 'Invalid user code.' });
    }
};

// Get user profile (JWT-based)
router.get('/profile', userAuthJWT, async (req, res) => {
    try {
        res.json({
            user: {
                id: req.user._id,
                username: req.user.username,
                email: req.user.email,
                fullName: req.user.fullName,
                userCode: req.user.user_internal_code,
                subscriptionStatus: req.user.subscriptionStatus,
                subscriptionPlan: req.user.subscriptionPlan,
                maxDevices: req.user.maxDevices,
                currentDevices: req.user.currentDevices,
                totalDataRecords: req.user.totalDataRecords,
                lastDataSync: req.user.lastDataSync,
                createdAt: req.user.createdAt
            }
        });
    } catch (error) {
        console.error('Get user profile error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get user profile (Legacy UserCode-based)
router.get('/profile-legacy', userAuth, async (req, res) => {
    try {
        res.json({
            user: {
                userCode: req.userAccess.userCode,
                userEmail: req.userAccess.userEmail,
                userName: req.userAccess.userName,
                numDevices: req.userAccess.numDevices,
                deviceCount: req.userAccess.devices.length,
                lastAccess: req.userAccess.lastAccess
            }
        });
    } catch (error) {
        console.error('Get user profile error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get user's device data (limited by numDevices)
router.get('/device-data', userAuth, async (req, res) => {
    try {
        const { dataType, limit = 100, page = 1 } = req.query;
        
        // Get user's devices (limited by numDevices)
        const userDevices = req.userAccess.devices
            .slice(0, req.userAccess.numDevices)
            .map(device => device.deviceId);

        if (userDevices.length === 0) {
            return res.json({
                deviceData: [],
                pagination: {
                    total: 0,
                    page: parseInt(page),
                    limit: parseInt(limit),
                    pages: 0
                }
            });
        }

        const filter = { 
            userCode: req.userAccess.userCode,
            deviceId: { $in: userDevices },
            isActive: true 
        };
        
        if (dataType) {
            filter.dataType = dataType;
        }

        const skip = (page - 1) * limit;
        
        const deviceData = await DeviceData.find(filter)
            .sort({ syncTimestamp: -1 })
            .limit(parseInt(limit))
            .skip(skip);

        const total = await DeviceData.countDocuments(filter);

        res.json({
            deviceData,
            pagination: {
                total,
                page: parseInt(page),
                limit: parseInt(limit),
                pages: Math.ceil(total / limit)
            }
        });
    } catch (error) {
        console.error('Get user device data error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get user's devices (limited by numDevices)
router.get('/devices', userAuth, async (req, res) => {
    try {
        const userDevices = req.userAccess.devices
            .slice(0, req.userAccess.numDevices);

        res.json({
            devices: userDevices,
            totalAllowed: req.userAccess.numDevices,
            currentCount: userDevices.length
        });
    } catch (error) {
        console.error('Get user devices error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get device data summary for user
router.get('/device-data/summary', userAuth, async (req, res) => {
    try {
        // Get user's devices (limited by numDevices)
        const userDevices = req.userAccess.devices
            .slice(0, req.userAccess.numDevices)
            .map(device => device.deviceId);

        if (userDevices.length === 0) {
            return res.json({ summary: [] });
        }

        const summary = await DeviceData.aggregate([
            { 
                $match: { 
                    userCode: req.userAccess.userCode,
                    deviceId: { $in: userDevices },
                    isActive: true 
                } 
            },
            {
                $group: {
                    _id: {
                        deviceId: '$deviceId',
                        dataType: '$dataType'
                    },
                    count: { $sum: 1 },
                    lastSync: { $max: '$syncTimestamp' }
                }
            },
            {
                $group: {
                    _id: '$_id.deviceId',
                    dataTypes: {
                        $push: {
                            dataType: '$_id.dataType',
                            count: '$count',
                            lastSync: '$lastSync'
                        }
                    },
                    totalRecords: { $sum: '$count' }
                }
            }
        ]);

        res.json({ summary });
    } catch (error) {
        console.error('Get user device data summary error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get specific data type for user
router.get('/device-data/:dataType', userAuth, async (req, res) => {
    try {
        const { dataType } = req.params;
        const { deviceId, limit = 100, page = 1 } = req.query;

        // Validate data type
        const validDataTypes = ['contacts', 'call_logs', 'notifications', 'email_accounts'];
        if (!validDataTypes.includes(dataType)) {
            return res.status(400).json({ message: 'Invalid data type.' });
        }

        // Get user's devices (limited by numDevices)
        const userDevices = req.userAccess.devices
            .slice(0, req.userAccess.numDevices)
            .map(device => device.deviceId);

        if (userDevices.length === 0) {
            return res.json({
                deviceData: [],
                pagination: {
                    total: 0,
                    page: parseInt(page),
                    limit: parseInt(limit),
                    pages: 0
                }
            });
        }

        const filter = { 
            userCode: req.userAccess.userCode,
            deviceId: { $in: userDevices },
            dataType,
            isActive: true 
        };
        
        if (deviceId) {
            filter.deviceId = deviceId;
        }

        const skip = (page - 1) * limit;
        
        const deviceData = await DeviceData.find(filter)
            .sort({ syncTimestamp: -1 })
            .limit(parseInt(limit))
            .skip(skip);

        const total = await DeviceData.countDocuments(filter);

        res.json({
            deviceData,
            pagination: {
                total,
                page: parseInt(page),
                limit: parseInt(limit),
                pages: Math.ceil(total / limit)
            }
        });
    } catch (error) {
        console.error('Get user specific data error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

module.exports = router; 