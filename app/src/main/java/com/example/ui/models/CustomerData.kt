package com.example.ui.models

import com.example.data.models.InvoiceDto

data class BookingUpdate(
    val timestamp: Long,
    val title: String,
    val message: String
)

enum class GalleryStatus(val displayName: String) {
    UNAVAILABLE("Not Yet Available"),
    PROCESSING("Processing Media"),
    READY("Ready to View")
}

data class CustomerBookingData(
    val id: String = "",
    val referenceId: String = "",
    val eventType: String = "",
    val eventDate: Long = 0L,
    val location: String = "",
    val selectedPackage: String = "",
    val status: BookingStatus = BookingStatus.NEW,
    val updates: List<BookingUpdate> = emptyList(),
    val galleryStatus: GalleryStatus = GalleryStatus.UNAVAILABLE,
    val invoice: InvoiceDto? = null
) {
    val bookingId: String get() = id.ifBlank { referenceId }
}
