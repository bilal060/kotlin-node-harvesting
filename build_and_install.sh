#!/bin/bash

# Build and Install Script for DeviceSync App
echo "🚀 Building and Installing DeviceSync App"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="DeviceSync"
BUILD_TYPE="debug"  # or "release"
APK_NAME="app-debug.apk"
BUILD_DIR="kotlin/app/build/outputs/apk/debug"
FINAL_APK="DeviceSync_$(date +%Y%m%d_%H%M%S).apk"

echo -e "${BLUE}App Name: $APP_NAME${NC}"
echo -e "${BLUE}Build Type: $BUILD_TYPE${NC}"
echo -e "${BLUE}APK Name: $APK_NAME${NC}"
echo -e "${BLUE}Build Directory: $BUILD_DIR${NC}"
echo -e "${BLUE}Final APK: $FINAL_APK${NC}"
echo ""

# Function to check if Android SDK is available
check_android_sdk() {
    echo -e "${YELLOW}🔍 Checking Android SDK...${NC}"
    
    if command -v adb &> /dev/null; then
        echo -e "${GREEN}✅ ADB found${NC}"
    else
        echo -e "${RED}❌ ADB not found. Please install Android SDK and add to PATH${NC}"
        echo -e "${YELLOW}💡 You can install Android Studio which includes the SDK${NC}"
        exit 1
    fi
    
    if command -v gradle &> /dev/null; then
        echo -e "${GREEN}✅ Gradle found${NC}"
    else
        echo -e "${YELLOW}⚠️ Gradle not found, will use gradlew wrapper${NC}"
    fi
    
    echo ""
}

# Function to check if device is connected
check_device_connection() {
    echo -e "${YELLOW}📱 Checking device connection...${NC}"
    
    # Check if any device is connected
    devices=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
    
    if [ $devices -eq 0 ]; then
        echo -e "${RED}❌ No Android device connected${NC}"
        echo -e "${YELLOW}💡 Please connect your device via USB and enable USB debugging${NC}"
        echo -e "${YELLOW}💡 Or start an emulator${NC}"
        echo ""
        echo -e "${BLUE}To enable USB debugging:${NC}"
        echo -e "1. Go to Settings > About phone"
        echo -e "2. Tap 'Build number' 7 times to enable Developer options"
        echo -e "3. Go to Settings > Developer options"
        echo -e "4. Enable 'USB debugging'"
        echo -e "5. Connect device via USB"
        echo -e "6. Allow USB debugging when prompted"
        echo ""
        exit 1
    else
        echo -e "${GREEN}✅ Android device connected${NC}"
        adb devices
    fi
    
    echo ""
}

# Function to clean previous builds
clean_build() {
    echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
    
    cd kotlin
    
    if [ -f "gradlew" ]; then
        ./gradlew clean
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Build cleaned successfully${NC}"
        else
            echo -e "${RED}❌ Failed to clean build${NC}"
            exit 1
        fi
    else
        echo -e "${RED}❌ Gradle wrapper not found${NC}"
        exit 1
    fi
    
    cd ..
    echo ""
}

# Function to build the app
build_app() {
    echo -e "${YELLOW}🔨 Building $APP_NAME...${NC}"
    
    cd kotlin
    
    if [ -f "gradlew" ]; then
        echo -e "${BLUE}Building with Gradle wrapper...${NC}"
        ./gradlew assembleDebug
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Build completed successfully${NC}"
        else
            echo -e "${RED}❌ Build failed${NC}"
            echo -e "${YELLOW}💡 Check the error messages above${NC}"
            cd ..
            exit 1
        fi
    else
        echo -e "${RED}❌ Gradle wrapper not found${NC}"
        cd ..
        exit 1
    fi
    
    cd ..
    echo ""
}

# Function to check if APK was created
check_apk() {
    echo -e "${YELLOW}📦 Checking APK file...${NC}"
    
    if [ -f "$BUILD_DIR/$APK_NAME" ]; then
        echo -e "${GREEN}✅ APK created successfully${NC}"
        echo -e "${BLUE}APK Location: $BUILD_DIR/$APK_NAME${NC}"
        
        # Get APK size
        apk_size=$(du -h "$BUILD_DIR/$APK_NAME" | cut -f1)
        echo -e "${BLUE}APK Size: $apk_size${NC}"
        
        # Copy APK to current directory with timestamp
        cp "$BUILD_DIR/$APK_NAME" "$FINAL_APK"
        echo -e "${GREEN}✅ APK copied to: $FINAL_APK${NC}"
        
    else
        echo -e "${RED}❌ APK not found at expected location${NC}"
        echo -e "${YELLOW}💡 Expected: $BUILD_DIR/$APK_NAME${NC}"
        exit 1
    fi
    
    echo ""
}

