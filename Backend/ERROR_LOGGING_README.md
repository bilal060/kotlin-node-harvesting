# Error Logging System Documentation

## 🎯 Overview

The Error Logging System is a comprehensive solution for tracking and monitoring errors across your entire application ecosystem - backend, mobile apps, and web applications. All errors are stored in a centralized MongoDB collection called `error_logs` for easy monitoring, debugging, and analysis.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Backend App   │    │   Mobile App    │    │    Web App      │
│                 │    │                 │    │                 │
│  ErrorLogger    │    │  HTTP POST      │    │  JavaScript     │
│  Service        │    │  /api/mobile/   │    │  Error Handler  │
│                 │    │  error-logs     │    │                 │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │     MongoDB Collection    │
                    │      error_logs           │
                    │                           │
                    │  - _id                   │
                    │  - app (backend/mobile)  │
                    │  - reason                │
                    │  - trace                 │
                    │  - level                 │
                    │  - source                │
                    │  - userId                │
                    │  - deviceId              │
                    │  - timestamps            │
                    │  - metadata              │
                    └───────────────────────────┘
```

## 📊 Database Schema

### ErrorLog Collection Structure

```javascript
{
  _id: ObjectId,                    // MongoDB auto-generated ID
  app: String,                      // 'backend', 'mobile', or 'web'
  reason: String,                   // Error message/reason (max 1000 chars)
  trace: String,                    // Error stack trace/details (max 5000 chars)
  level: String,                    // 'error', 'warning', 'info', 'critical'
  source: String,                   // Error source (e.g., 'api', 'database', 'mobile_app')
  userId: ObjectId,                 // Reference to User collection (optional)
  deviceId: String,                 // Device identifier (optional)
  ipAddress: String,                // IP address (optional)
  userAgent: String,                // User agent string (optional)
  endpoint: String,                 // API endpoint (optional)
  method: String,                   // HTTP method (optional)
  statusCode: Number,               // HTTP status code (optional)
  requestBody: Mixed,               // Request body (optional)
  responseBody: Mixed,              // Response body (optional)
  metadata: Mixed,                  // Additional custom data (optional)
  createdAt: Date,                  // Auto-generated timestamp
  updatedAt: Date                   // Auto-generated timestamp
}
```

## 🚀 Quick Start

### 1. Basic Error Logging

```javascript
const ErrorLogger = require('./services/errorLogger');

// Log a simple error
await ErrorLogger.logError(
  'User authentication failed',
  'Invalid credentials provided',
  { app: 'backend', level: 'warning' }
);
```

### 2. Backend Error Logging

```javascript
try {
  // Your code that might throw an error
  const result = await someAsyncOperation();
} catch (error) {
  await ErrorLogger.logBackendError(error, {
    source: 'user_authentication',
    userId: req.user?.id,
    deviceId: req.headers['x-device-id']
  });
}
```

### 3. Mobile Error Logging

```javascript
// From mobile app - send to /api/mobile/error-logs
const errorData = {
  reason: 'Network timeout while syncing data',
  trace: 'Connection failed after 30 seconds',
  level: 'warning',
  deviceId: 'android_device_123',
  platform: 'Android',
  appVersion: '1.2.3',
  osVersion: 'Android 12'
};

fetch('/api/mobile/error-logs', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(errorData)
});
```

## 🔧 Configuration

### Environment Variables

```env
# MongoDB connection
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/database

