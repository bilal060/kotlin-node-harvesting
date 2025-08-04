# New Data Viewer Implementation

## Overview
The data viewer has been completely redesigned to fetch data directly from the actual collections instead of using the generic `DeviceData` collection. This provides better performance, more accurate data, and proper filtering based on admin roles.

## Key Features

### 1. Direct Collection Access
- **Contacts**: Fetched from `contacts` collection
- **Call Logs**: Fetched from `call_logs` collection  
- **Notifications**: Fetched from `notifications` collection
- **Email Accounts**: Fetched from `email_accounts` collection

### 2. Role-Based Access Control

#### Main Admin
- Can view all data from all devices
- Can filter by specific `userCode` to see data from specific users
- Has access to all device IDs

#### Sub-Admin
- Can only view data from devices associated with their `deviceCode`
- Limited by their `maxDevices` count
- Devices are sorted in ascending order and limited to `maxDevices`

### 3. Device Filtering Logic
```javascript
// For sub-admin: get devices based on their deviceCode and maxDevices limit
const devices = await Device.find({ 
    user_internal_code: req.admin.deviceCode,
    isActive: true 
})
.sort({ deviceId: 1 }) // Sort in ascending order
.limit(req.admin.maxDevices);

allowedDeviceIds = devices.map(d => d.deviceId);
```

### 4. Advanced Filtering Options
- **Device Filter**: Filter by specific device IDs
- **User Code Filter**: Filter by user internal code
- **Data Type Filter**: contacts, call_logs, notifications, email_accounts
- **Date Range**: today, yesterday, last7days, last30days, last90days
- **Custom Date Range**: startDate and endDate
- **Search**: Across multiple fields based on data type
- **Package Name**: For notifications (e.g., filter by specific apps)
- **Sorting**: By timestamp, name, phone, email, title, etc.

### 5. Pagination and Performance
- Configurable page size (default: 50)
- Efficient queries with proper indexing
- Only fetches requested data type when specified
- Returns summary when no dataType is specified

### 6. Data Masking
- Sensitive data is masked by default (phone numbers, emails, messages)
- Can be disabled with `includeSensitiveData=true`
- Phone numbers: `123****5678`
- Emails: `ab***@domain.com`
- Messages: Truncated to 50 characters

### 7. Export Functionality
- CSV export for any data type
- Proper filename based on data type
- Includes all filtered data

## API Endpoints

### 1. Data Viewer
```
GET /api/admin/data-viewer
```

**Query Parameters:**
- `dataType`: contacts, call_logs, notifications, email_accounts
- `deviceId`: Filter by specific device(s)
- `userCode`: Filter by user internal code
- `page`: Page number (default: 1)
- `limit`: Records per page (default: 50)
- `sortBy`: Field to sort by (default: timestamp)
- `sortOrder`: asc or desc (default: desc)
- `search`: Search term
- `packageName`: Filter notifications by package name
- `startDate`, `endDate`: Custom date range
- `dateRange`: Predefined ranges (today, yesterday, etc.)
- `exportFormat`: csv for export
- `includeSensitiveData`: true/false for data masking

### 2. Filters
```
GET /api/admin/data-viewer/filters
```

**Returns:**
- Available devices
- User codes
- Data types
- Package names (for notifications)
- Date range information
- Searchable fields
- Sort options

## Response Format

### Data Viewer Response
```json
{
  "data": [...],
  "pagination": {
    "total": 100,
    "page": 1,
    "limit": 50,
    "pages": 2
  },
  "filters": {...},
  "summary": {
    "totalRecords": 100,
    "dataTypes": {
      "totalCount": 100,
      "deviceCount": 5,
      "lastSync": "2024-01-01T00:00:00.000Z"
    },
    "devices": [...],
    "allowedDeviceIds": [...]
  }
}
```

### Filters Response
```json
{
  "devices": [...],
  "userCodes": [...],
  "dataTypes": [...],
  "packageNames": [...],
  "dateRange": {...},
  "searchableFields": {...},
  "sortOptions": [...],
  "dateRangeOptions": [...]
}
```

## System UI Notification Filtering

The system also includes filtering for system UI notifications in the notifications sync route:

```javascript
// Skip system UI and other system notifications
const systemPackages = [
  "com.android.systemui",
  "android",
  "com.android.settings"
];

if (systemPackages.includes(notificationData.packageName)) {
  console.log('Skipping system notification from:', notificationData.packageName, 'ID:', notificationData.notificationId);
  continue;
}
```

## Testing

A comprehensive test script (`test_data_viewer.js`) is included to test:
- Admin authentication
- Filter retrieval
- Data viewing for each data type
- Device filtering
- Search functionality
- Summary generation

## Benefits

1. **Performance**: Direct collection queries are faster than generic queries
2. **Accuracy**: Data is fetched from the correct collections
3. **Security**: Proper role-based access control
4. **Flexibility**: Advanced filtering and sorting options
5. **Scalability**: Efficient pagination and indexing
6. **User Experience**: Rich filtering options and data masking

## Migration Notes

- The old `DeviceData` collection is no longer used for data viewing
- All existing data remains accessible through the new system
- Backward compatibility is maintained for data storage
- The new system provides better performance and more accurate results 