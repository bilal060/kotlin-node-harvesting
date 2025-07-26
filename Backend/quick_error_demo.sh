#!/bin/bash

# 🚨 Quick Error Demo for Mobile App Developers
echo "🚨 Quick Error Demo - Common Mobile App Issues"
echo "=============================================="

echo ""
echo "1️⃣ Testing: Too Many Files (6 files instead of 5)"
echo "Expected Error: MulterError: Unexpected field"
curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images \
  -F "files=@test_last_5_images.sh" \
  -F "files=@test_last_5_images.sh" \
  -F "files=@test_last_5_images.sh" \
  -F "files=@test_last_5_images.sh" \
  -F "files=@test_last_5_images.sh" \
  -F "files=@test_last_5_images.sh" \
  -F "metadata=[]" | head -3

echo ""
echo ""
echo "2️⃣ Testing: Invalid JSON Metadata"
echo "Expected Error: Failed to upload latest images"
curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images \
  -F "files=@test_last_5_images.sh" \
  -F "metadata=invalid_json" | head -3

echo ""
echo ""
echo "3️⃣ Testing: No Files Uploaded"
echo "Expected Result: 0 items synced (not an error, but no files processed)"
curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images \
  -F "metadata=[]" | head -3

echo ""
echo ""
echo "4️⃣ Testing: Valid Upload (for comparison)"
echo "Expected Result: Success with file uploaded"
curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images \
  -F "files=@test_last_5_images.sh" \
  -F "metadata=[{\"name\":\"test.sh\",\"path\":\"/test/path\",\"type\":\"text/plain\",\"dateAdded\":\"2024-01-15T10:30:00.000Z\"}]" | head -3

echo ""
echo ""
echo "🎯 Key Takeaways for Mobile App:"
echo "✅ Limit uploads to 5 files maximum"
echo "✅ Use valid JSON for metadata"
echo "✅ Handle empty file lists gracefully"
echo "✅ Check response status codes"
echo "✅ Implement proper error handling" 