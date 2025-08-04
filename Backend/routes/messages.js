const express = require('express');
const router = express.Router();
const Message = require('../models/Message');
const Device = require('../models/Device');
const { queueMiddleware } = require('../middleware/queueMiddleware');
const colors = require('colors');

// Sync messages with queue middleware
router.post('/sync', queueMiddleware('messages'), async (req, res) => {
  try {
    const { deviceId, messages } = req.body;

    if (!deviceId || !Array.isArray(messages)) {
      return res.status(400).json({ error: 'Device ID and messages array are required' });
    }

    console.log(colors.blue(`💬 Processing ${messages.length} messages for device ${deviceId}`));

    // Get device information to extract user_internal_code
    const device = await Device.findOne({ deviceId });
    const user_internal_code = device?.user_internal_code || 'DEFAULT';

    let newMessagesCount = 0;
    let updatedMessagesCount = 0;
    let errorCount = 0;

    for (const messageData of messages) {
      try {
        const existingMessage = await Message.findOne({
          deviceId,
          messageId: messageData.messageId
        });

        if (existingMessage) {
          // Update existing message
          Object.assign(existingMessage, messageData, { syncedAt: new Date() });
          await existingMessage.save();
          updatedMessagesCount++;
        } else {
          // Create new message
          const newMessage = new Message({
            ...messageData,
            deviceId,
            user_internal_code: user_internal_code,
            syncedAt: new Date()
          });
          await newMessage.save();
          newMessagesCount++;
        }
      } catch (messageError) {
        console.error(colors.red('Error processing message:', messageError));
        errorCount++;
        // Continue with other messages
      }
    }

    // Update device stats and sync timestamp
    await Device.findOneAndUpdate(
      { deviceId },
      {
        $inc: { 'stats.totalMessages': newMessagesCount },
        'lastSync.messages': new Date(),
        lastSeen: new Date()
      }
    );

    res.json({
      success: true,
      message: 'Messages synced successfully',
      newMessages: newMessagesCount,
      updatedMessages: updatedMessagesCount,
      errorCount: errorCount,
      totalProcessed: messages.length,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error(colors.red('Messages sync error:', error));
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

// Get messages for a device with advanced filtering and pagination
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { 
      page = 1, 
      limit = 100, 
      search, 
      dateFilter = 'all',
      type,
      sortBy = 'timestamp',
      sortOrder = 'desc'
    } = req.query;

    let query = { deviceId };
    
    // Search filter
    if (search) {
      query.$or = [
        { body: { $regex: search, $options: 'i' } },
        { address: { $regex: search, $options: 'i' } },
        { type: { $regex: search, $options: 'i' } }
      ];
    }

    // Message type filter
    if (type) {
      query.type = type;
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

    const messages = await Message.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));

    const total = await Message.countDocuments(query);

    res.json({
      success: true,
      data: messages,
      pagination: {
        current: parseInt(page),
        pages: Math.ceil(total / parseInt(limit)),
        total,
        limit: parseInt(limit)
      },
      filters: {
        search,
        dateFilter,
        type,
        sortBy,
        sortOrder
      }
    });
  } catch (error) {
    console.error('Get messages error:', error);
    res.status(500).json({ 
      success: false,
      error: 'Internal server error',
      message: error.message 
    });
  }
});

// Get message statistics
router.get('/:deviceId/stats', async (req, res) => {
  try {
    const { deviceId } = req.params;

    const stats = await Message.aggregate([
      { $match: { deviceId } },
      {
        $group: {
          _id: {
            type: '$type',
            isIncoming: '$isIncoming'
          },
          count: { $sum: 1 }
        }
      }
    ]);

    const totalMessages = await Message.countDocuments({ deviceId });

    const typeStats = await Message.aggregate([
      { $match: { deviceId } },
      {
        $group: {
          _id: '$type',
          count: { $sum: 1 },
          unreadCount: {
            $sum: {
              $cond: [{ $eq: ['$isRead', false] }, 1, 0]
            }
          }
        }
      }
    ]);

    res.json({
      totalMessages,
      byTypeAndDirection: stats,
      byType: typeStats
    });
  } catch (error) {
    console.error('Get message stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});



// Delete message
router.delete('/:deviceId/:messageId/:type', async (req, res) => {
  try {
    const { deviceId, messageId, type } = req.params;

    const message = await Message.findOneAndDelete({ deviceId, messageId, type });

    if (!message) {
      return res.status(404).json({ error: 'Message not found' });
    }

    // Update device stats
    await Device.findOneAndUpdate(
      { deviceId },
      { $inc: { 'stats.totalMessages': -1 } }
    );

    res.json({ message: 'Message deleted successfully' });
  } catch (error) {
    console.error('Delete message error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
