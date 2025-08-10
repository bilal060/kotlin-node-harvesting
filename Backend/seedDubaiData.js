// Import models
const mongoose = require('mongoose');
require('dotenv').config();
const Attraction = require('./models/Attraction');
const Service = require('./models/Service');
const TourPackage = require('./models/TourPackage');

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
        
        // Connect to database first
        console.log('🔗 Connecting to database...');
        const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://dbuser:Bil%40l112@cluster0.ey6gj6g.mongodb.net/sync_data';
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
            serverSelectionTimeoutMS: 10000
        });
        console.log('✅ Database connected successfully');
        
        // Clear existing data
        console.log('🧹 Clearing existing data...');
        await Attraction.deleteMany({});
        await Service.deleteMany({});
        await TourPackage.deleteMany({});
        console.log('✅ Existing data cleared');
        
        // Insert data
        console.log('📝 Inserting new data...');
        const attractions = await Attraction.insertMany(sampleAttractions);
        const services = await Service.insertMany(sampleServices);
        const packages = await TourPackage.insertMany(sampleTourPackages);
        
        console.log('🎉 Seeding completed!');
        console.log(`📊 Attractions: ${attractions.length}, Services: ${services.length}, Packages: ${packages.length}`);
        
        // Close database connection and exit
        await mongoose.connection.close();
        console.log('🔌 Database connection closed');
        process.exit(0);
        
    } catch (error) {
        console.error('❌ Error:', error);
        
        // Close database connection on error
        if (mongoose.connection.readyState === 1) {
            await mongoose.connection.close();
            console.log('🔌 Database connection closed due to error');
        }
        
        process.exit(1);
    }
}

// Run the seeder if this file is executed directly
if (require.main === module) {
    seedData();
}

module.exports = seedData; 