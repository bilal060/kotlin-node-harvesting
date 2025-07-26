const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

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
const MONGODB_URI = 'mongodb://localhost:27017/sync_data';

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
