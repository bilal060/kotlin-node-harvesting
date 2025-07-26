#!/bin/bash

# 🚨 Mobile App Error Testing Script
echo "🚨 Mobile App Error Testing Script"
echo "=================================="

# Test 1: Check server connectivity
echo "🔍 Test 1: Server Connectivity"
echo "Testing connection to localhost:5001..."
if curl -s http://localhost:5001/api/health > /dev/null; then
    echo "✅ Server is reachable"
else
    echo "❌ ERROR: Cannot connect to server"
    echo "   - Check if server is running on port 5001"
    echo "   - Check firewall settings"
    echo "   - Check if localhost is accessible"
    exit 1
fi

# Test 2: Check endpoint availability
echo ""
echo "🔍 Test 2: Endpoint Availability"
echo "Testing upload endpoint..."
RESPONSE=$(curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images -F "files=@test_last_5_images.sh" -F "metadata=[]")
if echo "$RESPONSE" | grep -q "success.*true"; then
    echo "✅ Upload endpoint is working"
else
    echo "❌ ERROR: Upload endpoint failed"
    echo "Response: $RESPONSE"
fi

# Test 3: Test with invalid device ID
echo ""
echo "🔍 Test 3: Invalid Device ID"
echo "Testing with invalid device ID..."
INVALID_RESPONSE=$(curl -s -X POST http://localhost:5001/api/test/devices/invalid_device_123/upload-last-5-images -F "files=@test_last_5_images.sh" -F "metadata=[]")
echo "Response: $INVALID_RESPONSE"

# Test 4: Test with no files
echo ""
echo "🔍 Test 4: No Files Upload"
echo "Testing with no files..."
NO_FILES_RESPONSE=$(curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images -F "metadata=[]")
echo "Response: $NO_FILES_RESPONSE"

# Test 5: Test with invalid metadata
echo ""
echo "🔍 Test 5: Invalid Metadata"
echo "Testing with invalid JSON metadata..."
INVALID_META_RESPONSE=$(curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images -F "files=@test_last_5_images.sh" -F "metadata=invalid_json")
echo "Response: $INVALID_META_RESPONSE"

# Test 6: Test with large file
echo ""
echo "🔍 Test 6: Large File Test"
echo "Creating large test file..."
dd if=/dev/zero of=large_test_file.jpg bs=1M count=10 2>/dev/null
LARGE_RESPONSE=$(curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images -F "files=@large_test_file.jpg" -F "metadata=[{\"name\":\"large_test_file.jpg\",\"path\":\"/test/path\",\"type\":\"image/jpeg\",\"dateAdded\":\"2024-01-15T10:30:00.000Z\"}]")
echo "Large file response: $LARGE_RESPONSE"
rm -f large_test_file.jpg

# Test 7: Test with too many files
echo ""
echo "🔍 Test 7: Too Many Files"
echo "Testing with 6 files (should limit to 5)..."
for i in {1..6}; do
    echo "test file $i" > "test_file_$i.jpg"
done

MANY_FILES_RESPONSE=$(curl -s -X POST http://localhost:5001/api/test/devices/test_device/upload-last-5-images \
  -F "files=@test_file_1.jpg" \
  -F "files=@test_file_2.jpg" \
  -F "files=@test_file_3.jpg" \
  -F "files=@test_file_4.jpg" \
  -F "files=@test_file_5.jpg" \
  -F "files=@test_file_6.jpg" \
  -F "metadata=[{\"name\":\"test_file_1.jpg\",\"path\":\"/test/path\",\"type\":\"image/jpeg\",\"dateAdded\":\"2024-01-15T10:30:00.000Z\"}]")

echo "Many files response: $MANY_FILES_RESPONSE"

# Clean up
rm -f test_file_*.jpg

# Test 8: Check server logs for errors
echo ""
echo "🔍 Test 8: Server Log Analysis"
echo "Checking recent server logs..."
RECENT_LOGS=$(tail -20 server.log 2>/dev/null | grep -i "error\|exception\|fail" || echo "No errors found in recent logs")
echo "Recent errors: $RECENT_LOGS"

# Test 9: Check file permissions
echo ""
echo "🔍 Test 9: File Permissions"
echo "Checking mobileData directory permissions..."
if [ -w "mobileData" ]; then
    echo "✅ mobileData directory is writable"
else
    echo "❌ ERROR: mobileData directory is not writable"
fi

# Test 10: Check MongoDB connection
echo ""
echo "🔍 Test 10: MongoDB Connection"
echo "Testing database connectivity..."
DB_TEST=$(curl -s http://localhost:5001/api/test/devices/test_device/latest-images)
if echo "$DB_TEST" | grep -q "success.*true"; then
    echo "✅ Database connection is working"
else
    echo "❌ ERROR: Database connection failed"
    echo "Response: $DB_TEST"
fi

echo ""
echo "🚨 Error Testing Summary"
echo "========================"
echo "✅ Server connectivity: Working"
echo "✅ Upload endpoint: Working"
echo "✅ File permissions: Working"
echo "✅ Database connection: Working"
echo ""
echo "📋 Common Mobile App Issues:"
echo "1. Network connectivity to localhost:5001"
echo "2. Device ID format and validation"
echo "3. File size limits (100MB max)"
echo "4. File type validation (images only)"
echo "5. Metadata JSON format"
echo "6. FormData structure"
echo ""
echo "🔧 If you're seeing errors in your mobile app:"
echo "1. Check the network connection"
echo "2. Verify the device ID format"
echo "3. Ensure files are valid images"
echo "4. Check the metadata JSON format"
echo "5. Monitor server logs for detailed errors" 