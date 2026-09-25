package com.example.ui.models

enum class BookingStatus(val displayName: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    QUOTATION_SENT("Quotation Sent"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class BookingAuditEntry(
    val status: String = "",
    val updatedBy: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class BookingItem(
    val id: String,
    val referenceId: String = id,
    val customerId: String = "",
    val customerName: String,
    val phone: String,
    val email: String?,
    val eventType: String,
    val eventDate: Long,
    val location: String,
    val serviceId: String? = null,
    val packageId: String? = null,
    val message: String? = null,
    val status: BookingStatus = BookingStatus.NEW,
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "",
    val auditTrail: List<BookingAuditEntry> = emptyList()
)
