# JSON Parsing Enhancements with Error Recovery

## 🚨 Problem Description

The server was experiencing `SyntaxError: Unexpected token '"', ""{\\"errors"... is not valid JSON` errors when receiving requests from the Android mobile app. This was caused by malformed JSON with:

1. **Double-escaped quotes**: `""{\\"errors\\"...`
2. **Double quotes wrapping**: `"{"errors":[...]}"`
3. **Improper escaping**: Mixed quote escaping patterns

## 🔧 Solution Implemented

### 1. Enhanced JSON Parsing Middleware

Added robust JSON parsing middleware with automatic error recovery:

```javascript
// Custom JSON error handler with recovery attempts
app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    // Try to recover from common JSON issues
    if (typeof req.body === 'string') {
      try {
        let cleanedBody = req.body;
        
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
        
        // Continue to next middleware
        return next();
        
      } catch (recoveryError) {
        console.log('❌ JSON recovery failed:', recoveryError.message);
      }
    }
    
    // Return error response if recovery failed
    return res.status(400).json({
      success: false,
      message: 'Invalid JSON format',
      error: 'The request body contains malformed JSON',
      details: err.message,
      suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes',
      recoveryAttempted: true,
      timestamp: new Date().toISOString()
    });
  }
  next();
});
```

### 2. Enhanced Body Parser with Recovery

Modified `bodyParser.json()` to include recovery attempts in the `verify` function:

```javascript
app.use(bodyParser.json({ 
  limit: '50mb',
  verify: (req, res, buf) => {
    try {
      JSON.parse(buf);
    } catch (e) {
      // Try recovery before throwing error
      const bodyStr = buf.toString();
      let cleanedBody = bodyStr;
      
      // Handle double-escaped quotes issue
      if (cleanedBody.startsWith('"') && cleanedBody.endsWith('"')) {
        cleanedBody = cleanedBody.slice(1, -1);
      }
      
      // Handle escaped quotes
      cleanedBody = cleanedBody.replace(/\\"/g, '"');
      cleanedBody = cleanedBody.replace(/\\\\/g, '\\');
      
      try {
        JSON.parse(cleanedBody);
        console.log('✅ JSON recovery in verify function successful');
        req.body = cleanedBody;
        return;
      } catch (recoveryError) {
        console.log('❌ JSON recovery in verify function failed:', recoveryError.message);
      }
      
      throw new Error('Invalid JSON format');
    }
  }
}));
```

### 3. Special Mobile Error Logs Middleware

Added specific middleware for the `/api/mobile/error-logs` route:

```javascript
// Special middleware for mobile error logs to handle malformed JSON
app.use('/api/mobile/error-logs', (req, res, next) => {
  if (req.method === 'POST') {
    console.log('🔍 Mobile error logs request intercepted:', {
      url: req.url,
      method: req.method,
      contentType: req.headers['content-type'],
      bodyLength: req.body ? (typeof req.body === 'string' ? req.body.length : JSON.stringify(req.body).length) : 0
    });
    
    // If body is a string (malformed JSON), try to fix it
    if (typeof req.body === 'string') {
      try {
        let cleanedBody = req.body;
        
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
        
        console.log('✅ Mobile error logs JSON recovery successful');
        
      } catch (recoveryError) {
        console.log('❌ Mobile error logs JSON recovery failed:', recoveryError.message);
      }
    }
  }
  next();
});
```

### 4. Enhanced Error Logging

Improved error logging with detailed information:

```javascript
console.error('❌ JSON Parse Error:', {
  url: req.url,
  method: req.method,
  error: err.message,
  timestamp: new Date().toISOString()
});
```

### 5. Global Error Handler

Added comprehensive global error handler for unhandled errors:

```javascript
// Global error handler for unhandled errors
app.use((err, req, res, next) => {
  console.error('❌ Unhandled Error:', {
    url: req.url,
    method: req.method,
    error: err.message,
    stack: err.stack,
    timestamp: new Date().toISOString()
  });

  // Handle JSON parsing errors
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    return res.status(400).json({
      success: false,
      message: 'Invalid JSON format',
      error: 'The request body contains malformed JSON',
      details: err.message,
      suggestion: 'Please check your JSON syntax and ensure proper escaping of quotes'
    });
  }

  // Handle other errors
  return res.status(500).json({
    success: false,
    message: 'Internal server error',
    error: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong'
  });
});
```

## 🧪 Testing

### Test Server

Created `test_json_recovery.js` to test the JSON recovery functionality:

```bash
node test_json_recovery.js
```

### Test Cases

1. **Valid JSON**: `{"errors":[{"reason":"test error","trace":"test trace"}]}`
2. **Malformed JSON with double-escaped quotes**: `"{\\"errors\\":[{\\"reason\\":\\"test error\\",\\"trace\\":\\"test trace\\"}]}"`
3. **Malformed JSON with double quotes**: `"{"errors":[{"reason":"test error","trace":"test trace"}]}"`

### Test Commands

```bash
# Valid JSON
curl -X POST http://localhost:3001/api/mobile/error-logs/batch \
  -H "Content-Type: application/json" \
  -d '{"errors":[{"reason":"test error","trace":"test trace"}]}'

# Malformed JSON with double-escaped quotes
curl -X POST http://localhost:3001/api/mobile/error-logs/batch \
  -H "Content-Type: application/json" \
  -d '"{\\"errors\\":[{\\"reason\\":\\"test error\\",\\"trace\\":\\"test trace\\"}]}"'

# Malformed JSON with double quotes
curl -X POST http://localhost:3001/api/mobile/error-logs/batch \
  -H "Content-Type: application/json" \
  -d '"{"errors":[{"reason":"test error","trace":"test trace"}]}"'
```

## 📊 Benefits

1. **Automatic Recovery**: Server automatically attempts to fix common JSON issues
2. **Better Error Messages**: Detailed error responses with recovery status
3. **Improved Logging**: Enhanced debugging information for troubleshooting
4. **Graceful Degradation**: Server continues to function even with malformed requests
5. **Production Resilience**: Handles edge cases that might occur in production

## 🔍 Root Cause Analysis

The malformed JSON issue was caused by:

1. **Android Serialization**: The Android app was sending JSON as strings instead of proper JSON objects
2. **Double Escaping**: Multiple layers of escaping during JSON serialization
3. **Quote Wrapping**: JSON being wrapped in additional quotes

## 🚀 Future Improvements

1. **Android App Fix**: Update the Android app to send proper JSON objects
2. **Monitoring**: Add metrics for JSON recovery success/failure rates
3. **Rate Limiting**: Implement rate limiting for malformed requests
4. **Alerting**: Set up alerts for high JSON parsing error rates

## 📝 Files Modified

- `Backend/server.js` - Enhanced JSON parsing middleware
- `Backend/routes/mobileErrorLogs.js` - Improved error logging
- `Backend/test_json_recovery.js` - Test server for verification
- `Backend/JSON_PARSING_ENHANCEMENTS.md` - This documentation

## ✅ Status

- **Implemented**: ✅ JSON parsing with error recovery
- **Tested**: ✅ Local testing with test server
- **Deployed**: ✅ Pushed to production
- **Monitoring**: 🔍 Production monitoring in progress
