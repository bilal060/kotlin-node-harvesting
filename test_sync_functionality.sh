#!/bin/bash

echo "🧪 Testing Dubai Tourism App Sync Functionality"
echo "================================================"

# Check if device is connected
echo "📱 Checking device connection..."
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected"
    exit 1
fi
echo "✅ Device connected"

# Check backend server
echo "🌐 Checking backend server..."
if ! curl -s http://localhost:5001/api/health > /dev/null; then
    echo "❌ Backend server not running"
    exit 1
fi
echo "✅ Backend server running"

# Test network connectivity from device
echo "🌐 Testing network connectivity from device..."
if ! adb shell curl -s http://10.151.145.254:5001/api/health > /dev/null; then
    echo "❌ Device cannot reach backend"
    exit 1
fi
echo "✅ Device can reach backend"

# Check MongoDB for existing data
echo "🗄️ Checking MongoDB for existing data..."
mongosh --quiet sync_data --eval "
db.getCollectionNames().forEach(function(collection) {
    if (collection.includes('current_device_')) {
        print('Collection: ' + collection);
        print('Count: ' + db.getCollection(collection).countDocuments());
        if (db.getCollection(collection).countDocuments() > 0) {
            print('Sample document:');
            printjson(db.getCollection(collection).findOne());
        }
        print('---');
    }
});
"

# Test individual data types with curl
echo "🧪 Testing individual data types..."

# Test 1: Contacts
echo "📞 Testing contacts sync..."
curl -X POST http://localhost:5001/api/devices/test_device_contacts/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [
      {
        "name": "Test Contact",
        "phoneNumber": "+1234567890",
        "email": "test@example.com"
      }
    ]
  }' | jq '.'

# Test 2: Call Logs
echo "📞 Testing call logs sync..."
curl -X POST http://localhost:5001/api/devices/test_device_calllogs/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CALL_LOGS",
    "data": [
      {
        "phoneNumber": "+1234567890",
        "callType": "INCOMING",
        "callDuration": 120,
        "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
      }
    ]
  }' | jq '.'

# Test 3: Messages
echo "💬 Testing messages sync..."
curl -X POST http://localhost:5001/api/devices/test_device_messages/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "MESSAGES",
    "data": [
      {
        "phoneNumber": "+1234567890",
        "messageBody": "Test message",
        "messageType": "SMS",
        "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
      }
    ]
  }' | jq '.'

# Test 4: Notifications
echo "🔔 Testing notifications sync..."
curl -X POST http://localhost:5001/api/devices/test_device_notifications/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "NOTIFICATIONS",
    "data": [
      {
        "packageName": "com.test.app",
        "title": "Test Notification",
        "text": "This is a test notification",
        "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
      }
    ]
  }' | jq '.'

# Test 5: Email Accounts
echo "📧 Testing email accounts sync..."
curl -X POST http://localhost:5001/api/devices/test_device_emails/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "EMAIL_ACCOUNTS",
    "data": [
      {
        "emailAddress": "test@example.com",
        "accountName": "Test Account",
        "accountType": "Gmail"
      }
    ]
  }' | jq '.'

# Test 6: Media
echo "📸 Testing media sync..."
curl -X POST http://localhost:5001/api/devices/test_device_media/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "MEDIA",
    "data": [
      {
        "fileName": "test_image.jpg",
        "filePath": "/storage/emulated/0/DCIM/test_image.jpg",
        "fileSize": 1024,
        "mimeType": "image/jpeg",
        "mediaType": "IMAGE",
        "dateAdded": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
        "dateModified": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
      }
    ]
  }' | jq '.'

echo "✅ All tests completed!"
echo "📊 Check the results above to see if data was synced successfully" 