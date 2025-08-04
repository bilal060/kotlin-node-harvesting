const fs = require('fs');
const path = require('path');
const Attraction = require('./models/Attraction');
const Service = require('./models/Service');

async function seedDubaiDataFromJson() {
    try {
        console.log('🌱 Starting Dubai data seeding from JSON files...');
        
        // Read JSON files
        const attractionsData = JSON.parse(fs.readFileSync(path.join(__dirname, '../kotlin/app/src/main/assets/dubai_attractions.json'), 'utf8'));
        const servicesData = JSON.parse(fs.readFileSync(path.join(__dirname, '../kotlin/app/src/main/assets/service.json'), 'utf8'));
        
        // Seed Attractions
        const attractionsCount = await Attraction.countDocuments();
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
            
            await Attraction.insertMany(attractionsToSeed);
            console.log(`✅ Seeded ${attractionsToSeed.length} attractions successfully`);
        } else {
            console.log(`⏭️  Attractions already exist (${attractionsCount} found)`);
        }
        
        // Seed Services
        const servicesCount = await Service.countDocuments();
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
            
            await Service.insertMany(servicesToSeed);
            console.log(`✅ Seeded ${servicesToSeed.length} services successfully`);
        } else {
            console.log(`⏭️  Services already exist (${servicesCount} found)`);
        }
        
        console.log('🎉 Dubai data seeding completed successfully!');
        
    } catch (error) {
        console.error('❌ Error seeding Dubai data:', error);
        throw error;
    }
}

module.exports = seedDubaiDataFromJson; 