package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.PackageItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackageUiState(
    val isLoading: Boolean = true,
    val packages: List<PackageItem> = emptyList()
)

class PackageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PackageUiState())
    val uiState: StateFlow<PackageUiState> = _uiState.asStateFlow()

    init {
        loadPackages()
    }

    private fun loadPackages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulate network delay for fetching from backend/Admin panel
            delay(800)

            val mockPackages = listOf(
                PackageItem(
                    id = "pkg_basic",
                    name = "Basic Essentials",
                    description = "Perfect for intimate gatherings and short events, providing essential coverage.",
                    price = "1,200",
                    currency = "$",
                    includedServices = listOf("Photography", "Basic Retouching", "Online Gallery"),
                    photographyHours = "4 Hours",
                    videographyHours = "None",
                    numberOfPhotographers = 1,
                    numberOfVideographers = 0,
                    albumInformation = "Not included",
                    deliveryInformation = "Digital delivery within 2 weeks",
                    optionalAddOns = listOf("Extra Hour - $150", "Highlight Video - $500"),
                    isRecommended = false
                ),
                PackageItem(
                    id = "pkg_standard",
                    name = "Standard Classic",
                    description = "Our most popular package offering comprehensive coverage for your special day.",
                    price = "2,500",
                    currency = "$",
                    includedServices = listOf("Photography", "Cinematography", "Advanced Retouching", "Online Gallery"),
                    photographyHours = "8 Hours",
                    videographyHours = "8 Hours",
                    numberOfPhotographers = 2,
                    numberOfVideographers = 1,
                    albumInformation = "20-page Standard Album",
                    deliveryInformation = "Digital delivery within 3 weeks",
                    optionalAddOns = listOf("Extra Hour - $250", "Drone Coverage - $400", "Pre-Wedding Shoot - $300"),
                    isRecommended = true
                ),
                PackageItem(
                    id = "pkg_premium",
                    name = "Premium Royal",
                    description = "The ultimate luxury experience with full-day coverage and premium deliverables.",
                    price = "4,500",
                    currency = "$",
                    includedServices = listOf("Photography", "Cinematography", "Drone Footage", "Premium Retouching", "Priority Delivery"),
                    photographyHours = "Unlimited (Full Day)",
                    videographyHours = "Unlimited (Full Day)",
                    numberOfPhotographers = 2,
                    numberOfVideographers = 2,
                    albumInformation = "40-page Premium Leather Album & 2 Parent Albums",
                    deliveryInformation = "Digital delivery within 1 week (Priority)",
                    optionalAddOns = listOf("Same Day Edit - $600", "Additional Parent Album - $250"),
                    isRecommended = false
                ),
                PackageItem(
                    id = "pkg_custom",
                    name = "Custom Tailored",
                    description = "A fully bespoke package designed exclusively around your unique event requirements.",
                    price = "Custom",
                    currency = "",
                    includedServices = listOf("Custom combination of all services"),
                    photographyHours = "Flexible",
                    videographyHours = "Flexible",
                    numberOfPhotographers = 1, // Minimum
                    numberOfVideographers = 0, // Flexible
                    albumInformation = "A La Carte",
                    deliveryInformation = "Flexible schedule",
                    optionalAddOns = listOf("All add-ons available"),
                    isRecommended = false
                )
            )

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    packages = mockPackages
                )
            }
        }
    }

    fun getPackageById(id: String): PackageItem? {
        return _uiState.value.packages.find { it.id == id }
    }
}
