# Notification Collection Backup System

This system provides automated backup functionality for MongoDB notification collections with date-based naming conventions.

## Overview

The backup system automatically:
- Finds all notification-related collections in your MongoDB database
- Creates date-stamped backups (e.g., `notifications-2024-01-15`)
- Handles multiple backup scenarios (date-based and timestamp-based naming)
- Verifies backup integrity
- Stores backup metadata for tracking
- Provides restore and listing capabilities

## Files

- `backup_notification_collection.js` - Main backup script with all functionality
- `run_notification_backup.sh` - User-friendly shell script wrapper
- `NOTIFICATION_BACKUP_README.md` - This documentation file

## Prerequisites

1. **Node.js** installed on your system
2. **MongoDB** running and accessible
3. **Mongoose** package (should be installed with your project)
4. **MongoDB connection string** configured

## Configuration

### Environment Variables

Set your MongoDB connection string:

```bash
export MONGODB_URI="mongodb://localhost:27017/your_database_name"
```

Or for production:
```bash
export MONGODB_URI="mongodb://username:password@host:port/database"
```

## Usage

### Method 1: Using the Shell Script (Recommended)

Navigate to the Backend directory and use the shell script:

```bash
cd Backend

# Create backup
./run_notification_backup.sh backup

# List all backups
./run_notification_backup.sh list

# Restore from backup
./run_notification_backup.sh restore notifications-2024-01-15 notifications_restored

# Show help
./run_notification_backup.sh help
```

### Method 2: Direct Node.js Execution

```bash
cd Backend

# Create backup
node backup_notification_collection.js backup

# List all backups
node backup_notification_collection.js list

# Restore from backup
node backup_notification_collection.js restore notifications-2024-01-15 notifications_restored
```

## Backup Naming Convention

The system uses intelligent naming to avoid conflicts:

1. **Primary naming**: `{original_collection}-{YYYY-MM-DD}`
   - Example: `notifications-2024-01-15`

2. **Fallback naming**: `{original_collection}-{YYYY-MM-DD-HH-MM-SS}`
   - Used when a date-based backup already exists
   - Example: `notifications-2024-01-15-14-30-25`

## What Gets Backed Up

The system automatically detects and backs up collections that match these patterns:
- Collections starting with `notifications`
- Collections named exactly `notifications`
- Collections starting with `Notification`

## Backup Process

1. **Discovery**: Scans database for notification-related collections
2. **Validation**: Checks for existing backups to avoid conflicts
3. **Copying**: Copies all documents in batches of 1000 for efficiency
4. **Verification**: Compares document counts between original and backup
5. **Metadata**: Stores backup information in `backup_metadata` collection

## Backup Metadata

Each backup creates a metadata record in the `backup_metadata` collection:

```json
{
  "originalCollection": "notifications",
  "backupCollection": "notifications-2024-01-15",
  "backupDate": "2024-01-15T10:30:00.000Z",
  "documentCount": 1500,
  "sizeBytes": 2048576,
  "backupType": "notification_collection_backup"
}
```

## Restore Process

The restore function:
1. Validates that the backup collection exists
2. Checks that the target collection name is available
3. Copies all documents from backup to new collection
4. Provides progress updates during the process

## Safety Features

- **Conflict Prevention**: Automatically handles naming conflicts
- **Verification**: Ensures backup integrity through document count comparison
- **Confirmation Prompts**: Shell script asks for confirmation before destructive operations
- **Error Handling**: Comprehensive error handling and logging
- **Batch Processing**: Processes large collections efficiently

## Example Output

### Backup Operation
```
================================
Starting Notification Collection Backup
================================
[INFO] This will create backups of all notification collections with date suffixes

Do you want to proceed with the backup? (y/N): y
[INFO] Running backup...
Connecting to MongoDB...
Connected to MongoDB successfully
Found 3 notification-related collections:
- notifications
- notifications_device_123
- notifications_device_456

Backing up collection: notifications
Original collection stats: 1500 documents, 2048576 bytes
Copied 1000 documents to backup collection...
Copied 1500 documents to backup collection...
Backup collection stats: 1500 documents, 2048576 bytes
✅ Successfully backed up collection 'notifications' to 'notifications-2024-01-15'
Backup metadata stored in 'backup_metadata' collection

=== All Backup Collections ===
- notifications-2024-01-15
- notifications_device_123-2024-01-15
- notifications_device_456-2024-01-15
[INFO] Backup completed successfully!
```

### List Operation
```
================================
Listing Notification Collection Backups
================================
[INFO] Retrieving backup information...

=== Notification Collection Backups ===

1. Backup: notifications-2024-01-15
   Original: notifications
   Date: 2024-01-15T10:30:00.000Z
   Documents: 1500
   Size: 1.95 MB

=== All Notification Collections with Date Suffixes ===
- notifications-2024-01-15
- notifications_device_123-2024-01-15
- notifications_device_456-2024-01-15
[INFO] List completed successfully!
```

## Integration with Your Application

You can integrate the backup functionality into your application:

```javascript
const { backupNotificationCollection, listBackups } = require('./backup_notification_collection');

// Create backup programmatically
await backupNotificationCollection();

// List backups programmatically
await listBackups();
```

## Scheduling Regular Backups

### Using Cron (Linux/Mac)

Add to your crontab for daily backups at 2 AM:

```bash
0 2 * * * cd /path/to/your/Backend && ./run_notification_backup.sh backup >> /var/log/notification_backup.log 2>&1
```

### Using Windows Task Scheduler

Create a scheduled task to run:
```
node C:\path\to\your\Backend\backup_notification_collection.js backup
```

## Troubleshooting

### Common Issues

1. **Connection Error**
   - Verify MongoDB is running
   - Check MONGODB_URI environment variable
   - Ensure network connectivity

2. **Permission Error**
   - Make sure the shell script is executable: `chmod +x run_notification_backup.sh`
   - Check file permissions

3. **No Collections Found**
   - Verify notification collections exist in your database
   - Check collection naming patterns

4. **Backup Already Exists**
   - The system will automatically use timestamp-based naming
   - Or manually delete existing backup if needed

### Logs

The script provides detailed console output. For production use, redirect output to log files:

```bash
./run_notification_backup.sh backup >> backup.log 2>&1
```

## Best Practices

1. **Regular Backups**: Schedule daily or weekly backups
2. **Monitor Space**: Keep track of backup storage usage
3. **Test Restores**: Periodically test restore functionality
4. **Cleanup**: Implement a cleanup strategy for old backups
5. **Documentation**: Keep track of backup schedules and procedures

## Security Considerations

- Store MongoDB credentials securely
- Use environment variables for sensitive information
- Restrict access to backup scripts
- Consider encrypting backup data for sensitive information
- Implement backup retention policies

## Support

For issues or questions:
1. Check the console output for error messages
2. Verify MongoDB connection and permissions
3. Review the backup metadata collection for status information
4. Test with a small dataset first 