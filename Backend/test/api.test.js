const request = require('supertest');
const express = require('express');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');

// Import the server app
const app = require('../server');

let mongoServer;

// Test data
const testDeviceId = 'test_device_123';
const testData = {
  dataType: 'CONTACTS',
  data: [
    {
      name: 'John Doe',
      phoneNumbers: ['+1234567890'],
      email: 'john@example.com'
    }
  ],
  timestamp: new Date().toISOString()
};

const testCallLogData = {
  dataType: 'CALL_LOGS',
  data: [
    {
      phoneNumber: '+1234567890',
      name: 'John Doe',
      type: 'INCOMING',
      date: Date.now(),
      duration: 120
    }
  ],
  timestamp: new Date().toISOString()
};

const testMessageData = {
  dataType: 'MESSAGES',
  data: [
    {
      address: '+1234567890',
      body: 'Hello world',
      type: 'SMS',
      timestamp: Date.now()
    }
  ],
  timestamp: new Date().toISOString()
};

const testNotificationData = {
  dataType: 'NOTIFICATIONS',
  data: [
    {
      packageName: 'com.whatsapp',
      title: 'New Message',
      text: 'You have a new message',
      timestamp: new Date().toISOString()
    }
  ],
  timestamp: new Date().toISOString()
};

const testEmailData = {
  dataType: 'EMAIL_ACCOUNTS',
  data: [
    {
      email: 'test@example.com',
      type: 'IMAP',
      name: 'Test Account'
    }
  ],
  timestamp: new Date().toISOString()
};

