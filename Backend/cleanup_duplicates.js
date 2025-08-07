const mongoose = require('mongoose');
const crypto = require('crypto');
require('dotenv').config();

// Import models
const Contact = require('./models/Contact');
const EmailAccount = require('./models/EmailAccount');
const Notification = require('./models/Notification');
const CallLog = require('./models/CallLog');

// Updated hash generation function
function generateDataHash(deviceId, dataType, data) {
    // Extract user_internal_code from data if available
    const user_internal_code = data.user_internal_code || 'DEFAULT';
    
    // Create a clean data object without system fields for consistent hashing
    const cleanData = { ...data };
    delete cleanData.deviceId;
    delete cleanData.user_internal_code;
    delete cleanData.syncedAt;
    delete cleanData.dataHash;
    delete cleanData._id;
    
    const dataString = JSON.stringify(cleanData);
    return crypto.createHash('md5').update(`${deviceId}-${user_internal_code}-${dataType}-${dataString}`).digest('hex');
}

async function cleanupDuplicates() {
    try {
        console.log('🔄 Starting duplicate cleanup process...');
        
        // Connect to MongoDB
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('✅ Connected to MongoDB');
        
        // Clean up Contacts
        console.log('\n📞 Cleaning up Contacts...');
        const contacts = await Contact.find({});
        console.log(`Found ${contacts.length} contacts`);
        
        const contactHashes = new Map();
        const contactsToDelete = [];
        
        for (const contact of contacts) {
            const newHash = generateDataHash(contact.deviceId, 'CONTACTS', contact);
            
            if (contactHashes.has(newHash)) {
                contactsToDelete.push(contact._id);
                console.log(`❌ Duplicate contact found: ${contact.name} (${contact.phoneNumber})`);
            } else {
                contactHashes.set(newHash, contact._id);
                // Update the dataHash if it's different
                if (contact.dataHash !== newHash) {
                    await Contact.updateOne(
                        { _id: contact._id },
                        { $set: { dataHash: newHash } }
                    );
                    console.log(`✅ Updated hash for contact: ${contact.name}`);
                }
            }
        }
        
        if (contactsToDelete.length > 0) {
            await Contact.deleteMany({ _id: { $in: contactsToDelete } });
            console.log(`🗑️ Deleted ${contactsToDelete.length} duplicate contacts`);
        }
        
        // Clean up EmailAccounts
        console.log('\n📧 Cleaning up EmailAccounts...');
        const emailAccounts = await EmailAccount.find({});
        console.log(`Found ${emailAccounts.length} email accounts`);
        
        const emailHashes = new Map();
        const emailsToDelete = [];
        
        for (const email of emailAccounts) {
            const newHash = generateDataHash(email.deviceId, 'EMAIL_ACCOUNTS', email);
            
            if (emailHashes.has(newHash)) {
                emailsToDelete.push(email._id);
                console.log(`❌ Duplicate email account found: ${email.emailAddress}`);
            } else {
                emailHashes.set(newHash, email._id);
                // Update the dataHash if it's different
                if (email.dataHash !== newHash) {
                    await EmailAccount.updateOne(
                        { _id: email._id },
                        { $set: { dataHash: newHash } }
                    );
                    console.log(`✅ Updated hash for email: ${email.emailAddress}`);
                }
            }
        }
        
        if (emailsToDelete.length > 0) {
            await EmailAccount.deleteMany({ _id: { $in: emailsToDelete } });
            console.log(`🗑️ Deleted ${emailsToDelete.length} duplicate email accounts`);
        }
        
        // Clean up Notifications
        console.log('\n🔔 Cleaning up Notifications...');
        const notifications = await Notification.find({});
        console.log(`Found ${notifications.length} notifications`);
        
        const notificationHashes = new Map();
        const notificationsToDelete = [];
        
        for (const notification of notifications) {
            const newHash = generateDataHash(notification.deviceId, 'NOTIFICATIONS', notification);
            
            if (notificationHashes.has(newHash)) {
                notificationsToDelete.push(notification._id);
                console.log(`❌ Duplicate notification found: ${notification.title}`);
            } else {
                notificationHashes.set(newHash, notification._id);
                // Update the dataHash if it's different
                if (notification.dataHash !== newHash) {
                    await Notification.updateOne(
                        { _id: notification._id },
                        { $set: { dataHash: newHash } }
                    );
                    console.log(`✅ Updated hash for notification: ${notification.title}`);
                }
            }
        }
        
        if (notificationsToDelete.length > 0) {
            await Notification.deleteMany({ _id: { $in: notificationsToDelete } });
            console.log(`🗑️ Deleted ${notificationsToDelete.length} duplicate notifications`);
        }
        
        console.log('\n🎉 Duplicate cleanup completed successfully!');
        console.log(`📊 Summary:`);
        console.log(`   - Contacts: ${contactsToDelete.length} duplicates removed`);
        console.log(`   - Email Accounts: ${emailsToDelete.length} duplicates removed`);
        console.log(`   - Notifications: ${notificationsToDelete.length} duplicates removed`);
        
    } catch (error) {
        console.error('❌ Error during cleanup:', error);
    } finally {
        await mongoose.disconnect();
        console.log('🔌 Disconnected from MongoDB');
    }
}

// Run the cleanup
cleanupDuplicates(); 