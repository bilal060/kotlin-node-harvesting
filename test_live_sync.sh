#!/bin/bash

echo "🧪 LIVE SYNC TEST"
echo "=================="

DEVICE_ID="test_device_$(date +%s)"

echo "📱 Test Device ID: $DEVICE_ID"
echo "🌐 Backend URL: http://10.151.145.254:5001/api/"

echo ""
echo "1️⃣ Testing Contact Sync..."
CONTACT_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [
      {
        "name": "Test Contact",
        "phoneNumbers": ["+1234567890"],
        "emails": ["test@example.com"],
        "company": "Test Company"
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Contact Response: $CONTACT_RESPONSE"

echo ""
echo "2️⃣ Testing Call Log Sync..."
CALL_LOG_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CALL_LOGS",
    "data": [
      {
        "phoneNumber": "+1234567890",
        "name": "Test Call",
        "duration": 60,
        "type": "INCOMING",
        "timestamp": "'$(date +%s)'"
      }
    ],
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')

echo "Call Log Response: $CALL_LOG_RESPONSE"

echo ""
echo "3️⃣ Testing Message Sync..."
MESSAGE_RESPONSE=$(curl -s -X POST http://10.151.145.254:5001/api/test/devices/$DEVICE_ID/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "MESSAGES",
    "data": [
      {
        "address": "+1234567890",
        "body": "Test message",
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
echo "4️⃣ Checking Backend Logs for Errors..."
echo "----------------------------------------"
tail -5 Backend/server.log

echo ""
echo "✅ LIVE SYNC TEST COMPLETE"
echo "==========================" 