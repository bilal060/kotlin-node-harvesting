package com.devicesync.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

class TeamChatActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_chat)
        
        setupToolbar()
        setupContactButtons()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Team Chat"
    }
    
    private fun setupContactButtons() {
        // Phone contact
        findViewById<CardView>(R.id.phoneCard).setOnClickListener {
            showPhoneDialog()
        }
        
        // Email contact
        findViewById<CardView>(R.id.emailCard).setOnClickListener {
            showEmailDialog()
        }
        
        // WhatsApp contact
        findViewById<CardView>(R.id.whatsappCard).setOnClickListener {
            showWhatsAppDialog()
        }
        
        // Live chat
        findViewById<CardView>(R.id.liveChatCard).setOnClickListener {
            showLiveChatDialog()
        }
    }
    
    private fun showPhoneDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("📞 Call Our Team")
            .setMessage("Speak directly with our Dubai tourism experts!\n\n" +
                    "Phone: +971525278207\n\n" +
                    "Available: 9 AM - 6 PM (GST)\n" +
                    "Languages: English, Arabic, Russian, Chinese")
            .setPositiveButton("Call Now") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:+971525278207")
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun showEmailDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("📧 Email Support")
            .setMessage("Send us an email for detailed assistance!\n\n" +
                    "Email: bilal.xbt@gmail.com\n\n" +
                    "We'll respond within 24 hours with:\n" +
                    "• Tour recommendations\n" +
                    "• Booking assistance\n" +
                    "• Travel planning help\n" +
                    "• Custom itinerary requests")
            .setPositiveButton("Send Email") { _, _ ->
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.type = "message/rfc822"
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("bilal.xbt@gmail.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Dubai Discoveries - Support Request")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Dubai Discoveries team,\n\nI need assistance with...")
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."))
                } catch (e: Exception) {
                    // Handle case where no email app is available
                    val fallbackDialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
                        .setTitle("No Email App")
                        .setMessage("Please install an email app or contact us directly at:\n\nbilal.xbt@gmail.com")
                        .setPositiveButton("OK") { _, _ -> }
                        .create()
                    fallbackDialog.show()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun showWhatsAppDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("💬 WhatsApp Chat")
            .setMessage("Chat with us on WhatsApp for instant support!\n\n" +
                    "WhatsApp: +971525278207\n\n" +
                    "Perfect for:\n" +
                    "• Quick questions\n" +
                    "• Tour bookings\n" +
                    "• Real-time assistance\n" +
                    "• Photo sharing")
            .setPositiveButton("Open WhatsApp") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://wa.me/971525278207?text=Hello%20Dubai%20Discoveries%20team,%20I%20need%20assistance%20with...")
                    startActivity(intent)
                } catch (e: Exception) {
                    val fallbackDialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
                        .setTitle("WhatsApp Not Available")
                        .setMessage("Please install WhatsApp or contact us via phone: +971525278207")
                        .setPositiveButton("OK") { _, _ -> }
                        .create()
                    fallbackDialog.show()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    private fun showLiveChatDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("💬 Live Chat")
            .setMessage("Our live chat feature is coming soon!\n\n" +
                    "For now, please use:\n" +
                    "• Phone: +971525278207\n" +
                    "• Email: bilal.xbt@gmail.com\n" +
                    "• WhatsApp: +971525278207\n\n" +
                    "We're working on bringing you instant chat support!")
            .setPositiveButton("OK") { _, _ -> }
            .create()
        
        dialog.show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 