package com.example.data.repository

import android.util.Log
import com.example.data.models.*
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class FirebaseRepository {
    private var db: FirebaseFirestore? = null
    private var _isInitialized: Boolean = false

    val isInitialized: Boolean
        get() {
            if (_isInitialized && db != null) return true
            return try {
                FirebaseApp.getInstance()
                db = Firebase.firestore
                _isInitialized = true
                true
            } catch (e: Throwable) {
                _isInitialized = false
                db = null
                false
            }
        }
    
    init {
        // Initial check
        isInitialized
    }

    // Users
    suspend fun createUserProfile(user: UserDto) {
        if (!isInitialized || db == null) return
        // Secure role enforcement: Never allow non-admin client to set role to admin
        val safeUser = if (user.role.equals("admin", ignoreCase = true)) {
            user.copy(role = "customer")
        } else {
            user
        }
        db!!.collection("users").document(safeUser.uid).set(safeUser).await()
    }

    suspend fun getUserProfile(uid: String): UserDto? {
        if (!isInitialized || db == null || uid.isBlank()) return null
        return try {
            val snapshot = db!!.collection("users").document(uid).get().await()
            snapshot.toObject(UserDto::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting user profile", e)
            null
        }
    }

    // Bookings
    suspend fun createBooking(booking: BookingDto): String {
        if (!isInitialized || db == null) {
            throw IllegalStateException("Firebase is not initialized. Please configure Firebase with google-services.json.")
        }
        val docRef = if (booking.id.isNotBlank()) db!!.collection("bookings").document(booking.id) else db!!.collection("bookings").document()
        val toSave = if (booking.id.isBlank()) booking.copy(id = docRef.id, referenceId = booking.referenceId.ifBlank { docRef.id }) else booking
        docRef.set(toSave).await()
        return docRef.id
    }

    suspend fun updateBookingStatus(
        id: String,
        newStatus: String,
        adminNotes: String? = null,
        updatedBy: String = "admin"
    ): Boolean {
        if (!isInitialized || db == null) return false
        return try {
            val docRef = db!!.collection("bookings").document(id)
            val snapshot = docRef.get().await()
            val currentBooking = snapshot.toObject(BookingDto::class.java)

            val newAudit = AuditEntryDto(
                status = newStatus,
                updatedBy = updatedBy,
                timestamp = System.currentTimeMillis(),
                note = adminNotes ?: ""
            )

            val updatedAuditTrail = (currentBooking?.auditTrail ?: emptyList()) + newAudit

            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedBy" to updatedBy,
                "auditTrail" to updatedAuditTrail
            )

            if (adminNotes != null) {
                updates["adminNotes"] = adminNotes
            }

            docRef.update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateBookingEventDetails(
        id: String,
        eventType: String,
        eventDate: Long,
        eventLocation: String,
        selectedPackage: String = "",
        adminNotes: String? = null,
        updatedBy: String = "admin"
    ): Boolean {
        if (!isInitialized || db == null) return false
        return try {
            val docRef = db!!.collection("bookings").document(id)
            val snapshot = docRef.get().await()
            val currentBooking = snapshot.toObject(BookingDto::class.java)

            val newAudit = AuditEntryDto(
                status = currentBooking?.status ?: "NEW",
                updatedBy = updatedBy,
                timestamp = System.currentTimeMillis(),
                note = "Updated event details"
            )
            val updatedAuditTrail = (currentBooking?.auditTrail ?: emptyList()) + newAudit

            val updates = mutableMapOf<String, Any>(
                "eventType" to eventType,
                "eventDate" to eventDate,
                "eventLocation" to eventLocation,
                "packageId" to selectedPackage,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedBy" to updatedBy,
                "auditTrail" to updatedAuditTrail
            )
            if (adminNotes != null) {
                updates["adminNotes"] = adminNotes
            }

            docRef.update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getBookings(): Flow<List<BookingDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("bookings")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(BookingDto::class.java)
                    trySend(bookings)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getCustomerBookings(customerId: String = "", email: String = ""): Flow<List<BookingDto>> = callbackFlow {
        if (!isInitialized || db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val collection = db!!.collection("bookings")
        val query = if (customerId.isNotBlank()) {
            collection.whereEqualTo("customerId", customerId)
        } else if (email.isNotBlank()) {
            collection.whereEqualTo("email", email)
        } else {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bookings = snapshot.toObjects(BookingDto::class.java)
                trySend(bookings)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getCustomerBookings(email: String): Flow<List<BookingDto>> = getCustomerBookings("", email)

    suspend fun getBookingById(id: String): BookingDto? {
        if (!isInitialized) return null
        val snapshot = db!!.collection("bookings").document(id).get().await()
        return snapshot.toObject(BookingDto::class.java)
    }

    // Services
    fun getServices(): Flow<List<ServiceDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val services = snapshot.toObjects(ServiceDto::class.java)
                    trySend(services)
                }
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun createService(service: ServiceDto): String {
        if (!isInitialized) return ""
        val docRef = if (service.id.isNotBlank()) {
            db!!.collection("services").document(service.id)
        } else {
            db!!.collection("services").document()
        }
        val toSave = if (service.id.isBlank()) service.copy(id = docRef.id) else service
        docRef.set(toSave).await()
        return docRef.id
    }

    suspend fun updateService(service: ServiceDto) {
        if (!isInitialized || service.id.isBlank()) return
        db!!.collection("services").document(service.id).set(service).await()
    }

    suspend fun deleteService(id: String) {
        if (!isInitialized || id.isBlank()) return
        db!!.collection("services").document(id).delete().await()
    }

    // Packages
    fun getPackages(): Flow<List<PackageDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("packages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val packages = snapshot.toObjects(PackageDto::class.java)
                    trySend(packages)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPackage(pkg: PackageDto): String {
        if (!isInitialized) return ""
        val docRef = if (pkg.id.isNotBlank()) {
            db!!.collection("packages").document(pkg.id)
        } else {
            db!!.collection("packages").document()
        }
        val toSave = if (pkg.id.isBlank()) pkg.copy(id = docRef.id) else pkg
        docRef.set(toSave).await()
        return docRef.id
    }

    suspend fun updatePackage(pkg: PackageDto) {
        if (!isInitialized || pkg.id.isBlank()) return
        db!!.collection("packages").document(pkg.id).set(pkg).await()
    }

    suspend fun deletePackage(id: String) {
        if (!isInitialized || id.isBlank()) return
        db!!.collection("packages").document(id).delete().await()
    }

    // Portfolio
    fun getPortfolioItems(): Flow<List<PortfolioDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("portfolio")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(PortfolioDto::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun createPortfolioItem(item: PortfolioDto) {
        if (!isInitialized) return
        db!!.collection("portfolio").add(item).await()
    }
    
    suspend fun deletePortfolioItem(id: String) {
        if (!isInitialized) return
        db!!.collection("portfolio").document(id).delete().await()
    }

    // Reviews
    fun getApprovedReviews(): Flow<List<ReviewDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("reviews")
            .whereEqualTo("approved", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(ReviewDto::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
    
    fun getPendingReviews(): Flow<List<ReviewDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("reviews")
            .whereEqualTo("approved", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(ReviewDto::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
    
    fun getAllReviews(): Flow<List<ReviewDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(ReviewDto::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createReview(review: ReviewDto) {
        if (!isInitialized) return
        db!!.collection("reviews").add(review).await()
    }

    suspend fun approveReview(id: String) {
        if (!isInitialized || id.isBlank()) return
        db!!.collection("reviews").document(id).update("approved", true).await()
    }

    suspend fun deleteReview(id: String) {
        if (!isInitialized || id.isBlank()) return
        db!!.collection("reviews").document(id).delete().await()
    }

    // Notifications
    fun getNotificationsForUser(email: String): Flow<List<NotificationDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("notifications")
            .whereEqualTo("recipientUserId", email) // Using email as identifier for now
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(NotificationDto::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
    
    fun getAdminNotifications(): Flow<List<NotificationDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("notifications")
            .whereEqualTo("type", "ADMIN_ALERT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(NotificationDto::class.java)
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun markNotificationAsRead(id: String) {
        if (!isInitialized) return
        db!!.collection("notifications").document(id).update("read", true).await()
    }

    // Settings
    suspend fun getStudioSettings(): StudioSettingsDto? {
        if (!isInitialized) return null
        val snapshot = db!!.collection("studioSettings").document("main_settings").get().await()
        return snapshot.toObject(StudioSettingsDto::class.java)
    }
    
    suspend fun updateStudioSettings(settings: StudioSettingsDto) {
        if (!isInitialized) return
        db!!.collection("studioSettings").document("main_settings").set(settings).await()
    }

    // --- Invoices & Receipts ---

    suspend fun createOrUpdateInvoice(invoice: InvoiceDto): String {
        if (!isInitialized || db == null) {
            throw IllegalStateException("Firebase is not initialized. Please configure Firebase with google-services.json.")
        }
        val docRef = if (invoice.id.isNotBlank()) {
            db!!.collection("invoices").document(invoice.id)
        } else {
            db!!.collection("invoices").document()
        }
        val invoiceToSave = if (invoice.id.isBlank()) invoice.copy(id = docRef.id) else invoice
        docRef.set(invoiceToSave).await()
        return docRef.id
    }

    suspend fun getInvoiceById(invoiceId: String): InvoiceDto? {
        if (!isInitialized) return null
        return try {
            val snapshot = db!!.collection("invoices").document(invoiceId).get().await()
            snapshot.toObject(InvoiceDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getInvoiceByBookingId(bookingId: String): InvoiceDto? {
        if (!isInitialized) return null
        return try {
            val snapshot = db!!.collection("invoices")
                .whereEqualTo("bookingId", bookingId)
                .limit(1)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(InvoiceDto::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun getAllInvoices(): Flow<List<InvoiceDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("invoices")
            .orderBy("issueDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(InvoiceDto::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getCustomerInvoices(email: String, customerId: String = ""): Flow<List<InvoiceDto>> = callbackFlow {
        if (!isInitialized) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val query = if (email.isNotBlank()) {
            db!!.collection("invoices").whereEqualTo("customerEmail", email)
        } else {
            db!!.collection("invoices").whereEqualTo("customerId", customerId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObjects(InvoiceDto::class.java))
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun recordPayment(
        invoiceId: String,
        amount: Double,
        method: String,
        notes: String,
        recordedBy: String
    ): Boolean {
        if (!isInitialized || db == null) return false
        return try {
            val docRef = db!!.collection("invoices").document(invoiceId)
            val snapshot = docRef.get().await()
            val currentInvoice = snapshot.toObject(InvoiceDto::class.java) ?: return false

            val newAmountPaid = (currentInvoice.amountPaid + amount).coerceAtMost(currentInvoice.totalAmount)
            val newBalance = (currentInvoice.totalAmount - newAmountPaid).coerceAtLeast(0.0)
            val newStatus = when {
                newBalance <= 0.001 -> "Paid"
                newAmountPaid > 0 -> "Partially Paid"
                else -> "Unpaid"
            }

            val receiptNumber = "RCP-${currentInvoice.invoiceNumber.replace("INV-", "")}-${currentInvoice.paymentRecords.size + 1}"
            val newRecord = PaymentRecordDto(
                paymentId = "PAY-${System.currentTimeMillis()}",
                amount = amount,
                method = method,
                referenceNotes = notes,
                recordedBy = recordedBy,
                timestamp = System.currentTimeMillis(),
                receiptId = receiptNumber
            )

            val updatedRecords = currentInvoice.paymentRecords + newRecord

            docRef.update(
                mapOf(
                    "amountPaid" to newAmountPaid,
                    "balanceDue" to newBalance,
                    "paymentStatus" to newStatus,
                    "paymentRecords" to updatedRecords,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Contact Messages ---

    suspend fun createContactMessage(message: ContactMessageDto): String {
        if (!isInitialized || db == null) {
            throw IllegalStateException("Firebase is not initialized. Please configure Firebase with google-services.json.")
        }
        val docRef = if (message.id.isNotBlank()) {
            db!!.collection("contactMessages").document(message.id)
        } else {
            db!!.collection("contactMessages").document()
        }
        val toSave = if (message.id.isBlank()) message.copy(id = docRef.id) else message
        docRef.set(toSave).await()
        return docRef.id
    }

    fun getContactMessages(): Flow<List<ContactMessageDto>> = callbackFlow {
        if (!isInitialized || db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db!!.collection("contactMessages")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(ContactMessageDto::class.java))
                }
            }
        awaitClose { listener.remove() }
    }
}
