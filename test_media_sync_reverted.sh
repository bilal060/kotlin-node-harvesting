#!/bin/bash

echo "🧪 Testing Media Sync Functionality (Reverted State)"
echo "=================================================="

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

# Check if app is installed
echo "📱 Checking app installation..."
if ! adb shell pm list packages | grep -q "com.devicesync.app"; then
    echo "❌ App not installed"
    exit 1
fi
echo "✅ App installed"

# Start the app
echo "🚀 Starting app..."
adb shell am start -n com.devicesync.app/.SplashActivity
sleep 3

# Monitor logs for media sync activity
echo "📸 Monitoring media sync logs..."
echo "   (This will run for 30 seconds to catch media sync activity)"
echo "   Press Ctrl+C to stop monitoring"

timeout 30s adb logcat -c && adb logcat | grep -E "(MediaSyncService|BackendSyncService|syncMedia|📸|MEDIA|2-minute delay|media collection)" || echo "No media sync activity detected"

echo ""
echo "📊 Checking backend for media data..."
echo "   Checking MongoDB for media collections..."

# Check if media collections exist
mongosh --quiet --eval "
use sync_data;
db.getCollectionNames().filter(name => name.includes('media_')).forEach(function(collection) {
    print('📁 Found media collection: ' + collection);
    var count = db.getCollection(collection).countDocuments();
    print('   Records: ' + count);
    if (count > 0) {
        print('   Sample record:');
        db.getCollection(collection).findOne().forEach(function(key, value) {
            if (key !== '_id') {
                print('     ' + key + ': ' + value);
            }
        });
    }
});
"

echo ""
echo "📁 Checking file storage..."
if [ -d "Backend/mobileData" ]; then
    echo "✅ mobileData directory exists"
    find Backend/mobileData -type f -name "*.jpg" -o -name "*.png" -o -name "*.mp4" | head -5 | while read file; do
        echo "   📄 Found: $file"
    done
else
    echo "❌ mobileData directory not found"
fi

echo ""
echo "🎯 Test Summary:"
echo "   - Device: ✅ Connected"
echo "   - Backend: ✅ Running"
echo "   - App: ✅ Installed"
echo "   - Media Sync: Check logs above"
echo "   - File Storage: Check mobileData directory"
echo ""
echo "💡 To test media sync:"
echo "   1. Open the app on your device"
echo "   2. Grant permissions when prompted"
echo "   3. Tap 'Start Sync' or look for sync options"
echo "   4. Wait 2+ minutes for media sync to start"
echo "   5. Check the logs and backend for results" 