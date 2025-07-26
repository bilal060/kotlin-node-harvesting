const express = require('express');
const router = express.Router();
const Device = require('../models/Device');

// Register or get device
router.post('/register', async (req, res) => {
  try {
    const { deviceId, deviceInfo } = req.body;

    if (!deviceId) {
      return res.status(400).json({ error: 'Device ID is required' });
    }

    let device = await Device.findOne({ deviceId });

    if (!device) {
      // Create new device with default settings
      device = new Device({
        deviceId,
        ...deviceInfo,
        registeredAt: new Date(),
        lastSeen: new Date()
      });
      await device.save();
      
      return res.status(201).json({
        message: 'Device registered successfully',
        device,
        isNewDevice: true
      });
    } else {
      // Update existing device
      device.lastSeen = new Date();
      device.isActive = true;
      await device.save();

      return res.json({
        message: 'Device found',
        device,
        isNewDevice: false
      });
    }
  } catch (error) {
    console.error('Device registration error:', error);
    res.status(500).json({ error: 'Internal server error' });
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

// Get all devices (admin endpoint)
router.get('/', async (req, res) => {
  try {
    const devices = await Device.find().sort({ lastSeen: -1 });
    res.json(devices);
  } catch (error) {
    console.error('Get devices error:', error);
    res.status(500).json({ error: 'Internal server error' });
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

module.exports = router;
