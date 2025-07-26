#!/bin/bash

echo "🌐 NETWORK CONNECTIVITY DIAGNOSIS"
echo "================================="

echo "1️⃣ Current Computer IP Addresses..."
echo "-----------------------------------"
echo "Available IP addresses on this computer:"
ifconfig | grep "inet " | grep -v 127.0.0.1 | while read line; do
    IP=$(echo $line | awk '{print $2}')
    echo "  📡 $IP"
done

echo ""
echo "2️⃣ Testing Backend on Different IPs..."
echo "--------------------------------------"

# Test 10.151.145.254
echo "Testing 10.151.145.254:5001..."
RESPONSE_1=$(curl -s --connect-timeout 5 http://10.151.145.254:5001/api/health)
if [ -n "$RESPONSE_1" ]; then
    echo "✅ 10.151.145.254:5001 - WORKING"
    echo "   Response: $RESPONSE_1"
else
    echo "❌ 10.151.145.254:5001 - NOT ACCESSIBLE"
fi

# Test 192.168.1.1
echo "Testing 192.168.1.1:5001..."
RESPONSE_2=$(curl -s --connect-timeout 5 http://192.168.1.1:5001/api/health)
if [ -n "$RESPONSE_2" ]; then
    echo "✅ 192.168.1.1:5001 - WORKING"
    echo "   Response: $RESPONSE_2"
else
    echo "❌ 192.168.1.1:5001 - NOT ACCESSIBLE"
fi

# Test localhost
echo "Testing localhost:5001..."
RESPONSE_3=$(curl -s --connect-timeout 5 http://localhost:5001/api/health)
if [ -n "$RESPONSE_3" ]; then
    echo "✅ localhost:5001 - WORKING"
    echo "   Response: $RESPONSE_3"
else
    echo "❌ localhost:5001 - NOT ACCESSIBLE"
fi

echo ""
echo "3️⃣ Mobile Device Network Test..."
echo "--------------------------------"
echo "📱 ON YOUR MOBILE DEVICE:"
echo ""
echo "1. Open your mobile web browser"
echo "2. Try these URLs one by one:"
echo "   - http://10.151.145.254:5001"
echo "   - http://192.168.1.1:5001"
echo "   - http://localhost:5001"
echo ""
echo "3. Tell me which one works (shows JSON response)"
echo ""

echo "4️⃣ Current Mobile App Configuration..."
echo "--------------------------------------"
echo "Mobile app is currently configured to use:"
echo "  🌐 http://10.151.145.254:5001/api/"
echo ""

echo "5️⃣ Network Troubleshooting..."
echo "-----------------------------"
echo "Common issues and solutions:"
echo ""
echo "❌ Issue: Mobile device cannot reach 10.151.145.254"
echo "💡 Solution: Mobile device might be on different network"
echo "   - Check if mobile device is connected to same WiFi"
echo "   - Try using computer's other IP address"
echo "   - Use computer's local IP (usually 192.168.x.x)"
echo ""
echo "❌ Issue: Firewall blocking connection"
echo "💡 Solution: Check firewall settings"
echo "   - Ensure port 5001 is open"
echo "   - Allow incoming connections"
echo ""
echo "❌ Issue: Backend not accessible from mobile"
echo "💡 Solution: Update mobile app configuration"
echo "   - Change BASE_URL in RetrofitClient.kt"
echo "   - Rebuild and reinstall app"
echo ""

echo "🎯 NEXT STEPS"
echo "============="
echo "1. Test the URLs on your mobile browser"
echo "2. Tell me which IP address works"
echo "3. I'll update the mobile app configuration"
echo "4. Rebuild and reinstall the app"
echo ""

echo "🔧 READY TO FIX NETWORK CONNECTIVITY"
echo "====================================" 