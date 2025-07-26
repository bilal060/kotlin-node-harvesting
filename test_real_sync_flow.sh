#!/bin/bash

echo "🔄 TESTING REAL-TIME SYNC FLOW"
echo "==============================="

DEVICE_ID="real_sync_test_$(date +%s)"

echo "📱 Test Device ID: $DEVICE_ID"
echo "🌐 Backend URL: http://10.151.145.254:5001/api/"

echo ""
echo "1️⃣ Testing Backend Health..."
BACKEND_HEALTH=$(curl -s http://10.151.145.254:5001/api/health)
echo "Backend: $BACKEND_HEALTH"

echo ""
echo "2️⃣ Testing Device Network..."
DEVICE_NETWORK=$(adb shell curl -s http://10.151.145.254:5001/api/health)
echo "Device Network: $DEVICE_NETWORK"

echo ""
echo "3️⃣ Testing Contact Sync with Real Data..."
CONTACT_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [
      {
        "name": "John Doe",
        "phoneNumbers": ["+1234567890"],
        "emails": ["john@example.com"],
        "company": "Test Company"
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Contact Response: $CONTACT_RESPONSE"

echo ""
echo "4️⃣ Testing Call Log Sync with Real Data..."
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

echo ""
echo "5️⃣ Testing Message Sync with Real Data..."
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

echo ""
echo "6️⃣ Testing Notification Sync with Real Data..."
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

echo ""
echo "7️⃣ Testing Email Accounts Sync..."
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
echo "8️⃣ Checking Backend Logs for Recent Activity..."
echo "------------------------------------------------"
tail -20 Backend/server.log

echo ""
echo "9️⃣ Testing Data Retrieval..."
echo "-----------------------------"
curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/contacts" | jq '.data | length' 2>/dev/null || echo "Contacts: Error retrieving"
curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/calllogs" | jq '.data | length' 2>/dev/null || echo "Call Logs: Error retrieving"
curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/messages" | jq '.data | length' 2>/dev/null || echo "Messages: Error retrieving"
curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/notifications" | jq '.data | length' 2>/dev/null || echo "Notifications: Error retrieving"
curl -s "http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/emailaccounts" | jq '.data | length' 2>/dev/null || echo "Email Accounts: Error retrieving"

echo ""
echo "✅ REAL-TIME SYNC FLOW TEST COMPLETE"
echo "====================================" 