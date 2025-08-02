const express = require('express');
const router = express.Router();

// Basic user routes
router.get('/health', (req, res) => {
    res.json({ status: 'User routes working' });
});

module.exports = router; 