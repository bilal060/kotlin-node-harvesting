package com.devicesync.app.data

import com.devicesync.app.models.Activity

object DummyDataProvider {
    
    // Popular Destinations with detailed information and proper images
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
    
    // Top Activities with detailed information and proper images
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

    // UAE Travel Tips
    val travelTips = listOf(
        TravelTip(
            title = "Best Time to Visit",
            description = "Visit between November and March for the best weather. Summers can be extremely hot with temperatures reaching 45°C. The winter months offer pleasant temperatures ranging from 20-30°C, perfect for outdoor activities and sightseeing.",
            icon = "🌤️"
        ),
        TravelTip(
            title = "Dress Code & Culture",
            description = "Respect local customs by dressing modestly. While Dubai is modern, it's still a Muslim country. Cover shoulders and knees in public areas. Swimwear is acceptable at beaches and pools. During Ramadan, avoid eating, drinking, or smoking in public during daylight hours.",
            icon = "👗"
        ),
        TravelTip(
            title = "Transportation Tips",
            description = "Dubai Metro is clean, efficient, and affordable. Get a Nol card for easy access. Taxis are plentiful and metered. Uber and Careem are popular ride-sharing apps. For longer distances, consider renting a car, but be aware of aggressive driving styles.",
            icon = "🚇"
        ),
        TravelTip(
            title = "Money & Payments",
            description = "UAE Dirham (AED) is the local currency. Credit cards are widely accepted. ATMs are available everywhere. Tipping is not mandatory but appreciated (10-15% in restaurants). Keep some cash for small purchases and tips.",
            icon = "💰"
        ),
        TravelTip(
            title = "Safety & Security",
            description = "Dubai is one of the safest cities in the world. Crime rates are very low. However, always be aware of your surroundings. Keep valuables secure. Emergency number is 999. Police are helpful and speak English.",
            icon = "🛡️"
        ),
        TravelTip(
            title = "Food & Dining",
            description = "Dubai offers incredible culinary diversity. Try local Emirati cuisine, Arabic mezze, and international dishes. Alcohol is served in licensed venues (hotels, bars). Pork is not widely available. Many restaurants offer halal options.",
            icon = "🍽️"
        ),
        TravelTip(
            title = "Shopping & Souks",
            description = "Dubai is a shopping paradise. Visit traditional souks for spices, gold, and textiles. Modern malls offer luxury brands. Bargaining is expected in souks. Tax-free shopping makes it attractive for international visitors.",
            icon = "🛍️"
        ),
        TravelTip(
            title = "Internet & Connectivity",
            description = "Free Wi-Fi is available in most hotels, malls, and restaurants. Consider getting a local SIM card for data. VPN services may be restricted. Social media and messaging apps work normally.",
            icon = "📶"
        ),
        TravelTip(
            title = "Language & Communication",
            description = "Arabic is the official language, but English is widely spoken in tourist areas. Most signs are in both Arabic and English. Learning basic Arabic phrases is appreciated but not necessary for tourists.",
            icon = "🗣️"
        ),
        TravelTip(
            title = "Photography & Social Media",
            description = "Photography is generally allowed in public places. Avoid taking photos of government buildings, military installations, or people without permission. Be respectful when photographing locals. Social media sharing is common and accepted.",
            icon = "📸"
        )
    )

    // Traveler Reviews
    val reviews = listOf(
        Review(
            id = "1",
            userId = "user_1",
            userName = "Sarah Johnson",
            userAvatar = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face",
            rating = 5.0f,
            title = "Amazing Desert Safari Experience",
            comment = "Amazing experience! The desert safari was absolutely incredible. Our guide was knowledgeable and the sunset views were breathtaking.",
            date = System.currentTimeMillis() - 172800000, // 2 days ago
            location = "Dubai, UAE",
            helpfulCount = 24,
            isVerified = true,
            images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=300&fit=crop&crop=center"),
            tags = listOf("Desert Safari", "Sunset", "Great Guide")
        ),
        Review(
            id = "2",
            userId = "user_2",
            userName = "Michael Chen",
            userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face",
            rating = 4.8f,
            title = "Burj Khalifa - Must Visit!",
            comment = "The Burj Khalifa observation deck offers the most spectacular views of Dubai. The sunset timing was perfect and the staff was very helpful.",
            date = System.currentTimeMillis() - 86400000, // 1 day ago
            location = "Dubai, UAE",
            helpfulCount = 18,
            isVerified = true,
            images = listOf("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=400&h=300&fit=crop&crop=center"),
            tags = listOf("Burj Khalifa", "Sunset", "Amazing Views")
        ),
        Review(
            id = "3",
            userId = "user_3",
            userName = "Emma Rodriguez",
            userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&h=150&fit=crop&crop=face",
            rating = 4.9f,
            title = "Luxury Marina Cruise",
            comment = "The marina cruise was absolutely magical! The yacht was luxurious, the dinner was delicious, and the Dubai Fountain show from the water was unforgettable.",
            date = System.currentTimeMillis() - 43200000, // 12 hours ago
            location = "Dubai, UAE",
            helpfulCount = 15,
            isVerified = true,
            images = listOf("https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=300&fit=crop&crop=center"),
            tags = listOf("Marina Cruise", "Luxury", "Fountain Show")
        )
    )