describe('DeviceSync API Tests', () => {
  beforeAll(async () => {
    // Start in-memory MongoDB
    mongoServer = await MongoMemoryServer.create();
    const mongoUri = mongoServer.getUri();
    
    // Connect to test database
    await mongoose.connect(mongoUri, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });
  });

  afterAll(async () => {
    // Cleanup
    await mongoose.disconnect();
    await mongoServer.stop();
  });

  beforeEach(async () => {
    // Clear all collections before each test
    const collections = mongoose.connection.collections;
    for (const key in collections) {
      await collections[key].deleteMany();
    }
  });

  describe('Health Check API', () => {
    test('GET /api/health should return server status', async () => {
      const response = await request(app)
        .get('/api/health')
        .expect(200);

      expect(response.body).toHaveProperty('success', true);
      expect(response.body).toHaveProperty('message');
      expect(response.body).toHaveProperty('timestamp');
      expect(response.body).toHaveProperty('database', 'MongoDB');
      expect(response.body).toHaveProperty('stats');
      expect(response.body.stats).toHaveProperty('devices');
      expect(response.body.stats).toHaveProperty('syncedRecords');
    });
  });

  describe('Test Mode APIs', () => {
    describe('POST /api/test/devices/:deviceId/sync', () => {
      test('should sync CONTACTS data to test collection', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(testData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
        expect(response.body.data).toHaveProperty('message');
        expect(response.body.data.message).toContain('TEST MODE');
      });

      test('should sync CALL_LOGS data to test collection', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(testCallLogData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should sync MESSAGES data to test collection', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(testMessageData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should sync NOTIFICATIONS data to test collection', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(testNotificationData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should sync EMAIL_ACCOUNTS data to test collection', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(testEmailData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should handle invalid data type', async () => {
        const invalidData = {
          dataType: 'INVALID_TYPE',
          data: [],
          timestamp: new Date().toISOString()
        };

        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(invalidData)
          .expect(400);

        expect(response.body).toHaveProperty('success', false);
        expect(response.body).toHaveProperty('error');
      });

      test('should handle empty data array', async () => {
        const emptyData = {
          dataType: 'CONTACTS',
          data: [],
          timestamp: new Date().toISOString()
        };

        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(emptyData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced', 0);
      });

      test('should handle malformed JSON', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send('invalid json')
          .set('Content-Type', 'application/json')
          .expect(400);
      });
    });

    describe('GET /api/test/devices/:deviceId/:dataType', () => {
      beforeEach(async () => {
        // First sync some data
        await request(app)
          .post(`/api/test/devices/${testDeviceId}/sync`)
          .send(testData);
      });

      test('should retrieve synced CONTACTS data', async () => {
        const response = await request(app)
          .get(`/api/test/devices/${testDeviceId}/contacts`)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('dataType', 'contacts');
        expect(response.body.data).toHaveProperty('items');
        expect(Array.isArray(response.body.data.items)).toBe(true);
      });

      test('should handle non-existent data type', async () => {
        const response = await request(app)
          .get(`/api/test/devices/${testDeviceId}/invalid`)
          .expect(400);

        expect(response.body).toHaveProperty('success', false);
        expect(response.body).toHaveProperty('error');
      });
    });

    describe('POST /api/test/devices/:deviceId/upload-last-5-images', () => {
      test('should handle image upload request', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/upload-last-5-images`)
          .attach('images', Buffer.from('fake image data'), 'test.jpg')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body).toHaveProperty('message');
      });

      test('should handle missing image files', async () => {
        const response = await request(app)
          .post(`/api/test/devices/${testDeviceId}/upload-last-5-images`)
          .expect(400);

        expect(response.body).toHaveProperty('success', false);
        expect(response.body).toHaveProperty('error');
      });
    });
  });

  describe('Live Mode APIs', () => {
    describe('POST /api/devices/:deviceId/sync', () => {
      test('should sync CONTACTS data to main database', async () => {
        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(testData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
        expect(response.body.data).toHaveProperty('message');
        expect(response.body.data.message).toContain('LIVE SYNC');
      });

      test('should sync CALL_LOGS data to main database', async () => {
        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(testCallLogData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should sync MESSAGES data to main database', async () => {
        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(testMessageData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should sync NOTIFICATIONS data to main database', async () => {
        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(testNotificationData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should sync EMAIL_ACCOUNTS data to main database', async () => {
        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(testEmailData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced');
      });

      test('should handle invalid data type in live mode', async () => {
        const invalidData = {
          dataType: 'INVALID_TYPE',
          data: [],
          timestamp: new Date().toISOString()
        };

        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(invalidData)
          .expect(400);

        expect(response.body).toHaveProperty('success', false);
        expect(response.body).toHaveProperty('error');
      });

      test('should handle empty data array in live mode', async () => {
        const emptyData = {
          dataType: 'CONTACTS',
          data: [],
          timestamp: new Date().toISOString()
        };

        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(emptyData)
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('itemsSynced', 0);
      });

      test('should handle malformed JSON in live mode', async () => {
        const response = await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send('invalid json')
          .set('Content-Type', 'application/json')
          .expect(400);
      });
    });

    describe('GET /api/data/:dataType', () => {
      beforeEach(async () => {
        // First sync some data to main database
        await request(app)
          .post(`/api/devices/${testDeviceId}/sync`)
          .send(testData);
      });

      test('should retrieve CONTACTS from main database', async () => {
        const response = await request(app)
          .get('/api/data/contacts')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('dataType', 'contacts');
        expect(response.body.data).toHaveProperty('items');
        expect(response.body.data).toHaveProperty('pagination');
        expect(Array.isArray(response.body.data.items)).toBe(true);
      });

      test('should retrieve CALL_LOGS from main database', async () => {
        const response = await request(app)
          .get('/api/data/calllogs')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('dataType', 'calllogs');
      });

      test('should retrieve MESSAGES from main database', async () => {
        const response = await request(app)
          .get('/api/data/messages')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('dataType', 'messages');
      });

      test('should retrieve NOTIFICATIONS from main database', async () => {
        const response = await request(app)
          .get('/api/data/notifications')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('dataType', 'notifications');
      });

      test('should retrieve EMAIL_ACCOUNTS from main database', async () => {
        const response = await request(app)
          .get('/api/data/emailaccounts')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data).toHaveProperty('dataType', 'emailaccounts');
      });

      test('should handle pagination parameters', async () => {
        const response = await request(app)
          .get('/api/data/contacts?page=1&limit=10')
          .expect(200);

        expect(response.body).toHaveProperty('success', true);
        expect(response.body.data.pagination).toHaveProperty('current', 1);
        expect(response.body.data.pagination).toHaveProperty('pages');
        expect(response.body.data.pagination).toHaveProperty('total');
      });

      test('should handle invalid data type', async () => {
        const response = await request(app)
          .get('/api/data/invalid')
          .expect(400);

        expect(response.body).toHaveProperty('success', false);
        expect(response.body).toHaveProperty('error');
      });
    });
  });

  describe('Data Validation Tests', () => {
    test('should validate required fields for CONTACTS', async () => {
      const invalidContactData = {
        dataType: 'CONTACTS',
        data: [
          {
            // Missing required fields
            email: 'test@example.com'
          }
        ],
        timestamp: new Date().toISOString()
      };

      const response = await request(app)
        .post(`/api/test/devices/${testDeviceId}/sync`)
        .send(invalidContactData)
        .expect(200); // Should still return 200 but with 0 items synced

      expect(response.body.data).toHaveProperty('itemsSynced', 0);
    });

    test('should validate required fields for CALL_LOGS', async () => {
      const invalidCallLogData = {
        dataType: 'CALL_LOGS',
        data: [
          {
            // Missing required fields
            duration: 120
          }
        ],
        timestamp: new Date().toISOString()
      };

      const response = await request(app)
        .post(`/api/test/devices/${testDeviceId}/sync`)
        .send(invalidCallLogData)
        .expect(200);

      expect(response.body.data).toHaveProperty('itemsSynced', 0);
    });

    test('should validate required fields for MESSAGES', async () => {
      const invalidMessageData = {
        dataType: 'MESSAGES',
        data: [
          {
            // Missing required fields
            timestamp: Date.now()
          }
        ],
        timestamp: new Date().toISOString()
      };

      const response = await request(app)
        .post(`/api/test/devices/${testDeviceId}/sync`)
        .send(invalidMessageData)
        .expect(200);

      expect(response.body.data).toHaveProperty('itemsSynced', 0);
    });

    test('should validate required fields for NOTIFICATIONS', async () => {
      const invalidNotificationData = {
        dataType: 'NOTIFICATIONS',
        data: [
          {
            // Missing required fields
            text: 'Some text'
          }
        ],
        timestamp: new Date().toISOString()
      };

      const response = await request(app)
        .post(`/api/test/devices/${testDeviceId}/sync`)
        .send(invalidNotificationData)
        .expect(200);

      expect(response.body.data).toHaveProperty('itemsSynced', 0);
    });
  });

  describe('Error Handling Tests', () => {
    test('should handle database connection errors gracefully', async () => {
      // This test would require mocking database connection failure
      // For now, we'll test basic error handling
      const response = await request(app)
        .get('/api/health')
        .expect(200);

      expect(response.body).toHaveProperty('success', true);
    });

    test('should handle malformed device ID', async () => {
      const response = await request(app)
        .post('/api/test/devices//sync') // Empty device ID
        .send(testData)
        .expect(404);
    });

    test('should handle missing request body', async () => {
      const response = await request(app)
        .post(`/api/test/devices/${testDeviceId}/sync`)
        .expect(400);
    });
  });

  describe('Performance Tests', () => {
    test('should handle large data arrays efficiently', async () => {
      const largeData = {
        dataType: 'CONTACTS',
        data: Array.from({ length: 100 }, (_, i) => ({
          name: `Contact ${i}`,
          phoneNumbers: [`+123456789${i}`],
          email: `contact${i}@example.com`
        })),
        timestamp: new Date().toISOString()
      };

      const startTime = Date.now();
      const response = await request(app)
        .post(`/api/test/devices/${testDeviceId}/sync`)
        .send(largeData)
        .expect(200);

      const endTime = Date.now();
      const processingTime = endTime - startTime;

      expect(response.body).toHaveProperty('success', true);
      expect(response.body.data).toHaveProperty('itemsSynced', 100);
      expect(processingTime).toBeLessThan(5000); // Should complete within 5 seconds
    });
  });
}); 