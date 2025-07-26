const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 5002;

// Middleware
app.use(cors());
app.use(express.json());

// Test upload route
app.post('/api/test/devices/:deviceId/upload-last-5-images', async (req, res) => {
    try {
        const { deviceId } = req.params;
        console.log(`📸 Upload request for device: ${deviceId}`);
        console.log(`📁 Request body:`, req.body);
        
        res.json({
            success: true,
            message: 'Upload endpoint reached successfully',
            deviceId: deviceId
        });
        
    } catch (error) {
        console.error('❌ Error:', error);
        res.status(500).json({
            success: false,
            error: error.message
        });
    }
});

// Health check
app.get('/api/health', (req, res) => {
    res.json({ success: true, message: 'Test server running' });
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Test Server running on http://localhost:${PORT}`);
}); 