    // Multilingual Dummy Data
    object MultilingualData {
        
        // English Data
        val englishDestinations = listOf(
            Destination(
                id = "en_1",
                name = "Burj Khalifa",
                location = "Downtown Dubai",
                description = "The world's tallest building offering spectacular city views from observation decks.",
                rating = 4.8f,
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center"),
                basePrice = 149.0,
                timeSlots = listOf(TimeSlot("09:00 AM", "11:00 AM", 149.0, "Morning Tour")),
                amenities = listOf("Guided Tour", "Skip-the-Line", "Audio Guide"),
                badge = "Most Booked"
            )
        )

        // Mongolian Data
        val mongolianDestinations = listOf(
            Destination(
                id = "mn_1",
                name = "Бурж Халифа",
                location = "Дубай хотын төв",
                description = "Дэлхийн хамгийн өндөр барилга бөгөөд ажиглалтын тавцангаас хотын гайхалтай үзэмжийг харж болно.",
                rating = 4.8f,
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center"),
                basePrice = 149.0,
                timeSlots = listOf(TimeSlot("09:00 AM", "11:00 AM", 149.0, "Өглөөний аялал")),
                amenities = listOf("Удирдлагатай аялал", "Шугамыг алгасах", "Аудио заавар"),
                badge = "Хамгийн их захиалагдсан"
            )
        )

        // Russian Data
        val russianDestinations = listOf(
            Destination(
                id = "ru_1",
                name = "Бурдж-Халифа",
                location = "Центр Дубая",
                description = "Самое высокое здание в мире, предлагающее захватывающие виды на город с смотровых площадок.",
                rating = 4.8f,
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center"),
                basePrice = 149.0,
                timeSlots = listOf(TimeSlot("09:00 AM", "11:00 AM", 149.0, "Утренняя экскурсия")),
                amenities = listOf("Экскурсия с гидом", "Без очереди", "Аудиогид"),
                badge = "Самое популярное"
            )
        )

        // Chinese Data
        val chineseDestinations = listOf(
            Destination(
                id = "zh_1",
                name = "哈利法塔",
                location = "迪拜市中心",
                description = "世界最高建筑，从观景台可欣赏壮丽的城市景观。",
                rating = 4.8f,
                images = listOf("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center"),
                basePrice = 149.0,
                timeSlots = listOf(TimeSlot("09:00 AM", "11:00 AM", 149.0, "上午游览")),
                amenities = listOf("导游陪同", "免排队", "语音导览"),
                badge = "最受欢迎"
            )
        )

        // Multilingual Activities
        val englishActivities = listOf(
            Activity(
                id = "en_act_1",
                name = "Desert Safari Adventure",
                category = "Adventure",
                description = "Experience the thrill of dune bashing in the Dubai desert with traditional Bedouin camp experience.",
                duration = "6 hours",
                rating = 4.9f,
                images = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"),
                basePrice = 89.0,
                timeSlots = listOf(TimeSlot("02:00 PM", "08:00 PM", 89.0, "Afternoon Safari")),
                features = listOf("Dune Bashing", "Camel Ride", "BBQ Dinner")
            )
        )

        val mongolianActivities = listOf(
            Activity(
                id = "mn_act_1",
                name = "Цөлийн аяллын адал явдал",
                category = "Адал явдал",
                description = "Дубайн цөлд элсний дов дээр явж, уугуул ардын хөдөлгөөнт аяллын туршлагатай болно.",
                duration = "6 цаг",
                rating = 4.9f,
                images = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"),
                basePrice = 89.0,
                timeSlots = listOf(TimeSlot("02:00 PM", "08:00 PM", 89.0, "Үдээс хойшхи аялал")),
                features = listOf("Элсний дов дээр явгах", "Тэмээ унах", "BBQ оройн хоол")
            )
        )

        val russianActivities = listOf(
            Activity(
                id = "ru_act_1",
                name = "Приключение в пустыне",
                category = "Приключения",
                description = "Испытайте острые ощущения от катания по дюнам в пустыне Дубая с традиционным опытом бедуинского лагеря.",
                duration = "6 часов",
                rating = 4.9f,
                images = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"),
                basePrice = 89.0,
                timeSlots = listOf(TimeSlot("02:00 PM", "08:00 PM", 89.0, "Дневное сафари")),
                features = listOf("Катание по дюнам", "Верховая езда на верблюде", "BBQ ужин")
            )
        )

        val chineseActivities = listOf(
            Activity(
                id = "zh_act_1",
                name = "沙漠探险之旅",
                category = "探险",
                description = "在迪拜沙漠体验沙丘冲浪的刺激，体验传统贝都因营地生活。",
                duration = "6小时",
                rating = 4.9f,
                images = listOf("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&h=600&fit=crop"),
                basePrice = 89.0,
                timeSlots = listOf(TimeSlot("02:00 PM", "08:00 PM", 89.0, "下午探险")),
                features = listOf("沙丘冲浪", "骑骆驼", "烧烤晚餐")
            )
        )
    }
} 