#!/bin/bash

# Mobile Data Capture Script
# This script captures all available data from the mobile device

echo "📱 Starting Mobile Data Capture..." > mobile_data_capture.txt
echo "Timestamp: $(date)" >> mobile_data_capture.txt
echo "==========================================" >> mobile_data_capture.txt

# Clear logcat first
adb logcat -c

echo "🔄 Clearing logs and starting capture..." >> mobile_data_capture.txt
echo "" >> mobile_data_capture.txt

# Start monitoring logs in background
adb logcat | grep -E "(DataCollector|📧|📊|📦|📤|📡|✅|❌|💥|CHUNK|CHUNKING|Found.*accounts|Processing account|Email accounts collection|Contacts collected|Call logs collected|Notifications collected|SMS collected|Device info|App list|File list|Calendar events|Browser history|Location data|WiFi networks|Bluetooth devices|Installed apps|System settings|Battery info|Storage info|Network info|Hardware info|Account info|User data|Permission status|Sync status|Upload status|Server response|Error|Exception|Warning|Info|Debug)" > temp_logs.txt &

# Store the background process ID
LOG_PID=$!

echo "⏳ Waiting for data collection to complete..." >> mobile_data_capture.txt
echo "Please run the sync in the app now..." >> mobile_data_capture.txt
echo "" >> mobile_data_capture.txt

# Wait for user to trigger sync
echo "🔄 Please open the app and tap 'Sync Data to Server' now..."
echo "⏳ Waiting 60 seconds for data collection..."

# Wait for 60 seconds to allow data collection
sleep 60

# Stop the background log monitoring
kill $LOG_PID 2>/dev/null

echo "📊 Data Collection Summary:" >> mobile_data_capture.txt
echo "==========================================" >> mobile_data_capture.txt

# Process the collected logs
if [ -f temp_logs.txt ]; then
    echo "📋 Raw Log Data:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    cat temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    echo "==========================================" >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    # Extract specific data sections
    echo "📧 Email Accounts Found:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Found.*accounts|Processing account|📧 EMAIL ACCOUNT OBJECT|📊 EMAIL ACCOUNTS SUMMARY)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📱 Device Information:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Device info|Hardware info|System info|Model|Manufacturer|Android version|Build number)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📞 Contacts & Communication:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Contacts collected|Call logs collected|SMS collected|Phone numbers|Contact names)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "🔔 Notifications:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Notifications collected|Notification|App notifications)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📱 Installed Apps:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Installed apps|App list|Package name|App name|App version)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📁 Files & Storage:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(File list|Storage info|File path|File size|File type)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📅 Calendar & Events:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Calendar events|Event|Calendar|Date|Time)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "🌐 Browser & Internet:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Browser history|URL|Website|Internet|WiFi networks|Network info)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📍 Location & Sensors:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Location data|GPS|Latitude|Longitude|Sensor|Accelerometer|Gyroscope)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "🔋 System & Hardware:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Battery info|Hardware info|CPU|Memory|Storage|System settings|Permission status)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "📡 Sync & Upload Status:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Sync status|Upload status|Server response|📦|📤|📡|✅|❌|💥|CHUNK|CHUNKING)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    echo "⚠️ Errors & Warnings:" >> mobile_data_capture.txt
    echo "------------------------------------------" >> mobile_data_capture.txt
    grep -E "(Error|Exception|Warning|Could not|Failed|Permission denied|Access denied)" temp_logs.txt >> mobile_data_capture.txt
    echo "" >> mobile_data_capture.txt
    
    # Clean up temp file
    rm temp_logs.txt
else
    echo "❌ No log data captured. Please ensure the app is running and sync is triggered." >> mobile_data_capture.txt
fi

echo "==========================================" >> mobile_data_capture.txt
echo "📱 Mobile Data Capture Complete!" >> mobile_data_capture.txt
echo "📄 Data saved to: mobile_data_capture.txt" >> mobile_data_capture.txt
echo "⏰ Completed at: $(date)" >> mobile_data_capture.txt

echo "✅ Mobile data capture completed!"
echo "📄 Data saved to: mobile_data_capture.txt" 