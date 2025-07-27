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
    
    // Ready-Made Packages
    val packages = listOf(
        Package(
            id = "1",
            name = "3 Days Dubai Adventure",
            duration = "3 Days",
            price = "AED 1,200",
            highlights = listOf("Burj Khalifa", "Desert Safari", "Dubai Mall"),
            imageUrl = "dubai_package",
            description = "Perfect introduction to Dubai's highlights"
        ),
        Package(
            id = "2",
            name = "5 Days UAE Explorer",
            duration = "5 Days",
            price = "AED 2,100",
            highlights = listOf("Dubai", "Abu Dhabi", "Sharjah"),
            imageUrl = "uae_package",
            description = "Comprehensive UAE experience across multiple cities"
        ),
        Package(
            id = "3",
            name = "7 Days Luxury Escape",
            duration = "7 Days",
            price = "AED 3,500",
            highlights = listOf("5-Star Hotels", "Private Tours", "VIP Access"),
            imageUrl = "luxury_package",
            description = "Premium luxury experience with exclusive access"
        ),
        Package(
            id = "4",
            name = "Weekend Getaway",
            duration = "2 Days",
            price = "AED 800",
            highlights = listOf("Quick Tour", "Essential Attractions"),
            imageUrl = "weekend_package",
            description = "Perfect for a short but memorable visit"
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