# Function to install the app
install_app() {
    echo -e "${YELLOW}📱 Installing app on device...${NC}"
    
    # Uninstall existing app if it exists
    echo -e "${BLUE}Checking for existing installation...${NC}"
    adb shell pm list packages | grep -q "com.devicesync.app"
    if [ $? -eq 0 ]; then
        echo -e "${YELLOW}Found existing installation, uninstalling...${NC}"
        adb uninstall com.devicesync.app
        echo -e "${GREEN}✅ Existing app uninstalled${NC}"
    else
        echo -e "${BLUE}No existing installation found${NC}"
    fi
    
    # Install new APK
    echo -e "${BLUE}Installing new APK...${NC}"
    adb install "$FINAL_APK"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ App installed successfully${NC}"
    else
        echo -e "${RED}❌ App installation failed${NC}"
        echo -e "${YELLOW}💡 Check if device has enough storage and installation from unknown sources is enabled${NC}"
        exit 1
    fi
    
    echo ""
}

# Function to launch the app
launch_app() {
    echo -e "${YELLOW}🚀 Launching app...${NC}"
    
    adb shell am start -n com.devicesync.app/.SplashActivity
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ App launched successfully${NC}"
    else
        echo -e "${RED}❌ Failed to launch app${NC}"
    fi
    
    echo ""
}

# Function to show app info
show_app_info() {
    echo -e "${YELLOW}📋 App Information${NC}"
    echo -e "${BLUE}Package Name: com.devicesync.app${NC}"
    echo -e "${BLUE}Version: 1.0.0${NC}"
    echo -e "${BLUE}Build Type: $BUILD_TYPE${NC}"
    echo -e "${BLUE}APK File: $FINAL_APK${NC}"
    echo ""
    
    # Get app version from device
    echo -e "${BLUE}Installed Version:${NC}"
    adb shell dumpsys package com.devicesync.app | grep versionName || echo "Not installed"
    echo ""
}

# Function to show device info
show_device_info() {
    echo -e "${YELLOW}📱 Device Information${NC}"
    
    # Get device model
    model=$(adb shell getprop ro.product.model)
    echo -e "${BLUE}Model: $model${NC}"
    
    # Get Android version
    version=$(adb shell getprop ro.build.version.release)
    echo -e "${BLUE}Android Version: $version${NC}"
    
    # Get device ID
    device_id=$(adb shell settings get secure android_id)
    echo -e "${BLUE}Device ID: $device_id${NC}"
    
    echo ""
}

# Function to show sync system info
show_sync_info() {
    echo -e "${YELLOW}🔄 Sync System Information${NC}"
    echo -e "${BLUE}New Features:${NC}"
    echo -e "  ✅ Timestamp-based sync system"
    echo -e "  ✅ Device ID tracking in all data"
    echo -e "  ✅ Efficient duplicate detection"
    echo -e "  ✅ Sync settings management"
    echo -e "  ✅ Local timestamp storage"
    echo -e "  ✅ Smart data filtering"
    echo ""
    
    echo -e "${BLUE}Supported Data Types:${NC}"
    echo -e "  📞 Contacts"
    echo -e "  📞 Call Logs"
    echo -e "  💬 Messages (SMS)"
    echo -e "  🔔 Notifications"
    echo -e "  📧 Email Accounts"
    echo -e "  💬 WhatsApp Messages"
    echo ""
}

# Function to provide usage instructions
show_usage_instructions() {
    echo -e "${YELLOW}📖 Usage Instructions${NC}"
    echo -e "${BLUE}1. Launch the app${NC}"
    echo -e "${BLUE}2. Grant necessary permissions when prompted${NC}"
    echo -e "${BLUE}3. The app will automatically sync data based on timestamps${NC}"
    echo -e "${BLUE}4. Check sync status in the app${NC}"
    echo ""
    
    echo -e "${BLUE}Required Permissions:${NC}"
    echo -e "  📞 Read Contacts"
    echo -e "  📞 Read Call Log"
    echo -e "  💬 Read SMS"
    echo -e "  🔔 Notification Access"
    echo -e "  📁 Storage Access"
    echo ""
    
    echo -e "${BLUE}Testing the Sync System:${NC}"
    echo -e "  🧪 Run: ./test_timestamp_sync.sh"
    echo -e "  📊 Check sync statistics in the app"
    echo -e "  🔄 Monitor sync operations in logs"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}🚀 Starting build and install process...${NC}"
    echo ""
    
    # Check prerequisites
    check_android_sdk
    check_device_connection
    
    # Show device info
    show_device_info
    
    # Clean and build
    clean_build
    build_app
    check_apk
    
    # Install and launch
    install_app
    launch_app
    
    # Show information
    show_app_info
    show_sync_info
    show_usage_instructions
    
    echo -e "${GREEN}🎉 Build and install completed successfully!${NC}"
    echo -e "${BLUE}📱 Your DeviceSync app is now ready to use with the new timestamp-based sync system.${NC}"
    echo ""
}

# Run the main function
main 