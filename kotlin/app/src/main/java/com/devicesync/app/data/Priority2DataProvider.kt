package com.devicesync.app.data

import java.util.*

object Priority2DataProvider {
    
    // User Reviews Data
    fun getSampleReviews(): List<Review> {
        return listOf(
            Review(
                id = "review_1",
                userId = "user_123",
                userName = "Sarah Johnson",
                userAvatar = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face",
                rating = 5.0f,
                title = "Amazing Dubai Experience!",
                comment = "The tour was absolutely incredible! Our guide Ahmed was knowledgeable and friendly. The Burj Khalifa visit was breathtaking, and the desert safari was the highlight of our trip. Highly recommend!",
                date = System.currentTimeMillis() - 86400000, // 1 day ago
                location = "Dubai, UAE",
                helpfulCount = 24,
                isVerified = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center",
                    "https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=400&h=300&fit=crop&crop=center"
                ),
                tags = listOf("Burj Khalifa", "Desert Safari", "Great Guide")
            ),
            Review(
                id = "review_2",
                userId = "user_456",
                userName = "Michael Chen",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face",
                rating = 4.5f,
                title = "Excellent Service and Organization",
                comment = "Very well organized tour. The pickup was on time, the guide was professional, and we saw all the major attractions. The only minor issue was the lunch venue was a bit crowded.",
                date = System.currentTimeMillis() - 172800000, // 2 days ago
                location = "Dubai, UAE",
                helpfulCount = 18,
                isVerified = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
                ),
                tags = listOf("Well Organized", "Professional Guide", "Major Attractions")
            ),
            Review(
                id = "review_3",
                userId = "user_789",
                userName = "Emma Rodriguez",
                userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&h=150&fit=crop&crop=face",
                rating = 5.0f,
                title = "Perfect Family Tour",
                comment = "Took our family of 4 on this tour and it was perfect! The kids loved the aquarium and the fountain show. The guide was patient with our children and made sure everyone had a great time.",
                date = System.currentTimeMillis() - 259200000, // 3 days ago
                location = "Dubai, UAE",
                helpfulCount = 31,
                isVerified = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=400&h=300&fit=crop&crop=center",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"
                ),
                tags = listOf("Family Friendly", "Kids Loved It", "Patient Guide")
            ),
            Review(
                id = "review_4",
                userId = "user_101",
                userName = "David Thompson",
                userAvatar = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
                rating = 4.0f,
                title = "Good Value for Money",
                comment = "The tour covered all the main attractions and was reasonably priced. The guide was informative, though a bit rushed at times. Overall, good value for what we paid.",
                date = System.currentTimeMillis() - 345600000, // 4 days ago
                location = "Dubai, UAE",
                helpfulCount = 12,
                isVerified = false,
                images = emptyList(),
                tags = listOf("Good Value", "Main Attractions", "Informative")
            ),
            Review(
                id = "review_5",
                userId = "user_202",
                userName = "Lisa Wang",
                userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&h=150&fit=crop&crop=face",
                rating = 4.8f,
                title = "Beautiful Architecture and Culture",
                comment = "Loved learning about Dubai's history and seeing the amazing architecture. The mosque visit was particularly moving. Our guide Fatima was very knowledgeable about Islamic culture.",
                date = System.currentTimeMillis() - 432000000, // 5 days ago
                location = "Dubai, UAE",
                helpfulCount = 27,
                isVerified = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=400&h=300&fit=crop&crop=center"
                ),
                tags = listOf("Architecture", "Culture", "Knowledgeable Guide")
            )
        )
    }
    
    // Trip Templates Data
    fun getSampleTripTemplates(): List<TripTemplate> {
        return listOf(
            TripTemplate(
                id = "template_1",
                name = "Dubai Essentials - 3 Days",
                description = "Perfect introduction to Dubai's most iconic attractions",
                duration = 3,
                difficulty = TripDifficulty.EASY,
                price = 299.99,
                currency = "USD",
                imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
                highlights = listOf(
                    "Burj Khalifa Observation Deck",
                    "Dubai Mall & Fountain Show",
                    "Desert Safari with BBQ Dinner",
                    "Palm Jumeirah & Atlantis",
                    "Dubai Marina Cruise"
                ),
                itinerary = listOf(
                    DayPlan(
                        day = 1,
                        title = "Downtown Dubai",
                        description = "Explore the heart of modern Dubai",
                        activities = listOf(
                            Activity("1", "Burj Khalifa Visit", "Attractions", "Visit the world's tallest building", "2 hours", 4.8f, listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"), 1500.0),
                            Activity("2", "Dubai Mall", "Shopping", "World's largest shopping mall", "3 hours", 4.5f, listOf("https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=400&h=300&fit=crop&crop=center"), 0.0)
                        ),
                        meals = listOf(
                            Meal(MealType.BREAKFAST, "Hotel breakfast", true),
                            Meal(MealType.LUNCH, "Local restaurant", true),
                            Meal(MealType.DINNER, "Fountain view dinner", true)
                        )
                    ),
                    DayPlan(
                        day = 2,
                        title = "Desert Adventure",
                        description = "Experience the Arabian desert",
                        activities = listOf(
                            Activity("3", "Desert Safari", "Adventure", "Dune bashing and camel ride", "4 hours", 4.9f, listOf("https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=400&h=300&fit=crop&crop=center"), 120.0)
                        ),
                        meals = listOf(
                            Meal(MealType.BREAKFAST, "Hotel breakfast", true),
                            Meal(MealType.DINNER, "Desert BBQ dinner", true)
                        )
                    ),
                    DayPlan(
                        day = 3,
                        title = "Marina & Palm",
                        description = "Modern Dubai waterfront",
                        activities = listOf(
                            Activity("4", "Palm Jumeirah", "Attractions", "Visit the iconic palm island", "2 hours", 4.7f, listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"), 80.0),
                            Activity("5", "Marina Cruise", "Cruise", "Evening cruise on Dubai Marina", "2 hours", 4.6f, listOf("https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=400&h=300&fit=crop&crop=center"), 95.0)
                        ),
                        meals = listOf(
                            Meal(MealType.BREAKFAST, "Hotel breakfast", true),
                            Meal(MealType.LUNCH, "Marina restaurant", true)
                        )
                    )
                ),
                included = listOf(
                    "Hotel accommodation (3 nights)",
                    "All transportation",
                    "Professional guide",
                    "All entrance fees",
                    "Meals as specified",
                    "Desert safari with BBQ"
                ),
                excluded = listOf(
                    "International flights",
                    "Personal expenses",
                    "Optional activities",
                    "Tips for guides"
                ),
                requirements = listOf(
                    "Valid passport",
                    "Comfortable walking shoes",
                    "Modest clothing for mosque visits",
                    "Camera for photos"
                ),
                rating = 4.8f,
                reviewCount = 156,
                isPopular = true,
                isCustomizable = true
            ),
            TripTemplate(
                id = "template_2",
                name = "Luxury Dubai - 5 Days",
                description = "Premium experience with luxury accommodations and exclusive access",
                duration = 5,
                difficulty = TripDifficulty.MODERATE,
                price = 899.99,
                currency = "USD",
                imageUrl = "https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=800&h=600&fit=crop&crop=center",
                highlights = listOf(
                    "5-star hotel accommodation",
                    "Private yacht charter",
                    "Helicopter tour of Dubai",
                    "Exclusive dining experiences",
                    "VIP access to attractions",
                    "Personal concierge service"
                ),
                itinerary = listOf(
                    DayPlan(
                        day = 1,
                        title = "Luxury Arrival",
                        description = "Arrive in style with VIP airport transfer",
                        activities = listOf(
                            Activity("6", "VIP Airport Transfer", "Transport", "Luxury car transfer from airport", "1 hour", 5.0f, listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"), 200.0)
                        ),
                        meals = listOf(
                            Meal(MealType.DINNER, "Fine dining at Burj Al Arab", true)
                        )
                    )
                ),
                included = listOf(
                    "5-star hotel accommodation",
                    "VIP airport transfers",
                    "Private guide throughout",
                    "All luxury experiences",
                    "Fine dining experiences",
                    "Personal concierge"
                ),
                excluded = listOf(
                    "International flights",
                    "Personal shopping",
                    "Additional spa treatments"
                ),
                requirements = listOf(
                    "Valid passport",
                    "Smart casual attire for dining",
                    "Advance booking required"
                ),
                rating = 4.9f,
                reviewCount = 89,
                isPopular = false,
                isCustomizable = true
            )
        )
    }
    
    // Push Notifications Data
    fun getSampleNotifications(): List<PushNotification> {
        return listOf(
            PushNotification(
                id = "notif_1",
                title = "Tour Reminder",
                message = "Your Dubai Desert Safari tour starts in 2 hours. Please be ready at the hotel lobby.",
                type = NotificationType.REMINDER,
                data = mapOf("tour_id" to "desert_safari_123", "pickup_time" to "15:30"),
                timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
                priority = NotificationPriority.HIGH
            ),
            PushNotification(
                id = "notif_2",
                title = "Payment Successful",
                message = "Your payment of $299.99 for Dubai Essentials Tour has been processed successfully.",
                type = NotificationType.PAYMENT_SUCCESS,
                data = mapOf("amount" to "299.99", "booking_id" to "BOOK_12345"),
                timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
                priority = NotificationPriority.HIGH
            ),
            PushNotification(
                id = "notif_3",
                title = "Special Offer",
                message = "Get 20% off on Burj Khalifa tickets! Limited time offer valid until tomorrow.",
                type = NotificationType.SPECIAL_OFFER,
                data = mapOf("discount" to "20%", "valid_until" to "tomorrow"),
                timestamp = System.currentTimeMillis() - 10800000, // 3 hours ago
                priority = NotificationPriority.NORMAL
            ),
            PushNotification(
                id = "notif_4",
                title = "Weather Alert",
                message = "High temperature expected today (38°C). Stay hydrated and avoid outdoor activities during peak hours.",
                type = NotificationType.WEATHER_ALERT,
                data = mapOf("temperature" to "38°C", "condition" to "hot"),
                timestamp = System.currentTimeMillis() - 14400000, // 4 hours ago
                priority = NotificationPriority.NORMAL
            ),
            PushNotification(
                id = "notif_5",
                title = "New Message from Guide",
                message = "Sarah: Hi! I'll meet you at the Burj Khalifa entrance at 9 AM tomorrow. Don't forget your camera!",
                type = NotificationType.TOUR_UPDATE,
                data = mapOf("guide_name" to "Sarah", "meeting_time" to "9 AM"),
                timestamp = System.currentTimeMillis() - 18000000, // 5 hours ago
                priority = NotificationPriority.NORMAL
            )
        )
    }
    
    // Payment Methods Data
    fun getSamplePayments(): List<Payment> {
        return listOf(
            Payment(
                id = "payment_1",
                amount = 299.99,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                method = PaymentMethod.CREDIT_CARD,
                bookingId = "BOOK_12345",
                timestamp = System.currentTimeMillis() - 7200000,
                description = "Dubai Essentials Tour - 3 Days",
                transactionId = "TXN_123456789"
            ),
            Payment(
                id = "payment_2",
                amount = 95.00,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                method = PaymentMethod.PAYPAL,
                bookingId = "BOOK_12346",
                timestamp = System.currentTimeMillis() - 86400000,
                description = "Burj Khalifa Observation Deck",
                transactionId = "TXN_987654321"
            ),
            Payment(
                id = "payment_3",
                amount = 120.00,
                currency = "USD",
                status = PaymentStatus.PENDING,
                method = PaymentMethod.APPLE_PAY,
                bookingId = "BOOK_12347",
                timestamp = System.currentTimeMillis() - 3600000,
                description = "Desert Safari with BBQ",
                transactionId = null
            )
        )
    }
    
    // Live Chat Data
    fun getSampleChatRooms(): List<ChatRoom> {
        return listOf(
            ChatRoom(
                id = "support_room",
                title = "Customer Support",
                participants = listOf(
                    ChatParticipant("user_1", "You", SenderType.USER),
                    ChatParticipant("support_1", "Ahmed - Support", SenderType.SUPPORT, isOnline = true)
                ),
                lastMessage = ChatMessage(
                    id = "msg_1",
                    senderId = "support_1",
                    senderName = "Ahmed - Support",
                    senderType = SenderType.SUPPORT,
                    message = "How can I help you with your booking today?",
                    timestamp = System.currentTimeMillis() - 300000, // 5 minutes ago
                    isRead = true
                ),
                unreadCount = 0,
                isActive = true,
                createdAt = System.currentTimeMillis() - 86400000 // 1 day ago
            ),
            ChatRoom(
                id = "guide_room",
                title = "Tour Guide - Sarah",
                participants = listOf(
                    ChatParticipant("user_1", "You", SenderType.USER),
                    ChatParticipant("guide_1", "Sarah - Tour Guide", SenderType.GUIDE, isOnline = true)
                ),
                lastMessage = ChatMessage(
                    id = "msg_2",
                    senderId = "guide_1",
                    senderName = "Sarah - Tour Guide",
                    senderType = SenderType.GUIDE,
                    message = "I'll meet you at the Burj Khalifa entrance at 9 AM tomorrow!",
                    timestamp = System.currentTimeMillis() - 18000000, // 5 hours ago
                    isRead = false
                ),
                unreadCount = 1,
                isActive = true,
                createdAt = System.currentTimeMillis() - 172800000 // 2 days ago
            )
        )
    }
    
    // Audio Tours Data
    fun getSampleAudioTours(): List<AudioTour> {
        return listOf(
            AudioTour(
                id = "audio_1",
                title = "Burj Khalifa: Towering Above Dubai",
                description = "Discover the world's tallest building with this comprehensive audio guide",
                location = "Burai Khalifa, Downtown Dubai",
                duration = 45,
                language = "English",
                audioUrl = "https://example.com/audio/burj_khalifa_tour.mp3",
                imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
                stops = listOf(
                    AudioStop(
                        id = "stop_1",
                        title = "Welcome to Burj Khalifa",
                        description = "Introduction to the world's tallest building",
                        coordinates = Coordinates(25.197197, 55.274376),
                        audioUrl = "https://example.com/audio/burj_welcome.mp3",
                        duration = 180,
                        facts = listOf(
                            "Burj Khalifa stands 828 meters tall",
                            "It has 163 floors",
                            "Construction took 6 years to complete"
                        ),
                        tips = listOf(
                            "Visit during sunset for the best views",
                            "Book tickets in advance to avoid queues",
                            "Don't forget your camera"
                        )
                    )
                ),
                rating = 4.8f,
                downloadCount = 15420,
                isFree = false,
                price = 9.99
            ),
            AudioTour(
                id = "audio_2",
                title = "Sheikh Zayed Grand Mosque",
                description = "Explore the stunning beauty and spiritual significance of this architectural wonder",
                location = "Sheikh Zayed Grand Mosque, Abu Dhabi",
                duration = 60,
                language = "English",
                audioUrl = "https://example.com/audio/mosque_tour.mp3",
                imageUrl = "https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=800&h=600&fit=crop&crop=center",
                stops = listOf(
                    AudioStop(
                        id = "mosque_stop_1",
                        title = "The Grand Entrance",
                        description = "Welcome to one of the world's largest mosques",
                        coordinates = Coordinates(24.4129, 54.4747),
                        audioUrl = "https://example.com/audio/mosque_entrance.mp3",
                        duration = 200,
                        facts = listOf(
                            "The mosque can accommodate 40,000 worshippers",
                            "It features 82 domes of different sizes",
                            "The main prayer hall houses the world's largest chandelier"
                        ),
                        tips = listOf(
                            "Dress modestly when visiting",
                            "Remove shoes before entering",
                            "Photography is allowed but be respectful"
                        )
                    )
                ),
                rating = 4.9f,
                downloadCount = 12350,
                isFree = true
            )
        )
    }
} 