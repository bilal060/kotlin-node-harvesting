package com.devicesync.app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devicesync.app.adapters.AttractionAdapter
import com.devicesync.app.adapters.ServiceAdapter
import com.devicesync.app.adapters.TicketAdapter
import com.devicesync.app.data.Ticket
import com.devicesync.app.data.models.*
import java.text.SimpleDateFormat
import java.util.*

class TripManagementActivity : AppCompatActivity() {
    
    private lateinit var tripTitleText: TextView
    private lateinit var tripDateText: TextView
    private lateinit var guideNameText: TextView
    private lateinit var guidePhoneText: TextView
    private lateinit var guideImage: ImageView
    private lateinit var guideRatingText: TextView
    private lateinit var todayProgressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var yesterdayProgressText: TextView
    private lateinit var tomorrowPreviewText: TextView
    private lateinit var attractionsRecyclerView: RecyclerView
    private lateinit var servicesRecyclerView: RecyclerView
    private lateinit var bookedTicketsRecyclerView: RecyclerView
    private lateinit var pendingTicketsRecyclerView: RecyclerView
    private lateinit var reportIssueButton: Button
    
    private lateinit var currentTrip: TripPlan
    private lateinit var todayPlan: DailyPlan
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_management)
        
        setupViews()
        loadTripData()
        setupRecyclerViews()
        setupClickListeners()
    }
    
    private fun setupViews() {
        tripTitleText = findViewById(R.id.tripTitleText)
        tripDateText = findViewById(R.id.tripDateText)
        guideNameText = findViewById(R.id.guideNameText)
        guidePhoneText = findViewById(R.id.guidePhoneText)
        guideImage = findViewById(R.id.guideImage)
        guideRatingText = findViewById(R.id.guideRatingText)
        todayProgressText = findViewById(R.id.todayProgressText)
        progressBar = findViewById(R.id.progressBar)
        yesterdayProgressText = findViewById(R.id.yesterdayProgressText)
        tomorrowPreviewText = findViewById(R.id.tomorrowPreviewText)
        attractionsRecyclerView = findViewById(R.id.attractionsRecyclerView)
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView)
        bookedTicketsRecyclerView = findViewById(R.id.bookedTicketsRecyclerView)
        pendingTicketsRecyclerView = findViewById(R.id.pendingTicketsRecyclerView)
        reportIssueButton = findViewById(R.id.reportIssueButton)
    }
    
    private fun loadTripData() {
        // Load sample trip data
        currentTrip = createSampleTrip()
        todayPlan = currentTrip.dailyPlans[1] // Day 2 (today)
        
        // Update UI
        tripTitleText.text = currentTrip.title
        tripDateText.text = "${currentTrip.startDate} - ${currentTrip.endDate}"
        
        // Guide info
        guideNameText.text = currentTrip.guide.name
        guidePhoneText.text = currentTrip.guide.phone
        guideRatingText.text = "★ ${currentTrip.guide.rating}"
        
        Glide.with(this)
            .load(currentTrip.guide.imageUrl)
            .placeholder(R.drawable.placeholder_attraction)
            .into(guideImage)
        
        // Progress
        todayProgressText.text = "Today's Progress: ${todayPlan.progress}%"
        progressBar.progress = todayPlan.progress
        
        // Yesterday's achievements
        val yesterdayPlan = currentTrip.dailyPlans[0]
        val completedAttractions = yesterdayPlan.attractions.count { it.isCompleted }
        yesterdayProgressText.text = "Yesterday: Completed $completedAttractions attractions"
        
        // Tomorrow's preview
        val tomorrowPlan = currentTrip.dailyPlans[2]
        val tomorrowAttractions = tomorrowPlan.attractions.size
        tomorrowPreviewText.text = "Tomorrow: $tomorrowAttractions attractions planned"
    }
    
    private fun setupRecyclerViews() {
        // Attractions
        attractionsRecyclerView.layoutManager = LinearLayoutManager(this)
        val attractionAdapter = AttractionAdapter(todayPlan.attractions) { attraction ->
            // Handle attraction click
            Toast.makeText(this, "Viewing ${attraction.name}", Toast.LENGTH_SHORT).show()
        }
        attractionsRecyclerView.adapter = attractionAdapter
        
        // Services
        servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        val serviceAdapter = ServiceAdapter(todayPlan.services) { service ->
            // Handle service click
            Toast.makeText(this, "Viewing ${service.name}", Toast.LENGTH_SHORT).show()
        }
        servicesRecyclerView.adapter = serviceAdapter
        
        // Booked tickets
        bookedTicketsRecyclerView.layoutManager = LinearLayoutManager(this)
        val bookedTicketAdapter = TicketAdapter(currentTrip.bookedTickets.map { 
            Ticket(it.id, it.attractionName, "AED ${it.price}", it.validDate, "✓ Booked")
        }) { ticket ->
            // Handle ticket click
            Toast.makeText(this, "Viewing ticket for ${ticket.attractionName}", Toast.LENGTH_SHORT).show()
        }
        bookedTicketsRecyclerView.adapter = bookedTicketAdapter
        
        // Pending tickets
        pendingTicketsRecyclerView.layoutManager = LinearLayoutManager(this)
        val pendingTicketAdapter = TicketAdapter(currentTrip.pendingTickets.map { 
            Ticket(it.id, it.attractionName, "AED ${it.estimatedPrice}", it.plannedDate, "⏳ Pending")
        }) { ticket ->
            // Handle pending ticket click
            Toast.makeText(this, "Book ticket for ${ticket.attractionName}", Toast.LENGTH_SHORT).show()
        }
        pendingTicketsRecyclerView.adapter = pendingTicketAdapter
    }
    
    private fun setupClickListeners() {
        reportIssueButton.setOnClickListener {
            showReportIssueDialog()
        }
        
        // Guide contact
        guidePhoneText.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:${currentTrip.guide.phone}")
            startActivity(intent)
        }
    }
    
    private fun showReportIssueDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Report Issue with Guide")
            .setMessage("Would you like to send a request to the manager about an issue with your guide?")
            .setPositiveButton("Send Request") { _, _ ->
                // Send request to manager
                Toast.makeText(this, "Request sent to manager", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun createSampleTrip(): TripPlan {
        val guide = Guide(
            id = "1",
            name = "Ahmed Hassan",
            phone = "+971 50 123 4567",
            email = "ahmed@dubaitourism.com",
            imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
            rating = 4.8f,
            specialties = listOf("Historical Tours", "Cultural Experiences"),
            languages = listOf("English", "Arabic", "French")
        )
        
        val dailyPlans = listOf(
            // Yesterday (Day 1)
            DailyPlan(
                id = "1",
                date = "2024-03-14",
                dayNumber = 1,
                attractions = listOf(
                    PlannedAttraction("1", "Burj Khalifa", "Downtown Dubai", "09:00", "11:00", TicketStatus.USED, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "World's tallest building", true),
                    PlannedAttraction("2", "Dubai Mall", "Downtown Dubai", "11:30", "14:00", TicketStatus.USED, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "World's largest mall", true)
                ),
                services = listOf(),
                meals = listOf(),
                transportation = listOf(),
                status = DayStatus.COMPLETED,
                progress = 100
            ),
            // Today (Day 2)
            DailyPlan(
                id = "2",
                date = "2024-03-15",
                dayNumber = 2,
                attractions = listOf(
                    PlannedAttraction("3", "Palm Jumeirah", "Palm Jumeirah", "10:00", "12:00", TicketStatus.BOOKED, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "Iconic palm-shaped island", false),
                    PlannedAttraction("4", "Dubai Frame", "Zabeel Park", "14:00", "16:00", TicketStatus.PENDING, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "Modern architectural landmark", false),
                    PlannedAttraction("5", "Dubai Marina", "Dubai Marina", "17:00", "19:00", TicketStatus.BOOKED, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "Luxury yacht cruise", false)
                ),
                services = listOf(
                    PlannedService("1", "Desert Safari", ServiceType.ACTIVITY, "20:00", "23:00", "Dubai Desert", ServiceStatus.SCHEDULED, "Evening desert adventure")
                ),
                meals = listOf(
                    Meal("1", MealType.LUNCH, "Palm Restaurant", "12:30", "Palm Jumeirah", true),
                    Meal("2", MealType.DINNER, "Desert Camp", "21:00", "Dubai Desert", true)
                ),
                transportation = listOf(
                    Transportation("1", TransportType.CAR, "Hotel", "Palm Jumeirah", "09:30", true),
                    Transportation("2", TransportType.CAR, "Palm Jumeirah", "Dubai Frame", "13:30", true)
                ),
                status = DayStatus.IN_PROGRESS,
                progress = 35
            ),
            // Tomorrow (Day 3)
            DailyPlan(
                id = "3",
                date = "2024-03-16",
                dayNumber = 3,
                attractions = listOf(
                    PlannedAttraction("6", "Dubai Museum", "Al Fahidi", "09:00", "11:00", TicketStatus.PENDING, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "Historical museum", false),
                    PlannedAttraction("7", "Gold Souk", "Deira", "11:30", "13:00", TicketStatus.PENDING, "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400", "Traditional gold market", false)
                ),
                services = listOf(),
                meals = listOf(),
                transportation = listOf(),
                status = DayStatus.UPCOMING,
                progress = 0
            )
        )
        
        val bookedTickets = listOf(
            BookedTicket("1", "Palm Jumeirah", "TK001", "2024-03-10", "2024-03-15", "AED 150", TicketStatus.BOOKED),
            BookedTicket("2", "Dubai Marina", "TK002", "2024-03-10", "2024-03-15", "AED 200", TicketStatus.BOOKED)
        )
        
        val pendingTickets = listOf(
            PendingTicket("1", "Dubai Frame", "2024-03-15", "AED 50", Priority.HIGH),
            PendingTicket("2", "Dubai Museum", "2024-03-16", "AED 25", Priority.MEDIUM),
            PendingTicket("3", "Gold Souk", "2024-03-16", "Free", Priority.LOW)
        )
        
        return TripPlan(
            id = "1",
            title = "Dubai Essential Experience",
            startDate = "2024-03-14",
            endDate = "2024-03-16",
            status = TripStatus.ACTIVE,
            guide = guide,
            dailyPlans = dailyPlans,
            totalCost = "AED 1,999",
            bookedTickets = bookedTickets,
            pendingTickets = pendingTickets
        )
    }
} 