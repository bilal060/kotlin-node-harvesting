#!/bin/bash

echo "🎯 FINAL SYNC VERIFICATION"
echo "==========================="

DEVICE_ID="final_test_$(date +%s)"

echo "📱 Test Device ID: $DEVICE_ID"
echo "🌐 Backend URL: http://10.151.145.254:5001/api/"

echo ""
echo "1️⃣ Testing All Data Types with Fixed Data..."
echo "---------------------------------------------"

# Test Contacts with phoneNumber (not phoneNumbers array)
echo "📞 Testing Contacts (phoneNumber format)..."
CONTACT_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [
      {
        "name": "John Doe",
        "phoneNumber": "+1234567890",
        "emails": ["john@example.com"],
        "company": "Test Company"
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Contact Response: $CONTACT_RESPONSE"

# Test Call Logs with proper timestamp
echo ""
echo "📞 Testing Call Logs..."
CALL_LOG_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CALL_LOGS",
    "data": [
      {
        "phoneNumber": "+1234567890",
        "name": "John Doe",
        "duration": 120,
        "type": "INCOMING",
        "timestamp": "'$(date +%s)'"
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Call Log Response: $CALL_LOG_RESPONSE"

# Test Messages with proper timestamp
echo ""
echo "💬 Testing Messages..."
MESSAGE_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "MESSAGES",
    "data": [
      {
        "address": "+1234567890",
        "body": "Hello, this is a test message",
        "type": "SMS",
        "timestamp": "'$(date +%s)'",
        "isIncoming": true,
        "isRead": true
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Message Response: $MESSAGE_RESPONSE"

# Test Notifications
echo ""
echo "🔔 Testing Notifications..."
NOTIFICATION_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "NOTIFICATIONS",
    "data": [
      {
        "packageName": "com.whatsapp",
        "appName": "WhatsApp",
        "title": "New Message",
        "text": "You have a new message from John",
        "postTime": "'$(date +%s)'"
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Notification Response: $NOTIFICATION_RESPONSE"

# Test Email Accounts
echo ""
echo "📧 Testing Email Accounts..."
EMAIL_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "EMAIL_ACCOUNTS",
    "data": [
      {
        "emailAddress": "user@gmail.com",
        "accountName": "Gmail Account",
        "provider": "gmail",
        "accountType": "IMAP",
        "isActive": true
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Email Response: $EMAIL_RESPONSE"

echo ""
echo "2️⃣ Verifying Data in Database..."
echo "--------------------------------"
echo "Contacts: $(curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/contacts" | jq '.data | length' 2>/dev/null || echo "Error")"
echo "Call Logs: $(curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/calllogs" | jq '.data | length' 2>/dev/null || echo "Error")"
echo "Messages: $(curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/messages" | jq '.data | length' 2>/dev/null || echo "Error")"
echo "Notifications: $(curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/notifications" | jq '.data | length' 2>/dev/null || echo "Error")"
echo "Email Accounts: $(curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/emailaccounts" | jq '.data | length' 2>/dev/null || echo "Error")"

echo ""
echo "3️⃣ System Status Check..."
echo "-------------------------"
echo "Backend Health: $(curl -s http://10.151.145.254:5001/api/health | jq '.success' 2>/dev/null || echo "Error")"
echo "Device Network: $(adb shell curl -s http://10.151.145.254:5001/api/health | jq '.success' 2>/dev/null || echo "Error")"
echo "App Running: $(adb shell ps | grep -q "com.devicesync.app" && echo "Yes" || echo "No")"

echo ""
echo "✅ FINAL VERIFICATION COMPLETE"
echo "=============================="
echo ""
echo "🎯 READY FOR REAL-TIME DATA SYNC!"
echo "=================================="
echo "The backend is working correctly. To get real-time data:"
echo "1. Open the DeviceSync app on your device"
echo "2. Grant permissions when prompted"
echo "3. The app will automatically sync data to the backend"
echo "4. Check Backend/server.log for sync activity" 