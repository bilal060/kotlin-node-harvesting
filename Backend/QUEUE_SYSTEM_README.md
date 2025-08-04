# Queue System for DeviceSync Backend

## Overview

The Queue System is designed to handle large data sets efficiently by automatically queuing data when it exceeds a threshold of 500 items. This system ensures optimal performance and prevents server overload when processing massive amounts of device data.

## Features

### 🎯 Core Features
- **Automatic Queuing**: Data sets > 500 items are automatically queued
- **Batch Processing**: Data is processed in batches of 100 items
- **Retry Logic**: Failed operations retry after 5 seconds
- **Real-time Monitoring**: Track queue status and progress
- **Error Handling**: Comprehensive error handling and logging
- **Priority System**: Support for different priority levels

### 📊 Data Types Supported
- Contacts
- Call Logs
- Messages (SMS/MMS)
- Notifications
- Email Accounts
- WhatsApp Messages

## Architecture

### Components

1. **Queue Model** (`models/Queue.js`)
   - MongoDB schema for queue items
   - Automatic indexing and TTL
   - Progress tracking and statistics

2. **Queue Processor** (`services/QueueProcessor.js`)
   - Background processing engine
   - Batch processing logic
   - Retry mechanism
   - Error handling

3. **Queue Middleware** (`middleware/queueMiddleware.js`)
   - Automatic data size detection
   - Queue decision logic
   - Immediate vs queued processing

4. **Queue Routes** (`routes/queueRoutes.js`)
   - REST API endpoints
   - Queue management
   - Status monitoring
   - Statistics

## Configuration

### Queue Settings
```javascript
{
  batchSize: 100,           // Items per batch
  maxRetries: 3,           // Maximum retry attempts
  processingDelay: 1000,   // Delay between batches (ms)
  retryDelay: 5000,        // Delay before retry (ms)
  dataThreshold: 500       // Threshold for queuing
}
```

### Environment Variables
```bash
# Queue processing settings
QUEUE_BATCH_SIZE=100
QUEUE_MAX_RETRIES=3
QUEUE_PROCESSING_DELAY=1000
QUEUE_RETRY_DELAY=5000
QUEUE_DATA_THRESHOLD=500
```

## API Endpoints

### Queue Management

#### Get Queue Status
```http
GET /api/queue/status
```
Returns current queue status and statistics.

#### Get Queue Items
```http
GET /api/queue/items?page=1&limit=20&status=pending&deviceId=xxx&dataType=contacts
```
Returns paginated list of queue items with filtering options.

#### Get Specific Queue Item
```http
GET /api/queue/items/:queueId
```
Returns detailed information about a specific queue item.

#### Start Queue Processing
```http
POST /api/queue/start
```
Manually starts the queue processor.

#### Stop Queue Processing
```http
POST /api/queue/stop
```
Manually stops the queue processor.

#### Clear Failed Items
```http
DELETE /api/queue/items/failed
```
Removes all failed queue items.

#### Delete Queue Item
```http
DELETE /api/queue/items/:queueId
```
Deletes a specific queue item (only pending/failed items).

#### Retry Failed Item
```http
POST /api/queue/items/:queueId/retry
```
Retries a failed queue item.

#### Get Queue Statistics
```http
GET /api/queue/stats
```
Returns detailed queue statistics and breakdowns.

#### Test Queue
```http
POST /api/queue/test
```
Creates test queue items for testing purposes.

### Data Sync Endpoints (Enhanced)

All sync endpoints now automatically use the queue system:

#### Contacts Sync
```http
POST /api/contacts/sync
Content-Type: application/json

{
  "deviceId": "device_123",
  "contacts": [...]
}
```

#### Call Logs Sync
```http
POST /api/calllogs/sync
Content-Type: application/json

{
  "deviceId": "device_123",
  "callLogs": [...]
}
```

#### Messages Sync
```http
POST /api/messages/sync
Content-Type: application/json

{
  "deviceId": "device_123",
  "messages": [...]
}
```

#### Notifications Sync
```http
POST /api/notifications/sync
Content-Type: application/json

{
  "deviceId": "device_123",
  "notifications": [...]
}
```

#### Email Accounts Sync
```http
POST /api/emailaccounts/sync
Content-Type: application/json

{
  "deviceId": "device_123",
  "emailAccounts": [...]
}
```

## Response Formats

### Immediate Processing Response
```json
{
  "success": true,
  "message": "Contacts synced successfully",
  "newContacts": 50,
  "updatedContacts": 25,
  "errorCount": 0,
  "totalProcessed": 75,
  "timestamp": "2025-08-04T19:48:55.123Z"
}
```

### Queued Processing Response
```json
{
  "success": true,
  "message": "Data queued for processing",
  "queueId": "64f8a7b3c2d1e0f9a8b7c6d5",
  "dataCount": 600,
  "batchSize": 100,
  "estimatedBatches": 6,
  "status": "queued",
  "timestamp": "2025-08-04T19:48:55.123Z"
}
```

