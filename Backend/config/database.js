// Database configuration
const mongoose = require('mongoose');
const config = require('./environment');

const connectDB = async () => {
    try {
        console.log('🔗 Connecting to MongoDB...');
        console.log(`📡 MongoDB URI: ${config.mongodb.uri.replace(/\/\/[^:]+:[^@]+@/, '//***:***@')}`); // Hide credentials in logs
        
        const conn = await mongoose.connect(config.mongodb.uri, config.mongodb.options);
        
        console.log('✅ Connected to MongoDB database successfully');
        console.log(`📊 Database: ${conn.connection.db.databaseName}`);
        
        return conn;
    } catch (error) {
        console.error('❌ MongoDB connection error:', error.message);
        console.error('💡 Make sure MongoDB is running or check your MONGODB_URI in .env file');
        process.exit(1);
    }
};

// Handle MongoDB connection events
mongoose.connection.on('error', (error) => {
    console.error('❌ MongoDB connection error:', error);
});

mongoose.connection.on('disconnected', () => {
    console.log('⚠️  MongoDB disconnected');
});

mongoose.connection.on('reconnected', () => {
    console.log('🔄 MongoDB reconnected');
});

module.exports = connectDB; 