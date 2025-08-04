#!/usr/bin/env node

const mongoose = require('mongoose');
const config = require('./config/environment');
const TourGallery = require('./models/TourGallery');

async function testTourGalleryAPI() {
    try {
        console.log('🖼️  Testing Tour Gallery API...');
        
        // Connect to MongoDB
        await mongoose.connect(config.mongodb.uri, config.mongodb.options);
        console.log('✅ Connected to MongoDB');
        
        // Test 1: Get all gallery items
        console.log('\n📋 Test 1: Get all gallery items');
        const allItems = await TourGallery.find({ isActive: true }).sort({ order: 1 }).limit(5);
        console.log(`Found ${allItems.length} active gallery items`);
        if (allItems.length > 0) {
            console.log('Sample item:', {
                title: allItems[0].title,
                type: allItems[0].type,
                category: allItems[0].category,
                filePath: allItems[0].filePath
            });
        }
        
        // Test 2: Get images only
        console.log('\n🖼️  Test 2: Get images only');
        const images = await TourGallery.find({ type: 'image', isActive: true }).sort({ order: 1 }).limit(3);
        console.log(`Found ${images.length} images`);
        images.forEach((img, index) => {
            console.log(`  ${index + 1}. ${img.title} (${img.category})`);
        });
        
        // Test 3: Get videos only
        console.log('\n🎥 Test 3: Get videos only');
        const videos = await TourGallery.find({ type: 'video', isActive: true }).sort({ order: 1 }).limit(3);
        console.log(`Found ${videos.length} videos`);
        videos.forEach((video, index) => {
            console.log(`  ${index + 1}. ${video.title} (${video.duration}s, ${video.category})`);
        });
        
        // Test 4: Get hero items
        console.log('\n⭐ Test 4: Get hero items');
        const heroItems = await TourGallery.find({ category: 'hero', isActive: true }).sort({ order: 1 }).limit(3);
        console.log(`Found ${heroItems.length} hero items`);
        heroItems.forEach((item, index) => {
            console.log(`  ${index + 1}. ${item.title} (${item.type})`);
        });
        
        // Test 5: Get by category
        console.log('\n📂 Test 5: Get by category (attractions)');
        const attractionItems = await TourGallery.find({ category: 'attractions', isActive: true }).sort({ order: 1 }).limit(3);
        console.log(`Found ${attractionItems.length} attraction items`);
        attractionItems.forEach((item, index) => {
            console.log(`  ${index + 1}. ${item.title} (${item.type})`);
        });
        
        // Test 6: Get featured items
        console.log('\n🏆 Test 6: Get featured items');
        const featuredItems = await TourGallery.find({ isFeatured: true, isActive: true }).sort({ order: 1 }).limit(3);
        console.log(`Found ${featuredItems.length} featured items`);
        featuredItems.forEach((item, index) => {
            console.log(`  ${index + 1}. ${item.title} (${item.type}, ${item.category})`);
        });
        
        // Test 7: Group by type (simulate API response)
        console.log('\n📊 Test 7: Group by type');
        const allGalleryItems = await TourGallery.find({ isActive: true }).sort({ order: 1 });
        const groupedData = {
            images: allGalleryItems.filter(item => item.type === 'image'),
            videos: allGalleryItems.filter(item => item.type === 'video')
        };
        console.log(`Grouped Summary:`);
        console.log(`  Images: ${groupedData.images.length}`);
        console.log(`  Videos: ${groupedData.videos.length}`);
        console.log(`  Total: ${allGalleryItems.length}`);
        
        // Test 8: Statistics
        console.log('\n📈 Test 8: Gallery Statistics');
        const totalItems = await TourGallery.countDocuments();
        const imageCount = await TourGallery.countDocuments({ type: 'image' });
        const videoCount = await TourGallery.countDocuments({ type: 'video' });
        const activeCount = await TourGallery.countDocuments({ isActive: true });
        const featuredCount = await TourGallery.countDocuments({ isFeatured: true });
        
        console.log(`Statistics:`);
        console.log(`  Total items: ${totalItems}`);
        console.log(`  Images: ${imageCount}`);
        console.log(`  Videos: ${videoCount}`);
        console.log(`  Active: ${activeCount}`);
        console.log(`  Featured: ${featuredCount}`);
        
        console.log('\n✅ All tour gallery API tests completed successfully!');
        
    } catch (error) {
        console.error('❌ Error testing tour gallery API:', error);
    } finally {
        await mongoose.connection.close();
        console.log('🔌 Disconnected from MongoDB');
    }
}

// Run the test
testTourGalleryAPI(); 