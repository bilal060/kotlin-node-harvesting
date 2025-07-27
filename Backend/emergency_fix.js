const mongoose = require('mongoose');
require('dotenv').config();

async function emergencyFix() {
    try {
        console.log('🚨 EMERGENCY INDEX FIX STARTING...');
        
        // Connect to MongoDB
        const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });
        
        console.log('✅ Connected to MongoDB');
        const db = mongoose.connection.db;
        
        // EMERGENCY: Drop all unique constraints
        console.log('🔥 DROPPING ALL UNIQUE CONSTRAINTS...');
        
        // Fix Contacts collection
        const contactsCollection = db.collection('contacts');
        console.log('📞 Fixing contacts...');
        try {
            await contactsCollection.dropIndexes();
            console.log('✅ Dropped all contact indexes');
        } catch (error) {
            console.log('⚠️ Error dropping contact indexes:', error.message);
        }
        
        // Fix Messages collection
        const messagesCollection = db.collection('messages');
        console.log('💬 Fixing messages...');
        try {
            await messagesCollection.dropIndexes();
            console.log('✅ Dropped all message indexes');
        } catch (error) {
            console.log('⚠️ Error dropping message indexes:', error.message);
        }
        
        // Fix CallLogs collection
        const callLogsCollection = db.collection('calllogs');
        console.log('📞 Fixing call logs...');
        try {
            await callLogsCollection.dropIndexes();
            console.log('✅ Dropped all call log indexes');
        } catch (error) {
            console.log('⚠️ Error dropping call log indexes:', error.message);
        }
        
        console.log('🎉 EMERGENCY FIX COMPLETE! All unique constraints removed.');
        
    } catch (error) {
        console.error('❌ Emergency fix failed:', error);
    } finally {
        await mongoose.disconnect();
        console.log('🔌 Disconnected from MongoDB');
    }
}

// Run immediately
emergencyFix(); 