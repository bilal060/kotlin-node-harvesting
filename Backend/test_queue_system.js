const axios = require('axios');
const colors = require('colors');

const BASE_URL = 'http://localhost:5001/api';

// Test configuration
const TEST_DEVICE_ID = 'test_device_001';
const TEST_DATA_TYPES = ['contacts', 'calllogs', 'messages', 'notifications', 'emailaccounts'];

// Generate sample data
function generateSampleData(dataType, count) {
    const data = [];
    for (let i = 0; i < count; i++) {
        const baseItem = {
            id: `test_${dataType}_${i}`,
            timestamp: new Date().toISOString(),
            test: true
        };

        switch (dataType) {
            case 'contacts':
                data.push({
                    ...baseItem,
                    contactId: `contact_${i}`,
                    name: `Test Contact ${i}`,
                    phoneNumber: `+1234567890${i.toString().padStart(2, '0')}`,
                    email: `contact${i}@test.com`
                });
                break;
            case 'calllogs':
                data.push({
                    ...baseItem,
                    callId: `call_${i}`,
                    phoneNumber: `+1234567890${i.toString().padStart(2, '0')}`,
                    type: i % 2 === 0 ? 'incoming' : 'outgoing',
                    duration: Math.floor(Math.random() * 300)
                });
                break;
            case 'messages':
                data.push({
                    ...baseItem,
                    messageId: `msg_${i}`,
                    phoneNumber: `+1234567890${i.toString().padStart(2, '0')}`,
                    type: i % 2 === 0 ? 'inbox' : 'sent',
                    content: `Test message ${i}`
                });
                break;
            case 'notifications':
                data.push({
                    ...baseItem,
                    notificationId: `notif_${i}`,
                    packageName: `com.test.app${i % 5}`,
                    title: `Test Notification ${i}`,
                    content: `Test notification content ${i}`
                });
                break;
            case 'emailaccounts':
                data.push({
                    ...baseItem,
                    email: `user${i}@test.com`,
                    provider: i % 2 === 0 ? 'gmail' : 'outlook',
                    isActive: true
                });
                break;
        }
    }
    return data;
}

// Test queue system
async function testQueueSystem() {
    console.log(colors.cyan('🧪 Testing Queue System'));
    console.log(colors.cyan('========================'));
    console.log('');

    try {
        // Test 1: Small data set (should process immediately)
        console.log(colors.yellow('📊 Test 1: Small data set (100 items) - Should process immediately'));
        const smallData = generateSampleData('contacts', 100);
        
        const smallResponse = await axios.post(`${BASE_URL}/contacts/sync`, {
            deviceId: TEST_DEVICE_ID,
            contacts: smallData
        });
        
        console.log(colors.green('✅ Small data response:'), smallResponse.data);
        console.log('');

        // Test 2: Large data set (should be queued)
        console.log(colors.yellow('📊 Test 2: Large data set (600 items) - Should be queued'));
        const largeData = generateSampleData('calllogs', 600);
        
        const largeResponse = await axios.post(`${BASE_URL}/calllogs/sync`, {
            deviceId: TEST_DEVICE_ID,
            callLogs: largeData
        });
        
        console.log(colors.green('✅ Large data response:'), largeResponse.data);
        console.log('');

        // Test 3: Check queue status
        console.log(colors.yellow('📊 Test 3: Check queue status'));
        const statusResponse = await axios.get(`${BASE_URL}/queue/status`);
        console.log(colors.green('✅ Queue status:'), statusResponse.data);
        console.log('');

        // Test 4: Get queue items
        console.log(colors.yellow('📊 Test 4: Get queue items'));
        const itemsResponse = await axios.get(`${BASE_URL}/queue/items`);
        console.log(colors.green('✅ Queue items:'), itemsResponse.data);
        console.log('');

        // Test 5: Test queue with different data types
        console.log(colors.yellow('📊 Test 5: Test queue with different data types'));
        
        for (const dataType of TEST_DATA_TYPES) {
            const testData = generateSampleData(dataType, 550); // Just over threshold
            
            try {
                const response = await axios.post(`${BASE_URL}/${dataType}/sync`, {
                    deviceId: TEST_DEVICE_ID,
                    [dataType]: testData
                });
                
                console.log(colors.green(`✅ ${dataType} test:`, response.data.message));
            } catch (error) {
                console.log(colors.red(`❌ ${dataType} test failed:`, error.response?.data?.message || error.message));
            }
        }
        console.log('');

        // Test 6: Get detailed statistics
        console.log(colors.yellow('📊 Test 6: Get detailed statistics'));
        const statsResponse = await axios.get(`${BASE_URL}/queue/stats`);
        console.log(colors.green('✅ Queue statistics:'), statsResponse.data);
        console.log('');

        // Test 7: Monitor queue processing
        console.log(colors.yellow('📊 Test 7: Monitor queue processing for 30 seconds'));
        let monitoringTime = 0;
        const monitorInterval = setInterval(async () => {
            try {
                const statusResponse = await axios.get(`${BASE_URL}/queue/status`);
                const stats = statusResponse.data.statistics;
                
                console.log(colors.blue(`⏱️  ${monitoringTime}s - Queue status:`));
                stats.forEach(stat => {
                    console.log(`   ${stat._id}: ${stat.count} items`);
                });
                
                monitoringTime += 5;
                
                if (monitoringTime >= 30) {
                    clearInterval(monitorInterval);
                    console.log(colors.green('✅ Monitoring completed'));
                }
            } catch (error) {
                console.log(colors.red('❌ Error monitoring queue:', error.message));
                clearInterval(monitorInterval);
            }
        }, 5000);

    } catch (error) {
        console.error(colors.red('❌ Test failed:'), error.response?.data || error.message);
    }
}

