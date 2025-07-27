// Test environment configuration
console.log('🧪 Testing Environment Configuration...\n');

// Test 1: Load environment config
try {
    const config = require('./config/environment');
    console.log('✅ Environment config loaded successfully');
    console.log(`📡 MongoDB URI: ${config.mongodb.uri.replace(/\/\/[^:]+:[^@]+@/, '//***:***@')}`);
    console.log(`🌍 Environment: ${config.server.environment}`);
    console.log(`🚀 Port: ${config.server.port}`);
} catch (error) {
    console.error('❌ Failed to load environment config:', error.message);
}

// Test 2: Check environment variables
console.log('\n🔍 Environment Variables:');
console.log(`NODE_ENV: ${process.env.NODE_ENV || 'not set'}`);
console.log(`MONGODB_URI: ${process.env.MONGODB_URI ? 'set' : 'not set'}`);
console.log(`PORT: ${process.env.PORT || 'not set'}`);

// Test 3: Test MongoDB connection
console.log('\n🔗 Testing MongoDB Connection...');
const mongoose = require('mongoose');
const config = require('./config/environment');

mongoose.connect(config.mongodb.uri, config.mongodb.options)
    .then(() => {
        console.log('✅ MongoDB connection successful');
        process.exit(0);
    })
    .catch((error) => {
        console.error('❌ MongoDB connection failed:', error.message);
        process.exit(1);
    }); 