package com.devicesync.app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.devicesync.app.fragments.CustomTripStep1Fragment
import com.devicesync.app.fragments.CustomTripStep2Fragment
import com.devicesync.app.fragments.CustomTripStep3Fragment
import com.devicesync.app.fragments.CustomTripSummaryFragment

class CustomTripAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CustomTripStep1Fragment()
            1 -> CustomTripStep2Fragment()
            2 -> CustomTripStep3Fragment()
            3 -> CustomTripSummaryFragment()
            else -> CustomTripStep1Fragment()
        }
    }
} 