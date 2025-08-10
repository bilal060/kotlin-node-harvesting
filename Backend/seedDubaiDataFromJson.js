const fs = require('fs');
const path = require('path');
const mongoose = require('mongoose');
require('dotenv').config();
const Attraction = require('./models/Attraction');
const Service = require('./models/Service');

async function seedDubaiDataFromJson() {
    try {
        console.log('🌱 Starting Dubai data seeding from JSON files...');
        
        // Check if already connected (when called from server)
        if (mongoose.connection.readyState !== 1) {
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
        } else {
            console.log('✅ Database already connected');
        }
        
        // Read JSON files
        console.log('📖 Reading JSON files...');
        const attractionsData = [
            {
              "name": "Burj Khalifa (At The Top)",
              "simple_price": 170,
              "premium_price": 315,
              "location": "Downtown Dubai",
              "images": [
                "https://www.burjkhalifa.ae/en/wp-content/uploads/2018/10/The-Burj-Khalifa-Exterior.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/9/9f/Burj_Khalifa_Dubai.jpg",
                "https://www.thedubaiMall.com/images/burj-khalifa-dubai.jpg",
                "https://cdn.getyourguide.com/img/tour/5c20e3cd35422.jpeg/145.jpg"
              ]
            },
            {
              "name": "Museum of the Future",
              "simple_price": 164,
              "premium_price": 230,
              "location": "Sheikh Zayed Road, Dubai",
              "images": [
                "https://cdn.britannica.com/44/1944-004-55A362F7.jpg",
                "https://www.dubaitravelplanner.com/wp-content/uploads/2022/02/Museum-of-the-Future-building.jpg",
                "https://cdn.getyourguide.com/img/tour/5f99dfe032b3a.jpeg",
                "https://cdn.vox-cdn.com/thumbor/nH_J3Ftd6Q3-Qj3z4rOZLKlf9Ss=/0x0:6000x3375/1200x800/filters:focal(2504x681:3352x1529)/cdn.vox-cdn.com/uploads/chorus_image/image/68189691/GettyImages_1238788136.0.jpg"
              ]
            },
            {
              "name": "Dubai Miracle Garden",
              "simple_price": 90,
              "premium_price": 135,
              "location": "Al Barsha South, Dubai",
              "images": [
                "https://www.miraclegarden.ae/images/garden_1.jpg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1611825160/1000x750_q8dj3i.jpg",
                "https://cdn.tourradar.com/s3/tour/1500x800/137109_aeb2fbd0.jpg",
                "https://www.visitdubai.com/-/media/gardens/miracle-garden/hero/hero-1.jpg"
              ]
            },
            {
              "name": "Aura Sky Pool",
              "simple_price": 215,
              "premium_price": 265,
              "location": "Palm Tower, Dubai",
              "images": [
                "https://auraskypool.com/wp-content/uploads/2021/11/AURA-Skypool-Lounge-Dubai-Deck-Infinity-View-scaled.jpg",
                "https://cdn.tourism-review.com/images/news/featured/aura-sky-pool-dubai-infinity-pool-18311.jpg",
                "https://images.adsttc.com/media/images/5f53/5c0a/b357/0a41/f000/03e3/large_jpg/Aura_Sky_Pool_10.jpg",
                "https://dubaiexpatmedia.com/wp-content/uploads/2020/12/Aura-Sky-Pool-Dubai-1024x683.jpg"
              ]
            },
            {
              "name": "Wild Wadi Waterpark",
              "simple_price": 314,
              "premium_price": 369,
              "location": "Near Burj Al Arab, Dubai",
              "images": [
                "https://www.jumeirah.com/-/media/project/jumeirah/jumeirah/images/hotels-and-resorts/dubai/wild-wadi/hero-images/overview.jpg",
                "https://cdn.getyourguide.com/img/location/5f64466c2acb4.jpeg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1559809472/wild-wadi-waterpark-ovjhsj.jpg",
                "https://cdn.ek.ae/wild-wadi-waterpark-dubai_17e226d1.jpg"
              ]
            },
            {
              "name": "Dubai Safari Park",
              "simple_price": 65,
              "premium_price": 95,
              "location": "Al Warqa, Dubai",
              "images": [
                "https://dnhg.org/wp-content/uploads/2020/12/safaripark.jpg",
                "https://cdn.tourism-review.com/images/news/featured/dubai-safari-park-19382.jpg",
                "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0e/9e/4c/32/dubai-safari-park.jpg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1529395468/park-safari-dubai-safari-park.jpg"
              ]
            },
            {
              "name": "Desert Safari",
              "simple_price": 165,
              "premium_price": 215,
              "location": "Dubai Outskirts",
              "images": [
                "https://www.oasispalmdubai.com/blog/wp-content/uploads/2021/12/Dubai-Desert-Safari-Tour.jpg",
                "https://cdn.getyourguide.com/img/location/5f64466c2acb4.jpeg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1576220344/desert-safari-tours-12389.jpg",
                "https://images.fineartamerica.com/images/artworkimages/mediumlarge/2/desert-safari-dubai-douglas-champlin.jpg"
              ]
            },
            {
              "name": "Dinosaur Park Dubai",
              "simple_price": 50,
              "premium_price": 80,
              "location": "Al Warqa 3, Dubai",
              "images": [
                "https://cdn.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1622079783/dinosaur-park-panoramic-4-1_hdbunq.jpg",
                "https://cdn.getyourguide.com/img/location/59a24782dc2e8.jpeg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1602730400/dino-park-dubai.jpg",
                "https://dubaifaqs.com/wp-content/uploads/2021/06/dinosaur-park-uae.jpg"
              ]
            },
            {
              "name": "Dubai Fountain Show",
              "simple_price": 0,
              "premium_price": 100,
              "location": "Downtown Dubai",
              "images": [
                "https://cdn.getyourguide.com/img/location/59b725338d60a.jpeg",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
                "https://cdn.tourism-review.com/images/news/featured/dubai-fountain-show-19366.jpg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1511282571/dubai-fountain-show-12385.jpg"
              ]
            },
            {
              "name": "ARTE Museum",
              "simple_price": 45,
              "premium_price": 75,
              "location": "Alserkal Avenue, Dubai",
              "images": [
                "https://arte.museum/wp-content/uploads/2020/08/arte-exterior-2.jpg",
                "https://cdn.getyourguide.com/img/location/5ea5ae42356c7.jpeg",
                "https://cdn.tourism-review.com/images/news/featured/arte-museum-dubai-20314.jpg",
                "https://images.unsplash.com/photo-1542831371-d531d36971e6"
              ]
            },
            {
              "name": "AYA Universe",
              "simple_price": 150,
              "premium_price": 220,
              "location": "Dubai Marina",
              "images": [
                "https://ayauniverse.com/wp-content/uploads/2022/03/aya-universe-space-1.jpg",
                "https://cdn.getyourguide.com/img/location/5f8b2a94421f1.jpeg",
                "https://media.timeoutdubai.com/timeoutdubai/image/upload/f_auto,q_auto/v1586296913/aya-universe-12375.jpg",
                "https://images.unsplash.com/photo-1506748686214-e9df14d4d9d0"
              ]
            }
          ]
        const servicesData = {
            "services": [
              {
                "id": "hotel_booking",
                "name": "Hotel Booking",
                "description": "Reservation services for luxury hotels, resorts, and boutique accommodations in Dubai.",
                "average_cost": {
                  "budget_hotel": 300,
                  "mid_range_hotel": 700,
                  "luxury_hotel": 2000
                },
                "currency": "AED",
                "unit": "per night",
                "images": [
                  "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                  "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800",
                  "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800"
                ]
              },
              {
                "id": "honeymoon_package",
                "name": "Honeymoon Packages",
                "description": "Special packages for newlyweds, including luxury accommodations, dinners, spa treatments, and romantic activities.",
                "average_cost": {
                  "budget_package": 5000,
                  "premium_package": 12000,
                  "luxury_package": 25000
                },
                "currency": "AED",
                "unit": "per couple",
                "images": [
                  "https://images.unsplash.com/photo-1519225421980-715cb0215aed?w=800",
                  "https://images.unsplash.com/photo-1519741497674-611481863552?w=800",
                  "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"
                ]
              },
              {
                "id": "group_tour",
                "name": "Group Tours",
                "description": "Organized tours for groups, including transportation, guides, and visits to popular attractions.",
                "average_cost": {
                  "small_group_tour": 300,
                  "large_group_tour": 500
                },
                "currency": "AED",
                "unit": "per person",
                "images": [
                  "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=800",
                  "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800",
                  "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800"
                ]
              },
              {
                "id": "family_tour",
                "name": "Family Tour Packages",
                "description": "Tailored tour packages for families, including kid-friendly activities, accommodation, and meals.",
                "average_cost": {
                  "standard_package": 5000,
                  "premium_package": 10000,
                  "luxury_package": 15000
                },
                "currency": "AED",
                "unit": "per family",
                "images": [
                  "https://images.unsplash.com/photo-1566427567-09d23beee8b3?w=800",
                  "https://images.unsplash.com/photo-1519741497674-611481863552?w=800",
                  "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"
                ]
              },
              {
                "id": "exclusive_tour",
                "name": "Exclusive VIP Tours",
                "description": "Private, luxury tours with a personal guide and exclusive access to top attractions, including transport in high-end vehicles.",
                "average_cost": {
                  "half_day_tour": 2000,
                  "full_day_tour": 4000,
                  "luxury_full_day_tour": 8000
                },
                "currency": "AED",
                "unit": "per person",
                "images": [
                  "https://images.unsplash.com/photo-1595267757509-84845fbc05f7?w=800",
                  "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800",
                  "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"
                ]
              },
              {
                "id": "airport_pickup_drop",
                "name": "Airport Pickup & Drop",
                "description": "Transportation to and from Dubai International Airport with professional drivers and modern vehicles.",
                "average_cost": {
                  "sedan": 120,
                  "suv": 180,
                  "van": 250
                },
                "currency": "AED",
                "unit": "per trip",
                "images": [
                  "https://images.unsplash.com/photo-1606574201062-b69738113689?w=800",
                  "https://images.unsplash.com/photo-1602182250843-b19ec90b15b1?w=800",
                  "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800"
                ]
              },
              {
                "id": "flight_ticket_booking",
                "name": "Flight Ticket Booking",
                "description": "International and domestic flight bookings with flexible dates and competitive pricing.",
                "average_cost": {
                  "economy": 1200,
                  "business": 4500,
                  "first_class": 8500
                },
                "currency": "AED",
                "unit": "per person",
                "images": [
                  "https://images.unsplash.com/photo-1571407970349-bc81e63a1d74?w=800",
                  "https://images.unsplash.com/photo-1555564351-77dc0b8212ff?w=800",
                  "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800"
                ]
              },
              {
                "id": "lunch_breakfast",
                "name": "Lunch & Breakfast",
                "description": "Daily meal packages including continental breakfast and traditional or international lunch.",
                "average_cost": {
                  "breakfast": 40,
                  "lunch": 80
                },
                "currency": "AED",
                "unit": "per person",
                "images": [
                  "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800",
                  "https://images.unsplash.com/photo-1585238342028-966f50418347?w=800",
                  "https://images.unsplash.com/photo-1519741497674-611481863552?w=800"
                ]
              },
              {
                "id": "birthday_party",
                "name": "Birthday Party Planning",
                "description": "Customized birthday celebrations with decorations, cake, entertainment, and venue arrangements.",
                "average_cost": {
                  "basic_package": 1000,
                  "themed_package": 2500,
                  "luxury_package": 5000
                },
                "currency": "AED",
                "unit": "per event",
                "images": [
                  "https://images.unsplash.com/photo-1581579185169-79b4ae1cfa7e?w=800",
                  "https://images.unsplash.com/photo-1601654821455-166dbb622c0a?w=800",
                  "https://images.unsplash.com/photo-1519741497674-611481863552?w=800"
                ]
              }
            ]
          }
          
        console.log(`📊 Loaded ${attractionsData.length} attractions and ${servicesData.services.length} services from JSON files`);
        
        // Seed Attractions
        console.log('📍 Starting attractions seeding...');
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