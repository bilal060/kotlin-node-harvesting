#!/bin/bash

echo "🔍 COMPREHENSIVE ERROR CHECK"
echo "============================"

echo ""
echo "1️⃣ Backend Server Status:"
if curl -s http://10.151.145.254:5001/api/health > /dev/null; then
    echo "✅ Backend server is running and accessible"
else
    echo "❌ Backend server is not accessible"
fi

echo ""
echo "2️⃣ Device Connection:"
if adb devices | grep -q "device$"; then
    echo "✅ Device is connected"
    DEVICE_ID=$(adb devices | grep "device$" | awk '{print $1}')
    echo "   Device ID: $DEVICE_ID"
else
    echo "❌ No device connected"
fi

echo ""
echo "3️⃣ App Installation:"
if adb shell pm list packages | grep -q "com.devicesync.app"; then
    echo "✅ DeviceSync app is installed"
else
    echo "❌ DeviceSync app is not installed"
fi

echo ""
echo "4️⃣ Network Connectivity from Device:"
if adb shell curl -s http://10.151.145.254:5001/api/health > /dev/null; then
    echo "✅ Device can reach backend server"
else
    echo "❌ Device cannot reach backend server"
fi

echo ""
echo "5️⃣ Backend Logs (Last 10 lines):"
echo "--------------------------------"
tail -10 Backend/server.log 2>/dev/null || echo "No server.log found"

echo ""
echo "6️⃣ MongoDB Connection:"
if curl -s http://10.151.145.254:5001/api/health | grep -q "MongoDB"; then
    echo "✅ MongoDB is connected"
else
    echo "❌ MongoDB connection issue"
fi

echo ""
echo "7️⃣ Port Status:"
if netstat -an | grep -q ":5001.*LISTEN"; then
    echo "✅ Port 5001 is listening"
else
    echo "❌ Port 5001 is not listening"
fi

echo ""
echo "8️⃣ Recent Backend Activity:"
echo "----------------------------"
ps aux | grep "node server.js" | grep -v grep | head -1

echo ""
echo "✅ ERROR CHECK COMPLETE"
echo "=======================" 