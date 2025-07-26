# 🎯 TOP-TIER: Mobile App Integration Guide - Last 5 Images

## Overview
This guide provides the complete implementation for uploading the last 5 images from mobile devices to the server using the new top-tier endpoint.

## 🚀 Backend Endpoint
- **URL**: `POST /api/test/devices/:deviceId/upload-last-5-images`
- **Method**: POST with FormData
- **Max Files**: 5 images
- **Response**: JSON with upload status and file details

## 📱 Mobile App Implementation

### Kotlin (Android) Implementation

```kotlin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class Last5ImagesUploader {
    private val client = OkHttpClient()
    private val serverUrl = "http://localhost:5001"
    
    fun uploadLast5Images(deviceId: String, imageFiles: List<File>, metadata: List<Map<String, Any>>) {
        try {
            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // Add files
            imageFiles.forEachIndexed { index, file ->
                val fileBody = file.asRequestBody("image/*".toMediaType())
                requestBody.addFormDataPart("files", file.name, fileBody)
            }
            
            // Add metadata JSON
            val metadataJson = JSONArray().apply {
                metadata.forEach { item ->
                    put(JSONObject().apply {
                        put("name", item["name"] ?: "")
                        put("path", item["path"] ?: "")
                        put("type", item["type"] ?: "image/jpeg")
                        put("dateAdded", item["dateAdded"] ?: "")
                    })
                }
            }
            
            requestBody.addFormDataPart("metadata", metadataJson.toString())
            
            // Create request
            val request = Request.Builder()
                .url("$serverUrl/api/test/devices/$deviceId/upload-last-5-images")
                .post(requestBody.build())
                .build()
            
            // Execute request
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("🎯 TOP-TIER: Successfully uploaded last 5 images")
                    println("Response: $responseBody")
                } else {
                    println("❌ Upload failed: ${response.code}")
                }
            }
            
        } catch (e: Exception) {
            println("❌ Error uploading images: ${e.message}")
        }
    }
}

// Usage Example
fun uploadLast5ImagesFromDevice() {
    val uploader = Last5ImagesUploader()
    val deviceId = "current_device_7f7e113e"
    
    // Get last 5 images from device storage
    val imageFiles = getLast5ImagesFromDevice() // Your implementation
    val metadata = getImageMetadata(imageFiles) // Your implementation
    
    uploader.uploadLast5Images(deviceId, imageFiles, metadata)
}

// Helper function to get last 5 images from device
fun getLast5ImagesFromDevice(): List<File> {
    // Implementation to get last 5 images from device storage
    // This would typically use MediaStore API or similar
    return listOf() // Placeholder
}
```

### React Native Implementation

```javascript
import { Platform } from 'react-native';

class Last5ImagesUploader {
    constructor() {
        this.serverUrl = 'http://localhost:5001';
    }
    
    async uploadLast5Images(deviceId, imageFiles, metadata) {
        try {
            // Create FormData
            const formData = new FormData();
            
            // Add files
            imageFiles.forEach((file, index) => {
                formData.append('files', {
                    uri: file.uri,
                    type: file.type || 'image/jpeg',
                    name: file.name || `image_${index}.jpg`
                });
            });
            
            // Add metadata
            formData.append('metadata', JSON.stringify(metadata));
            
            // Make request
            const response = await fetch(
                `${this.serverUrl}/api/test/devices/${deviceId}/upload-last-5-images`,
                {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                }
            );
            
            const result = await response.json();
            
            if (result.success) {
                console.log('🎯 TOP-TIER: Successfully uploaded last 5 images');
                console.log('Response:', result);
                return result;
            } else {
                console.error('❌ Upload failed:', result.error);
                throw new Error(result.error);
            }
            
        } catch (error) {
            console.error('❌ Error uploading images:', error);
            throw error;
        }
    }
}

// Usage Example
const uploadLast5ImagesFromDevice = async () => {
    const uploader = new Last5ImagesUploader();
    const deviceId = 'current_device_7f7e113e';
    
    // Get last 5 images from device
    const imageFiles = await getLast5ImagesFromDevice(); // Your implementation
    const metadata = getImageMetadata(imageFiles); // Your implementation
    
    try {
        const result = await uploader.uploadLast5Images(deviceId, imageFiles, metadata);
        console.log('Upload successful:', result);
    } catch (error) {
        console.error('Upload failed:', error);
    }
};

// Helper function to get last 5 images from device
const getLast5ImagesFromDevice = async () => {
    // Implementation to get last 5 images from device storage
    // This would typically use react-native-image-picker or similar
    return []; // Placeholder
};
```

## 🔍 Testing the Integration

### 1. Test Upload Endpoint
```bash
# Test with curl
curl -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg" \
  -F "files=@image3.jpg" \
  -F "files=@image4.jpg" \
  -F "files=@image5.jpg" \
  -F "metadata=[{\"name\":\"image1.jpg\",\"path\":\"/storage/emulated/0/Pictures/image1.jpg\",\"type\":\"image/jpeg\",\"dateAdded\":\"2024-01-15T10:30:00.000Z\"}]"
```

### 2. Test Retrieval Endpoint
```bash
# Get latest images
curl http://localhost:5001/api/test/devices/test_device/latest-images
```

### 3. Check File Storage
```bash
# Check if files are saved
ls -la mobileData/test_device/Images/
```

## 📊 Expected Response Format

### Upload Response
```json
{
  "success": true,
  "data": {
    "success": true,
    "itemsSynced": 5,
    "message": "🎯 TOP-TIER: 5 latest images uploaded successfully for device test_device",
    "uploadedFiles": [
      {
        "name": "image1.jpg",
        "size": 1024,
        "path": "/path/to/saved/image1.jpg",
        "dateAdded": "2024-01-15T10:30:00.000Z"
      }
    ],
    "totalImagesInDevice": 10,
    "latestImagesCount": 5
  }
}
```

### Retrieval Response
```json
{
  "success": true,
  "data": {
    "deviceId": "test_device",
    "latestImagesCount": 5,
    "latestImages": [
      {
        "_id": "image_id",
        "deviceId": "test_device",
        "name": "image1.jpg",
        "originalPath": "/storage/emulated/0/Pictures/image1.jpg",
        "serverPath": "/path/to/saved/image1.jpg",
        "size": 1024,
        "type": "image",
        "mimeType": "image/jpeg",
        "dateAdded": "2024-01-15T10:30:00.000Z",
        "syncTime": "2024-01-15T10:30:00.000Z",
        "dataHash": "hash_value",
        "isLatestImage": true
      }
    ],
    "totalImagesInDevice": 10
  }
}
```

## 🎯 Key Features

1. **🎯 TOP-TIER Performance**: Optimized for exactly 5 images
2. **📸 Image Deduplication**: Prevents uploading same images twice
3. **🗄️ Database Storage**: Metadata stored in MongoDB
4. **📁 File Organization**: Images saved in organized folders
5. **🔄 Latest Tracking**: Tracks which images are the latest
6. **📊 Comprehensive Response**: Detailed upload statistics

## 🚀 Next Steps

1. Implement the mobile app code in your Android/Kotlin app
2. Test with real device images
3. Verify file storage and database entries
4. Monitor server logs for any issues

The backend is ready and tested! 🎉 