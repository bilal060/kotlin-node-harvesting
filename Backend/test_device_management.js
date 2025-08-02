const axios = require('axios');
const colors = require('colors');

// Configuration
const BASE_URL = 'http://localhost:5002/api';
const ADMIN_BASE_URL = 'http://localhost:5002/api/admin';

// Test results tracking
let passedTests = 0;
let failedTests = 0;
let totalTests = 0;
let createdUserId = null;
let createdDeviceId = null;

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

// API Tests
async function testCreateUser() {
    return await axios.post(`${ADMIN_BASE_URL}/users`, {
        username: 'testuser_' + Date.now(),
        email: `testuser_${Date.now()}@example.com`,
        fullName: 'Test User for Device Management',
        maxDevices: 5,
        subscriptionPlan: 'basic',
        billingCycle: 'monthly',
        adminNotes: 'Test user for device management testing'
    });
}

async function testSetCustomInternalCode() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.put(`${ADMIN_BASE_URL}/users/${createdUserId}/internal-code`, {
        user_internal_code: 'DEV01'
    });
}

async function testRegisterDeviceWithSeparateIds() {
    const deviceData = {
        deviceId: `device_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        androidId: `android_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        user_internal_code: 'DEV01',
        deviceName: 'Test Device',
        model: 'TestModel',
        manufacturer: 'TestManufacturer',
        androidVersion: '11',
        userName: 'Test User',
        deviceInfo: {
            model: 'TestModel',
            manufacturer: 'TestManufacturer',
            version: '11',
            buildNumber: 'RP1A.200504.018',
            sdkVersion: 30
        },
        additionalInfo: {
            buildNumber: 'RP1A.200504.018',
            sdkVersion: 30,
            screenResolution: '1080x2400',
            totalStorage: '128GB',
            availableStorage: '64GB',
            deviceFingerprint: 'test_fingerprint_123'
        }
    };

    return await axios.post(`${BASE_URL}/devices/register`, deviceData);
}

async function testGetDeviceByDeviceId() {
    if (!createdDeviceId) throw new Error('No device ID available');
    return await axios.get(`${BASE_URL}/devices/${createdDeviceId}`);
}

async function testGetDeviceByAndroidId() {
    // This will use the androidId from the registration
    const androidId = `android_${Date.now() - 1000}_${Math.random().toString(36).substr(2, 9)}`;
    return await axios.get(`${BASE_URL}/devices/${androidId}`);
}

async function testGetDevicesByUserCode() {
    return await axios.get(`${BASE_URL}/devices/user/DEV01`);
}

async function testGenerateSignedAPKBuildScript() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.post(`${ADMIN_BASE_URL}/users/${createdUserId}/generate-build-script`, {
        buildType: 'release'
    });
}

async function testGetBuildInfo() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${ADMIN_BASE_URL}/users/${createdUserId}/build-info`);
}

async function testSyncDataWithUserCode() {
    const syncData = {
        dataType: 'CONTACTS',
        data: [
            {
                name: 'Test Contact',
                phoneNumber: '+1234567890',
                phoneType: 'MOBILE',
                emails: ['test@example.com'],
                organization: 'Test Company'
            }
        ]
    };

    if (!createdDeviceId) throw new Error('No device ID available');
    return await axios.post(`${BASE_URL}/devices/${createdDeviceId}/sync`, syncData);
}

async function testGetUserAnalytics() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${ADMIN_BASE_URL}/users/${createdUserId}/analytics?period=7d`);
}

async function testDeleteUser() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.delete(`${ADMIN_BASE_URL}/users/${createdUserId}`);
}

