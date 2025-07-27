#!/bin/bash

# Test script to verify duplicate detection is working
echo "🧪 Testing Duplicate Detection System"
echo "====================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DEVICE_ID="test_device_$(date +%s)"
BACKEND_URL="https://kotlin-node-harvesting.onrender.com"
TEST_COUNT=3

echo -e "${BLUE}Device ID: $DEVICE_ID${NC}"
echo -e "${BLUE}Backend URL: $BACKEND_URL${NC}"
echo -e "${BLUE}Test Count: $TEST_COUNT${NC}"
echo ""

# Function to check if backend is running
check_backend() {
    echo -e "${YELLOW}🔍 Checking backend status...${NC}"
    response=$(curl -s "$BACKEND_URL/api/health")
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Backend is running${NC}"
        echo "Response: $response"
    else
        echo -e "${RED}❌ Backend is not accessible${NC}"
        exit 1
    fi
    echo ""
}

# Function to register device
register_device() {
    echo -e "${YELLOW}📱 Registering device...${NC}"
    response=$(curl -s -X POST "$BACKEND_URL/api/devices/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"deviceId\": \"$DEVICE_ID\",
            \"deviceInfo\": {
                \"name\": \"Test Device\",
                \"model\": \"Test Model\",
                \"manufacturer\": \"Test Manufacturer\",
                \"androidVersion\": \"11\",
                \"appVersion\": \"1.0.0\"
            }
        }")
    
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Device registered successfully${NC}"
        echo "Response: $response"
    else
        echo -e "${RED}❌ Failed to register device${NC}"
        exit 1
    fi
    echo ""
}

# Function to sync test data
sync_test_data() {
    local test_number=$1
    echo -e "${YELLOW}🔄 Running sync test #$test_number...${NC}"
    
    # Test contacts sync
    echo -e "${BLUE}📞 Syncing contacts...${NC}"
    contacts_response=$(curl -s -X POST "$BACKEND_URL/api/test/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"CONTACTS\",
            \"data\": [
                {
                    \"name\": \"John Doe\",
                    \"phoneNumber\": \"+1234567890\",
                    \"phoneType\": \"MOBILE\",
                    \"emails\": [\"john@example.com\"],
                    \"organization\": \"Test Corp\"
                },
                {
                    \"name\": \"Jane Smith\",
                    \"phoneNumber\": \"+0987654321\",
                    \"phoneType\": \"MOBILE\",
                    \"emails\": [\"jane@example.com\"],
                    \"organization\": \"Test Corp\"
                }
            ],
            \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"
        }")
    
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Contacts sync response: $contacts_response${NC}"
    else
        echo -e "${RED}❌ Contacts sync failed${NC}"
    fi
    
    # Test call logs sync
    echo -e "${BLUE}📞 Syncing call logs...${NC}"
    calllogs_response=$(curl -s -X POST "$BACKEND_URL/api/test/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"CALL_LOGS\",
            \"data\": [
                {
                    \"number\": \"+1234567890\",
                    \"type\": \"INCOMING\",
                    \"duration\": 120,
                    \"date\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"
                },
                {
                    \"number\": \"+0987654321\",
                    \"type\": \"OUTGOING\",
                    \"duration\": 60,
                    \"date\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"
                }
            ],
            \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"
        }")
    
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Call logs sync response: $calllogs_response${NC}"
    else
        echo -e "${RED}❌ Call logs sync failed${NC}"
    fi
    
    # Test messages sync
    echo -e "${BLUE}💬 Syncing messages...${NC}"
    messages_response=$(curl -s -X POST "$BACKEND_URL/api/test/devices/$DEVICE_ID/sync" \
        -H "Content-Type: application/json" \
        -d "{
            \"dataType\": \"MESSAGES\",
            \"data\": [
                {
                    \"address\": \"+1234567890\",
                    \"body\": \"Hello, this is a test message\",
                    \"type\": \"SMS\",
                    \"isIncoming\": true,
                    \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\",
                    \"isRead\": true
                },
                {
                    \"address\": \"+0987654321\",
                    \"body\": \"Reply to test message\",
                    \"type\": \"SMS\",
                    \"isIncoming\": false,
                    \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\",
                    \"isRead\": true
                }
            ],
            \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"
        }")
    
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✅ Messages sync response: $messages_response${NC}"
    else
        echo -e "${RED}❌ Messages sync failed${NC}"
    fi
    
    echo ""
}

