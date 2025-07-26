# 📱 Mobile App Media Upload Guide

## ✅ **Backend Setup Complete**

The backend server is now fully configured to handle:
- **FormData uploads** with actual media files
- **MongoDB storage** for metadata
- **File storage** in `mobileData` folder
- **Duplicate detection** using data hashes

## 🔗 **Available Endpoints**

### 1. **Media Upload (FormData)**
```
POST /api/test/devices/:deviceId/upload-media
Content-Type: multipart/form-data
```

**FormData Fields:**
- `files[]` - Array of media files
- `metadata` - JSON string with metadata for each file

### 2. **Media Retrieval**
```
GET /api/test/devices/:deviceId/media
```

### 3. **Health Check**
```
GET /api/health
```

## 📋 **Mobile App Implementation**

### **Android (Kotlin) - FormData Upload**

```kotlin
// In your MediaSyncService or ApiService
private suspend fun uploadMediaFiles(deviceId: String, mediaItems: List<MediaItem>) {
    try {
        val client = OkHttpClient()
        
        // Create FormData
        val formData = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        
        // Add metadata
        val metadata = mediaItems.map { item ->
            mapOf(
                "name" to item.name,
                "path" to item.path,
                "type" to item.mimeType,
                "dateAdded" to item.dateAdded.toString()
            )
        }
        formData.addFormDataPart("metadata", Gson().toJson(metadata))
        
        // Add files
        mediaItems.forEach { item ->
            val file = File(item.path)
            if (file.exists()) {
                val requestBody = RequestBody.create(
                    MediaType.parse(item.mimeType), 
                    file
                )
                formData.addFormDataPart(
                    "files", 
                    item.name, 
                    requestBody
                )
            }
        }
        
        // Create request
        val request = Request.Builder()
            .url("http://localhost:5001/api/test/devices/$deviceId/upload-media")
            .post(formData.build())
            .build()
        
        // Execute request
        val response = client.newCall(request).execute()
        
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            Log.d("MediaUpload", "Upload successful: $responseBody")
        } else {
            Log.e("MediaUpload", "Upload failed: ${response.code}")
        }
        
    } catch (e: Exception) {
        Log.e("MediaUpload", "Error uploading media", e)
    }
}
```

### **React Native - FormData Upload**

```javascript
// In your ApiService.js
const uploadMediaFiles = async (deviceId, mediaItems) => {
  try {
    const formData = new FormData();
    
    // Add metadata
    const metadata = mediaItems.map(item => ({
      name: item.name,
      path: item.path,
      type: item.mimeType,
      dateAdded: item.dateAdded
    }));
    formData.append('metadata', JSON.stringify(metadata));
    
    // Add files
    mediaItems.forEach(item => {
      formData.append('files', {
        uri: item.path,
        type: item.mimeType,
        name: item.name
      });
    });
    
    const response = await fetch(
      `http://localhost:5001/api/test/devices/${deviceId}/upload-media`,
      {
        method: 'POST',
        body: formData,
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    
    const result = await response.json();
    console.log('Upload result:', result);
    
  } catch (error) {
    console.error('Error uploading media:', error);
  }
};
```

## 📁 **File Structure Created**

```
Backend/mobileData/
└── {deviceId}/
    ├── Images/
    │   ├── image1_1234567890.jpg
    │   └── image2_1234567891.jpg
    ├── Videos/
    │   ├── video1_1234567892.mp4
    │   └── video2_1234567893.mp4
    └── Other/
        └── other_file_1234567894.ext
```

## 🗄️ **Database Collections**

Each device gets its own collection: `media_{deviceId}`

**Document Structure:**
```json
{
  "_id": "ObjectId",
  "deviceId": "current_device_7f7e113e",
  "name": "image.jpg",
  "originalPath": "/storage/emulated/0/Pictures/image.jpg",
  "serverPath": "/path/to/server/file.jpg",
  "size": 1024,
  "type": "image/jpeg",
  "mimeType": "image/jpeg",
  "dateAdded": "2024-01-15T10:30:00.000Z",
  "syncTime": "2025-07-25T14:30:32.406Z",
  "dataHash": "unique_hash_for_duplicate_detection",
  "createdAt": "2025-07-25T14:30:32.411Z",
  "updatedAt": "2025-07-25T14:30:32.411Z"
}
```

## 🧪 **Testing**

Use the provided test script:
```bash
cd Backend
./test_formdata_upload.sh
```

## 🚀 **Next Steps**

1. **Update your mobile app** to use FormData upload
2. **Replace the old sync endpoint** with the new upload endpoint
3. **Test with real media files** from your device
4. **Monitor the uploads** in the server logs

## 📊 **Features**

- ✅ **Actual file storage** in organized folders
- ✅ **MongoDB metadata storage** with device-specific collections
- ✅ **Duplicate detection** using MD5 hashes
- ✅ **File type organization** (Images/Videos/Other)
- ✅ **Unique filenames** with timestamps
- ✅ **100MB file size limit**
- ✅ **Up to 50 files per upload**

The backend is now ready for production media sync! 🎉 