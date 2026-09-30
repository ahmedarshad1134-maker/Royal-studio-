package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.StudioSettingsDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminSettingsUiState(
    val studioName: String = "",
    val tagline: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val address: String = "",
    val businessHours: String = "",
    val aboutText: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AdminSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    private val dbRepo = RepositoryProvider.firebaseRepository

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            if (dbRepo.isInitialized) {
                try {
                    val settings = dbRepo.getStudioSettings()
                    if (settings != null) {
                        _uiState.update {
                            it.copy(
                                studioName = settings.studioName,
                                tagline = settings.tagline,
                                phone = settings.phone,
                                whatsapp = settings.whatsapp,
                                email = settings.email,
                                address = settings.address,
                                businessHours = settings.businessHours,
                                aboutText = settings.aboutText,
                                isLoading = false
                            )
                        }
                    } else {
                        // Fallback defaults
                        _uiState.update {
                            it.copy(
                                studioName = "Royal Studio",
                                phone = "+916289172657",
                                whatsapp = "+916289172657",
                                address = "F-55/A Battikal 2nd lean",
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load settings") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateField(
        studioName: String? = null,
        tagline: String? = null,
        phone: String? = null,
        whatsapp: String? = null,
        email: String? = null,
        address: String? = null,
        businessHours: String? = null,
        aboutText: String? = null
    ) {
        _uiState.update {
            it.copy(
                studioName = studioName ?: it.studioName,
                tagline = tagline ?: it.tagline,
                phone = phone ?: it.phone,
                whatsapp = whatsapp ?: it.whatsapp,
                email = email ?: it.email,
                address = address ?: it.address,
                businessHours = businessHours ?: it.businessHours,
                aboutText = aboutText ?: it.aboutText
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                val current = _uiState.value
                val dto = StudioSettingsDto(
                    id = "main_settings",
                    studioName = current.studioName.trim(),
                    tagline = current.tagline.trim(),
                    phone = current.phone.trim(),
                    whatsapp = current.whatsapp.trim(),
                    email = current.email.trim(),
                    address = current.address.trim(),
                    businessHours = current.businessHours.trim(),
                    aboutText = current.aboutText.trim()
                )
                dbRepo.updateStudioSettings(dto)
                _uiState.update { it.copy(isSaving = false, message = "Studio settings saved successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save settings") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

@Composable
fun AdminSettingsSection(
    viewModel: AdminSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Studio Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { viewModel.saveSettings() },
                enabled = !uiState.isSaving && !uiState.isLoading
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (uiState.isSaving) "Saving..." else "Save Changes")
            }
        }

        uiState.message?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Text(text = it, modifier = Modifier.padding(12.dp), color = Color(0xFF2E7D32))
            }
        }

        uiState.error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(text = it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "General Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = uiState.studioName,
                        onValueChange = { viewModel.updateField(studioName = it) },
                        label = { Text("Studio Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.tagline,
                        onValueChange = { viewModel.updateField(tagline = it) },
                        label = { Text("Tagline / Slogan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.updateField(phone = it) },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.whatsapp,
                        onValueChange = { viewModel.updateField(whatsapp = it) },
                        label = { Text("WhatsApp Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateField(email = it) },
                        label = { Text("Studio Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.updateField(address = it) },
                        label = { Text("Physical Studio Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.businessHours,
                        onValueChange = { viewModel.updateField(businessHours = it) },
                        label = { Text("Operating Hours") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.aboutText,
                        onValueChange = { viewModel.updateField(aboutText = it) },
                        label = { Text("About the Studio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                }
            }
        }
    }
}
