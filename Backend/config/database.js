// Database configuration
const mongoose = require('mongoose');
require('dotenv').config();

const connectDB = async () => {
    try {
        const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
        
        console.log('🔗 Connecting to MongoDB...');
        console.log(`📡 MongoDB URI: ${MONGODB_URI.replace(/\/\/[^:]+:[^@]+@/, '//***:***@')}`); // Hide credentials in logs
        
        // Check if we're in production and don't have a proper MongoDB URI
        if (process.env.NODE_ENV === 'production' && MONGODB_URI.includes('localhost')) {
            console.error('❌ Production environment detected but using localhost MongoDB');
            console.error('💡 Please set MONGODB_URI environment variable to a cloud MongoDB instance');
            console.error('🔗 Recommended: Use MongoDB Atlas (https://www.mongodb.com/atlas)');
            process.exit(1);
        }

        console.log(MONGODB_URI)
        
        const conn = await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
            serverSelectionTimeoutMS: 10000, // Increased timeout for cloud connections
            socketTimeoutMS: 45000, // Close sockets after 45s of inactivity
            maxPoolSize: 10, // Maximum number of connections in the pool
            minPoolSize: 2,  // Minimum number of connections in the pool
        });
        
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