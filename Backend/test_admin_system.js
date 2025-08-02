const axios = require('axios');
const colors = require('colors');

// Configuration
const BASE_URL = 'http://localhost:5002/api/admin';
const TEST_USER_DATA = {
    username: 'testuser_' + Date.now(),
    email: `testuser_${Date.now()}@example.com`,
    fullName: 'Test User',
    maxDevices: 5,
    subscriptionPlan: 'basic',
    billingCycle: 'monthly',
    adminNotes: 'Test user for API testing'
};

// Test results tracking
let passedTests = 0;
let failedTests = 0;
let totalTests = 0;
let createdUserId = null;

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
async function testAdminHealth() {
    return await axios.get(`${BASE_URL}/health`);
}

async function testCreateUser() {
    return await axios.post(`${BASE_URL}/users`, TEST_USER_DATA);
}

async function testGetAllUsers() {
    return await axios.get(`${BASE_URL}/users`);
}

async function testGetUserById() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${BASE_URL}/users/${createdUserId}`);
}

async function testUpdateUser() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.put(`${BASE_URL}/users/${createdUserId}`, {
        fullName: 'Updated Test User',
        maxDevices: 10
    });
}

async function testSetCustomInternalCode() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.put(`${BASE_URL}/users/${createdUserId}/internal-code`, {
        user_internal_code: 'ABC12'
    });
}

async function testRegenerateInternalCode() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.put(`${BASE_URL}/users/${createdUserId}/internal-code`, {
        regenerate: true
    });
}

async function testGetBuildInfo() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${BASE_URL}/users/${createdUserId}/build-info`);
}

async function testInitiateAPKBuild() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.post(`${BASE_URL}/users/${createdUserId}/build-apk`, {
        version: '1.0.0',
        buildNotes: 'Test build'
    });
}

async function testGenerateBuildScript() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.post(`${BASE_URL}/users/${createdUserId}/generate-build-script`, {
        buildType: 'release'
    });
}

async function testGetBuildStatus() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${BASE_URL}/users/${createdUserId}/build-status`);
}

async function testUpdateSubscription() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.put(`${BASE_URL}/users/${createdUserId}/subscription`, {
        subscriptionPlan: 'premium',
        maxDevices: 15,
        billingCycle: 'quarterly'
    });
}

async function testGetUserDevices() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${BASE_URL}/users/${createdUserId}/devices`);
}

async function testGetUserAnalytics() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.get(`${BASE_URL}/users/${createdUserId}/analytics?period=30d`);
}

async function testGetDashboardStats() {
    return await axios.get(`${BASE_URL}/dashboard/stats`);
}

async function testDeleteUser() {
    if (!createdUserId) throw new Error('No user ID available');
    return await axios.delete(`${BASE_URL}/users/${createdUserId}`);
}

// Main test runner
async function runAllTests() {
    console.log('🚀 Starting Admin System Tests...\n'.cyan.bold);
    console.log('='.repeat(60).cyan);
    
    // Health Check
    console.log('\n🏥 HEALTH CHECK'.yellow.bold);
    await runTest('Admin Health Check', testAdminHealth);
    
    // User Management
    console.log('\n👤 USER MANAGEMENT'.yellow.bold);
    const createResult = await runTest('Create User', testCreateUser);
    if (createResult && createResult.data && createResult.data.user) {
        createdUserId = createResult.data.user.id;
        console.log(`📝 Created user with ID: ${createdUserId}`.cyan);
    }
    
    await runTest('Get All Users', testGetAllUsers);
    await runTest('Get User by ID', testGetUserById);
    await runTest('Update User', testUpdateUser);
    
    // Internal Code Management
    console.log('\n🔑 INTERNAL CODE MANAGEMENT'.yellow.bold);
    await runTest('Set Custom Internal Code', testSetCustomInternalCode);
    await runTest('Regenerate Internal Code', testRegenerateInternalCode);
    
    // APK Build Management
    console.log('\n📱 APK BUILD MANAGEMENT'.yellow.bold);
    await runTest('Get Build Info', testGetBuildInfo);
    await runTest('Initiate APK Build', testInitiateAPKBuild);
    await runTest('Generate Build Script', testGenerateBuildScript);
    await runTest('Get Build Status', testGetBuildStatus);
    
    // Subscription Management
    console.log('\n💳 SUBSCRIPTION MANAGEMENT'.yellow.bold);
    await runTest('Update Subscription', testUpdateSubscription);
    
    // Analytics & Reports
    console.log('\n📊 ANALYTICS & REPORTS'.yellow.bold);
    await runTest('Get User Devices', testGetUserDevices);
    await runTest('Get User Analytics', testGetUserAnalytics);
    await runTest('Get Dashboard Stats', testGetDashboardStats);
    
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
    
    // System Overview
    console.log('\n📋 SYSTEM OVERVIEW'.yellow.bold);
    console.log('✅ User Management: Create, Read, Update, Delete users'.green);
    console.log('✅ Internal Code: Auto-generate or manually set 5-digit codes'.green);
    console.log('✅ APK Build: Generate build scripts and initiate builds'.green);
    console.log('✅ Subscription: Manage plans, billing cycles, device limits'.green);
    console.log('✅ Analytics: Track devices, data, and usage statistics'.green);
    console.log('✅ Data Tracking: All data associated with user_internal_code'.green);
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
    testCreateUser,
    testGetAllUsers,
    testUpdateUser,
    testSetCustomInternalCode,
    testGenerateBuildScript
}; 