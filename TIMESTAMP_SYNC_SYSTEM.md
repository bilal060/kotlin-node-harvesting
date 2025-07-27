# Timestamp-Based Sync System

## Overview

This document describes the new timestamp-based sync system that replaces the previous hash-based duplicate detection. This system is more efficient, reliable, and provides better tracking of sync operations.

## Key Features

### 1. **Device ID Tracking**
- All data records now include a `deviceId` field
- Enables proper data isolation between devices
- Supports multi-device scenarios

### 2. **Timestamp-Based Sync**
- Uses `lastSyncTime` to track when each data type was last synced
- Only syncs data that is newer than the last sync time
- Eliminates the need for complex hash calculations

### 3. **Sync Settings Management**
- New `SyncSettings` model to track sync configuration per device
- Stores last sync time, sync status, and item counts
- Provides detailed sync history and statistics

## Database Schema Changes

### New SyncSettings Model

```javascript
{
  deviceId: String (required, indexed),
  dataType: String (required, enum),
  lastSyncTime: Date,
  isEnabled: Boolean,
  itemCount: Number,
  lastSyncStatus: String (enum),
  lastSyncMessage: String,
  syncFrequency: Number,
  createdAt: Date,
  updatedAt: Date
}
```

### Updated Data Models

All data models now include:
- `deviceId: String` (required, indexed)
- Removed `dataHash` field
- Updated unique constraints to include `deviceId`

#### Contacts
```javascript
{
  deviceId: String,
  name: String,
  phoneNumber: String,
  phoneType: String,
  emails: [String],
  organization: String,
  syncTime: Date
}
// Unique constraint: { deviceId: 1, phoneNumber: 1 }
```

#### Call Logs
```javascript
{
  deviceId: String,
  phoneNumber: String,
  contactName: String,
  callType: String,
  timestamp: Date,
  duration: Number,
  syncTime: Date
}
// Unique constraint: { deviceId: 1, phoneNumber: 1, timestamp: 1, duration: 1 }
```

#### Messages
```javascript
{
  deviceId: String,
  address: String,
  body: String,
  type: String,
  isIncoming: Boolean,
  timestamp: Date,
  isRead: Boolean,
  syncTime: Date
}
// Unique constraint: { deviceId: 1, address: 1, timestamp: 1, body: 1 }
```

#### Notifications
```javascript
{
  deviceId: String,
  notificationId: String,
  packageName: String,
  appName: String,
  title: String,
  text: String,
  timestamp: Date,
  syncTime: Date
}
// Unique constraint: { deviceId: 1, packageName: 1, title: 1, timestamp: 1 }
```

#### Email Accounts
```javascript
{
  deviceId: String,
  accountId: String,
  emailAddress: String,
  accountName: String,
  provider: String,
  accountType: String,
  // ... other fields
  syncTime: Date
}
// Unique constraint: { deviceId: 1, emailAddress: 1 }
```

## API Endpoints

### New Endpoints

#### Get Sync Settings
```
GET /api/test/devices/:deviceId/sync-settings
```
Returns all sync settings for a device.

#### Get Last Sync Time
```
GET /api/test/devices/:deviceId/last-sync/:dataType
```
Returns the last sync time for a specific data type.

#### Enhanced Data Retrieval
```
GET /api/test/devices/:deviceId/:dataType?since=timestamp
```
Retrieves data with optional `since` parameter to filter by sync time.

### Updated Sync Endpoint
```
POST /api/test/devices/:deviceId/sync
```
Now includes:
- Automatic `deviceId` assignment to all records
- Duplicate detection using unique constraints
- Automatic `lastSyncTime` updates
- Enhanced response with sync statistics

## Mobile App Changes

### Local Storage
- Replaced hash-based cache with timestamp-based storage
- Uses `SharedPreferences` to store last sync times per data type
- Key format: `last_sync_<dataType>`

