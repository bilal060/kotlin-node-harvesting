#!/bin/bash

# 🎯 TOP-TIER: Test with Real Device Files
echo "🎯 TOP-TIER: Testing with Real Device Files..."

# Use real device ID and files
DEVICE_ID="test_device_file_storage_1753447187"
REAL_IMAGE="mobileData/${DEVICE_ID}/Images/test_image_1753447187.jpg"
REAL_VIDEO="mobileData/${DEVICE_ID}/Videos/test_video_1753447187.mp4"

echo "📱 Using device: $DEVICE_ID"
echo "📸 Real image file: $REAL_IMAGE"
echo "🎥 Real video file: $REAL_VIDEO"

# Create metadata for real files
cat > real_files_metadata.json << EOF
[
  {
    "name": "test_image_1753447187.jpg",
    "path": "/storage/emulated/0/Pictures/test_image_1753447187.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:30:00.000Z"
  },
  {
    "name": "test_video_1753447187.mp4",
    "path": "/storage/emulated/0/Videos/test_video_1753447187.mp4",
    "type": "video/mp4",
    "dateAdded": "2024-01-15T10:31:00.000Z"
  }
]
EOF

# Test the top-tier endpoint with real files
echo "📤 Testing TOP-TIER upload with real device files..."

curl -X POST http://localhost:5001/api/test/devices/${DEVICE_ID}/upload-last-5-images \
  -F "files=@${REAL_IMAGE}" \
  -F "files=@${REAL_VIDEO}" \
  -F "metadata=$(cat real_files_metadata.json)"

echo ""
echo "📋 Testing latest images retrieval for real device..."
curl http://localhost:5001/api/test/devices/${DEVICE_ID}/latest-images

echo ""
echo "📊 Checking file storage..."
ls -la mobileData/${DEVICE_ID}/Images/
ls -la mobileData/${DEVICE_ID}/Videos/

echo ""
echo "🧹 Cleaning up test metadata..."
rm -f real_files_metadata.json

echo "🎯 TOP-TIER: Real device test completed!" 