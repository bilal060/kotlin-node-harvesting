const axios = require('axios');

const BASE_URL = 'http://localhost:5002/api';

// Test admin login to get token
async function testAdminLogin() {
    try {
        const response = await axios.post(`${BASE_URL}/admin/login`, {
            email: 'bilal.xbt@gmail.com',
            password: 'bilal123'
        });
        
        console.log('Admin login successful');
        return response.data.token;
    } catch (error) {
        console.error('Admin login failed:', error.response?.data || error.message);
        return null;
    }
}

// Test data viewer filters
async function testDataViewerFilters(token) {
    try {
        const response = await axios.get(`${BASE_URL}/admin/data-viewer/filters`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        
        console.log('\n=== Data Viewer Filters ===');
        console.log('Devices:', response.data.devices.length);
        console.log('User Codes:', response.data.userCodes);
        console.log('Data Types:', response.data.dataTypes);
        console.log('Package Names:', response.data.packageNames?.slice(0, 10));
        console.log('Date Range:', response.data.dateRange);
        
        return response.data;
    } catch (error) {
        console.error('Data viewer filters failed:', error.response?.data || error.message);
        return null;
    }
}

// Test data viewer with different data types
async function testDataViewer(token, dataType) {
    try {
        const response = await axios.get(`${BASE_URL}/admin/data-viewer`, {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                dataType: dataType,
                page: 1,
                limit: 10,
                sortBy: 'timestamp',
                sortOrder: 'desc'
            }
        });
        
        console.log(`\n=== Data Viewer - ${dataType} ===`);
        console.log('Total Records:', response.data.pagination.total);
        console.log('Current Page:', response.data.pagination.page);
        console.log('Data Count:', response.data.data.length);
        console.log('Summary:', response.data.summary);
        
        if (response.data.data.length > 0) {
            console.log('Sample Data:', JSON.stringify(response.data.data[0], null, 2));
        }
        
        return response.data;
    } catch (error) {
        console.error(`Data viewer ${dataType} failed:`, error.response?.data || error.message);
        return null;
    }
}

// Test data viewer without dataType (summary)
async function testDataViewerSummary(token) {
    try {
        const response = await axios.get(`${BASE_URL}/admin/data-viewer`, {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                page: 1,
                limit: 10
            }
        });
        
        console.log('\n=== Data Viewer Summary ===');
        console.log('Summary:', response.data.summary);
        console.log('Devices:', response.data.summary.devices.length);
        
        return response.data;
    } catch (error) {
        console.error('Data viewer summary failed:', error.response?.data || error.message);
        return null;
    }
}

// Test with device filter
async function testDataViewerWithDeviceFilter(token, deviceId) {
    try {
        const response = await axios.get(`${BASE_URL}/admin/data-viewer`, {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                deviceId: deviceId,
                dataType: 'notifications',
                page: 1,
                limit: 5
            }
        });
        
        console.log(`\n=== Data Viewer with Device Filter (${deviceId}) ===`);
        console.log('Total Records:', response.data.pagination.total);
        console.log('Data Count:', response.data.data.length);
        
        return response.data;
    } catch (error) {
        console.error('Data viewer with device filter failed:', error.response?.data || error.message);
        return null;
    }
}

// Test with search filter
async function testDataViewerWithSearch(token, searchTerm) {
    try {
        const response = await axios.get(`${BASE_URL}/admin/data-viewer`, {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                search: searchTerm,
                dataType: 'contacts',
                page: 1,
                limit: 5
            }
        });
        
        console.log(`\n=== Data Viewer with Search (${searchTerm}) ===`);
        console.log('Total Records:', response.data.pagination.total);
        console.log('Data Count:', response.data.data.length);
        
        return response.data;
    } catch (error) {
        console.error('Data viewer with search failed:', error.response?.data || error.message);
        return null;
    }
}

// Main test function
async function runTests() {
    console.log('Testing New Data Viewer...\n');
    
    // Test admin login
    const token = await testAdminLogin();
    if (!token) {
        console.log('Cannot proceed without admin token');
        return;
    }
    
    // Test filters
    const filters = await testDataViewerFilters(token);
    if (!filters) {
        console.log('Cannot proceed without filters');
        return;
    }
    
    // Test summary
    await testDataViewerSummary(token);
    
    // Test each data type
    const dataTypes = ['contacts', 'call_logs', 'notifications', 'email_accounts'];
    for (const dataType of dataTypes) {
        await testDataViewer(token, dataType);
    }
    
    // Test with device filter if devices exist
    if (filters.devices.length > 0) {
        await testDataViewerWithDeviceFilter(token, filters.devices[0].deviceId);
    }
    
    // Test with search
    await testDataViewerWithSearch(token, 'test');
    
    console.log('\n=== All Tests Completed ===');
}

// Run tests
runTests().catch(console.error); 