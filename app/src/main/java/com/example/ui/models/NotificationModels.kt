package com.example.ui.models

enum class NotificationType {
    ENQUIRY_RECEIVED,
    BOOKING_UPDATE,
    EVENT_REMINDER,
    REVIEW_PENDING,
    SYSTEM_ALERT
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType,
    val referenceId: String? = null
)