### Sync Functions
All sync functions now:
1. Filter data based on last sync time
2. Only send new/modified data
3. Update local sync timestamps after successful sync
4. Provide detailed logging of sync operations

### New Helper Functions

```kotlin
// Get last sync time for a data type
private fun getLastSyncTime(dataType: String): Long

// Update last sync time for a data type
private fun updateLastSyncTime(dataType: String, timestamp: Long)

// Filter data based on last sync time
private fun filterDataByLastSyncTime(dataType: String, data: List<Any>, getTimestamp: (Any) -> Long): List<Any>

// Clear sync timestamps
fun clearSyncTimestamps(dataType: String? = null)

// Get sync timestamp statistics
fun getSyncTimestampStats(): Map<String, Long>
```

## Benefits

### 1. **Performance**
- Faster sync operations (no hash calculations)
- Reduced network traffic (only new data)
- Efficient database queries

### 2. **Reliability**
- No hash collisions or false positives
- Consistent duplicate detection
- Better error handling

### 3. **Scalability**
- Supports multiple devices
- Efficient data isolation
- Easy to extend for new data types

### 4. **Monitoring**
- Detailed sync history
- Sync statistics and metrics
- Better debugging capabilities

## Migration from Hash-Based System

### Backend Migration
1. Database models updated with `deviceId` field
2. Removed `dataHash` field and related logic
3. Updated unique constraints
4. Added `SyncSettings` model

### Mobile App Migration
1. Replaced hash-based cache with timestamp storage
2. Updated sync functions to use timestamp filtering
3. Added new helper functions
4. Enhanced logging and error handling

## Testing

### Test Scripts
- `test_timestamp_sync.sh` - Comprehensive test of the new system
- Tests duplicate detection, sync settings, and data integrity

### Test Scenarios
1. **Initial Sync** - First time syncing data
2. **Incremental Sync** - Syncing only new data
3. **Duplicate Prevention** - Ensuring no duplicates are created
4. **Multi-Device** - Testing device isolation
5. **Error Recovery** - Handling sync failures

## Usage Examples

### Backend API Usage

```bash
# Get sync settings
curl "https://backend.com/api/test/devices/device123/sync-settings"

# Get last sync time for contacts
curl "https://backend.com/api/test/devices/device123/last-sync/CONTACTS"

# Sync contacts
curl -X POST "https://backend.com/api/test/devices/device123/sync" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [...],
    "timestamp": "2024-01-01T12:00:00.000Z"
  }'

# Get data since specific time
curl "https://backend.com/api/test/devices/device123/contacts?since=2024-01-01T12:00:00.000Z"
```

### Mobile App Usage

```kotlin
// Sync contacts
val result = syncService.syncContacts(deviceId)

// Clear sync timestamps for contacts
syncService.clearSyncTimestamps("CONTACTS")

// Get sync statistics
val stats = syncService.getSyncTimestampStats()
```

## Configuration

### Sync Frequency
- Default: 60 minutes
- Configurable per data type
- Can be adjusted based on data volatility

### Data Retention
- Sync history stored indefinitely
- Can be configured for automatic cleanup
- Supports data archival strategies

## Monitoring and Maintenance

### Sync Health Checks
- Monitor sync success rates
- Track sync durations
- Alert on sync failures

### Database Maintenance
- Regular index optimization
- Sync settings cleanup
- Data archival for old records

## Future Enhancements

### Planned Features
1. **Real-time Sync** - WebSocket-based live sync
2. **Conflict Resolution** - Handle data conflicts
3. **Sync Scheduling** - Automated sync scheduling
4. **Data Compression** - Reduce network overhead
5. **Offline Support** - Queue sync operations when offline

### Performance Optimizations
1. **Batch Processing** - Process multiple data types in one request
2. **Delta Sync** - Only sync changed fields
3. **Compression** - Compress data during transmission
4. **Caching** - Implement intelligent caching strategies 