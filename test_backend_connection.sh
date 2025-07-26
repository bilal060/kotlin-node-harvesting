#!/bin/bash

echo "🔍 Testing Backend Connection for Kotlin App"
echo "============================================="

# Test backend health
echo "1️⃣ Testing Backend Health..."
BACKEND_HEALTH=$(curl -s http://10.151.145.254:5001/api/health)
echo "Backend Response: $BACKEND_HEALTH"

# Test device connection
echo ""
echo "2️⃣ Testing Device Connection..."
adb devices

# Test app installation
echo ""
echo "3️⃣ Testing App Installation..."
adb shell pm list packages | grep devicesync

# Test network connectivity from device
echo ""
echo "4️⃣ Testing Network Connectivity from Device..."
adb shell curl -s http://10.151.145.254:5001/api/health

echo ""
echo "✅ Backend Connection Test Complete!"
echo "📱 App should now be able to connect to: http://10.151.145.254:5001/api/" 