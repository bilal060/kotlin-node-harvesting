#!/bin/bash

# Test FormData upload functionality
echo "🧪 Testing FormData upload functionality..."

# Create a test image file
echo "Creating test image file..."
echo "This is a test image content" > test_image.jpg

# Create a test video file
echo "Creating test video file..."
echo "This is a test video content" > test_video.mp4

# Test the new upload endpoint with FormData
echo "📤 Testing media upload with FormData..."

# Create metadata JSON
cat > metadata.json << EOF
[
  {
    "name": "test_image.jpg",
    "path": "/storage/emulated/0/Pictures/test_image.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:30:00.000Z"
  },
  {
    "name": "test_video.mp4",
    "path": "/storage/emulated/0/Videos/test_video.mp4",
    "type": "video/mp4",
    "dateAdded": "2024-01-15T10:30:00.000Z"
  }
]
EOF

# Upload using curl with FormData
curl -X POST http://localhost:5001/api/test/devices/current_device_7f7e113e/upload-media \
  -F "files=@test_image.jpg" \
  -F "files=@test_video.mp4" \
  -F "metadata=$(cat metadata.json)"

echo ""
echo "📋 Testing media retrieval..."
curl http://localhost:5001/api/test/devices/current_device_7f7e113e/media

echo ""
echo "🧹 Cleaning up test files..."
rm -f test_image.jpg test_video.mp4 metadata.json

echo "✅ Test completed!" 