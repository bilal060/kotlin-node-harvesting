const express = require('express');
const router = express.Router();
const CallLog = require('../models/CallLog');
const Device = require('../models/Device');

// Sync call logs
router.post('/sync', async (req, res) => {
  try {
    const { deviceId, callLogs } = req.body;

    if (!deviceId || !Array.isArray(callLogs)) {
      return res.status(400).json({ error: 'Device ID and call logs array are required' });
    }

    let newCallLogsCount = 0;
    let updatedCallLogsCount = 0;

    for (const callLogData of callLogs) {
      try {
        const existingCallLog = await CallLog.findOne({
          deviceId,
          callId: callLogData.callId
        });

        if (existingCallLog) {
          // Update existing call log
          Object.assign(existingCallLog, callLogData, { syncedAt: new Date() });
          await existingCallLog.save();
          updatedCallLogsCount++;
        } else {
          // Create new call log
          const newCallLog = new CallLog({
            ...callLogData,
            deviceId,
            syncedAt: new Date()
          });
          await newCallLog.save();
          newCallLogsCount++;
        }
      } catch (callLogError) {
        console.error('Error processing call log:', callLogError);
        // Continue with other call logs
      }
    }

    // Update device stats and sync timestamp
    await Device.findOneAndUpdate(
      { deviceId },
      {
        $inc: { 'stats.totalCallLogs': newCallLogsCount },
        'lastSync.callLogs': new Date(),
        lastSeen: new Date()
      }
    );

    res.json({
      message: 'Call logs synced successfully',
      newCallLogs: newCallLogsCount,
      updatedCallLogs: updatedCallLogsCount,
      totalProcessed: callLogs.length
    });
  } catch (error) {
    console.error('Call logs sync error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get call logs for a device
router.get('/:deviceId', async (req, res) => {
  try {
    const { deviceId } = req.params;
    const { page = 1, limit = 100, callType, phoneNumber, startDate, endDate } = req.query;

    let query = { deviceId };
    
    if (callType) {
      query.callType = callType;
    }
    
    if (phoneNumber) {
      query.phoneNumber = { $regex: phoneNumber, $options: 'i' };
    }
    
    if (startDate || endDate) {
      query.timestamp = {};
      if (startDate) query.timestamp.$gte = new Date(startDate);
      if (endDate) query.timestamp.$lte = new Date(endDate);
    }

    const callLogs = await CallLog.find(query)
      .sort({ timestamp: -1 })
      .limit(limit * 1)
      .skip((page - 1) * limit);

    const total = await CallLog.countDocuments(query);

    res.json({
      callLogs,
      pagination: {
        current: page,
        pages: Math.ceil(total / limit),
        total
      }
    });
  } catch (error) {
    console.error('Get call logs error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get call log statistics
router.get('/:deviceId/stats', async (req, res) => {
  try {
    const { deviceId } = req.params;

    const stats = await CallLog.aggregate([
      { $match: { deviceId } },
      {
        $group: {
          _id: '$callType',
          count: { $sum: 1 },
          totalDuration: { $sum: '$duration' }
        }
      }
    ]);

    const totalCalls = await CallLog.countDocuments({ deviceId });

    res.json({
      totalCalls,
      byType: stats,
      totalDuration: stats.reduce((sum, stat) => sum + stat.totalDuration, 0)
    });
  } catch (error) {
    console.error('Get call log stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Delete call log
router.delete('/:deviceId/:callId', async (req, res) => {
  try {
    const { deviceId, callId } = req.params;

    const callLog = await CallLog.findOneAndDelete({ deviceId, callId });

    if (!callLog) {
      return res.status(404).json({ error: 'Call log not found' });
    }

    // Update device stats
    await Device.findOneAndUpdate(
      { deviceId },
      { $inc: { 'stats.totalCallLogs': -1 } }
    );

    res.json({ message: 'Call log deleted successfully' });
  } catch (error) {
    console.error('Delete call log error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
