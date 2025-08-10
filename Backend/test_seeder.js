const fs = require('fs');
const path = require('path');
const mongoose = require('mongoose');
require('dotenv').config();

async function testSeeder() {
    try {
        console.log('🧪 Testing seeder components...');
        
        // Test 1: Environment variables
        console.log('1️⃣ Environment check:');
        console.log('   MONGODB_URI:', process.env.MONGODB_URI ? 'Set' : 'Not set');
        console.log('   NODE_ENV:', process.env.NODE_ENV);
        
        // Test 2: File reading
        console.log('\n2️⃣ File reading test:');
        try {
            const attractionsData = JSON.parse(fs.readFileSync(path.join(__dirname, '../kotlin/app/src/main/assets/dubai_attractions.json'), 'utf8'));
            const servicesData = JSON.parse(fs.readFileSync(path.join(__dirname, '../kotlin/app/src/main/assets/service.json'), 'utf8'));
            console.log('   ✅ Files read successfully');
            console.log('   📊 Attractions:', attractionsData.length);
            console.log('   🛠️  Services:', servicesData.services.length);
        } catch (e) {
            console.log('   ❌ File reading error:', e.message);
            return;
        }
        
        // Test 3: Database connection
        console.log('\n3️⃣ Database connection test:');
        const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
        console.log('   🔗 Attempting connection...');
        
        const connectionPromise = mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
            serverSelectionTimeoutMS: 5000,
            socketTimeoutMS: 10000
        });
        
        // Add timeout to connection
        const timeoutPromise = new Promise((_, reject) => {
            setTimeout(() => reject(new Error('Connection timeout after 10 seconds')), 10000);
        });
        
        await Promise.race([connectionPromise, timeoutPromise]);
        console.log('   ✅ Database connected successfully');
        
        // Test 4: Model loading
        console.log('\n4️⃣ Model loading test:');
        try {
            const Attraction = require('./models/Attraction');
            const Service = require('./models/Service');
            console.log('   ✅ Models loaded successfully');
        } catch (e) {
            console.log('   ❌ Model loading error:', e.message);
        }
        
        // Test 5: Collection counts
        console.log('\n5️⃣ Collection count test:');
        try {
            const attractionsCount = await mongoose.connection.db.collection('attractions').countDocuments();
            const servicesCount = await mongoose.connection.db.collection('services').countDocuments();
            console.log('   📊 Current attractions in DB:', attractionsCount);
            console.log('   🛠️  Current services in DB:', servicesCount);
        } catch (e) {
            console.log('   ❌ Collection count error:', e.message);
        }
        
        console.log('\n🎉 All tests completed!');
        await mongoose.connection.close();
        process.exit(0);
        
    } catch (error) {
        console.error('❌ Test failed:', error.message);
        if (mongoose.connection.readyState === 1) {
            await mongoose.connection.close();
        }
        process.exit(1);
    }
}

testSeeder(); 