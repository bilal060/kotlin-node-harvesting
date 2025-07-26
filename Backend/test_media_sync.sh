#!/bin/bash

echo "🧪 Testing Media Sync Backend Functionality"
echo "============================================="

# Test device ID
DEVICE_ID="test_device_media_sync_$(date +%s)"

echo "📱 Using test device ID: $DEVICE_ID"
echo ""

# Test 1: Health Check
echo "🔍 Test 1: Backend Health Check"
curl -s http://localhost:5001/api/health
echo ""
echo ""

# Test 2: Media Sync Test
echo "📸 Test 2: Media Sync Test"
echo "Sending sample media data..."

MEDIA_DATA='{
  "dataType": "MEDIA",
  "data": [
    {
      "mediaId": "img_123456",
      "fileName": "test_image.jpg",
      "filePath": "/storage/emulated/0/DCIM/Camera/test_image.jpg",
      "fileSize": 2048576,
      "mimeType": "image/jpeg",
      "mediaType": "IMAGE",
      "dateAdded": 1704067200000,
      "dateModified": 1704067200000,
      "width": 1920,
      "height": 1080,
      "duration": 0,
      "albumName": "Camera",
      "artist": "",
      "description": "Test image from camera",
      "location": "",
      "isFavorite": false,
      "isHidden": false
    },
    {
      "mediaId": "vid_789012",
      "fileName": "test_video.mp4",
      "filePath": "/storage/emulated/0/DCIM/Camera/test_video.mp4",
      "fileSize": 10485760,
      "mimeType": "video/mp4",
      "mediaType": "VIDEO",
      "dateAdded": 1704067200000,
      "dateModified": 1704067200000,
      "width": 1920,
      "height": 1080,
      "duration": 30000,
      "albumName": "Camera",
      "artist": "",
      "description": "Test video from camera",
      "location": "",
      "isFavorite": false,
      "isHidden": false
    },
    {
      "mediaId": "wa_345678",
      "fileName": "whatsapp_image.jpg",
      "filePath": "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/whatsapp_image.jpg",
      "fileSize": 1024000,
      "mimeType": "image/jpeg",
      "mediaType": "IMAGE",
      "dateAdded": 1704067200000,
      "dateModified": 1704067200000,
      "width": 1280,
      "height": 720,
      "duration": 0,
      "albumName": "WhatsApp Media",
      "artist": "",
      "description": "Image from WhatsApp",
      "location": "",
      "isFavorite": false,
      "isHidden": false
    }
  ],
  "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")'"
}'

echo "📤 Sending media data to backend..."
RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "$MEDIA_DATA" \
  http://localhost:5001/api/test/devices/$DEVICE_ID/sync)

echo "📥 Response: $RESPONSE"
echo ""

# Test 3: Check Database
echo "🗄️ Test 3: Database Check"
echo "Checking if media data was stored..."

# Wait a moment for database write
sleep 2

# Check MongoDB collections
echo "📊 Database Collections:"
mongosh --quiet --eval "
  db = db.getSiblingDB('device_sync');
  print('=== DATABASE STATUS ===');
  print('Database: ' + db.getName());
  
  // List all collections
  print('\\n=== ALL COLLECTIONS ===');
  db.getCollectionNames().forEach(function(collection) {
    const count = db.getCollection(collection).countDocuments();
    print(collection + ': ' + count + ' documents');
  });
  
  // Check media collection specifically
  const mediaCollection = 'media_$DEVICE_ID';
  print('\\n=== MEDIA COLLECTION ===');
  if (db.getCollectionNames().includes(mediaCollection)) {
    const mediaCount = db.getCollection(mediaCollection).countDocuments();
    print(mediaCollection + ': ' + mediaCount + ' media files');
    
    // Show sample media documents
    print('\\n=== SAMPLE MEDIA FILES ===');
    db.getCollection(mediaCollection).find().limit(3).forEach(function(doc) {
      print('- ' + doc.fileName + ' (' + doc.mediaType + ', ' + doc.fileSize + ' bytes)');
    });
  } else {
    print('Media collection not found: ' + mediaCollection);
  }
  
  // Check sync history
  print('\\n=== SYNC HISTORY ===');
  const historyCount = db.sync_histories.countDocuments({deviceId: '$DEVICE_ID'});
  print('Sync history entries: ' + historyCount);
  
  if (historyCount > 0) {
    db.sync_histories.find({deviceId: '$DEVICE_ID'}).sort({syncStartTime: -1}).limit(1).forEach(function(entry) {
      print('Last sync: ' + entry.dataType + ' - ' + entry.itemsSynced + ' items - ' + entry.status);
    });
  }
"

echo ""
echo "✅ Media Sync Backend Test Complete!"
echo "=====================================" 