const express = require('express');
const router = express.Router();

// Import client route modules
const dashboardRoutes = require('./dashboard');

// Mount client routes
router.use('/dashboard', dashboardRoutes);

// Client health check
router.get('/health', async (req, res) => {
  try {
    res.json({
      success: true,
      message: 'Client API is healthy',
      timestamp: new Date().toISOString(),
      version: '1.0.0'
    });
  } catch (error) {
    console.error('Client health check error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router; 