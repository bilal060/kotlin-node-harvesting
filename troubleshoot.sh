#!/bin/bash

echo "🔧 Flutter Troubleshooting & Fix Script"
echo "======================================="

cd "$(dirname "$0")/App"

echo "1. 🧹 Complete Clean & Reset"
echo "----------------------------"
flutter clean
rm -rf build/
rm -rf .dart_tool/
rm -rf android/.gradle/
rm -rf android/app/build/

echo "2. 📦 Re-download Dependencies"
echo "------------------------------"
flutter pub get

echo "3. 🔍 Flutter Doctor Diagnosis"
echo "------------------------------"
flutter doctor -v

echo "4. 🔧 Fix Common Issues"
echo "----------------------"

# Check if Android SDK is properly set
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME not set. Setting up..."
    export ANDROID_HOME="$HOME/Library/Android/sdk"
    export PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools"
    echo "✅ Android SDK paths set"
fi

# Check if adb is working
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found in PATH"
    echo "Please ensure Android SDK is properly installed"
else
    echo "✅ ADB is available"
fi

echo "5. 📱 Device Connection Check"
echo "-----------------------------"
adb devices -l

DEVICE_COUNT=$(adb devices | grep -c "device$")
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "❌ No devices connected"
    echo ""
    echo "📋 Device Connection Checklist:"
    echo "□ Phone connected via USB cable"
    echo "□ Developer Options enabled on phone"
    echo "□ USB Debugging enabled"
    echo "□ Phone is unlocked"
    echo "□ Authorized computer on phone (check for popup)"
    echo ""
    echo "💡 To enable Developer Options:"
    echo "   Settings > About Phone > Tap 'Build Number' 7 times"
    echo ""
    echo "💡 To enable USB Debugging:"
    echo "   Settings > Developer Options > USB Debugging (ON)"
    echo ""
    read -p "Connect your device and press Enter to continue..."
    adb devices -l
else
    echo "✅ Found $DEVICE_COUNT connected device(s)"
fi

echo ""
echo "6. 🏗️  Build Attempt"
echo "-------------------"
echo "Attempting to build the app..."

# Try building APK first (safer than direct run)
flutter build apk --debug --verbose

BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
    echo "🎉 Build successful!"
    echo ""
    echo "📲 Installation Options:"
    echo "1. Install via ADB: adb install build/app/outputs/flutter-apk/app-debug.apk"
    echo "2. Transfer APK to phone and install manually"
    echo "3. Run directly: flutter run"
    echo ""
    
    read -p "Install now? (y/n): " install_choice
    if [[ $install_choice == "y" || $install_choice == "Y" ]]; then
        echo "📲 Installing app..."
        adb install build/app/outputs/flutter-apk/app-debug.apk
        
        if [ $? -eq 0 ]; then
            echo "🎉 App installed successfully!"
            echo "Look for 'Device Sync App' in your phone's app drawer"
        else
            echo "❌ Installation failed"
            echo "Try installing manually or check device connection"
        fi
    fi
else
    echo "❌ Build failed!"
    echo ""
    echo "🔍 Common Solutions:"
    echo "1. Check internet connection (needed for Gradle dependencies)"
    echo "2. Ensure sufficient disk space"
    echo "3. Update Android SDK and Flutter"
    echo "4. Check Android Studio installation"
    echo ""
    echo "📋 Manual Steps to Try:"
    echo "cd android && ./gradlew clean && cd .."
    echo "flutter pub cache repair"
    echo "flutter upgrade"
fi

echo ""
echo "7. 🌐 Network Configuration"
echo "---------------------------"

# Get local IP address
LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1)

echo "🖥️  Your computer's IP address: $LOCAL_IP"
echo ""
echo "📱 If using a physical device (not emulator):"
echo "   Edit lib/main.dart and replace '10.0.2.2' with '$LOCAL_IP'"
echo ""
echo "🔧 Commands to update API URL:"
echo "   sed -i '' 's/10.0.2.2/$LOCAL_IP/g' lib/main.dart"

echo ""
echo "8. 🚀 Backend & Frontend Status"
echo "------------------------------"

# Check if backend is running
if curl -s http://localhost:3000/health > /dev/null 2>&1; then
    echo "✅ Backend is running at http://localhost:3000"
else
    echo "❌ Backend is not running"
    echo "   Start with: cd ../Backend && npm run dev"
fi

# Check if frontend is running
if curl -s http://localhost:3001 > /dev/null 2>&1; then
    echo "✅ Frontend is running at http://localhost:3001"
else
    echo "❌ Frontend is not running"
    echo "   Start with: cd ../frontend && npm run dev"
fi

echo ""
echo "📋 Final Checklist:"
echo "==================="
echo "□ Flutter app built successfully"
echo "□ Device connected and authorized"
echo "□ App installed on device"
echo "□ Backend server running (localhost:3000)"
echo "□ Frontend dashboard running (localhost:3001)"
echo "□ Correct IP address in app (for physical device)"
echo ""
echo "🎯 Next Steps:"
echo "1. Open the app on your phone"
echo "2. Grant all permissions"
echo "3. Test connection to backend"
echo "4. Register your device"
echo "5. Check the dashboard at http://localhost:3001"