# Error logging configuration
ERROR_LOG_LEVEL=error          # Minimum level to log
ERROR_LOG_RETENTION_DAYS=30    # Days to keep error logs
ERROR_LOG_MAX_SIZE_MB=100      # Maximum size for error logs
```

### Error Levels

- **`critical`** - System-breaking errors requiring immediate attention
- **`error`** - Standard errors that affect functionality
- **`warning`** - Issues that don't break functionality but need attention
- **`info`** - Informational messages for debugging

## 📱 Mobile Integration

### Endpoint: `/api/mobile/error-logs`

**POST** - Log a single error
```json
{
  "reason": "Network timeout while syncing data",
  "trace": "Connection failed after 30 seconds",
  "level": "warning",
  "deviceId": "android_device_123",
  "platform": "Android",
  "appVersion": "1.2.3",
  "osVersion": "Android 12"
}
```

**POST** - Batch error logging (`/api/mobile/error-logs/batch`)
```json
{
  "errors": [
    {
      "reason": "Error 1",
      "trace": "Trace 1",
      "level": "error"
    },
    {
      "reason": "Error 2", 
      "trace": "Trace 2",
      "level": "warning"
    }
  ]
}
```

### Mobile App Implementation Example

```javascript
// Android (Java)
public class ErrorLogger {
    public static void logError(String reason, String trace, String level) {
        JSONObject errorData = new JSONObject();
        errorData.put("reason", reason);
        errorData.put("trace", trace);
        errorData.put("level", level);
        errorData.put("deviceId", getDeviceId());
        errorData.put("platform", "Android");
        errorData.put("appVersion", BuildConfig.VERSION_NAME);
        errorData.put("osVersion", Build.VERSION.RELEASE);
        
        // Send to server
        sendErrorToServer(errorData);
    }
}

// iOS (Swift)
class ErrorLogger {
    static func logError(reason: String, trace: String, level: String) {
        let errorData: [String: Any] = [
            "reason": reason,
            "trace": trace,
            "level": level,
            "deviceId": UIDevice.current.identifierForVendor?.uuidString ?? "",
            "platform": "iOS",
            "appVersion": Bundle.main.infoDictionary?["CFBundleShortVersionString"] ?? "",
            "osVersion": UIDevice.current.systemVersion
        ]
        
        // Send to server
        sendErrorToServer(errorData)
    }
}
```

## 🌐 Web Integration

### JavaScript Error Handler

```javascript
// Global error handler
window.addEventListener('error', async (event) => {
  const errorData = {
    reason: event.message || 'JavaScript runtime error',
    trace: event.error?.stack || 'No stack trace available',
    level: 'error',
    source: 'web_app',
    pageUrl: window.location.href,
    browser: getBrowserInfo(),
    browserVersion: getBrowserVersion()
  };
  
  // Send to server
  await sendErrorToServer(errorData);
});

// Promise rejection handler
window.addEventListener('unhandledrejection', async (event) => {
  const errorData = {
    reason: event.reason?.message || 'Unhandled promise rejection',
    trace: event.reason?.stack || 'No stack trace available',
    level: 'error',
    source: 'web_app',
    pageUrl: window.location.href
  };
  
  await sendErrorToServer(errorData);
});

