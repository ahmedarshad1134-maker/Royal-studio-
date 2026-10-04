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
    val reviewSubmissionSuccess: Boolean = false,
    val reviewSubmissionError: String? = null,
    val isUserSignedIn: Boolean = false
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
    private val authRepo = RepositoryProvider.authRepository

    init {
        loadReviews()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepo.getAuthStateUpdates().collect { user ->
                _uiState.update { it.copy(isUserSignedIn = user != null) }
            }
        }
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

    fun clearSubmissionError() {
        _uiState.update { it.copy(reviewSubmissionError = null) }
    }

    fun submitReview(name: String, rating: Int, eventType: String, reviewText: String) {
        // Validation: rating between 1 and 5
        if (rating !in 1..5) {
            _uiState.update { it.copy(reviewSubmissionError = "Rating must be between 1 and 5 stars.") }
            return
        }
        val trimmedReview = reviewText.trim()
        if (trimmedReview.isBlank()) {
            _uiState.update { it.copy(reviewSubmissionError = "Review text cannot be blank.") }
            return
        }
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(reviewSubmissionError = "Please enter your name.") }
            return
        }

        // If user is not signed in, show a message and do not call Firestore
        val currentUser = authRepo.currentUser
        if (currentUser == null) {
            _uiState.update { 
                it.copy(
                    reviewSubmissionError = "Please sign in to submit a review.",
                    isUserSignedIn = false
                ) 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isSubmittingReview = true, 
                    reviewSubmissionSuccess = false,
                    reviewSubmissionError = null
                ) 
            }
            
            val reviewId = "REV-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
            val newReview = ReviewItem(
                id = reviewId,
                customerName = trimmedName,
                rating = rating,
                eventType = eventType.trim(),
                review = trimmedReview,
                approved = false, // Must be approved by admin
                featured = false,
                createdAt = System.currentTimeMillis()
            )

            if (!repository.isInitialized) {
                _uiState.update { 
                    it.copy(
                        isSubmittingReview = false,
                        reviewSubmissionError = "Firebase service is not initialized."
                    ) 
                }
                return@launch
            }

            try {
                // Set customerId = currentUser.uid in the ReviewDto
                val dto = ReviewDto(
                    id = reviewId,
                    customerId = currentUser.uid,
                    customerName = trimmedName,
                    rating = rating,
                    eventType = eventType.trim(),
                    review = trimmedReview,
                    approved = false,
                    featured = false,
                    createdAt = Date()
                )
                repository.createReview(dto)

                // Only set reviewSubmissionSuccess = true after the write truly succeeds
                _uiState.update { currentState ->
                    currentState.copy(
                        isSubmittingReview = false,
                        reviewSubmissionSuccess = true,
                        reviewSubmissionError = null,
                        allReviews = currentState.allReviews + newReview
                    )
                }
                
                // Reset success state after a delay
                delay(3000)
                _uiState.update { it.copy(reviewSubmissionSuccess = false) }
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Failed to submit review", e)
                _uiState.update { 
                    it.copy(
                        isSubmittingReview = false,
                        reviewSubmissionSuccess = false,
                        reviewSubmissionError = e.message ?: "Failed to submit review. Please try again."
                    ) 
                }
            }
        }
    }
}
