package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.AppNotification
import com.example.ui.models.NotificationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class NotificationState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList()
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationState())
    val uiState: StateFlow<NotificationState> = _uiState.asStateFlow()

    // Architecture simulation: in a real app, this service would take a user's Auth Token 
    // and fetch securely scoped data from the backend via NotificationService/Repository.
    fun loadCustomerNotifications(bookingRef: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500)
            
            val mockNotifications = listOf(
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Booking Confirmed",
                    message = "Your booking $bookingRef has been confirmed.",
                    timestamp = System.currentTimeMillis() - 3600000L, // 1 hr ago
                    isRead = false,
                    type = NotificationType.BOOKING_UPDATE,
                    referenceId = bookingRef
                ),
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Enquiry Received",
                    message = "We have received your enquiry and will contact you shortly.",
                    timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
                    isRead = true,
                    type = NotificationType.ENQUIRY_RECEIVED,
                    referenceId = bookingRef
                )
            )
            
            _uiState.update { it.copy(isLoading = false, notifications = mockNotifications) }
        }
    }

    fun loadAdminNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500)
            
            val mockNotifications = listOf(
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "New Enquiry Received",
                    message = "A new wedding enquiry has been submitted by John Doe.",
                    timestamp = System.currentTimeMillis() - 1800000L, // 30 mins ago
                    isRead = false,
                    type = NotificationType.ENQUIRY_RECEIVED,
                    referenceId = "REQ-12345"
                ),
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Pending Review",
                    message = "A new review is awaiting your approval.",
                    timestamp = System.currentTimeMillis() - 7200000L, // 2 hrs ago
                    isRead = false,
                    type = NotificationType.REVIEW_PENDING,
                    referenceId = "REV-999"
                ),
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "Upcoming Event Reminder",
                    message = "Event 'Smith Wedding' is happening tomorrow.",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    isRead = true,
                    type = NotificationType.EVENT_REMINDER,
                    referenceId = "REQ-11111"
                )
            )
            
            _uiState.update { it.copy(isLoading = false, notifications = mockNotifications) }
        }
    }

    fun markAsRead(notificationId: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == notificationId) it.copy(isRead = true) else it
                }
            )
        }
    }
    
    fun markAllAsRead() {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }
    }
}
