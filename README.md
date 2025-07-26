
# Device Synchronization System

A comprehensive system for synchronizing device data including contacts, call logs, messages, WhatsApp messages, notifications, and email accounts. The system consists of a Node.js backend with MongoDB, a Next.js frontend dashboard, and a Flutter mobile application.

## Project Structure

```
├── Backend/          # Node.js + MongoDB API server
├── frontend/         # Next.js admin dashboard
└── App/             # Flutter mobile application
```

## Features

- **Device Registration & Management**: Automatic device registration and status tracking
- **Real-time Data Sync**: Synchronizes contacts, call logs, SMS, WhatsApp messages, notifications
- **Admin Dashboard**: Web-based interface to monitor and manage devices
- **Background Processing**: Automatic background sync with configurable frequencies
- **Settings Management**: Remote configuration of sync frequencies and enabled features
- **Security**: Encrypted data transmission and secure API endpoints

## System Architecture

### Backend (Node.js + MongoDB)
- RESTful API for device management and data synchronization
- MongoDB for scalable data storage
- Real-time settings updates
- Comprehensive logging and error handling

### Frontend (Next.js + Tailwind CSS)
- Modern admin dashboard
- Real-time device monitoring
- Settings management interface
- Data visualization and statistics

### Mobile App (Flutter)
- Cross-platform mobile application
- Background data harvesting
- Permission management
- Real-time sync status

## Quick Start

### Prerequisites
- Node.js 16+ and npm
- MongoDB 4.4+
- Flutter 3.0+
- Android Studio (for Android development)

### Backend Setup

1. Navigate to the Backend directory:
```bash
cd Backend
```

2. Install dependencies:
```bash
npm install
```

3. Configure environment variables:
```bash
cp .env.example .env
# Edit .env with your MongoDB URI and other settings
```

4. Start the server:
```bash
npm run dev
```

The backend will be available at `http://localhost:3000`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Configure environment variables:
```bash
echo "NEXT_PUBLIC_API_URL=http://localhost:3000" > .env.local
```

4. Start the development server:
```bash
npm run dev
```

The frontend will be available at `http://localhost:3001`

### Mobile App Setup

1. Navigate to the App directory:
```bash
cd App
```

2. Install Flutter dependencies:
```bash
flutter pub get
```

3. Configure API endpoint in `lib/utils/constants.dart`:
```dart
static const String baseUrl = 'http://10.0.2.2:3000/api'; // For Android emulator
```

4. Run the app:
```bash
flutter run
```

## API Endpoints

### Device Management
- `POST /api/devices/register` - Register a new device
- `GET /api/devices/:deviceId/settings` - Get device settings
- `PUT /api/devices/:deviceId/settings` - Update device settings
- `GET /api/devices` - Get all devices (admin)

### Data Synchronization
- `POST /api/contacts/sync` - Sync contacts
- `POST /api/call-logs/sync` - Sync call logs
- `POST /api/notifications/sync` - Sync notifications
- `POST /api/messages/sync` - Sync messages (SMS, WhatsApp)
- `POST /api/email-accounts/sync` - Sync email accounts

### Data Retrieval
- `GET /api/contacts/:deviceId` - Get device contacts
- `GET /api/call-logs/:deviceId` - Get device call logs
- `GET /api/notifications/:deviceId` - Get device notifications
- `GET /api/messages/:deviceId` - Get device messages

## Configuration

### Default Sync Frequencies
- **Contacts**: Every 24 hours (1440 minutes)
- **Call Logs**: Every 24 hours (1440 minutes)
- **Email Accounts**: Every 24 hours (1440 minutes)
- **Notifications**: Every 30 seconds
- **Messages**: Every 60 seconds
- **WhatsApp**: Every 60 seconds
- **Settings Update**: Every 2 minutes

### Environment Variables

#### Backend (.env)
```env
PORT=3000
MONGODB_URI=mongodb://localhost:27017/device_sync
JWT_SECRET=your_super_secret_jwt_key_here_change_in_production
NODE_ENV=development
```

#### Frontend (.env.local)
```env
NEXT_PUBLIC_API_URL=http://localhost:3000
```

## Device Workflow

### 1. Device Registration
- On app startup, device checks if ID exists in local storage
- If not exists: generates unique device ID and registers with server
- If exists: updates device status and fetches current settings

