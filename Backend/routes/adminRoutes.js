const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Admin = require('../models/Admin');
const UserAccess = require('../models/UserAccess');
const DeviceData = require('../models/DeviceData');
const auth = require('../middleware/auth');

const router = express.Router();

// Admin Authentication Middleware
const adminAuth = async (req, res, next) => {
    try {
        const token = req.header('Authorization')?.replace('Bearer ', '');
        if (!token) {
            return res.status(401).json({ message: 'Access denied. No token provided.' });
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'devicesync-secret-key-change-in-production');
        const admin = await Admin.findById(decoded.adminId);
        
        if (!admin || !admin.isActive) {
            return res.status(401).json({ message: 'Invalid token or admin inactive.' });
        }

        req.admin = admin;
        next();
    } catch (error) {
        res.status(401).json({ message: 'Invalid token.' });
    }
};

// Admin Login
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        // Check if admin exists
        const admin = await Admin.findOne({ email: email.toLowerCase() });
        if (!admin || !admin.isActive) {
            return res.status(401).json({ message: 'Invalid credentials or admin inactive.' });
        }

        // Verify password
        const isValidPassword = await admin.comparePassword(password);
        if (!isValidPassword) {
            return res.status(401).json({ message: 'Invalid credentials.' });
        }

        // Update last login
        admin.lastLogin = new Date();
        await admin.save();

        // Generate JWT token
        const token = jwt.sign(
            { adminId: admin._id, email: admin.email, role: admin.role },
            process.env.JWT_SECRET || 'devicesync-secret-key-change-in-production',
            { expiresIn: '24h' }
        );

        res.json({
            message: 'Admin login successful',
            token,
            admin: {
                id: admin._id,
                username: admin.username,
                email: admin.email,
                role: admin.role,
                permissions: admin.permissions
            }
        });
    } catch (error) {
        console.error('Admin login error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get Admin Profile
router.get('/profile', adminAuth, async (req, res) => {
    try {
        res.json({
            admin: {
                id: req.admin._id,
                username: req.admin.username,
                email: req.admin.email,
                role: req.admin.role,
                permissions: req.admin.permissions,
                lastLogin: req.admin.lastLogin
            }
        });
    } catch (error) {
        console.error('Get admin profile error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Generate 5-digit user code
const generateUserCode = () => {
    return Math.floor(10000 + Math.random() * 90000).toString();
};

// Create new user access
router.post('/users', adminAuth, async (req, res) => {
    try {
        const { userEmail, userName, numDevices } = req.body;

        // Validate input
        if (!userEmail || !userName || !numDevices) {
            return res.status(400).json({ message: 'All fields are required.' });
        }

        if (numDevices < 1 || numDevices > 10) {
            return res.status(400).json({ message: 'Number of devices must be between 1 and 10.' });
        }

        // Check if user already exists
        const existingUser = await UserAccess.findOne({ userEmail: userEmail.toLowerCase() });
        if (existingUser) {
            return res.status(400).json({ message: 'User with this email already exists.' });
        }

        // Generate unique 5-digit code
        let userCode;
        let isUnique = false;
        while (!isUnique) {
            userCode = generateUserCode();
            const existingCode = await UserAccess.findOne({ userCode });
            if (!existingCode) {
                isUnique = true;
            }
        }

        // Create new user access
        const userAccess = new UserAccess({
            userCode,
            userEmail: userEmail.toLowerCase(),
            userName,
            numDevices,
            createdBy: req.admin._id
        });

        await userAccess.save();

        res.status(201).json({
            message: 'User access created successfully',
            userAccess: {
                userCode,
                userEmail,
                userName,
                numDevices,
                createdAt: userAccess.createdAt
            }
        });
    } catch (error) {
        console.error('Create user access error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get all users
router.get('/users', adminAuth, async (req, res) => {
    try {
        const users = await UserAccess.find({ isActive: true })
            .populate('createdBy', 'username email')
            .sort({ createdAt: -1 });

        res.json({
            users: users.map(user => ({
                id: user._id,
                userCode: user.userCode,
                userEmail: user.userEmail,
                userName: user.userName,
                numDevices: user.numDevices,
                createdAt: user.createdAt,
                lastAccess: user.lastAccess,
                deviceCount: user.devices.length,
                createdBy: user.createdBy
            }))
        });
    } catch (error) {
        console.error('Get users error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get user by code
router.get('/users/:userCode', adminAuth, async (req, res) => {
    try {
        const { userCode } = req.params;
        
        const user = await UserAccess.findOne({ userCode: userCode.toUpperCase() })
            .populate('createdBy', 'username email');

        if (!user) {
            return res.status(404).json({ message: 'User not found.' });
        }

        res.json({
            user: {
                id: user._id,
                userCode: user.userCode,
                userEmail: user.userEmail,
                userName: user.userName,
                numDevices: user.numDevices,
                createdAt: user.createdAt,
                lastAccess: user.lastAccess,
                devices: user.devices,
                createdBy: user.createdBy
            }
        });
    } catch (error) {
        console.error('Get user error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Update user access
router.put('/users/:userCode', adminAuth, async (req, res) => {
    try {
        const { userCode } = req.params;
        const { userName, numDevices, isActive } = req.body;

        const user = await UserAccess.findOne({ userCode: userCode.toUpperCase() });
        if (!user) {
            return res.status(404).json({ message: 'User not found.' });
        }

        if (userName) user.userName = userName;
        if (numDevices !== undefined) {
            if (numDevices < 1 || numDevices > 10) {
                return res.status(400).json({ message: 'Number of devices must be between 1 and 10.' });
            }
            user.numDevices = numDevices;
        }
        if (isActive !== undefined) user.isActive = isActive;

        await user.save();

        res.json({
            message: 'User updated successfully',
            user: {
                id: user._id,
                userCode: user.userCode,
                userEmail: user.userEmail,
                userName: user.userName,
                numDevices: user.numDevices,
                isActive: user.isActive
            }
        });
    } catch (error) {
        console.error('Update user error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get all device data (admin view)
router.get('/device-data', adminAuth, async (req, res) => {
    try {
        const { userCode, deviceId, dataType, limit = 100, page = 1 } = req.query;
        
        const filter = { isActive: true };
        if (userCode) filter.userCode = userCode.toUpperCase();
        if (deviceId) filter.deviceId = deviceId;
        if (dataType) filter.dataType = dataType;

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
        console.error('Get device data error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get device data summary
router.get('/device-data/summary', adminAuth, async (req, res) => {
    try {
        const summary = await DeviceData.aggregate([
            { $match: { isActive: true } },
            {
                $group: {
                    _id: {
                        userCode: '$userCode',
                        dataType: '$dataType'
                    },
                    count: { $sum: 1 },
                    lastSync: { $max: '$syncTimestamp' }
                }
            },
            {
                $group: {
                    _id: '$_id.userCode',
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
        console.error('Get device data summary error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get all devices
router.get('/devices', adminAuth, async (req, res) => {
    try {
        const devices = await DeviceData.aggregate([
            { $match: { isActive: true } },
            {
                $group: {
                    _id: {
                        userCode: '$userCode',
                        deviceId: '$deviceId'
                    },
                    deviceName: { $first: '$deviceName' },
                    deviceModel: { $first: '$deviceModel' },
                    lastSync: { $max: '$syncTimestamp' },
                    dataTypes: { $addToSet: '$dataType' }
                }
            },
            {
                $group: {
                    _id: '$_id.userCode',
                    devices: {
                        $push: {
                            deviceId: '$_id.deviceId',
                            deviceName: '$deviceName',
                            deviceModel: '$deviceModel',
                            lastSync: '$lastSync',
                            dataTypes: '$dataTypes'
                        }
                    }
                }
            }
        ]);

        res.json({ devices });
    } catch (error) {
        console.error('Get devices error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

module.exports = router; 