# DeviceSync Backend Server

A simple Express.js backend server for the DeviceSync Android application.

## 🚀 Quick Start

### Prerequisites
- Node.js (version 14 or higher)
- npm or yarn

### Installation

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the server:**
   ```bash
   npm start
   ```

   Or for development with auto-restart:
   ```bash
   npm run dev
   ```

### Server Information
- **URL**: http://localhost:3000
- **API Base**: http://localhost:3000/api/
- **Health Check**: http://localhost:3000/api/health

## 📱 API Endpoints

### Device Management
- `POST /api/devices` - Register a new device
- `GET /api/devices` - Get all devices
- `GET /api/devices/:deviceId` - Get specific device

### Data Synchronization
- `POST /api/devices/:deviceId/sync` - Sync data for a device
- `GET /api/devices/:deviceId/data/:dataType` - Get synced data
- `GET /api/devices/:deviceId/sync-history` - Get sync history

### Data Type Management
- `GET /api/devices/:deviceId/data-types` - Get data types for device
- `PUT /api/devices/:deviceId/data-types/:dataType` - Update data type

### Health Check
- `GET /api/health` - Server health status

## 🔧 Configuration

### For Android Emulator
The Android app is configured to connect to `http://10.0.2.2:3000/api/` (localhost from emulator perspective).

### For Physical Device
If using a physical device, update the BASE_URL in the Android app to your computer's IP address:
```kotlin
private const val BASE_URL = "http://192.168.1.100:3000/api/"
```

## 📊 Data Storage

Currently using in-memory storage. For production, replace with:
- MongoDB
- PostgreSQL
- MySQL
- Redis

## 🛠️ Development

### Adding New Endpoints
1. Add route in `server.js`
2. Update Android `ApiService.kt` interface
3. Test with Postman or curl

### Testing API
```bash
# Health check
curl http://localhost:3000/api/health

# Get devices
curl http://localhost:3000/api/devices

# Register device
curl -X POST http://localhost:3000/api/devices \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test123","deviceName":"Test Device"}'
```

## 🔒 Security Notes

This is a development server. For production:
- Add authentication
- Use HTTPS
- Implement rate limiting
- Add input validation
- Use environment variables
- Add database persistence

## 📝 Logs

The server logs all API requests and responses. Check the console for:
- Device registrations
- Sync operations
- Data retrieval
- Error messages
