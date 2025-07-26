#!/bin/bash

echo "Building and running DeviceSync Kotlin app..."

# Build the project
echo "Building project..."
./gradlew build

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Check for connected devices
    echo "Checking for connected devices..."
    adb devices
    
    # Install the app
    echo "Installing app..."
    ./gradlew installDebug
    
    if [ $? -eq 0 ]; then
        echo "Installation successful!"
        
        # Launch the app
        echo "Launching app..."
        adb shell am start -n com.devicesync.app/.MainActivity
        
        echo "App launched successfully!"
    else
        echo "Installation failed!"
        exit 1
    fi
else
    echo "Build failed!"
    exit 1
fi 