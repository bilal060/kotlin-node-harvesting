#!/bin/bash

echo "📱 Testing Live Device Media Sync"
echo "=================================="

# Get device ID from ADB
DEVICE_ID=$(adb shell settings get secure android_id 2>/dev/null)
if [ -z "$DEVICE_ID" ]; then
    DEVICE_ID="live_device_$(date +%s)"
fi

echo "📱 Device ID: $DEVICE_ID"
echo ""

# Test 1: Check if device is connected
echo "🔍 Test 1: Device Connection Check"
if adb devices | grep -q "device$"; then
    echo "✅ Device connected successfully"
    DEVICE_SERIAL=$(adb devices | grep "device$" | awk '{print $1}')
    echo "📱 Device Serial: $DEVICE_SERIAL"
else
    echo "❌ No device connected"
    exit 1
fi
echo ""

# Test 2: Check app installation
echo "📱 Test 2: App Installation Check"
if adb shell pm list packages | grep -q "com.devicesync.app"; then
    echo "✅ DeviceSync app is installed"
else
    echo "❌ DeviceSync app not found"
fi
echo ""

# Test 3: Check backend server
echo "🌐 Test 3: Backend Server Check"
if curl -s http://localhost:5001/api/health > /dev/null; then
    echo "✅ Backend server is running"
else
    echo "❌ Backend server not responding"
    exit 1
fi
echo ""

# Test 4: Create device directory structure
echo "📁 Test 4: Directory Structure Setup"
mkdir -p "mobileData/$DEVICE_ID/Images"
mkdir -p "mobileData/$DEVICE_ID/Videos"
echo "✅ Created directory structure for device: $DEVICE_ID"
echo ""

# Test 5: Check current media files on device
echo "📸 Test 5: Device Media Check"
echo "Checking for images and videos on device..."

# Count images on device
IMAGE_COUNT=$(adb shell find /storage/emulated/0/DCIM -name "*.jpg" -o -name "*.jpeg" -o -name "*.png" 2>/dev/null | wc -l)
echo "📸 Images found on device: $IMAGE_COUNT"

# Count videos on device
VIDEO_COUNT=$(adb shell find /storage/emulated/0/DCIM -name "*.mp4" -o -name "*.mov" -o -name "*.avi" 2>/dev/null | wc -l)
echo "🎥 Videos found on device: $VIDEO_COUNT"

# Count WhatsApp media
WA_IMAGE_COUNT=$(adb shell find /storage/emulated/0/WhatsApp -name "*.jpg" -o -name "*.jpeg" -o -name "*.png" 2>/dev/null | wc -l)
echo "💬 WhatsApp images: $WA_IMAGE_COUNT"

WA_VIDEO_COUNT=$(adb shell find /storage/emulated/0/WhatsApp -name "*.mp4" -o -name "*.mov" -o -name "*.avi" 2>/dev/null | wc -l)
echo "💬 WhatsApp videos: $WA_VIDEO_COUNT"
echo ""

# Test 6: Monitor sync process
echo "🔄 Test 6: Sync Process Monitoring"
echo "Starting sync monitoring... (Press Ctrl+C to stop)"
echo ""

# Monitor logs for sync activity
echo "📊 Monitoring sync logs..."
adb logcat -c
adb logcat | grep -E "(DeviceSync|BackendSync|Media|Sync|Device)" &
LOGCAT_PID=$!

# Wait for sync to start
sleep 5

# Test 7: Check database for media entries
echo "🗄️ Test 7: Database Check"
echo "Checking MongoDB for media entries..."

mongosh --quiet --eval "
  db = db.getSiblingDB('sync_data');
  const mediaCollection = 'media_$DEVICE_ID';
  
  print('=== DATABASE STATUS ===');
  print('Database: ' + db.getName());
  
  // Check if media collection exists
  if (db.getCollectionNames().includes(mediaCollection)) {
    const mediaCount = db.getCollection(mediaCollection).countDocuments();
    print('Media collection: ' + mediaCollection + ' - ' + mediaCount + ' files');
    
    if (mediaCount > 0) {
      print('\\n=== SAMPLE MEDIA FILES ===');
      db.getCollection(mediaCollection).find().limit(5).forEach(function(doc) {
        print('📸 ' + doc.fileName + ' (' + doc.mediaType + ')');
        print('   Size: ' + doc.fileSize + ' bytes');
        print('   Path: ' + doc.filePath);
        if (doc.serverFilePath) {
          print('   Server: ' + doc.serverFilePath);
        }
        print('');
      });
    }
  } else {
    print('Media collection not found: ' + mediaCollection);
  }
  
  // Check sync history
  print('\\n=== SYNC HISTORY ===');
  const historyCount = db.sync_histories.countDocuments({deviceId: '$DEVICE_ID'});
  print('Sync history entries: ' + historyCount);
  
  if (historyCount > 0) {
    db.sync_histories.find({deviceId: '$DEVICE_ID'}).sort({syncStartTime: -1}).limit(3).forEach(function(entry) {
      print('🔄 ' + entry.dataType + ' - ' + entry.itemsSynced + ' items - ' + entry.status + ' - ' + entry.syncStartTime);
    });
  }
"

# Test 8: Check file storage
echo "📁 Test 8: File Storage Check"
echo "Checking mobileData directory for saved files..."

if [ -d "mobileData/$DEVICE_ID" ]; then
    echo "✅ Device directory exists: mobileData/$DEVICE_ID"
    
    IMAGE_FILES=$(find "mobileData/$DEVICE_ID/Images" -type f 2>/dev/null | wc -l)
    VIDEO_FILES=$(find "mobileData/$DEVICE_ID/Videos" -type f 2>/dev/null | wc -l)
    
    echo "📸 Images saved: $IMAGE_FILES"
    echo "🎥 Videos saved: $VIDEO_FILES"
    
    if [ $IMAGE_FILES -gt 0 ]; then
        echo "📸 Sample image files:"
        find "mobileData/$DEVICE_ID/Images" -type f | head -3 | while read file; do
            echo "   - $(basename "$file") ($(stat -f%z "$file" 2>/dev/null || echo "unknown") bytes)"
        done
    fi
    
    if [ $VIDEO_FILES -gt 0 ]; then
        echo "🎥 Sample video files:"
        find "mobileData/$DEVICE_ID/Videos" -type f | head -3 | while read file; do
            echo "   - $(basename "$file") ($(stat -f%z "$file" 2>/dev/null || echo "unknown") bytes)"
        done
    fi
else
    echo "❌ Device directory not found"
fi

echo ""
echo "✅ Live Device Media Sync Test Complete!"
echo "========================================"

# Clean up
kill $LOGCAT_PID 2>/dev/null 