// Test specific queue item
async function testSpecificQueueItem(queueId) {
    console.log(colors.cyan(`🧪 Testing specific queue item: ${queueId}`));
    console.log(colors.cyan('=========================================='));
    console.log('');

    try {
        const response = await axios.get(`${BASE_URL}/queue/items/${queueId}`);
        console.log(colors.green('✅ Queue item details:'), response.data);
    } catch (error) {
        console.error(colors.red('❌ Error getting queue item:'), error.response?.data || error.message);
    }
}

// Test queue management
async function testQueueManagement() {
    console.log(colors.cyan('🧪 Testing Queue Management'));
    console.log(colors.cyan('============================'));
    console.log('');

    try {
        // Test start queue processing
        console.log(colors.yellow('📊 Test: Start queue processing'));
        const startResponse = await axios.post(`${BASE_URL}/queue/start`);
        console.log(colors.green('✅ Start response:'), startResponse.data);
        console.log('');

        // Test stop queue processing
        console.log(colors.yellow('📊 Test: Stop queue processing'));
        const stopResponse = await axios.post(`${BASE_URL}/queue/stop`);
        console.log(colors.green('✅ Stop response:'), stopResponse.data);
        console.log('');

        // Test clear failed items
        console.log(colors.yellow('📊 Test: Clear failed items'));
        const clearResponse = await axios.delete(`${BASE_URL}/queue/items/failed`);
        console.log(colors.green('✅ Clear response:'), clearResponse.data);
        console.log('');

    } catch (error) {
        console.error(colors.red('❌ Queue management test failed:'), error.response?.data || error.message);
    }
}

// Main execution
async function main() {
    const args = process.argv.slice(2);
    
    if (args.length === 0) {
        await testQueueSystem();
    } else if (args[0] === 'item' && args[1]) {
        await testSpecificQueueItem(args[1]);
    } else if (args[0] === 'manage') {
        await testQueueManagement();
    } else {
        console.log(colors.yellow('Usage:'));
        console.log('  node test_queue_system.js                    - Run full queue system test');
        console.log('  node test_queue_system.js item <queueId>     - Test specific queue item');
        console.log('  node test_queue_system.js manage             - Test queue management');
    }
}

// Run the test
main().catch(console.error); 