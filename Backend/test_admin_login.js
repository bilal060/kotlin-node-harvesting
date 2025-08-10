const mongoose = require('mongoose');
const Admin = require('./models/Admin');
const config = require('./config/environment');

async function testAdminLogin() {
    try {
        await mongoose.connect(config.mongodb.uri, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });

        console.log('Connected to MongoDB');

        const admin = await Admin.findOne({ email: 'bilal.xbt@gmail.com' });
        
        if (!admin) {
            console.log('❌ Admin user not found');
            return;
        }

        console.log('✅ Admin user found:');
        console.log('   Username:', admin.username);
        console.log('   Email:', admin.email);
        console.log('   Role:', admin.role);
        console.log('   Is Active:', admin.isActive);

        const testPassword = 'bilal123';
        const isValidPassword = await admin.comparePassword(testPassword);
        
        console.log('\n🔐 Password test:');
        console.log('   Is valid:', isValidPassword);

    } catch (error) {
        console.error('❌ Error:', error);
    } finally {
        await mongoose.disconnect();
    }
}

testAdminLogin(); 