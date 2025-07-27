// Database configuration
const mongoose = require('mongoose');
require('dotenv').config();

// Fix indexes on startup
async function fixIndexesOnStartup(db) {
    try {
        console.log('🔄 Fixing MongoDB indexes on startup...');
        
        // Fix Contacts collection
        const contactsCollection = db.collection('contacts');
        console.log('📞 Fixing contacts indexes...');
        try {
            await contactsCollection.dropIndexes();
            console.log('✅ Dropped existing contact indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for contacts');
        }
        await contactsCollection.createIndex({ deviceId: 1, phoneNumber: 1 });
        await contactsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new contact indexes');

        // Fix Messages collection
        const messagesCollection = db.collection('messages');
        console.log('💬 Fixing messages indexes...');
        try {
            await messagesCollection.dropIndexes();
            console.log('✅ Dropped existing message indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for messages');
        }
        await messagesCollection.createIndex({ deviceId: 1, address: 1, timestamp: 1, body: 1 });
        await messagesCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new message indexes');

        // Fix CallLogs collection
        const callLogsCollection = db.collection('calllogs');
        console.log('📞 Fixing call logs indexes...');
        try {
            await callLogsCollection.dropIndexes();
            console.log('✅ Dropped existing call log indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for call logs');
        }
        await callLogsCollection.createIndex({ deviceId: 1, phoneNumber: 1, timestamp: 1, duration: 1 });
        await callLogsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new call log indexes');

        console.log('🎉 All indexes fixed successfully!');
    } catch (error) {
        console.error('❌ Error fixing indexes:', error);
    }
}

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
        
        // Fix indexes on startup
        await fixIndexesOnStartup(conn.connection.db);
        
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