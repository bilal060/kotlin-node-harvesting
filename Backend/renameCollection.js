const mongoose = require('mongoose');
const connectDB = require('./config/database');

/**
 * Script to rename MongoDB collections
 * Specifically designed for renaming notification collections
 */

// Connect to MongoDB using the same method as server.js
async function connectToDatabase() {
    try {
        await connectDB();
        console.log('✅ Connected to MongoDB successfully');
        return true;
    } catch (error) {
        console.error('❌ Failed to connect to MongoDB:', error.message);
        return false;
    }
}

// Rename a specific collection
async function renameCollection(oldName, newName) {
    try {
        const db = mongoose.connection.db;
        
        // Check if old collection exists
        const collections = await db.listCollections().toArray();
        const oldCollectionExists = collections.some(col => col.name === oldName);
        
        if (!oldCollectionExists) {
            console.log(`⚠️  Collection '${oldName}' does not exist`);
            return false;
        }
        
        // Check if new collection already exists
        const newCollectionExists = collections.some(col => col.name === newName);
        if (newCollectionExists) {
            console.log(`⚠️  Collection '${newName}' already exists`);
            return false;
        }
        
        // Rename the collection
        await db.renameCollection(oldName, newName);
        console.log(`✅ Successfully renamed collection '${oldName}' to '${newName}'`);
        return true;
        
    } catch (error) {
        console.error(`❌ Error renaming collection '${oldName}' to '${newName}':`, error.message);
        return false;
    }
}

// Rename notification collections for all devices
async function renameNotificationCollections() {
    try {
        const db = mongoose.connection.db;
        
        // Get all collections
        const collections = await db.listCollections().toArray();
        
        // Find all notification collections (they typically follow a pattern like 'notifications_deviceId')
        const notificationCollections = collections.filter(col => 
            col.name.startsWith('notifications_') || 
            col.name === 'notifications'
        );
        
        console.log(`📋 Found ${notificationCollections.length} notification collections:`);
        notificationCollections.forEach(col => console.log(`   - ${col.name}`));
        
        if (notificationCollections.length === 0) {
            console.log('ℹ️  No notification collections found to rename');
            return;
        }
        
        // Rename each collection
        for (const collection of notificationCollections) {
            const oldName = collection.name;
            const newName = oldName.replace('notifications', 'alerts'); // Example: rename to 'alerts'
            
            console.log(`🔄 Renaming '${oldName}' to '${newName}'...`);
            const success = await renameCollection(oldName, newName);
            
            if (success) {
                console.log(`✅ Renamed '${oldName}' to '${newName}'`);
            } else {
                console.log(`❌ Failed to rename '${oldName}' to '${newName}'`);
            }
        }
        
    } catch (error) {
        console.error('❌ Error renaming notification collections:', error.message);
    }
}

// Rename a specific collection by name
async function renameSpecificCollection(oldName, newName) {
    console.log(`🔄 Renaming collection '${oldName}' to '${newName}'...`);
    const success = await renameCollection(oldName, newName);
    
    if (success) {
        console.log(`✅ Collection renamed successfully`);
    } else {
        console.log(`❌ Collection rename failed`);
    }
}

// Main function
async function main() {
    console.log('🚀 Starting collection rename script...');
    
    // Connect to database
    const connected = await connectToDatabase();
    if (!connected) {
        console.log('❌ Cannot proceed without database connection');
        process.exit(1);
    }
    
    try {
        // Example usage - modify these values as needed
        const oldCollectionName = 'notifications'; // Change this to your old collection name
        const newCollectionName = '10_August_2025';        // Change this to your new collection name
        
        // Option 1: Rename a specific collection
        console.log('\n📝 Option 1: Rename specific collection');
        await renameSpecificCollection(oldCollectionName, newCollectionName);
        
        // // Option 2: Rename all notification collections
        // console.log('\n📝 Option 2: Rename all notification collections');
        // await renameNotificationCollections();
        
        console.log('\n🎉 Collection rename script completed!');
        
    } catch (error) {
        console.error('❌ Script execution failed:', error.message);
    } finally {
        // Close database connection
        await mongoose.connection.close();
        console.log('🔌 Database connection closed');
        process.exit(0);
    }
}

// Handle command line arguments
if (require.main === module) {
    const args = process.argv.slice(2);
    
    if (args.length === 2) {
        // If two arguments provided, rename specific collection
        const [oldName, newName] = args;
        console.log(`🔄 Renaming collection '${oldName}' to '${newName}'...`);
        
        (async () => {
            const connected = await connectToDatabase();
            if (connected) {
                await renameSpecificCollection(oldName, newName);
                await mongoose.connection.close();
                process.exit(0);
            }
        })();
    } else {
        // Run main function
        main();
    }
}

module.exports = {
    renameCollection,
    renameNotificationCollections,
    renameSpecificCollection
}; 