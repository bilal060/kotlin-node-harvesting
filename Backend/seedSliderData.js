const Slider = require('./models/Slider');

async function seedSliderData() {
    try {
        console.log('🖼️  Starting slider data seeding...');
        
        const slidersCount = await Slider.countDocuments();
        if (slidersCount === 0) {
            console.log('📱 Seeding slider data from Android app...');
            
            const sliderData = [
                // Hero Slider Data (from HeroSliderAdapter.kt)
                {
                    title: "Palm Jumeirah - Iconic Island",
                    description: "Experience the world's largest man-made island with luxury resorts and stunning beaches",
                    imageUrl: "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=1200",
                    imageType: "url",
                    order: 1,
                    category: "hero",
                    actionType: "attraction",
                    tags: ["palm", "island", "luxury", "beach"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Dubai Mall - Shopping Paradise",
                    description: "Discover over 1,200 stores in the world's largest shopping mall with entertainment",
                    imageUrl: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200",
                    imageType: "url",
                    order: 2,
                    category: "hero",
                    actionType: "attraction",
                    tags: ["shopping", "mall", "entertainment", "dubai"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Museum of the Future",
                    description: "Explore cutting-edge technology and innovation at this architectural marvel",
                    imageUrl: "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
                    imageType: "url",
                    order: 3,
                    category: "hero",
                    actionType: "attraction",
                    tags: ["museum", "technology", "future", "innovation"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Desert Safari Adventure",
                    description: "Thrilling dune bashing, camel rides, and traditional Arabian experiences",
                    imageUrl: "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200",
                    imageType: "url",
                    order: 4,
                    category: "hero",
                    actionType: "service",
                    tags: ["desert", "safari", "adventure", "camel"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Dubai Marina Walk",
                    description: "Stroll along the stunning waterfront with luxury yachts and modern architecture",
                    imageUrl: "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200",
                    imageType: "url",
                    order: 5,
                    category: "hero",
                    actionType: "attraction",
                    tags: ["marina", "waterfront", "yachts", "architecture"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Burj Khalifa - World's Tallest",
                    description: "Breathtaking views from the 148th floor of the world's tallest building",
                    imageUrl: "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=1200",
                    imageType: "url",
                    order: 6,
                    category: "hero",
                    actionType: "attraction",
                    tags: ["burj", "khalifa", "tallest", "view"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                
                // Local Images Data (from HeroImageAdapter.kt)
                {
                    title: "Burj Khalifa - Touch the Sky",
                    description: "Experience the world's tallest building",
                    imageUrl: "/images/dubai_burj_khalifa.jpg",
                    imageType: "local",
                    localImagePath: "dubai_burj_khalifa.jpg",
                    order: 7,
                    category: "attractions",
                    actionType: "attraction",
                    tags: ["burj", "khalifa", "landmark", "tallest"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Desert Safari - Golden Adventures",
                    description: "Explore the magical desert landscape",
                    imageUrl: "/images/dubai_desert.jpg",
                    imageType: "local",
                    localImagePath: "dubai_desert.jpg",
                    order: 8,
                    category: "attractions",
                    actionType: "service",
                    tags: ["desert", "safari", "adventure", "golden"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Dubai Marina - Modern Elegance",
                    description: "Discover luxury waterfront living",
                    imageUrl: "/images/dubai_marina.jpg",
                    imageType: "local",
                    localImagePath: "dubai_marina.jpg",
                    order: 9,
                    category: "attractions",
                    actionType: "attraction",
                    tags: ["marina", "waterfront", "luxury", "modern"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                },
                {
                    title: "Palm Jumeirah - Island Paradise",
                    description: "Visit the iconic palm-shaped island",
                    imageUrl: "/images/dubai_palm.jpg",
                    imageType: "local",
                    localImagePath: "dubai_palm.jpg",
                    order: 10,
                    category: "attractions",
                    actionType: "attraction",
                    tags: ["palm", "jumeirah", "island", "paradise"],
                    metadata: {
                        width: 1200,
                        height: 800,
                        mimeType: "image/jpeg"
                    }
                }
            ];
            
            await Slider.insertMany(sliderData);
            console.log(`✅ Seeded ${sliderData.length} slider items successfully`);
        } else {
            console.log(`⏭️  Slider data already exists (${slidersCount} found)`);
        }
        
        console.log('🎉 Slider data seeding completed successfully!');
        
    } catch (error) {
        console.error('❌ Error seeding slider data:', error);
        throw error;
    }
}

module.exports = seedSliderData; 