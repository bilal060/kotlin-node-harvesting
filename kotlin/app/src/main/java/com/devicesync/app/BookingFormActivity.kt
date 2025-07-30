package com.devicesync.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class BookingFormActivity : AppCompatActivity() {
    
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var peopleCountText: TextView
    private lateinit var decreasePeopleButton: Button
    private lateinit var increasePeopleButton: Button
    private lateinit var contactNameEditText: EditText
    private lateinit var contactEmailEditText: EditText
    private lateinit var contactPhoneEditText: EditText
    private lateinit var specialRequestsEditText: EditText
    private lateinit var bookNowButton: Button

    
    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null
    private var peopleCount = 2
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BookingFormActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_form)
        
        initializeViews()
        setupClickListeners()
        updatePeopleCount()
        
        // Handle dates passed from main activity
        handlePassedDates()
    }
    
    private fun initializeViews() {
        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        dateButton = findViewById(R.id.dateButton)
        timeButton = findViewById(R.id.timeButton)
        peopleCountText = findViewById(R.id.peopleCountText)
        decreasePeopleButton = findViewById(R.id.decreasePeopleButton)
        increasePeopleButton = findViewById(R.id.increasePeopleButton)
        contactNameEditText = findViewById(R.id.contactNameEditText)
        contactEmailEditText = findViewById(R.id.contactEmailEditText)
        contactPhoneEditText = findViewById(R.id.contactPhoneEditText)
        specialRequestsEditText = findViewById(R.id.specialRequestsEditText)
        bookNowButton = findViewById(R.id.bookNowButton)
    }
    
    private fun setupClickListeners() {
        dateButton.setOnClickListener {
            showDatePicker()
        }
        
        timeButton.setOnClickListener {
            showTimePicker()
        }
        
        decreasePeopleButton.setOnClickListener {
            if (peopleCount > 1) {
                peopleCount--
                updatePeopleCount()
            }
        }
        
        increasePeopleButton.setOnClickListener {
            if (peopleCount < 20) {
                peopleCount++
                updatePeopleCount()
            }
        }
        
        bookNowButton.setOnClickListener {
            submitBooking()
        }
    }
    
    private fun handlePassedDates() {
        val startDateMillis = intent.getLongExtra("startDate", -1)
        val endDateMillis = intent.getLongExtra("endDate", -1)
        
        if (startDateMillis != -1L && endDateMillis != -1L) {
            val startDate = Calendar.getInstance().apply { timeInMillis = startDateMillis }
            val endDate = Calendar.getInstance().apply { timeInMillis = endDateMillis }
            
            // Set the date button to show the date range
            val dateRangeText = "${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
            dateButton.text = dateRangeText
            
            // Set selected date to start date for time picker
            selectedDate = startDate
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                selectedDate?.let { date ->
                    dateButton.text = "📅 ${dateFormat.format(date.time)}"
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                selectedTime?.let { time ->
                    timeButton.text = "🕐 ${timeFormat.format(time.time)}"
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        
        timePickerDialog.show()
    }
    
    private fun updatePeopleCount() {
        peopleCountText.text = peopleCount.toString()
    }
    
    private fun submitBooking() {
        // Validate form
        if (!validateForm()) {
            return
        }
        
        // Show success dialog
        AlertDialog.Builder(this, R.style.WhiteDialogTheme)
            .setTitle("Booking Submitted")
            .setMessage("Your booking has been submitted successfully! We will contact you soon to confirm your reservation.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun validateForm(): Boolean {
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (selectedTime == null) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (contactNameEditText.text.isNullOrBlank()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (contactEmailEditText.text.isNullOrBlank()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (contactPhoneEditText.text.isNullOrBlank()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 