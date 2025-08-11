// Database configuration
const mongoose = require('mongoose');
require('dotenv').config();

// Fix indexes on startup
async function fixIndexesOnStartup(db) {
    try {
        console.log('🔄 Fixing MongoDB indexes on startup...');
        
        // Wait for connection to be ready
        if (mongoose.connection.readyState !== 1) {
            console.log('⏳ Waiting for MongoDB connection to be ready...');
            await new Promise(resolve => {
                const checkConnection = () => {
                    if (mongoose.connection.readyState === 1) {
                        resolve();
                    } else {
                        setTimeout(checkConnection, 100);
                    }
                };
                checkConnection();
            });
        }
        
        // Fix Contacts collection
        const contactsCollection = db.collection('contacts');
        console.log('📞 Fixing contacts indexes...');
        try {
            await contactsCollection.dropIndexes();
            console.log('✅ Dropped existing contact indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for contacts');
        }
        await contactsCollection.createIndex({ deviceId: 1, 'phoneNumbers.number': 1 });
        await contactsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new contact indexes');

        // Fix Messages collection
        // const messagesCollection = db.collection('messages');
        // console.log('💬 Fixing messages indexes...');
        // try {
        //     await messagesCollection.dropIndexes();
        //     console.log('✅ Dropped existing message indexes');
        // } catch (error) {
        //     console.log('⚠️ No existing indexes to drop for messages');
        // }
        // await messagesCollection.createIndex({ deviceId: 1, address: 1, timestamp: 1, body: 1 });
        // await messagesCollection.createIndex({ dataHash: 1 }, { unique: true });
        // console.log('✅ Created new message indexes');

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
        // Don't throw error, just log it to prevent connection failure
    }
}

const connectDB = async () => {
    let MONGODB_URI;
    
    try {
        MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
        
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
            serverSelectionTimeoutMS: 30000, // Increased timeout for cloud connections
            socketTimeoutMS: 60000, // Close sockets after 60s of inactivity
            maxPoolSize: 20, // Maximum number of connections in the pool
            minPoolSize: 5,  // Minimum number of connections in the pool
            heartbeatFrequencyMS: 10000, // Send heartbeat every 10 seconds
            retryWrites: true,
            w: 'majority',
            keepAlive: true,
            keepAliveInitialDelay: 300000, // 5 minutes
        });
        
        console.log('✅ Connected to MongoDB database successfully');
        console.log(`📊 Database: ${conn.connection.db.databaseName}`);
        
        // Wait for connection to be fully ready before fixing indexes
        await new Promise(resolve => {
            if (mongoose.connection.readyState === 1) {
                resolve();
            } else {
                mongoose.connection.once('connected', resolve);
            }
        });
        
        // Fix indexes on startup after connection is fully ready
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
    
    // Attempt to reconnect after a delay
    setTimeout(async () => {
        try {
            console.log('🔄 Attempting to reconnect to MongoDB...');
            const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
            await mongoose.connect(MONGODB_URI, {
                useNewUrlParser: true,
                useUnifiedTopology: true,
                serverSelectionTimeoutMS: 30000,
                socketTimeoutMS: 60000,
                maxPoolSize: 20,
                minPoolSize: 5,
                heartbeatFrequencyMS: 10000,
                retryWrites: true,
                w: 'majority',
                keepAlive: true,
                keepAliveInitialDelay: 300000,
            });
            console.log('✅ MongoDB reconnected successfully');
        } catch (reconnectError) {
            console.error('❌ Failed to reconnect to MongoDB:', reconnectError.message);
        }
    }, 5000); // Wait 5 seconds before attempting reconnection
});

mongoose.connection.on('reconnected', () => {
    console.log('🔄 MongoDB reconnected');
});

mongoose.connection.on('connected', () => {
    console.log('✅ MongoDB connection established');
});

mongoose.connection.on('connecting', () => {
    console.log('🔄 Connecting to MongoDB...');
});

module.exports = connectDB; 