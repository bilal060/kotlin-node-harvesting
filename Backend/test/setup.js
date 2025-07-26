// Test setup file for Jest
const mongoose = require('mongoose');

// Increase timeout for all tests
jest.setTimeout(30000);

// Global test setup
beforeAll(async () => {
  // Set test environment
  process.env.NODE_ENV = 'test';
  
  // Suppress console logs during tests unless explicitly needed
  if (process.env.SUPPRESS_LOGS !== 'false') {
    console.log = jest.fn();
    console.error = jest.fn();
    console.warn = jest.fn();
  }
});

// Global test teardown
afterAll(async () => {
  // Clean up any remaining connections
  if (mongoose.connection.readyState !== 0) {
    await mongoose.disconnect();
  }
});

// Handle unhandled promise rejections in tests
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection at:', promise, 'reason:', reason);
});

// Handle uncaught exceptions in tests
process.on('uncaughtException', (error) => {
  console.error('Uncaught Exception:', error);
}); 