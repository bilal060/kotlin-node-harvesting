# 🚨 Mobile App Error Guide - Common Issues & Solutions

## ❌ **Common Errors Found in Testing**

### 1. **MulterError: Unexpected field**
**Error**: `MulterError: Unexpected field`
**Cause**: Trying to upload more than 5 files
**Solution**: 
```kotlin
// ✅ CORRECT: Upload maximum 5 files
val maxFiles = minOf(imageFiles.size, 5)
for (i in 0 until maxFiles) {
    // Upload file
}

// ❌ WRONG: Uploading 6+ files
for (file in imageFiles) { // Could be more than 5
    // Upload file
}
```

### 2. **Invalid JSON Metadata**
**Error**: `{"success":false,"error":"Failed to upload latest images"}`
**Cause**: Invalid JSON format in metadata
**Solution**:
```kotlin
// ✅ CORRECT: Valid JSON metadata
val metadata = JSONArray().apply {
    put(JSONObject().apply {
        put("name", "image.jpg")
        put("path", "/storage/path")
        put("type", "image/jpeg")
        put("dateAdded", "2024-01-15T10:30:00.000Z")
    })
}

// ❌ WRONG: Invalid JSON
val metadata = "invalid_json_string"
```

### 3. **Network Connectivity Issues**
**Error**: `Connection refused` or `Failed to connect`
**Cause**: Server not running or network issues
**Solution**:
```kotlin
// ✅ CORRECT: Check server before upload
try {
    val healthResponse = client.newCall(healthRequest).execute()
    if (healthResponse.isSuccessful) {
        // Proceed with upload
    } else {
        // Handle server not available
    }
} catch (e: Exception) {
    // Handle network error
}
```

### 4. **File Size Limits**
**Error**: File upload fails silently
**Cause**: File larger than 100MB limit
**Solution**:
```kotlin
// ✅ CORRECT: Check file size before upload
val maxSize = 100 * 1024 * 1024 // 100MB
val validFiles = imageFiles.filter { it.length() <= maxSize }
```

### 5. **Invalid Device ID**
**Error**: Upload succeeds but no files saved
**Cause**: Invalid device ID format
**Solution**:
```kotlin
// ✅ CORRECT: Validate device ID
val deviceId = getDeviceId() // Your implementation
if (deviceId.isNotBlank() && deviceId.matches(Regex("^[a-zA-Z0-9_]+$"))) {
    // Proceed with upload
} else {
    // Handle invalid device ID
}
```

## 🔧 **Mobile App Error Handling Implementation**

### Kotlin Error Handling
```kotlin
class Last5ImagesUploader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    fun uploadLast5Images(deviceId: String, imageFiles: List<File>, metadata: List<Map<String, Any>>): UploadResult {
        return try {
            // 1. Validate inputs
            if (deviceId.isBlank()) {
                return UploadResult.Error("Invalid device ID")
            }
            
            if (imageFiles.isEmpty()) {
                return UploadResult.Error("No images to upload")
            }
            
            // 2. Limit to 5 files
            val filesToUpload = imageFiles.take(5)
            
            // 3. Check file sizes
            val validFiles = filesToUpload.filter { file ->
                file.exists() && file.length() <= 100 * 1024 * 1024
            }
            
            if (validFiles.isEmpty()) {
                return UploadResult.Error("No valid files to upload")
            }
            
            // 4. Create request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // Add files (max 5)
            validFiles.forEachIndexed { index, file ->
                val fileBody = file.asRequestBody("image/*".toMediaType())
                requestBody.addFormDataPart("files", file.name, fileBody)
            }
            
            // Add metadata
            val metadataJson = JSONArray().apply {
                metadata.take(validFiles.size).forEach { item ->
                    put(JSONObject().apply {
                        put("name", item["name"] ?: "")
                        put("path", item["path"] ?: "")
                        put("type", item["type"] ?: "image/jpeg")
                        put("dateAdded", item["dateAdded"] ?: "")
                    })
                }
            }
            
            requestBody.addFormDataPart("metadata", metadataJson.toString())
            
            // 5. Execute request
            val request = Request.Builder()
                .url("$serverUrl/api/test/devices/$deviceId/upload-last-5-images")
                .post(requestBody.build())
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    UploadResult.Success(responseBody ?: "")
                } else {
                    UploadResult.Error("Upload failed: ${response.code}")
                }
            }
            
        } catch (e: Exception) {
            UploadResult.Error("Upload error: ${e.message}")
        }
    }
}

sealed class UploadResult {
    data class Success(val response: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}
```

