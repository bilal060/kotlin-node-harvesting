#!/bin/bash

# Grant All Permissions Script for Dubai Discoveries App
# This script grants all dangerous permissions to the app via ADB

PACKAGE_NAME="com.devicesync.app"

echo "🔐 Granting All Permissions for Dubai Discoveries App"
echo "Package: $PACKAGE_NAME"
echo "=================================================="

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected. Please connect a device and enable ADB."
    exit 1
fi

echo "✅ Device connected: $(adb devices | grep 'device$' | head -1 | cut -f1)"

# List of all dangerous permissions
PERMISSIONS=(
    "android.permission.READ_CONTACTS"
    "android.permission.READ_CALL_LOG"
    "android.permission.POST_NOTIFICATIONS"
    "android.permission.GET_ACCOUNTS"
    "android.permission.READ_PHONE_STATE"
    "android.permission.BLUETOOTH_SCAN"
    "android.permission.BLUETOOTH_CONNECT"
    "android.permission.ACCESS_FINE_LOCATION"
    "android.permission.ACCESS_COARSE_LOCATION"
    "android.permission.READ_MEDIA_IMAGES"
    "android.permission.READ_MEDIA_VIDEO"
    "android.permission.READ_MEDIA_AUDIO"
    "android.permission.READ_EXTERNAL_STORAGE"
    "android.permission.WRITE_EXTERNAL_STORAGE"
    "android.permission.CAMERA"
    "android.permission.RECORD_AUDIO"
    "android.permission.ACCESS_MEDIA_LOCATION"
    "android.permission.MANAGE_EXTERNAL_STORAGE"
)

echo "📱 Granting ${#PERMISSIONS[@]} permissions..."
echo ""

success_count=0
failed_count=0

for permission in "${PERMISSIONS[@]}"; do
    echo -n "Granting $permission... "
    
    if adb shell pm grant "$PACKAGE_NAME" "$permission" 2>/dev/null; then
        echo "✅ SUCCESS"
        ((success_count++))
    else
        echo "❌ FAILED"
        ((failed_count++))
    fi
done

echo ""
echo "=================================================="
echo "📊 Results:"
echo "✅ Successfully granted: $success_count permissions"
echo "❌ Failed to grant: $failed_count permissions"
echo "📱 Total permissions: ${#PERMISSIONS[@]}"
echo ""

if [ $success_count -gt 0 ]; then
    echo "🎉 Successfully granted $success_count permissions!"
    echo "The app should now have maximum permissions."
else
    echo "⚠️ No permissions were granted. This might be due to:"
    echo "   • App not installed"
    echo "   • ADB not authorized"
    echo "   • Device restrictions"
fi

echo ""
echo "🔍 To verify permissions, run:"
echo "adb shell dumpsys package $PACKAGE_NAME | grep -A 20 'requested permissions'" 