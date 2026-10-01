package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.UserRole
import com.example.ui.models.BookingStatus
import com.example.ui.models.BookingUpdate
import com.example.ui.models.CustomerBookingData
import com.example.ui.models.GalleryStatus
import com.example.data.models.InvoiceDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerAreaUiState(
    // Auth State
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val isSignUpMode: Boolean = false,
    val passwordResetSent: Boolean = false,
    val passwordResetMessage: String? = null,
    
    // Customer Info
    val customerName: String = "",
    val bookingData: CustomerBookingData? = null
)

class CustomerAreaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerAreaUiState())
    val uiState: StateFlow<CustomerAreaUiState> = _uiState.asStateFlow()
    
    private val authRepo = RepositoryProvider.authRepository
    private val repository = RepositoryProvider.firebaseRepository

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepo.getAuthStateUpdates().collect { user ->
                if (user != null) {
                    val role = authRepo.getUserRole(user.uid)
                    if (role == UserRole.CUSTOMER) {
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = true,
                                customerName = user.displayName?.ifBlank { null } 
                                    ?: user.email?.substringBefore("@")?.replaceFirstChar { char -> char.uppercase() } 
                                    ?: "Customer",
                                authError = null
                            )
                        }
                        loadCustomerData(user.uid, user.email ?: "")
                    } else {
                        // Admin signed in - do not allow admin in Customer Area
                        authRepo.signOut()
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = false,
                                authError = "Administrator accounts cannot access the Customer Area. Please use the Admin Portal."
                            ) 
                        }
                    }
                } else {
                    _uiState.update { it.copy(isAuthenticated = false, bookingData = null) }
                }
            }
        }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, authError = null) }
    }

    fun authenticate(email: String, password: String, name: String = "") {
        val trimmedEmail = email.trim()
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
                    authRepo.signOut()
                    _uiState.update { 
                        it.copy(
                            isAuthenticating = false, 
                            authError = "Administrator accounts cannot access the Customer Area. Please use the Admin Portal."
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(isAuthenticating = false, authError = null) 
                    }
                    if (user != null) {
                        loadCustomerData(user.uid, user.email ?: "")
                    }
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
                    authRepo.signOut()
                    _uiState.update { 
                        it.copy(
                            isAuthenticating = false, 
                            authError = "Administrator accounts cannot access the Customer Area. Please use the Admin Portal."
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(isAuthenticating = false, authError = null) 
                    }
                    if (user != null) {
                        loadCustomerData(user.uid, user.email ?: "")
                    }
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
        val trimmed = email.trim()
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
            _uiState.update { it.copy(bookingData = null) }
            return
        }
        try {
            repository.getCustomerBookings(currentUid, currentEmail)
                .catch {
                    _uiState.update { it.copy(bookingData = null) }
                }
                .collect { bookings: List<com.example.data.models.BookingDto> ->
                // Strictly isolate customer bookings matching authenticated user's ID or email
                val matchedBooking = bookings.firstOrNull { 
                    (currentUid.isNotBlank() && it.customerId == currentUid) || 
                    (currentEmail.isNotBlank() && it.email.equals(currentEmail, ignoreCase = true)) 
                }
                
                if (matchedBooking != null) {
                    val statusEnum = when (matchedBooking.status) {
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
                    val safeUpdates = if (matchedBooking.auditTrail.isNotEmpty()) {
                        matchedBooking.auditTrail.map { audit ->
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
                                timestamp = matchedBooking.createdAt?.time ?: System.currentTimeMillis(),
                                title = "Enquiry Received",
                                message = "Thank you for submitting your enquiry. Our team is reviewing details."
                            )
                        )
                    }

                    val invoice = repository.getInvoiceByBookingId(matchedBooking.id)

                    val customerData = CustomerBookingData(
                        id = matchedBooking.id,
                        referenceId = matchedBooking.referenceId.ifBlank { matchedBooking.id },
                        eventType = matchedBooking.eventType,
                        eventDate = matchedBooking.eventDate,
                        location = matchedBooking.eventLocation,
                        selectedPackage = matchedBooking.packageId.ifBlank { "Royal Studio Package" },
                        status = statusEnum,
                        updates = safeUpdates,
                        galleryStatus = galleryStatus,
                        invoice = invoice
                    )
                    _uiState.update { it.copy(bookingData = customerData) }
                } else {
                    // When no booking records found, show clean empty state (CustomerNoBookingsScreen)
                    _uiState.update { it.copy(bookingData = null) }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(bookingData = null) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.update { CustomerAreaUiState() }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(authError = null) }
    }
}
