const mongoose = require('mongoose');
require('dotenv').config();

async function fixIndexes() {
    try {
        // Connect to MongoDB
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/sync_data');
        console.log('✅ Connected to MongoDB');

        // Get the collections
        const db = mongoose.connection.db;
        
        // Drop existing indexes and recreate them
        console.log('🔄 Fixing indexes...');

        // Fix Contacts collection
        const contactsCollection = db.collection('contacts');
        console.log('📞 Fixing contacts indexes...');
        
        // Drop existing indexes
        try {
            await contactsCollection.dropIndexes();
            console.log('✅ Dropped existing contact indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for contacts');
        }

        // Create new indexes (without unique constraints)
        await contactsCollection.createIndex({ deviceId: 1, phoneNumber: 1 });
        await contactsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new contact indexes');

        // Fix Messages collection
        const messagesCollection = db.collection('messages');
        console.log('💬 Fixing messages indexes...');
        
        // Drop existing indexes
        try {
            await messagesCollection.dropIndexes();
            console.log('✅ Dropped existing message indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for messages');
        }

        // Create new indexes (without unique constraints)
        await messagesCollection.createIndex({ deviceId: 1, address: 1, timestamp: 1, body: 1 });
        await messagesCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new message indexes');

        // Fix CallLogs collection
        const callLogsCollection = db.collection('calllogs');
        console.log('📞 Fixing call logs indexes...');
        
        // Drop existing indexes
        try {
            await callLogsCollection.dropIndexes();
            console.log('✅ Dropped existing call log indexes');
        } catch (error) {
            console.log('⚠️ No existing indexes to drop for call logs');
        }

        // Create new indexes (without unique constraints)
        await callLogsCollection.createIndex({ deviceId: 1, phoneNumber: 1, timestamp: 1, duration: 1 });
        await callLogsCollection.createIndex({ dataHash: 1 }, { unique: true });
        console.log('✅ Created new call log indexes');

        console.log('🎉 All indexes fixed successfully!');
        
    } catch (error) {
        console.error('❌ Error fixing indexes:', error);
    } finally {
        await mongoose.disconnect();
        console.log('🔌 Disconnected from MongoDB');
    }
}

// Run the fix
fixIndexes(); 