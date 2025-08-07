package com.devicesync.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.devicesync.app.CustomTripActivity
import com.devicesync.app.R
import com.devicesync.app.data.Attraction
import com.devicesync.app.data.Service
import com.devicesync.app.data.StaticDataRepository
import java.text.NumberFormat
import java.util.*

class CustomTripSummaryFragment : Fragment() {

    private lateinit var tripDetailsText: TextView
    private lateinit var attractionsText: TextView
    private lateinit var servicesText: TextView
    private lateinit var pricingText: TextView
    private lateinit var confirmButton: Button
    private lateinit var previousButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_trip_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupListeners()
        displayTripSummary()
    }

    private fun setupViews(view: View) {
        tripDetailsText = view.findViewById(R.id.tripDetailsText)
        attractionsText = view.findViewById(R.id.attractionsText)
        servicesText = view.findViewById(R.id.servicesText)
        pricingText = view.findViewById(R.id.pricingText)
        confirmButton = view.findViewById(R.id.confirmButton)
        previousButton = view.findViewById(R.id.previousButton)
    }

    private fun setupListeners() {
        confirmButton.setOnClickListener {
            (activity as? CustomTripActivity)?.confirmTrip()
        }

        previousButton.setOnClickListener {
            (activity as? CustomTripActivity)?.previousStep()
        }
    }

    private fun displayTripSummary() {
        val customTripActivity = activity as? CustomTripActivity ?: return
        
        // Display trip details
        val tripDetails = buildString {
            appendLine("📅 Trip Details:")
            appendLine("Start Date: ${customTripActivity.startDate}")
            appendLine("End Date: ${customTripActivity.endDate}")
            appendLine("Adults: ${customTripActivity.numberOfAdults}")
            appendLine("Kids: ${customTripActivity.numberOfKids}")
            appendLine("Total Guests: ${customTripActivity.numberOfAdults + customTripActivity.numberOfKids}")
        }
        tripDetailsText.text = tripDetails

        // Display selected attractions
        val attractions = getSelectedAttractions(customTripActivity)
        val attractionsSummary = buildString {
            appendLine("🏛️ Selected Attractions:")
            if (attractions.isNotEmpty()) {
                attractions.forEach { attraction ->
                    appendLine("• ${attraction.name} - ${formatPrice(attraction.simplePrice)}")
                }
            } else {
                appendLine("No attractions selected")
            }
        }
        attractionsText.text = attractionsSummary

        // Display selected services
        val services = getSelectedServices(customTripActivity)
        val servicesSummary = buildString {
            appendLine("🚗 Selected Services:")
            if (services.isNotEmpty()) {
                services.forEach { service ->
                    appendLine("• ${service.name} - ${formatPrice(service.simplePrice)}")
                }
            } else {
                appendLine("No services selected")
            }
        }
        servicesText.text = servicesSummary

        // Display pricing breakdown
        val pricing = calculatePricing(customTripActivity, attractions, services)
        val pricingSummary = buildString {
            appendLine("💰 Pricing Breakdown:")
            appendLine("Attractions: ${formatPrice(pricing["attractionsCost"] as Double)}")
            appendLine("Services: ${formatPrice(pricing["servicesCost"] as Double)}")
            appendLine("Tour Guide Fee: ${formatPrice(pricing["tourGuideFee"] as Double)}")
            appendLine("Service Charge (${pricing["serviceChargePercentage"]}%): ${formatPrice(pricing["serviceCharge"] as Double)}")
            appendLine("")
            appendLine("Subtotal: ${formatPrice(pricing["subtotal"] as Double)}")
            appendLine("Total: ${formatPrice(pricing["totalCost"] as Double)}")
        }
        pricingText.text = pricingSummary
    }

    private fun getSelectedAttractions(customTripActivity: CustomTripActivity): List<Attraction> {
        val allAttractions = StaticDataRepository.attractions
        val selectedIds = customTripActivity.selectedAttractionsByDay.values.flatten().toSet()
        return allAttractions.filter { it.id.toString() in selectedIds }
    }

    private fun getSelectedServices(customTripActivity: CustomTripActivity): List<Service> {
        val allServices = StaticDataRepository.services
        val selectedIds = customTripActivity.selectedServicesByDay.values.flatten().toSet()
        return allServices.filter { it.id in selectedIds }
    }

    private fun calculatePricing(
        customTripActivity: CustomTripActivity,
        attractions: List<Attraction>,
        services: List<Service>
    ): Map<String, Any> {
        val totalGuests = customTripActivity.numberOfAdults + customTripActivity.numberOfKids
        
        // Calculate costs based on actual prices and number of guests
        val attractionsCost = attractions.sumOf { it.simplePrice * totalGuests }
        val servicesCost = services.sumOf { it.simplePrice * totalGuests }
        val tourGuideFee = CustomTripActivity.TOUR_GUIDE_FEE * totalGuests
        val serviceChargePercentage = CustomTripActivity.SERVICE_CHARGE_PERCENTAGE
        
        val subtotal = attractionsCost + servicesCost + tourGuideFee
        val serviceCharge = subtotal * (serviceChargePercentage / 100.0)
        val totalCost = subtotal + serviceCharge

        return mapOf(
            "attractionsCost" to attractionsCost,
            "servicesCost" to servicesCost,
            "tourGuideFee" to tourGuideFee,
            "serviceChargePercentage" to serviceChargePercentage,
            "serviceCharge" to serviceCharge,
            "subtotal" to subtotal,
            "totalCost" to totalCost
        )
    }

    private fun formatPrice(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        return formatter.format(price)
    }
} 