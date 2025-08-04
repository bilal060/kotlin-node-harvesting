#!/bin/bash

# Email Data Capture Script
# This script captures detailed email account information

echo "📧 Starting Email Data Capture..." > email_data_capture.txt
echo "Timestamp: $(date)" >> email_data_capture.txt
echo "==========================================" >> email_data_capture.txt

# Clear logcat first
adb logcat -c

echo "🔄 Clearing logs and starting email capture..." >> email_data_capture.txt
echo "" >> email_data_capture.txt

# Start monitoring logs in background with specific filters
adb logcat | grep -E "(DataCollector|📧|📊|Found.*accounts|Processing account|Email accounts collection|Could not get|uid.*cannot get)" > temp_email_logs.txt &

# Store the background process ID
LOG_PID=$!

echo "⏳ Waiting for email data collection..." >> email_data_capture.txt
echo "Please run the sync in the app now..." >> email_data_capture.txt
echo "" >> email_data_capture.txt

# Wait for user to trigger sync
echo "🔄 Please open the app and tap 'Sync Data to Server' now..."
echo "⏳ Waiting 30 seconds for email data collection..."

# Wait for 30 seconds to allow data collection
sleep 30

# Stop the background log monitoring
kill $LOG_PID 2>/dev/null

echo "📊 Email Data Collection Summary:" >> email_data_capture.txt
echo "==========================================" >> email_data_capture.txt

# Process the collected logs
if [ -f temp_email_logs.txt ]; then
    echo "📋 Raw Email Log Data:" >> email_data_capture.txt
    echo "------------------------------------------" >> email_data_capture.txt
    cat temp_email_logs.txt >> email_data_capture.txt
    echo "" >> email_data_capture.txt
    echo "==========================================" >> email_data_capture.txt
    echo "" >> email_data_capture.txt
    
    # Extract email account details
    echo "📧 Email Account Objects:" >> email_data_capture.txt
    echo "------------------------------------------" >> email_data_capture.txt
    grep -E "(📧 EMAIL ACCOUNT OBJECT|📊 EMAIL ACCOUNTS SUMMARY)" temp_email_logs.txt >> email_data_capture.txt
    echo "" >> email_data_capture.txt
    
    echo "📧 Email Account Details:" >> email_data_capture.txt
    echo "------------------------------------------" >> email_data_capture.txt
    grep -E "(Name:|Type:|Description:|Account Info:|Full JSON:)" temp_email_logs.txt >> email_data_capture.txt
    echo "" >> email_data_capture.txt
    
    echo "📧 Account Processing:" >> email_data_capture.txt
    echo "------------------------------------------" >> email_data_capture.txt
    grep -E "(Processing account|Found.*accounts)" temp_email_logs.txt >> email_data_capture.txt
    echo "" >> email_data_capture.txt
    
    echo "⚠️ Access Issues:" >> email_data_capture.txt
    echo "------------------------------------------" >> email_data_capture.txt
    grep -E "(Could not get|uid.*cannot get|Permission denied|Access denied)" temp_email_logs.txt >> email_data_capture.txt
    echo "" >> email_data_capture.txt
    
    # Clean up temp file
    rm temp_email_logs.txt
else
    echo "❌ No email log data captured. Please ensure the app is running and sync is triggered." >> email_data_capture.txt
fi

echo "==========================================" >> email_data_capture.txt
echo "📧 Email Data Capture Complete!" >> email_data_capture.txt
echo "📄 Data saved to: email_data_capture.txt" >> email_data_capture.txt
echo "⏰ Completed at: $(date)" >> email_data_capture.txt

echo "✅ Email data capture completed!"
echo "📄 Data saved to: email_data_capture.txt" 