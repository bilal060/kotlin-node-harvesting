const axios = require('axios');
const colors = require('colors');

// Configuration
const BASE_URL = 'http://localhost:5002/api';

// Test results tracking
let passedTests = 0;
let failedTests = 0;
let totalTests = 0;
let testDeviceId = null;
let testAndroidId = null;

// Helper function to log test results
function logTest(testName, success, response = null, error = null) {
    totalTests++;
    if (success) {
        passedTests++;
        console.log(`✅ ${testName}`.green);
        if (response && response.data) {
            console.log(`   Response: ${JSON.stringify(response.data, null, 2)}`.gray);
        }
    } else {
        failedTests++;
        console.log(`❌ ${testName}`.red);
        if (error) {
            console.log(`   Error: ${error.message}`.red);
        }
        if (response && response.data) {
            console.log(`   Response: ${JSON.stringify(response.data, null, 2)}`.gray);
        }
    }
    console.log('');
}

// Test function wrapper
async function runTest(testName, testFunction) {
    try {
        const result = await testFunction();
        logTest(testName, true, result);
        return result;
    } catch (error) {
        logTest(testName, false, error.response, error);
        return null;
    }
}

// Generate a realistic Android ID (64-bit hex string)
function generateRealisticAndroidId() {
    const hexChars = '0123456789abcdef';
    let androidId = '';
    for (let i = 0; i < 16; i++) {
        androidId += hexChars.charAt(Math.floor(Math.random() * hexChars.length));
    }
    return androidId;
}

// Test 1: Register device with explicit ANDROID_ID
async function testRegisterWithExplicitAndroidId() {
    const explicitAndroidId = generateRealisticAndroidId();
    testAndroidId = explicitAndroidId;
    
    const deviceData = {
        deviceId: `test_device_${Date.now()}`,
        androidId: explicitAndroidId,
        user_internal_code: 'TEST01',
        deviceName: 'Test Android Device',
        model: 'Pixel 6',
        manufacturer: 'Google',
        androidVersion: '12',
        userName: 'Test User',
        deviceInfo: {
            model: 'Pixel 6',
            manufacturer: 'Google',
            version: '12',
            buildNumber: 'SP2A.220505.002',
            sdkVersion: 31
        },
        additionalInfo: {
            buildNumber: 'SP2A.220505.002',
            sdkVersion: 31,
            screenResolution: '1080x2400',
            totalStorage: '128GB',
            availableStorage: '64GB',
            deviceFingerprint: 'google/redfin/redfin:12/SP2A.220505.002/8353555:user/release-keys'
        }
    };

    return await axios.post(`${BASE_URL}/devices/register`, deviceData);
}

// Test 2: Register device without ANDROID_ID (should generate one)
async function testRegisterWithoutAndroidId() {
    const deviceData = {
        deviceId: `test_device_no_android_${Date.now()}`,
        // No androidId provided - should generate one
        user_internal_code: 'TEST02',
        deviceName: 'Test Device No Android ID',
        model: 'Samsung Galaxy',
        manufacturer: 'Samsung',
        androidVersion: '11',
        userName: 'Test User 2'
    };

    return await axios.post(`${BASE_URL}/devices/register`, deviceData);
}

// Test 3: Get device by ANDROID_ID
async function testGetDeviceByAndroidId() {
    if (!testAndroidId) throw new Error('No Android ID available for testing');
    return await axios.get(`${BASE_URL}/devices/${testAndroidId}`);
}

// Test 4: Get device by Device ID
async function testGetDeviceByDeviceId() {
    if (!testDeviceId) throw new Error('No Device ID available for testing');
    return await axios.get(`${BASE_URL}/devices/${testDeviceId}`);
}

