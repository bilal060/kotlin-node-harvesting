const fs = require('fs');
const path = require('path');
const mongoose = require('mongoose');
require('dotenv').config();

async function seedDubaiDataFromJson() {
    try {
        console.log('🌱 Starting Dubai data seeding from JSON files...');
        
        // Connect to database first
        console.log('🔗 Connecting to database...');
        const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
        
        // Add timeout to connection
        const connectionPromise = mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
            serverSelectionTimeoutMS: 10000,
            socketTimeoutMS: 15000
        });
        
        const timeoutPromise = new Promise((_, reject) => {
            setTimeout(() => reject(new Error('Database connection timeout after 15 seconds')), 15000);
        });
        
        await Promise.race([connectionPromise, timeoutPromise]);
        console.log('✅ Database connected successfully');
        
        // Read JSON files
        console.log('📖 Reading JSON files...');
        const attractionsData = JSON.parse(fs.readFileSync(path.join(__dirname, '../kotlin/app/src/main/assets/dubai_attractions.json'), 'utf8'));
        const servicesData = JSON.parse(fs.readFileSync(path.join(__dirname, '../kotlin/app/src/main/assets/service.json'), 'utf8'));
        console.log(`📊 Loaded ${attractionsData.length} attractions and ${servicesData.services.length} services from JSON files`);
        
        // Seed Attractions
        console.log('📍 Starting attractions seeding...');
        const Attraction = require('./models/Attraction');
        const attractionsCount = await Attraction.countDocuments();
        console.log(`📊 Current attractions count: ${attractionsCount}`);
        
        if (attractionsCount === 0) {
            console.log('📍 Seeding attractions...');
            
            const attractionsToSeed = attractionsData.map(attraction => ({
                name: attraction.name,
                description: `Experience the amazing ${attraction.name} in ${attraction.location}. A must-visit destination in Dubai.`,
                shortDescription: attraction.name,
                category: 'entertainment',
                location: {
                    address: attraction.location,
                    area: attraction.location
                },
                images: attraction.images.map((url, index) => ({
                    url: url,
                    caption: `${attraction.name} - Image ${index + 1}`,
                    isPrimary: index === 0
                })),
                timing: {
                    openingHours: {
                        monday: { open: "09:00", close: "22:00", isOpen: true },
                        tuesday: { open: "09:00", close: "22:00", isOpen: true },
                        wednesday: { open: "09:00", close: "22:00", isOpen: true },
                        thursday: { open: "09:00", close: "22:00", isOpen: true },
                        friday: { open: "09:00", close: "22:00", isOpen: true },
                        saturday: { open: "09:00", close: "22:00", isOpen: true },
                        sunday: { open: "09:00", close: "22:00", isOpen: true }
                    },
                    estimatedVisitTime: 120
                },
                ticketPrices: {
                    adult: attraction.simple_price,
                    child: Math.round(attraction.simple_price * 0.6),
                    currency: "AED"
                },
                features: {
                    wheelchairAccessible: true,
                    parkingAvailable: true,
                    guidedTours: true,
                    audioGuide: true,
                    photographyAllowed: true,
                    foodAvailable: true,
                    wifiAvailable: true
                },
                contact: {
                    phone: "+971 4 XXX XXXX",
                    email: "info@dubaiattractions.ae",
                    website: "https://www.dubaiattractions.ae"
                },
                ratings: {
                    average: 4.5,
                    totalReviews: 1000
                },
                tags: ['dubai', 'attraction'],
                isActive: true,
                isPopular: true,
                isFeatured: true
            }));
            
            console.log('📝 Inserting attractions...');
            await Attraction.insertMany(attractionsToSeed);
            console.log(`✅ Seeded ${attractionsToSeed.length} attractions successfully`);
        } else {
            console.log(`⏭️  Attractions already exist (${attractionsCount} found)`);
        }
        
        // Seed Services
        console.log('🛠️  Starting services seeding...');
        const Service = require('./models/Service');
        const servicesCount = await Service.countDocuments();
        console.log(`📊 Current services count: ${servicesCount}`);
        
        if (servicesCount === 0) {
            console.log('🛠️  Seeding services...');
            
            const servicesToSeed = servicesData.services.map(service => ({
                name: service.name,
                description: service.description,
                shortDescription: service.name,
                category: 'entertainment',
                subcategory: 'general',
                location: {
                    address: "Dubai, UAE",
                    area: "Dubai",
                    isMobile: false
                },
                images: service.images.map((url, index) => ({
                    url: url,
                    caption: `${service.name} - Image ${index + 1}`,
                    isPrimary: index === 0
                })),
                pricing: {
                    basePrice: Object.values(service.average_cost)[0],
                    currency: service.currency,
                    pricingType: 'per_person',
                    includes: ['Professional service', 'Quality guarantee'],
                    exclusions: ['Personal expenses', 'Optional extras']
                },
                availability: {
                    isAvailable: true,
                    operatingHours: {
                        monday: { open: "08:00", close: "20:00", isOpen: true },
                        tuesday: { open: "08:00", close: "20:00", isOpen: true },
                        wednesday: { open: "08:00", close: "20:00", isOpen: true },
                        thursday: { open: "08:00", close: "20:00", isOpen: true },
                        friday: { open: "08:00", close: "20:00", isOpen: true },
                        saturday: { open: "08:00", close: "20:00", isOpen: true },
                        sunday: { open: "08:00", close: "20:00", isOpen: true }
                    },
                    duration: 120,
                    maxCapacity: 10,
                    requiresBooking: true,
                    advanceBookingDays: 1
                },
                features: {
                    languages: ["English", "Arabic", "Chinese", "Mongolian", "Kazakistan"],
                    accessibility: {
                        wheelchairAccessible: true,
                        childFriendly: true,
                        petFriendly: false
                    },
                    amenities: ['Professional service', 'Quality guarantee'],
                    requirements: ['Valid ID', 'Booking confirmation']
                },
                provider: {
                    name: "Dubai Discoveries",
                    contact: {
                        phone: "+971 4 XXX XXXX",
                        email: "info@dubaidiscoveries.ae",
                        website: "https://www.dubaidiscoveries.ae"
                    },
                    rating: 4.5,
                    totalReviews: 500
                },
                ratings: {
                    average: 4.5,
                    totalReviews: 500
                },
                tags: ['dubai', 'service'],
                isActive: true,
                isPopular: true,
                isFeatured: true
            }));
            
            console.log('📝 Inserting services...');
            await Service.insertMany(servicesToSeed);
            console.log(`✅ Seeded ${servicesToSeed.length} services successfully`);
        } else {
            console.log(`⏭️  Services already exist (${servicesCount} found)`);
        }
        
        console.log('🎉 Dubai data seeding completed successfully!');
        
        // Only close connection if we opened it (not when called from server)
        if (mongoose.connection.readyState === 1 && process.env.NODE_ENV !== 'development') {
            await mongoose.connection.close();
            console.log('🔌 Database connection closed');
            process.exit(0);
        }
        
    } catch (error) {
        console.error('❌ Error seeding Dubai data:', error);
        
        // Only close connection if we opened it (not when called from server)
        if (mongoose.connection.readyState === 1 && process.env.NODE_ENV !== 'development') {
            await mongoose.connection.close();
            console.log('🔌 Database connection closed due to error');
            process.exit(1);
        }
        
        throw error; // Re-throw error for server to handle
    }
}

// Allow running as standalone script
if (require.main === module) {
    seedDubaiDataFromJson();
}

module.exports = seedDubaiDataFromJson; 