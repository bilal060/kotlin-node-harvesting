#!/bin/bash

# Debug Sync Issues - Test Individual Data Types
echo "🐛 Debugging Sync Issues"
echo "========================"

SERVER_URL="http://localhost:5001"
DEVICE_ID="debug_device_$(date +%s)"

echo "📱 Test Device ID: $DEVICE_ID"
echo "🌐 Server URL: $SERVER_URL"
echo ""

# Test 1: Call Logs with Debug Info
echo "1️⃣ Testing Call Logs (Debug)..."
CALL_LOG_DEBUG='{
  "dataType": "CALL_LOGS",
  "data": [
    {
      "number": "+1555123456",
      "name": "Test Call",
      "type": "INCOMING",
      "date": "'$(date +%s)'",
      "duration": 120
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending call log data: $CALL_LOG_DEBUG"
CALL_LOG_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$CALL_LOG_DEBUG")

echo "Call Log Response: $CALL_LOG_RESPONSE"
echo ""

# Test 2: Messages with Debug Info
echo "2️⃣ Testing Messages (Debug)..."
MESSAGE_DEBUG='{
  "dataType": "MESSAGES",
  "data": [
    {
      "address": "+1555123456",
      "body": "Test message",
      "type": "SMS",
      "date": "'$(date +%s)'",
      "read": true
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending message data: $MESSAGE_DEBUG"
MESSAGE_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$MESSAGE_DEBUG")

echo "Message Response: $MESSAGE_RESPONSE"
echo ""

# Test 3: Notifications with Debug Info
echo "3️⃣ Testing Notifications (Debug)..."
NOTIFICATION_DEBUG='{
  "dataType": "NOTIFICATIONS",
  "data": [
    {
      "packageName": "com.test.app",
      "appName": "Test App",
      "title": "Test Notification",
      "text": "This is a test notification",
      "postTime": "'$(date +%s)'"
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending notification data: $NOTIFICATION_DEBUG"
NOTIFICATION_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$NOTIFICATION_DEBUG")

echo "Notification Response: $NOTIFICATION_RESPONSE"
echo ""

# Test 4: Check what was actually saved
echo "4️⃣ Checking Saved Data..."
echo "Call Logs: $(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/call-logs" | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"
echo "Messages: $(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/messages" | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"
echo "Notifications: $(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/notifications" | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"

echo ""
echo "🔍 Debug Complete - Check server logs for detailed error messages" 