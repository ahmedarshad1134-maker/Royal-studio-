package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.models.PortfolioDto
import com.example.data.repository.RepositoryProvider
import com.example.data.repository.UploadState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminPortfolioUiState(
    val items: List<PortfolioDto> = emptyList(),
    val uploadState: UploadState = UploadState.Idle,
    val isLoading: Boolean = false
)

class AdminPortfolioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminPortfolioUiState())
    val uiState: StateFlow<AdminPortfolioUiState> = _uiState.asStateFlow()

    private val dbRepo = RepositoryProvider.firebaseRepository
    private val storageRepo = RepositoryProvider.storageRepository

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            if (dbRepo.isInitialized) {
                dbRepo.getPortfolioItems().collect { items ->
                    _uiState.update { it.copy(items = items) }
                }
            }
        }
    }

    fun uploadMedia(uri: Uri, category: String, title: String) {
        viewModelScope.launch {
            if (!storageRepo.isInitialized || !dbRepo.isInitialized) {
                _uiState.update { it.copy(uploadState = UploadState.Error("Firebase not initialized")) }
                return@launch
            }

            storageRepo.uploadPortfolioMedia(category.lowercase(), uri).collect { state ->
                _uiState.update { it.copy(uploadState = state) }
                if (state is UploadState.Success) {
                    // Create Firestore document
                    val item = PortfolioDto(
                        title = title.ifBlank { "Untitled" },
                        category = category.uppercase(),
                        mediaType = "IMAGE", // Simplified for this example, or derive from MIME type
                        mediaUrl = state.url,
                        enabled = true,
                        featured = false
                    )
                    dbRepo.createPortfolioItem(item)
                    // Reset upload state after a short delay
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(uploadState = UploadState.Idle) }
                }
            }
        }
    }

    fun deleteItem(item: PortfolioDto) {
        viewModelScope.launch {
            if (item.mediaUrl.isNotBlank()) {
                storageRepo.deleteMedia(item.mediaUrl)
            }
            if (item.id.isNotBlank()) {
                dbRepo.deletePortfolioItem(item.id)
            }
        }
    }
}

@Composable
fun AdminPortfolioSection(viewModel: AdminPortfolioViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("WEDDING") }
    var mediaTitle by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.uploadMedia(it, selectedCategory, mediaTitle)
                showDialog = false
                mediaTitle = ""
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Portfolio Items (${uiState.items.size})", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Media")
                Spacer(Modifier.width(8.dp))
                Text("Upload")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.uploadState !is UploadState.Idle) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (val state = uiState.uploadState) {
                        is UploadState.Uploading -> Text("Starting upload...")
                        is UploadState.Progress -> {
                            Text("Uploading: ${state.progress.toInt()}%")
                            LinearProgressIndicator(
                                progress = { state.progress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        is UploadState.Success -> Text("Upload successful!", color = Color.Green)
                        is UploadState.Error -> Text("Upload failed: ${state.error}", color = MaterialTheme.colorScheme.error)
                        is UploadState.Cancelled -> Text("Upload cancelled")
                        else -> {}
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.items) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.mediaUrl,
                            contentDescription = item.title,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(item.category, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { viewModel.deleteItem(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Upload New Media") },
            text = {
                Column {
                    OutlinedTextField(
                        value = mediaTitle,
                        onValueChange = { mediaTitle = it },
                        label = { Text("Title (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Category:", style = MaterialTheme.typography.labelMedium)
                    // Simplified dropdown alternative for categories
                    val categories = listOf("WEDDING", "PRE_WEDDING", "ENGAGEMENT", "BIRTHDAY", "BABY_FAMILY", "ANNIVERSARY", "OTHER_EVENTS")
                    categories.forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                            Text(cat, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                }) {
                    Text("Select Media")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
