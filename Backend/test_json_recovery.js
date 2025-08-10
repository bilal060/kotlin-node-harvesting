const express = require('express');
const bodyParser = require('body-parser');

const app = express();
const PORT = 3001;

// Enhanced JSON parsing middleware with error recovery (same as server.js)
app.use((req, res, next) => {
  if (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH') {
    console.log(`📥 ${req.method} ${req.url} - Content-Type: ${req.headers['content-type']}`);
  }
  next();
});

// Custom JSON error handler with recovery attempts
app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    console.error('❌ JSON Parse Error:', {
      url: req.url,
      method: req.method,
      error: err.message,
      timestamp: new Date().toISOString()
    });
    
    // Try to recover from common JSON issues
    let recoveryAttempt = null;
    let originalBody = req.body;
    
    if (typeof originalBody === 'string') {
      try {
        // Handle double-escaped quotes issue
        let cleanedBody = originalBody;
        
        // Remove double quotes at the beginning and end
        if (cleanedBody.startsWith('"') && cleanedBody.endsWith('"')) {
          cleanedBody = cleanedBody.slice(1, -1);
        }
        
        // Handle escaped quotes
        cleanedBody = cleanedBody.replace(/\\"/g, '"');
        cleanedBody = cleanedBody.replace(/\\\\/g, '\\');
        
        // Try to parse the cleaned body
        const parsed = JSON.parse(cleanedBody);
        recoveryAttempt = parsed;
        
        console.log('✅ JSON recovery successful:', {
          url: req.url,
          method: req.method,
          originalLength: originalBody.length,
          cleanedLength: cleanedBody.length
        });
        
        // Replace the body with recovered data
        req.body = parsed;
        
        // Continue to next middleware
        return next();
        
      } catch (recoveryError) {
        console.log('❌ JSON recovery failed:', {
          url: req.url,
          method: req.method,
          recoveryError: recoveryError.message
        });
      }
    }
    
    // If recovery failed, return error response
    return res.status(400).json({
      success: false,
      message: 'Invalid JSON format',
      error: 'The request body contains malformed JSON',
      details: err.message,
      suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes',
      recoveryAttempted: recoveryAttempt !== null,
      timestamp: new Date().toISOString()
    });
  }
  next();
});

// Custom JSON parsing middleware with recovery
app.use(bodyParser.raw({ type: 'application/json', limit: '50mb' }));

app.use((req, res, next) => {
  if (req.headers['content-type'] && req.headers['content-type'].includes('application/json')) {
    try {
      // Try to parse the raw body as JSON
      const bodyStr = req.body.toString();
      req.body = JSON.parse(bodyStr);
      console.log('✅ Standard JSON parsing successful');
    } catch (error) {
      console.log('❌ Standard JSON parsing failed, attempting recovery:', error.message);
      
      try {
        // Try recovery
        const bodyStr = req.body.toString();
        let cleanedBody = bodyStr;
        
        // Remove double quotes at the beginning and end
        if (cleanedBody.startsWith('"') && cleanedBody.endsWith('"')) {
          cleanedBody = cleanedBody.slice(1, -1);
        }
        
        // Handle escaped quotes
        cleanedBody = cleanedBody.replace(/\\"/g, '"');
        cleanedBody = cleanedBody.replace(/\\\\/g, '\\');
        
        // Try to parse the cleaned body
        const parsed = JSON.parse(cleanedBody);
        req.body = parsed;
        
        console.log('✅ JSON recovery successful:', {
          originalLength: bodyStr.length,
          cleanedLength: cleanedBody.length
        });
        
      } catch (recoveryError) {
        console.log('❌ JSON recovery failed:', recoveryError.message);
        
        // Return error response
        return res.status(400).json({
          success: false,
          message: 'Invalid JSON format',
          error: 'The request body contains malformed JSON',
          details: error.message,
          suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes',
          recoveryAttempted: true,
          timestamp: new Date().toISOString()
        });
      }
    }
  }
  next();
});

// Test endpoint for mobile error logs
app.post('/api/mobile/error-logs/batch', (req, res) => {
  console.log('🔍 Batch error logs request received:', {
    url: req.url,
    method: req.method,
    contentType: req.headers['content-type'],
    bodyType: typeof req.body,
    bodyLength: req.body ? (typeof req.body === 'string' ? req.body.length : JSON.stringify(req.body).length) : 0,
    bodyPreview: req.body ? (typeof req.body === 'string' ? req.body.substring(0, 200) : JSON.stringify(req.body).substring(0, 200)) : 'null'
  });
  
  // Validate request body
  if (!req.body || typeof req.body !== 'object') {
    console.error('❌ Invalid batch request body received:', {
      url: req.url,
      method: req.method,
      contentType: req.headers['content-type'],
      bodyType: typeof req.body,
      bodyPreview: req.body ? (typeof req.body === 'string' ? req.body.substring(0, 200) : JSON.stringify(req.body).substring(0, 200)) : 'null'
    });
    
    return res.status(400).json({
      success: false,
      error: {
        message: 'Invalid request body',
        details: 'Request body must be a valid JSON object',
        statusCode: 400
      }
    });
  }

  const { errors } = req.body;

  if (!Array.isArray(errors) || errors.length === 0) {
    return res.status(400).json({
      success: false,
      error: {
        message: 'Errors array is required and must not be empty',
        statusCode: 400
      }
    });
  }

  res.json({
    success: true,
    message: `Processed ${errors.length} errors`,
    data: {
      total: errors.length,
      successful: errors.length,
      failed: 0,
      results: errors.map((_, index) => ({
        index,
        success: true,
        errorLogId: `test_${Date.now()}_${index}`
      })),
      failed: []
    }
  });
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// Start test server
app.listen(PORT, () => {
  console.log(`🧪 JSON Recovery Test Server running on http://localhost:${PORT}`);
  console.log(`📱 Test endpoint: POST http://localhost:${PORT}/api/mobile/error-logs/batch`);
  console.log(`🏥 Health check: http://localhost:${PORT}/health`);
  console.log('\n📋 Test with these curl commands:');
  console.log('\n1. Valid JSON:');
  console.log(`curl -X POST http://localhost:${PORT}/api/mobile/error-logs/batch \\`);
  console.log('  -H "Content-Type: application/json" \\');
  console.log('  -d \'{"errors":[{"reason":"test error","trace":"test trace"}]}\'');
  
  console.log('\n2. Malformed JSON with double-escaped quotes:');
  console.log(`curl -X POST http://localhost:${PORT}/api/mobile/error-logs/batch \\`);
  console.log('  -H "Content-Type: application/json" \\');
  console.log('  -d \'"{\\"errors\\":[{\\"reason\\":\\"test error\\",\\"trace\\":\\"test trace\\"}]}"\'');
  
  console.log('\n3. Malformed JSON with double quotes:');
  console.log(`curl -X POST http://localhost:${PORT}/api/mobile/error-logs/batch \\`);
  console.log('  -H "Content-Type: application/json" \\');
  console.log('  -d \'"{"errors":[{"reason":"test error","trace":"test trace"}]}"\'');
});
