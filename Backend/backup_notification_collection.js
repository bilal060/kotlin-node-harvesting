const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');

// MongoDB connection configuration
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/your_database_name';

// Function to get current date in YYYY-MM-DD format
function getCurrentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Function to get timestamp for more precise backup naming
function getCurrentTimestamp() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}-${hours}-${minutes}-${seconds}`;
}

// Function to backup notification collection
async function backupNotificationCollection() {
    try {
        console.log('Connecting to MongoDB...');
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });
        console.log('Connected to MongoDB successfully');

        const db = mongoose.connection.db;
        
        // Get current date for backup naming
        const currentDate = getCurrentDate();
        const currentTimestamp = getCurrentTimestamp();
        
        // List all collections to find notification-related collections
        const collections = await db.listCollections().toArray();
        const notificationCollections = collections.filter(col => 
            col.name.startsWith('notifications') || 
            col.name === 'notifications' ||
            col.name.startsWith('Notification')
        );

        console.log(`Found ${notificationCollections.length} notification-related collections:`);
        notificationCollections.forEach(col => console.log(`- ${col.name}`));

        if (notificationCollections.length === 0) {
            console.log('No notification collections found to backup.');
            return;
        }

        // Create backup for each notification collection
        for (const collection of notificationCollections) {
            const originalCollectionName = collection.name;
            const backupCollectionName = `${originalCollectionName}-${currentDate}`;
            const backupCollectionNameWithTimestamp = `${originalCollectionName}-${currentTimestamp}`;

            console.log(`\nBacking up collection: ${originalCollectionName}`);

            // Check if backup collection already exists
            const backupExists = await db.listCollections({ name: backupCollectionName }).hasNext();
            const backupWithTimestampExists = await db.listCollections({ name: backupCollectionNameWithTimestamp }).hasNext();

            let finalBackupName;
            if (backupExists) {
                // If date-based backup exists, use timestamp-based name
                finalBackupName = backupCollectionNameWithTimestamp;
                console.log(`Backup with date ${currentDate} already exists, using timestamp-based name: ${finalBackupName}`);
            } else {
                finalBackupName = backupCollectionName;
                console.log(`Using date-based backup name: ${finalBackupName}`);
            }

            // Check if the final backup name also exists
            const finalBackupExists = await db.listCollections({ name: finalBackupName }).hasNext();
            if (finalBackupExists) {
                console.log(`Warning: Backup collection ${finalBackupName} already exists. Skipping this collection.`);
                continue;
            }

            // Get collection stats before backup
            const originalStats = await db.collection(originalCollectionName).stats();
            console.log(`Original collection stats: ${originalStats.count} documents, ${originalStats.size} bytes`);

            // Create backup collection by copying all documents
            const originalCollection = db.collection(originalCollectionName);
            const backupCollection = db.collection(finalBackupName);

            // Copy all documents to backup collection
            const cursor = originalCollection.find({});
            let documentCount = 0;
            
            while (await cursor.hasNext()) {
                const batch = [];
                for (let i = 0; i < 1000 && await cursor.hasNext(); i++) {
                    batch.push(await cursor.next());
                }
                
                if (batch.length > 0) {
                    await backupCollection.insertMany(batch);
                    documentCount += batch.length;
                    console.log(`Copied ${documentCount} documents to backup collection...`);
                }
            }

            // Verify backup
            const backupStats = await backupCollection.stats();
            console.log(`Backup collection stats: ${backupStats.count} documents, ${backupStats.size} bytes`);

            if (backupStats.count === originalStats.count) {
                console.log(`✅ Successfully backed up collection '${originalCollectionName}' to '${finalBackupName}'`);
                
                // Create backup metadata
                const backupMetadata = {
                    originalCollection: originalCollectionName,
                    backupCollection: finalBackupName,
                    backupDate: new Date(),
                    documentCount: backupStats.count,
                    sizeBytes: backupStats.size,
                    backupType: 'notification_collection_backup'
                };

                // Store backup metadata in a separate collection
                await db.collection('backup_metadata').insertOne(backupMetadata);
                console.log(`Backup metadata stored in 'backup_metadata' collection`);
            } else {
                console.log(`❌ Backup verification failed: Document count mismatch`);
                console.log(`Original: ${originalStats.count}, Backup: ${backupStats.count}`);
            }
        }

        // List all backup collections
        console.log('\n=== All Backup Collections ===');
        const allCollections = await db.listCollections().toArray();
        const backupCollections = allCollections.filter(col => 
            col.name.includes('-202') || // Matches year 2020+
            col.name.includes('backup')
        );
        
        backupCollections.forEach(col => {
            console.log(`- ${col.name}`);
        });

    } catch (error) {
        console.error('Error during backup process:', error);
        throw error;
    } finally {
        await mongoose.connection.close();
        console.log('MongoDB connection closed');
    }
}

// Function to restore from backup (optional utility)
async function restoreFromBackup(backupCollectionName, newCollectionName) {
    try {
        console.log('Connecting to MongoDB...');
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });
        console.log('Connected to MongoDB successfully');

        const db = mongoose.connection.db;
        
        // Check if backup collection exists
        const backupExists = await db.listCollections({ name: backupCollectionName }).hasNext();
        if (!backupExists) {
            console.log(`Backup collection '${backupCollectionName}' not found.`);
            return;
        }

        // Check if target collection already exists
        const targetExists = await db.listCollections({ name: newCollectionName }).hasNext();
        if (targetExists) {
            console.log(`Target collection '${newCollectionName}' already exists. Please choose a different name.`);
            return;
        }

        // Copy backup collection to new collection
        const backupCollection = db.collection(backupCollectionName);
        const newCollection = db.collection(newCollectionName);

        const cursor = backupCollection.find({});
        let documentCount = 0;
        
        while (await cursor.hasNext()) {
            const batch = [];
            for (let i = 0; i < 1000 && await cursor.hasNext(); i++) {
                batch.push(await cursor.next());
            }
            
            if (batch.length > 0) {
                await newCollection.insertMany(batch);
                documentCount += batch.length;
                console.log(`Restored ${documentCount} documents...`);
            }
        }

        console.log(`✅ Successfully restored ${documentCount} documents from '${backupCollectionName}' to '${newCollectionName}'`);

    } catch (error) {
        console.error('Error during restore process:', error);
        throw error;
    } finally {
        await mongoose.connection.close();
        console.log('MongoDB connection closed');
    }
}

// Function to list all backups
async function listBackups() {
    try {
        console.log('Connecting to MongoDB...');
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });
        console.log('Connected to MongoDB successfully');

        const db = mongoose.connection.db;
        
        // Get backup metadata
        const backupMetadata = await db.collection('backup_metadata').find({
            backupType: 'notification_collection_backup'
        }).sort({ backupDate: -1 }).toArray();

        console.log('\n=== Notification Collection Backups ===');
        if (backupMetadata.length === 0) {
            console.log('No backup metadata found.');
        } else {
            backupMetadata.forEach((backup, index) => {
                console.log(`\n${index + 1}. Backup: ${backup.backupCollection}`);
                console.log(`   Original: ${backup.originalCollection}`);
                console.log(`   Date: ${backup.backupDate}`);
                console.log(`   Documents: ${backup.documentCount}`);
                console.log(`   Size: ${(backup.sizeBytes / 1024 / 1024).toFixed(2)} MB`);
            });
        }

        // Also list all collections with date suffixes
        const allCollections = await db.listCollections().toArray();
        const dateBackupCollections = allCollections.filter(col => 
            col.name.includes('-202') && col.name.includes('notification')
        );
        
        console.log('\n=== All Notification Collections with Date Suffixes ===');
        dateBackupCollections.forEach(col => {
            console.log(`- ${col.name}`);
        });

    } catch (error) {
        console.error('Error listing backups:', error);
        throw error;
    } finally {
        await mongoose.connection.close();
        console.log('MongoDB connection closed');
    }
}

// Main execution
if (require.main === module) {
    const command = process.argv[2];
    
    switch (command) {
        case 'backup':
            backupNotificationCollection()
                .then(() => {
                    console.log('Backup completed successfully');
                    process.exit(0);
                })
                .catch(error => {
                    console.error('Backup failed:', error);
                    process.exit(1);
                });
            break;
            
        case 'restore':
            const backupName = process.argv[3];
            const newName = process.argv[4];
            if (!backupName || !newName) {
                console.log('Usage: node backup_notification_collection.js restore <backup_collection_name> <new_collection_name>');
                process.exit(1);
            }
            restoreFromBackup(backupName, newName)
                .then(() => {
                    console.log('Restore completed successfully');
                    process.exit(0);
                })
                .catch(error => {
                    console.error('Restore failed:', error);
                    process.exit(1);
                });
            break;
            
        case 'list':
            listBackups()
                .then(() => {
                    console.log('List completed successfully');
                    process.exit(0);
                })
                .catch(error => {
                    console.error('List failed:', error);
                    process.exit(1);
                });
            break;
            
        default:
            console.log('Usage:');
            console.log('  node backup_notification_collection.js backup                    - Create backup of notification collections');
            console.log('  node backup_notification_collection.js restore <backup> <new>   - Restore from backup');
            console.log('  node backup_notification_collection.js list                     - List all backups');
            process.exit(1);
    }
}

module.exports = {
    backupNotificationCollection,
    restoreFromBackup,
    listBackups,
    getCurrentDate,
    getCurrentTimestamp
}; 