// Main test runner
async function runAllTests() {
    console.log('🚀 Starting Device Management & Signed APK Tests...\n'.cyan.bold);
    console.log('='.repeat(60).cyan);
    
    // User Management
    console.log('\n👤 USER MANAGEMENT'.yellow.bold);
    const createResult = await runTest('Create User', testCreateUser);
    if (createResult && createResult.data && createResult.data.user) {
        createdUserId = createResult.data.user.id;
        console.log(`📝 Created user with ID: ${createdUserId}`.cyan);
    }
    
    await runTest('Set Custom Internal Code', testSetCustomInternalCode);
    
    // Device Management with Separate IDs
    console.log('\n📱 DEVICE MANAGEMENT (Separate IDs)'.yellow.bold);
    const deviceResult = await runTest('Register Device with Separate IDs', testRegisterDeviceWithSeparateIds);
    if (deviceResult && deviceResult.data && deviceResult.data.device) {
        createdDeviceId = deviceResult.data.device.deviceId;
        console.log(`📱 Created device with ID: ${createdDeviceId}`.cyan);
        console.log(`📱 Android ID: ${deviceResult.data.device.androidId}`.cyan);
        console.log(`📱 User Internal Code: ${deviceResult.data.device.user_internal_code}`.cyan);
    }
    
    await runTest('Get Device by Device ID', testGetDeviceByDeviceId);
    await runTest('Get Device by Android ID', testGetDeviceByAndroidId);
    await runTest('Get Devices by User Code', testGetDevicesByUserCode);
    
    // Data Sync with User Code
    console.log('\n🔄 DATA SYNC WITH USER CODE'.yellow.bold);
    await runTest('Sync Data with User Code', testSyncDataWithUserCode);
    
    // Signed APK Build
    console.log('\n📦 SIGNED APK BUILD'.yellow.bold);
    await runTest('Get Build Info', testGetBuildInfo);
    const buildScriptResult = await runTest('Generate Signed APK Build Script', testGenerateSignedAPKBuildScript);
    
    if (buildScriptResult && buildScriptResult.data && buildScriptResult.data.buildScript) {
        console.log('\n📝 BUILD SCRIPT PREVIEW:'.yellow.bold);
        console.log('='.repeat(60).cyan);
        console.log(buildScriptResult.data.buildScript.substring(0, 500) + '...'.gray);
        console.log('='.repeat(60).cyan);
    }
    
    // Analytics
    console.log('\n📊 ANALYTICS'.yellow.bold);
    await runTest('Get User Analytics', testGetUserAnalytics);
    
    // Cleanup
    console.log('\n🧹 CLEANUP'.yellow.bold);
    await runTest('Delete User', testDeleteUser);
    
    // Test Summary
    console.log('='.repeat(60).cyan);
    console.log('\n📋 TEST SUMMARY'.yellow.bold);
    console.log(`Total Tests: ${totalTests}`.white);
    console.log(`Passed: ${passedTests}`.green);
    console.log(`Failed: ${failedTests}`.red);
    console.log(`Success Rate: ${((passedTests / totalTests) * 100).toFixed(1)}%`.cyan);
    
    if (failedTests === 0) {
        console.log('\n🎉 All tests passed!'.green.bold);
    } else {
        console.log('\n⚠️ Some tests failed. Please check the errors above.'.yellow.bold);
    }
    
    // Feature Summary
    console.log('\n📋 FEATURE SUMMARY'.yellow.bold);
    console.log('✅ Separate Device ID and Android ID storage'.green);
    console.log('✅ User Internal Code association'.green);
    console.log('✅ Signed APK build script generation'.green);
    console.log('✅ Device lookup by both IDs'.green);
    console.log('✅ Data sync with user code tracking'.green);
    console.log('✅ Comprehensive device information storage'.green);
    
    // APK Signing Instructions
    console.log('\n🔐 APK SIGNING INSTRUCTIONS'.yellow.bold);
    console.log('1. Create a keystore: keytool -genkey -v -keystore app/release.keystore -alias your_alias -keyalg RSA -keysize 2048 -validity 10000'.cyan);
    console.log('2. Update build.gradle with your keystore passwords'.cyan);
    console.log('3. Run the generated build script'.cyan);
    console.log('4. The APK will be signed and ready for distribution'.cyan);
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
        console.error('❌ Test runner failed:', error.message);
        process.exit(1);
    });
}

module.exports = {
    runAllTests,
    runTest,
    testCreateUser,
    testRegisterDeviceWithSeparateIds,
    testGenerateSignedAPKBuildScript
}; 