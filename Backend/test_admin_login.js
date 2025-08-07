const mongoose = require('mongoose');
const Admin = require('./models/Admin');
const bcrypt = require('bcryptjs');
const config = require('./config/environment');

async function testAdminLogin() {
    try {
        // Connect to MongoDB
        await mongoose.connect(config.mongodb.uri, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });

        console.log('Connected to MongoDB');

        // Check if admin exists
        const admin = await Admin.findOne({ email: 'bilal.xbt@gmail.com' });
        
        if (!admin) {
            console.log('❌ Admin user not found');
            return;
        }

        console.log('✅ Admin user found:');
        console.log('   ID:', admin._id);
        console.log('   Username:', admin.username);
        console.log('   Email:', admin.email);
        console.log('   Role:', admin.role);
        console.log('   Is Active:', admin.isActive);
        console.log('   Permissions:', admin.permissions);
        console.log('   Allowed Data Types:', admin.allowedDataTypes);

        // Test password
        const testPassword = 'bilal123';
        const isValidPassword = await admin.comparePassword(testPassword);
        
        console.log('\n🔐 Password test:');
        console.log('   Test password:', testPassword);
        console.log('   Is valid:', isValidPassword);

        if (!isValidPassword) {
            console.log('❌ Password is incorrect');
            
            // Let's create a new admin with correct password
            console.log('\n🔄 Creating new admin with correct password...');
            
            const newAdmin = new Admin({
                username: 'bilal_admin_new',
                email: 'bilal.xbt@gmail.com',
                password: 'bilal123',
                role: 'admin',
                permissions: ['view_devices', 'manage_users', 'manage_codes', 'view_analytics', 'system_settings']
            });

            await newAdmin.save();
            console.log('✅ New admin created successfully');
        }

    } catch (error) {
        console.error('❌ Error:', error);
    } finally {
        await mongoose.disconnect();
        console.log('Disconnected from MongoDB');
    }
}

// Run the test
testAdminLogin(); 