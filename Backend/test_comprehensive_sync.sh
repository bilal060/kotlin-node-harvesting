#!/bin/bash

echo "🧪 COMPREHENSIVE DATA SYNC TEST"
echo "================================"

# Test 1: Email Accounts
echo ""
echo "📧 Testing Email Accounts..."
curl -X POST http://localhost:5001/api/test/devices/7f7e113e/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "EMAIL_ACCOUNTS",
    "data": [
      {
        "email": "test1@gmail.com",
        "type": "Gmail",
        "name": "Test Account 1"
      },
      {
        "email": "test2@outlook.com", 
        "type": "Outlook",
        "name": "Test Account 2"
      }
    ],
    "timestamp": "2025-07-25T03:30:00.000Z"
  }'

echo ""
echo ""

# Test 2: Notifications
echo "🔔 Testing Notifications..."
curl -X POST http://localhost:5001/api/test/devices/7f7e113e/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "NOTIFICATIONS",
    "data": [
      {
        "packageName": "com.whatsapp",
        "title": "John Doe",
        "text": "Hey, how are you doing?",
        "postTime": "2025-07-25T03:30:00.000Z"
      },
      {
        "packageName": "com.android.email",
        "title": "New email from Jane Smith",
        "text": "Meeting notes attached",
        "postTime": "2025-07-25T03:30:00.000Z"
      }
    ],
    "timestamp": "2025-07-25T03:30:00.000Z"
  }'

echo ""
echo ""

# Test 3: WhatsApp Messages
echo "💬 Testing WhatsApp Messages..."
curl -X POST http://localhost:5001/api/test/devices/7f7e113e/sync \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "WHATSAPP",
    "data": [
      {
        "address": "John Doe",
        "body": "Hey, how are you doing?",
        "type": "WHATSAPP",
        "date": "2025-07-25T03:30:00.000Z"
      },
      {
        "address": "Jane Smith",
        "body": "Can you send me the meeting notes?",
        "type": "WHATSAPP", 
        "date": "2025-07-25T03:30:00.000Z"
      }
    ],
    "timestamp": "2025-07-25T03:30:00.000Z"
  }'

echo ""
echo ""

# Check database results
echo "📊 Checking Database Results..."
echo "Email Accounts:"
mongosh sync_data --eval "db.emailaccounts_7f7e113e.countDocuments()"

echo "Notifications:"
mongosh sync_data --eval "db.notifications_7f7e113e.countDocuments()"

echo "WhatsApp Messages:"
mongosh sync_data --eval "db.messages_7f7e113e.countDocuments({type: 'WHATSAPP'})"

echo ""
echo "✅ Test completed!" 