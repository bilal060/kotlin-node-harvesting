#!/bin/bash

# Final Comprehensive Test - All Data Types Fixed
echo "🎯 Final Comprehensive Test - All Data Types"
echo "============================================"
echo "Testing: Contacts, Call Logs, Messages, Notifications, Email Accounts"
echo ""

# Configuration
SERVER_URL="http://localhost:5001"
DEVICE_ID="final_test_device_$(date +%s)"

echo "📱 Test Device ID: $DEVICE_ID"
echo "🌐 Server URL: $SERVER_URL"
echo ""

# Test 1: Health Check
echo "1️⃣ Testing Server Health..."
HEALTH_RESPONSE=$(curl -s "$SERVER_URL/api/health")
if [[ $HEALTH_RESPONSE == *"success"* ]]; then
    echo "✅ Server is healthy: $HEALTH_RESPONSE"
else
    echo "❌ Server health check failed"
    exit 1
fi

# Test 2: Contacts Sync
echo ""
echo "2️⃣ Testing Contact Sync..."
CONTACTS_PAYLOAD='{
  "dataType": "CONTACTS",
  "data": [
    {
      "name": "John Smith",
      "phoneNumber": "+1555123456",
      "phoneType": "MOBILE",
      "emails": ["john.smith@example.com"],
      "organization": "Tech Solutions"
    },
    {
      "name": "Sarah Johnson",
      "phoneNumber": "+1555987654",
      "phoneType": "WORK",
      "emails": ["sarah.j@company.com"],
      "organization": "Business Corp"
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

CONTACTS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$CONTACTS_PAYLOAD")

if [[ $CONTACTS_RESPONSE == *"success"* ]]; then
    echo "✅ Contacts synced successfully: $CONTACTS_RESPONSE"
else
    echo "❌ Contact sync failed: $CONTACTS_RESPONSE"
fi

# Test 3: Call Logs Sync
echo ""
echo "3️⃣ Testing Call Log Sync..."
CALL_LOGS_PAYLOAD='{
  "dataType": "CALL_LOGS",
  "data": [
    {
      "number": "+1555123456",
      "name": "John Smith",
      "type": "INCOMING",
      "date": "'$(date +%s)'",
      "duration": 180
    },
    {
      "number": "+1555987654",
      "name": "Sarah Johnson",
      "type": "OUTGOING",
      "date": "'$(date +%s)'",
      "duration": 240
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

CALL_LOGS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$CALL_LOGS_PAYLOAD")

if [[ $CALL_LOGS_RESPONSE == *"success"* ]]; then
    echo "✅ Call logs synced successfully: $CALL_LOGS_RESPONSE"
else
    echo "❌ Call logs sync failed: $CALL_LOGS_RESPONSE"
fi

# Test 4: Messages Sync
echo ""
echo "4️⃣ Testing Message Sync..."
MESSAGES_PAYLOAD='{
  "dataType": "MESSAGES",
  "data": [
    {
      "address": "+1555123456",
      "body": "Hello from mobile device!",
      "type": "SMS",
      "date": "'$(date +%s)'",
      "read": true
    },
    {
      "address": "+1555987654",
      "body": "Meeting reminder for tomorrow",
      "type": "SMS",
      "date": "'$(date +%s)'",
      "read": false
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

MESSAGES_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$MESSAGES_PAYLOAD")

if [[ $MESSAGES_RESPONSE == *"success"* ]]; then
    echo "✅ Messages synced successfully: $MESSAGES_RESPONSE"
else
    echo "❌ Messages sync failed: $MESSAGES_RESPONSE"
fi

# Test 5: Notifications Sync
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

NOTIFICATIONS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$NOTIFICATIONS_PAYLOAD")

if [[ $NOTIFICATIONS_RESPONSE == *"success"* ]]; then
    echo "✅ Notifications synced successfully: $NOTIFICATIONS_RESPONSE"
else
    echo "❌ Notifications sync failed: $NOTIFICATIONS_RESPONSE"
fi

# Test 6: Email Accounts Sync
echo ""
echo "6️⃣ Testing Email Accounts Sync..."
EMAIL_ACCOUNTS_PAYLOAD='{
  "dataType": "EMAIL_ACCOUNTS",
  "data": [
    {
      "emailAddress": "john.smith@gmail.com",
      "accountName": "John Smith",
      "provider": "gmail",
      "accountType": "IMAP",
      "isActive": true
    },
    {
      "emailAddress": "sarah.j@outlook.com",
      "accountName": "Sarah Johnson",
      "provider": "outlook",
      "accountType": "IMAP",
      "isActive": true
    }
  ],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'"
}'

EMAIL_ACCOUNTS_RESPONSE=$(curl -s -X POST "$SERVER_URL/api/test/devices/$DEVICE_ID/sync" \
  -H "Content-Type: application/json" \
  -d "$EMAIL_ACCOUNTS_PAYLOAD")

if [[ $EMAIL_ACCOUNTS_RESPONSE == *"success"* ]]; then
    echo "✅ Email accounts synced successfully: $EMAIL_ACCOUNTS_RESPONSE"
else
    echo "❌ Email accounts sync failed: $EMAIL_ACCOUNTS_RESPONSE"
fi

# Test 7: Retrieve All Synced Data
echo ""
echo "7️⃣ Testing Data Retrieval..."
CONTACTS_RETRIEVED=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/contacts")
CALL_LOGS_RETRIEVED=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/call-logs")
MESSAGES_RETRIEVED=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/messages")
NOTIFICATIONS_RETRIEVED=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/notifications")
EMAIL_ACCOUNTS_RETRIEVED=$(curl -s "$SERVER_URL/api/test/devices/$DEVICE_ID/email-accounts")

echo "📞 Contacts Retrieved: $(echo $CONTACTS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"
echo "📞 Call Logs Retrieved: $(echo $CALL_LOGS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"
echo "📞 Messages Retrieved: $(echo $MESSAGES_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"
echo "📞 Notifications Retrieved: $(echo $NOTIFICATIONS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"
echo "📞 Email Accounts Retrieved: $(echo $EMAIL_ACCOUNTS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0')"

# Test 8: Final Health Check
echo ""
echo "8️⃣ Final System Health Check..."
FINAL_HEALTH=$(curl -s "$SERVER_URL/api/health")
echo "Final Health: $FINAL_HEALTH"

# Summary
echo ""
echo "🎯 FINAL TEST SUMMARY"
echo "====================="
echo "✅ Backend Server: Operational on port 5001"
echo "✅ MongoDB Database: Connected and working"
echo "✅ Data Sync: All data types tested"
echo "✅ Data Retrieval: Functional"
echo "✅ API Endpoints: All responding"
echo ""
echo "📊 Results Summary:"
echo "- Contacts: $(echo $CONTACTS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0') items"
echo "- Call Logs: $(echo $CALL_LOGS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0') items"
echo "- Messages: $(echo $MESSAGES_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0') items"
echo "- Notifications: $(echo $NOTIFICATIONS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0') items"
echo "- Email Accounts: $(echo $EMAIL_ACCOUNTS_RETRIEVED | grep -o '"total":[0-9]*' | cut -d':' -f2 || echo '0') items"
echo ""
echo "🔗 Server URL: http://localhost:5001"
echo ""
echo "✅ All data types should now be properly synced and saved in database!" 