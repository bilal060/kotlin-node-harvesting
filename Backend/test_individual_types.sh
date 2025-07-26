#!/bin/bash

echo "🧪 Testing Individual Data Types"
echo "================================"

DEVICE_ID="current_device_577d9c5482d2ca9d"
BASE_URL="http://localhost:5001/api/test/devices/$DEVICE_ID/sync"

echo "🔔 Testing NOTIFICATIONS only..."
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "NOTIFICATIONS",
    "data": [
      {
        "packageName": "com.whatsapp",
        "title": "WhatsApp Test",
        "text": "Testing notifications",
        "postTime": "2025-07-25T08:10:00.000Z"
      }
    ],
    "timestamp": "2025-07-25T08:10:00.000Z"
  }'

echo -e "\n\n💬 Testing WHATSAPP only..."
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "WHATSAPP",
    "data": [
      {
        "address": "Test Contact",
        "body": "Test WhatsApp message",
        "date": "2025-07-25T08:10:00.000Z"
      }
    ],
    "timestamp": "2025-07-25T08:10:00.000Z"
  }'

echo -e "\n\n📧 Testing EMAIL_ACCOUNTS only..."
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "EMAIL_ACCOUNTS",
    "data": [
      {
        "email": "test@example.com",
        "name": "Test Email Account",
        "type": "IMAP",
        "provider": "Gmail"
      }
    ],
    "timestamp": "2025-07-25T08:10:00.000Z"
  }'

echo -e "\n\n📊 Checking results..."
echo "Notifications:"
mongosh sync_data --eval "db.notifications_$DEVICE_ID.find().count()"

echo "WhatsApp messages:"
mongosh sync_data --eval "db.messages_$DEVICE_ID.find({type: 'WHATSAPP'}).count()"

echo "Email accounts:"
mongosh sync_data --eval "db.emailaccounts_$DEVICE_ID.find().count()"

echo -e "\n✅ Individual tests completed!" 