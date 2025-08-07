package com.devicesync.app.data

import com.devicesync.app.data.models.TourPackage

object UpdatedDummyDataProvider {
    
    // Popular Destinations with proper, unique images
    val destinations = listOf(
        Destination(
            id = "1",
            name = "Burj Khalifa",
            location = "Downtown Dubai",
            description = "The Burj Khalifa is the tallest building in the world, standing at 828 meters. This architectural marvel offers breathtaking views of Dubai from its observation decks on the 124th and 148th floors. Experience the city from new heights with our guided tours.",
            rating = 4.8f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop&crop=center"
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
                "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&h=600&fit=crop&crop=center",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop&crop=top",
                "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&h=600&fit=crop&crop=bottom",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop&crop=left",
                "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&h=600&fit=crop&crop=right",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop&crop=entropy"
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
    
    // Top Activities with proper, unique images
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
            basePrice = 120.0,
            timeSlots = listOf(
                TimeSlot("06:00 PM", "08:00 PM", 120.0, "Sunset Cruise"),
                TimeSlot("07:00 PM", "09:00 PM", 130.0, "Evening Cruise"),
                TimeSlot("08:00 PM", "10:00 PM", 140.0, "Night Cruise")
            ),
            features = listOf("Luxury Yacht", "Gourmet Dinner", "Live Entertainment", "Fountain Views", "Hotel Pickup")
        ),
        Activity(
            id = "3",
            name = "Helicopter Tour",
            category = "Adventure",
            description = "Soar above Dubai's iconic landmarks in a helicopter tour. Get bird's eye views of Burj Khalifa, Palm Jumeirah, Dubai Marina, and the stunning coastline. A truly unforgettable experience.",
            duration = "1 hour",
            rating = 4.8f,
            images = listOf(
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop"
            ),
            basePrice = 450.0,
            timeSlots = listOf(
                TimeSlot("09:00 AM", "10:00 AM", 450.0, "Morning Flight"),
                TimeSlot("02:00 PM", "03:00 PM", 450.0, "Afternoon Flight"),
                TimeSlot("05:00 PM", "06:00 PM", 500.0, "Sunset Flight")
            ),
            features = listOf("Bird's Eye Views", "Professional Pilot", "Safety Briefing", "Photo Service", "Hotel Pickup")
        ),
        Activity(
            id = "4",
            name = "Hot Air Balloon",
            category = "Adventure",
            description = "Float above the Dubai desert in a hot air balloon at sunrise. Experience the tranquility of the desert from above and enjoy a traditional breakfast in the desert after landing.",
            duration = "4 hours",
            rating = 4.9f,
            images = listOf(
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"
            ),
            basePrice = 350.0,
            timeSlots = listOf(
                TimeSlot("05:00 AM", "09:00 AM", 350.0, "Sunrise Flight"),
                TimeSlot("06:00 AM", "10:00 AM", 350.0, "Early Morning Flight")
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
        TourPackage(
            id = "1",
            name = "Dubai Essential Package",
            description = "Perfect introduction to Dubai's iconic landmarks and experiences. Includes all must-see attractions with comfortable accommodation.",
            duration = "3 Days / 2 Nights",
            price = "AED 1,999",
            imageUrls = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"),
            highlights = listOf("Burj Khalifa", "Desert Safari", "Dubai Mall", "Dubai Frame", "Hotel", "Airport Transfers"),
            itinerary = listOf("Day 1: Arrival & Burj Khalifa", "Day 2: Desert Safari & Dubai Mall", "Day 3: Dubai Frame & Departure"),
            includes = listOf("Hotel Accommodation", "Airport Transfers", "All Attraction Tickets", "Professional Guide", "Daily Breakfast"),
            category = "essential"
        ),
        TourPackage(
            id = "2",
            name = "Luxury Dubai Experience",
            description = "Ultimate luxury experience with exclusive access to Dubai's finest attractions and premium services.",
            duration = "5 Days / 4 Nights",
            price = "AED 4,999",
            imageUrls = listOf("https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=300&fit=crop&crop=center"),
            highlights = listOf("Burj Al Arab", "Helicopter Tour", "Private Safari", "Palm Jumeirah", "Luxury Hotel", "Private Guide", "Spa"),
            itinerary = listOf("Day 1: Arrival & Burj Al Arab", "Day 2: Helicopter Tour & Palm Jumeirah", "Day 3: Private Safari", "Day 4: Spa & Shopping", "Day 5: Departure"),
            includes = listOf("Luxury Hotel Accommodation", "Private Airport Transfers", "All Premium Attraction Tickets", "Private Guide", "All Meals", "Spa Treatment"),
            category = "luxury"
        ),
        TourPackage(
            id = "3",
            name = "Family Adventure Package",
            description = "Perfect family vacation with kid-friendly attractions and activities that everyone will enjoy.",
            duration = "4 Days / 3 Nights",
            price = "AED 2,999",
            imageUrls = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center"),
            highlights = listOf("Dubai Parks", "Aquaventure", "Dubai Aquarium", "KidZania", "Family Hotel", "Kids Club", "All Meals"),
            itinerary = listOf("Day 1: Arrival & Dubai Parks", "Day 2: Aquaventure & Dubai Aquarium", "Day 3: KidZania & Shopping", "Day 4: Departure"),
            includes = listOf("Family Hotel Accommodation", "Airport Transfers", "All Attraction Tickets", "Kids Club Access", "All Meals", "Family Guide"),
            category = "family"
        ),
        TourPackage(
            id = "4",
            name = "Full Day Abu Dhabi City Tour from Dubai with Louvre Museum",
            description = "Experience the best of Abu Dhabi in a comprehensive day tour including the iconic Louvre Museum and Sheikh Zayed Grand Mosque.",
            duration = "1 Day",
            price = "AED 899",
            imageUrls = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"),
            highlights = listOf("Louvre Museum", "Sheikh Zayed Grand Mosque", "Emirates Palace", "Corniche", "Heritage Village"),
            itinerary = listOf("Morning: Pickup from Dubai", "Mid-morning: Sheikh Zayed Grand Mosque", "Afternoon: Louvre Museum", "Evening: Emirates Palace & Corniche", "Night: Return to Dubai"),
            includes = listOf("Hotel Pickup & Drop", "Professional Guide", "Louvre Museum Ticket", "Lunch", "Transportation", "Water"),
            category = "day-tour"
        ),
        TourPackage(
            id = "5",
            name = "Desert Safari & Dinner Package",
            description = "Experience the thrill of desert adventure with dune bashing, camel rides, and traditional Arabic dinner under the stars.",
            duration = "1 Day",
            price = "AED 299",
            imageUrls = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center"),
            highlights = listOf("Dune Bashing", "Camel Riding", "Sandboarding", "Traditional Dinner", "Belly Dance", "Henna Painting"),
            itinerary = listOf("Afternoon: Pickup from Hotel", "Sunset: Dune Bashing & Activities", "Evening: Traditional Dinner & Entertainment", "Night: Return to Hotel"),
            includes = listOf("Hotel Pickup & Drop", "Dune Bashing", "Camel Ride", "Sandboarding", "Traditional Dinner", "Belly Dance Show", "Henna Painting"),
            category = "adventure"
        )
    )
} 