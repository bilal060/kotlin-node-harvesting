package com.devicesync.app.data

import com.devicesync.app.models.Activity

object DummyDataProvider {
    
    // Popular Destinations with detailed information
    val destinations = listOf(
        Destination(
            id = "1",
            name = "Burj Khalifa",
            location = "Downtown Dubai",
            description = "The Burj Khalifa is the tallest building in the world, standing at 828 meters. This architectural marvel offers breathtaking views of Dubai from its observation decks on the 124th and 148th floors. Experience the city from new heights with our guided tours.",
            rating = 4.8f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop"
            ),
            basePrice = 149.0,
            timeSlots = listOf(
                TimeSlot("09:00 AM", "11:00 AM", 149.0, "Morning Tour"),
                TimeSlot("02:00 PM", "04:00 PM", 169.0, "Afternoon Tour"),
                TimeSlot("06:00 PM", "08:00 PM", 199.0, "Sunset Tour"),
                TimeSlot("08:00 PM", "10:00 PM", 179.0, "Evening Tour")
            ),
            amenities = listOf("Guided Tour", "Skip-the-Line", "Audio Guide", "Photo Service", "Refreshments"),
            badge = "Most Booked"
        ),
        Destination(
            id = "2",
            name = "Sheikh Zayed Mosque",
            location = "Abu Dhabi",
            description = "The Sheikh Zayed Grand Mosque is one of the world's largest mosques and a stunning example of Islamic architecture. With its pristine white marble, intricate floral designs, and the world's largest hand-knotted carpet, it's a must-visit cultural landmark.",
            rating = 4.9f,
            images = listOf(
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop"
            ),
            basePrice = 0.0,
            timeSlots = listOf(
                TimeSlot("09:00 AM", "11:00 AM", 0.0, "Morning Visit"),
                TimeSlot("02:00 PM", "04:00 PM", 0.0, "Afternoon Visit"),
                TimeSlot("06:00 PM", "08:00 PM", 0.0, "Evening Visit")
            ),
            amenities = listOf("Free Entry", "Guided Tour", "Cultural Experience", "Photography Allowed", "Dress Code Provided"),
            badge = "Cultural Hub"
        ),
        Destination(
            id = "3",
            name = "Palm Jumeirah",
            location = "Dubai",
            description = "The Palm Jumeirah is an artificial archipelago shaped like a palm tree. Home to luxury hotels, pristine beaches, and the iconic Atlantis resort, it's a symbol of Dubai's architectural ambition and luxury lifestyle.",
            rating = 4.7f,
            images = listOf(
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop"
            ),
            basePrice = 89.0,
            timeSlots = listOf(
                TimeSlot("10:00 AM", "12:00 PM", 89.0, "Morning Tour"),
                TimeSlot("03:00 PM", "05:00 PM", 99.0, "Afternoon Tour"),
                TimeSlot("07:00 PM", "09:00 PM", 119.0, "Evening Tour")
            ),
            amenities = listOf("Beach Access", "Hotel Views", "Photo Opportunities", "Luxury Experience", "Transport Included"),
            badge = "Luxury"
        ),
        Destination(
            id = "4",
            name = "Dubai Frame",
            location = "Zabeel Park",
            description = "The Dubai Frame is a 150-meter-high picture frame that offers stunning views of both old and new Dubai. Walk across the glass bridge and experience the contrast between the historic Deira district and modern Dubai skyline.",
            rating = 4.5f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop"
            ),
            basePrice = 50.0,
            timeSlots = listOf(
                TimeSlot("09:00 AM", "11:00 AM", 50.0, "Morning Visit"),
                TimeSlot("02:00 PM", "04:00 PM", 50.0, "Afternoon Visit"),
                TimeSlot("06:00 PM", "08:00 PM", 60.0, "Sunset Visit")
            ),
            amenities = listOf("Glass Bridge", "Panoramic Views", "Historical Exhibition", "Photo Service", "Audio Guide"),
            badge = "New Attraction"
        ),
        Destination(
            id = "5",
            name = "Dubai Mall",
            location = "Downtown Dubai",
            description = "The Dubai Mall is the world's largest shopping mall by total area, featuring over 1,200 retail stores, an indoor theme park, an ice rink, and the famous Dubai Aquarium. It's a shopping and entertainment paradise.",
            rating = 4.6f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop"
            ),
            basePrice = 0.0,
            timeSlots = listOf(
                TimeSlot("10:00 AM", "12:00 PM", 0.0, "Morning Shopping"),
                TimeSlot("02:00 PM", "04:00 PM", 0.0, "Afternoon Shopping"),
                TimeSlot("06:00 PM", "08:00 PM", 0.0, "Evening Shopping")
            ),
            amenities = listOf("Free Entry", "Shopping", "Entertainment", "Food Court", "Aquarium Access"),
            badge = "Shopping"
        )
    )
    
    // Top Activities with detailed information
    val activities = listOf(
        Activity(
            id = "1",
            name = "Desert Safari Adventure",
            category = "Adventure",
            description = "Experience the thrill of dune bashing in the Dubai desert, followed by a traditional Bedouin camp experience. Enjoy camel rides, henna painting, traditional dance performances, and a delicious BBQ dinner under the stars.",
            duration = "6 hours",
            rating = 4.9f,
            images = listOf(
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"
            ),
            basePrice = 89.0,
            timeSlots = listOf(
                TimeSlot("02:00 PM", "08:00 PM", 89.0, "Afternoon Safari"),
                TimeSlot("03:00 PM", "09:00 PM", 99.0, "Evening Safari"),
                TimeSlot("04:00 PM", "10:00 PM", 109.0, "Sunset Safari"),
                TimeSlot("05:00 PM", "11:00 PM", 119.0, "Night Safari")
            ),
            features = listOf("Dune Bashing", "Camel Ride", "BBQ Dinner", "Live Entertainment", "Hotel Pickup")
        ),
        Activity(
            id = "2",
            name = "Marina Cruise",
            category = "Cruise",
            description = "Luxury yacht cruise along Dubai Marina with stunning views of the city skyline. Enjoy a gourmet dinner, live entertainment, and the magical Dubai Fountain show from the water.",
            duration = "2 hours",
            rating = 4.7f,
            images = listOf(
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=600&fit=crop"
            ),
            basePrice = 180.0,
            timeSlots = listOf(
                TimeSlot("06:00 PM", "08:00 PM", 180.0, "Sunset Cruise"),
                TimeSlot("07:00 PM", "09:00 PM", 200.0, "Dinner Cruise"),
                TimeSlot("08:00 PM", "10:00 PM", 220.0, "Evening Cruise")
            ),
            features = listOf("Luxury Yacht", "Gourmet Dinner", "Live Entertainment", "Fountain Views", "Open Bar")
        ),
        Activity(
            id = "3",
            name = "Skydiving Experience",
            category = "Adventure",
            description = "Adrenaline-pumping skydiving experience over Palm Jumeirah. Jump from 13,000 feet and enjoy breathtaking views of Dubai's iconic landmarks while experiencing the ultimate thrill.",
            duration = "4 hours",
            rating = 4.9f,
            images = listOf(
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"
            ),
            basePrice = 1200.0,
            timeSlots = listOf(
                TimeSlot("08:00 AM", "12:00 PM", 1200.0, "Morning Jump"),
                TimeSlot("02:00 PM", "06:00 PM", 1300.0, "Afternoon Jump"),
                TimeSlot("04:00 PM", "08:00 PM", 1400.0, "Sunset Jump")
            ),
            features = listOf("Professional Instructor", "Safety Equipment", "Video Recording", "Certificate", "Transport")
        ),
        Activity(
            id = "4",
            name = "Hot Air Balloon",
            category = "Adventure",
            description = "Soar above the Dubai desert in a hot air balloon and witness the magical sunrise over the golden dunes. Includes breakfast in the desert and a traditional falconry show.",
            duration = "4 hours",
            rating = 4.8f,
            images = listOf(
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"
            ),
            basePrice = 450.0,
            timeSlots = listOf(
                TimeSlot("05:00 AM", "09:00 AM", 450.0, "Sunrise Flight"),
                TimeSlot("06:00 AM", "10:00 AM", 500.0, "Morning Flight")
            ),
            features = listOf("Sunrise Views", "Desert Breakfast", "Falconry Show", "Certificate", "Hotel Pickup")
        ),
        Activity(
            id = "5",
            name = "Dubai Aquarium",
            category = "Attractions",
            description = "Explore the underwater world at Dubai Aquarium, home to thousands of aquatic animals including sharks, rays, and colorful fish. Walk through the tunnel and get up close with marine life.",
            duration = "2 hours",
            rating = 4.6f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop"
            ),
            basePrice = 120.0,
            timeSlots = listOf(
                TimeSlot("10:00 AM", "12:00 PM", 120.0, "Morning Visit"),
                TimeSlot("02:00 PM", "04:00 PM", 120.0, "Afternoon Visit"),
                TimeSlot("06:00 PM", "08:00 PM", 140.0, "Evening Visit")
            ),
            features = listOf("Underwater Tunnel", "Shark Encounter", "Glass Bottom Boat", "Educational Tour", "Photo Service")
        )
    )

    // Travel Packages
    val packages = listOf(
        Package(
            id = "1",
            name = "Dubai Essential Package",
            duration = "3 Days / 2 Nights",
            price = "AED 1,999",
            highlights = listOf(
                "Burj Khalifa Observation Deck",
                "Desert Safari with BBQ Dinner",
                "Dubai Mall & Fountain Show",
                "Dubai Frame & Museum of Future",
                "Hotel Accommodation (4-star)",
                "Airport Transfers"
            ),
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center",
            description = "Perfect introduction to Dubai's iconic landmarks and experiences. Includes all must-see attractions with comfortable accommodation."
        ),
        Package(
            id = "2",
            name = "Luxury Dubai Experience",
            duration = "5 Days / 4 Nights",
            price = "AED 4,999",
            highlights = listOf(
                "Burj Al Arab Afternoon Tea",
                "Helicopter City Tour",
                "Private Desert Safari",
                "Palm Jumeirah & Atlantis",
                "Luxury Hotel (5-star)",
                "Private Guide & Driver",
                "Spa Treatment"
            ),
            imageUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=300&fit=crop&crop=center",
            description = "Ultimate luxury experience with exclusive access to Dubai's finest attractions and premium services."
        ),
        Package(
            id = "3",
            name = "Family Adventure Package",
            duration = "4 Days / 3 Nights",
            price = "AED 2,999",
            highlights = listOf(
                "Dubai Parks & Resorts",
                "Aquaventure Waterpark",
                "Dubai Aquarium & Underwater Zoo",
                "KidZania Dubai",
                "Family Hotel (4-star)",
                "Kids Club Access",
                "All Meals Included"
            ),
            imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center",
            description = "Perfect family vacation with kid-friendly attractions and activities that everyone will enjoy."
        )
    )

    // Traveler Reviews
    val reviews = listOf(
        Review(
            id = "1",
            userName = "Sarah Johnson",
            userImageUrl = "user_sarah",
            rating = 5.0f,
            comment = "Amazing experience! The desert safari was absolutely incredible. Our guide was knowledgeable and the sunset views were breathtaking.",
            destination = "Dubai",
            date = "2 days ago"
        ),
        Review(
            id = "2",
            userName = "Michael Chen",
            userImageUrl = "user_michael",
            rating = 4.8f,
            comment = "The Burj Khalifa visit exceeded all expectations. The views from the top are simply spectacular. Highly recommend!",
            destination = "Dubai",
            date = "1 week ago"
        ),
        Review(
            id = "3",
            userName = "Emma Rodriguez",
            userImageUrl = "user_emma",
            rating = 4.9f,
            comment = "Sheikh Zayed Mosque is absolutely stunning. The architecture and peaceful atmosphere made it a highlight of our trip.",
            destination = "Abu Dhabi",
            date = "3 days ago"
        ),
        Review(
            id = "4",
            userName = "David Thompson",
            userImageUrl = "user_david",
            rating = 4.7f,
            comment = "The marina cruise was perfect for our anniversary. Romantic dinner with amazing city views. Will definitely return!",
            destination = "Dubai",
            date = "5 days ago"
        ),
        Review(
            id = "5",
            userName = "Lisa Wang",
            userImageUrl = "user_lisa",
            rating = 4.8f,
            comment = "Skydiving over Palm Jumeirah was the most thrilling experience of my life. Professional team and unforgettable memories.",
            destination = "Dubai",
            date = "1 week ago"
        ),
        Review(
            id = "6",
            userName = "Ahmed Al Mansouri",
            userImageUrl = "user_ahmed",
            rating = 5.0f,
            comment = "The Dubai Essential Package was perfect for our first visit. Everything was well-organized and the hotel was excellent. Highly recommend!",
            destination = "Dubai",
            date = "3 days ago"
        ),
        Review(
            id = "7",
            userName = "Jennifer Smith",
            userImageUrl = "user_jennifer",
            rating = 4.9f,
            comment = "Luxury package was worth every penny! The helicopter tour was absolutely breathtaking and the private guide was incredibly knowledgeable.",
            destination = "Dubai",
            date = "1 week ago"
        ),
        Review(
            id = "8",
            userName = "Carlos Rodriguez",
            userImageUrl = "user_carlos",
            rating = 4.8f,
            comment = "Family package was perfect for our kids aged 8 and 12. They loved the waterpark and theme parks. Great value for money!",
            destination = "Dubai",
            date = "4 days ago"
        ),
        Review(
            id = "9",
            userName = "Priya Patel",
            userImageUrl = "user_priya",
            rating = 4.7f,
            comment = "The desert safari was the highlight of our trip. The dune bashing was thrilling and the traditional dinner was delicious.",
            destination = "Dubai",
            date = "6 days ago"
        ),
        Review(
            id = "10",
            userName = "Robert Wilson",
            userImageUrl = "user_robert",
            rating = 5.0f,
            comment = "Dubai exceeded all our expectations. The city is beautiful, the people are friendly, and the experiences are unforgettable.",
            destination = "Dubai",
            date = "2 weeks ago"
        )
    )
    
    // Travel Tips
    val travelTips = listOf(
        TravelTip(
            id = "1",
            title = "UAE Dress Code Explained",
            imageUrl = "dress_code",
            excerpt = "Learn about appropriate clothing for different occasions and locations in the UAE.",
            category = "Culture"
        ),
        TravelTip(
            id = "2",
            title = "Top 5 Places to Visit in Summer",
            imageUrl = "summer_places",
            excerpt = "Discover the best indoor and outdoor attractions to beat the summer heat.",
            category = "Seasonal"
        ),
        TravelTip(
            id = "3",
            title = "Best Food in Dubai Under AED 30",
            imageUrl = "budget_food",
            excerpt = "Delicious local and international cuisine that won't break the bank.",
            category = "Food"
        ),
        TravelTip(
            id = "4",
            title = "Public Transport Guide",
            imageUrl = "transport",
            excerpt = "Navigate Dubai's metro, buses, and taxis like a local.",
            category = "Transport"
        ),
        TravelTip(
            id = "5",
            title = "Photography Hotspots",
            imageUrl = "photography",
            excerpt = "Capture the most Instagram-worthy moments in the UAE.",
            category = "Photography"
        ),
        TravelTip(
            id = "6",
            title = "Shopping Malls vs Souks",
            imageUrl = "shopping",
            excerpt = "When to visit modern malls and when to explore traditional markets.",
            category = "Shopping"
        )
    )
} 