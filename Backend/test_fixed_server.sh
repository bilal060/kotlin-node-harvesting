#!/bin/bash

echo "🧪 Testing Fixed DeviceSync Backend Server"
echo "=========================================="

SERVER_URL="http://localhost:5001"

echo ""
echo "1. Testing Health Endpoint..."
curl -s "$SERVER_URL/api/health" | jq '.' || echo "❌ Health check failed"

echo ""
echo "2. Testing Device Registration..."
curl -s -X POST "$SERVER_URL/api/devices" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test-device-123",
    "deviceName": "Test Android Device",
    "model": "Pixel 7",
    "manufacturer": "Google",
    "androidVersion": "13"
  }' | jq '.' || echo "❌ Device registration failed"

echo ""
echo "3. Testing Get Device..."
curl -s "$SERVER_URL/api/devices/test-device-123" | jq '.' || echo "❌ Get device failed"

echo ""
echo "4. Testing Get All Devices..."
curl -s "$SERVER_URL/api/devices" | jq '.' || echo "❌ Get all devices failed"

echo ""
echo "5. Testing Device Settings..."
curl -s "$SERVER_URL/api/devices/test-device-123/settings" | jq '.' || echo "❌ Get device settings failed"

echo ""
echo "6. Testing Data Types..."
curl -s "$SERVER_URL/api/devices/test-device-123/data-types" | jq '.' || echo "❌ Get data types failed"

echo ""
echo "7. Testing Sync History..."
curl -s "$SERVER_URL/api/devices/test-device-123/sync-history" | jq '.' || echo "❌ Get sync history failed"

echo ""
echo "8. Testing Data Sync (Contacts)..."
curl -s -X POST "$SERVER_URL/api/devices/test-device-123/sync" \
  -H "Content-Type: application/json" \
  -d '{
    "dataType": "CONTACTS",
    "data": [
      {
        "name": "John Doe",
        "phoneNumber": "+1234567890",
        "phoneType": "MOBILE",
        "emails": ["john@example.com"],
        "organization": "Test Company"
      }
    ],
    "timestamp": "2024-01-15T10:30:00.000Z"
  }' | jq '.' || echo "❌ Data sync failed"

echo ""
echo "9. Testing Authentication - Login..."
curl -s -X POST "$SERVER_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass"
  }' | jq '.' || echo "❌ Login failed"

echo ""
echo "10. Testing Authentication - Register..."
curl -s -X POST "$SERVER_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "newpass",
    "firstName": "New",
    "lastName": "User"
  }' | jq '.' || echo "❌ Registration failed"

echo ""
echo "✅ All endpoint tests completed!"
echo ""
echo "🎯 Next Steps:"
echo "1. Stop your current backend server"
echo "2. Replace server.js with server_fixed_comprehensive.js"
echo "3. Restart the server: npm start"
echo "4. Test your Kotlin app - all sync issues should be resolved!"