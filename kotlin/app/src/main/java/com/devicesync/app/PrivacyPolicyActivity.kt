package com.devicesync.app

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.devicesync.app.R

class PrivacyPolicyActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy"
        
        val privacyText = findViewById<TextView>(R.id.privacyText)
        privacyText.movementMethod = ScrollingMovementMethod()
        privacyText.text = getPrivacyPolicyText()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    private fun getPrivacyPolicyText(): String {
        return """
            PRIVACY POLICY FOR DUBAI DISCOVERIES
            
            Effective Date: ${java.time.LocalDate.now()}
            
            Welcome to Dubai Discoveries! We are committed to protecting your privacy and providing you with a secure and personalized tourism experience. This Privacy Policy explains how we collect, use, and protect your information when you use our Dubai tourism application.
            
            🏛️ ABOUT OUR APP
            Dubai Discoveries is a comprehensive tourism application designed to enhance your Dubai travel experience by providing personalized recommendations, booking services, and travel assistance.
            
            📱 PERMISSIONS WE REQUEST AND WHY
            
            1. INTERNET & NETWORK ACCESS
               • Purpose: To provide real-time tourism information, booking services, and sync your travel data
               • How we use it: Connect to our servers for attractions data, booking confirmations, and travel updates
               • Benefit to you: Access to live information, instant bookings, and seamless travel planning
            
            2. CONTACTS PERMISSION
               • Purpose: To help you coordinate group tours and share travel plans with friends and family
               • How we use it: Enable group booking features and allow you to share trip details with contacts
               • Benefit to you: Easier group tour coordination and sharing of travel experiences
               • Data protection: We do not store or share your contacts without your explicit permission
            
            3. SMS PERMISSION
               • Purpose: To send you booking confirmations, travel updates, and important tour notifications
               • How we use it: Send automated confirmations for bookings, flight updates, and tour reminders
               • Benefit to you: Stay informed about your travel plans and receive important updates
               • Data protection: We only read SMS related to our services and do not access personal messages
            
            4. CALL LOGS PERMISSION
               • Purpose: To track important travel-related calls for better customer service
               • How we use it: Monitor customer support interactions and tour guide communications
               • Benefit to you: Improved customer service and better tracking of travel arrangements
               • Data protection: We only access calls related to our tourism services
            
            5. STORAGE & MEDIA PERMISSION
               • Purpose: To save your travel photos, documents, and create travel memories
               • How we use it: Store your travel photos, save booking confirmations, and create travel albums
               • Benefit to you: Preserve your Dubai travel memories and access important travel documents
               • Data protection: Your media files are stored locally and not shared without permission
            
            6. LOCATION PERMISSION
               • Purpose: To provide location-based recommendations and nearby attraction suggestions
               • How we use it: Show nearby attractions, restaurants, and services based on your location
               • Benefit to you: Discover hidden gems and get personalized recommendations
               • Data protection: Location data is only used for recommendations and not stored permanently
            
            7. NOTIFICATION PERMISSION
               • Purpose: To keep you informed about tour updates, booking confirmations, and special offers
               • How we use it: Send timely notifications about your travel plans and Dubai attractions
               • Benefit to you: Never miss important travel updates or exclusive Dubai offers
               • Data protection: Notifications are only sent for legitimate tourism purposes
            
            🔒 HOW WE PROTECT YOUR DATA
            
            • Encryption: All data transmission is encrypted using industry-standard protocols
            • Secure Storage: Your personal information is stored securely on our servers
            • Limited Access: Only authorized personnel have access to your data
            • Data Retention: We retain your data only as long as necessary for tourism services
            • No Third-Party Sharing: We do not sell or share your data with third parties for marketing purposes
            
            📊 DATA WE COLLECT
            
            • Personal Information: Name, email, phone number for booking purposes
            • Travel Preferences: Your interests and preferences for personalized recommendations
            • Usage Data: How you interact with our app to improve services
            • Location Data: Only when you grant permission for nearby recommendations
            • Communication Data: SMS and call logs related to our tourism services
            
            🎯 HOW WE USE YOUR DATA
            
            • Provide Tourism Services: Bookings, recommendations, and travel assistance
            • Personalize Experience: Tailored recommendations based on your preferences
            • Customer Support: Respond to your inquiries and provide assistance
            • Service Improvement: Analyze usage patterns to enhance our services
            • Legal Compliance: Meet legal obligations and protect against fraud
            
            🚫 WHAT WE DON'T DO
            
            • We do not sell your personal information to third parties
            • We do not use your data for purposes unrelated to tourism services
            • We do not access your personal SMS or call logs without permission
            • We do not share your location data with unauthorized parties
            • We do not store your data longer than necessary
            
            👤 YOUR RIGHTS
            
            You have the right to:
            • Access your personal data stored by our app
            • Request correction of inaccurate information
            • Request deletion of your data
            • Opt-out of certain data collection
            • Withdraw consent for specific permissions
            • Contact us with privacy concerns
            
            🔄 DATA RETENTION
            
            • Booking Data: Retained for 2 years for customer service purposes
            • Usage Analytics: Retained for 1 year for service improvement
            • Communication Data: Retained for 6 months for support purposes
            • Location Data: Not stored permanently, only used for real-time recommendations
            
            🌍 INTERNATIONAL DATA TRANSFER
            
            Your data may be processed in countries other than your own. We ensure that such transfers comply with applicable data protection laws and implement appropriate safeguards.
            
            📱 APP UPDATES
            
            This Privacy Policy may be updated as our services evolve. We will notify you of significant changes through the app or email.
            
            📞 CONTACT US
            
            If you have questions about this Privacy Policy or our data practices:
            
            Email: privacy@dubaidiscoveries.com
            Phone: +971-4-XXX-XXXX
            Address: Dubai Tourism Office, Dubai, UAE
            
            🛡️ COMPLIANCE
            
            We comply with:
            • General Data Protection Regulation (GDPR)
            • UAE Data Protection Law
            • Google Play Store Privacy Requirements
            • Android Permission Guidelines
            
            By using Dubai Discoveries, you agree to this Privacy Policy and our data practices. We are committed to protecting your privacy while providing you with an exceptional Dubai tourism experience.
            
            Thank you for choosing Dubai Discoveries!
        """.trimIndent()
    }
} 