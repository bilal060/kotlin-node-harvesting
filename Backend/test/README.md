# DeviceSync API Test Suite

This directory contains comprehensive unit tests for all DeviceSync backend APIs.

## 📋 Test Coverage

### 1. Health Check API
- `GET /api/health` - Server status and database connectivity

### 2. Test Mode APIs
- `POST /api/test/devices/:deviceId/sync` - Sync data to test collections
- `GET /api/test/devices/:deviceId/:dataType` - Retrieve synced test data
- `POST /api/test/devices/:deviceId/upload-last-5-images` - Image upload functionality

### 3. Live Mode APIs
- `POST /api/devices/:deviceId/sync` - Sync data to main database
- `GET /api/data/:dataType` - Retrieve data from main database

### 4. Data Types Tested
- **CONTACTS** - Contact information sync
- **CALL_LOGS** - Call history sync
- **MESSAGES** - SMS/MMS sync
- **NOTIFICATIONS** - App notifications sync
- **EMAIL_ACCOUNTS** - Email account information sync

## 🚀 Running Tests

### Quick Start
```bash
# Run all tests
./run-tests.sh

# Or use npm directly
npm run test:api
```

### Individual Test Commands
```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run only API tests
npm run test:api

# Run API tests in watch mode
npm run test:api:watch
```

## 📊 Test Categories

### 1. **Health Check Tests**
- Server status verification
- Database connectivity
- API response structure validation

### 2. **Test Mode API Tests**
- Data synchronization to test collections
- All data type validations
- Error handling for invalid data
- Empty data array handling
- Malformed JSON handling

### 3. **Live Mode API Tests**
- Data synchronization to main database
- All data type validations
- Error handling for invalid data
- Empty data array handling
- Malformed JSON handling

### 4. **Data Retrieval Tests**
- Test collection data retrieval
- Main database data retrieval
- Pagination functionality
- Invalid data type handling

### 5. **Data Validation Tests**
- Required field validation for all data types
- Schema validation
- Data type conversion validation

### 6. **Error Handling Tests**
- Database connection errors
- Malformed device IDs
- Missing request bodies
- Invalid endpoints

### 7. **Performance Tests**
- Large data array processing
- Response time validation
- Memory usage optimization

## 🧪 Test Environment

### In-Memory Database
- Uses `mongodb-memory-server` for isolated testing
- No external database dependencies
- Automatic cleanup between tests

### Test Data
- Comprehensive test data for all data types
- Edge cases and invalid data scenarios
- Performance test data with large arrays

### Mocking
- Database operations are isolated
- No external API calls
- Controlled test environment

## 📈 Test Results

### Expected Output
```
🧪 DeviceSync API Test Suite
================================
🔍 Running API Tests...

 PASS  test/api.test.js
  DeviceSync API Tests
    Health Check API
      ✓ GET /api/health should return server status (45ms)
    Test Mode APIs
      POST /api/test/devices/:deviceId/sync
        ✓ should sync CONTACTS data to test collection (23ms)
        ✓ should sync CALL_LOGS data to test collection (15ms)
        ✓ should sync MESSAGES data to test collection (12ms)
        ✓ should sync NOTIFICATIONS data to test collection (18ms)
        ✓ should sync EMAIL_ACCOUNTS data to test collection (14ms)
        ✓ should handle invalid data type (8ms)
        ✓ should handle empty data array (7ms)
        ✓ should handle malformed JSON (6ms)
    Live Mode APIs
      POST /api/devices/:deviceId/sync
        ✓ should sync CONTACTS data to main database (25ms)
        ✓ should sync CALL_LOGS data to main database (19ms)
        ✓ should sync MESSAGES data to main database (16ms)
        ✓ should sync NOTIFICATIONS data to main database (21ms)
        ✓ should sync EMAIL_ACCOUNTS data to main database (17ms)
        ✓ should handle invalid data type in live mode (9ms)
        ✓ should handle empty data array in live mode (8ms)
        ✓ should handle malformed JSON in live mode (7ms)
    Data Validation Tests
      ✓ should validate required fields for CONTACTS (12ms)
      ✓ should validate required fields for CALL_LOGS (11ms)
      ✓ should validate required fields for MESSAGES (10ms)
      ✓ should validate required fields for NOTIFICATIONS (13ms)
    Error Handling Tests
      ✓ should handle database connection errors gracefully (8ms)
      ✓ should handle malformed device ID (6ms)
      ✓ should handle missing request body (5ms)
    Performance Tests
      ✓ should handle large data arrays efficiently (234ms)

Test Suites: 1 passed, 1 total
Tests:       25 passed, 25 total
Snapshots:   0 total
Time:        2.145s
```

## 🔧 Configuration

### Jest Configuration
- Test timeout: 30 seconds
- Coverage reporting enabled
- In-memory MongoDB for testing
- Automatic cleanup between tests

### Environment Variables
- `NODE_ENV=test` - Test environment
- `SUPPRESS_LOGS=false` - Show console logs during tests

## 🐛 Troubleshooting

### Common Issues

1. **MongoDB Memory Server Issues**
   ```bash
   # Clear Jest cache
   npx jest --clearCache
   ```

2. **Test Timeout Issues**
   ```bash
   # Increase timeout
   npm test -- --testTimeout=60000
   ```

3. **Port Conflicts**
   ```bash
   # Kill existing processes
   pkill -f "node server.js"
   ```

4. **Dependency Issues**
   ```bash
   # Reinstall dependencies
   rm -rf node_modules package-lock.json
   npm install
   ```

## 📝 Adding New Tests

### Test Structure
```javascript
describe('New API Endpoint', () => {
  test('should handle valid request', async () => {
    const response = await request(app)
      .post('/api/new-endpoint')
      .send(testData)
      .expect(200);

    expect(response.body).toHaveProperty('success', true);
  });

  test('should handle invalid request', async () => {
    const response = await request(app)
      .post('/api/new-endpoint')
      .send(invalidData)
      .expect(400);

    expect(response.body).toHaveProperty('success', false);
  });
});
```

### Test Data Pattern
```javascript
const testData = {
  dataType: 'NEW_TYPE',
  data: [
    {
      // Test data fields
    }
  ],
  timestamp: new Date().toISOString()
};
```

## 🎯 Best Practices

1. **Test Isolation**: Each test should be independent
2. **Data Cleanup**: Clear collections before each test
3. **Error Scenarios**: Test both success and failure cases
4. **Performance**: Include performance tests for large datasets
5. **Validation**: Test all data validation rules
6. **Documentation**: Keep test descriptions clear and descriptive

## 📞 Support

For test-related issues or questions:
1. Check the troubleshooting section
2. Review test logs for specific error messages
3. Ensure all dependencies are properly installed
4. Verify MongoDB memory server is working correctly 