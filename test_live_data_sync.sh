#!/bin/bash

# Test Live Data Synchronization between Mobile App and Server
echo "🔄 Testing Live Data Synchronization"
echo "====================================="

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

# Test 2: Device Registration
echo ""
echo "2️⃣ Testing Device Registration..."
DEVICE_DATA='{
  "deviceId": "'$DEVICE_ID'",
  "model": "Test Device",
  "brand": "Test Brand",
  "systemName": "Android",
  "systemVersion": "13",
  "appVersion": "1.0.0",
  "buildNumber": "1"
}'

REGISTER_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/register" \
  -H "Content-Type: application/json" \
  -d "$DEVICE_DATA")
echo "Registration Response: $REGISTER_RESPONSE"

# Test 3: Sync Contacts
echo ""
echo "3️⃣ Testing Contact Sync..."
CONTACTS_DATA='[
  {
    "id": "contact_1",
    "name": "John Doe",
    "phoneNumbers": ["+1234567890"],
    "emails": ["john@example.com"],
    "company": "Test Company",
    "jobTitle": "Developer",
    "createdAt": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
    "updatedAt": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  },
  {
    "id": "contact_2",
    "name": "Jane Smith",
    "phoneNumbers": ["+0987654321"],
    "emails": ["jane@example.com"],
    "company": "Another Company",
    "jobTitle": "Manager",
    "createdAt": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
    "updatedAt": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }
]'

CONTACTS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": '$CONTACTS_DATA',
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')
echo "Contacts Sync Response: $CONTACTS_RESPONSE"

# Test 4: Sync Call Logs
echo ""
echo "4️⃣ Testing Call Log Sync..."
CALL_LOGS_DATA='[
  {
    "id": "call_1",
    "phoneNumber": "+1234567890",
    "name": "John Doe",
    "duration": 120,
    "type": "incoming",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
    "dateTime": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  },
  {
    "id": "call_2",
    "phoneNumber": "+0987654321",
    "name": "Jane Smith",
    "duration": 300,
    "type": "outgoing",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
    "dateTime": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }
]'

CALL_LOGS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CALL_LOGS",
    "data": '$CALL_LOGS_DATA',
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')
echo "Call Logs Sync Response: $CALL_LOGS_RESPONSE"

# Test 5: Sync Messages
echo ""
echo "5️⃣ Testing Message Sync..."
MESSAGES_DATA='[
  {
    "id": "msg_1",
    "address": "+1234567890",
    "body": "Hello from test device!",
    "type": "inbox",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
    "date": "'$(date +%s)'",
    "read": true
  },
  {
    "id": "msg_2",
    "address": "+0987654321",
    "body": "Test message response",
    "type": "sent",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
    "date": "'$(date +%s)'",
    "read": false
  }
]'

MESSAGES_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "MESSAGES",
    "data": '$MESSAGES_DATA',
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
  }')
echo "Messages Sync Response: $MESSAGES_RESPONSE"

# Test 6: Get Device Status
echo ""
echo "6️⃣ Testing Device Status..."
STATUS_RESPONSE=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/status")
echo "Device Status Response: $STATUS_RESPONSE"

# Test 7: Final Health Check
echo ""
echo "7️⃣ Final Health Check..."
FINAL_HEALTH=$(curl -s "$SERVER_URL/api/health")
echo "Final Health Response: $FINAL_HEALTH"

echo ""
echo "✅ Live Data Sync Test Completed!"
echo "📊 Check the server logs for detailed information"
echo "🔗 Server running on: $SERVER_URL" 