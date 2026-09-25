package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.BookingItem
import com.example.ui.models.BookingStatus
import com.example.ui.models.BookingAuditEntry
import com.example.data.models.BookingDto
import com.example.data.models.AuditEntryDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar
import kotlin.random.Random

data class BookingUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 6,
    
    val eventType: String = "",
    val eventDateMillis: Long? = null,
    val location: String = "",
    val selectedOffering: String = "", // Represents selected service or package
    
    val customerName: String = "",
    val phone: String = "",
    val email: String = "",
    val message: String = "",
    
    val errors: Map<String, String> = emptyMap(),
    
    val isSubmitting: Boolean = false,
    val submissionError: String? = null,
    val submittedBooking: BookingItem? = null
)

class BookingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    private val repository = RepositoryProvider.firebaseRepository
    private val authRepo = RepositoryProvider.authRepository

    private fun generateReferenceId(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "RS-$year-$code"
    }

    fun updateEventType(type: String) {
        _uiState.update { it.copy(eventType = type, errors = it.errors - "eventType") }
    }

    fun updateEventDate(dateMillis: Long?) {
        _uiState.update { it.copy(eventDateMillis = dateMillis, errors = it.errors - "eventDate") }
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(location = location, errors = it.errors - "location") }
    }

    fun updateSelectedOffering(offering: String) {
        _uiState.update { it.copy(selectedOffering = offering, errors = it.errors - "offering") }
    }

    fun updateCustomerInfo(name: String, phone: String, email: String, message: String) {
        _uiState.update { 
            it.copy(
                customerName = name,
                phone = phone,
                email = email,
                message = message,
                errors = emptyMap()
            ) 
        }
    }

    fun nextStep() {
        val currentState = _uiState.value
        val errors = mutableMapOf<String, String>()

        when (currentState.currentStep) {
            1 -> if (currentState.eventType.isBlank()) errors["eventType"] = "Please select an event type."
            2 -> {
                if (currentState.eventDateMillis == null) {
                    errors["eventDate"] = "Please select a date."
                } else if (currentState.eventDateMillis < System.currentTimeMillis() - 86400000L) {
                    errors["eventDate"] = "Event date must be today or in the future."
                }
            }
            3 -> {
                val loc = currentState.location.trim()
                if (loc.isBlank()) {
                    errors["location"] = "Please enter an event location."
                } else if (loc.length > 200) {
                    errors["location"] = "Location cannot exceed 200 characters."
                }
            }
            4 -> if (currentState.selectedOffering.isBlank()) errors["offering"] = "Please select a service or package."
            5 -> {
                val name = currentState.customerName.trim()
                val phone = currentState.phone.trim()
                val email = currentState.email.trim()

                if (name.isBlank()) {
                    errors["customerName"] = "Name is required."
                } else if (name.length > 100) {
                    errors["customerName"] = "Name cannot exceed 100 characters."
                }

                if (phone.isBlank()) {
                    errors["phone"] = "Phone number is required."
                } else if (phone.length < 7 || phone.length > 20 || !phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' || it == '(' || it == ')' }) {
                    errors["phone"] = "Please enter a valid phone number (digits, +, -)."
                }
                
                if (email.isNotBlank()) {
                    if (email.length > 100 || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errors["email"] = "Please enter a valid email address."
                    }
                }

                if (currentState.message.length > 1000) {
                    errors["message"] = "Message must not exceed 1000 characters."
                }
            }
        }

        if (errors.isEmpty()) {
            if (currentState.currentStep < currentState.totalSteps) {
                _uiState.update { it.copy(currentStep = it.currentStep + 1, errors = emptyMap()) }
            }
        } else {
            _uiState.update { it.copy(errors = errors) }
        }
    }

    fun previousStep() {
        val currentState = _uiState.value
        if (currentState.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errors = emptyMap()) }
        }
    }

    fun submitEnquiry() {
        val currentState = _uiState.value
        if (currentState.isSubmitting || currentState.submittedBooking != null) return // Prevent double-submit
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submissionError = null) }
            
            val refId = generateReferenceId()
            val currentUserId = authRepo.currentUser?.uid ?: ""
            val currentUserEmail = authRepo.currentUser?.email ?: currentState.email.trim()

            val initialAudit = BookingAuditEntry(
                status = "NEW",
                updatedBy = if (currentUserId.isNotBlank()) "Customer ($currentUserId)" else "Client (${currentState.customerName})",
                timestamp = System.currentTimeMillis(),
                note = "Enquiry submitted via mobile app"
            )

            val newBooking = BookingItem(
                id = refId,
                referenceId = refId,
                customerId = currentUserId,
                customerName = currentState.customerName.trim(),
                phone = currentState.phone.trim(),
                email = currentUserEmail.takeIf { it.isNotBlank() },
                eventType = currentState.eventType,
                eventDate = currentState.eventDateMillis ?: 0L,
                location = currentState.location.trim(),
                packageId = currentState.selectedOffering,
                serviceId = null,
                message = currentState.message.takeIf { it.isNotBlank() }?.trim(),
                status = BookingStatus.NEW,
                adminNotes = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                updatedBy = "Customer",
                auditTrail = listOf(initialAudit)
            )

            if (repository.isInitialized) {
                try {
                    val dto = BookingDto(
                        id = refId,
                        referenceId = refId,
                        customerId = currentUserId,
                        customerName = currentState.customerName.trim(),
                        phone = currentState.phone.trim(),
                        email = currentUserEmail,
                        eventType = currentState.eventType,
                        eventDate = currentState.eventDateMillis ?: 0L,
                        eventLocation = currentState.location.trim(),
                        serviceId = "",
                        packageId = currentState.selectedOffering,
                        message = currentState.message.trim(),
                        status = "NEW",
                        createdAt = Date(),
                        updatedAt = Date(),
                        adminNotes = "",
                        updatedBy = "Customer",
                        auditTrail = listOf(
                            AuditEntryDto(
                                status = "NEW",
                                updatedBy = "Customer",
                                timestamp = System.currentTimeMillis(),
                                note = "Enquiry submitted"
                            )
                        )
                    )
                    repository.createBooking(dto)
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            submittedBooking = newBooking,
                            submissionError = null
                        ) 
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            submissionError = "Unable to connect or submit enquiry: ${e.localizedMessage ?: "Network error. Please try again."}"
                        ) 
                    }
                }
            } else {
                delay(1200)
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        submittedBooking = newBooking,
                        submissionError = null
                    ) 
                }
            }
        }
    }

    fun resetBooking() {
        _uiState.value = BookingUiState()
    }
}

