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
import com.example.data.models.PackageDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminPackagesUiState(
    val packages: List<PackageDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AdminPackagesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminPackagesUiState())
    val uiState: StateFlow<AdminPackagesUiState> = _uiState.asStateFlow()

    private val dbRepo = RepositoryProvider.firebaseRepository

    init {
        loadPackages()
    }

    private fun loadPackages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (dbRepo.isInitialized) {
                dbRepo.getPackages()
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { packages ->
                        _uiState.update { it.copy(packages = packages, isLoading = false) }
                    }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun savePackage(id: String, name: String, description: String, price: String, featuresStr: String, enabled: Boolean, featured: Boolean) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Package name cannot be empty") }
            return
        }
        val features = featuresStr.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                if (id.isBlank()) {
                    dbRepo.createPackage(
                        PackageDto(
                            name = name.trim(),
                            description = description.trim(),
                            price = price.trim(),
                            features = features,
                            enabled = enabled,
                            featured = featured
                        )
                    )
                    _uiState.update { it.copy(isSaving = false, message = "Package added successfully") }
                } else {
                    dbRepo.updatePackage(
                        PackageDto(
                            id = id,
                            name = name.trim(),
                            description = description.trim(),
                            price = price.trim(),
                            features = features,
                            enabled = enabled,
                            featured = featured
                        )
                    )
                    _uiState.update { it.copy(isSaving = false, message = "Package updated successfully") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save package") }
            }
        }
    }

    fun deletePackage(id: String) {
        viewModelScope.launch {
            try {
                dbRepo.deletePackage(id)
                _uiState.update { it.copy(message = "Package deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete package") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

@Composable
fun AdminPackagesSection(
    viewModel: AdminPackagesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var pkgToEdit by remember { mutableStateOf<PackageDto?>(null) }
    var pkgToDelete by remember { mutableStateOf<PackageDto?>(null) }

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
                text = "Packages (${uiState.packages.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    pkgToEdit = null
                    showDialog = true
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Package")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Package")
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
        } else if (uiState.packages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No packages configured yet. Tap 'Add Package' to create one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.packages, key = { it.id }) { pkg ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pkg.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (pkg.price.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(pkg.price) }
                                        )
                                    }
                                }
                                if (pkg.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pkg.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )
                                }
                                if (pkg.features.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${pkg.features.size} features included",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    pkgToEdit = pkg
                                    showDialog = true
                                }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Package", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { pkgToDelete = pkg }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Package", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        PackageFormDialog(
            initialPackage = pkgToEdit,
            isSaving = uiState.isSaving,
            onDismiss = { showDialog = false },
            onSave = { id, name, desc, price, feats, enabled, featured ->
                viewModel.savePackage(id, name, desc, price, feats, enabled, featured)
                showDialog = false
            }
        )
    }

    pkgToDelete?.let { pkg ->
        AlertDialog(
            onDismissRequest = { pkgToDelete = null },
            title = { Text("Delete Package") },
            text = { Text("Are you sure you want to delete '${pkg.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePackage(pkg.id)
                        pkgToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pkgToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PackageFormDialog(
    initialPackage: PackageDto?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (id: String, name: String, description: String, price: String, features: String, enabled: Boolean, featured: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var description by remember { mutableStateOf(initialPackage?.description ?: "") }
    var price by remember { mutableStateOf(initialPackage?.price ?: "") }
    var featuresStr by remember { mutableStateOf(initialPackage?.features?.joinToString("\n") ?: "") }
    var enabled by remember { mutableStateOf(initialPackage?.enabled ?: true) }
    var featured by remember { mutableStateOf(initialPackage?.featured ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialPackage == null) "Add Package" else "Edit Package") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Package Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (e.g. $1,200 or ₹45,000)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = featuresStr,
                    onValueChange = { featuresStr = it },
                    label = { Text("Features (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enabled")
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = featured, onCheckedChange = { featured = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Featured")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(initialPackage?.id ?: "", name, description, price, featuresStr, enabled, featured) },
                enabled = !isSaving && name.isNotBlank()
            ) {
                Text(if (initialPackage == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