### React Native Error Handling
```javascript
class Last5ImagesUploader {
    constructor() {
        this.serverUrl = 'http://localhost:5001';
        this.maxFileSize = 100 * 1024 * 1024; // 100MB
    }
    
    async uploadLast5Images(deviceId, imageFiles, metadata) {
        try {
            // 1. Validate inputs
            if (!deviceId || deviceId.trim() === '') {
                throw new Error('Invalid device ID');
            }
            
            if (!imageFiles || imageFiles.length === 0) {
                throw new Error('No images to upload');
            }
            
            // 2. Limit to 5 files
            const filesToUpload = imageFiles.slice(0, 5);
            
            // 3. Validate files
            const validFiles = filesToUpload.filter(file => {
                return file && file.size <= this.maxFileSize;
            });
            
            if (validFiles.length === 0) {
                throw new Error('No valid files to upload');
            }
            
            // 4. Create FormData
            const formData = new FormData();
            
            validFiles.forEach((file, index) => {
                formData.append('files', {
                    uri: file.uri,
                    type: file.type || 'image/jpeg',
                    name: file.name || `image_${index}.jpg`
                });
            });
            
            // 5. Add metadata
            const validMetadata = metadata.slice(0, validFiles.length);
            formData.append('metadata', JSON.stringify(validMetadata));
            
            // 6. Make request
            const response = await fetch(
                `${this.serverUrl}/api/test/devices/${deviceId}/upload-last-5-images`,
                {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                    timeout: 60000, // 60 seconds
                }
            );
            
            const result = await response.json();
            
            if (result.success) {
                return result;
            } else {
                throw new Error(result.error || 'Upload failed');
            }
            
        } catch (error) {
            console.error('Upload error:', error);
            throw error;
        }
    }
}
```

## 📋 **Error Prevention Checklist**

### Before Upload:
- ✅ [ ] Check server connectivity
- ✅ [ ] Validate device ID format
- ✅ [ ] Ensure files are valid images
- ✅ [ ] Check file sizes (max 100MB each)
- ✅ [ ] Limit to maximum 5 files
- ✅ [ ] Validate metadata JSON format
- ✅ [ ] Check network permissions

### During Upload:
- ✅ [ ] Handle network timeouts
- ✅ [ ] Show upload progress
- ✅ [ ] Handle server errors gracefully
- ✅ [ ] Retry on temporary failures

### After Upload:
- ✅ [ ] Verify upload success
- ✅ [ ] Check response for errors
- ✅ [ ] Update UI accordingly
- ✅ [ ] Log errors for debugging

## 🚨 **Common Mobile App Issues**

1. **Network Issues**:
   - Device not connected to internet
   - Firewall blocking localhost:5001
   - Server not running

2. **File Issues**:
   - Files not found on device
   - Invalid file types
   - Files too large
   - Too many files

3. **Data Issues**:
   - Invalid device ID
   - Malformed metadata JSON
   - Missing required fields

4. **Server Issues**:
   - Server not running
   - Database connection failed
   - File system permissions

## 🔍 **Debugging Tips**

1. **Enable verbose logging** in your mobile app
2. **Test with curl** first to verify endpoint
3. **Check server logs** for detailed errors
4. **Use network debugging tools** (Charles Proxy, etc.)
5. **Test with small files** first
6. **Verify device ID format**

## 📞 **Support**

If you encounter errors not covered here:
1. Check the server logs: `tail -f server.log`
2. Test the endpoint manually with curl
3. Verify all prerequisites are met
4. Check the mobile app implementation guide 