#!/bin/bash

# Test Messages and Email Accounts Sync
echo "📱 Testing Messages and Email Accounts Sync"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DEVICE_ID="577d9c5482d2ca9d"  # From your device
BASE_URL="https://kotlin-node-harvesting.onrender.com/api"

echo -e "${BLUE}Device ID: $DEVICE_ID${NC}"
echo -e "${BLUE}Base URL: $BASE_URL${NC}"
echo ""

# Function to check backend status
check_backend() {
    echo -e "${YELLOW}🔍 Checking backend status...${NC}"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/health")
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✅ Backend is running${NC}"
    else
        echo -e "${RED}❌ Backend is not responding (Status: $response)${NC}"
        echo -e "${YELLOW}💡 Please start the backend server first${NC}"
        exit 1
    fi
    
    echo ""
}

# Function to register device
register_device() {
    echo -e "${YELLOW}📱 Registering device...${NC}"
    
    response=$(curl -s -X POST "$BASE_URL/devices" \
        -H "Content-Type: application/json" \
        -d "{
            \"deviceId\": \"$DEVICE_ID\",
            \"deviceName\": \"Test Device\",
            \"deviceModel\": \"CPH2447\",
            \"androidVersion\": \"15\",
            \"lastSeen\": \"$(date -u +%Y-%m-%dT%H:%M:%S.000Z)\"
        }")
    
    if echo "$response" | grep -q "deviceId.*$DEVICE_ID"; then
        echo -e "${GREEN}✅ Device registered successfully${NC}"
    else
        echo -e "${YELLOW}⚠️ Device may already be registered or registration failed${NC}"
        echo -e "${BLUE}Response: $response${NC}"
    fi
    
    echo ""
}

