#!/bin/bash

echo "🚀 TRIGGERING REAL APP SYNC"
echo "============================"

echo "1️⃣ Checking if app is running..."
if adb shell ps | grep -q "com.devicesync.app"; then
    echo "✅ App is running"
else
    echo "❌ App is not running"
    exit 1
fi

echo ""
echo "2️⃣ Opening the app..."
adb shell am start -n com.devicesync.app/.MainActivity

echo ""
echo "3️⃣ Waiting for app to load..."
sleep 5

echo ""
echo "4️⃣ Checking app logs for API calls..."
echo "----------------------------------------"
adb logcat -d | grep -E "(Retrofit|OkHttp|API|sync)" | tail -10

echo ""
echo "5️⃣ Checking backend for incoming requests..."
echo "--------------------------------------------"
tail -10 Backend/server.log

echo ""
echo "6️⃣ Testing direct API call from device..."
echo "------------------------------------------"
adb shell curl -s http://10.151.145.254:5001/api/health

echo ""
echo "✅ APP SYNC TRIGGER COMPLETE"
echo "============================" 