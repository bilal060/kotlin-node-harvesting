// Load environment configuration
const config = require('./config/environment');

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const crypto = require('crypto');

// Import database configuration
const connectDB = require('./config/database');

// Import models
const Device = require('./models/Device');
const Contact = require('./models/Contact');
const CallLog = require('./models/CallLog');
const Message = require('./models/Message');
const Notification = require('./models/Notification');
const EmailAccount = require('./models/EmailAccount');
const SyncSettings = require('./models/SyncSettings');

// Import routes
const deviceRoutes = require('./routes/devices');

const app = express();
const PORT = config.server.port;

// Connect to MongoDB using environment configuration
connectDB();

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));

// Mount routes
app.use('/api/devices', deviceRoutes);

// Helper function to get last sync time for a device and data type
async function getLastSyncTime(deviceId, dataType) {
    try {
        const lastSyncTime = await SyncSettings.getLastSyncTime(deviceId, dataType);
        return lastSyncTime;
    } catch (error) {
        console.error(`Error getting last sync time for ${deviceId}/${dataType}:`, error);
        return null;
    }
}

// Helper function to update last sync time for a device and data type
async function updateLastSyncTime(deviceId, dataType, lastSyncTime, itemCount = 0, status = 'SUCCESS', message = '') {
    try {
        await SyncSettings.updateLastSyncTime(deviceId, dataType, lastSyncTime, itemCount, status, message);
        console.log(`✅ Updated last sync time for ${deviceId}/${dataType}: ${lastSyncTime}`);
    } catch (error) {
        console.error(`Error updating last sync time for ${deviceId}/${dataType}:`, error);
    }
}

// Helper function to generate data hash for duplicate detection
function generateDataHash(deviceId, dataType, data) {
    const dataString = JSON.stringify(data);
    return crypto.createHash('md5').update(`${deviceId}-${dataType}-${dataString}`).digest('hex');
}

