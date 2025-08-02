const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const Admin = require('./models/Admin');
const config = require('./config/environment');

async function seedAdmin() {
    try {
        // Connect to MongoDB
        await mongoose.connect(config.mongodb.uri, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });

        console.log('Connected to MongoDB');

        // Check if admin already exists
        const existingAdmin = await Admin.findOne({ email: 'bilal.xbt@gmail.com' });
        
        if (existingAdmin) {
            console.log('Admin user already exists');
            return;
        }

        // Hash password
        const saltRounds = 10;
        const hashedPassword = await bcrypt.hash('bilal123', saltRounds);

        // Create admin user
        const admin = new Admin({
            email: 'bilal.xbt@gmail.com',
            password: hashedPassword,
            name: 'Bilal Admin',
            role: 'admin',
            permissions: ['manage_users', 'view_all_devices', 'manage_devices', 'view_analytics']
        });

        await admin.save();

        console.log('✅ Admin user created successfully');
        console.log('Email: bilal.xbt@gmail.com');
        console.log('Password: bilal123');

    } catch (error) {
        console.error('❌ Error seeding admin:', error);
    } finally {
        await mongoose.disconnect();
        console.log('Disconnected from MongoDB');
    }
}

// Run the seed function
seedAdmin(); 