package com.example.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.ContactMessageDto
import com.example.data.repository.FirebaseRepository
import com.example.data.repository.RepositoryProvider
import com.example.ui.models.ContactInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class ContactUiState(
    val isLoading: Boolean = true,
    val contactInfo: ContactInfo? = null,
    
    // Form fields
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val message: String = "",
    
    val errors: Map<String, String> = emptyMap(),
    
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submissionError: String? = null
)

class ContactViewModel(
    private val repository: FirebaseRepository = RepositoryProvider.firebaseRepository
) : ViewModel() {
    constructor() : this(RepositoryProvider.firebaseRepository)

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        loadContactInfo()
    }

    private fun loadContactInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            var studioContact: ContactInfo? = null
            if (repository.isInitialized) {
                try {
                    val settings = repository.getStudioSettings()
                    if (settings != null) {
                        studioContact = ContactInfo(
                            phone = settings.phone.ifBlank { "+916289172657" },
                            whatsapp = settings.whatsapp.ifBlank { "+916289172657" },
                            address = settings.address.ifBlank { "F-55/A Battikal 2nd lean" },
                            instagramUrl = settings.socialLinks["Instagram"] ?: "https://instagram.com/royalstudio",
                            facebookUrl = settings.socialLinks["Facebook"] ?: "https://facebook.com/royalstudio",
                            mapUrl = "https://maps.google.com"
                        )
                    }
                } catch (e: Exception) {
                    Log.w("ContactViewModel", "Could not load studio settings from Firestore", e)
                }
            }

            if (studioContact == null) {
                studioContact = ContactInfo(
                    phone = "+916289172657",
                    whatsapp = "+916289172657",
                    address = "F-55/A Battikal 2nd lean",
                    instagramUrl = "https://instagram.com/royalstudio",
                    facebookUrl = "https://facebook.com/royalstudio",
                    mapUrl = "https://maps.google.com"
                )
            }

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    contactInfo = studioContact
                )
            }
        }
    }

    fun updateForm(name: String, phone: String, email: String, message: String) {
        _uiState.update { 
            it.copy(
                name = name,
                phone = phone,
                email = email,
                message = message,
                errors = emptyMap(), // Clear errors on typing
                submissionError = null // Clear submission error on typing
            ) 
        }
    }

    fun submitContactForm() {
        val currentState = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (currentState.name.isBlank()) errors["name"] = "Name is required."
        if (currentState.phone.isBlank()) errors["phone"] = "Phone is required."
        if (currentState.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            errors["email"] = "Please enter a valid email."
        }
        if (currentState.message.isBlank()) errors["message"] = "Message is required."

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submissionError = null) }
            
            try {
                val messageDto = ContactMessageDto(
                    name = currentState.name.trim(),
                    phone = currentState.phone.trim(),
                    email = currentState.email.trim(),
                    message = currentState.message.trim(),
                    createdAt = Date()
                )
                repository.createContactMessage(messageDto)
                
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        submissionError = null,
                        // Reset form
                        name = "",
                        phone = "",
                        email = "",
                        message = "",
                        errors = emptyMap()
                    )
                }
                
                // Auto hide success message
                delay(4000)
                _uiState.update { it.copy(submissionSuccess = false) }
            } catch (e: Exception) {
                Log.e("ContactViewModel", "Error submitting contact form", e)
                _uiState.update { 
                    it.copy(
                        isSubmitting = false,
                        submissionError = e.localizedMessage ?: "Failed to submit message. Please try again."
                    )
                }
            }
        }
    }
}
