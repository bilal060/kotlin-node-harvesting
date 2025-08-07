package com.devicesync.app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.devicesync.app.CustomTripActivity
import com.devicesync.app.R
import java.text.SimpleDateFormat
import java.util.*

class CustomTripStep1Fragment : Fragment() {

    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var adultsInput: EditText
    private lateinit var kidsInput: EditText
    private lateinit var nextButton: Button
    private lateinit var totalDaysText: TextView

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_trip_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupDatePickers()
        setupListeners()
    }

    private fun setupViews(view: View) {
        startDateInput = view.findViewById(R.id.startDateInput)
        endDateInput = view.findViewById(R.id.endDateInput)
        adultsInput = view.findViewById(R.id.adultsInput)
        kidsInput = view.findViewById(R.id.kidsInput)
        nextButton = view.findViewById(R.id.nextButton)
        totalDaysText = view.findViewById(R.id.totalDaysText)

        // Set default values
        adultsInput.setText("1")
        kidsInput.setText("0")
    }

    private fun setupDatePickers() {
        startDateInput.setOnClickListener {
            showDatePicker(startDateInput, true)
        }

        endDateInput.setOnClickListener {
            showDatePicker(endDateInput, false)
        }
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            if (validateInputs()) {
                updateTripDetails()
                (activity as? CustomTripActivity)?.nextStep()
            }
        }

        // Update total days when dates change
        startDateInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateTotalDays()
            }
        })

        endDateInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateTotalDays()
            }
        })
    }

    private fun showDatePicker(editText: EditText, isStartDate: Boolean) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                
                if (isStartDate) {
                    // Ensure end date is after start date
                    if (endDateInput.text.isNotEmpty()) {
                        val endDate = dateFormat.parse(endDateInput.text.toString())
                        if (endDate != null && selectedDate.time.after(endDate)) {
                            endDateInput.setText("")
                        }
                    }
                } else {
                    // Ensure start date is before end date
                    if (startDateInput.text.isNotEmpty()) {
                        val startDate = dateFormat.parse(startDateInput.text.toString())
                        if (startDate != null && selectedDate.time.before(startDate)) {
                            return@DatePickerDialog
                        }
                    }
                }
                
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (isStartDate) {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        } else {
            if (startDateInput.text.isNotEmpty()) {
                val startDate = dateFormat.parse(startDateInput.text.toString())
                if (startDate != null) {
                    datePickerDialog.datePicker.minDate = startDate.time
                }
            }
        }

        datePickerDialog.show()
    }

    private fun updateTotalDays() {
        if (startDateInput.text.isNotEmpty() && endDateInput.text.isNotEmpty()) {
            try {
                val startDate = dateFormat.parse(startDateInput.text.toString())
                val endDate = dateFormat.parse(endDateInput.text.toString())
                
                if (startDate != null && endDate != null) {
                    val diffInMillis = endDate.time - startDate.time
                    val diffInDays = (diffInMillis / (24 * 60 * 60 * 1000)).toInt() + 1
                    totalDaysText.text = "Total Days: $diffInDays"
                }
            } catch (e: Exception) {
                totalDaysText.text = "Total Days: 0"
            }
        } else {
            totalDaysText.text = "Total Days: 0"
        }
    }

    private fun validateInputs(): Boolean {
        if (startDateInput.text.isEmpty()) {
            startDateInput.error = "Please select start date"
            return false
        }
        
        if (endDateInput.text.isEmpty()) {
            endDateInput.error = "Please select end date"
            return false
        }
        
        val adults = adultsInput.text.toString().toIntOrNull() ?: 0
        if (adults < 1) {
            adultsInput.error = "At least 1 adult required"
            return false
        }
        
        val kids = kidsInput.text.toString().toIntOrNull() ?: 0
        if (kids < 0) {
            kidsInput.error = "Invalid number of kids"
            return false
        }
        
        return true
    }

    private fun updateTripDetails() {
        val adults = adultsInput.text.toString().toIntOrNull() ?: 1
        val kids = kidsInput.text.toString().toIntOrNull() ?: 0
        
        (activity as? CustomTripActivity)?.updateTripDetails(
            startDateInput.text.toString(),
            endDateInput.text.toString(),
            adults,
            kids
        )
    }
} 