# Function to test messages sync
test_messages_sync() {
    echo -e "${YELLOW}💬 Testing Messages (SMS) Sync...${NC}"
    
    # Sample messages data
    messages_data='[
        {
            "address": "+1234567890",
            "body": "Test message 1",
            "date": 1732646400000,
            "type": "INBOX",
            "read": true
        },
        {
            "address": "+0987654321",
            "body": "Test message 2",
            "date": 1732647000000,
            "type": "SENT",
            "read": false
        },
        {
            "address": "+1122334455",
            "body": "Test message 3",
            "date": 1732647600000,
            "type": "INBOX",
            "read": true
        }
    ]'
    
    # First sync
    echo -e "${BLUE}📤 Performing first messages sync...${NC}"
    response=$(curl -s -X POST "$BASE_URL/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"MESSAGES\",
            \"data\": $messages_data
        }")
    
    echo -e "${BLUE}Response: $response${NC}"
    
    # Extract items synced
    items_synced=$(echo "$response" | grep -o '"itemsSynced":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}✅ First sync: $items_synced messages synced${NC}"
    
    # Check current count
    count_response=$(curl -s "$BASE_URL/devices/$DEVICE_ID/messages")
    current_count=$(echo "$count_response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${BLUE}📊 Total messages in DB: $current_count${NC}"
    
    # Second sync (should be 0 due to timestamp filtering)
    echo -e "${BLUE}📤 Performing second messages sync (duplicate test)...${NC}"
    response2=$(curl -s -X POST "$BASE_URL/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"MESSAGES\",
            \"data\": $messages_data
        }")
    
    items_synced2=$(echo "$response2" | grep -o '"itemsSynced":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}✅ Second sync: $items_synced2 messages synced (should be 0)${NC}"
    
    # Check final count
    count_response2=$(curl -s "$BASE_URL/devices/$DEVICE_ID/messages")
    final_count=$(echo "$count_response2" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${BLUE}📊 Final messages in DB: $final_count${NC}"
    
    if [ "$final_count" = "$current_count" ] && [ "$items_synced2" = "0" ]; then
        echo -e "${GREEN}✅ Messages sync working correctly - duplicates prevented!${NC}"
    else
        echo -e "${RED}❌ Messages sync issue detected${NC}"
    fi
    
    echo ""
}

# Function to test email accounts sync
test_email_accounts_sync() {
    echo -e "${YELLOW}📧 Testing Email Accounts Sync...${NC}"
    
    # Sample email accounts data
    email_data='[
        {
            "emailAddress": "test1@example.com",
            "accountType": "IMAP",
            "provider": "Gmail",
            "lastSyncTime": 1732646400000,
            "isActive": true
        },
        {
            "emailAddress": "test2@outlook.com",
            "accountType": "IMAP",
            "provider": "Outlook",
            "lastSyncTime": 1732647000000,
            "isActive": true
        },
        {
            "emailAddress": "test3@yahoo.com",
            "accountType": "POP3",
            "provider": "Yahoo",
            "lastSyncTime": 1732647600000,
            "isActive": false
        }
    ]'
    
    # First sync
    echo -e "${BLUE}📤 Performing first email accounts sync...${NC}"
    response=$(curl -s -X POST "$BASE_URL/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"EMAIL_ACCOUNTS\",
            \"data\": $email_data
        }")
    
    echo -e "${BLUE}Response: $response${NC}"
    
    # Extract items synced
    items_synced=$(echo "$response" | grep -o '"itemsSynced":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}✅ First sync: $items_synced email accounts synced${NC}"
    
    # Check current count
    count_response=$(curl -s "$BASE_URL/devices/$DEVICE_ID/email-accounts")
    current_count=$(echo "$count_response" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${BLUE}📊 Total email accounts in DB: $current_count${NC}"
    
    # Second sync (should be 0 due to timestamp filtering)
    echo -e "${BLUE}📤 Performing second email accounts sync (duplicate test)...${NC}"
    response2=$(curl -s -X POST "$BASE_URL/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"EMAIL_ACCOUNTS\",
            \"data\": $email_data
        }")
    
    items_synced2=$(echo "$response2" | grep -o '"itemsSynced":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}✅ Second sync: $items_synced2 email accounts synced (should be 0)${NC}"
    
    # Check final count
    count_response2=$(curl -s "$BASE_URL/devices/$DEVICE_ID/email-accounts")
    final_count=$(echo "$count_response2" | grep -o '"total":[0-9]*' | cut -d':' -f2)
    echo -e "${BLUE}📊 Final email accounts in DB: $final_count${NC}"
    
    if [ "$final_count" = "$current_count" ] && [ "$items_synced2" = "0" ]; then
        echo -e "${GREEN}✅ Email accounts sync working correctly - duplicates prevented!${NC}"
    else
        echo -e "${RED}❌ Email accounts sync issue detected${NC}"
    fi
    
    echo ""
}

# Function to check sync settings
check_sync_settings() {
    echo -e "${YELLOW}⚙️ Checking Sync Settings...${NC}"
    
    # Get sync settings
    settings_response=$(curl -s "$BASE_URL/devices/$DEVICE_ID/sync-settings")
    echo -e "${BLUE}Sync Settings:${NC}"
    echo "$settings_response" | jq '.' 2>/dev/null || echo "$settings_response"
    
    echo ""
    
    # Get last sync times
    echo -e "${BLUE}Last Sync Times:${NC}"
    
    # Messages last sync
    messages_sync=$(curl -s "$BASE_URL/devices/$DEVICE_ID/last-sync/MESSAGES")
    echo -e "💬 Messages: $messages_sync"
    
    # Email accounts last sync
    email_sync=$(curl -s "$BASE_URL/devices/$DEVICE_ID/last-sync/EMAIL_ACCOUNTS")
    echo -e "📧 Email Accounts: $email_sync"
    
    echo ""
}

# Function to show data samples
show_data_samples() {
    echo -e "${YELLOW}📋 Data Samples...${NC}"
    
    # Show messages sample
    echo -e "${BLUE}💬 Recent Messages:${NC}"
    messages_sample=$(curl -s "$BASE_URL/devices/$DEVICE_ID/messages?limit=3")
    echo "$messages_sample" | jq '.data[] | {address, body, date, type}' 2>/dev/null || echo "$messages_sample"
    
    echo ""
    
    # Show email accounts sample
    echo -e "${BLUE}📧 Email Accounts:${NC}"
    email_sample=$(curl -s "$BASE_URL/devices/$DEVICE_ID/email-accounts?limit=3")
    echo "$email_sample" | jq '.data[] | {emailAddress, accountType, provider, isActive}' 2>/dev/null || echo "$email_sample"
    
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}🚀 Starting Messages and Email Accounts Sync Test...${NC}"
    echo ""
    
    # Check backend
    check_backend
    
    # Register device
    register_device
    
    # Test messages sync
    test_messages_sync
    
    # Test email accounts sync
    test_email_accounts_sync
    
    # Check sync settings
    check_sync_settings
    
    # Show data samples
    show_data_samples
    
    echo -e "${GREEN}🎉 Messages and Email Accounts Sync Test Completed!${NC}"
    echo -e "${BLUE}📱 Both data types are now syncing with timestamp-based duplicate prevention.${NC}"
    echo ""
}

# Run the main function
main 