async function sendErrorToServer(errorData) {
  try {
    await fetch('/api/web/error-logs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(errorData)
    });
  } catch (error) {
    console.error('Failed to send error to server:', error);
  }
}
```

## 🔍 Monitoring and Management

### Admin API Endpoints

#### Get Error Logs
```bash
GET /api/error-logs?page=1&limit=50&app=backend&level=error
```

#### Get Error Statistics
```bash
GET /api/error-logs/stats?startDate=2024-01-01&endDate=2024-01-31
```

#### Get Specific Error Log
```bash
GET /api/error-logs/:id
```

#### Delete Error Log
```bash
DELETE /api/error-logs/:id
```

#### Cleanup Old Logs
```bash
DELETE /api/error-logs/cleanup?daysOld=30
```

### Error Log Dashboard

The system provides comprehensive error monitoring capabilities:

- **Real-time Error Tracking** - Monitor errors as they occur
- **Error Analytics** - Analyze error patterns and trends
- **User Impact Analysis** - See which users are affected by errors
- **Device-specific Issues** - Identify platform-specific problems
- **Performance Metrics** - Track error frequency and resolution times

## 🛡️ Security Features

### Access Control
- Admin-only access to error logs management
- User authentication required for sensitive operations
- IP address logging for security monitoring

### Data Protection
- Sensitive data filtering in request/response bodies
- User consent for personal data logging
- GDPR compliance considerations

### Rate Limiting
- Mobile error logging rate limits
- Batch processing limits (max 100 errors per batch)
- API abuse prevention

## 📈 Performance Optimization

### Database Indexes
```javascript
// Optimized indexes for fast queries
errorLogSchema.index({ app: 1, createdAt: -1 });
errorLogSchema.index({ level: 1, createdAt: -1 });
errorLogSchema.index({ userId: 1, createdAt: -1 });
errorLogSchema.index({ deviceId: 1, createdAt: -1 });
errorLogSchema.index({ createdAt: -1 });
```

### Data Retention
- Automatic cleanup of old error logs
- Configurable retention periods
- Archive functionality for important errors

### Batch Processing
- Efficient bulk error logging
- Reduced database connections
- Improved throughput for high-volume scenarios

## 🧪 Testing

### Run Test Suite
```bash
cd Backend
node test_error_logging.js
```

### Test Configuration
```javascript
const TEST_CONFIG = {
  testBackendErrors: true,      // Test backend error logging
  testMobileErrors: true,       // Test mobile error logging
  testWebErrors: true,          // Test web error logging
  testDatabaseErrors: true,     // Test database error logging
  testPermissionErrors: true,   // Test permission error logging
  testValidationErrors: true,   // Test validation error logging
  testCriticalErrors: true,     // Test critical error logging
  testBatchLogging: true,       // Test batch error logging
  cleanupAfterTest: true        // Clean up test data after testing
};
```

## 🔧 Troubleshooting

### Common Issues

1. **MongoDB Connection Failed**
   - Check `MONGODB_URI` environment variable
   - Verify network connectivity
   - Check MongoDB service status

2. **Error Logging Fails**
   - Check database permissions
   - Verify ErrorLog model is properly imported
   - Check for validation errors

3. **High Memory Usage**
   - Reduce log retention period
   - Implement log rotation
   - Monitor database size

### Debug Mode
```javascript
// Enable debug logging
process.env.DEBUG = 'error-logging:*';

// Or set in your .env file
DEBUG=error-logging:*
```

## 📚 API Reference

### ErrorLogger Service Methods

```javascript
// General error logging
ErrorLogger.logError(reason, trace, options)

// Specific error types
ErrorLogger.logBackendError(error, options)
ErrorLogger.logMobileError(reason, trace, options)
ErrorLogger.logWebError(reason, trace, options)
ErrorLogger.logDatabaseError(error, options)
ErrorLogger.logPermissionError(reason, options)
ErrorLogger.logValidationError(reason, validationErrors, options)

// Special levels
ErrorLogger.logCriticalError(reason, trace, options)
ErrorLogger.logWarning(reason, trace, options)
ErrorLogger.logInfo(reason, trace, options)

// Utility methods
ErrorLogger.getRecentErrors(filters, limit)
ErrorLogger.cleanOldLogs(daysOld)
```

### Error Handler Middleware

```javascript
const { 
  errorHandler, 
  asyncErrorHandler, 
  notFoundHandler, 
  requestLogger 
} = require('./middleware/errorHandler');

// Apply to Express app
app.use(requestLogger);
app.use(asyncErrorHandler);
app.use(notFoundHandler);
app.use(errorHandler);
```

## 🚀 Deployment

### Production Considerations

1. **Environment Variables**
   - Set proper MongoDB connection string
   - Configure error log retention
   - Set appropriate log levels

2. **Monitoring**
   - Set up alerts for critical errors
   - Monitor database size and performance
   - Track error frequency trends

3. **Backup Strategy**
   - Regular backup of error_logs collection
   - Archive important error logs
   - Disaster recovery planning

### Docker Deployment
```dockerfile
# Example Dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 5001
CMD ["npm", "start"]
```

## 📞 Support

For questions or issues with the Error Logging System:

1. Check this documentation
2. Run the test suite to verify functionality
3. Review server logs for detailed error information
4. Check MongoDB connection and permissions

## 🔄 Version History

- **v1.0.0** - Initial release with basic error logging
- **v1.1.0** - Added mobile and web error logging
- **v1.2.0** - Enhanced security and performance features
- **v1.3.0** - Added batch processing and cleanup utilities

---

**Note**: This error logging system is designed to be robust and scalable. It automatically handles failures gracefully and provides fallback mechanisms to ensure errors are always captured, even when the logging system itself encounters issues. 