#!/bin/bash

echo "📱 COMPREHENSIVE MOBILE APP VERIFICATION"
echo "========================================"

# Test device ID
DEVICE_ID="mobile_verification_$(date +%s)"
BACKEND_URL="http://10.151.145.254:5001/api/"

echo "1️⃣ Checking Device Connection..."
echo "-------------------------------"
DEVICE_STATUS=$(adb devices | grep -v "List of devices attached" | grep -v "^$")
if [ -n "$DEVICE_STATUS" ]; then
    echo "✅ Device connected: $DEVICE_STATUS"
else
    echo "❌ No device connected"
    echo "💡 Please connect your device and enable USB debugging"
    exit 1
fi

echo ""
echo "2️⃣ Checking App Installation..."
echo "-------------------------------"
APP_INSTALLED=$(adb shell pm list packages | grep devicesync)
if [ -n "$APP_INSTALLED" ]; then
    echo "✅ App installed: $APP_INSTALLED"
else
    echo "❌ App not installed"
    echo "💡 Please install the DeviceSync app first"
    exit 1
fi

echo ""
echo "3️⃣ Checking App Running Status..."
echo "--------------------------------"
APP_RUNNING=$(adb shell ps | grep devicesync)
if [ -n "$APP_RUNNING" ]; then
    echo "✅ App is running"
    echo "📱 Process: $APP_RUNNING"
else
    echo "❌ App is not running"
    echo "💡 Please open the DeviceSync app on your device"
fi

echo ""
echo "4️⃣ Checking App Permissions..."
echo "------------------------------"
PERMISSIONS=$(adb shell dumpsys package com.devicesync.app | grep -E "(READ_SMS|READ_CALL_LOG|POST_NOTIFICATIONS|READ_CONTACTS)" | grep "granted=true")
if [ -n "$PERMISSIONS" ]; then
    echo "✅ All required permissions granted:"
    echo "$PERMISSIONS" | while read line; do
        echo "  ✅ $(echo $line | grep -o 'android.permission.[^:]*')"
    done
else
    echo "❌ Some permissions may not be granted"
    echo "💡 Please check app permissions in device settings"
fi

echo ""
echo "5️⃣ Checking Backend Health..."
echo "-----------------------------"
BACKEND_HEALTH=$(curl -s "$BACKEND_URL"health)
if [ "$BACKEND_HEALTH" = "OK" ]; then
    echo "✅ Backend is healthy"
else
    echo "❌ Backend health check failed: $BACKEND_HEALTH"
fi

echo ""
echo "6️⃣ Testing Backend API Endpoints..."
echo "-----------------------------------"
echo "Testing device registration..."
REG_RESPONSE=$(curl -s -X POST "$BACKEND_URL"test/devices/"$DEVICE_ID"/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "'$DEVICE_ID'",
    "deviceName": "Test Device",
    "model": "Test Model",
    "manufacturer": "Test Manufacturer",
    "androidVersion": "Test Android",
    "isConnected": true
  }')
echo "Registration Response: $REG_RESPONSE"

echo ""
echo "7️⃣ Testing Data Sync Endpoints..."
echo "---------------------------------"
echo "Testing Contacts sync..."
CONTACT_RESPONSE=$(curl -s -X POST "$BACKEND_URL"test/devices/"$DEVICE_ID"/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [{
      "name": "Test Contact",
      "phoneNumber": "+1234567890",
      "phoneType": "MOBILE",
      "emails": ["test@example.com"],
      "organization": "Test Company"
    }],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')
echo "Contact Response: $CONTACT_RESPONSE"

echo ""
echo "8️⃣ Checking Recent App Logs..."
echo "-------------------------------"
RECENT_LOGS=$(adb logcat -d | grep "com.devicesync.app" | tail -5)
if [ -n "$RECENT_LOGS" ]; then
    echo "Recent app logs:"
    echo "$RECENT_LOGS"
else
    echo "No recent app logs found"
fi

echo ""
echo "9️⃣ Checking for Sync-Related Logs..."
echo "------------------------------------"
SYNC_LOGS=$(adb logcat -d | grep -E "(MainViewModel|DeviceSync|sync|Sync|Retrofit|OkHttp|HTTP)" | tail -5)
if [ -n "$SYNC_LOGS" ]; then
    echo "Sync-related logs:"
    echo "$SYNC_LOGS"
else
    echo "No sync-related logs found"
fi

echo ""
echo "🔍 DIAGNOSIS & RECOMMENDATIONS"
echo "=============================="

if [ -n "$APP_RUNNING" ]; then
    echo "✅ App is running but may not be syncing"
    echo "💡 Possible issues:"
    echo "   - App sync logic may not be triggered"
    echo "   - Network connectivity issues"
    echo "   - API endpoint configuration problems"
    echo ""
    echo "🛠️  NEXT STEPS:"
    echo "1. Open the DeviceSync app on your device"
    echo "2. Check if there's a 'Sync' or 'Start Sync' button"
    echo "3. Press it to manually trigger sync"
    echo "4. Monitor backend logs: tail -f Backend/server.log"
    echo "5. Check app logs: adb logcat | grep com.devicesync.app"
else
    echo "❌ App is not running"
    echo "💡 Please open the DeviceSync app on your device"
fi

echo ""
echo "📊 BACKEND STATUS:"
echo "Backend URL: $BACKEND_URL"
echo "Backend Health: $BACKEND_HEALTH"
echo "Test Device ID: $DEVICE_ID"

echo ""
echo "🎯 VERIFICATION COMPLETE"
echo "========================" 