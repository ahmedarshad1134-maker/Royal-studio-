package com.example.ui.models

data class TeamMember(
    val id: String,
    val name: String,
    val role: String,
    val bio: String,
    val photoResId: Int? = null
)

data class AboutInfo(
    val studioName: String,
    val shortIntro: String,
    val story: String,
    val philosophy: String,
    val experienceText: String,
    val achievements: List<String>,
    val teamMembers: List<TeamMember>
)