### 2. Settings Management
- Device fetches settings from server every 2 minutes (configurable)
- Settings include sync frequencies and enable/disable flags for each data type
- Admin can update settings through web dashboard

### 3. Data Synchronization
- Background workers sync data based on configured frequencies
- Only new/updated data is synced (incremental sync)
- Sync timestamps are maintained for each data type

### 4. Monitoring
- Admin dashboard shows real-time device status
- Statistics for each data type
- Last sync times and connection status

## Permissions Required (Android)

The mobile app requires the following permissions:

- **Contacts**: `READ_CONTACTS`, `WRITE_CONTACTS`
- **Phone**: `READ_PHONE_STATE`, `READ_CALL_LOG`
- **SMS**: `READ_SMS`, `RECEIVE_SMS`
- **Storage**: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
- **Notifications**: `BIND_NOTIFICATION_LISTENER_SERVICE`
- **Background**: `WAKE_LOCK`, `FOREGROUND_SERVICE`
- **Accounts**: `GET_ACCOUNTS`, `READ_PROFILE`

## WhatsApp Message Extraction

WhatsApp message extraction requires special considerations:

### Method 1: Root Access (Most Complete)
- Requires rooted Android device
- Direct access to WhatsApp database files
- Can extract complete message history
- **Security Risk**: Root access compromises device security

### Method 2: Backup Extraction
- Uses WhatsApp backup files
- Limited to backup frequency
- Requires storage permissions
- May not include real-time messages

### Method 3: Accessibility Service (Limited)
- Uses Android Accessibility API
- Can capture visible messages
- May violate app store policies
- Limited effectiveness

**Note**: WhatsApp message extraction may violate terms of service and privacy laws. Ensure compliance with local regulations and user consent.

## Security Considerations

1. **Data Encryption**: All API communications use HTTPS
2. **Access Control**: Implement proper authentication for admin dashboard
3. **Data Privacy**: Ensure compliance with GDPR/CCPA requirements
4. **Permission Management**: Request minimal necessary permissions
5. **Secure Storage**: Encrypt sensitive data in local storage

## Production Deployment

### Backend Deployment
1. Use environment variables for configuration
2. Set up MongoDB with proper authentication
3. Enable HTTPS with SSL certificates
4. Implement rate limiting and request validation
5. Set up monitoring and logging

### Frontend Deployment
1. Build optimized production bundle: `npm run build`
2. Deploy to hosting service (Vercel, Netlify, etc.)
3. Configure API URL for production

### Mobile App Deployment
1. Update API endpoints for production
2. Sign APK for release
3. Submit to Google Play Store (ensure policy compliance)

## Troubleshooting

### Common Issues

1. **Permission Denied Errors**
   - Ensure all required permissions are granted
   - Check Android manifest file
   - Request permissions at runtime

2. **WhatsApp Access Issues**
   - Verify device has root access
   - Check WhatsApp database file paths
   - Ensure app has file system permissions

3. **API Connection Issues**
   - Verify backend server is running
   - Check network connectivity
   - Ensure correct API endpoint URLs

4. **Background Sync Not Working**
   - Disable battery optimization for the app
   - Check background app restrictions
   - Verify WorkManager configuration

### Debugging

1. **Backend Logs**
   ```bash
   # View server logs
   npm run dev
   
   # Check MongoDB logs
   sudo tail -f /var/log/mongodb/mongod.log
   ```

2. **Flutter Logs**
   ```bash
   # View app logs
   flutter logs
   
   # Debug on connected device
   flutter run --debug
   ```

3. **Database Inspection**
   ```bash
   # Connect to MongoDB
   mongo device_sync
   
   # View collections
   show collections
   
   # Query devices
   db.devices.find().pretty()
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This software is intended for legitimate device management and data synchronization purposes. Users are responsible for:

1. Obtaining proper consent from device owners
2. Complying with local privacy laws and regulations
3. Ensuring appropriate use of extracted data
4. Maintaining data security and confidentiality

The developers are not responsible for misuse of this software or any legal consequences arising from its use.

## Support

For support and questions:
1. Check the troubleshooting section
2. Review the API documentation
3. Create an issue on GitHub
4. Contact the development team

---

**Note**: This system handles sensitive personal data. Always ensure compliance with applicable privacy laws and obtain proper user consent before deployment.
