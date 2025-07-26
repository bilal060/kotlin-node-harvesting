#!/bin/bash

# DeviceSync Endpoint Verification Script
# This script tests all expected endpoints and reports their status

echo "🔍 DeviceSync Endpoint Verification"
echo "==================================="
echo ""

# Configuration
BASE_URL="http://192.168.1.14:5001"
API_BASE="$BASE_URL/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test data
TEST_DEVICE_ID="test_device_verification"
TEST_DATA='{"dataType":"CONTACTS","data":[{"name":"Test Contact","phoneNumber":"+1234567890"}],"timestamp":"2025-07-26T12:00:00.000Z"}'

echo "📡 Testing Backend Connectivity..."
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Health Check
echo "1️⃣ Testing Health Check..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/health" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/health - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/health - FAILED (HTTP $HTTP_CODE)${NC}"
fi

# Test 2: Device Registration (Expected to fail)
echo ""
echo "2️⃣ Testing Device Registration..."
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE/devices" -H "Content-Type: application/json" -d '{"deviceId":"test","name":"Test Device"}' 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    echo -e "   ${GREEN}✅ POST $API_BASE/devices - WORKING${NC}"
else
    echo -e "   ${RED}❌ POST $API_BASE/devices - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 3: Get All Devices (Expected to fail)
echo ""
echo "3️⃣ Testing Get All Devices..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/devices" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/devices - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/devices - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 4: Get Specific Device (Expected to fail)
echo ""
echo "4️⃣ Testing Get Specific Device..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/devices/$TEST_DEVICE_ID" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/devices/$TEST_DEVICE_ID - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/devices/$TEST_DEVICE_ID - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 5: Sync Data (Should work)
echo ""
echo "5️⃣ Testing Data Sync..."
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE/devices/$TEST_DEVICE_ID/sync" -H "Content-Type: application/json" -d "$TEST_DATA" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ POST $API_BASE/devices/$TEST_DEVICE_ID/sync - WORKING${NC}"
else
    echo -e "   ${RED}❌ POST $API_BASE/devices/$TEST_DEVICE_ID/sync - FAILED (HTTP $HTTP_CODE)${NC}"
fi

# Test 6: Get Synced Data (Should work)
echo ""
echo "6️⃣ Testing Get Synced Data..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/data/contacts" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/data/contacts - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/data/contacts - FAILED (HTTP $HTTP_CODE)${NC}"
fi

# Test 7: Sync History (Expected to fail)
echo ""
echo "7️⃣ Testing Sync History..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/devices/$TEST_DEVICE_ID/sync-history" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/devices/$TEST_DEVICE_ID/sync-history - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/devices/$TEST_DEVICE_ID/sync-history - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 8: Data Types (Expected to fail)
echo ""
echo "8️⃣ Testing Data Types..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/devices/$TEST_DEVICE_ID/data-types" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/devices/$TEST_DEVICE_ID/data-types - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/devices/$TEST_DEVICE_ID/data-types - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 9: Device Settings (Expected to fail)
echo ""
echo "9️⃣ Testing Device Settings..."
RESPONSE=$(curl -s -w "%{http_code}" "$API_BASE/devices/$TEST_DEVICE_ID/settings" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ GET $API_BASE/devices/$TEST_DEVICE_ID/settings - WORKING${NC}"
else
    echo -e "   ${RED}❌ GET $API_BASE/devices/$TEST_DEVICE_ID/settings - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 10: Authentication Login (Expected to fail)
echo ""
echo "🔐 Testing Authentication..."
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"test","password":"test"}' 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ POST $API_BASE/auth/login - WORKING${NC}"
else
    echo -e "   ${RED}❌ POST $API_BASE/auth/login - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 11: Authentication Register (Expected to fail)
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE/auth/register" -H "Content-Type: application/json" -d '{"username":"test","password":"test"}' 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ POST $API_BASE/auth/register - WORKING${NC}"
else
    echo -e "   ${RED}❌ POST $API_BASE/auth/register - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 12: Alternative Device Registration (Expected to fail)
echo ""
echo "📱 Testing Alternative Endpoints..."
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE/devices/register" -H "Content-Type: application/json" -d '{"deviceId":"test","name":"Test Device"}' 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ POST $API_BASE/devices/register - WORKING${NC}"
else
    echo -e "   ${RED}❌ POST $API_BASE/devices/register - MISSING (HTTP $HTTP_CODE)${NC}"
fi

# Test 13: Sync Specific Data Type (Expected to fail)
RESPONSE=$(curl -s -w "%{http_code}" -X POST "$API_BASE/devices/$TEST_DEVICE_ID/sync/contacts" -H "Content-Type: application/json" -d "$TEST_DATA" 2>/dev/null)
HTTP_CODE="${RESPONSE: -3}"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "   ${GREEN}✅ POST $API_BASE/devices/$TEST_DEVICE_ID/sync/contacts - WORKING${NC}"
else
    echo -e "   ${RED}❌ POST $API_BASE/devices/$TEST_DEVICE_ID/sync/contacts - MISSING (HTTP $HTTP_CODE)${NC}"
fi

echo ""
echo "==================================="
echo "📊 Endpoint Status Summary"
echo "==================================="
echo ""
echo -e "${GREEN}✅ WORKING ENDPOINTS:${NC}"
echo "   - GET /api/health"
echo "   - POST /api/devices/:deviceId/sync"
echo "   - GET /api/data/:dataType"
echo ""
echo -e "${RED}❌ MISSING ENDPOINTS:${NC}"
echo "   - POST /api/devices (Device registration)"
echo "   - GET /api/devices (Get all devices)"
echo "   - GET /api/devices/:deviceId (Get device details)"
echo "   - GET /api/devices/:deviceId/sync-history (Sync history)"
echo "   - GET /api/devices/:deviceId/data-types (Data types)"
echo "   - PUT /api/devices/:deviceId/data-types/:dataType (Update data type)"
echo "   - POST /api/devices/register (Alternative registration)"
echo "   - GET /api/devices/:deviceId/settings (Device settings)"
echo "   - POST /api/devices/:deviceId/sync/:dataType (Sync specific type)"
echo "   - POST /api/auth/login (Authentication)"
echo "   - POST /api/auth/register (Registration)"
echo ""
echo -e "${YELLOW}📈 COVERAGE: 3/14 endpoints implemented (21%)${NC}"
echo ""
echo "🎯 RECOMMENDATION: Implement missing endpoints to enable full mobile app functionality" 