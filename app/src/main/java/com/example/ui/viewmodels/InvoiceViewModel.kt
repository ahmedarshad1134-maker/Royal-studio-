package com.example.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.BookingDto
import com.example.data.models.InvoiceDto
import com.example.data.models.LineItemDto
import com.example.data.models.PaymentRecordDto
import com.example.data.repository.RepositoryProvider
import com.example.util.InvoicePdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

data class InvoiceUiState(
    val invoices: List<InvoiceDto> = emptyList(),
    val filteredInvoices: List<InvoiceDto> = emptyList(),
    val selectedInvoice: InvoiceDto? = null,
    val selectedStatusFilter: String = "ALL", // ALL, Unpaid, Partially Paid, Paid, Refunded
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isActionInProgress: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class InvoiceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    private val repository = RepositoryProvider.firebaseRepository
    private val authRepo = RepositoryProvider.authRepository

    init {
        loadInvoices()
    }

    fun loadInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (repository.isInitialized) {
                repository.getAllInvoices()
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load invoices: ${e.localizedMessage}"
                            )
                        }
                    }
                    .collect { list ->
                        _uiState.update { current ->
                            val updatedSelected = if (current.selectedInvoice != null) {
                                list.firstOrNull { it.id == current.selectedInvoice.id } ?: current.selectedInvoice
                            } else null

                            current.copy(
                                invoices = list,
                                isLoading = false,
                                selectedInvoice = updatedSelected
                            )
                        }
                        applyFilters()
                    }
            } else {
                // Mock demo invoices
                val mocks = getMockInvoices()
                _uiState.update { it.copy(invoices = mocks, isLoading = false) }
                applyFilters()
            }
        }
    }

    fun setStatusFilter(filter: String) {
        _uiState.update { it.copy(selectedStatusFilter = filter) }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        val filtered = current.invoices.filter { inv ->
            val matchesFilter = if (current.selectedStatusFilter == "ALL") true
            else inv.paymentStatus.equals(current.selectedStatusFilter, ignoreCase = true)

            val matchesSearch = if (current.searchQuery.isBlank()) true else {
                val q = current.searchQuery.trim().lowercase(Locale.getDefault())
                inv.invoiceNumber.lowercase(Locale.getDefault()).contains(q) ||
                inv.bookingReference.lowercase(Locale.getDefault()).contains(q) ||
                inv.customerName.lowercase(Locale.getDefault()).contains(q) ||
                inv.customerPhone.lowercase(Locale.getDefault()).contains(q) ||
                inv.eventType.lowercase(Locale.getDefault()).contains(q)
            }
            matchesFilter && matchesSearch
        }
        _uiState.update { it.copy(filteredInvoices = filtered) }
    }

    fun selectInvoice(invoice: InvoiceDto?) {
        _uiState.update { it.copy(selectedInvoice = invoice, successMessage = null, errorMessage = null) }
    }

    /**
     * Create or regenerate an invoice for a specific booking
     */
    fun generateInvoiceForBooking(
        booking: BookingDto,
        subtotalAmount: Double,
        additionalCharges: Double = 0.0,
        discountAmount: Double = 0.0,
        taxRate: Double = 0.0,
        notes: String = ""
    ) {
        val currentAdmin = authRepo.currentUser?.email ?: "Admin"
        val subtotal = subtotalAmount.coerceAtLeast(0.0)
        val charges = additionalCharges.coerceAtLeast(0.0)
        val discount = discountAmount.coerceAtLeast(0.0)
        val taxableSubtotal = (subtotal + charges - discount).coerceAtLeast(0.0)
        val taxAmount = if (taxRate > 0) (taxableSubtotal * taxRate / 100.0) else 0.0
        val totalAmount = taxableSubtotal + taxAmount

        val invoiceNumber = "INV-2026-${Random.nextInt(1000, 9999)}"

        val lineItems = mutableListOf<LineItemDto>()
        lineItems.add(
            LineItemDto(
                description = "${booking.eventType} Coverage - ${booking.packageId.ifBlank { "Standard Studio Service" }}",
                amount = subtotal,
                category = "PACKAGE"
            )
        )
        if (charges > 0) {
            lineItems.add(
                LineItemDto(
                    description = "Additional Service & Production Crew",
                    amount = charges,
                    category = "EXTRA"
                )
            )
        }
        if (discount > 0) {
            lineItems.add(
                LineItemDto(
                    description = "Promotional / Loyalty Discount",
                    amount = -discount,
                    category = "DISCOUNT"
                )
            )
        }

        val invoice = InvoiceDto(
            invoiceNumber = invoiceNumber,
            bookingId = booking.id,
            bookingReference = booking.referenceId.ifBlank { booking.id },
            customerId = booking.customerId,
            customerName = booking.customerName,
            customerPhone = booking.phone,
            customerEmail = booking.email,
            eventType = booking.eventType,
            eventDate = booking.eventDate,
            eventLocation = booking.eventLocation,
            selectedPackage = booking.packageId,
            selectedServices = listOf(booking.eventType),
            lineItems = lineItems,
            subtotal = subtotal,
            additionalCharges = charges,
            discount = discount,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            amountPaid = 0.0,
            balanceDue = totalAmount,
            paymentStatus = "Unpaid",
            issueDate = System.currentTimeMillis(),
            dueDate = if (booking.eventDate > 0) booking.eventDate - (7 * 86400000L) else System.currentTimeMillis() + (14 * 86400000L),
            notes = notes
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }
            val id = repository.createOrUpdateInvoice(invoice)
            val created = invoice.copy(id = id)

            _uiState.update { current ->
                val updatedList = (listOf(created) + current.invoices.filter { it.id != id })
                current.copy(
                    isActionInProgress = false,
                    invoices = updatedList,
                    selectedInvoice = created,
                    successMessage = "Invoice ${created.invoiceNumber} created successfully"
                )
            }
            applyFilters()
        }
    }

    /**
     * Record a customer payment against an invoice
     */
    fun recordPayment(
        invoiceId: String,
        amount: Double,
        method: String,
        notes: String
    ) {
        val currentAdmin = authRepo.currentUser?.email ?: "Admin"
        val validAmount = amount.coerceAtLeast(0.0)
        if (validAmount <= 0) {
            _uiState.update { it.copy(errorMessage = "Payment amount must be greater than zero") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true, errorMessage = null) }
            val success = repository.recordPayment(
                invoiceId = invoiceId,
                amount = validAmount,
                method = method,
                notes = notes,
                recordedBy = currentAdmin
            )

            if (success) {
                // Update local representation
                _uiState.update { current ->
                    val updatedInvoices = current.invoices.map { inv ->
                        if (inv.id == invoiceId) {
                            val newPaid = (inv.amountPaid + validAmount).coerceAtMost(inv.totalAmount)
                            val newBal = (inv.totalAmount - newPaid).coerceAtLeast(0.0)
                            val newStatus = when {
                                newBal <= 0.001 -> "Paid"
                                newPaid > 0 -> "Partially Paid"
                                else -> "Unpaid"
                            }
                            val newRecord = PaymentRecordDto(
                                paymentId = "PAY-${System.currentTimeMillis()}",
                                amount = validAmount,
                                method = method,
                                referenceNotes = notes,
                                recordedBy = currentAdmin,
                                timestamp = System.currentTimeMillis(),
                                receiptId = "RCP-${inv.invoiceNumber.replace("INV-", "")}-${inv.paymentRecords.size + 1}"
                            )
                            inv.copy(
                                amountPaid = newPaid,
                                balanceDue = newBal,
                                paymentStatus = newStatus,
                                paymentRecords = inv.paymentRecords + newRecord
                            )
                        } else inv
                    }

                    val updatedSelected = updatedInvoices.firstOrNull { it.id == invoiceId }
                    current.copy(
                        isActionInProgress = false,
                        invoices = updatedInvoices,
                        selectedInvoice = updatedSelected,
                        successMessage = "Payment of ₹$validAmount recorded successfully"
                    )
                }
                applyFilters()
            } else {
                _uiState.update {
                    it.copy(isActionInProgress = false, errorMessage = "Failed to record payment.")
                }
            }
        }
    }

    /**
     * Update invoice payment status manually if needed (e.g. Refunded)
     */
    fun updatePaymentStatus(invoiceId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val target = _uiState.value.invoices.firstOrNull { it.id == invoiceId }
            if (target != null) {
                val updated = target.copy(paymentStatus = newStatus)
                repository.createOrUpdateInvoice(updated)

                _uiState.update { current ->
                    val updatedList = current.invoices.map { if (it.id == invoiceId) updated else it }
                    current.copy(
                        isActionInProgress = false,
                        invoices = updatedList,
                        selectedInvoice = if (current.selectedInvoice?.id == invoiceId) updated else current.selectedInvoice,
                        successMessage = "Status updated to $newStatus"
                    )
                }
                applyFilters()
            }
        }
    }

    /**
     * Share or open Invoice PDF
     */
    fun shareInvoicePdf(context: Context, invoice: InvoiceDto) {
        val pdfFile = InvoicePdfGenerator.generateInvoicePdf(context, invoice)
        if (pdfFile != null && pdfFile.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Royal Studio Invoice #${invoice.invoiceNumber}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
        } else {
            _uiState.update { it.copy(errorMessage = "Could not generate Invoice PDF file") }
        }
    }

    /**
     * Share or open Receipt PDF
     */
    fun shareReceiptPdf(context: Context, invoice: InvoiceDto, payment: PaymentRecordDto) {
        val pdfFile = InvoicePdfGenerator.generateReceiptPdf(context, invoice, payment)
        if (pdfFile != null && pdfFile.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Royal Studio Payment Receipt #${payment.receiptId}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Receipt PDF"))
        } else {
            _uiState.update { it.copy(errorMessage = "Could not generate Receipt PDF file") }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    private fun getMockInvoices(): List<InvoiceDto> {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        return listOf(
            InvoiceDto(
                id = "INV_DEMO_1",
                invoiceNumber = "INV-2026-7812",
                bookingId = "RS-2026-C104",
                bookingReference = "RS-2026-C104",
                customerId = "cust_simran",
                customerName = "Simran & Kabir Varma",
                customerPhone = "+91 98111 22334",
                customerEmail = "kabir.varma@example.com",
                eventType = "Wedding",
                eventDate = now + oneDay * 45,
                eventLocation = "ITC Grand Bharat, Gurugram",
                selectedPackage = "Royal Imperial Wedding",
                subtotal = 350000.0,
                additionalCharges = 35000.0,
                discount = 25000.0,
                taxRate = 18.0,
                taxAmount = 64800.0,
                totalAmount = 424800.0,
                amountPaid = 150000.0,
                balanceDue = 274800.0,
                paymentStatus = "Partially Paid",
                issueDate = now - oneDay * 5,
                dueDate = now + oneDay * 30,
                paymentRecords = listOf(
                    PaymentRecordDto(
                        paymentId = "PAY-1001",
                        amount = 150000.0,
                        method = "Bank Transfer",
                        referenceNotes = "Token advance for date reservation",
                        recordedBy = "admin@royalstudio.com",
                        timestamp = now - oneDay * 4,
                        receiptId = "RCP-7812-1"
                    )
                ),
                notes = "Token advance paid. Balance due 15 days before main wedding ceremony."
            ),
            InvoiceDto(
                id = "INV_DEMO_2",
                invoiceNumber = "INV-2026-4419",
                bookingId = "RS-2026-M419",
                bookingReference = "RS-2026-M419",
                customerId = "cust_pooja",
                customerName = "Pooja Malhotra",
                customerPhone = "+91 91234 56789",
                customerEmail = "pooja.m@example.com",
                eventType = "Pre-Wedding",
                eventDate = now + oneDay * 12,
                eventLocation = "Nahargarh Fort & Jal Mahal, Jaipur",
                selectedPackage = "Cinematic Romance Package",
                subtotal = 125000.0,
                additionalCharges = 15000.0,
                discount = 0.0,
                taxRate = 0.0,
                taxAmount = 0.0,
                totalAmount = 140000.0,
                amountPaid = 140000.0,
                balanceDue = 0.0,
                paymentStatus = "Paid",
                issueDate = now - oneDay * 10,
                dueDate = now + oneDay * 2,
                paymentRecords = listOf(
                    PaymentRecordDto(
                        paymentId = "PAY-2001",
                        amount = 70000.0,
                        method = "UPI",
                        referenceNotes = "50% advance booking",
                        recordedBy = "admin@royalstudio.com",
                        timestamp = now - oneDay * 8,
                        receiptId = "RCP-4419-1"
                    ),
                    PaymentRecordDto(
                        paymentId = "PAY-2002",
                        amount = 70000.0,
                        method = "Bank Transfer",
                        referenceNotes = "Full balance clearance",
                        recordedBy = "admin@royalstudio.com",
                        timestamp = now - oneDay * 2,
                        receiptId = "RCP-4419-2"
                    )
                ),
                notes = "Full payment received. Permits for Jaipur shoot cleared."
            ),
            InvoiceDto(
                id = "INV_DEMO_3",
                invoiceNumber = "INV-2026-8921",
                bookingId = "RS-2026-X892",
                bookingReference = "RS-2026-X892",
                customerId = "cust_rohan",
                customerName = "Ananya & Rohan Sharma",
                customerPhone = "+91 98765 43210",
                customerEmail = "rohan.sharma@example.com",
                eventType = "Wedding",
                eventDate = now + oneDay * 24,
                eventLocation = "The Leela Palace, Udaipur",
                selectedPackage = "Royal Imperial Wedding",
                subtotal = 400000.0,
                additionalCharges = 0.0,
                discount = 0.0,
                taxRate = 0.0,
                taxAmount = 0.0,
                totalAmount = 400000.0,
                amountPaid = 0.0,
                balanceDue = 400000.0,
                paymentStatus = "Unpaid",
                issueDate = now - oneDay * 1,
                dueDate = now + oneDay * 10,
                paymentRecords = emptyList(),
                notes = "Quotation accepted; awaiting token payment confirmation."
            )
        )
    }
}
