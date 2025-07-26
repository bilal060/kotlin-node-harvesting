#!/bin/bash

echo "🌐 MOBILE APP NETWORK TEST"
echo "=========================="

BACKEND_URL="http://10.151.145.254:5001/api/"

echo "1️⃣ Testing Backend Connectivity..."
echo "--------------------------------"
BACKEND_HEALTH=$(curl -s "$BACKEND_URL"health)
if [ "$BACKEND_HEALTH" = "OK" ]; then
    echo "✅ Backend health check: OK"
elif [[ "$BACKEND_HEALTH" == *"success"* ]]; then
    echo "✅ Backend is responding: $BACKEND_HEALTH"
else
    echo "❌ Backend not responding: $BACKEND_HEALTH"
fi

echo ""
echo "2️⃣ Testing API Endpoints..."
echo "---------------------------"
echo "Testing device registration endpoint..."
REG_RESPONSE=$(curl -s -X POST "$BACKEND_URL"test/devices/test_device_$(date +%s)/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test_device",
    "deviceName": "Test Device",
    "model": "Test Model",
    "manufacturer": "Test Manufacturer",
    "androidVersion": "Test Android",
    "isConnected": true
  }')
echo "Registration Response: $REG_RESPONSE"

echo ""
echo "3️⃣ Mobile App Status..."
echo "----------------------"
APP_RUNNING=$(adb shell ps | grep devicesync)
if [ -n "$APP_RUNNING" ]; then
    echo "✅ App is running"
    echo "📱 Process: $APP_RUNNING"
else
    echo "❌ App is not running"
fi

echo ""
echo "4️⃣ Manual Testing Instructions..."
echo "--------------------------------"
echo "📱 ON YOUR MOBILE DEVICE:"
echo ""
echo "1. Open the DeviceSync app"
echo "2. Look for these buttons on the screen:"
echo "   - 'Test Data Fetch' - Tests local data access"
echo "   - 'Test All' - Runs comprehensive sync test"
echo "   - 'Current Device Sync' - Shows sync status"
echo ""
echo "3. Try pressing 'Test Data Fetch' first:"
echo "   - This will test if the app can access contacts, call logs, SMS"
echo "   - You should see toast messages with counts"
echo ""
echo "4. Then try pressing 'Test All':"
echo "   - This will attempt to sync all data types to the backend"
echo "   - You should see progress messages"
echo ""
echo "5. Check the sync status:"
echo "   - The 'Current Device Sync' button should show status"
echo ""

echo "5️⃣ Monitoring Commands..."
echo "-------------------------"
echo "To monitor backend for incoming requests:"
echo "  tail -f Backend/server.log | grep -E '(POST|GET|device)'"
echo ""
echo "To monitor app logs:"
echo "  adb logcat | grep com.devicesync.app"
echo ""
echo "To monitor sync-specific logs:"
echo "  adb logcat | grep -E '(MainViewModel|DeviceSync|sync|Sync)'"
echo ""

echo "🎯 NETWORK TEST COMPLETE"
echo "========================" 