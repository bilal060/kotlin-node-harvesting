package com.devicesync.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.CustomTripActivity
import com.devicesync.app.R
import com.devicesync.app.adapters.SelectableServiceAdapter
import com.devicesync.app.data.Service
import com.devicesync.app.data.StaticDataRepository

class CustomTripStep3Fragment : Fragment() {

    private lateinit var servicesRecyclerView: RecyclerView
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var selectedCountText: TextView
    private lateinit var servicesAdapter: SelectableServiceAdapter
    private val selectedServices = mutableListOf<Service>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_trip_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        setupListeners()
        loadServices()
    }

    private fun setupViews(view: View) {
        servicesRecyclerView = view.findViewById(R.id.servicesRecyclerView)
        nextButton = view.findViewById(R.id.nextButton)
        previousButton = view.findViewById(R.id.previousButton)
        selectedCountText = view.findViewById(R.id.selectedCountText)
    }

    private fun setupRecyclerView() {
        servicesAdapter = SelectableServiceAdapter(
            onServiceSelected = { service, isSelected ->
                if (isSelected) {
                    selectedServices.add(service)
                } else {
                    selectedServices.remove(service)
                }
                updateSelectedCount()
            }
        )

        servicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = servicesAdapter
        }
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            saveSelectedServices()
            (activity as? CustomTripActivity)?.nextStep()
        }

        previousButton.setOnClickListener {
            (activity as? CustomTripActivity)?.previousStep()
        }
    }

    private fun loadServices() {
        val services = StaticDataRepository.services
        servicesAdapter.updateServices(services)
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        selectedCountText.text = "Selected: ${selectedServices.size} services"
    }

    private fun saveSelectedServices() {
        val customTripActivity = activity as? CustomTripActivity
        customTripActivity?.let { activity ->
            // Clear previous selections
            activity.selectedServicesByDay.clear()
            
            // Distribute services across days (simple distribution)
            val totalDays = calculateTotalDays()
            if (totalDays > 0) {
                selectedServices.forEachIndexed { index, service ->
                    val day = (index % totalDays) + 1
                    if (!activity.selectedServicesByDay.containsKey(day)) {
                        activity.selectedServicesByDay[day] = mutableListOf()
                    }
                    activity.selectedServicesByDay[day]?.add(service.id)
                }
            }
        }
    }

    private fun calculateTotalDays(): Int {
        val customTripActivity = activity as? CustomTripActivity
        return if (customTripActivity?.startDate?.isNotEmpty() == true && 
                   customTripActivity?.endDate?.isNotEmpty() == true) {
            try {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val startDate = dateFormat.parse(customTripActivity.startDate)
                val endDate = dateFormat.parse(customTripActivity.endDate)
                
                if (startDate != null && endDate != null) {
                    val diffInMillis = endDate.time - startDate.time
                    (diffInMillis / (24 * 60 * 60 * 1000)).toInt() + 1
                } else {
                    1
                }
            } catch (e: Exception) {
                1
            }
        } else {
            1
        }
    }
} 