const axios = require('axios');
const colors = require('colors');

// Configuration
const BASE_URL = 'http://localhost:5002/api';
const TEST_DEVICE_ID = 'test_device_' + Date.now();

// Test results tracking
let passedTests = 0;
let failedTests = 0;
let totalTests = 0;

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

// Test data
const testContact = {
    name: 'Test Contact',
    phoneNumber: '+1234567890',
    phoneType: 'MOBILE',
    emails: ['test@example.com'],
    organization: 'Test Company'
};

const testCallLog = {
    phoneNumber: '+1234567890',
    contactName: 'Test Contact',
    callType: 'INCOMING',
    timestamp: new Date().toISOString(),
    duration: 60
};

const testNotification = {
    title: 'Test Notification',
    text: 'This is a test notification',
    packageName: 'com.test.app',
    timestamp: new Date().toISOString()
};

const testEmailAccount = {
    email: 'test@example.com',
    type: 'GMAIL',
    isActive: true,
    lastSyncTime: new Date().toISOString()
};

// API Tests
async function testHealthCheck() {
    return await axios.get(`${BASE_URL}/health`);
}

async function testAdminHealth() {
    return await axios.get(`${BASE_URL}/admin/health`);
}

async function testUserHealth() {
    return await axios.get(`${BASE_URL}/user/health`);
}

async function testDeviceRegistration() {
    return await axios.post(`${BASE_URL}/devices/register`, {
        deviceId: TEST_DEVICE_ID,
        androidId: 'test_android_id',
        deviceInfo: {
            platform: 'Android',
            version: '11',
            model: 'Test Device'
        }
    });
}

async function testGetAllDevices() {
    return await axios.get(`${BASE_URL}/devices`);
}

async function testGetDeviceById() {
    return await axios.get(`${BASE_URL}/devices/${TEST_DEVICE_ID}`);
}

async function testUpdateDeviceStatus() {
    return await axios.put(`${BASE_URL}/devices/${TEST_DEVICE_ID}/status`, {
        isActive: true
    });
}

async function testUpdateDeviceSettings() {
    return await axios.put(`${BASE_URL}/devices/${TEST_DEVICE_ID}/settings`, {
        syncInterval: 300,
        autoSync: true
    });
}

async function testSyncContacts() {
    return await axios.post(`${BASE_URL}/devices/${TEST_DEVICE_ID}/sync`, {
        dataType: 'CONTACTS',
        data: [testContact]
    });
}

async function testSyncCallLogs() {
    return await axios.post(`${BASE_URL}/devices/${TEST_DEVICE_ID}/sync`, {
        dataType: 'CALL_LOGS',
        data: [testCallLog]
    });
}

async function testSyncNotifications() {
    return await axios.post(`${BASE_URL}/devices/${TEST_DEVICE_ID}/sync`, {
        dataType: 'NOTIFICATIONS',
        data: [testNotification]
    });
}

async function testSyncEmailAccounts() {
    return await axios.post(`${BASE_URL}/devices/${TEST_DEVICE_ID}/sync`, {
        dataType: 'EMAIL_ACCOUNTS',
        data: [testEmailAccount]
    });
}

async function testSyncMessagesDisabled() {
    return await axios.post(`${BASE_URL}/devices/${TEST_DEVICE_ID}/sync`, {
        dataType: 'MESSAGES',
        data: [{ text: 'test message' }]
    });
}

async function testGetContacts() {
    return await axios.get(`${BASE_URL}/contacts/${TEST_DEVICE_ID}`);
}

async function testGetCallLogs() {
    return await axios.get(`${BASE_URL}/calllogs/${TEST_DEVICE_ID}`);
}

async function testGetNotifications() {
    return await axios.get(`${BASE_URL}/notifications/${TEST_DEVICE_ID}`);
}

async function testGetEmailAccounts() {
    return await axios.get(`${BASE_URL}/emailaccounts/${TEST_DEVICE_ID}`);
}

