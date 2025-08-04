#!/usr/bin/env node

const mongoose = require('mongoose');
const config = require('./config/environment');
const TourPackage = require('./models/TourPackage');

async function testTourPackages() {
    try {
        console.log('📦 Testing Tour Packages...');
        
        // Connect to MongoDB
        await mongoose.connect(config.mongodb.uri, config.mongodb.options);
        console.log('✅ Connected to MongoDB');
        
        // Test 1: Get all tour packages
        console.log('\n📋 Test 1: Get all tour packages');
        const allPackages = await TourPackage.find({}).sort({ createdAt: -1 });
        console.log(`Found ${allPackages.length} tour packages`);
        
        allPackages.forEach((pkg, index) => {
            console.log(`\n${index + 1}. ${pkg.name}`);
            console.log(`   Category: ${pkg.category}`);
            console.log(`   Duration: ${pkg.duration.days} days, ${pkg.duration.nights} nights`);
            console.log(`   Price: ${pkg.pricing.adult} AED (Adult)`);
            console.log(`   Rating: ${pkg.ratings.average}/5 (${pkg.ratings.totalReviews} reviews)`);
            console.log(`   Popular: ${pkg.isPopular ? 'Yes' : 'No'}`);
            console.log(`   Featured: ${pkg.isFeatured ? 'Yes' : 'No'}`);
            
            if (pkg.tags && pkg.tags.length > 0) {
                console.log(`   Tags: ${pkg.tags.join(', ')}`);
            }
            
            if (pkg.highlights && pkg.highlights.length > 0) {
                console.log(`   Highlights: ${pkg.highlights.slice(0, 2).join(', ')}...`);
            }
        });
        
        // Test 2: Get packages by category
        console.log('\n📂 Test 2: Get packages by category');
        const culturalPackages = await TourPackage.find({ category: 'cultural' });
        console.log(`Cultural packages: ${culturalPackages.length}`);
        culturalPackages.forEach(pkg => {
            console.log(`   - ${pkg.name}`);
        });
        
        const essentialPackages = await TourPackage.find({ category: 'essential' });
        console.log(`Essential packages: ${essentialPackages.length}`);
        essentialPackages.forEach(pkg => {
            console.log(`   - ${pkg.name}`);
        });
        
        // Test 3: Get popular packages
        console.log('\n🏆 Test 3: Get popular packages');
        const popularPackages = await TourPackage.find({ isPopular: true });
        console.log(`Popular packages: ${popularPackages.length}`);
        popularPackages.forEach(pkg => {
            console.log(`   - ${pkg.name} (${pkg.category})`);
        });
        
        // Test 4: Get featured packages
        console.log('\n⭐ Test 4: Get featured packages');
        const featuredPackages = await TourPackage.find({ isFeatured: true });
        console.log(`Featured packages: ${featuredPackages.length}`);
        featuredPackages.forEach(pkg => {
            console.log(`   - ${pkg.name} (${pkg.category})`);
        });
        
        // Test 5: Package statistics
        console.log('\n📈 Test 5: Package Statistics');
        const totalPackages = await TourPackage.countDocuments();
        const culturalCount = await TourPackage.countDocuments({ category: 'cultural' });
        const essentialCount = await TourPackage.countDocuments({ category: 'essential' });
        const popularCount = await TourPackage.countDocuments({ isPopular: true });
        const featuredCount = await TourPackage.countDocuments({ isFeatured: true });
        
        console.log(`Statistics:`);
        console.log(`  Total packages: ${totalPackages}`);
        console.log(`  Cultural: ${culturalCount}`);
        console.log(`  Essential: ${essentialCount}`);
        console.log(`  Popular: ${popularCount}`);
        console.log(`  Featured: ${featuredCount}`);
        
        // Test 6: Abu Dhabi specific package
        console.log('\n🏛️  Test 6: Abu Dhabi Package Details');
        const abuDhabiPackage = await TourPackage.findOne({ name: { $regex: /Abu Dhabi/i } });
        if (abuDhabiPackage) {
            console.log(`Found Abu Dhabi package: ${abuDhabiPackage.name}`);
            console.log(`Description: ${abuDhabiPackage.description}`);
            console.log(`Duration: ${abuDhabiPackage.duration.days} day(s)`);
            console.log(`Price: ${abuDhabiPackage.pricing.adult} AED (Adult)`);
            console.log(`Rating: ${abuDhabiPackage.ratings.average}/5`);
            
            if (abuDhabiPackage.itinerary && abuDhabiPackage.itinerary.length > 0) {
                console.log(`Itinerary activities: ${abuDhabiPackage.itinerary[0].activities.length} activities`);
                abuDhabiPackage.itinerary[0].activities.slice(0, 3).forEach((activity, index) => {
                    console.log(`   ${index + 1}. ${activity.time} - ${activity.activity}`);
                });
            }
            
            if (abuDhabiPackage.includes && abuDhabiPackage.includes.length > 0) {
                console.log(`Includes: ${abuDhabiPackage.includes.slice(0, 3).join(', ')}...`);
            }
            
            if (abuDhabiPackage.highlights && abuDhabiPackage.highlights.length > 0) {
                console.log(`Highlights: ${abuDhabiPackage.highlights.slice(0, 2).join(', ')}...`);
            }
        } else {
            console.log('❌ Abu Dhabi package not found');
        }
        
        console.log('\n✅ All tour package tests completed successfully!');
        
    } catch (error) {
        console.error('❌ Error testing tour packages:', error);
    } finally {
        await mongoose.connection.close();
        console.log('🔌 Disconnected from MongoDB');
    }
}

// Run the test
testTourPackages(); 