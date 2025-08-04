#!/usr/bin/env node

const mongoose = require('mongoose');
const config = require('./config/environment');
const seedDubaiDataFromJson = require('./seedDubaiDataFromJson');

async function main() {
    try {
        console.log('🚀 Starting Dubai Data Seeder...');
        
        // Connect to MongoDB
        await mongoose.connect(config.mongodb.uri, config.mongodb.options);
        console.log('✅ Connected to MongoDB');
        
        // Run the seeder
        await seedDubaiDataFromJson();
        
        console.log('✅ Seeder completed successfully!');
        
    } catch (error) {
        console.error('❌ Error running seeder:', error);
        process.exit(1);
    } finally {
        await mongoose.connection.close();
        console.log('🔌 Disconnected from MongoDB');
    }
}

// Run the seeder
main(); 