package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.models.ServiceDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminServicesUiState(
    val services: List<ServiceDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AdminServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminServicesUiState())
    val uiState: StateFlow<AdminServicesUiState> = _uiState.asStateFlow()

    private val dbRepo = RepositoryProvider.firebaseRepository

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (dbRepo.isInitialized) {
                dbRepo.getServices()
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { services ->
                        _uiState.update { it.copy(services = services, isLoading = false) }
                    }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveService(id: String, name: String, description: String, imageUrl: String, enabled: Boolean) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Service name cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                if (id.isBlank()) {
                    dbRepo.createService(ServiceDto(name = name.trim(), description = description.trim(), imageUrl = imageUrl.trim(), enabled = enabled))
                    _uiState.update { it.copy(isSaving = false, message = "Service added successfully") }
                } else {
                    dbRepo.updateService(ServiceDto(id = id, name = name.trim(), description = description.trim(), imageUrl = imageUrl.trim(), enabled = enabled))
                    _uiState.update { it.copy(isSaving = false, message = "Service updated successfully") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save service") }
            }
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            try {
                dbRepo.deleteService(id)
                _uiState.update { it.copy(message = "Service deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete service") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

@Composable
fun AdminServicesSection(
    viewModel: AdminServicesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ServiceDto?>(null) }
    var serviceToDelete by remember { mutableStateOf<ServiceDto?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Services (${uiState.services.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    serviceToEdit = null
                    showDialog = true
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Service")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Service")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        uiState.message?.let {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Text(text = it, modifier = Modifier.padding(12.dp), color = Color(0xFF2E7D32))
            }
        }

        uiState.error?.let {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(text = it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No services configured yet. Tap 'Add Service' to create one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.services, key = { it.id }) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (service.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = service.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    serviceToEdit = service
                                    showDialog = true
                                }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Service", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { serviceToDelete = service }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Service", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ServiceFormDialog(
            initialService = serviceToEdit,
            isSaving = uiState.isSaving,
            onDismiss = { showDialog = false },
            onSave = { id, name, desc, img, enabled ->
                viewModel.saveService(id, name, desc, img, enabled)
                showDialog = false
            }
        )
    }

    serviceToDelete?.let { service ->
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = { Text("Delete Service") },
            text = { Text("Are you sure you want to delete '${service.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteService(service.id)
                        serviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ServiceFormDialog(
    initialService: ServiceDto?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (id: String, name: String, description: String, imageUrl: String, enabled: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialService?.name ?: "") }
    var description by remember { mutableStateOf(initialService?.description ?: "") }
    var imageUrl by remember { mutableStateOf(initialService?.imageUrl ?: "") }
    var enabled by remember { mutableStateOf(initialService?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialService == null) "Add Service" else "Edit Service") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Service Enabled")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(initialService?.id ?: "", name, description, imageUrl, enabled) },
                enabled = !isSaving && name.isNotBlank()
            ) {
                Text(if (initialService == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
