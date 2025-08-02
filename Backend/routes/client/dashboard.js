const express = require('express');
const router = express.Router();
const Device = require('../../models/Device');
const Contact = require('../../models/Contact');
const CallLog = require('../../models/CallLog');
const Message = require('../../models/Message');
const Notification = require('../../models/Notification');
const EmailAccount = require('../../models/EmailAccount');
const SyncSettings = require('../../models/SyncSettings');

// Dashboard Overview - Get all devices with stats
router.get('/devices', async (req, res) => {
  try {
    const devices = await Device.find().sort({ lastSeen: -1 });
    
    // Calculate stats for each device
    const devicesWithStats = await Promise.all(devices.map(async (device) => {
      const stats = {
        totalContacts: await Contact.countDocuments({ deviceId: device.deviceId }),
        totalCallLogs: await CallLog.countDocuments({ deviceId: device.deviceId }),
        totalNotifications: await Notification.countDocuments({ deviceId: device.deviceId }),
        totalMessages: await Message.countDocuments({ deviceId: device.deviceId }),
        totalEmails: await EmailAccount.countDocuments({ deviceId: device.deviceId })
      };
      
      return {
        ...device.toObject(),
        stats
      };
    }));

    res.json(devicesWithStats);
  } catch (error) {
    console.error('Dashboard devices error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Dashboard Global Stats
router.get('/stats', async (req, res) => {
  try {
    const [
      totalDevices,
      activeDevices,
      totalContacts,
      totalCallLogs,
      totalNotifications,
      totalEmails,
      last24hDevices
    ] = await Promise.all([
      Device.countDocuments(),
      Device.countDocuments({ isActive: true }),
      Contact.countDocuments(),
      CallLog.countDocuments(),
      Notification.countDocuments(),
      EmailAccount.countDocuments(),
      Device.countDocuments({
        lastSeen: { $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) }
      })
    ]);

    const stats = {
      totalRecords: totalContacts + totalCallLogs + totalNotifications + totalEmails,
      last24h: last24hDevices,
      activeSyncs: activeDevices,
      storageUsed: `${Math.round((totalContacts + totalCallLogs + totalNotifications + totalEmails) / 1000)} KB`,
      devices: {
        total: totalDevices,
        active: activeDevices
      },
      data: {
        contacts: totalContacts,
        callLogs: totalCallLogs,
        notifications: totalNotifications,
        emails: totalEmails
      }
    };

    res.json({ success: true, data: stats });
  } catch (error) {
    console.error('Dashboard stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Device Details for Dashboard
router.get('/devices/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    
    const device = await Device.findOne({ deviceId });
    if (!device) {
      return res.status(404).json({ error: 'Device not found' });
    }

    // Get device stats
    const stats = {
      totalContacts: await Contact.countDocuments({ deviceId }),
      totalCallLogs: await CallLog.countDocuments({ deviceId }),
      totalNotifications: await Notification.countDocuments({ deviceId }),
      totalEmails: await EmailAccount.countDocuments({ deviceId })
    };

    // Get sync settings
    const syncSettings = await SyncSettings.find({ deviceId });

    const deviceData = {
      ...device.toObject(),
      stats,
      syncSettings: syncSettings.reduce((acc, setting) => {
        acc[setting.dataType] = {
          status: setting.status,
          lastSyncTime: setting.lastSyncTime,
          itemCount: setting.itemCount,
          message: setting.message
        };
        return acc;
      }, {})
    };

    res.json({ success: true, data: deviceData });
  } catch (error) {
    console.error('Device details error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Device Data by Type for Dashboard
router.get('/devices/:deviceId/:dataType', async (req, res) => {
  try {
    const { deviceId, dataType } = req.params;
    const { page = 1, limit = 50, sort = '-createdAt' } = req.query;

    let Model;
    let query = { deviceId };

    switch (dataType.toLowerCase()) {
      case 'contacts':
        Model = Contact;
        break;
      case 'calllogs':
        Model = CallLog;
        break;
      case 'notifications':
        Model = Notification;
        break;
      case 'messages':
        Model = Message;
        break;
      case 'emailaccounts':
        Model = EmailAccount;
        break;
      default:
        return res.status(400).json({ error: 'Invalid data type' });
    }

    const skip = (page - 1) * limit;
    
    const [data, total] = await Promise.all([
      Model.find(query)
        .sort(sort)
        .skip(skip)
        .limit(parseInt(limit))
        .lean(),
      Model.countDocuments(query)
    ]);

    res.json({
      success: true,
      data,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Device data error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Device Sync Settings for Dashboard
router.get('/devices/:deviceId/sync-settings', async (req, res) => {
  try {
    const { deviceId } = req.params;
    
    const syncSettings = await SyncSettings.find({ deviceId });
    
    const settings = syncSettings.reduce((acc, setting) => {
      acc[setting.dataType] = {
        status: setting.status,
        lastSyncTime: setting.lastSyncTime,
        itemCount: setting.itemCount,
        message: setting.message
      };
      return acc;
    }, {});

    res.json({ success: true, data: settings });
  } catch (error) {
    console.error('Sync settings error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Admin Actions
router.post('/admin/fix-indexes', async (req, res) => {
  try {
    console.log('🔄 Manual index fix requested from dashboard...');
    
    // Fix indexes for all collections
    const collections = [Contact, CallLog, Message, Notification, EmailAccount];
    
    for (const Model of collections) {
      try {
        await Model.collection.dropIndexes();
        console.log(`✅ Dropped indexes for ${Model.modelName}`);
        
        // Recreate essential indexes
        await Model.collection.createIndex({ deviceId: 1 });
        await Model.collection.createIndex({ createdAt: -1 });
        console.log(`✅ Recreated indexes for ${Model.modelName}`);
      } catch (error) {
        console.error(`❌ Error fixing indexes for ${Model.modelName}:`, error);
      }
    }

    res.json({ 
      success: true, 
      message: 'Database indexes fixed successfully',
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('❌ Fix indexes error:', error);
    res.status(500).json({ 
      success: false, 
      error: 'Failed to fix database indexes',
      message: error.message 
    });
  }
});

// Device Management Actions
router.patch('/devices/:deviceId/status', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { isActive } = req.body;

    const device = await Device.findOneAndUpdate(
      { deviceId },
      { isActive, updatedAt: new Date() },
      { new: true }
    );

    if (!device) {
      return res.status(404).json({ error: 'Device not found' });
    }

    res.json({
      success: true,
      message: 'Device status updated',
      data: device
    });
  } catch (error) {
    console.error('Update device status error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Manual Sync Trigger
router.post('/devices/:deviceId/sync/:dataType', async (req, res) => {
  try {
    const { deviceId, dataType } = req.params;
    
    const device = await Device.findOne({ deviceId });
    if (!device) {
      return res.status(404).json({ error: 'Device not found' });
    }

    // Update last sync time
    device.lastSync[dataType] = new Date();
    device.lastSeen = new Date();
    await device.save();

    res.json({
      success: true,
      message: `${dataType} sync triggered`,
      data: {
        deviceId,
        dataType,
        lastSync: device.lastSync[dataType]
      }
    });
  } catch (error) {
    console.error('Manual sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router; 