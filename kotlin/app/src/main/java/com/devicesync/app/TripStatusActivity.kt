package com.devicesync.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.TicketAdapter
import com.devicesync.app.data.Ticket

class TripStatusActivity : AppCompatActivity() {
    
    private lateinit var todayPlanText: TextView
    private lateinit var guideInfoText: TextView
    private lateinit var yesterdayAchievementsText: TextView
    private lateinit var todayProgressText: TextView
    private lateinit var tomorrowPlanText: TextView
    private lateinit var ticketsRecyclerView: RecyclerView
    private lateinit var reportIssueButton: Button
    private lateinit var ticketAdapter: TicketAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_status)
        
        initializeViews()
        setupRecyclerView()
        loadTripData()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        todayPlanText = findViewById(R.id.todayPlanText)
        guideInfoText = findViewById(R.id.guideInfoText)
        yesterdayAchievementsText = findViewById(R.id.yesterdayAchievementsText)
        todayProgressText = findViewById(R.id.todayProgressText)
        tomorrowPlanText = findViewById(R.id.tomorrowPlanText)
        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView)
        reportIssueButton = findViewById(R.id.reportIssueButton)
    }
    
    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter(emptyList()) { _ ->
            // Handle ticket click
        }
        
        ticketsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TripStatusActivity)
            adapter = ticketAdapter
        }
    }
    
    private fun loadTripData() {
        // Today's Plan
        todayPlanText.text = """
            🗓️ Today's Plan (Aug 10, 2024)
            
            🌅 9:00 AM - Hotel Pickup
            🏛️ 10:00 AM - Burj Khalifa Visit
            🍽️ 12:30 PM - Lunch at Dubai Mall
            🏜️ 2:00 PM - Desert Safari
            🌅 6:00 PM - Sunset at Palm Jumeirah
            🍽️ 8:00 PM - Dinner at Marina
            🏨 10:00 PM - Return to Hotel
        """.trimIndent()
        
        // Guide Information
        guideInfoText.text = """
            👨‍💼 Your Guide: Ahmed Al Mansouri
            📞 Contact: +971 50 123 4567
            🚗 Vehicle: Toyota Land Cruiser
            🕐 Meeting Time: 9:00 AM at Hotel Lobby
            📍 Pickup Location: Hotel Lobby
        """.trimIndent()
        
        // Yesterday's Achievements
        yesterdayAchievementsText.text = """
            ✅ Yesterday's Achievements (Aug 9, 2024)
            
            🏛️ Completed: Dubai Museum Tour
            🛍️ Completed: Gold Souk Shopping
            🍽️ Completed: Traditional Emirati Dinner
            📸 Completed: Dubai Creek Photo Session
            🚢 Completed: Abra Boat Ride
        """.trimIndent()
        
        // Today's Progress
        todayProgressText.text = """
            📊 Today's Progress
            
            🏛️ Burj Khalifa: ✅ Completed
            🍽️ Lunch: ✅ Completed
            🏜️ Desert Safari: 🔄 In Progress
            🌅 Palm Jumeirah: ⏳ Pending
            🍽️ Dinner: ⏳ Pending
        """.trimIndent()
        
        // Tomorrow's Plan
        tomorrowPlanText.text = """
            📅 Tomorrow's Plan (Aug 11, 2024)
            
            🏖️ 9:00 AM - JBR Beach Visit
            🏛️ 11:00 AM - Dubai Frame
            🍽️ 1:00 PM - Lunch at Global Village
            🎡 3:00 PM - Ain Dubai Wheel
            🛍️ 5:00 PM - Mall of Emirates
            🍽️ 7:00 PM - Dinner at Burj Al Arab
            🏨 9:00 PM - Return to Hotel
        """.trimIndent()
        
        // Load tickets
        val tickets = listOf(
            Ticket("TK001", "Burj Khalifa", "AED 149", "2024-08-10", "✓ Booked"),
            Ticket("TK002", "Desert Safari", "AED 250", "2024-08-10", "✓ Booked"),
            Ticket("TK003", "Palm Jumeirah", "AED 180", "2024-08-10", "⏳ Pending"),
            Ticket("TK004", "Marina Dinner", "AED 120", "2024-08-10", "⏳ Pending"),
            Ticket("TK005", "JBR Beach", "AED 80", "2024-08-11", "⏳ Pending"),
            Ticket("TK006", "Dubai Frame", "AED 95", "2024-08-11", "⏳ Pending")
        )
        
        ticketAdapter.updateTickets(tickets)
    }
    
    private fun setupClickListeners() {
        reportIssueButton.setOnClickListener {
            showReportIssueDialog()
        }
    }
    
    private fun showReportIssueDialog() {
        val dialog = AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("🚨 Report Issue with Guide")
            .setMessage("Please describe the issue you're experiencing with your guide:")
            .setView(R.layout.dialog_report_issue)
            .setPositiveButton("Submit Report") { _, _ ->
                // Handle report submission
                showSuccessDialog("Report submitted successfully. We'll contact you shortly.")
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("✅ Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
} 