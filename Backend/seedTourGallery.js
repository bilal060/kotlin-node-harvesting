const TourGallery = require('./models/TourGallery');

async function seedTourGallery() {
    try {
        console.log('🖼️  Starting tour gallery data seeding...');
        
        const galleryCount = await TourGallery.countDocuments();
        if (galleryCount === 0) {
            console.log('📸 Seeding tour gallery data...');
            
            const galleryData = [
                // Hero Images
                {
                    title: "Burj Khalifa Sunset",
                    description: "Stunning sunset view of Burj Khalifa from Dubai Mall",
                    type: "image",
                    filePath: "/uploads/images/burj_khalifa_sunset.jpg",
                    fileName: "burj_khalifa_sunset.jpg",
                    fileSize: 2048576,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    category: "hero",
                    tags: ["burj", "khalifa", "sunset", "dubai", "landmark"],
                    isActive: true,
                    isFeatured: true,
                    order: 1
                },
                {
                    title: "Palm Jumeirah Aerial",
                    description: "Aerial view of the iconic Palm Jumeirah island",
                    type: "image",
                    filePath: "/uploads/images/palm_jumeirah_aerial.jpg",
                    fileName: "palm_jumeirah_aerial.jpg",
                    fileSize: 3145728,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    category: "hero",
                    tags: ["palm", "jumeirah", "aerial", "island", "luxury"],
                    isActive: true,
                    isFeatured: true,
                    order: 2
                },
                {
                    title: "Dubai Marina Night",
                    description: "Beautiful night view of Dubai Marina with illuminated skyscrapers",
                    type: "image",
                    filePath: "/uploads/images/dubai_marina_night.jpg",
                    fileName: "dubai_marina_night.jpg",
                    fileSize: 2621440,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    category: "hero",
                    tags: ["marina", "night", "skyscrapers", "illuminated"],
                    isActive: true,
                    isFeatured: true,
                    order: 3
                },
                
                // Attraction Images
                {
                    title: "Museum of the Future Exterior",
                    description: "Architectural marvel of the Museum of the Future",
                    type: "image",
                    filePath: "/uploads/images/museum_future_exterior.jpg",
                    fileName: "museum_future_exterior.jpg",
                    fileSize: 1835008,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "attractions",
                    tags: ["museum", "future", "architecture", "technology"],
                    isActive: true,
                    isFeatured: false,
                    order: 4
                },
                {
                    title: "Dubai Miracle Garden",
                    description: "Colorful flower displays at Dubai Miracle Garden",
                    type: "image",
                    filePath: "/uploads/images/miracle_garden.jpg",
                    fileName: "miracle_garden.jpg",
                    fileSize: 2097152,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "attractions",
                    tags: ["garden", "flowers", "colorful", "nature"],
                    isActive: true,
                    isFeatured: false,
                    order: 5
                },
                {
                    title: "Wild Wadi Waterpark",
                    description: "Exciting water slides and attractions at Wild Wadi",
                    type: "image",
                    filePath: "/uploads/images/wild_wadi.jpg",
                    fileName: "wild_wadi.jpg",
                    fileSize: 2359296,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "attractions",
                    tags: ["waterpark", "slides", "fun", "family"],
                    isActive: true,
                    isFeatured: false,
                    order: 6
                },
                
                // Service Images
                {
                    title: "Luxury Hotel Suite",
                    description: "Elegant luxury hotel suite with city views",
                    type: "image",
                    filePath: "/uploads/images/luxury_hotel_suite.jpg",
                    fileName: "luxury_hotel_suite.jpg",
                    fileSize: 1572864,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "services",
                    tags: ["hotel", "luxury", "suite", "accommodation"],
                    isActive: true,
                    isFeatured: false,
                    order: 7
                },
                {
                    title: "Desert Safari Camp",
                    description: "Traditional desert camp setup for safari experiences",
                    type: "image",
                    filePath: "/uploads/images/desert_safari_camp.jpg",
                    fileName: "desert_safari_camp.jpg",
                    fileSize: 2097152,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "services",
                    tags: ["desert", "safari", "camp", "traditional"],
                    isActive: true,
                    isFeatured: false,
                    order: 8
                },
                
                // Package Images
                {
                    title: "Dubai City Tour",
                    description: "Comprehensive city tour covering major attractions",
                    type: "image",
                    filePath: "/uploads/images/dubai_city_tour.jpg",
                    fileName: "dubai_city_tour.jpg",
                    fileSize: 1835008,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "packages",
                    tags: ["city", "tour", "attractions", "comprehensive"],
                    isActive: true,
                    isFeatured: false,
                    order: 9
                },
                {
                    title: "Honeymoon Package",
                    description: "Romantic honeymoon experience in Dubai",
                    type: "image",
                    filePath: "/uploads/images/honeymoon_package.jpg",
                    fileName: "honeymoon_package.jpg",
                    fileSize: 1572864,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "packages",
                    tags: ["honeymoon", "romantic", "couple", "luxury"],
                    isActive: true,
                    isFeatured: false,
                    order: 10
                },
                {
                    title: "Abu Dhabi City Tour",
                    description: "Comprehensive Abu Dhabi city tour with cultural landmarks",
                    type: "image",
                    filePath: "/uploads/images/abu_dhabi_city_tour.jpg",
                    fileName: "abu_dhabi_city_tour.jpg",
                    fileSize: 2097152,
                    mimeType: "image/jpeg",
                    dimensions: {
                        width: 1600,
                        height: 900
                    },
                    category: "packages",
                    tags: ["abu dhabi", "city tour", "cultural", "landmarks"],
                    isActive: true,
                    isFeatured: true,
                    order: 11
                },
                
                // Videos
                {
                    title: "Dubai City Overview",
                    description: "Comprehensive video overview of Dubai's major attractions",
                    type: "video",
                    filePath: "/uploads/videos/dubai_city_overview.mp4",
                    fileName: "dubai_city_overview.mp4",
                    fileSize: 52428800, // 50MB
                    mimeType: "video/mp4",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    duration: 180, // 3 minutes
                    thumbnail: "/uploads/thumbnails/dubai_city_overview_thumb.jpg",
                    category: "general",
                    tags: ["dubai", "city", "overview", "attractions"],
                    isActive: true,
                    isFeatured: true,
                    order: 11
                },
                {
                    title: "Burj Khalifa Experience",
                    description: "Virtual tour of Burj Khalifa observation deck",
                    type: "video",
                    filePath: "/uploads/videos/burj_khalifa_experience.mp4",
                    fileName: "burj_khalifa_experience.mp4",
                    fileSize: 41943040, // 40MB
                    mimeType: "video/mp4",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    duration: 120, // 2 minutes
                    thumbnail: "/uploads/thumbnails/burj_khalifa_experience_thumb.jpg",
                    category: "attractions",
                    tags: ["burj", "khalifa", "observation", "deck", "view"],
                    isActive: true,
                    isFeatured: true,
                    order: 12
                },
                {
                    title: "Desert Safari Adventure",
                    description: "Thrilling desert safari experience with dune bashing",
                    type: "video",
                    filePath: "/uploads/videos/desert_safari_adventure.mp4",
                    fileName: "desert_safari_adventure.mp4",
                    fileSize: 62914560, // 60MB
                    mimeType: "video/mp4",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    duration: 240, // 4 minutes
                    thumbnail: "/uploads/thumbnails/desert_safari_adventure_thumb.jpg",
                    category: "services",
                    tags: ["desert", "safari", "adventure", "dune", "bashing"],
                    isActive: true,
                    isFeatured: false,
                    order: 13
                },
                {
                    title: "Dubai Fountain Show",
                    description: "Spectacular Dubai Fountain show with music and lights",
                    type: "video",
                    filePath: "/uploads/videos/dubai_fountain_show.mp4",
                    fileName: "dubai_fountain_show.mp4",
                    fileSize: 31457280, // 30MB
                    mimeType: "video/mp4",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    duration: 90, // 1.5 minutes
                    thumbnail: "/uploads/thumbnails/dubai_fountain_show_thumb.jpg",
                    category: "attractions",
                    tags: ["fountain", "show", "music", "lights", "spectacular"],
                    isActive: true,
                    isFeatured: false,
                    order: 14
                },
                {
                    title: "Luxury Hotel Tour",
                    description: "Virtual tour of luxury hotel facilities and amenities",
                    type: "video",
                    filePath: "/uploads/videos/luxury_hotel_tour.mp4",
                    fileName: "luxury_hotel_tour.mp4",
                    fileSize: 47185920, // 45MB
                    mimeType: "video/mp4",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    duration: 150, // 2.5 minutes
                    thumbnail: "/uploads/thumbnails/luxury_hotel_tour_thumb.jpg",
                    category: "services",
                    tags: ["hotel", "luxury", "facilities", "amenities", "tour"],
                    isActive: true,
                    isFeatured: false,
                    order: 15
                },
                {
                    title: "Abu Dhabi Louvre Museum",
                    description: "Explore the magnificent Louvre Abu Dhabi museum and its art collections",
                    type: "video",
                    filePath: "/uploads/videos/abu_dhabi_louvre.mp4",
                    fileName: "abu_dhabi_louvre.mp4",
                    fileSize: 52428800, // 50MB
                    mimeType: "video/mp4",
                    dimensions: {
                        width: 1920,
                        height: 1080
                    },
                    duration: 180, // 3 minutes
                    thumbnail: "/uploads/thumbnails/abu_dhabi_louvre_thumb.jpg",
                    category: "packages",
                    tags: ["abu dhabi", "louvre", "museum", "art", "cultural"],
                    isActive: true,
                    isFeatured: true,
                    order: 16
                }
            ];
            
            await TourGallery.insertMany(galleryData);
            console.log(`✅ Seeded ${galleryData.length} gallery items successfully`);
            
            // Log summary by type
            const imageCount = galleryData.filter(item => item.type === 'image').length;
            const videoCount = galleryData.filter(item => item.type === 'video').length;
            console.log(`📊 Gallery Summary:`);
            console.log(`   Images: ${imageCount}`);
            console.log(`   Videos: ${videoCount}`);
            console.log(`   Hero: ${galleryData.filter(item => item.category === 'hero').length}`);
            console.log(`   Attractions: ${galleryData.filter(item => item.category === 'attractions').length}`);
            console.log(`   Services: ${galleryData.filter(item => item.category === 'services').length}`);
            console.log(`   Packages: ${galleryData.filter(item => item.category === 'packages').length}`);
            
        } else {
            console.log(`⏭️  Tour gallery data already exists (${galleryCount} found)`);
        }
        
        console.log('🎉 Tour gallery data seeding completed successfully!');
        
    } catch (error) {
        console.error('❌ Error seeding tour gallery data:', error);
        throw error;
    }
}

module.exports = seedTourGallery; 