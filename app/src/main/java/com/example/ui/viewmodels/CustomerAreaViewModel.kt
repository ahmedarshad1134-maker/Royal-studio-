package com.example.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.BookingDto
import com.example.data.models.InvoiceDto
import com.example.data.models.UserRole
import com.example.data.repository.RepositoryProvider
import com.example.ui.models.BookingStatus
import com.example.ui.models.BookingUpdate
import com.example.ui.models.CustomerBookingData
import com.example.ui.models.GalleryStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerAreaUiState(
    // Auth State
    val isAuthenticated: Boolean = false,
    val isAdminSession: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val isSignUpMode: Boolean = false,
    val passwordResetSent: Boolean = false,
    val passwordResetMessage: String? = null,
    
    // Customer Info
    val customerName: String = "",
    val bookings: List<CustomerBookingData> = emptyList(),
    val selectedBooking: CustomerBookingData? = null
) {
    val bookingData: CustomerBookingData?
        get() = selectedBooking ?: bookings.firstOrNull()
}

class CustomerAreaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerAreaUiState())
    val uiState: StateFlow<CustomerAreaUiState> = _uiState.asStateFlow()
    
    private val authRepo = RepositoryProvider.authRepository
    private val repository = RepositoryProvider.firebaseRepository

    private var sessionJob: Job? = null
    private var customerDataJob: Job? = null

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
                        customerDataJob?.cancel()
                        customerDataJob = null
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = false,
                                isAdminSession = true,
                                customerName = user.displayName?.ifBlank { null } 
                                    ?: user.email?.substringBefore("@") 
                                    ?: "Administrator",
                                bookings = emptyList(),
                                selectedBooking = null,
                                authError = null
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = true,
                                isAdminSession = false,
                                customerName = user.displayName?.ifBlank { null } 
                                    ?: user.email?.substringBefore("@")?.replaceFirstChar { char -> char.uppercase() } 
                                    ?: "Customer",
                                authError = null
                            )
                        }
                        customerDataJob?.cancel()
                        customerDataJob = viewModelScope.launch {
                            loadCustomerData(user.uid, user.email ?: "")
                        }
                    }
                } else {
                    customerDataJob?.cancel()
                    customerDataJob = null
                    _uiState.update { 
                        it.copy(
                            isAuthenticated = false, 
                            isAdminSession = false,
                            bookings = emptyList(), 
                            selectedBooking = null
                        ) 
                    }
                }
            }
        }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, authError = null) }
    }

    fun selectBooking(booking: CustomerBookingData) {
        _uiState.update { it.copy(selectedBooking = booking) }
    }

    fun clearSelectedBooking() {
        _uiState.update { it.copy(selectedBooking = null) }
    }

    fun authenticate(email: String, password: String, name: String = "") {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter both Email and Password.") }
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
            
            val result = if (_uiState.value.isSignUpMode) {
                authRepo.signUp(trimmedEmail, password, name)
            } else {
                authRepo.signIn(trimmedEmail, password)
            }
            
            if (result.isSuccess) {
                val user = result.getOrNull()
                val role = authRepo.getUserRole(user?.uid ?: "")
                if (role == UserRole.ADMIN) {
                    _uiState.update { 
                        it.copy(
                            isAuthenticating = false, 
                            isAdminSession = true,
                            authError = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(isAuthenticating = false, isAdminSession = false, authError = null) 
                    }
                    // Relies strictly on checkSession auth-state listener to load customer data
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false, 
                        authError = result.exceptionOrNull()?.message ?: "Authentication failed. Please verify credentials."
                    ) 
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null) }
            val result = authRepo.signInWithGoogle(idToken)
            if (result.isSuccess) {
                val user = result.getOrNull()
                val role = authRepo.getUserRole(user?.uid ?: "")
                if (role == UserRole.ADMIN) {
                    _uiState.update { 
                        it.copy(
                            isAuthenticating = false, 
                            isAdminSession = true,
                            authError = null
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(isAuthenticating = false, isAdminSession = false, authError = null) 
                    }
                    // Relies strictly on checkSession auth-state listener to load customer data
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false, 
                        authError = result.exceptionOrNull()?.message ?: "Google authentication failed. Please try again."
                    ) 
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val trimmed = email.trim().lowercase()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter your email address to reset password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null, passwordResetMessage = null) }
            val result = authRepo.sendPasswordResetEmail(trimmed)
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false,
                        passwordResetSent = true,
                        passwordResetMessage = "Password reset email sent to $trimmed. Please check your inbox.",
                        authError = null
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false,
                        passwordResetSent = false,
                        authError = result.exceptionOrNull()?.message ?: "Failed to send password reset email."
                    ) 
                }
            }
        }
    }

    fun setAuthenticating(isAuth: Boolean) {
        _uiState.update { it.copy(isAuthenticating = isAuth) }
    }

    fun setAuthError(error: String?) {
        _uiState.update { it.copy(authError = error, isAuthenticating = false) }
    }

    fun clearPasswordResetMessage() {
        _uiState.update { it.copy(passwordResetMessage = null, passwordResetSent = false) }
    }

    private suspend fun loadCustomerData(currentUid: String, currentEmail: String) {
        if (!repository.isInitialized) {
            _uiState.update { it.copy(bookings = emptyList(), selectedBooking = null) }
            return
        }
        val cleanEmail = currentEmail.trim().lowercase()
        try {
            // FIX 1 & FIX 2 & FIX 3:
            // Combine customer bookings and customer invoices (queries conforming to Firestore rules)
            combine(
                repository.getCustomerBookings(customerId = currentUid, email = cleanEmail),
                repository.getCustomerInvoices(email = cleanEmail, customerId = currentUid)
            ) { bookingsDto: List<BookingDto>, invoicesDto: List<InvoiceDto> ->
                // Sort by createdAt descending
                val sortedBookings = bookingsDto.sortedByDescending { it.createdAt?.time ?: 0L }

                val customerBookings = sortedBookings.map { booking ->
                    val statusEnum = when (booking.status) {
                        "CONTACTED" -> BookingStatus.CONTACTED
                        "QUOTATION_SENT" -> BookingStatus.QUOTATION_SENT
                        "CONFIRMED" -> BookingStatus.CONFIRMED
                        "COMPLETED" -> BookingStatus.COMPLETED
                        "CANCELLED" -> BookingStatus.CANCELLED
                        else -> BookingStatus.NEW
                    }

                    val galleryStatus = when (statusEnum) {
                        BookingStatus.COMPLETED -> GalleryStatus.READY
                        BookingStatus.CONFIRMED -> GalleryStatus.PROCESSING
                        else -> GalleryStatus.UNAVAILABLE
                    }

                    // Generate customer-safe updates from auditTrail (NEVER exposing internal adminNotes)
                    val customerAudits = booking.auditTrail.filter { !it.adminOnly }
                    val safeUpdates = if (customerAudits.isNotEmpty()) {
                        customerAudits.map { audit ->
                            val title = when (audit.status) {
                                "NEW" -> "Enquiry Received"
                                "CONTACTED" -> "Team Contacted You"
                                "QUOTATION_SENT" -> "Quotation & Proposal Sent"
                                "CONFIRMED" -> "Booking Confirmed"
                                "COMPLETED" -> "Event Completed & Media Processing"
                                "CANCELLED" -> "Enquiry / Booking Cancelled"
                                else -> "Status Updated"
                            }
                            val customerSafeMsg = when (audit.status) {
                                "NEW" -> "Thank you for submitting your enquiry. Royal Studio will review your date and requirements."
                                "CONTACTED" -> "Our event director has initiated contact to discuss options and itinerary details."
                                "QUOTATION_SENT" -> "A tailored quote has been prepared and transmitted for your review."
                                "CONFIRMED" -> "Your booking is officially confirmed! Dates and crew have been locked in."
                                "COMPLETED" -> "Event coverage wrapped up. Our editing studio is curating your photographs and film."
                                "CANCELLED" -> "This booking or enquiry has been closed or cancelled."
                                else -> "Booking status changed to ${audit.status}"
                            }
                            BookingUpdate(
                                timestamp = if (audit.timestamp > 0) audit.timestamp else System.currentTimeMillis(),
                                title = title,
                                message = customerSafeMsg
                            )
                        }.sortedByDescending { it.timestamp }
                    } else {
                        listOf(
                            BookingUpdate(
                                timestamp = booking.createdAt?.time ?: System.currentTimeMillis(),
                                title = "Enquiry Received",
                                message = "Thank you for submitting your enquiry. Our team is reviewing details."
                            )
                        )
                    }

                    // FIX 1: Show the invoice that matches the booking's id
                    val matchedInvoice = invoicesDto.firstOrNull { it.bookingId == booking.id }

                    CustomerBookingData(
                        id = booking.id,
                        referenceId = booking.referenceId.ifBlank { booking.id },
                        eventType = booking.eventType,
                        eventDate = booking.eventDate,
                        location = booking.eventLocation,
                        selectedPackage = booking.packageId.ifBlank { "Royal Studio Package" },
                        status = statusEnum,
                        updates = safeUpdates,
                        galleryStatus = galleryStatus,
                        invoice = matchedInvoice
                    )
                }

                val currentSelectedId = _uiState.value.selectedBooking?.id
                val updatedSelected = customerBookings.find { it.id == currentSelectedId }

                _uiState.update { 
                    it.copy(
                        bookings = customerBookings,
                        selectedBooking = updatedSelected
                    ) 
                }
            }.catch { e ->
                Log.e("CustomerAreaViewModel", "Error in combined bookings and invoices stream", e)
                _uiState.update { it.copy(bookings = emptyList(), selectedBooking = null) }
            }.collect()
        } catch (e: Exception) {
            Log.e("CustomerAreaViewModel", "Error loading customer data", e)
            _uiState.update { it.copy(bookings = emptyList(), selectedBooking = null) }
        }
    }

    fun logout() {
        customerDataJob?.cancel()
        customerDataJob = null
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.update { CustomerAreaUiState() }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(authError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        customerDataJob?.cancel()
        sessionJob?.cancel()
    }
}
