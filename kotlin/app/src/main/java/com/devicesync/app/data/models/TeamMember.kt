package com.devicesync.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TeamMember(
    val id: String,
    val name: String,
    val position: String,
    val imageUrl: String,
    val facebookUrl: String? = null,
    val instagramUrl: String? = null,
    val email: String? = null,
    val description: String = ""
) : Parcelable 