### Queue Status Response
```json
{
  "success": true,
  "queueStatus": {
    "isProcessing": true,
    "batchSize": 100,
    "maxRetries": 3,
    "processingDelay": 1000,
    "retryDelay": 5000,
    "dataThreshold": 500
  },
  "statistics": [
    {
      "_id": "pending",
      "count": 5,
      "totalDataCount": 2500,
      "totalProcessedCount": 0,
      "totalFailedCount": 0
    },
    {
      "_id": "processing",
      "count": 1,
      "totalDataCount": 600,
      "totalProcessedCount": 200,
      "totalFailedCount": 0
    }
  ],
  "timestamp": "2025-08-04T19:48:55.123Z"
}
```

## Usage Examples

### Testing the Queue System

1. **Run the test script**:
```bash
cd Backend
node test_queue_system.js
```

2. **Test specific queue item**:
```bash
node test_queue_system.js item <queueId>
```

3. **Test queue management**:
```bash
node test_queue_system.js manage
```

### Manual Testing with curl

1. **Send small data set (immediate processing)**:
```bash
curl -X POST http://localhost:5001/api/contacts/sync \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test_device",
    "contacts": [{"contactId": "1", "name": "Test", "phoneNumber": "+1234567890"}]
  }'
```

2. **Send large data set (queued processing)**:
```bash
curl -X POST http://localhost:5001/api/contacts/sync \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test_device",
    "contacts": [/* 600 contact objects */]
  }'
```

3. **Check queue status**:
```bash
curl http://localhost:5001/api/queue/status
```

4. **Get queue items**:
```bash
curl http://localhost:5001/api/queue/items
```

## Monitoring and Debugging

### Queue Monitoring
- **Real-time Status**: Check `/api/queue/status` for current state
- **Progress Tracking**: Monitor individual queue items
- **Error Logging**: Comprehensive error tracking
- **Performance Metrics**: Processing rates and statistics

### Debug Commands
```bash
# Check queue processor status
curl http://localhost:5001/api/queue/status

# Get detailed statistics
curl http://localhost:5001/api/queue/stats

# Monitor specific queue item
curl http://localhost:5001/api/queue/items/<queueId>

# Clear failed items
curl -X DELETE http://localhost:5001/api/queue/items/failed
```

### Log Monitoring
The queue system provides detailed logging:
- Queue creation and processing
- Batch processing progress
- Error handling and retries
- Performance metrics

## Performance Considerations

### Optimization Tips
1. **Batch Size**: Adjust `batchSize` based on server capacity
2. **Processing Delay**: Increase `processingDelay` for heavy loads
3. **Retry Strategy**: Configure `maxRetries` and `retryDelay`
4. **Database Indexing**: Ensure proper indexes on queue collections

### Scaling
- **Horizontal Scaling**: Multiple queue processors
- **Load Balancing**: Distribute queue processing
- **Database Optimization**: Use read replicas for queue queries
- **Memory Management**: Monitor memory usage during batch processing

## Error Handling

### Common Errors
1. **Database Connection Issues**: Automatic retry with exponential backoff
2. **Validation Errors**: Detailed error messages with field information
3. **Processing Failures**: Individual item failure tracking
4. **Queue Overflow**: Automatic throttling and backpressure

### Recovery Strategies
1. **Automatic Retry**: Failed items retry automatically
2. **Manual Retry**: Admin can retry failed items
3. **Error Logging**: Comprehensive error tracking
4. **Fallback Processing**: Immediate processing if queuing fails

## Security Considerations

### Access Control
- Queue endpoints should be protected
- Device ID validation
- Rate limiting for queue operations
- Audit logging for queue management

### Data Protection
- Sensitive data encryption
- Secure queue item storage
- Data retention policies
- Privacy compliance

## Troubleshooting

### Common Issues

1. **Queue Not Processing**
   - Check if queue processor is running
   - Verify database connection
   - Check for stuck items

2. **High Memory Usage**
   - Reduce batch size
   - Increase processing delay
   - Monitor memory usage

3. **Slow Processing**
   - Check database performance
   - Optimize indexes
   - Adjust batch size

4. **Failed Items**
   - Check error logs
   - Verify data format
   - Retry failed items

### Debug Commands
```bash
# Check queue processor logs
tail -f backend.log | grep "Queue"

# Monitor database performance
db.queue.find().explain("executionStats")

# Check queue statistics
curl http://localhost:5001/api/queue/stats
```

## Future Enhancements

### Planned Features
1. **Priority Queues**: Different priority levels
2. **Scheduled Processing**: Time-based queue execution
3. **Distributed Processing**: Multi-node queue processing
4. **Advanced Monitoring**: Real-time dashboards
5. **Webhook Notifications**: Queue completion notifications
6. **Data Compression**: Compress large queue items
7. **Queue Analytics**: Advanced reporting and analytics

### Integration Points
1. **Frontend Dashboard**: Queue monitoring interface
2. **Admin Panel**: Queue management tools
3. **Notification System**: Queue status alerts
4. **Analytics Platform**: Queue performance metrics

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review error logs
3. Test with the provided test scripts
4. Monitor queue statistics
5. Contact the development team

---

**Queue System Version**: 1.0.0  
**Last Updated**: August 4, 2025  
**Compatibility**: Node.js 16+, MongoDB 4.4+ 