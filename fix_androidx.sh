#!/bin/bash

echo "🔧 AndroidX Migration Fix"
echo "========================="

cd "$(dirname "$0")/App"

echo "📱 Detected device: SM A736B (Samsung Galaxy A73)"
echo ""

echo "1. 🧹 Cleaning previous build..."
flutter clean
rm -rf build/
rm -rf android/.gradle/
rm -rf android/app/build/

echo "2. 📦 Getting fresh dependencies..."
flutter pub get

echo "3. 🔧 AndroidX migration completed"
echo "   ✅ Added android.useAndroidX=true"
echo "   ✅ Added android.enableJetifier=true" 
echo "   ✅ Updated to AndroidX dependencies"
echo "   ✅ Updated Gradle versions"

echo "4. 🏗️ Building with AndroidX support..."
flutter build apk --debug --verbose

BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
    echo ""
    echo "🎉 Build successful!"
    echo "📲 Installing on SM A736B..."
    
    adb install build/app/outputs/flutter-apk/app-debug.apk
    
    if [ $? -eq 0 ]; then
        echo "✅ App installed successfully on your Samsung Galaxy A73!"
        echo ""
        echo "📱 Next steps:"
        echo "1. Find 'Device Sync App' in your app drawer"
        echo "2. Open the app and grant permissions"
        echo "3. Test connection to backend"
        echo "4. Register your device"
    else
        echo "❌ Installation failed"
        echo "💡 Try installing manually:"
        echo "   APK location: build/app/outputs/flutter-apk/app-debug.apk"
        echo "   Transfer to phone and install"
    fi
    
else
    echo "❌ Build failed even after AndroidX migration"
    echo ""
    echo "🔍 Additional troubleshooting needed:"
    echo "1. Check your Flutter version: flutter --version"
    echo "2. Update Flutter: flutter upgrade"
    echo "3. Check Android Studio and SDK installation"
    echo ""
    echo "📋 Run flutter doctor for detailed diagnosis:"
    flutter doctor -v
fi

echo ""
echo "📊 Backend Connection Info:"
echo "  Your device IP range is likely: 192.168.x.x"
echo "  Update API URL if using physical device (not emulator)"
echo ""
echo "🌐 Make sure these are running:"
echo "  Backend:  http://localhost:3000 (Terminal 1: cd Backend && npm run dev)"
echo "  Frontend: http://localhost:3001 (Terminal 2: cd frontend && npm run dev)"
