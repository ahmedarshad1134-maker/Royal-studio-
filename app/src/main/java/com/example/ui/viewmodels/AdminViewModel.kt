package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.UserRole
import com.example.ui.models.BookingItem
import com.example.ui.models.ReviewItem
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

data class AdminUiState(
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val errorMessage: String? = null,
    
    // Stats
    val newEnquiriesCount: Int = 0,
    val upcomingEventsCount: Int = 0,
    val confirmedBookingsCount: Int = 0,
    val completedEventsCount: Int = 0,
    val portfolioCount: Int = 0,
    val servicesCount: Int = 0,
    val packagesCount: Int = 0,
    val pendingReviewsCount: Int = 0,
    
    val currentSection: AdminSection = AdminSection.DASHBOARD
)

enum class AdminSection(val title: String) {
    DASHBOARD("Dashboard"),
    BOOKINGS("Bookings Management"),
    INVOICES("Invoices & Billing"),
    PORTFOLIO("Portfolio Management"),
    SERVICES("Services"),
    PACKAGES("Packages"),
    REVIEWS("Reviews"),
    SETTINGS("Contact & Settings")
}

class AdminViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private val repository = RepositoryProvider.firebaseRepository
    private val authRepo = RepositoryProvider.authRepository

    private var sessionJob: Job? = null
    private var dashboardStatsJob: Job? = null

    init {
        checkSession()
    }

    private fun checkSession() {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            authRepo.getAuthStateUpdates().collect { user ->
                if (user != null) {
                    val role = authRepo.getUserRole(user.uid)
                    if (role == UserRole.ADMIN) {
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = true,
                                isAuthenticating = false,
                                authError = null
                            )
                        }
                        dashboardStatsJob?.cancel()
                        dashboardStatsJob = viewModelScope.launch {
                            loadDashboardStats()
                        }
                    } else {
                        // User is signed in but not an authorized admin
                        dashboardStatsJob?.cancel()
                        dashboardStatsJob = null
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = false,
                                isAuthenticating = false
                            ) 
                        }
                    }
                } else {
                    dashboardStatsJob?.cancel()
                    dashboardStatsJob = null
                    _uiState.update { 
                        it.copy(
                            isAuthenticated = false,
                            isAuthenticating = false
                        ) 
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter both administrator email and password.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.update { it.copy(authError = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null) }
            
            if (!authRepo.isInitialized) {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false, 
                        authError = "Firebase service is not initialized. Please ensure google-services.json is configured in the app/ directory."
                    ) 
                }
                return@launch
            }

            val result = authRepo.signIn(trimmedEmail, password)
            if (result.isSuccess) {
                val user = result.getOrNull()
                val role = authRepo.getUserRole(user?.uid ?: "", forceRefresh = true)
                if (role == UserRole.ADMIN) {
                    _uiState.update { 
                        it.copy(isAuthenticated = true, isAuthenticating = false, authError = null)
                    }
                    // Relies strictly on checkSession auth-state listener to load dashboard stats
                } else {
                    // Sign out immediately - unauthorized account
                    authRepo.signOut()
                    _uiState.update { 
                        it.copy(
                            isAuthenticating = false, 
                            authError = "Access Denied: This account is not an authorized Administrator."
                        )
                    }
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false, 
                        authError = result.exceptionOrNull()?.message ?: "Login failed. Invalid credentials."
                    )
                }
            }
        }
    }

    private suspend fun loadDashboardStats() {
        if (!repository.isInitialized) return
        
        coroutineScope {
            launch {
                repository.getBookings()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Failed to load bookings: ${e.localizedMessage}") }
                    }
                    .collect { bookings ->
                        val startOfToday = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        _uiState.update { state ->
                            state.copy(
                                newEnquiriesCount = bookings.count { it.status == "NEW" },
                                confirmedBookingsCount = bookings.count { it.status == "CONFIRMED" },
                                completedEventsCount = bookings.count { it.status == "COMPLETED" },
                                upcomingEventsCount = bookings.count { it.status == "CONFIRMED" && it.eventDate >= startOfToday }
                            )
                        }
                    }
            }
            launch {
                repository.getPendingReviews()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Failed to load reviews: ${e.localizedMessage}") }
                    }
                    .collect { reviews ->
                        _uiState.update { it.copy(pendingReviewsCount = reviews.size) }
                    }
            }
            launch {
                repository.getPortfolioItems()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Failed to load portfolio: ${e.localizedMessage}") }
                    }
                    .collect { items ->
                        _uiState.update { it.copy(portfolioCount = items.size) }
                    }
            }
            launch {
                repository.getServices()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Failed to load services: ${e.localizedMessage}") }
                    }
                    .collect { services ->
                        _uiState.update { it.copy(servicesCount = services.size) }
                    }
            }
            launch {
                repository.getPackages()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Failed to load packages: ${e.localizedMessage}") }
                    }
                    .collect { packages ->
                        _uiState.update { it.copy(packagesCount = packages.size) }
                    }
            }
        }
    }
    
    fun logout() {
        dashboardStatsJob?.cancel()
        dashboardStatsJob = null
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.update { AdminUiState() }
        }
    }
    
    fun navigateTo(section: AdminSection) {
        _uiState.update { it.copy(currentSection = section) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(authError = null, errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        dashboardStatsJob?.cancel()
        sessionJob?.cancel()
    }
}