async function testGetMessages() {
    return await axios.get(`${BASE_URL}/messages/${TEST_DEVICE_ID}`);
}

async function testFixIndexes() {
    return await axios.post(`${BASE_URL}/fix-indexes`);
}

async function testGetGlobalStats() {
    return await axios.get(`${BASE_URL}/client/stats`);
}

async function testGetDeviceStats() {
    return await axios.get(`${BASE_URL}/client/devices/${TEST_DEVICE_ID}`);
}

async function testGetLiveData() {
    return await axios.get(`${BASE_URL}/data/contacts?page=1&limit=10`);
}

async function testUploadLast5Images() {
    return await axios.post(`${BASE_URL}/test/devices/${TEST_DEVICE_ID}/upload-last-5-images`, {
        images: ['test1.jpg', 'test2.jpg']
    });
}

async function testTestSync() {
    return await axios.post(`${BASE_URL}/test/devices/${TEST_DEVICE_ID}/test-sync`, {
        dataType: 'CONTACTS'
    });
}

async function testGetSyncStats() {
    return await axios.get(`${BASE_URL}/client/devices/${TEST_DEVICE_ID}/sync-stats`);
}

// Main test runner
async function runAllTests() {
    console.log('🚀 Starting API Tests...\n'.cyan.bold);
    console.log('='.repeat(60).cyan);
    
    // Health Checks
    console.log('\n🏥 HEALTH CHECKS'.yellow.bold);
    await runTest('Main Health Check', testHealthCheck);
    await runTest('Admin Health Check', testAdminHealth);
    await runTest('User Health Check', testUserHealth);
    
    // Device Management
    console.log('\n📱 DEVICE MANAGEMENT'.yellow.bold);
    await runTest('Device Registration', testDeviceRegistration);
    await runTest('Get All Devices', testGetAllDevices);
    await runTest('Get Device by ID', testGetDeviceById);
    await runTest('Update Device Status', testUpdateDeviceStatus);
    await runTest('Update Device Settings', testUpdateDeviceSettings);
    
    // Data Sync Tests
    console.log('\n🔄 DATA SYNC TESTS'.yellow.bold);
    await runTest('Sync Contacts', testSyncContacts);
    await runTest('Sync Call Logs', testSyncCallLogs);
    await runTest('Sync Notifications', testSyncNotifications);
    await runTest('Sync Email Accounts', testSyncEmailAccounts);
    await runTest('Sync Messages (Should be disabled)', testSyncMessagesDisabled);
    
    // Data Retrieval Tests
    console.log('\n📊 DATA RETRIEVAL TESTS'.yellow.bold);
    await runTest('Get Contacts', testGetContacts);
    await runTest('Get Call Logs', testGetCallLogs);
    await runTest('Get Notifications', testGetNotifications);
    await runTest('Get Email Accounts', testGetEmailAccounts);
    await runTest('Get Messages', testGetMessages);
    
    // Admin Functions
    console.log('\n⚙️ ADMIN FUNCTIONS'.yellow.bold);
    await runTest('Fix Database Indexes', testFixIndexes);
    await runTest('Get Global Stats', testGetGlobalStats);
    await runTest('Get Device Stats', testGetDeviceStats);
    await runTest('Get Live Data', testGetLiveData);
    await runTest('Upload Last 5 Images', testUploadLast5Images);
    await runTest('Test Sync', testTestSync);
    await runTest('Get Sync Stats', testGetSyncStats);
    
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
}

// Error handling for axios
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response) {
            // Server responded with error status
            return Promise.reject(error);
        } else if (error.request) {
            // Request was made but no response received
            console.log('❌ No response received from server'.red);
            return Promise.reject(new Error('No response from server'));
        } else {
            // Something else happened
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
    testHealthCheck,
    testDeviceRegistration,
    testSyncContacts,
    testSyncCallLogs,
    testSyncNotifications,
    testSyncEmailAccounts
}; 