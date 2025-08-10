const mongoose = require('mongoose');
const connectDB = require('./config/database');

/**
 * Script to remove all MongoDB collections except "10_August_2025"
 * WARNING: This is a destructive operation that will permanently delete data
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

// Get all collections in the database
async function getAllCollections() {
    try {
        const db = mongoose.connection.db;
        const collections = await db.listCollections().toArray();
        return collections.map(col => col.name);
    } catch (error) {
        console.error('❌ Error getting collections:', error.message);
        return [];
    }
}

// Remove a specific collection
async function removeCollection(collectionName) {
    try {
        const db = mongoose.connection.db;
        await db.dropCollection(collectionName);
        console.log(`✅ Successfully removed collection: ${collectionName}`);
        return true;
    } catch (error) {
        console.error(`❌ Error removing collection '${collectionName}':`, error.message);
        return false;
    }
}

// Remove all collections except the protected one
async function removeAllCollectionsExcept(protectedCollectionName) {
    try {
        console.log(`🛡️  Protected collection: ${protectedCollectionName}`);
        
        // Get all collections
        const allCollections = await getAllCollections();
        console.log(`📋 Total collections found: ${allCollections.length}`);
        
        if (allCollections.length === 0) {
            console.log('ℹ️  No collections found in database');
            return;
        }
        
        // Filter out the protected collection
        const collectionsToRemove = allCollections.filter(name => name !== protectedCollectionName);
        
        if (collectionsToRemove.length === 0) {
            console.log(`ℹ️  Only the protected collection '${protectedCollectionName}' exists. Nothing to remove.`);
            return;
        }
        
        console.log(`🗑️  Collections to be removed (${collectionsToRemove.length}):`);
        collectionsToRemove.forEach(name => console.log(`   - ${name}`));
        
        // Safety confirmation
        console.log('\n⚠️  WARNING: This operation will permanently delete all data in the above collections!');
        console.log(`🛡️  Only collection '${protectedCollectionName}' will be preserved.`);
        
        // Simulate user confirmation (in real usage, you might want to add actual user input)
        const confirmed = true; // Set to false if you want to require manual confirmation
        
        if (!confirmed) {
            console.log('❌ Operation cancelled by user');
            return;
        }
        
        console.log('\n🚀 Starting collection removal...');
        
        // Remove collections one by one
        let successCount = 0;
        let failureCount = 0;
        
        for (const collectionName of collectionsToRemove) {
            console.log(`🗑️  Removing collection: ${collectionName}`);
            const success = await removeCollection(collectionName);
            
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        
        console.log('\n📊 Removal Summary:');
        console.log(`✅ Successfully removed: ${successCount} collections`);
        console.log(`❌ Failed to remove: ${failureCount} collections`);
        console.log(`🛡️  Protected collection: ${protectedCollectionName}`);
        
        // Verify final state
        const remainingCollections = await getAllCollections();
        console.log(`📋 Remaining collections: ${remainingCollections.length}`);
        remainingCollections.forEach(name => console.log(`   - ${name}`));
        
    } catch (error) {
        console.error('❌ Error during collection removal:', error.message);
    }
}

// Main function
async function main() {
    console.log('🚀 Starting collection removal script...');
    console.log('⚠️  WARNING: This will permanently delete all collections except "10_August_2025"');
    
    // Connect to database
    const connected = await connectToDatabase();
    if (!connected) {
        console.log('❌ Cannot proceed without database connection');
        process.exit(1);
    }
    
    try {
        const protectedCollection = '10_August_2025';
        
        // Check if protected collection exists
        const allCollections = await getAllCollections();
        const protectedExists = allCollections.includes(protectedCollection);
        
        if (!protectedExists) {
            console.log(`⚠️  Warning: Protected collection '${protectedCollection}' does not exist!`);
            console.log('Available collections:');
            allCollections.forEach(name => console.log(`   - ${name}`));
            
            const proceed = true; // Set to false if you want to stop when protected collection doesn't exist
            if (!proceed) {
                console.log('❌ Operation cancelled - protected collection not found');
                process.exit(1);
            }
        }
        
        // Remove all collections except the protected one
        await removeAllCollectionsExcept(protectedCollection);
        
        console.log('\n🎉 Collection removal script completed!');
        
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
    
    if (args.length === 1) {
        // If one argument provided, use it as the protected collection name
        const protectedCollection = args[0];
        console.log(`🛡️  Using '${protectedCollection}' as protected collection`);
        
        (async () => {
            const connected = await connectToDatabase();
            if (connected) {
                await removeAllCollectionsExcept(protectedCollection);
                await mongoose.connection.close();
                process.exit(0);
            }
        })();
    } else {
        // Run main function with default protected collection "10_August_2025"
        main();
    }
}

module.exports = {
    removeCollection,
    removeAllCollectionsExcept,
    getAllCollections
}; 