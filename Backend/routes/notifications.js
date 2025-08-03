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

    // Get device information to extract user_internal_code
    const device = await Device.findOne({ deviceId });
    const user_internal_code = device?.user_internal_code || 'DEFAULT';

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
            user_internal_code: user_internal_code,
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

// Get notifications for a device with advanced filtering and pagination
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { 
      page = 1, 
      limit = 100, 
      search, 
      dateFilter = 'all',
      packageName,
      sortBy = 'timestamp',
      sortOrder = 'desc'
    } = req.query;

    let query = { deviceId };
    
    // Search filter
    if (search) {
      query.$or = [
        { packageName: { $regex: search, $options: 'i' } },
        { title: { $regex: search, $options: 'i' } },
        { text: { $regex: search, $options: 'i' } }
      ];
    }

    // Package name filter
    if (packageName) {
      query.packageName = packageName;
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
          query.timestamp = { $gte: today };
          break;
        case 'yesterday':
          query.timestamp = { $gte: yesterday, $lt: today };
          break;
        case 'last7days':
          query.timestamp = { $gte: last7Days };
          break;
        case 'last30days':
          query.timestamp = { $gte: last30Days };
          break;
      }
    }

    // Sort options
    const sortOptions = {};
    sortOptions[sortBy] = sortOrder === 'desc' ? -1 : 1;

    const notifications = await Notification.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));

    const total = await Notification.countDocuments(query);

    res.json({
      success: true,
      data: notifications,
      pagination: {
        current: parseInt(page),
        pages: Math.ceil(total / parseInt(limit)),
        total,
        limit: parseInt(limit)
      },
      filters: {
        search,
        dateFilter,
        packageName,
        sortBy,
        sortOrder
      }
    });
  } catch (error) {
    console.error('Get notifications error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
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