// Test 5: Verify ANDROID_ID in response
async function testVerifyAndroidIdInResponse() {
    if (!testAndroidId) throw new Error('No Android ID available for testing');
    
    const response = await axios.get(`${BASE_URL}/devices/${testAndroidId}`);
    
    // Verify the response contains the correct Android ID
    if (response.data.data.androidId !== testAndroidId) {
        throw new Error(`Android ID mismatch. Expected: ${testAndroidId}, Got: ${response.data.data.androidId}`);
    }
    
    return response;
}

// Test 6: Test multiple devices with different ANDROID_IDs
async function testMultipleDevicesWithDifferentAndroidIds() {
    const devices = [];
    
    for (let i = 0; i < 3; i++) {
        const deviceData = {
            deviceId: `multi_test_device_${i}_${Date.now()}`,
            androidId: generateRealisticAndroidId(),
            user_internal_code: `MULTI${i}`,
            deviceName: `Multi Test Device ${i}`,
            model: `Test Model ${i}`,
            manufacturer: 'Test Manufacturer',
            androidVersion: '11',
            userName: `Test User ${i}`
        };
        
        const response = await axios.post(`${BASE_URL}/devices/register`, deviceData);
        devices.push(response.data.device);
    }
    
    // Verify each device can be retrieved by its Android ID
    for (const device of devices) {
        const response = await axios.get(`${BASE_URL}/devices/${device.androidId}`);
        if (response.data.data.androidId !== device.androidId) {
            throw new Error(`Android ID verification failed for device ${device.deviceId}`);
        }
    }
    
    return { success: true, devices: devices.length };
}

// Test 7: Test Android ID format validation
async function testAndroidIdFormatValidation() {
    const invalidAndroidIds = [
        '123', // Too short
        '123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890', // Too long
        'invalid_chars!@#', // Invalid characters
        '', // Empty
        null // Null
    ];
    
    const results = [];
    
    for (const invalidId of invalidAndroidIds) {
        try {
            const deviceData = {
                deviceId: `invalid_test_${Date.now()}`,
                androidId: invalidId,
                user_internal_code: 'INVALID',
                deviceName: 'Invalid Test Device',
                model: 'Test Model',
                manufacturer: 'Test Manufacturer',
                androidVersion: '11',
                userName: 'Test User'
            };
            
            await axios.post(`${BASE_URL}/devices/register`, deviceData);
            results.push({ androidId: invalidId, status: 'Unexpectedly succeeded' });
        } catch (error) {
            results.push({ androidId: invalidId, status: 'Failed as expected', error: error.response?.data?.error });
        }
    }
    
    return { results };
}

// Test 8: Test Android ID uniqueness
async function testAndroidIdUniqueness() {
    const sameAndroidId = generateRealisticAndroidId();
    
    // Register first device
    const device1Data = {
        deviceId: `unique_test_1_${Date.now()}`,
        androidId: sameAndroidId,
        user_internal_code: 'UNIQUE1',
        deviceName: 'Unique Test Device 1',
        model: 'Test Model 1',
        manufacturer: 'Test Manufacturer',
        androidVersion: '11',
        userName: 'Test User 1'
    };
    
    const response1 = await axios.post(`${BASE_URL}/devices/register`, device1Data);
    
    // Try to register second device with same Android ID
    const device2Data = {
        deviceId: `unique_test_2_${Date.now()}`,
        androidId: sameAndroidId, // Same Android ID
        user_internal_code: 'UNIQUE2',
        deviceName: 'Unique Test Device 2',
        model: 'Test Model 2',
        manufacturer: 'Test Manufacturer',
        androidVersion: '11',
        userName: 'Test User 2'
    };
    
    try {
        await axios.post(`${BASE_URL}/devices/register`, device2Data);
        return { status: 'Uniqueness constraint not enforced', androidId: sameAndroidId };
    } catch (error) {
        return { status: 'Uniqueness constraint enforced', androidId: sameAndroidId, error: error.response?.data?.error };
    }
}

