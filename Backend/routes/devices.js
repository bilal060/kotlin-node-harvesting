const express = require('express');
const router = express.Router();
const Device = require('../models/Device');

// Register or get device - SIMPLIFIED
router.post('/register', async (req, res) => {
  try {
    console.log('📱 Device registration request body:', JSON.stringify(req.body, null, 2));
    
    // Extract deviceId from request or generate one if not provided
    let deviceId = req.body.deviceId;
    
    if (!deviceId) {
      deviceId = `device_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      console.log(`📱 No deviceId provided, generated: ${deviceId}`);
    }

    console.log(`📱 Checking if device exists: ${deviceId}`);

    // Check if device already exists
    const existingDevice = await Device.findOne({ deviceId: deviceId });

    if (existingDevice) {
      console.log(`✅ Device already exists: ${deviceId}`);
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
    
    console.log(`✅ New device registered: ${deviceId}`);
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

// Get device settings
router.get('/:deviceId/settings', async (req, res) => {
  try {
    const { deviceId } = req.params;
    
    const device = await Device.findOne({ deviceId });
    
    if (!device) {
      return res.status(404).json({ error: 'Device not found' });
    }

    // Update last seen
    device.lastSeen = new Date();
    await device.save();

    res.json({
      settings: device.settings,
      lastSync: device.lastSync,
      stats: device.stats
    });
  } catch (error) {
    console.error('Get settings error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Update device settings (admin endpoint)
router.put('/:deviceId/settings', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { settings } = req.body;

    const device = await Device.findOne({ deviceId });
    
    if (!device) {
      return res.status(404).json({ error: 'Device not found' });
    }

    device.settings = { ...device.settings, ...settings };
    device.updatedAt = new Date();
    await device.save();

    res.json({
      message: 'Settings updated successfully',
      settings: device.settings
    });
  } catch (error) {
    console.error('Update settings error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Update last sync timestamp
router.post('/:deviceId/sync/:dataType', async (req, res) => {
  try {
    const { deviceId, dataType } = req.params;
    
    const device = await Device.findOne({ deviceId });
    
    if (!device) {
      return res.status(404).json({ error: 'Device not found' });
    }

    device.lastSync[dataType] = new Date();
    device.lastSeen = new Date();
    await device.save();

    res.json({
      message: `${dataType} sync timestamp updated`,
      lastSync: device.lastSync
    });
  } catch (error) {
    console.error('Update sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get all devices with advanced filtering and pagination
router.get('/', async (req, res) => {
  try {
    const { 
      page = 1, 
      limit = 100, 
      search, 
      dateFilter = 'all',
      filterStatus = 'all',
      sortBy = 'lastSeen',
      sortOrder = 'desc'
    } = req.query;

    let query = {};
    
    // Search filter
    if (search) {
      query.$or = [
        { deviceName: { $regex: search, $options: 'i' } },
        { deviceId: { $regex: search, $options: 'i' } },
        { model: { $regex: search, $options: 'i' } },
        { manufacturer: { $regex: search, $options: 'i' } },
        { userName: { $regex: search, $options: 'i' } }
      ];
    }

    // Status filter
    if (filterStatus !== 'all') {
      query.isActive = filterStatus === 'active';
    }

    // Date filter
    if (dateFilter !== 'all') {
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      const last7Days = new Date(today);
      last7Days.setDate(last7Days.getDate() - 7);
      const last30Days = new Date(today);
      last30Days.setDate(last30Days.getDate() - 30);

      switch (dateFilter) {
        case 'today':
          query.createdAt = { $gte: today };
          break;
        case 'yesterday':
          query.createdAt = { $gte: yesterday, $lt: today };
          break;
        case 'last7days':
          query.createdAt = { $gte: last7Days };
          break;
        case 'last30days':
          query.createdAt = { $gte: last30Days };
          break;
      }
    }

    // Sort options
    const sortOptions = {};
    sortOptions[sortBy] = sortOrder === 'desc' ? -1 : 1;

    const devices = await Device.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));

    const total = await Device.countDocuments(query);

    res.json({
      success: true,
      data: devices,
      pagination: {
        current: parseInt(page),
        pages: Math.ceil(total / parseInt(limit)),
        total,
        limit: parseInt(limit)
      },
      filters: {
        search,
        dateFilter,
        filterStatus,
        sortBy,
        sortOrder
      }
    });
  } catch (error) {
    console.error('Get devices error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

// Update device status
router.patch('/:deviceId/status', async (req, res) => {
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
      message: 'Device status updated',
      device
    });
  } catch (error) {
    console.error('Update status error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete device
router.delete('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;

    const device = await Device.findOneAndDelete({ deviceId });

    if (!device) {
      return res.status(404).json({ 
        success: false,
        error: 'Device not found' 
      });
    }

    res.json({
      success: true,
      message: 'Device deleted successfully',
      device
    });
  } catch (error) {
    console.error('Delete device error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

module.exports = router;
