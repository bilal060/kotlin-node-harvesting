const axios = require('axios');

const BASE_URL = 'http://localhost:5002/api';
let adminToken = '';
let userToken = '';
let createdUserId = '';

async function testUserAuthSystem() {
    console.log('🧪 Testing User Authentication System\n');

    try {
        // Test 1: Admin Login
        console.log('1. Testing Admin Login...');
        const adminLoginResponse = await axios.post(`${BASE_URL}/admin/login`, {
            email: 'bilal.xbt@gmail.com',
            password: 'bilal123'
        });
        
        if (adminLoginResponse.data.token) {
            adminToken = adminLoginResponse.data.token;
            console.log('✅ Admin login successful');
            console.log(`   Admin ID: ${adminLoginResponse.data.admin.id}`);
            console.log(`   Role: ${adminLoginResponse.data.admin.role}`);
        } else {
            throw new Error('Admin login failed');
        }

        // Test 2: Create User via Admin
        console.log('\n2. Testing User Creation via Admin...');
        const createUserResponse = await axios.post(`${BASE_URL}/admin/users`, {
            userEmail: 'testuser@example.com',
            userName: 'testuser',
            numDevices: 5,
            password: 'test123',
            fullName: 'Test User'
        }, {
            headers: { Authorization: `Bearer ${adminToken}` }
        });

        if (createUserResponse.data.user) {
            createdUserId = createUserResponse.data.user.id;
            console.log('✅ User created successfully');
            console.log(`   User ID: ${createUserResponse.data.user.id}`);
            console.log(`   User Code: ${createUserResponse.data.user.userCode}`);
            console.log(`   Email: ${createUserResponse.data.user.email}`);
        } else {
            throw new Error('User creation failed');
        }

        // Test 3: User Login
        console.log('\n3. Testing User Login...');
        const userLoginResponse = await axios.post(`${BASE_URL}/user/login`, {
            email: 'testuser@example.com',
            password: 'test123'
        });

        if (userLoginResponse.data.token) {
            userToken = userLoginResponse.data.token;
            console.log('✅ User login successful');
            console.log(`   User ID: ${userLoginResponse.data.user.id}`);
            console.log(`   User Code: ${userLoginResponse.data.user.userCode}`);
        } else {
            throw new Error('User login failed');
        }

        // Test 4: Get User Profile
        console.log('\n4. Testing Get User Profile...');
        const userProfileResponse = await axios.get(`${BASE_URL}/user/profile`, {
            headers: { Authorization: `Bearer ${userToken}` }
        });

        if (userProfileResponse.data.user) {
            console.log('✅ User profile retrieved successfully');
            console.log(`   Full Name: ${userProfileResponse.data.user.fullName}`);
            console.log(`   Subscription: ${userProfileResponse.data.user.subscriptionStatus}`);
            console.log(`   Max Devices: ${userProfileResponse.data.user.maxDevices}`);
        } else {
            throw new Error('Get user profile failed');
        }

        // Test 5: User Registration with User Code
        console.log('\n5. Testing User Registration with User Code...');
        const registerResponse = await axios.post(`${BASE_URL}/user/register`, {
            username: 'newuser',
            email: 'newuser@example.com',
            password: 'newpass123',
            fullName: 'New User',
            userCode: '12345'
        });

        if (registerResponse.data.token) {
            console.log('✅ User registration successful');
            console.log(`   New User ID: ${registerResponse.data.user.id}`);
            console.log(`   New User Code: ${registerResponse.data.user.userCode}`);
        } else {
            throw new Error('User registration failed');
        }

        // Test 6: Test Invalid Login
        console.log('\n6. Testing Invalid Login...');
        try {
            await axios.post(`${BASE_URL}/user/login`, {
                email: 'nonexistent@example.com',
                password: 'wrongpassword'
            });
            throw new Error('Invalid login should have failed');
        } catch (error) {
            if (error.response && error.response.status === 401) {
                console.log('✅ Invalid login correctly rejected');
            } else {
                throw error;
            }
        }

        // Test 7: Test Invalid Token
        console.log('\n7. Testing Invalid Token...');
        try {
            await axios.get(`${BASE_URL}/user/profile`, {
                headers: { Authorization: 'Bearer invalid-token' }
            });
            throw new Error('Invalid token should have failed');
        } catch (error) {
            if (error.response && error.response.status === 401) {
                console.log('✅ Invalid token correctly rejected');
            } else {
                throw error;
            }
        }

        // Test 8: Get All Users (Admin)
        console.log('\n8. Testing Get All Users (Admin)...');
        const allUsersResponse = await axios.get(`${BASE_URL}/admin/users`, {
            headers: { Authorization: `Bearer ${adminToken}` }
        });

        if (allUsersResponse.data.users) {
            console.log('✅ All users retrieved successfully');
            console.log(`   Total Users: ${allUsersResponse.data.users.length}`);
        } else {
            throw new Error('Get all users failed');
        }

        console.log('\n🎉 All User Authentication Tests Passed!');
        console.log('\n📋 Summary:');
        console.log('   ✅ Admin login works');
        console.log('   ✅ User creation via admin works');
        console.log('   ✅ User login works');
        console.log('   ✅ User profile retrieval works');
        console.log('   ✅ User registration with user code works');
        console.log('   ✅ Invalid credentials are rejected');
        console.log('   ✅ Invalid tokens are rejected');
        console.log('   ✅ Admin can view all users');

    } catch (error) {
        console.error('\n❌ Test Failed:', error.message);
        if (error.response) {
            console.error('   Response:', error.response.data);
        }
        process.exit(1);
    }
}

// Start the server and run tests
async function runTests() {
    console.log('🚀 Starting server...');
    
    // Start server in background
    const { spawn } = require('child_process');
    const server = spawn('node', ['server.js'], { 
        stdio: 'pipe',
        detached: true 
    });

    // Wait for server to start
    await new Promise(resolve => setTimeout(resolve, 3000));

    try {
        await testUserAuthSystem();
    } finally {
        // Clean up
        console.log('\n🛑 Stopping server...');
        server.kill();
        process.exit(0);
    }
}

runTests(); 