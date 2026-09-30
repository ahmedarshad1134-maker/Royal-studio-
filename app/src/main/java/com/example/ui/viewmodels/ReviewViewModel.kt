package com.example.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.ReviewDto
import com.example.data.repository.RepositoryProvider
import com.example.ui.models.ReviewItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class ReviewsUiState(
    val isLoading: Boolean = true,
    val allReviews: List<ReviewItem> = emptyList(), // Admin sees all
    val isSubmittingReview: Boolean = false,
    val reviewSubmissionSuccess: Boolean = false
) {
    // Public facing lists
    val approvedReviews: List<ReviewItem>
        get() = allReviews.filter { it.approved }

    val featuredReview: ReviewItem?
        get() = approvedReviews.find { it.featured }
        
    val averageRating: Double?
        get() = if (approvedReviews.isEmpty()) null else approvedReviews.map { it.rating }.average()
}

class ReviewViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()
    private val repository = RepositoryProvider.firebaseRepository

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (repository.isInitialized) {
                repository.getApprovedReviews()
                    .catch { e ->
                        Log.w("ReviewViewModel", "Failed to load reviews from Firestore", e)
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    .collect { dtos ->
                        val items = dtos.map { dto ->
                            ReviewItem(
                                id = dto.id,
                                customerName = dto.customerName,
                                rating = dto.rating,
                                eventType = dto.eventType,
                                review = dto.review,
                                approved = dto.approved,
                                featured = dto.featured,
                                createdAt = dto.createdAt?.time ?: System.currentTimeMillis()
                            )
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                allReviews = items
                            )
                        }
                    }
            } else {
                delay(400)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        allReviews = emptyList()
                    )
                }
            }
        }
    }

    fun submitReview(name: String, rating: Int, eventType: String, reviewText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewSubmissionSuccess = false) }
            
            val newReview = ReviewItem(
                id = "REV-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                customerName = name,
                rating = rating,
                eventType = eventType,
                review = reviewText,
                approved = false, // Must be approved by admin
                featured = false,
                createdAt = System.currentTimeMillis()
            )

            if (repository.isInitialized) {
                try {
                    val dto = ReviewDto(
                        id = newReview.id,
                        customerName = name,
                        rating = rating,
                        eventType = eventType,
                        review = reviewText,
                        approved = false,
                        featured = false,
                        createdAt = Date()
                    )
                    repository.createReview(dto)
                } catch (e: Exception) {
                    Log.e("ReviewViewModel", "Failed to submit review", e)
                }
            } else {
                delay(600)
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    isSubmittingReview = false,
                    reviewSubmissionSuccess = true,
                    allReviews = currentState.allReviews + newReview
                )
            }
            
            // Reset success state after a delay
            delay(3000)
            _uiState.update { it.copy(reviewSubmissionSuccess = false) }
        }
    }
}
