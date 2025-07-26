#!/bin/bash

# Test Live Data Synchronization between Mobile App and Server (Fixed)
echo "🔄 Testing Live Data Synchronization (Fixed)"
echo "============================================="

# Server URL
SERVER_URL="http://localhost:5001"

# Test device ID
DEVICE_ID="test_device_$(date +%s)"

echo "📱 Device ID: $DEVICE_ID"
echo "🌐 Server URL: $SERVER_URL"

# Test 1: Health Check
echo ""
echo "1️⃣ Testing Server Health..."
HEALTH_RESPONSE=$(curl -s "$SERVER_URL/api/health")
echo "Health Response: $HEALTH_RESPONSE"

# Test 2: Sync Contacts (Fixed JSON)
echo ""
echo "2️⃣ Testing Contact Sync..."
CONTACTS_PAYLOAD='{
  "dataType": "CONTACTS",
  "data": [
    {
      "name": "John Doe",
      "phoneNumber": "+1234567890",
      "phoneType": "MOBILE",
      "emails": ["john@example.com"],
      "organization": "Test Company"
    },
    {
      "name": "Jane Smith", 
      "phoneNumber": "+0987654321",
      "phoneType": "MOBILE",
      "emails": ["jane@example.com"],
      "organization": "Another Company"
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending contacts payload..."
CONTACTS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$CONTACTS_PAYLOAD")
echo "Contacts Sync Response: $CONTACTS_RESPONSE"

# Test 3: Sync Call Logs (Fixed JSON)
echo ""
echo "3️⃣ Testing Call Log Sync..."
CALL_LOGS_PAYLOAD='{
  "dataType": "CALL_LOGS",
  "data": [
    {
      "number": "+1234567890",
      "name": "John Doe",
      "type": "incoming",
      "date": "'$(date +%s)'",
      "duration": 120
    },
    {
      "number": "+0987654321",
      "name": "Jane Smith", 
      "type": "outgoing",
      "date": "'$(date +%s)'",
      "duration": 300
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending call logs payload..."
CALL_LOGS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$CALL_LOGS_PAYLOAD")
echo "Call Logs Sync Response: $CALL_LOGS_RESPONSE"

# Test 4: Sync Messages (Fixed JSON)
echo ""
echo "4️⃣ Testing Message Sync..."
MESSAGES_PAYLOAD='{
  "dataType": "MESSAGES",
  "data": [
    {
      "address": "+1234567890",
      "body": "Hello from test device!",
      "type": "inbox",
      "date": "'$(date +%s)'"
    },
    {
      "address": "+0987654321",
      "body": "Test message response",
      "type": "sent", 
      "date": "'$(date +%s)'"
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending messages payload..."
MESSAGES_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$MESSAGES_PAYLOAD")
echo "Messages Sync Response: $MESSAGES_RESPONSE"

# Test 5: Sync Notifications (Fixed JSON)
echo ""
echo "5️⃣ Testing Notification Sync..."
NOTIFICATIONS_PAYLOAD='{
  "dataType": "NOTIFICATIONS",
  "data": [
    {
      "packageName": "com.whatsapp",
      "appName": "WhatsApp",
      "title": "New Message",
      "text": "You have a new message from John",
      "postTime": "'$(date +%s)'"
    },
    {
      "packageName": "com.gmail.android",
      "appName": "Gmail",
      "title": "New Email",
      "text": "You have received a new email",
      "postTime": "'$(date +%s)'"
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

echo "Sending notifications payload..."
NOTIFICATIONS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$NOTIFICATIONS_PAYLOAD")
echo "Notifications Sync Response: $NOTIFICATIONS_RESPONSE"

# Test 6: Get Synced Contacts
echo ""
echo "6️⃣ Testing Get Synced Contacts..."
GET_CONTACTS_RESPONSE=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/contacts")
echo "Get Contacts Response: $GET_CONTACTS_RESPONSE"

# Test 7: Get Synced Call Logs
echo ""
echo "7️⃣ Testing Get Synced Call Logs..."
GET_CALL_LOGS_RESPONSE=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/call-logs")
echo "Get Call Logs Response: $GET_CALL_LOGS_RESPONSE"

# Test 8: Final Health Check
echo ""
echo "8️⃣ Final Health Check..."
FINAL_HEALTH=$(curl -s "$SERVER_URL/api/health")
echo "Final Health Response: $FINAL_HEALTH"

echo ""
echo "✅ Live Data Sync Test Completed!"
echo "📊 Check the server logs for detailed information"
echo "🔗 Server running on: $SERVER_URL" 