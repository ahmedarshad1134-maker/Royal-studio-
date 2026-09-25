package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.ContactInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val submissionSuccess: Boolean = false
)

class ContactViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        loadContactInfo()
    }

    private fun loadContactInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulate network delay for fetching from backend/Admin panel
            delay(500)

            val mockContact = ContactInfo(
                phone = "[+00 000 000 0000]",
                whatsapp = "[+00 000 000 0000]",
                email = "[contact@yourstudio.com]",
                address = "[Your Studio Address, City, Country]",
                businessHours = "[Mon-Fri: 9:00 AM - 6:00 PM]",
                instagramUrl = "[https://instagram.com/yourprofile]",
                facebookUrl = "[https://facebook.com/yourprofile]",
                mapUrl = "[Google Maps Link]"
            )

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    contactInfo = mockContact
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
                errors = emptyMap() // Clear errors on typing
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
            _uiState.update { it.copy(isSubmitting = true) }
            
            // Simulate API call
            delay(1500)
            
            _uiState.update { 
                it.copy(
                    isSubmitting = false,
                    submissionSuccess = true,
                    // Reset form
                    name = "",
                    phone = "",
                    email = "",
                    message = ""
                )
            }
            
            // Auto hide success message
            delay(4000)
            _uiState.update { it.copy(submissionSuccess = false) }
        }
    }
}
