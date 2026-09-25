package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.ReviewItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulate network delay
            delay(600)

            // As per requirements: Do NOT invent customer reviews.
            // Leaving this empty to demonstrate the empty state and admin approval flow.
            val mockReviews = emptyList<ReviewItem>()

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    allReviews = mockReviews
                )
            }
        }
    }

    fun submitReview(name: String, rating: Int, eventType: String, reviewText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewSubmissionSuccess = false) }
            
            // Simulate API call
            delay(1200)
            
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
