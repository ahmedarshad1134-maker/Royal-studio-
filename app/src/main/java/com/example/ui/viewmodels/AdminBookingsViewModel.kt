package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.BookingDto
import com.example.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

data class AdminBookingsUiState(
    val bookings: List<BookingDto> = emptyList(),
    val filteredBookings: List<BookingDto> = emptyList(),
    val selectedFilter: String = "ALL", // ALL, NEW, CONTACTED, QUOTATION_SENT, CONFIRMED, COMPLETED, CANCELLED
    val searchQuery: String = "",
    val selectedBooking: BookingDto? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val conflictWarning: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class AdminBookingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBookingsUiState())
    val uiState: StateFlow<AdminBookingsUiState> = _uiState.asStateFlow()

    private val repository = RepositoryProvider.firebaseRepository
    private val authRepo = RepositoryProvider.authRepository

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (repository.isInitialized) {
                repository.getBookings()
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to stream bookings: ${e.localizedMessage}"
                            )
                        }
                    }
                    .collect { list ->
                        val sorted = list.sortedByDescending { it.createdAt?.time ?: it.eventDate }
                        _uiState.update { current ->
                            val updatedSelected = if (current.selectedBooking != null) {
                                sorted.firstOrNull { it.id == current.selectedBooking.id } ?: current.selectedBooking
                            } else null

                            current.copy(
                                bookings = sorted,
                                isLoading = false,
                                selectedBooking = updatedSelected
                            )
                        }
                        applyFilters()
                    }
            } else {
                // Initial demo bookings when backend is offline
                val demoBookings = getMockBookings()
                _uiState.update {
                    it.copy(
                        bookings = demoBookings,
                        isLoading = false
                    )
                }
                applyFilters()
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        val filtered = current.bookings.filter { item ->
            val matchesFilter = when (current.selectedFilter) {
                "ALL" -> true
                else -> item.status.equals(current.selectedFilter, ignoreCase = true)
            }
            val matchesSearch = if (current.searchQuery.isBlank()) true else {
                val q = current.searchQuery.trim().lowercase(Locale.getDefault())
                item.referenceId.lowercase(Locale.getDefault()).contains(q) ||
                item.id.lowercase(Locale.getDefault()).contains(q) ||
                item.customerName.lowercase(Locale.getDefault()).contains(q) ||
                item.eventType.lowercase(Locale.getDefault()).contains(q) ||
                item.phone.lowercase(Locale.getDefault()).contains(q) ||
                item.eventLocation.lowercase(Locale.getDefault()).contains(q)
            }
            matchesFilter && matchesSearch
        }
        _uiState.update { it.copy(filteredBookings = filtered) }
    }

    fun selectBooking(booking: BookingDto?) {
        _uiState.update {
            it.copy(
                selectedBooking = booking,
                conflictWarning = null,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    /**
     * Check for potential date conflicts before confirming
     */
    fun checkForDateConflict(booking: BookingDto): String? {
        if (booking.eventDate <= 0L) return null
        val calendar1 = java.util.Calendar.getInstance().apply { timeInMillis = booking.eventDate }
        val y1 = calendar1.get(java.util.Calendar.YEAR)
        val d1 = calendar1.get(java.util.Calendar.DAY_OF_YEAR)

        val conflicting = _uiState.value.bookings.firstOrNull { other ->
            other.id != booking.id && other.status == "CONFIRMED" && run {
                val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = other.eventDate }
                cal2.get(java.util.Calendar.YEAR) == y1 && cal2.get(java.util.Calendar.DAY_OF_YEAR) == d1
            }
        }

        return conflicting?.let {
            "Warning: An existing confirmed booking (#${it.referenceId.ifBlank { it.id }} - ${it.customerName}, ${it.eventType}) is already scheduled on this same date!"
        }
    }

    /**
     * Update booking status with audit trail and optional admin note
     */
    fun updateStatus(bookingId: String, newStatus: String, note: String = "") {
        val currentAdmin = authRepo.currentUser?.email ?: "Admin"
        val target = _uiState.value.bookings.firstOrNull { it.id == bookingId } ?: return

        // Check conflicts if confirming
        if (newStatus == "CONFIRMED") {
            val conflict = checkForDateConflict(target)
            if (conflict != null) {
                _uiState.update { it.copy(conflictWarning = conflict) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null, statusMessage = null) }
            val success = repository.updateBookingStatus(
                id = bookingId,
                newStatus = newStatus,
                adminNotes = note.takeIf { it.isNotBlank() },
                updatedBy = currentAdmin
            )

            if (success) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        statusMessage = "Status updated to $newStatus successfully"
                    )
                }
                // Refresh local list if using mock/offline
                if (!repository.isInitialized) {
                    val updatedList = _uiState.value.bookings.map { b ->
                        if (b.id == bookingId) {
                            b.copy(
                                status = newStatus,
                                adminNotes = if (note.isNotBlank()) note else b.adminNotes,
                                updatedAt = Date()
                            )
                        } else b
                    }
                    _uiState.update { it.copy(bookings = updatedList) }
                    applyFilters()
                }
            } else {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessage = "Failed to update booking status. Please check permissions."
                    )
                }
            }
        }
    }

    /**
     * Update internal notes specifically
     */
    fun saveInternalNotes(bookingId: String, notes: String) {
        val currentAdmin = authRepo.currentUser?.email ?: "Admin"
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
            val success = repository.updateBookingStatus(
                id = bookingId,
                newStatus = _uiState.value.selectedBooking?.status ?: "NEW",
                adminNotes = notes,
                updatedBy = currentAdmin
            )
            _uiState.update {
                it.copy(
                    isUpdating = false,
                    statusMessage = if (success) "Internal notes saved." else "Failed to save internal notes."
                )
            }
            if (!repository.isInitialized && success) {
                val updatedList = _uiState.value.bookings.map { b ->
                    if (b.id == bookingId) b.copy(adminNotes = notes, updatedAt = Date()) else b
                }
                _uiState.update { it.copy(bookings = updatedList) }
                applyFilters()
            }
        }
    }

    /**
     * Update event details (date, location, package, message)
     */
    fun updateEventDetails(
        bookingId: String,
        eventType: String,
        eventDate: Long,
        location: String,
        packageId: String
    ) {
        val currentAdmin = authRepo.currentUser?.email ?: "Admin"
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
            val success = repository.updateBookingEventDetails(
                id = bookingId,
                eventType = eventType,
                eventDate = eventDate,
                eventLocation = location,
                selectedPackage = packageId,
                updatedBy = currentAdmin
            )
            _uiState.update {
                it.copy(
                    isUpdating = false,
                    statusMessage = if (success) "Event details updated." else "Failed to update event details."
                )
            }
            if (!repository.isInitialized && success) {
                val updatedList = _uiState.value.bookings.map { b ->
                    if (b.id == bookingId) {
                        b.copy(
                            eventType = eventType,
                            eventDate = eventDate,
                            eventLocation = location,
                            packageId = packageId,
                            updatedAt = Date()
                        )
                    } else b
                }
                _uiState.update { it.copy(bookings = updatedList) }
                applyFilters()
            }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null, conflictWarning = null) }
    }

    private fun getMockBookings(): List<BookingDto> {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        return listOf(
            BookingDto(
                id = "RS-2026-X892",
                referenceId = "RS-2026-X892",
                customerName = "Ananya & Rohan Sharma",
                phone = "+91 98765 43210",
                email = "rohan.sharma@example.com",
                eventType = "Wedding",
                eventDate = now + oneDay * 24,
                eventLocation = "The Leela Palace, Udaipur",
                packageId = "Royal Imperial Wedding",
                message = "We need full 3-day coverage including Sangeet, Mehendi and Reception.",
                status = "NEW",
                createdAt = Date(now - oneDay * 1),
                updatedAt = Date(now - oneDay * 1),
                adminNotes = "High priority inquiry. Needs customized drone & cinematic crew package.",
                updatedBy = "Customer"
            ),
            BookingDto(
                id = "RS-2026-M419",
                referenceId = "RS-2026-M419",
                customerName = "Pooja Malhotra",
                phone = "+91 91234 56789",
                email = "pooja.m@example.com",
                eventType = "Pre-Wedding",
                eventDate = now + oneDay * 12,
                eventLocation = "Nahargarh Fort & Jal Mahal, Jaipur",
                packageId = "Cinematic Romance Package",
                message = "Looking for sunset portraiture and 4K aerial video.",
                status = "CONTACTED",
                createdAt = Date(now - oneDay * 3),
                updatedAt = Date(now - oneDay * 2),
                adminNotes = "Called client on Tuesday. Sent mood board. Awaiting location permissions.",
                updatedBy = "admin@royalstudio.com"
            ),
            BookingDto(
                id = "RS-2026-Q772",
                referenceId = "RS-2026-Q772",
                customerName = "Vikram Singhania",
                phone = "+91 99887 76655",
                email = "vikram.s@example.com",
                eventType = "Engagement",
                eventDate = now + oneDay * 35,
                eventLocation = "Taj Falaknuma, Hyderabad",
                packageId = "Signature Royale",
                message = "Evening ring ceremony with 300 guests.",
                status = "QUOTATION_SENT",
                createdAt = Date(now - oneDay * 5),
                updatedAt = Date(now - oneDay * 2),
                adminNotes = "Quotation #Q-882 sent for INR 2,75,000 including teaser film.",
                updatedBy = "admin@royalstudio.com"
            ),
            BookingDto(
                id = "RS-2026-C104",
                referenceId = "RS-2026-C104",
                customerName = "Simran & Kabir Varma",
                phone = "+91 98111 22334",
                email = "kabir.varma@example.com",
                eventType = "Wedding",
                eventDate = now + oneDay * 45,
                eventLocation = "ITC Grand Bharat, Gurugram",
                packageId = "Royal Imperial Wedding",
                message = "Both candid photography and traditional coverage needed.",
                status = "CONFIRMED",
                createdAt = Date(now - oneDay * 10),
                updatedAt = Date(now - oneDay * 4),
                adminNotes = "Advance token received. Lead photographer assigned: Amit. Second shooter: Priya.",
                updatedBy = "admin@royalstudio.com"
            ),
            BookingDto(
                id = "RS-2026-D330",
                referenceId = "RS-2026-D330",
                customerName = "Arjun Kapoor",
                phone = "+91 97766 55443",
                email = "arjun.k@example.com",
                eventType = "Anniversary",
                eventDate = now - oneDay * 7,
                eventLocation = "Oberoi Amarvilas, Agra",
                packageId = "Silver Elegance",
                message = "25th anniversary celebration coverage.",
                status = "COMPLETED",
                createdAt = Date(now - oneDay * 25),
                updatedAt = Date(now - oneDay * 6),
                adminNotes = "Event finished successfully. 450 raw shots delivered to editing queue.",
                updatedBy = "admin@royalstudio.com"
            )
        )
    }
}
