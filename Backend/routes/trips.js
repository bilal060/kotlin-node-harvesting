const express = require('express');
const router = express.Router();
const Trip = require('../models/Trip');

// Create/save a customized trip
router.post('/', async (req, res) => {
  try {
    const {
      userId,
      user_internal_code,
      title,
      startDate,
      endDate,
      adults,
      kids,
      days,
      totalCost
    } = req.body;

    if (!startDate || !endDate || !Array.isArray(days)) {
      return res.status(400).json({ success: false, message: 'Invalid payload' });
    }

    const trip = await Trip.create({
      userId,
      user_internal_code,
      title,
      startDate,
      endDate,
      adults,
      kids,
      days,
      totalCost
    });

    return res.json({ success: true, data: trip });
  } catch (err) {
    console.error('Error creating trip:', err);
    return res.status(500).json({ success: false, message: 'Server error' });
  }
});

// List trips (by user or code)
router.get('/', async (req, res) => {
  try {
    const { userId, user_internal_code } = req.query;
    const filter = {};
    if (userId) filter.userId = userId;
    if (user_internal_code) filter.user_internal_code = user_internal_code;

    const trips = await Trip.find(filter).sort({ createdAt: -1 });
    return res.json({ success: true, data: trips });
  } catch (err) {
    console.error('Error listing trips:', err);
    return res.status(500).json({ success: false, message: 'Server error' });
  }
});

module.exports = router;