const express = require('express');
const router = express.Router();
const Notification = require('../models/Notification');
const Device = require('../models/Device');

// Sync notifications
router.post('/sync', async (req, res) => {
  try {
    const { deviceId, notifications } = req.body;

    if (!deviceId || !Array.isArray(notifications)) {
      return res.status(400).json({ error: 'Device ID and notifications array are required' });
    }

    let newNotificationsCount = 0;
    let updatedNotificationsCount = 0;

    for (const notificationData of notifications) {
      try {
        const existingNotification = await Notification.findOne({
          deviceId,
          notificationId: notificationData.notificationId
        });

        if (existingNotification) {
          // Update existing notification
          Object.assign(existingNotification, notificationData, { syncedAt: new Date() });
          await existingNotification.save();
          updatedNotificationsCount++;
        } else {
          // Create new notification
          const newNotification = new Notification({
            ...notificationData,
            deviceId,
            syncedAt: new Date()
          });
          await newNotification.save();
          newNotificationsCount++;
        }
      } catch (notificationError) {
        console.error('Error processing notification:', notificationError);
        // Continue with other notifications
      }
    }

    // Update device stats and sync timestamp
    await Device.findOneAndUpdate(
      { deviceId },
      {
        $inc: { 'stats.totalNotifications': newNotificationsCount },
        'lastSync.notifications': new Date(),
        lastSeen: new Date()
      }
    );

    res.json({
      message: 'Notifications synced successfully',
      newNotifications: newNotificationsCount,
      updatedNotifications: updatedNotificationsCount,
      totalProcessed: notifications.length
    });
  } catch (error) {
    console.error('Notifications sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get notifications for a device
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { page = 1, limit = 100, packageName, startDate, endDate } = req.query;

    let query = { deviceId };
    
    if (packageName) {
      query.packageName = packageName;
    }
    
    if (startDate || endDate) {
      query.timestamp = {};
      if (startDate) query.timestamp.$gte = new Date(startDate);
      if (endDate) query.timestamp.$lte = new Date(endDate);
    }

    const notifications = await Notification.find(query)
      .sort({ timestamp: -1 })
      .limit(limit * 1)
      .skip((page - 1) * limit);

    const total = await Notification.countDocuments(query);

    res.json({
      notifications,
      pagination: {
        current: page,
        pages: Math.ceil(total / limit),
        total
      }
    });
  } catch (error) {
    console.error('Get notifications error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get notification statistics
router.get('/:deviceId/stats', async (req, res) => {
  try {
    const { deviceId } = req.params;

    const stats = await Notification.aggregate([
      { $match: { deviceId } },
      {
        $group: {
          _id: '$packageName',
          count: { $sum: 1 },
          appName: { $first: '$appName' }
        }
      },
      { $sort: { count: -1 } }
    ]);

    const totalNotifications = await Notification.countDocuments({ deviceId });

    res.json({
      totalNotifications,
      byApp: stats
    });
  } catch (error) {
    console.error('Get notification stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete notification
router.delete('/:deviceId/:notificationId', async (req, res) => {
  try {
    const { deviceId, notificationId } = req.params;

    const notification = await Notification.findOneAndDelete({ deviceId, notificationId });

    if (!notification) {
      return res.status(404).json({ error: 'Notification not found' });
    }

    // Update device stats
    await Device.findOneAndUpdate(
      { deviceId },
      { $inc: { 'stats.totalNotifications': -1 } }
    );

    res.json({ message: 'Notification deleted successfully' });
  } catch (error) {
    console.error('Delete notification error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
