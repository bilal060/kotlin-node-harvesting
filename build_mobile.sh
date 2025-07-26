#!/bin/bash

echo "📱 Device Sync Mobile App - Build & Install Script"
echo "================================================="

# Change to App directory
cd "$(dirname "$0")/App"

echo "🧹 Cleaning Flutter project..."
flutter clean

echo "📦 Getting dependencies..."
flutter pub get

echo "🔧 Cleaning Android build..."
cd android
./gradlew clean
cd ..

echo "🔍 Checking Flutter doctor..."
flutter doctor

echo "📱 Checking connected devices..."
DEVICES=$(flutter devices)
echo "$DEVICES"

if [[ $DEVICES == *"No devices detected"* ]]; then
    echo "❌ No devices detected!"
    echo ""
    echo "Please ensure:"
    echo "1. Your phone is connected via USB"
    echo "2. USB Debugging is enabled in Developer Options"
    echo "3. You've allowed USB debugging on your phone"
    echo ""
    echo "To enable Developer Options:"
    echo "- Go to Settings > About Phone"
    echo "- Tap 'Build Number' 7 times"
    echo "- Go back to Settings > Developer Options"
    echo "- Enable 'USB Debugging'"
    echo ""
    read -p "Press Enter after connecting your device..."
    flutter devices
fi

echo ""
echo "🚀 Choose installation method:"
echo "1. Run in debug mode (recommended)"
echo "2. Build and install APK"
echo "3. Build APK only"

read -p "Enter your choice (1-3): " choice

case $choice in
    1)
        echo "🏃 Running in debug mode..."
        flutter run --debug
        ;;
    2)
        echo "🔨 Building APK..."
        flutter build apk --debug
        
        if [ $? -eq 0 ]; then
            echo "✅ APK built successfully!"
            echo "📲 Installing APK..."
            adb install build/app/outputs/flutter-apk/app-debug.apk
            
            if [ $? -eq 0 ]; then
                echo "🎉 App installed successfully!"
                echo "You can now find 'Device Sync App' in your phone's app drawer"
            else
                echo "❌ Failed to install APK"
                echo "Try installing manually: build/app/outputs/flutter-apk/app-debug.apk"
            fi
        else
            echo "❌ Failed to build APK"
        fi
        ;;
    3)
        echo "🔨 Building APK..."
        flutter build apk --debug
        
        if [ $? -eq 0 ]; then
            echo "✅ APK built successfully!"
            echo "📁 APK location: build/app/outputs/flutter-apk/app-debug.apk"
            echo "📲 To install manually:"
            echo "   adb install build/app/outputs/flutter-apk/app-debug.apk"
            echo "   Or transfer the APK to your phone and install it"
        else
            echo "❌ Failed to build APK"
        fi
        ;;
    *)
        echo "❌ Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "📋 Next Steps:"
echo "1. Make sure the backend server is running: cd Backend && npm run dev"
echo "2. Make sure the frontend is running: cd frontend && npm run dev"
echo "3. Open the app on your phone"
echo "4. Grant permissions when prompted"
echo "5. Test the connection to the backend"
echo "6. Register your device"
echo ""
echo "🌐 Backend should be at: http://localhost:3000"
echo "🖥️  Frontend dashboard at: http://localhost:3001"
echo ""
echo "📱 If you're using a physical device (not emulator):"
echo "   Update the API URL in lib/main.dart from 10.0.2.2 to your computer's IP address"
