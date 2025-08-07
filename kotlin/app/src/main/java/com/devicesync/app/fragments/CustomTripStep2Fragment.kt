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
import com.devicesync.app.adapters.SelectableAttractionAdapter
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.StaticDataRepository

class CustomTripStep2Fragment : Fragment() {

    private lateinit var attractionsRecyclerView: RecyclerView
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var selectedCountText: TextView
    private lateinit var attractionsAdapter: SelectableAttractionAdapter
    private val selectedAttractions = mutableListOf<Attraction>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_trip_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        setupListeners()
        loadAttractions()
    }

    private fun setupViews(view: View) {
        attractionsRecyclerView = view.findViewById(R.id.attractionsRecyclerView)
        nextButton = view.findViewById(R.id.nextButton)
        previousButton = view.findViewById(R.id.previousButton)
        selectedCountText = view.findViewById(R.id.selectedCountText)
    }

    private fun setupRecyclerView() {
        attractionsAdapter = SelectableAttractionAdapter(
            onAttractionSelected = { attraction, isSelected ->
                if (isSelected) {
                    selectedAttractions.add(attraction)
                } else {
                    selectedAttractions.remove(attraction)
                }
                updateSelectedCount()
            }
        )

        attractionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = attractionsAdapter
        }
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            if (selectedAttractions.isNotEmpty()) {
                saveSelectedAttractions()
                (activity as? CustomTripActivity)?.nextStep()
            }
        }

        previousButton.setOnClickListener {
            (activity as? CustomTripActivity)?.previousStep()
        }
    }

    private fun loadAttractions() {
        val attractions = StaticDataRepository.attractions
        attractionsAdapter.updateAttractions(attractions)
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        selectedCountText.text = "Selected: ${selectedAttractions.size} attractions"
        nextButton.isEnabled = selectedAttractions.isNotEmpty()
    }

    private fun saveSelectedAttractions() {
        val customTripActivity = activity as? CustomTripActivity
        customTripActivity?.let { activity ->
            // Clear previous selections
            activity.selectedAttractionsByDay.clear()
            
            // Distribute attractions across days (simple distribution)
            val totalDays = calculateTotalDays()
            if (totalDays > 0) {
                selectedAttractions.forEachIndexed { index, attraction ->
                    val day = (index % totalDays) + 1
                    if (!activity.selectedAttractionsByDay.containsKey(day)) {
                        activity.selectedAttractionsByDay[day] = mutableListOf()
                    }
                    activity.selectedAttractionsByDay[day]?.add(attraction.id.toString())
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