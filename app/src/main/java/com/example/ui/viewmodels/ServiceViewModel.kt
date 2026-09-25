package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ui.models.ServiceItem
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull

data class ServiceUiState(
    val isLoading: Boolean = true,
    val services: List<ServiceItem> = emptyList(),
    val error: String? = null
)

class ServiceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()
    private val repository = RepositoryProvider.firebaseRepository

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            if (repository.isInitialized) {
                try {
                    repository.getServices()
                        .catch { e -> 
                            _uiState.update { it.copy(isLoading = false, error = e.message) }
                            fallbackToMockData()
                        }
                        .collect { dtos ->
                            if (dtos.isEmpty()) {
                                fallbackToMockData()
                            } else {
                                val mapped = dtos.map { dto ->
                                    ServiceItem(
                                        id = dto.id,
                                        name = dto.name,
                                        shortDescription = dto.description,
                                        description = dto.description,
                                        coverImageResId = R.drawable.service_wedding_1789061903404, // Placeholder for remote images
                                        features = emptyList(),
                                        startingPrice = "",
                                        suitableEventTypes = emptyList()
                                    )
                                }
                                _uiState.update { it.copy(isLoading = false, services = mapped) }
                            }
                        }
                } catch (e: Exception) {
                    fallbackToMockData()
                }
            } else {
                fallbackToMockData()
            }
        }
    }

    private suspend fun fallbackToMockData() {
        delay(800)
        val mockServices = listOf(
            ServiceItem(
                id = "wedding_photo",
                name = "Wedding Photography",
                shortDescription = "Timeless and elegant photography for your special day.",
                description = "Our wedding photography service focuses on capturing the authentic emotions, grand moments, and subtle details of your wedding day. We use a blend of photojournalistic and fine-art styles to create a timeless gallery you'll cherish forever.",
                coverImageResId = R.drawable.hero_wedding_cinematic_1789061882938,
                features = listOf("Full day coverage", "High-resolution digital gallery", "Pre-wedding consultation", "Second shooter included"),
                startingPrice = "$1,500",
                suitableEventTypes = listOf("Weddings", "Elopements")
            ),
            ServiceItem(
                id = "wedding_video",
                name = "Wedding Cinematography",
                shortDescription = "Cinematic storytelling that brings your wedding day to life.",
                description = "We craft breathtaking cinematic films that tell the unique story of your love. From the morning preparations to the final dance, our team uses industry-leading equipment to ensure your wedding film looks like a Hollywood masterpiece.",
                coverImageResId = R.drawable.service_wedding_1789061903404,
                features = listOf("Cinematic highlight film", "Drone footage (location permitting)", "Full ceremony edit", "Professional audio recording"),
                startingPrice = "$2,000",
                suitableEventTypes = listOf("Weddings", "Elopements")
            ),
            ServiceItem(
                id = "pre_wedding",
                name = "Pre-Wedding Photography",
                shortDescription = "Romantic portrait sessions before you say 'I do'.",
                description = "A dedicated portrait session to celebrate your engagement and get comfortable in front of the camera before your big day. Choose a location that is meaningful to you, and we'll capture your natural connection.",
                coverImageResId = R.drawable.portfolio_sample_1789061922601,
                features = listOf("2-hour location shoot", "Outfit changes", "Online gallery", "Save-the-date card design"),
                startingPrice = "$500",
                suitableEventTypes = listOf("Engagements", "Pre-Weddings")
            ),
            ServiceItem(
                id = "birthday",
                name = "Birthday Photography",
                shortDescription = "Capture the joy and celebration of your milestone birthdays.",
                description = "Whether it's a sweet sixteen, a 50th milestone, or a grand birthday gala, our photography team ensures every smile, toast, and surprise is documented with elegance and style.",
                coverImageResId = R.drawable.portfolio_birthday_1789062228988,
                features = listOf("Event coverage", "Candid moments", "Group portraits", "Quick turnaround"),
                startingPrice = "$400",
                suitableEventTypes = listOf("Birthdays", "Milestone Events")
            ),
            ServiceItem(
                id = "baby_family",
                name = "Baby/Family Photography",
                shortDescription = "Beautiful family portraits and newborn captures.",
                description = "Time flies, but family portraits last forever. We offer studio or location shoots tailored to capture the warmth, joy, and bond of your family, from newborns to multi-generational gatherings.",
                coverImageResId = R.drawable.portfolio_baby_1789062242119,
                features = listOf("Studio or outdoor session", "Props included for newborns", "Retouched portraits", "Print release"),
                startingPrice = "$350",
                suitableEventTypes = listOf("Maternity", "Newborns", "Family Gatherings")
            ),
            ServiceItem(
                id = "event_coverage",
                name = "Event Photography",
                shortDescription = "Professional coverage for corporate and private events.",
                description = "Comprehensive photography services for corporate galas, product launches, award ceremonies, and private parties. We deliver high-quality images that perfectly represent the atmosphere and success of your event.",
                coverImageResId = R.drawable.service_event_1789061936720,
                features = listOf("Discreet coverage", "Candid and staged shots", "Fast delivery for PR", "Dedicated event manager"),
                startingPrice = "$600",
                suitableEventTypes = listOf("Corporate Events", "Galas", "Private Parties")
            ),
            ServiceItem(
                id = "album_design",
                name = "Album Design",
                shortDescription = "Premium, handcrafted albums to showcase your memories.",
                description = "Turn your digital gallery into a tangible heirloom. Our design team curates and layouts your favorite images into a premium, handcrafted leather or linen album printed on archival fine-art paper.",
                coverImageResId = R.drawable.hero_wedding_cinematic_1789061882938,
                features = listOf("Custom layouts", "Premium cover materials", "Archival paper", "Multiple revision rounds"),
                startingPrice = "$300",
                suitableEventTypes = listOf("All Events")
            )
        )
        _uiState.update { 
            it.copy(
                isLoading = false,
                services = mockServices
            )
        }
    }

    fun getServiceById(id: String): ServiceItem? {
        return _uiState.value.services.find { it.id == id }
    }
}
