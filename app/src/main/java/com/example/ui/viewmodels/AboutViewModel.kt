package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.AboutInfo
import com.example.ui.models.TeamMember
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AboutUiState(
    val isLoading: Boolean = true,
    val aboutInfo: AboutInfo? = null
)

class AboutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        loadAboutInfo()
    }

    private fun loadAboutInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulate network delay for fetching from backend/Admin panel
            delay(500)

            val mockAbout = AboutInfo(
                studioName = "Royal Studio",
                shortIntro = "[Your Short Introduction Here]",
                story = "[Your Studio Story: Describe how your studio started, your journey, and your passion for photography/cinematography.]",
                philosophy = "[Your Philosophy: Describe your approach to capturing events, your style, and what makes your work unique.]",
                experienceText = "[X Years of Experience]",
                achievements = listOf(
                    "[Achievement/Award 1 Placeholder]",
                    "[Achievement/Award 2 Placeholder]"
                ),
                teamMembers = listOf(
                    TeamMember(
                        id = "team_1",
                        name = "[Photographer Name]",
                        role = "Lead Photographer",
                        bio = "[Short biography about the team member's expertise and passion.]"
                    ),
                    TeamMember(
                        id = "team_2",
                        name = "[Cinematographer Name]",
                        role = "Lead Cinematographer",
                        bio = "[Short biography about the team member's expertise and passion.]"
                    )
                )
            )

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    aboutInfo = mockAbout
                )
            }
        }
    }
}
