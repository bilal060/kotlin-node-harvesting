package com.devicesync.app.data

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
    
    // Top Activities
    val activities = listOf(
        Activity(
            id = "1",
            name = "Desert Safari Adventure",
            imageUrl = "desert_safari",
            duration = "6 hours",
            price = "AED 250",
            description = "Experience the thrill of dune bashing and camel rides",
            rating = 4.9f
        ),
        Activity(
            id = "2",
            name = "Burj Khalifa Sky Views",
            imageUrl = "burj_khalifa",
            duration = "3 hours",
            price = "AED 350",
            description = "Visit the world's tallest building and enjoy panoramic views",
            rating = 4.8f
        ),
        Activity(
            id = "3",
            name = "Dubai Marina Cruise",
            imageUrl = "marina_cruise",
            duration = "2 hours",
            price = "AED 180",
            description = "Luxury yacht cruise with dinner and entertainment",
            rating = 4.7f
        ),
        Activity(
            id = "4",
            name = "Skydiving Experience",
            imageUrl = "skydiving",
            duration = "4 hours",
            price = "AED 1,200",
            description = "Adrenaline-pumping skydiving over Palm Jumeirah",
            rating = 4.9f
        ),
        Activity(
            id = "5",
            name = "Sheikh Zayed Mosque Tour",
            imageUrl = "grand_mosque",
            duration = "3 hours",
            price = "AED 150",
            description = "Guided tour of the magnificent Grand Mosque",
            rating = 4.8f
        ),
        Activity(
            id = "6",
            name = "Dubai Mall Shopping",
            imageUrl = "dubai_mall",
            duration = "5 hours",
            price = "Free Entry",
            description = "World's largest shopping mall with entertainment",
            rating = 4.6f
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