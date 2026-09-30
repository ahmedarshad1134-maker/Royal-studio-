package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
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
import com.example.data.models.ReviewDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminReviewsUiState(
    val pendingReviews: List<ReviewDto> = emptyList(),
    val approvedReviews: List<ReviewDto> = emptyList(),
    val selectedTab: Int = 0, // 0 = Pending, 1 = Approved
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AdminReviewsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminReviewsUiState())
    val uiState: StateFlow<AdminReviewsUiState> = _uiState.asStateFlow()

    private val dbRepo = RepositoryProvider.firebaseRepository

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (dbRepo.isInitialized) {
                launch {
                    dbRepo.getPendingReviews()
                        .catch { e -> _uiState.update { it.copy(error = e.message) } }
                        .collect { pending ->
                            _uiState.update { it.copy(pendingReviews = pending, isLoading = false) }
                        }
                }
                launch {
                    dbRepo.getApprovedReviews()
                        .catch { e -> _uiState.update { it.copy(error = e.message) } }
                        .collect { approved ->
                            _uiState.update { it.copy(approvedReviews = approved) }
                        }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun approveReview(id: String) {
        viewModelScope.launch {
            try {
                dbRepo.approveReview(id)
                _uiState.update { it.copy(message = "Review approved successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to approve review") }
            }
        }
    }

    fun deleteReview(id: String) {
        viewModelScope.launch {
            try {
                dbRepo.deleteReview(id)
                _uiState.update { it.copy(message = "Review deleted") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete review") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

@Composable
fun AdminReviewsSection(
    viewModel: AdminReviewsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var reviewToDelete by remember { mutableStateOf<ReviewDto?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Customer Reviews Moderation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = uiState.selectedTab) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("Pending (${uiState.pendingReviews.size})") }
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("Approved (${uiState.approvedReviews.size})") }
            )
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

        val currentList = if (uiState.selectedTab == 0) uiState.pendingReviews else uiState.approvedReviews

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (uiState.selectedTab == 0) "No pending reviews to moderate." else "No approved reviews found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentList, key = { it.id }) { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = review.customerName.ifBlank { "Anonymous Client" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${review.rating}/5",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (review.eventType.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Event: ${review.eventType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = review.review,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!review.approved) {
                                    Button(
                                        onClick = { viewModel.approveReview(review.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Approve")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                OutlinedButton(
                                    onClick = { reviewToDelete = review },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reviewToDelete?.let { review ->
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to permanently delete this review from ${review.customerName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReview(review.id)
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
