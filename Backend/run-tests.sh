#!/bin/bash

# DeviceSync API Test Runner
# This script runs comprehensive tests for all backend APIs

echo "🧪 DeviceSync API Test Suite"
echo "================================"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Check if test dependencies are installed
if ! npm list jest > /dev/null 2>&1; then
    echo "📦 Installing test dependencies..."
    npm install --save-dev jest supertest mongodb-memory-server
fi

echo "🔍 Running API Tests..."
echo ""

# Run the tests with coverage
npm run test:api

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
    echo ""
    echo "📊 Test Summary:"
    echo "- Health Check API: ✅"
    echo "- Test Mode APIs: ✅"
    echo "- Live Mode APIs: ✅"
    echo "- Data Validation: ✅"
    echo "- Error Handling: ✅"
    echo "- Performance Tests: ✅"
else
    echo ""
    echo "❌ Some tests failed!"
    echo "Check the output above for details."
    exit 1
fi

echo ""
echo "🎯 Test Coverage Report:"
npm run test:coverage

echo ""
echo "🏁 Test suite completed!" 