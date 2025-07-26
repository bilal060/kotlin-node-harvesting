#!/bin/bash

echo "🧪 Testing Comprehensive Sync with Fixed Device ID"
echo "=================================================="

DEVICE_ID="current_device_577d9c5482d2ca9d"
BASE_URL="http://localhost:5001/api/test/devices/$DEVICE_ID/sync"

echo "📧 Testing EMAIL_ACCOUNTS sync..."
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "EMAIL_ACCOUNTS",
    "data": [
      {
        "email": "test@example.com",
        "name": "Test Account",
        "type": "IMAP"
      }
    ],
    "timestamp": "2025-07-25T08:05:00.000Z"
  }'

echo -e "\n\n🔔 Testing NOTIFICATIONS sync..."
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "NOTIFICATIONS",
    "data": [
      {
        "packageName": "com.whatsapp",
        "title": "WhatsApp Message",
        "text": "Hello from WhatsApp!",
        "postTime": "2025-07-25T08:05:00.000Z"
      }
    ],
    "timestamp": "2025-07-25T08:05:00.000Z"
  }'

echo -e "\n\n💬 Testing WHATSAPP sync..."
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "WHATSAPP",
    "data": [
      {
        "address": "John Doe",
        "body": "Hello from WhatsApp!",
        "date": "2025-07-25T08:05:00.000Z"
      }
    ],
    "timestamp": "2025-07-25T08:05:00.000Z"
  }'

echo -e "\n\n📊 Checking database collections..."
echo "Email accounts:"
mongosh sync_data --eval "db.emailaccounts_$DEVICE_ID.find().count()"

echo "Notifications:"
mongosh sync_data --eval "db.notifications_$DEVICE_ID.find().count()"

echo "WhatsApp messages:"
mongosh sync_data --eval "db.messages_$DEVICE_ID.find({type: 'WHATSAPP'}).count()"

echo -e "\n✅ Comprehensive test completed!" 