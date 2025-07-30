package com.devicesync.app.data

import com.devicesync.app.models.Activity

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
    
    // Travel Packages with proper, unique images
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
        ),
        Package(
            id = "4",
            name = "Cultural Heritage Tour",
            duration = "2 Days / 1 Night",
            price = "AED 1,499",
            highlights = listOf(
                "Sheikh Zayed Grand Mosque",
                "Dubai Museum & Al Fahidi Fort",
                "Traditional Souks & Markets",
                "Heritage Village Tour",
                "Traditional Arabic Dinner",
                "Cultural Guide"
            ),
            imageUrl = "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=300&fit=crop",
            description = "Immerse yourself in the rich cultural heritage of the UAE with visits to historic sites and traditional experiences."
        ),
        Package(
            id = "5",
            name = "Adventure & Thrills",
            duration = "3 Days / 2 Nights",
            price = "AED 2,499",
            highlights = listOf(
                "Skydiving Experience",
                "Hot Air Balloon Ride",
                "Dune Bashing Adventure",
                "Camel Trekking",
                "Adventure Hotel (4-star)",
                "Professional Guides"
            ),
            imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop",
            description = "Thrilling adventure package for adrenaline seekers with exciting outdoor activities and extreme sports."
        )
    )
} 