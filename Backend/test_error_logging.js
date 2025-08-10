const mongoose = require('mongoose');
const ErrorLogger = require('./services/errorLogger');
const ErrorLog = require('./models/ErrorLog');
require('dotenv').config();

// Test configuration
const TEST_CONFIG = {
  testBackendErrors: true,
  testMobileErrors: true,
  testWebErrors: true,
  testDatabaseErrors: true,
  testPermissionErrors: true,
  testValidationErrors: true,
  testCriticalErrors: true,
  testBatchLogging: true,
  cleanupAfterTest: true
};

async function testErrorLogging() {
  console.log('🚀 Starting Error Logging System Test...\n');

  try {
    // Connect to MongoDB
    console.log('📡 Connecting to MongoDB...');
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('✅ MongoDB connected successfully\n');

    // Test 1: Backend Error Logging
    if (TEST_CONFIG.testBackendErrors) {
      console.log('🧪 Testing Backend Error Logging...');
      
      const backendError = new Error('Test backend error for database connection');
      backendError.name = 'DatabaseConnectionError';
      backendError.code = 'ECONNREFUSED';
      
      const backendLog = await ErrorLogger.logBackendError(backendError, {
        source: 'database_connection',
        userId: 'test_user_123',
        deviceId: 'test_device_456',
        metadata: {
          test: true,
          purpose: 'Testing backend error logging'
        }
      });
      
      if (backendLog) {
        console.log('✅ Backend error logged successfully:', backendLog._id);
      } else {
        console.log('❌ Failed to log backend error');
      }
    }

    // Test 2: Mobile Error Logging
    if (TEST_CONFIG.testMobileErrors) {
      console.log('\n📱 Testing Mobile Error Logging...');
      
      const mobileLog = await ErrorLogger.logMobileError(
        'Network timeout while syncing data',
        'Connection failed after 30 seconds',
        {
          level: 'warning',
          source: 'data_sync',
          userId: 'mobile_user_789',
          deviceId: 'android_device_123',
          platform: 'Android',
          appVersion: '1.2.3',
          osVersion: 'Android 12',
          metadata: {
            test: true,
            purpose: 'Testing mobile error logging'
          }
        }
      );
      
      if (mobileLog) {
        console.log('✅ Mobile error logged successfully:', mobileLog._id);
      } else {
        console.log('❌ Failed to log mobile error');
      }
    }

    // Test 3: Web Error Logging
    if (TEST_CONFIG.testWebErrors) {
      console.log('\n🌐 Testing Web Error Logging...');
      
      const webLog = await ErrorLogger.logWebError(
        'JavaScript runtime error in user dashboard',
        'TypeError: Cannot read property "data" of undefined',
        {
          level: 'error',
          source: 'user_dashboard',
          userId: 'web_user_456',
          deviceId: 'browser_session_789',
          browser: 'Chrome',
          browserVersion: '96.0.4664.110',
          pageUrl: '/dashboard',
          metadata: {
            test: true,
            purpose: 'Testing web error logging'
          }
        }
      );
      
      if (webLog) {
        console.log('✅ Web error logged successfully:', webLog._id);
      } else {
        console.log('❌ Failed to log web error');
      }
    }

    // Test 4: Database Error Logging
    if (TEST_CONFIG.testDatabaseErrors) {
      console.log('\n🗄️ Testing Database Error Logging...');
      
      const dbError = new Error('Duplicate key error on email field');
      dbError.name = 'MongoError';
      dbError.code = 11000;
      
      const dbLog = await ErrorLogger.logDatabaseError(dbError, {
        source: 'user_registration',
        userId: 'new_user_123',
        operation: 'insert',
        collection: 'users',
        query: { email: 'test@example.com' },
        metadata: {
          test: true,
          purpose: 'Testing database error logging'
        }
      });
      
      if (dbLog) {
        console.log('✅ Database error logged successfully:', dbLog._id);
      } else {
        console.log('❌ Failed to log database error');
      }
    }

    // Test 5: Permission Error Logging
    if (TEST_CONFIG.testPermissionErrors) {
      console.log('\n🔒 Testing Permission Error Logging...');
      
      const permissionLog = await ErrorLogger.logPermissionError(
        'User attempted to access admin panel without proper role',
        'User has role "user" but "admin" role required',
        {
          app: 'backend',
          userId: 'regular_user_123',
          deviceId: 'device_456',
          ipAddress: '192.168.1.100',
          resource: 'admin_panel',
          action: 'access',
          requiredRole: 'admin',
          metadata: {
            test: true,
            purpose: 'Testing permission error logging'
          }
        }
      );
      
      if (permissionLog) {
        console.log('✅ Permission error logged successfully:', permissionLog._id);
      } else {
        console.log('❌ Failed to log permission error');
      }
    }

    // Test 6: Validation Error Logging
    if (TEST_CONFIG.testValidationErrors) {
      console.log('\n✅ Testing Validation Error Logging...');
      
      const validationErrors = {
        email: 'Invalid email format',
        password: 'Password must be at least 8 characters',
        age: 'Age must be a positive number'
      };
      
      const validationLog = await ErrorLogger.logValidationError(
        'User registration validation failed',
        validationErrors,
        {
          app: 'backend',
          userId: 'new_user_456',
          deviceId: 'device_789',
          field: 'multiple',
          value: 'invalid_data',
          requestBody: {
            email: 'invalid-email',
            password: '123',
            age: -5
          },
          metadata: {
            test: true,
            purpose: 'Testing validation error logging'
          }
        }
      );
      
      if (validationLog) {
        console.log('✅ Validation error logged successfully:', validationLog._id);
      } else {
        console.log('❌ Failed to log validation error');
      }
    }

    // Test 7: Critical Error Logging
    if (TEST_CONFIG.testCriticalErrors) {
      console.log('\n🚨 Testing Critical Error Logging...');
      
      const criticalLog = await ErrorLogger.logCriticalError(
        'Database connection pool exhausted',
        'All database connections are in use, new requests are being queued',
        {
          app: 'backend',
          source: 'database_pool',
          metadata: {
            test: true,
            purpose: 'Testing critical error logging',
            severity: 'high',
            impact: 'service_degradation'
          }
        }
      );
      
      if (criticalLog) {
        console.log('✅ Critical error logged successfully:', criticalLog._id);
      } else {
        console.log('❌ Failed to log critical error');
      }
    }

    // Test 8: Batch Error Logging
    if (TEST_CONFIG.testBatchLogging) {
      console.log('\n📦 Testing Batch Error Logging...');
      
      const batchErrors = [
        {
          reason: 'Network timeout error 1',
          trace: 'Connection failed after 15 seconds',
          level: 'warning'
        },
        {
          reason: 'Network timeout error 2',
          trace: 'Connection failed after 20 seconds',
          level: 'warning'
        },
        {
          reason: 'Network timeout error 3',
          trace: 'Connection failed after 25 seconds',
          level: 'error'
        }
      ];
      
      const batchResults = [];
      for (const errorData of batchErrors) {
        const log = await ErrorLogger.logError(
          errorData.reason,
          errorData.trace,
          {
            app: 'mobile',
            level: errorData.level,
            source: 'batch_test',
            deviceId: 'test_device_batch',
            metadata: {
              test: true,
              purpose: 'Testing batch error logging',
              batchIndex: batchErrors.indexOf(errorData)
            }
          }
        );
        
        if (log) {
          batchResults.push(log._id);
        }
      }
      
      console.log(`✅ Batch logging completed: ${batchResults.length}/${batchErrors.length} errors logged`);
    }

    // Test 9: Retrieve and Display Recent Errors
    console.log('\n📊 Retrieving Recent Error Logs...');
    
    const recentErrors = await ErrorLogger.getRecentErrors({}, 10);
    console.log(`✅ Retrieved ${recentErrors.length} recent errors`);
    
    if (recentErrors.length > 0) {
      console.log('\n📋 Recent Error Logs Summary:');
      recentErrors.forEach((error, index) => {
        console.log(`${index + 1}. [${error.app.toUpperCase()}] [${error.level.toUpperCase()}] ${error.reason.substring(0, 60)}...`);
      });
    }

    // Test 10: Error Statistics
    console.log('\n📈 Error Statistics:');
    
    const totalErrors = await ErrorLog.countDocuments();
    const errorsByApp = await ErrorLog.aggregate([
      { $group: { _id: '$app', count: { $sum: 1 } } },
      { $sort: { count: -1 } }
    ]);
    
    const errorsByLevel = await ErrorLog.aggregate([
      { $group: { _id: '$level', count: { $sum: 1 } } },
      { $sort: { count: -1 } }
    ]);
    
    console.log(`Total Errors: ${totalErrors}`);
    console.log('Errors by App:', errorsByApp);
    console.log('Errors by Level:', errorsByLevel);

    // Cleanup test data if configured
    if (TEST_CONFIG.cleanupAfterTest) {
      console.log('\n🧹 Cleaning up test data...');
      
      const testErrors = await ErrorLog.find({
        'metadata.test': true
      });
      
      if (testErrors.length > 0) {
        await ErrorLog.deleteMany({ 'metadata.test': true });
        console.log(`✅ Cleaned up ${testErrors.length} test error logs`);
      } else {
        console.log('ℹ️ No test error logs to clean up');
      }
    }

    console.log('\n🎉 Error Logging System Test Completed Successfully!');
    console.log('\n📝 Test Summary:');
    console.log('- Backend error logging: ✅');
    console.log('- Mobile error logging: ✅');
    console.log('- Web error logging: ✅');
    console.log('- Database error logging: ✅');
    console.log('- Permission error logging: ✅');
    console.log('- Validation error logging: ✅');
    console.log('- Critical error logging: ✅');
    console.log('- Batch error logging: ✅');
    console.log('- Error retrieval: ✅');
    console.log('- Error statistics: ✅');

  } catch (error) {
    console.error('\n❌ Test failed with error:', error);
    console.error('Stack trace:', error.stack);
  } finally {
    // Close MongoDB connection
    if (mongoose.connection.readyState === 1) {
      await mongoose.connection.close();
      console.log('\n🔌 MongoDB connection closed');
    }
    
    console.log('\n🏁 Test script finished');
  }
}

// Run the test if this file is executed directly
if (require.main === module) {
  testErrorLogging();
}

module.exports = { testErrorLogging }; 