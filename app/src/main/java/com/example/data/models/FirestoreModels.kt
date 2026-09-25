package com.example.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserDto(
    @DocumentId val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "customer",
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
) {
    @get:Exclude
    val id: String get() = uid

    @get:Exclude
    val userId: String get() = uid

    @get:Exclude
    val displayName: String get() = name

    @get:Exclude
    val phoneNumber: String get() = phone
}

data class BlockedDateDto(
    @DocumentId val id: String = "",
    val date: Long = 0,
    val reason: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

data class AuditEntryDto(
    val status: String = "",
    val updatedBy: String = "",
    val timestamp: Long = 0,
    val note: String = ""
)

data class BookingDto(
    @DocumentId val id: String = "",
    val referenceId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val phone: String = "",
    val email: String = "",
    val eventType: String = "",
    val eventDate: Long = 0,
    val eventLocation: String = "",
    val serviceId: String = "",
    val packageId: String = "",
    val message: String = "",
    val status: String = "NEW", // NEW, CONTACTED, QUOTATION_SENT, CONFIRMED, COMPLETED, CANCELLED
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null,
    val adminNotes: String = "",
    val updatedBy: String = "",
    val auditTrail: List<AuditEntryDto> = emptyList()
) {
    @get:Exclude
    val bookingId: String get() = id

    @get:Exclude
    val location: String get() = eventLocation

    @get:Exclude
    val date: Long get() = eventDate
}

data class ServiceDto(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val enabled: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)

data class PackageDto(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val currency: String = "USD",
    val features: List<String> = emptyList(),
    val enabled: Boolean = true,
    val featured: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)

data class PortfolioDto(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "",
    val mediaType: String = "IMAGE",
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val featured: Boolean = false,
    val enabled: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
) {
    @get:Exclude
    val imageUrl: String get() = mediaUrl

    @get:Exclude
    val url: String get() = mediaUrl
}

data class ReviewDto(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val rating: Int = 5,
    val review: String = "",
    val eventType: String = "",
    val photoUrl: String = "",
    val approved: Boolean = false,
    val featured: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
) {
    @get:Exclude
    val comment: String get() = review

    @get:Exclude
    val reviewText: String get() = review

    @get:Exclude
    val text: String get() = review
}

data class NotificationDto(
    @DocumentId val id: String = "",
    val recipientUserId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedBookingId: String? = null,
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null
)

data class StudioSettingsDto(
    @DocumentId val id: String = "main_settings",
    val studioName: String = "",
    val tagline: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val address: String = "",
    val businessHours: String = "",
    val socialLinks: Map<String, String> = emptyMap(),
    val aboutText: String = ""
)

data class LineItemDto(
    val description: String = "",
    val amount: Double = 0.0,
    val category: String = "SERVICE" // PACKAGE, SERVICE, EXTRA, DISCOUNT
)

data class PaymentRecordDto(
    val paymentId: String = "",
    val amount: Double = 0.0,
    val method: String = "Bank Transfer", // Bank Transfer, Cash, Card, UPI, Cheque
    val referenceNotes: String = "",
    val recordedBy: String = "",
    val timestamp: Long = 0L,
    val receiptId: String = ""
)

data class InvoiceDto(
    @DocumentId val id: String = "",
    val invoiceNumber: String = "", // e.g. INV-2026-XXXX
    val bookingId: String = "",
    val bookingReference: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val eventType: String = "",
    val eventDate: Long = 0L,
    val eventLocation: String = "",
    val selectedPackage: String = "",
    val selectedServices: List<String> = emptyList(),
    val lineItems: List<LineItemDto> = emptyList(),
    val subtotal: Double = 0.0,
    val additionalCharges: Double = 0.0,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0, // Configurable percentage e.g. 0.0 or 18.0
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val paymentStatus: String = "Unpaid", // Unpaid, Partially Paid, Paid, Refunded
    val currency: String = "INR",
    val issueDate: Long = 0L,
    val dueDate: Long = 0L,
    val paymentRecords: List<PaymentRecordDto> = emptyList(),
    val notes: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
) {
    @get:Exclude
    val status: String get() = paymentStatus

    @get:Exclude
    val total: Double get() = totalAmount

    @get:Exclude
    val items: List<LineItemDto> get() = lineItems

    @get:Exclude
    val payments: List<PaymentRecordDto> get() = paymentRecords
}
