package com.devicesync.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicesync.app.adapters.TeamMemberAdapter
import com.devicesync.app.data.models.TeamMember

class TeamActivity : AppCompatActivity() {
    
    private lateinit var teamRecyclerView: RecyclerView
    private lateinit var teamAdapter: TeamMemberAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)
        
        setupViews()
        loadTeamData()
    }
    
    private fun setupViews() {
        teamRecyclerView = findViewById(R.id.teamRecyclerView)
        teamRecyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadTeamData() {
        val teamMembers = listOf(
            TeamMember(
                id = "1",
                name = "Bilal",
                position = "Chairman",
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                facebookUrl = "https://facebook.com/bilal.dubai",
                instagramUrl = "https://instagram.com/bilal.dubai",
                email = "bilal@dubaitourism.com",
                description = "Leading our Dubai tourism vision with over 15 years of experience in the travel industry."
            ),
            TeamMember(
                id = "2",
                name = "Lena",
                position = "Supervisor",
                imageUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=400",
                facebookUrl = "https://facebook.com/lena.dubai",
                instagramUrl = "https://instagram.com/lena.dubai",
                email = "lena@dubaitourism.com",
                description = "Overseeing operations and ensuring exceptional customer experiences across all our tours."
            ),
            TeamMember(
                id = "3",
                name = "Marita",
                position = "Manager",
                imageUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400",
                facebookUrl = "https://facebook.com/marita.dubai",
                instagramUrl = "https://instagram.com/marita.dubai",
                email = "marita@dubaitourism.com",
                description = "Managing day-to-day operations and coordinating with our tour guides for seamless experiences."
            ),
            TeamMember(
                id = "4",
                name = "Quandil",
                position = "Manager",
                imageUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400",
                facebookUrl = "https://facebook.com/quandil.dubai",
                instagramUrl = "https://instagram.com/quandil.dubai",
                email = "quandil@dubaitourism.com",
                description = "Leading our customer relations and ensuring every guest receives personalized attention."
            ),
            TeamMember(
                id = "5",
                name = "Ahmed",
                position = "Tour Guide",
                imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                facebookUrl = "https://facebook.com/ahmed.guide",
                instagramUrl = "https://instagram.com/ahmed.guide",
                email = "ahmed@dubaitourism.com",
                description = "Expert guide specializing in historical Dubai tours and cultural experiences."
            ),
            TeamMember(
                id = "6",
                name = "Sarah",
                position = "Tour Guide",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                facebookUrl = "https://facebook.com/sarah.guide",
                instagramUrl = "https://instagram.com/sarah.guide",
                email = "sarah@dubaitourism.com",
                description = "Passionate guide for luxury tours and exclusive Dubai experiences."
            ),
            TeamMember(
                id = "7",
                name = "Mohammed",
                position = "Tour Guide",
                imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
                facebookUrl = "https://facebook.com/mohammed.guide",
                instagramUrl = "https://instagram.com/mohammed.guide",
                email = "mohammed@dubaitourism.com",
                description = "Adventure tour specialist with deep knowledge of Dubai's desert and outdoor activities."
            ),
            TeamMember(
                id = "8",
                name = "Fatima",
                position = "Tour Guide",
                imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
                facebookUrl = "https://facebook.com/fatima.guide",
                instagramUrl = "https://instagram.com/fatima.guide",
                email = "fatima@dubaitourism.com",
                description = "Family tour expert creating memorable experiences for visitors of all ages."
            ),
            TeamMember(
                id = "9",
                name = "Omar",
                position = "Tour Guide",
                imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
                facebookUrl = "https://facebook.com/omar.guide",
                instagramUrl = "https://instagram.com/omar.guide",
                email = "omar@dubaitourism.com",
                description = "Food and culture specialist showcasing Dubai's culinary delights and traditions."
            )
        )
        
        teamAdapter = TeamMemberAdapter(teamMembers)
        teamRecyclerView.adapter = teamAdapter
    }
} 