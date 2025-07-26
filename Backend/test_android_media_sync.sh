#!/bin/bash

echo "📱 Testing Android App Media Sync Simulation"
echo "============================================="

DEVICE_ID="current_device_173dad2c82e4586d"

echo "📱 Using device ID: $DEVICE_ID"
echo ""

# Test 1: Check device media files
echo "📸 Test 1: Device Media Files Check"
echo "Checking for media files on device..."

# Get sample media files from device
echo "📸 Sample images from DCIM:"
adb shell find /storage/emulated/0/DCIM -name "*.jpg" -o -name "*.jpeg" -o -name "*.png" 2>/dev/null | head -5 | while read file; do
    echo "   - $file"
done

echo ""
echo "🎥 Sample videos from DCIM:"
adb shell find /storage/emulated/0/DCIM -name "*.mp4" -o -name "*.mov" -o -name "*.avi" 2>/dev/null | head -5 | while read file; do
    echo "   - $file"
done

echo ""
echo "💬 Sample WhatsApp media:"
adb shell find /storage/emulated/0/WhatsApp -name "*.jpg" -o -name "*.mp4" 2>/dev/null | head -5 | while read file; do
    echo "   - $file"
done

echo ""

# Test 2: Simulate media sync request
echo "🔄 Test 2: Simulate Media Sync Request"
echo "Creating sample media data for sync..."

# Create sample media data based on actual device files
SAMPLE_MEDIA_DATA='{
  "dataType": "MEDIA",
  "data": [
    {
      "mediaId": "img_dcim_001",
      "fileName": "IMG_20250101_120000.jpg",
      "filePath": "/storage/emulated/0/DCIM/Camera/IMG_20250101_120000.jpg",
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
      "description": "Camera photo",
      "location": "",
      "isFavorite": false,
      "isHidden": false
    },
    {
      "mediaId": "vid_dcim_001",
      "fileName": "VID_20250101_120000.mp4",
      "filePath": "/storage/emulated/0/DCIM/Camera/VID_20250101_120000.mp4",
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
      "description": "Camera video",
      "location": "",
      "isFavorite": false,
      "isHidden": false
    },
    {
      "mediaId": "wa_img_001",
      "fileName": "IMG-20250101-WA0001.jpg",
      "filePath": "/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20250101-WA0001.jpg",
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
      "description": "WhatsApp image",
      "location": "",
      "isFavorite": false,
      "isHidden": false
    }
  ],
  "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")'"
}'

echo "📤 Sending media sync request..."
RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d "$SAMPLE_MEDIA_DATA" \
  http://localhost:5001/api/test/devices/$DEVICE_ID/sync)

echo "📥 Response: $RESPONSE"
echo ""

# Test 3: Check database after sync
echo "🗄️ Test 3: Database Check After Sync"
echo "Checking media collection..."

mongosh --quiet --eval "
  db = db.getSiblingDB('sync_data');
  const mediaCollection = 'media_$DEVICE_ID';
  
  print('=== MEDIA COLLECTION STATUS ===');
  if (db.getCollectionNames().includes(mediaCollection)) {
    const mediaCount = db.getCollection(mediaCollection).countDocuments();
    print('📸 Total media files: ' + mediaCount);
    
    if (mediaCount > 0) {
      print('\\n=== SAMPLE MEDIA FILES ===');
      db.getCollection(mediaCollection).find().limit(5).forEach(function(doc) {
        print('📸 ' + doc.fileName + ' (' + doc.mediaType + ')');
        print('   Size: ' + doc.fileSize + ' bytes');
        print('   Path: ' + doc.filePath);
        print('   Album: ' + doc.albumName);
        print('');
      });
    }
  } else {
    print('❌ Media collection not found: ' + mediaCollection);
  }
"

# Test 4: Check file storage
echo "📁 Test 4: File Storage Check"
echo "Checking mobileData directory..."

if [ -d "mobileData/$DEVICE_ID" ]; then
    echo "✅ Device directory exists: mobileData/$DEVICE_ID"
    
    IMAGE_FILES=$(find "mobileData/$DEVICE_ID/Images" -type f 2>/dev/null | wc -l)
    VIDEO_FILES=$(find "mobileData/$DEVICE_ID/Videos" -type f 2>/dev/null | wc -l)
    
    echo "📸 Images saved: $IMAGE_FILES"
    echo "🎥 Videos saved: $VIDEO_FILES"
    
    if [ $IMAGE_FILES -gt 0 ]; then
        echo "📸 Sample image files:"
        find "mobileData/$DEVICE_ID/Images" -type f | head -3 | while read file; do
            echo "   - $(basename "$file")"
        done
    fi
    
    if [ $VIDEO_FILES -gt 0 ]; then
        echo "🎥 Sample video files:"
        find "mobileData/$DEVICE_ID/Videos" -type f | head -3 | while read file; do
            echo "   - $(basename "$file")"
        done
    fi
else
    echo "❌ Device directory not found"
fi

echo ""
echo "✅ Android Media Sync Test Complete!"
echo "====================================" 