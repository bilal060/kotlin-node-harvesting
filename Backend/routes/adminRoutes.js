const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Admin = require('../models/Admin');
const User = require('../models/User');
const UserAccess = require('../models/UserAccess');
const Device = require('../models/Device');
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

// Generate 5-digit device code for sub-admin
const generateDeviceCode = () => {
    return Math.floor(10000 + Math.random() * 90000).toString();
};

// Create new user access
router.post('/users', adminAuth, async (req, res) => {
    try {
        const { userEmail, userName, numDevices, password, fullName } = req.body;

        // Validate input
        if (!userEmail || !userName || !numDevices || !password) {
            return res.status(400).json({ message: 'Email, username, password, and number of devices are required.' });
        }

        if (numDevices < 1 || numDevices > 10) {
            return res.status(400).json({ message: 'Number of devices must be between 1 and 10.' });
        }

        // Check if user already exists
        const existingUserAccess = await UserAccess.findOne({ userEmail: userEmail.toLowerCase() });
        const existingUser = await User.findOne({ email: userEmail.toLowerCase() });
        
        if (existingUserAccess || existingUser) {
            return res.status(400).json({ message: 'User with this email already exists.' });
        }

        // Generate unique 5-digit code
        let userCode;
        let isUnique = false;
        while (!isUnique) {
            userCode = generateUserCode();
            const existingCode = await UserAccess.findOne({ userCode });
            const existingUserWithCode = await User.findOne({ user_internal_code: userCode });
            if (!existingCode && !existingUserWithCode) {
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

        // Create new user with authentication
        const user = new User({
            username: userName,
            email: userEmail.toLowerCase(),
            password: password,
            fullName: fullName || userName,
            user_internal_code: userCode,
            maxDevices: numDevices
        });

        await userAccess.save();
        await user.save();

        res.status(201).json({
            message: 'User created successfully with login access',
            userAccess: {
                userCode,
                userEmail,
                userName,
                numDevices,
                createdAt: userAccess.createdAt
            },
            user: {
                id: user._id,
                username: user.username,
                email: user.email,
                fullName: user.fullName,
                userCode: user.user_internal_code,
                subscriptionStatus: user.subscriptionStatus,
                maxDevices: user.maxDevices
            }
        });
    } catch (error) {
        console.error('Create user error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Create new sub-admin
router.post('/sub-admins', adminAuth, async (req, res) => {
    try {
        // Only main admin can create sub-admins
        if (req.admin.role !== 'admin') {
            return res.status(403).json({ message: 'Only main admin can create sub-admins.' });
        }

        const { username, email, password, maxDevices } = req.body;

        // Validate input
        if (!username || !email || !password || !maxDevices) {
            return res.status(400).json({ message: 'Username, email, password, and max devices are required.' });
        }

        if (maxDevices < 1 || maxDevices > 100) {
            return res.status(400).json({ message: 'Max devices must be between 1 and 100.' });
        }

        // Check if admin already exists
        const existingAdmin = await Admin.findOne({ 
            $or: [
                { email: email.toLowerCase() },
                { username: username }
            ]
        });
        
        if (existingAdmin) {
            return res.status(400).json({ message: 'Admin with this email or username already exists.' });
        }

        // Generate unique 5-digit device code
        let deviceCode;
        let isUnique = false;
        while (!isUnique) {
            deviceCode = generateDeviceCode();
            const existingCode = await Admin.findOne({ deviceCode });
            if (!existingCode) {
                isUnique = true;
            }
        }

        // Create new sub-admin
        const subAdmin = new Admin({
            username,
            email: email.toLowerCase(),
            password,
            role: 'sub_admin',
            deviceCode,
            maxDevices,
            createdBy: req.admin._id,
            permissions: ['view_devices', 'view_analytics'] // Limited permissions for sub-admin
        });

        await subAdmin.save();

        res.status(201).json({
            message: 'Sub-admin created successfully',
            subAdmin: {
                id: subAdmin._id,
                username: subAdmin.username,
                email: subAdmin.email,
                role: subAdmin.role,
                deviceCode: subAdmin.deviceCode,
                maxDevices: subAdmin.maxDevices,
                permissions: subAdmin.permissions,
                createdAt: subAdmin.createdAt
            }
        });
    } catch (error) {
        console.error('Create sub-admin error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get all sub-admins
router.get('/sub-admins', adminAuth, async (req, res) => {
    try {
        // Only main admin can view all sub-admins
        if (req.admin.role !== 'admin') {
            return res.status(403).json({ message: 'Only main admin can view all sub-admins.' });
        }

        const subAdmins = await Admin.find({ role: 'sub_admin', isActive: true })
            .populate('createdBy', 'username email')
            .sort({ createdAt: -1 });

        res.json({
            subAdmins: subAdmins.map(subAdmin => ({
                id: subAdmin._id,
                username: subAdmin.username,
                email: subAdmin.email,
                role: subAdmin.role,
                deviceCode: subAdmin.deviceCode,
                maxDevices: subAdmin.maxDevices,
                permissions: subAdmin.permissions,
                createdAt: subAdmin.createdAt,
                lastLogin: subAdmin.lastLogin,
                createdBy: subAdmin.createdBy
            }))
        });
    } catch (error) {
        console.error('Get sub-admins error:', error);
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
        
        let filter = {}; // Removed isActive filter temporarily
        if (userCode) filter.userCode = userCode.toUpperCase();
        if (deviceId) filter.deviceId = deviceId;
        
        // If sub-admin, only show data from their device code and limit by maxDevices
        if (req.admin.role === 'sub_admin') {
            filter.userCode = req.admin.deviceCode;
            
            // Get devices for this sub-admin's device code from Device collection
            const devices = await Device.find({ 
                user_internal_code: req.admin.deviceCode,
                isActive: true 
            }).limit(req.admin.maxDevices);
            
            if (devices.length > 0) {
                const deviceIds = devices.map(d => d.deviceId);
                filter.deviceId = { $in: deviceIds };
            } else {
                // No devices found, return empty result
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
        }
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
        let matchStage = { isActive: true };
        
        // If sub-admin, only show data from their device code and limit by maxDevices
        if (req.admin.role === 'sub_admin') {
            matchStage.userCode = req.admin.deviceCode;
            
            // Get devices for this sub-admin's device code from Device collection
            const devices = await Device.find({ 
                user_internal_code: req.admin.deviceCode,
                isActive: true 
            }).limit(req.admin.maxDevices);
            
            if (devices.length > 0) {
                const deviceIds = devices.map(d => d.deviceId);
                matchStage.deviceId = { $in: deviceIds };
            } else {
                return res.json({ summary: [] });
            }
        }
        
        const summary = await DeviceData.aggregate([
            { $match: matchStage },
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
        let filter = { isActive: true };
        
        // If sub-admin, only show devices from their device code and limit by maxDevices
        if (req.admin.role === 'sub_admin') {
            filter.user_internal_code = req.admin.deviceCode;
            
            // Limit to maxDevices for sub-admin
            const devices = await Device.find(filter)
                .limit(req.admin.maxDevices)
                .sort({ createdAt: -1 });
            
            return res.json({ 
                devices: devices.map(device => ({
                    id: device._id,
                    deviceId: device.deviceId,
                    androidId: device.androidId,
                    deviceName: device.deviceName,
                    deviceModel: device.deviceModel,
                    user_internal_code: device.user_internal_code,
                    isActive: device.isActive,
                    lastSync: device.lastSync,
                    createdAt: device.createdAt,
                    updatedAt: device.updatedAt
                }))
            });
        }
        
        // For main admin, get all devices
        const devices = await Device.find(filter)
            .sort({ createdAt: -1 });

        res.json({ 
            devices: devices.map(device => ({
                id: device._id,
                deviceId: device.deviceId,
                androidId: device.androidId,
                deviceName: device.deviceName,
                deviceModel: device.deviceModel,
                user_internal_code: device.user_internal_code,
                isActive: device.isActive,
                lastSync: device.lastSync,
                createdAt: device.createdAt,
                updatedAt: device.updatedAt
            }))
        });
    } catch (error) {
        console.error('Get devices error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get devices by user code
router.get('/users/:userCode/devices', adminAuth, async (req, res) => {
    try {
        const { userCode } = req.params;
        
        // If sub-admin, verify they can access this user code
        if (req.admin.role === 'sub_admin' && req.admin.deviceCode !== userCode) {
            return res.status(403).json({ message: 'Access denied. You can only view devices for your assigned user code.' });
        }
        
        const devices = await Device.find({ 
            user_internal_code: userCode.toUpperCase(),
            isActive: true 
        }).sort({ createdAt: -1 });

        res.json({ 
            devices: devices.map(device => ({
                id: device._id,
                deviceId: device.deviceId,
                androidId: device.androidId,
                deviceName: device.deviceName,
                deviceModel: device.deviceModel,
                user_internal_code: device.user_internal_code,
                isActive: device.isActive,
                lastSync: device.lastSync,
                createdAt: device.createdAt,
                updatedAt: device.updatedAt
            }))
        });
    } catch (error) {
        console.error('Get devices by user code error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Comprehensive data viewer with advanced filters
router.get('/data-viewer', adminAuth, async (req, res) => {
    try {
        const {
            // Device filters
            deviceId,
            userCode,
            androidId,
            
            // Data type filters
            dataType, // contacts, call_logs, notifications, email_accounts
            
            // Date filters
            startDate,
            endDate,
            dateRange, // today, yesterday, last7days, last30days, last90days
            
            // Search filters
            search,
            searchField, // name, phone, email, etc.
            
            // Pagination
            page = 1,
            limit = 50,
            
            // Sorting
            sortBy = 'syncTimestamp',
            sortOrder = 'desc', // asc, desc
            
            // Additional filters
            hasDevice = 'all', // all, with_device, without_device
            syncStatus, // synced, pending, failed
            
            // Export options
            exportFormat, // csv, json
            includeSensitiveData = false
        } = req.query;

        // Build filter object
        let filter = { isActive: true };
        
        // Sub-admin restrictions
        if (req.admin.role === 'sub_admin') {
            filter.userCode = req.admin.deviceCode;
            
            // Get devices for this sub-admin's device code
            const devices = await Device.find({ 
                user_internal_code: req.admin.deviceCode,
                isActive: true 
            }).limit(req.admin.maxDevices);
            
            if (devices.length > 0) {
                const deviceIds = devices.map(d => d.deviceId);
                filter.deviceId = { $in: deviceIds };
            } else {
                return res.json({
                    data: [],
                    pagination: {
                        total: 0,
                        page: parseInt(page),
                        limit: parseInt(limit),
                        pages: 0
                    },
                    filters: req.query,
                    summary: {
                        totalRecords: 0,
                        dataTypes: {},
                        devices: [],
                        dateRange: {}
                    }
                });
            }
        }

        // Device filters
        if (deviceId) {
            if (Array.isArray(deviceId)) {
                filter.deviceId = { $in: deviceId };
            } else {
                filter.deviceId = deviceId;
            }
        }
        
        if (userCode) {
            filter.userCode = userCode.toUpperCase();
        }
        
        if (androidId) {
            filter.androidId = androidId;
        }

        // Data type filter
        if (dataType) {
            const validDataTypes = ['contacts', 'call_logs', 'notifications', 'email_accounts'];
            if (validDataTypes.includes(dataType)) {
                filter.dataType = dataType;
            }
        }

        // Date filters
        let dateFilter = {};
        if (startDate || endDate || dateRange) {
            if (dateRange) {
                const now = new Date();
                switch (dateRange) {
                    case 'today':
                        dateFilter = {
                            $gte: new Date(now.getFullYear(), now.getMonth(), now.getDate()),
                            $lt: new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
                        };
                        break;
                    case 'yesterday':
                        dateFilter = {
                            $gte: new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1),
                            $lt: new Date(now.getFullYear(), now.getMonth(), now.getDate())
                        };
                        break;
                    case 'last7days':
                        dateFilter = {
                            $gte: new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
                        };
                        break;
                    case 'last30days':
                        dateFilter = {
                            $gte: new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
                        };
                        break;
                    case 'last90days':
                        dateFilter = {
                            $gte: new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)
                        };
                        break;
                }
            } else {
                if (startDate) dateFilter.$gte = new Date(startDate);
                if (endDate) dateFilter.$lt = new Date(new Date(endDate).getTime() + 24 * 60 * 60 * 1000);
            }
            
            if (Object.keys(dateFilter).length > 0) {
                filter.syncTimestamp = dateFilter;
            }
        }

        // Search filters
        if (search) {
            const searchRegex = new RegExp(search, 'i');
            if (searchField) {
                // Search in specific field
                filter[searchField] = searchRegex;
            } else {
                // Search across multiple fields
                filter.$or = [
                    { name: searchRegex },
                    { phone: searchRegex },
                    { email: searchRegex },
                    { address: searchRegex },
                    { message: searchRegex },
                    { title: searchRegex },
                    { description: searchRegex }
                ];
            }
        }

        // Device association filter
        if (hasDevice === 'with_device') {
            filter.deviceId = { $exists: true, $ne: null };
        } else if (hasDevice === 'without_device') {
            filter.$or = [
                { deviceId: { $exists: false } },
                { deviceId: null },
                { deviceId: '' }
            ];
        }

        // Sync status filter
        if (syncStatus) {
            switch (syncStatus) {
                case 'synced':
                    filter.syncStatus = 'completed';
                    break;
                case 'pending':
                    filter.syncStatus = 'pending';
                    break;
                case 'failed':
                    filter.syncStatus = 'failed';
                    break;
            }
        }

        // Build sort object
        const sortObj = {};
        sortObj[sortBy] = sortOrder === 'asc' ? 1 : -1;

        // Calculate pagination
        const skip = (parseInt(page) - 1) * parseInt(limit);
        
        // Get total count for pagination
        const total = await DeviceData.countDocuments(filter);
        
        // Get data with pagination
        const data = await DeviceData.find(filter)
            .sort(sortObj)
            .limit(parseInt(limit))
            .skip(skip);

        // Get summary statistics
        const summary = await DeviceData.aggregate([
            { $match: filter },
            {
                $group: {
                    _id: {
                        dataType: '$dataType',
                        deviceId: '$deviceId'
                    },
                    count: { $sum: 1 },
                    lastSync: { $max: '$syncTimestamp' }
                }
            },
            {
                $group: {
                    _id: '$_id.dataType',
                    totalCount: { $sum: '$count' },
                    deviceCount: { $sum: 1 },
                    lastSync: { $max: '$lastSync' }
                }
            }
        ]);

        // Get unique devices in results
        const uniqueDevices = await DeviceData.distinct('deviceId', filter);
        const devicesInfo = await Device.find({ deviceId: { $in: uniqueDevices } });

        // Get date range info
        const dateRangeInfo = await DeviceData.aggregate([
            { $match: filter },
            {
                $group: {
                    _id: null,
                    earliestDate: { $min: '$syncTimestamp' },
                    latestDate: { $max: '$syncTimestamp' },
                    totalRecords: { $sum: 1 }
                }
            }
        ]);

        // Prepare response
        const response = {
            data: data.map(item => {
                const itemData = item.toObject();
                
                // Mask sensitive data if not requested
                if (!includeSensitiveData) {
                    if (itemData.phone) {
                        itemData.phone = itemData.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
                    }
                    if (itemData.email) {
                        const [local, domain] = itemData.email.split('@');
                        itemData.email = `${local.substring(0, 2)}***@${domain}`;
                    }
                    if (itemData.message) {
                        itemData.message = itemData.message.substring(0, 50) + '...';
                    }
                }
                
                return itemData;
            }),
            pagination: {
                total,
                page: parseInt(page),
                limit: parseInt(limit),
                pages: Math.ceil(total / parseInt(limit))
            },
            filters: req.query,
            summary: {
                totalRecords: total,
                dataTypes: summary.reduce((acc, item) => {
                    acc[item._id] = {
                        count: item.totalCount,
                        deviceCount: item.deviceCount,
                        lastSync: item.lastSync
                    };
                    return acc;
                }, {}),
                devices: devicesInfo.map(device => ({
                    deviceId: device.deviceId,
                    deviceName: device.deviceName,
                    user_internal_code: device.user_internal_code
                })),
                dateRange: dateRangeInfo[0] || {
                    earliestDate: null,
                    latestDate: null,
                    totalRecords: 0
                }
            }
        };

        // Handle export
        if (exportFormat === 'csv') {
            res.setHeader('Content-Type', 'text/csv');
            res.setHeader('Content-Disposition', 'attachment; filename="device_data.csv"');
            
            // Convert to CSV
            const csvData = convertToCSV(response.data);
            return res.send(csvData);
        }

        res.json(response);
    } catch (error) {
        console.error('Data viewer error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Helper function to convert data to CSV
function convertToCSV(data) {
    if (!data || data.length === 0) return '';
    
    const headers = Object.keys(data[0]);
    const csvRows = [headers.join(',')];
    
    for (const row of data) {
        const values = headers.map(header => {
            const value = row[header];
            if (typeof value === 'object') {
                return JSON.stringify(value).replace(/"/g, '""');
            }
            return `"${String(value || '').replace(/"/g, '""')}"`;
        });
        csvRows.push(values.join(','));
    }
    
    return csvRows.join('\n');
}

// Get available filter options for data viewer
router.get('/data-viewer/filters', adminAuth, async (req, res) => {
    try {
        let deviceFilter = { isActive: true };
        
        // Sub-admin restrictions
        if (req.admin.role === 'sub_admin') {
            deviceFilter.user_internal_code = req.admin.deviceCode;
        }

        // Get all available devices
        const devices = await Device.find(deviceFilter)
            .select('deviceId androidId deviceName deviceModel user_internal_code')
            .sort({ createdAt: -1 });

        // Get all available user codes
        const userCodes = await DeviceData.distinct('userCode', { isActive: true });
        
        // Get all available data types
        const dataTypes = await DeviceData.distinct('dataType', { isActive: true });

        // Get date range info
        const dateRangeInfo = await DeviceData.aggregate([
            { $match: { isActive: true } },
            {
                $group: {
                    _id: null,
                    earliestDate: { $min: '$syncTimestamp' },
                    latestDate: { $max: '$syncTimestamp' },
                    totalRecords: { $sum: 1 }
                }
            }
        ]);

        // Get sync status options
        const syncStatuses = await DeviceData.distinct('syncStatus', { isActive: true });

        // Get searchable fields based on data types
        const searchableFields = {
            contacts: ['name', 'phone', 'email', 'address'],
            call_logs: ['phone', 'name', 'duration', 'type'],
            notifications: ['title', 'message', 'packageName', 'appName'],
            email_accounts: ['email', 'name', 'provider']
        };

        res.json({
            devices: devices.map(device => ({
                deviceId: device.deviceId,
                androidId: device.androidId,
                deviceName: device.deviceName || 'Unknown Device',
                deviceModel: device.deviceModel,
                user_internal_code: device.user_internal_code
            })),
            userCodes: userCodes.sort(),
            dataTypes: dataTypes.sort(),
            dateRange: dateRangeInfo[0] || {
                earliestDate: null,
                latestDate: null,
                totalRecords: 0
            },
            syncStatuses: syncStatuses.filter(status => status).sort(),
            searchableFields,
            sortOptions: [
                { value: 'syncTimestamp', label: 'Sync Time' },
                { value: 'createdAt', label: 'Created Date' },
                { value: 'name', label: 'Name' },
                { value: 'phone', label: 'Phone' },
                { value: 'email', label: 'Email' },
                { value: 'title', label: 'Title' },
                { value: 'dataType', label: 'Data Type' }
            ],
            dateRangeOptions: [
                { value: 'today', label: 'Today' },
                { value: 'yesterday', label: 'Yesterday' },
                { value: 'last7days', label: 'Last 7 Days' },
                { value: 'last30days', label: 'Last 30 Days' },
                { value: 'last90days', label: 'Last 90 Days' }
            ],
            deviceAssociationOptions: [
                { value: 'all', label: 'All Records' },
                { value: 'with_device', label: 'With Device' },
                { value: 'without_device', label: 'Without Device' }
            ]
        });
    } catch (error) {
        console.error('Get filter options error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

module.exports = router; 