package com.devicesync.app.data

import com.devicesync.app.models.Activity

object DummyDataProvider {
    
    // Popular Destinations
    val destinations = listOf(
        Destination(
            id = "1",
            name = "Dubai",
            imageUrl = "dubai_skyline",
            description = "Experience the magic of Dubai with iconic landmarks",
            badge = "Most Booked",
            rating = 4.8f
        ),
        Destination(
            id = "2",
            name = "Abu Dhabi",
            imageUrl = "abu_dhabi_mosque",
            description = "Discover the capital's cultural wonders",
            badge = "Trending",
            rating = 4.7f
        ),
        Destination(
            id = "3",
            name = "Sharjah",
            imageUrl = "sharjah_heritage",
            description = "Cultural heritage and traditional museums",
            badge = "Cultural Hub",
            rating = 4.5f
        ),
        Destination(
            id = "4",
            name = "Fujairah",
            imageUrl = "fujairah_beach",
            description = "Beaches, mountains, and adventure",
            badge = "Nature Escape",
            rating = 4.6f
        ),
        Destination(
            id = "5",
            name = "Ras Al Khaimah",
            imageUrl = "rak_mountains",
            description = "Mountain adventures and luxury resorts",
            badge = "Adventure",
            rating = 4.4f
        )
    )
    
    // Top Activities - Updated for horizontal slider
    val activities = listOf(
        Activity(
            id = "1",
            title = "Burj Khalifa",
            description = "Visit the world's tallest building and enjoy panoramic views",
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center",
            rating = 4.8,
            reviewCount = 2500,
            price = 149,
            duration = "3 hours",
            category = "Attractions"
        ),
        Activity(
            id = "2",
            title = "Desert Safari",
            description = "Experience the thrill of dune bashing and camel rides",
            imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center",
            rating = 4.9,
            reviewCount = 1800,
            price = 250,
            duration = "6 hours",
            category = "Adventure"
        ),
        Activity(
            id = "3",
            title = "Marina Cruise",
            description = "Luxury yacht cruise with dinner and entertainment",
            imageUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=300&fit=crop&crop=center",
            rating = 4.7,
            reviewCount = 1200,
            price = 180,
            duration = "2 hours",
            category = "Cruise"
        ),
        Activity(
            id = "4",
            title = "Skydiving",
            description = "Adrenaline-pumping skydiving over Palm Jumeirah",
            imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop&crop=center",
            rating = 4.9,
            reviewCount = 800,
            price = 1200,
            duration = "4 hours",
            category = "Adventure"
        ),
        Activity(
            id = "5",
            title = "Sheikh Zayed Mosque",
            description = "Guided tour of the magnificent Grand Mosque",
            imageUrl = "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400&h=300&fit=crop&crop=center",
            rating = 4.8,
            reviewCount = 2100,
            price = 150,
            duration = "3 hours",
            category = "Cultural"
        ),
        Activity(
            id = "6",
            title = "Dubai Mall",
            description = "World's largest shopping mall with entertainment",
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center",
            rating = 4.6,
            reviewCount = 3200,
            price = 0,
            duration = "5 hours",
            category = "Shopping"
        )
    )
    
    // Ready-Made Packages (removed duplicate)
    
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