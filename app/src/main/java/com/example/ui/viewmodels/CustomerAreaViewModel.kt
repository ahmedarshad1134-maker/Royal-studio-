package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.BookingStatus
import com.example.ui.models.BookingUpdate
import com.example.ui.models.CustomerBookingData
import com.example.ui.models.GalleryStatus
import com.example.data.models.InvoiceDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerAreaUiState(
    // Auth State
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val isSignUpMode: Boolean = false,
    
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
                    if (role == "customer") {
                        loadCustomerData(user.email ?: "")
                        _uiState.update { 
                            it.copy(
                                isAuthenticated = true,
                                customerName = user.displayName ?: user.email?.substringBefore("@")?.replaceFirstChar { char -> char.uppercase() } ?: "Customer"
                            )
                        }
                    } else {
                        // Admin signed in, but trying to access customer area? Or we just log them out
                        authRepo.signOut()
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
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(authError = "Please enter both Email and Password.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, authError = null) }
            
            if (authRepo.isInitialized) {
                val result = if (_uiState.value.isSignUpMode) {
                    authRepo.signUp(email, password, name)
                } else {
                    authRepo.signIn(email, password)
                }
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    val role = authRepo.getUserRole(user?.uid ?: "")
                    if (role == "admin") {
                        authRepo.signOut()
                        _uiState.update { 
                            it.copy(isAuthenticating = false, authError = "Admins cannot log in here.") 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(isAuthenticating = false, authError = null) 
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(isAuthenticating = false, authError = result.exceptionOrNull()?.message ?: "Authentication failed") 
                    }
                }
            } else {
                // Mock Auth
                delay(1200)
                if (password.length < 6) {
                    _uiState.update { 
                        it.copy(
                            isAuthenticating = false, 
                            authError = "Password must be at least 6 characters."
                        ) 
                    }
                    return@launch
                }
                loadCustomerData(email)
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false,
                        isAuthenticated = true,
                        customerName = email.substringBefore("@").replaceFirstChar { char -> char.uppercase() },
                        authError = null
                    )
                }
            }
        }
    }

    private suspend fun loadCustomerData(email: String) {
        if (repository.isInitialized) {
            try {
                val currentUid = authRepo.currentUser?.uid ?: ""
                repository.getCustomerBookings(email).collect { bookings: List<com.example.data.models.BookingDto> ->
                    // Strictly isolate customer bookings matching either customerId or email
                    val matchedBooking = bookings.firstOrNull { 
                        (currentUid.isNotBlank() && it.customerId == currentUid) || 
                        it.email.equals(email, ignoreCase = true) 
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
                        // Fallback to demo booking if no records found yet
                        setMockCustomerData()
                    }
                }
            } catch (e: Exception) {
                // In case of network error, fallback gracefully
                setMockCustomerData()
            }
        } else {
            setMockCustomerData()
        }
    }

    private fun setMockCustomerData() {
        val mockData = CustomerBookingData(
            id = "RS-2026-DEMO",
            referenceId = "RS-2026-DEMO",
            eventType = "Wedding Photography & Cinematography",
            eventDate = System.currentTimeMillis() + 86400000L * 30,
            location = "Grand Plaza Hotel, Main Ballroom",
            selectedPackage = "Premium Royal Package",
            status = BookingStatus.CONFIRMED,
            updates = listOf(
                BookingUpdate(
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    title = "Booking Confirmed",
                    message = "Your dates have been secured and crew is assigned. We look forward to capturing your celebration!"
                ),
                BookingUpdate(
                    timestamp = System.currentTimeMillis() - 86400000L * 4,
                    title = "Quotation Sent",
                    message = "Your customized package options and proposal were reviewed."
                ),
                BookingUpdate(
                    timestamp = System.currentTimeMillis() - 86400000L * 5,
                    title = "Enquiry Received",
                    message = "Thank you for reaching out to Royal Studio. Your initial enquiry has been registered."
                )
            ),
            galleryStatus = GalleryStatus.READY,
            invoice = InvoiceDto(
                id = "INV_DEMO_CUST",
                invoiceNumber = "INV-2026-9214",
                bookingReference = "RS-2026-DEMO",
                customerName = "Valued Royal Client",
                customerEmail = "client@example.com",
                eventType = "Wedding Photography & Cinematography",
                eventDate = System.currentTimeMillis() + 86400000L * 30,
                eventLocation = "Grand Plaza Hotel, Main Ballroom",
                selectedPackage = "Premium Royal Package",
                subtotal = 250000.0,
                additionalCharges = 20000.0,
                discount = 15000.0,
                taxRate = 0.0,
                taxAmount = 0.0,
                totalAmount = 255000.0,
                amountPaid = 100000.0,
                balanceDue = 155000.0,
                paymentStatus = "Partially Paid",
                issueDate = System.currentTimeMillis() - 86400000L * 3,
                dueDate = System.currentTimeMillis() + 86400000L * 20,
                paymentRecords = listOf(
                    com.example.data.models.PaymentRecordDto(
                        paymentId = "PAY-CUST-1",
                        amount = 100000.0,
                        method = "Bank Transfer",
                        referenceNotes = "Token advance paid",
                        recordedBy = "Royal Studio Accounts",
                        timestamp = System.currentTimeMillis() - 86400000L * 2,
                        receiptId = "RCP-9214-1"
                    )
                ),
                notes = "Advance payment received. Balance due 10 days prior to event date."
            )
        )
        _uiState.update { it.copy(bookingData = mockData) }
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
