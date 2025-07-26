#!/bin/bash

echo "🧪 Testing File Storage System"
echo "=============================="

# Test device ID
DEVICE_ID="test_device_file_storage_$(date +%s)"

echo "📱 Using test device ID: $DEVICE_ID"
echo ""

# Test 1: Check if mobileData directory exists
echo "📁 Test 1: Directory Structure Check"
if [ -d "mobileData" ]; then
    echo "✅ mobileData directory exists"
else
    echo "❌ mobileData directory not found"
fi

# Create test device directory structure
mkdir -p "mobileData/$DEVICE_ID/Images"
mkdir -p "mobileData/$DEVICE_ID/Videos"

echo "✅ Created directory structure: mobileData/$DEVICE_ID/{Images,Videos}"
echo ""

# Test 2: File Upload Test
echo "📤 Test 2: File Upload Test"
echo "Creating test image file..."

# Create a test image file
TEST_IMAGE_PATH="mobileData/$DEVICE_ID/Images/test_image_$(date +%s).jpg"
echo "This is a test image file" > "$TEST_IMAGE_PATH"

# Create a test video file
TEST_VIDEO_PATH="mobileData/$DEVICE_ID/Videos/test_video_$(date +%s).mp4"
echo "This is a test video file" > "$TEST_VIDEO_PATH"

echo "✅ Created test files:"
echo "   - $TEST_IMAGE_PATH"
echo "   - $TEST_VIDEO_PATH"
echo ""

# Test 3: API Test - Get Media Files
echo "🌐 Test 3: API Test - Get Media Files"
echo "Testing GET /api/devices/$DEVICE_ID/media"

RESPONSE=$(curl -s "http://localhost:5001/api/devices/$DEVICE_ID/media")
echo "📥 Response: $RESPONSE"
echo ""

# Test 4: Directory Structure Verification
echo "📂 Test 4: Directory Structure Verification"
echo "Current structure:"
tree mobileData/ 2>/dev/null || find mobileData/ -type f | head -10
echo ""

# Test 5: File Upload via API
echo "📤 Test 5: File Upload via API"
echo "Testing POST /api/devices/$DEVICE_ID/upload-media"

# Create a test file for upload
UPLOAD_TEST_FILE="upload_test.txt"
echo "Test file content for upload" > "$UPLOAD_TEST_FILE"

# Test file upload
UPLOAD_RESPONSE=$(curl -s -X POST \
  -F "mediaFile=@$UPLOAD_TEST_FILE" \
  -F "mediaType=IMAGE" \
  -F "fileName=uploaded_test.txt" \
  "http://localhost:5001/api/devices/$DEVICE_ID/upload-media")

echo "📥 Upload Response: $UPLOAD_RESPONSE"
echo ""

# Clean up test file
rm -f "$UPLOAD_TEST_FILE"

# Test 6: Final Directory Check
echo "📂 Test 6: Final Directory Check"
echo "Files in mobileData/$DEVICE_ID/Images:"
ls -la "mobileData/$DEVICE_ID/Images/" 2>/dev/null || echo "No files found"

echo ""
echo "Files in mobileData/$DEVICE_ID/Videos:"
ls -la "mobileData/$DEVICE_ID/Videos/" 2>/dev/null || echo "No files found"

echo ""
echo "✅ File Storage System Test Complete!"
echo "=====================================" 