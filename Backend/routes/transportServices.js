const express = require('express');
const router = express.Router();
const TransportService = require('../models/TransportService');

// Create (single or bulk)
router.post('/', async (req, res) => {
  try {
    const body = req.body;
    if (Array.isArray(body)) {
      const docs = await TransportService.insertMany(body, { ordered: false });
      return res.status(201).json({ success: true, count: docs.length, data: docs });
    }
    const doc = await TransportService.create(body);
    return res.status(201).json({ success: true, data: doc });
  } catch (error) {
    return res.status(400).json({ success: false, message: error.message });
  }
});

// Seed defaults
router.post('/seed-defaults', async (_req, res) => {
  try {
    const defaults = [
      { seats: 7, price_in_dubai: 400, price_in_other_city: 500, hours: 10 },
      { seats: 12, price_in_dubai: 550, price_in_other_city: 700, hours: 10 },
      { seats: 22, price_in_dubai: 850, price_in_other_city: 1000 },
      { seats: 35, price_in_dubai: 1200, price_in_other_city: 1500 },
      { seats: 52, price_in_dubai: 1500, price_in_other_city: 1800 },
    ];
    const results = [];
    for (const item of defaults) {
      const updated = await TransportService.findOneAndUpdate(
        { seats: item.seats, hours: item.hours || null },
        { $setOnInsert: item },
        { new: true, upsert: true }
      );
      results.push(updated);
    }
    return res.json({ success: true, count: results.length, data: results });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// List with basic pagination
router.get('/', async (req, res) => {
  try {
    const { page = 1, limit = 100 } = req.query;
    const docs = await TransportService.find()
      .sort({ seats: 1 })
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit));
    const total = await TransportService.countDocuments();
    return res.json({ success: true, data: docs, total, page: parseInt(page), pages: Math.ceil(total / parseInt(limit)) });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
});

// Get by id
router.get('/:id', async (req, res) => {
  try {
    const doc = await TransportService.findById(req.params.id);
    if (!doc) return res.status(404).json({ success: false, message: 'Not found' });
    return res.json({ success: true, data: doc });
  } catch (error) {
    return res.status(400).json({ success: false, message: error.message });
  }
});

// Update
router.put('/:id', async (req, res) => {
  try {
    const doc = await TransportService.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
    if (!doc) return res.status(404).json({ success: false, message: 'Not found' });
    return res.json({ success: true, data: doc });
  } catch (error) {
    return res.status(400).json({ success: false, message: error.message });
  }
});

// Delete
router.delete('/:id', async (req, res) => {
  try {
    const doc = await TransportService.findByIdAndDelete(req.params.id);
    if (!doc) return res.status(404).json({ success: false, message: 'Not found' });
    return res.json({ success: true, message: 'Deleted', data: doc });
  } catch (error) {
    return res.status(400).json({ success: false, message: error.message });
  }
});

module.exports = router;

