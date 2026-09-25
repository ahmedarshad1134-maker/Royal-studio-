package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrivateGalleryUiState(
    val isLoading: Boolean = true,
    val mediaUrls: List<String> = emptyList(),
    val error: String? = null,
    val selectedPreviewUrl: String? = null
)

class PrivateGalleryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PrivateGalleryUiState())
    val uiState: StateFlow<PrivateGalleryUiState> = _uiState.asStateFlow()

    private val storageRepo = RepositoryProvider.storageRepository
    private val authRepo = RepositoryProvider.authRepository

    fun loadGallery(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = authRepo.currentUser
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "Please authenticate to view private gallery deliverables.") }
                return@launch
            }
            if (!storageRepo.isInitialized) {
                _uiState.update { it.copy(isLoading = false, error = "Cloud media storage is connecting. Please check back shortly.") }
                return@launch
            }
            
            try {
                val urls = storageRepo.listPrivateGalleryFiles(user.uid, bookingId)
                if (urls.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "Deliverables are being processed by our post-production studio.") }
                } else {
                    _uiState.update { it.copy(isLoading = false, mediaUrls = urls) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load private media deliverables.") }
            }
        }
    }

    fun selectPreview(url: String?) {
        _uiState.update { it.copy(selectedPreviewUrl = url) }
    }
}

@Composable
fun CustomerPrivateGalleryScreen(
    bookingId: String,
    navController: NavController,
    viewModel: PrivateGalleryViewModel = viewModel()
) {
    CustomerPrivateGalleryScreen(
        bookingId = bookingId,
        onBack = { navController.popBackStack() },
        viewModel = viewModel
    )
}

@Composable
fun CustomerPrivateGalleryScreen(
    bookingId: String,
    onBack: () -> Unit,
    viewModel: PrivateGalleryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingId) {
        if (bookingId.isNotBlank()) {
            viewModel.loadGallery(bookingId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Private Client Gallery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Reference #$bookingId",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gallery Notice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.mediaUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Gallery Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.selectPreview(url) }
                    )
                }
            }
        }

        // Full Screen Lightbox Preview
        uiState.selectedPreviewUrl?.let { previewUrl ->
            Dialog(
                onDismissRequest = { viewModel.selectPreview(null) },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = previewUrl,
                        contentDescription = "Full Image Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                    IconButton(
                        onClick = { viewModel.selectPreview(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close preview",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