# Function to check data counts
check_data_counts() {
    echo -e "${YELLOW}📊 Checking data counts...${NC}"
    
    # Check contacts count
    contacts_count=$(curl -s "$BACKEND_URL/api/test/devices/$DEVICE_ID/contacts" | jq -r '.data.items | length' 2>/dev/null || echo "0")
    echo -e "${BLUE}📞 Contacts count: $contacts_count${NC}"
    
    # Check call logs count
    calllogs_count=$(curl -s "$BACKEND_URL/api/test/devices/$DEVICE_ID/call-logs" | jq -r '.data.items | length' 2>/dev/null || echo "0")
    echo -e "${BLUE}📞 Call logs count: $calllogs_count${NC}"
    
    # Check messages count
    messages_count=$(curl -s "$BACKEND_URL/api/test/devices/$DEVICE_ID/messages" | jq -r '.data.items | length' 2>/dev/null || echo "0")
    echo -e "${BLUE}💬 Messages count: $messages_count${NC}"
    
    echo ""
}

# Function to analyze results
analyze_results() {
    echo -e "${YELLOW}📈 Analyzing results...${NC}"
    
    # Get final counts
    final_contacts=$(curl -s "$BACKEND_URL/api/test/devices/$DEVICE_ID/contacts" | jq -r '.data.items | length' 2>/dev/null || echo "0")
    final_calllogs=$(curl -s "$BACKEND_URL/api/test/devices/$DEVICE_ID/call-logs" | jq -r '.data.items | length' 2>/dev/null || echo "0")
    final_messages=$(curl -s "$BACKEND_URL/api/test/devices/$DEVICE_ID/messages" | jq -r '.data.items | length' 2>/dev/null || echo "0")
    
    # Expected counts (2 items per data type)
    expected_contacts=2
    expected_calllogs=2
    expected_messages=2
    
    echo -e "${BLUE}Expected counts:${NC}"
    echo -e "  📞 Contacts: $expected_contacts"
    echo -e "  📞 Call logs: $expected_calllogs"
    echo -e "  💬 Messages: $expected_messages"
    
    echo -e "${BLUE}Actual counts:${NC}"
    echo -e "  📞 Contacts: $final_contacts"
    echo -e "  📞 Call logs: $final_calllogs"
    echo -e "  💬 Messages: $final_messages"
    
    echo ""
    
    # Check if duplicate detection is working
    if [[ $final_contacts -eq $expected_contacts && $final_calllogs -eq $expected_calllogs && $final_messages -eq $expected_messages ]]; then
        echo -e "${GREEN}✅ SUCCESS: Duplicate detection is working correctly!${NC}"
        echo -e "${GREEN}   No duplicate records were created despite running $TEST_COUNT sync operations.${NC}"
    else
        echo -e "${RED}❌ FAILURE: Duplicate detection is not working properly!${NC}"
        echo -e "${RED}   Expected $expected_contacts contacts, got $final_contacts${NC}"
        echo -e "${RED}   Expected $expected_calllogs call logs, got $final_calllogs${NC}"
        echo -e "${RED}   Expected $expected_messages messages, got $final_messages${NC}"
    fi
    
    echo ""
}

# Main test execution
main() {
    echo -e "${BLUE}🚀 Starting duplicate detection test...${NC}"
    echo ""
    
    # Check backend status
    check_backend
    
    # Register device
    register_device
    
    # Run multiple sync operations
    for i in $(seq 1 $TEST_COUNT); do
        sync_test_data $i
        echo -e "${YELLOW}⏳ Waiting 2 seconds before next test...${NC}"
        sleep 2
    done
    
    # Check final data counts
    check_data_counts
    
    # Analyze results
    analyze_results
    
    echo -e "${BLUE}🏁 Test completed!${NC}"
}

# Run the test
main 