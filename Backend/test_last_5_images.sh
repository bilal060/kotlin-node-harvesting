#!/bin/bash

# 🎯 TOP-TIER: Test Last 5 Images Upload Functionality
echo "🎯 TOP-TIER: Testing Last 5 Images Upload Functionality..."

# Create 5 test image files
echo "Creating 5 test image files..."
for i in {1..5}; do
    echo "This is test image $i content" > "latest_image_$i.jpg"
done

# Test the new top-tier upload endpoint
echo "📤 Testing TOP-TIER last 5 images upload..."

# Create metadata JSON for the 5 images
cat > latest_images_metadata.json << EOF
[
  {
    "name": "latest_image_1.jpg",
    "path": "/storage/emulated/0/Pictures/latest_image_1.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:30:00.000Z"
  },
  {
    "name": "latest_image_2.jpg",
    "path": "/storage/emulated/0/Pictures/latest_image_2.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:31:00.000Z"
  },
  {
    "name": "latest_image_3.jpg",
    "path": "/storage/emulated/0/Pictures/latest_image_3.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:32:00.000Z"
  },
  {
    "name": "latest_image_4.jpg",
    "path": "/storage/emulated/0/Pictures/latest_image_4.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:33:00.000Z"
  },
  {
    "name": "latest_image_5.jpg",
    "path": "/storage/emulated/0/Pictures/latest_image_5.jpg",
    "type": "image/jpeg",
    "dateAdded": "2024-01-15T10:34:00.000Z"
  }
]
EOF

# Upload using curl with FormData
curl -X POST http://localhost:5001/api/test/devices/current_device_7f7e113e/upload-last-5-images \
  -F "files=@latest_image_1.jpg" \
  -F "files=@latest_image_2.jpg" \
  -F "files=@latest_image_3.jpg" \
  -F "files=@latest_image_4.jpg" \
  -F "files=@latest_image_5.jpg" \
  -F "metadata=$(cat latest_images_metadata.json)"

echo ""
echo "📋 Testing latest images retrieval..."
curl http://localhost:5001/api/test/devices/current_device_7f7e113e/latest-images

echo ""
echo "🧹 Cleaning up test files..."
rm -f latest_image_*.jpg latest_images_metadata.json

echo "🎯 TOP-TIER: Test completed!" 