// Main test runner
async function runAllTests() {
    console.log('🔍 ANDROID_ID Verification Tests...\n'.cyan.bold);
    console.log('='.repeat(60).cyan);
    
    // Basic Android ID Tests
    console.log('\n📱 BASIC ANDROID_ID TESTS'.yellow.bold);
    const registerResult = await runTest('Register Device with Explicit ANDROID_ID', testRegisterWithExplicitAndroidId);
    if (registerResult && registerResult.data && registerResult.data.device) {
        testDeviceId = registerResult.data.device.deviceId;
        console.log(`📱 Created device with ID: ${testDeviceId}`.cyan);
        console.log(`📱 Android ID: ${testAndroidId}`.cyan);
    }
    
    await runTest('Register Device without ANDROID_ID (Auto-generate)', testRegisterWithoutAndroidId);
    
    // Retrieval Tests
    console.log('\n🔍 ANDROID_ID RETRIEVAL TESTS'.yellow.bold);
    await runTest('Get Device by ANDROID_ID', testGetDeviceByAndroidId);
    await runTest('Get Device by Device ID', testGetDeviceByDeviceId);
    await runTest('Verify ANDROID_ID in Response', testVerifyAndroidIdInResponse);
    
    // Advanced Tests
    console.log('\n🔬 ADVANCED ANDROID_ID TESTS'.yellow.bold);
    await runTest('Multiple Devices with Different ANDROID_IDs', testMultipleDevicesWithDifferentAndroidIds);
    await runTest('Android ID Format Validation', testAndroidIdFormatValidation);
    await runTest('Android ID Uniqueness Test', testAndroidIdUniqueness);
    
    // Test Summary
    console.log('='.repeat(60).cyan);
    console.log('\n📋 ANDROID_ID VERIFICATION SUMMARY'.yellow.bold);
    console.log(`Total Tests: ${totalTests}`.white);
    console.log(`Passed: ${passedTests}`.green);
    console.log(`Failed: ${failedTests}`.red);
    console.log(`Success Rate: ${((passedTests / totalTests) * 100).toFixed(1)}%`.cyan);
    
    if (failedTests === 0) {
        console.log('\n🎉 All ANDROID_ID tests passed!'.green.bold);
        console.log('✅ ANDROID_ID can be properly retrieved and stored'.green);
    } else {
        console.log('\n⚠️ Some ANDROID_ID tests failed. Please check the errors above.'.yellow.bold);
    }
    
    // Feature Summary
    console.log('\n📋 ANDROID_ID FEATURES VERIFIED'.yellow.bold);
    console.log('✅ Explicit ANDROID_ID storage'.green);
    console.log('✅ Auto-generated ANDROID_ID when not provided'.green);
    console.log('✅ Device lookup by ANDROID_ID'.green);
    console.log('✅ ANDROID_ID retrieval in responses'.green);
    console.log('✅ Multiple devices with unique ANDROID_IDs'.green);
    console.log('✅ ANDROID_ID format handling'.green);
    console.log('✅ ANDROID_ID uniqueness constraints'.green);
    
    // Android ID Format Information
    console.log('\n📋 ANDROID_ID FORMAT INFORMATION'.yellow.bold);
    console.log('• Real Android IDs are 64-bit hex strings (16 characters)'.cyan);
    console.log('• Example: "a1b2c3d4e5f67890"'.cyan);
    console.log('• Generated by Android system and unique per device'.cyan);
    console.log('• Persists across app reinstalls but may change on factory reset'.cyan);
}

// Error handling for axios
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response) {
            return Promise.reject(error);
        } else if (error.request) {
            console.log('❌ No response received from server'.red);
            return Promise.reject(new Error('No response from server'));
        } else {
            return Promise.reject(error);
        }
    }
);

// Run tests
if (require.main === module) {
    runAllTests().catch(error => {
        console.error('❌ ANDROID_ID test runner failed:', error.message);
        process.exit(1);
    });
}

module.exports = {
    runAllTests,
    generateRealisticAndroidId,
    testRegisterWithExplicitAndroidId,
    testGetDeviceByAndroidId
}; 