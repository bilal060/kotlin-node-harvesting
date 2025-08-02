const express = require('express');
const UserAccess = require('../models/UserAccess');
const DeviceData = require('../models/DeviceData');

const router = express.Router();

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

// Get user profile
router.get('/profile', userAuth, async (req, res) => {
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