#!/bin/bash

echo "🎯 FINAL VERIFICATION - MOBILE APP SYNC STATUS"
echo "=============================================="

BACKEND_URL="http://10.151.145.254:5001/api/"

echo "1️⃣ Current System Status..."
echo "---------------------------"
echo "Device Connected: $(adb devices | grep -v "List of devices attached" | grep -v "^$" | wc -l) device(s)"
echo "App Running: $(adb shell ps | grep -q devicesync && echo "✅ Yes" || echo "❌ No")"
echo "Backend Health: $(curl -s "$BACKEND_URL"health | grep -q "success" && echo "✅ Healthy" || echo "❌ Unhealthy")"

echo ""
echo "2️⃣ Recent Activity Analysis..."
echo "-----------------------------"
echo "Recent app logs show:"
RECENT_ACTIVITY=$(adb logcat -d | grep "com.devicesync.app" | tail -5 | grep -E "(Toast|MainActivity|sync|Sync)")
if [ -n "$RECENT_ACTIVITY" ]; then
    echo "✅ App activity detected:"
    echo "$RECENT_ACTIVITY"
else
    echo "❌ No recent app activity found"
fi

echo ""
echo "3️⃣ Backend Request Analysis..."
echo "-----------------------------"
echo "Recent backend requests:"
BACKEND_REQUESTS=$(tail -10 Backend/server.log | grep -E "(POST|GET|device)" | tail -5)
if [ -n "$BACKEND_REQUESTS" ]; then
    echo "✅ Backend requests found:"
    echo "$BACKEND_REQUESTS"
else
    echo "❌ No recent backend requests"
fi

echo ""
echo "4️⃣ Database Records Check..."
echo "----------------------------"
echo "Current database records:"
echo "Contacts: $(curl -s "$BACKEND_URL"data/contacts | jq '.data | length' 2>/dev/null || echo "0")"
echo "Call Logs: $(curl -s "$BACKEND_URL"data/calllogs | jq '.data | length' 2>/dev/null || echo "0")"
echo "Messages: $(curl -s "$BACKEND_URL"data/messages | jq '.data | length' 2>/dev/null || echo "0")"
echo "Notifications: $(curl -s "$BACKEND_URL"data/notifications | jq '.data | length' 2>/dev/null || echo "0")"

echo ""
echo "🔍 DIAGNOSIS SUMMARY"
echo "==================="

# Check if app showed toast messages
TOAST_ACTIVITY=$(adb logcat -d | grep "com.devicesync.app" | grep -E "(Toast|toast)" | tail -1)
if [ -n "$TOAST_ACTIVITY" ]; then
    echo "✅ App Interaction Detected:"
    echo "   - Toast messages were shown"
    echo "   - App was actively used"
    echo "   - Buttons were likely pressed"
else
    echo "❌ No App Interaction:"
    echo "   - No toast messages detected"
    echo "   - App may not have been used"
fi

# Check if any backend requests were made
RECENT_BACKEND=$(tail -20 Backend/server.log | grep -E "(POST|GET)" | grep -v "test_device" | tail -1)
if [ -n "$RECENT_BACKEND" ]; then
    echo "✅ Backend Requests Detected:"
    echo "   - Mobile app made API calls"
    echo "   - Data sync attempted"
else
    echo "❌ No Backend Requests:"
    echo "   - Mobile app did not reach backend"
    echo "   - Possible network connectivity issue"
fi

echo ""
echo "💡 RECOMMENDATIONS"
echo "=================="

if [ -n "$TOAST_ACTIVITY" ] && [ -z "$RECENT_BACKEND" ]; then
    echo "🎯 ISSUE IDENTIFIED: App worked but no backend sync"
    echo ""
    echo "Possible causes:"
    echo "1. Network connectivity issue between app and backend"
    echo "2. API endpoint configuration problem"
    echo "3. Sync logic failed silently"
    echo "4. Backend URL not accessible from mobile device"
    echo ""
    echo "Next steps:"
    echo "1. Check mobile device internet connection"
    echo "2. Verify backend URL is accessible from mobile: http://10.151.145.254:5001"
    echo "3. Try pressing 'Test All' button again"
    echo "4. Check app logs for error messages"
elif [ -z "$TOAST_ACTIVITY" ]; then
    echo "🎯 ISSUE IDENTIFIED: App not used"
    echo ""
    echo "The app was not interacted with. Please:"
    echo "1. Open the DeviceSync app"
    echo "2. Press 'Test Data Fetch' button"
    echo "3. Press 'Test All' button"
    echo "4. Watch for toast messages"
else
    echo "✅ SUCCESS: App and backend working correctly"
fi

echo ""
echo "🎯 FINAL VERIFICATION COMPLETE"
echo "==============================" 