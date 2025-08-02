const mongoose = require('mongoose');
const config = require('./config/environment');

// Import models
const Attraction = require('./models/Attraction');
const Service = require('./models/Service');
const TourPackage = require('./models/TourPackage');

// Connect to MongoDB
mongoose.connect(config.mongodb.uri, config.mongodb.options);

const sampleAttractions = [
    {
        name: "Burj Khalifa",
        description: "The world's tallest building with stunning city views",
        shortDescription: "World's tallest building",
        category: "landmark",
        location: {
            address: "1 Sheikh Mohammed bin Rashid Blvd, Downtown Dubai",
            area: "Downtown Dubai"
        },
        images: [{ url: "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800", isPrimary: true }],
        timing: {
            openingHours: {
                monday: { open: "09:00", close: "23:00", isOpen: true },
                tuesday: { open: "09:00", close: "23:00", isOpen: true },
                wednesday: { open: "09:00", close: "23:00", isOpen: true },
                thursday: { open: "09:00", close: "23:00", isOpen: true },
                friday: { open: "09:00", close: "23:00", isOpen: true },
                saturday: { open: "09:00", close: "23:00", isOpen: true },
                sunday: { open: "09:00", close: "23:00", isOpen: true }
            },
            estimatedVisitTime: 120
        },
        ticketPrices: {
            adult: 149,
            child: 95,
            currency: "AED"
        },
        ratings: { average: 4.8, totalReviews: 15420 },
        isPopular: true,
        isFeatured: true
    }
];

const sampleServices = [
    {
        name: "Desert Safari Adventure",
        description: "Experience dune bashing and camel riding in the Dubai desert",
        shortDescription: "Thrilling desert adventure",
        category: "entertainment",
        location: { address: "Dubai Desert", area: "Dubai Desert" },
        images: [{ url: "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800", isPrimary: true }],
        pricing: { basePrice: 250, currency: "AED", pricingType: "per_person" },
        availability: {
            operatingHours: {
                monday: { open: "15:00", close: "22:00", isOpen: true },
                tuesday: { open: "15:00", close: "22:00", isOpen: true },
                wednesday: { open: "15:00", close: "22:00", isOpen: true },
                thursday: { open: "15:00", close: "22:00", isOpen: true },
                friday: { open: "15:00", close: "22:00", isOpen: true },
                saturday: { open: "15:00", close: "22:00", isOpen: true },
                sunday: { open: "15:00", close: "22:00", isOpen: true }
            },
            duration: 420
        },
        provider: { name: "Dubai Desert Adventures" },
        ratings: { average: 4.7, totalReviews: 2340 },
        isPopular: true
    }
];

const sampleTourPackages = [
    {
        name: "Dubai Essential Experience",
        description: "Complete Dubai experience with iconic attractions",
        shortDescription: "Essential Dubai tour",
        category: "essential",
        duration: { days: 3, nights: 2 },
        images: [{ url: "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800", isPrimary: true }],
        pricing: { adult: 1200, child: 800, currency: "AED" },
        itinerary: [
            {
                day: 1,
                title: "Arrival & City Orientation",
                activities: [
                    { time: "14:00", activity: "Hotel check-in", location: "Hotel", duration: 30, type: "hotel" },
                    { time: "16:00", activity: "City tour", location: "Dubai", duration: 180, type: "attraction" }
                ],
                meals: { breakfast: false, lunch: false, dinner: true }
            }
        ],
        provider: { name: "Dubai Discoveries Tours" },
        ratings: { average: 4.6, totalReviews: 890 },
        isPopular: true
    }
];

async function seedData() {
    try {
        console.log('🌱 Starting Dubai data seeding...');
        
        // Clear existing data
        await Attraction.deleteMany({});
        await Service.deleteMany({});
        await TourPackage.deleteMany({});
        
        // Insert data
        const attractions = await Attraction.insertMany(sampleAttractions);
        const services = await Service.insertMany(sampleServices);
        const packages = await TourPackage.insertMany(sampleTourPackages);
        
        console.log('🎉 Seeding completed!');
        console.log(`📊 Attractions: ${attractions.length}, Services: ${services.length}, Packages: ${packages.length}`);
        
    } catch (error) {
        console.error('❌ Error:', error);
    } finally {
        mongoose.connection.close();
    }
}

seedData(); 