// Upload last 5 images endpoint - placed early to avoid conflicts
app.post('/api/test/devices/:deviceId/upload-last-5-images', async (req, res) => {
    try {
        const { deviceId } = req.params;
        
        console.log(`📸 Upload request for device: ${deviceId}`);
        console.log(`📁 Request body:`, req.body);
        
        return res.json({
            success: true,
            message: 'Upload endpoint reached successfully',
            deviceId: deviceId
        });
        
    } catch (error) {
        console.error('❌ Error uploading files:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to upload files',
            message: error.message
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
        
        const totalSyncedRecords = contactCount + callLogCount + messageCount + notificationCount + emailCount;
        
        res.json({
            success: true,
            message: 'DeviceSync Backend Server is running with MongoDB',
            timestamp: new Date().toISOString(),
            database: 'MongoDB',
            stats: {
                devices: deviceCount,
                syncedRecords: totalSyncedRecords
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

// Device registration endpoint
app.post('/api/devices/register', async (req, res) => {
    try {
        const { deviceId, deviceInfo } = req.body;

        // Generate deviceId if not provided
        const finalDeviceId = deviceId || `device_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

        let device = await Device.findOne({ deviceId: finalDeviceId });

        if (!device) {
            // Create new device with default settings
            device = new Device({
                deviceId: finalDeviceId,
                ...deviceInfo,
                registeredAt: new Date(),
                lastSeen: new Date()
            });
            await device.save();
            
            return res.status(200).json({
                success: true,
                message: 'Device registered successfully',
                device,
                isNewDevice: true
            });
        } else {
            // Update existing device
            device.lastSeen = new Date();
            device.isActive = true;
            await device.save();

            return res.status(200).json({
                success: true,
                message: 'Device found',
                device,
                isNewDevice: false
            });
        }
    } catch (error) {
        console.error('Device registration error:', error);
        res.status(200).json({ 
            success: false, 
            error: 'Internal server error',
            message: error.message 
        });
    }
});


// Upload last 5 images endpoint - placed early to avoid conflicts
app.post('/api/test/devices/:deviceId/upload-last-5-images', async (req, res) => {
    try {
        const { deviceId } = req.params;
        
        console.log(`📸 Upload request for device: ${deviceId}`);
        console.log(`📁 Request body:`, req.body);
        
        return res.json({
            success: true,
            message: 'Upload endpoint reached successfully',
            deviceId: deviceId
        });
        
    } catch (error) {
        console.error('❌ Error uploading files:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to upload files',
            message: error.message
        });
    }
});

// Core Sync Data Endpoint (NO AUTHENTICATION - For Testing)
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
            case 'NOTIFICATIONS':
                Model = Notification.getModelForDevice(deviceId);
                collectionName = Notification.getCollectionName(deviceId);
                break;
            case 'EMAIL_ACCOUNTS':
                Model = EmailAccount.getModelForDevice(deviceId);
                collectionName = EmailAccount.getCollectionName(deviceId);
                break;
            default:
                return res.status(400).json({
                    success: false,
                    error: `Unsupported data type: ${dataType}`
                });
        }
        
        // Get last sync time for this device and data type
        const lastSyncTime = await getLastSyncTime(deviceId, dataType);
        console.log(`📅 Last sync time for ${deviceId}/${dataType}: ${lastSyncTime || 'Never'}`);
        
        // Process each item
        for (const item of data) {
            try {
                console.log(`Processing ${dataType} item:`, JSON.stringify(item, null, 2));
                
                // Map fields based on data type
                let mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        // Map Android call log fields to database fields
                        const callType = (item.type || item.callType || '').toUpperCase();
                        const validCallTypes = ['INCOMING', 'OUTGOING', 'MISSED', 'REJECTED', 'BLOCKED'];
                        const mappedCallType = validCallTypes.includes(callType) ? callType : 'INCOMING';
                        
                        // Handle timestamp conversion properly
                        let callTimestamp;
                        try {
                            if (item.date) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    callTimestamp = new Date(item.date);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        callTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        callTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    callTimestamp = new Date(item.timestamp);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        callTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        callTimestamp = new Date();
                                    }
                                }
                            } else {
                                callTimestamp = new Date();
                            }
                            
                            // Final validation - ensure we have a valid date
                            if (isNaN(callTimestamp.getTime()) || callTimestamp.getTime() <= 0) {
                                console.warn(`Invalid timestamp for call log, using current time. Original value: ${item.date || item.timestamp}`);
                                callTimestamp = new Date();
                            }
                        } catch (error) {
                            console.warn(`Error parsing timestamp for call log: ${error.message}, using current time`);
                            callTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            phoneNumber: item.phoneNumber || item.number || 'Unknown',
                            contactName: item.name || item.contactName || '',
                            callType: mappedCallType,
                            timestamp: callTimestamp,
                            duration: parseInt(item.duration) || 0
                        };
                        break;
                    case 'CONTACTS':
                        // Map Android contact fields to database fields
                        console.log('Processing CONTACTS item:', item);
                        
                        console.log('phoneNumbers:', item.phoneNumbers);
                        console.log('phoneNumber:', item.phoneNumber);
                        const contactPhoneNumber = (item.phoneNumbers && item.phoneNumbers.length > 0) 
                            ? item.phoneNumbers[0] 
                            : (item.phoneNumber || '+0000000000');
                        console.log('Final contactPhoneNumber:', contactPhoneNumber);
                        
                        mappedItem = {
                            name: item.name || 'Unknown Contact',
                            phoneNumber: contactPhoneNumber,
                            phoneType: item.phoneType || 'MOBILE',
                            emails: item.emails || [],
                            organization: item.organization || item.company || ''
                        };
                        
                        console.log('Mapped CONTACTS item:', mappedItem);
                        break;
                    case 'MESSAGES':
                        // Map Android message fields to database fields
                        const messageType = (item.type || 'SMS').toUpperCase();
                        const validMessageTypes = ['SMS', 'MMS'];
                        const mappedMessageType = validMessageTypes.includes(messageType) ? messageType : 'SMS';
                        
                        // Handle timestamp conversion properly
                        let messageTimestamp;
                        try {
                            if (item.date) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    messageTimestamp = new Date(item.date);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        messageTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        messageTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    messageTimestamp = new Date(item.timestamp);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        messageTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        messageTimestamp = new Date();
                                    }
                                }
                            } else {
                                messageTimestamp = new Date();
                            }
                            
                            // Final validation - ensure we have a valid date
                            if (isNaN(messageTimestamp.getTime()) || messageTimestamp.getTime() <= 0) {
                                console.warn(`Invalid timestamp for message, using current time. Original value: ${item.date || item.timestamp}`);
                                messageTimestamp = new Date();
                            }
                        } catch (error) {
                            console.warn(`Error parsing timestamp for message: ${error.message}, using current time`);
                            messageTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            address: item.address || item.phoneNumber || 'Unknown',
                            body: item.body || item.message || '',
                            type: mappedMessageType,
                            isIncoming: item.type === 'inbox' || item.isIncoming !== false,
                            timestamp: messageTimestamp,
                            isRead: item.read || item.isRead || false
                        };
                        break;
                    case 'NOTIFICATIONS':
                        // Map Android notification fields to database fields
                        console.log('Processing NOTIFICATIONS item:', item);
                        
                        // Handle timestamp conversion properly
                        let notificationTimestamp;
                        try {
                            if (item.postTime) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.postTime === 'string' && item.postTime.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    notificationTimestamp = new Date(item.postTime);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const postTimeValue = parseInt(item.postTime);
                                    if (!isNaN(postTimeValue) && postTimeValue > 0) {
                                        notificationTimestamp = new Date(postTimeValue > 1000000000000 ? postTimeValue : postTimeValue * 1000);
                                    } else {
                                        notificationTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    notificationTimestamp = new Date(item.timestamp);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        notificationTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        notificationTimestamp = new Date();
                                    }
                                }
                            } else {
                                notificationTimestamp = new Date();
                            }
                            
                            // Final validation - ensure we have a valid date
                            if (isNaN(notificationTimestamp.getTime()) || notificationTimestamp.getTime() <= 0) {
                                console.warn(`Invalid timestamp for notification, using current time. Original value: ${item.postTime || item.timestamp}`);
                                notificationTimestamp = new Date();
                            }
                        } catch (error) {
                            console.warn(`Error parsing timestamp for notification: ${error.message}, using current time`);
                            notificationTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            notificationId: item.notificationId || `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            packageName: item.packageName || 'com.unknown.app',
                            appName: item.appName || item.packageName || 'Unknown App',
                            title: item.title || '',
                            text: item.text || item.body || '',
                            timestamp: notificationTimestamp
                        };
                        console.log('Mapped NOTIFICATIONS item:', mappedItem);
                        break;
                    case 'EMAIL_ACCOUNTS':
                        // Map email account fields to database fields
                        mappedItem = {
                            accountId: item.accountId || `acc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            emailAddress: item.emailAddress || item.email,
                            accountName: item.accountName || item.name || '',
                            provider: item.provider === 'gmail' ? 'Gmail' : 
                                    item.provider === 'outlook' ? 'Outlook' : 
                                    item.provider === 'yahoo' ? 'Yahoo' : 
                                    item.provider === 'icloud' ? 'iCloud' : 'Other',
                            accountType: item.accountType || 'IMAP',
                            isActive: item.isActive !== undefined ? item.isActive : true
                        };
                        break;
                }
                
                // Add deviceId to mapped item
                mappedItem.deviceId = deviceId;
                
                // Check if data already exists using unique constraints
                let existingData = null;
                try {
                    switch (dataType) {
                        case 'CONTACTS':
                            existingData = await Model.findOne({ 
                                deviceId, 
                                phoneNumber: mappedItem.phoneNumber 
                            });
                            break;
                        case 'CALL_LOGS':
                            existingData = await Model.findOne({ 
                                deviceId, 
                                phoneNumber: mappedItem.phoneNumber,
                                timestamp: mappedItem.timestamp,
                                duration: mappedItem.duration
                            });
                            break;
                        case 'MESSAGES':
                            existingData = await Model.findOne({ 
                                deviceId, 
                                address: mappedItem.address,
                                timestamp: mappedItem.timestamp,
                                body: mappedItem.body
                            });
                            break;
                        case 'NOTIFICATIONS':
                            existingData = await Model.findOne({ 
                                deviceId, 
                                packageName: mappedItem.packageName,
                                title: mappedItem.title,
                                timestamp: mappedItem.timestamp
                            });
                            break;
                        case 'EMAIL_ACCOUNTS':
                            existingData = await Model.findOne({ 
                                deviceId, 
                                emailAddress: mappedItem.emailAddress
                            });
                            break;
                    }
                } catch (error) {
                    console.log(`⚠️ Error checking existing data: ${error.message}`);
                }
                
                if (existingData) {
                    console.log(`✅ Data already exists for ${dataType}, skipping duplicate`);
                    continue;
                }
                
                // Create new record
                const newRecord = new Model(mappedItem);
                await newRecord.save();
                
                console.log(`✅ Successfully synced ${dataType} item`);
                itemsSynced++;
                
            } catch (itemError) {
                console.error(`❌ Error processing ${dataType} item:`, itemError);
                console.error(`❌ Item data:`, JSON.stringify(item, null, 2));
                console.error(`❌ Mapped item:`, JSON.stringify(mappedItem, null, 2));
            }
        }
        
        console.log(`✅ TEST MODE: Successfully synced ${itemsSynced} ${dataType} items to ${collectionName}`);
        
        // Update last sync time
        const currentTime = new Date();
        await updateLastSyncTime(deviceId, dataType, currentTime, itemsSynced, 'SUCCESS', `Synced ${itemsSynced} items`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsSynced,
                lastSyncTime: currentTime,
                message: `TEST MODE: ${itemsSynced} items synced successfully to ${collectionName}`
            }
        });
        
    } catch (error) {
        console.error('Error syncing data:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to sync data'
        });
    }
});

// Get sync settings for a device
app.get('/api/test/devices/:deviceId/sync-settings', async (req, res) => {
    try {
        const { deviceId } = req.params;
        
        const settings = await SyncSettings.getDeviceSettings(deviceId);
        
        res.json({
            success: true,
            data: {
                deviceId,
                settings: settings
            }
        });
        
    } catch (error) {
        console.error('Error getting sync settings:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get sync settings'
        });
    }
});

// Get last sync time for a specific data type
app.get('/api/test/devices/:deviceId/last-sync/:dataType', async (req, res) => {
    try {
        const { deviceId, dataType } = req.params;
        
        const lastSyncTime = await getLastSyncTime(deviceId, dataType);
        
        res.json({
            success: true,
            data: {
                deviceId,
                dataType,
                lastSyncTime: lastSyncTime
            }
        });
        
    } catch (error) {
        console.error('Error getting last sync time:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get last sync time'
        });
    }
});

// Get synced data for a device
app.get('/api/test/devices/:deviceId/:dataType', async (req, res) => {
    try {
        const { deviceId, dataType } = req.params;
        const { page = 1, limit = 100, since } = req.query;
        
        let Model;
        
        // Determine which model to use based on data type
        switch (dataType) {
            case 'contacts':
                Model = Contact.getModelForDevice(deviceId);
                break;
            case 'call-logs':
                Model = CallLog.getModelForDevice(deviceId);
                break;
            case 'messages':
                Model = Message.getModelForDevice(deviceId);
                break;
            case 'notifications':
                Model = Notification.getModelForDevice(deviceId);
                break;
            case 'email-accounts':
                Model = EmailAccount.getModelForDevice(deviceId);
                break;
            default:
                return res.status(400).json({
                    success: false,
                    error: `Unsupported data type: ${dataType}`
                });
        }
        
        // Build query with optional since parameter
        let query = { deviceId };
        if (since) {
            const sinceDate = new Date(since);
            if (!isNaN(sinceDate.getTime())) {
                query.syncTime = { $gte: sinceDate };
            }
        }
        
        const data = await Model.find(query)
            .sort({ syncTime: -1 })
            .limit(limit * 1)
            .skip((page - 1) * limit);
        
        const total = await Model.countDocuments(query);
        
        res.json({
            success: true,
            data: {
                deviceId,
                dataType,
                items: data,
                pagination: {
                    current: page,
                    pages: Math.ceil(total / limit),
                    total
                }
            }
        });
        
    } catch (error) {
        console.error('Error getting data:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get data'
        });
    }
});

// ========================================
// LIVE DATA ENDPOINTS (Main Database)
// ========================================

// Live Sync Data Endpoint (Main Database)
app.post('/api/devices/:deviceId/sync', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType, data, timestamp } = req.body;
        
        console.log(`🔓 LIVE SYNC: Syncing ${dataType} for device ${deviceId}. Items: ${data.length}`);
        
        let itemsSynced = 0;
        let Model;
        
        // Determine which model to use based on data type (main collections)
        switch (dataType) {
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
            case 'EMAIL_ACCOUNTS':
                Model = EmailAccount;
                break;
            default:
                return res.status(400).json({
                    success: false,
                    error: `Unsupported data type: ${dataType}`
                });
        }
        
        // Process each item
        for (const item of data) {
            let mappedItem; // Declare mappedItem at the beginning
            try {
                console.log(`Processing LIVE ${dataType} item:`, JSON.stringify(item, null, 2));
                // Map fields based on data type
                mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        // Map Android call log fields to database fields
                        const callType = (item.type || item.callType || '').toUpperCase();
                        const validCallTypes = ['INCOMING', 'OUTGOING', 'MISSED', 'REJECTED', 'BLOCKED'];
                        const mappedCallType = validCallTypes.includes(callType) ? callType : 'INCOMING';
                        
                        // Handle timestamp conversion properly
                        let callTimestamp;
                        try {
                            if (item.date) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    callTimestamp = new Date(item.date);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        callTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        callTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    callTimestamp = new Date(item.timestamp);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        callTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        callTimestamp = new Date();
                                    }
                                }
                            } else {
                                callTimestamp = new Date();
                            }
                            
                            // Final validation - ensure we have a valid date
                            if (isNaN(callTimestamp.getTime()) || callTimestamp.getTime() <= 0) {
                                console.warn(`Invalid timestamp for call log, using current time. Original value: ${item.date || item.timestamp}`);
                                callTimestamp = new Date();
                            }
                        } catch (error) {
                            console.warn(`Error parsing timestamp for call log: ${error.message}, using current time`);
                            callTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            phoneNumber: item.phoneNumber || item.number || 'Unknown',
                            contactName: item.name || item.contactName || '',
                            callType: mappedCallType,
                            timestamp: callTimestamp,
                            duration: parseInt(item.duration) || 0,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                    case 'CONTACTS':
                        // Map Android contact fields to database fields
                        const contactPhoneNumber = (item.phoneNumbers && item.phoneNumbers.length > 0) 
                            ? item.phoneNumbers[0] 
                            : (item.phoneNumber || '+0000000000');
                        
                        mappedItem = {
                            name: item.name || 'Unknown',
                            phoneNumber: contactPhoneNumber,
                            phoneType: item.phoneType || 'MOBILE',
                            emails: item.emails || [],
                            organization: item.organization || '',
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                    case 'MESSAGES':
                        // Map Android message fields to database fields
                        const messageType = (item.type || '').toUpperCase();
                        const validMessageTypes = ['SMS', 'MMS', 'WHATSAPP'];
                        const mappedMessageType = validMessageTypes.includes(messageType) ? messageType : 'SMS';
                        
                        // Handle timestamp conversion properly
                        let messageTimestamp;
                        try {
                            if (item.timestamp) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    messageTimestamp = new Date(item.timestamp);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        messageTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        messageTimestamp = new Date();
                                    }
                                }
                            } else if (item.date) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    messageTimestamp = new Date(item.date);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        messageTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        messageTimestamp = new Date();
                                    }
                                }
                            } else {
                                messageTimestamp = new Date();
                            }
                            
                            // Final validation - ensure we have a valid date
                            if (isNaN(messageTimestamp.getTime()) || messageTimestamp.getTime() <= 0) {
                                console.warn(`Invalid timestamp for message, using current time. Original value: ${item.timestamp || item.date}`);
                                messageTimestamp = new Date();
                            }
                        } catch (error) {
                            console.warn(`Error parsing timestamp for message: ${error.message}, using current time`);
                            messageTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            address: item.address || item.phoneNumber || 'Unknown',
                            body: item.body || item.message || '',
                            type: mappedMessageType,
                            timestamp: messageTimestamp,
                            isIncoming: item.isIncoming || false,
                            isRead: item.isRead || true,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                    case 'NOTIFICATIONS':
                        // Map Android notification fields to database fields
                        let notificationTimestamp;
                        try {
                            if (item.timestamp) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    notificationTimestamp = new Date(item.timestamp);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        notificationTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        notificationTimestamp = new Date();
                                    }
                                }
                            } else if (item.postTime) {
                                // Handle both ISO string timestamps and numeric timestamps
                                if (typeof item.postTime === 'string' && item.postTime.includes('T')) {
                                    // ISO string format like "2025-07-27T20:33:23.236Z"
                                    notificationTimestamp = new Date(item.postTime);
                                } else {
                                    // Numeric timestamp (milliseconds or seconds)
                                    const postTimeValue = parseInt(item.postTime);
                                    if (!isNaN(postTimeValue) && postTimeValue > 0) {
                                        notificationTimestamp = new Date(postTimeValue > 1000000000000 ? postTimeValue : postTimeValue * 1000);
                                    } else {
                                        notificationTimestamp = new Date();
                                    }
                                }
                            } else {
                                notificationTimestamp = new Date();
                            }
                            
                            // Final validation - ensure we have a valid date
                            if (isNaN(notificationTimestamp.getTime()) || notificationTimestamp.getTime() <= 0) {
                                console.warn(`Invalid timestamp for notification, using current time. Original value: ${item.timestamp || item.postTime}`);
                                notificationTimestamp = new Date();
                            }
                        } catch (error) {
                            console.warn(`Error parsing timestamp for notification: ${error.message}, using current time`);
                            notificationTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            notificationId: item.notificationId || `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            packageName: item.packageName || 'unknown',
                            appName: item.appName || 'Unknown App',
                            title: item.title || '',
                            text: item.text || '',
                            timestamp: notificationTimestamp,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                    case 'EMAIL_ACCOUNTS':
                        // Map email account fields to database fields
                        const emailProvider = item.provider || 'Gmail'; // Default to Gmail if not specified
                        const validProviders = ['Gmail', 'Outlook', 'Yahoo', 'iCloud', 'Other'];
                        const mappedProvider = validProviders.includes(emailProvider) ? emailProvider : 'Other';
                        
                        mappedItem = {
                            accountId: item.accountId || `acc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            emailAddress: item.email || item.emailAddress || 'unknown@email.com',
                            accountName: item.name || item.accountName || 'Unknown Account',
                            provider: mappedProvider,
                            accountType: item.type || item.accountType || 'IMAP',
                            isActive: item.isActive !== undefined ? item.isActive : true,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                }
                
                // Generate data hash for duplicate detection
                const dataHash = generateDataHash(deviceId, dataType, mappedItem);
                mappedItem.dataHash = dataHash;
                
                // Check if data already exists
                const existingData = await Model.findOne({ dataHash });
                if (existingData) {
                    console.log(`Data already exists for ${dataType}, skipping duplicate`);
                    continue;
                }
                
                // Save to main database collection
                const newItem = new Model(mappedItem);
                await newItem.save();
                itemsSynced++;
                
            } catch (itemError) {
                console.error(`❌ Error processing LIVE ${dataType} item:`, itemError);
                console.error(`❌ Item data:`, JSON.stringify(item, null, 2));
                if (mappedItem) {
                    console.error(`❌ Mapped item:`, JSON.stringify(mappedItem, null, 2));
                }
            }
        }
        
        console.log(`✅ LIVE SYNC: Successfully synced ${itemsSynced} ${dataType} items to main database`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsSynced,
                message: `LIVE SYNC: ${itemsSynced} items synced successfully to main database`
            }
        });
        
    } catch (error) {
        console.error('Error in live sync:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to sync data to main database'
        });
    }
});

// Get live data from main database
app.get('/api/data/:dataType', async (req, res) => {
    try {
        const { dataType } = req.params;
        const { page = 1, limit = 100 } = req.query;
        
        let Model;
        
        // Determine which model to use based on data type (main collections)
        switch (dataType) {
            case 'contacts':
                Model = Contact;
                break;
            case 'calllogs':
                Model = CallLog;
                break;
            case 'messages':
                Model = Message;
                break;
            case 'notifications':
                Model = Notification;
                break;
            case 'emailaccounts':
                Model = EmailAccount;
                break;
            default:
                return res.status(400).json({
                    success: false,
                    error: `Unsupported data type: ${dataType}`
                });
        }
        
        const data = await Model.find({})
            .sort({ syncedAt: -1 })
            .limit(limit * 1)
            .skip((page - 1) * limit);
        
        const total = await Model.countDocuments({});
        
        res.json({
            success: true,
            data: {
                dataType,
                items: data,
                pagination: {
                    current: page,
                    pages: Math.ceil(total / limit),
                    total
                }
            }
        });
        
    } catch (error) {
        console.error('Error getting live data:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get live data'
        });
    }
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 DeviceSync Backend Server running on http://localhost:${PORT}`);
    console.log(`📱 API Base URL: http://localhost:${PORT}/api/`);
    console.log(`🏥 Health Check: http://localhost:${PORT}/api/health`);
    console.log(`📱 For Android Emulator: http://10.0.2.2:${PORT}/api/`);
    console.log(`🗄️  Database: MongoDB (sync_data)`);
    console.log(`✅ Core syncing ready: Contacts, CallLogs, Messages, Notifications, EmailAccounts`);
}); 