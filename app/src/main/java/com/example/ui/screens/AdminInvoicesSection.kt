package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.InvoiceDto
import com.example.data.models.PaymentRecordDto
import com.example.ui.theme.RoyalGold
import com.example.ui.viewmodels.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminInvoicesSection(
    viewModel: InvoiceViewModel = viewModel(),
    onBackToDashboard: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.selectedInvoice != null) {
        InvoiceDetailView(
            invoice = state.selectedInvoice!!,
            isActionInProgress = state.isActionInProgress,
            successMessage = state.successMessage,
            errorMessage = state.errorMessage,
            onBack = { viewModel.selectInvoice(null) },
            onRecordPayment = { amount, method, notes ->
                viewModel.recordPayment(state.selectedInvoice!!.id, amount, method, notes)
            },
            onUpdateStatus = { newStatus ->
                viewModel.updatePaymentStatus(state.selectedInvoice!!.id, newStatus)
            },
            onSharePdf = {
                viewModel.shareInvoicePdf(context, state.selectedInvoice!!)
            },
            onShareReceiptPdf = { payment ->
                viewModel.shareReceiptPdf(context, state.selectedInvoice!!, payment)
            },
            onDismissMessages = { viewModel.dismissMessages() }
        )
    } else {
        InvoicesListView(
            invoices = state.filteredInvoices,
            totalCount = state.invoices.size,
            selectedFilter = state.selectedStatusFilter,
            searchQuery = state.searchQuery,
            isLoading = state.isLoading,
            onSelectFilter = { viewModel.setStatusFilter(it) },
            onSearchChange = { viewModel.setSearchQuery(it) },
            onSelectInvoice = { viewModel.selectInvoice(it) },
            onRefresh = { viewModel.loadInvoices() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoicesListView(
    invoices: List<InvoiceDto>,
    totalCount: Int,
    selectedFilter: String,
    searchQuery: String,
    isLoading: Boolean,
    onSelectFilter: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectInvoice: (InvoiceDto) -> Unit,
    onRefresh: () -> Unit
) {
    val filters = listOf(
        "ALL" to "All",
        "Unpaid" to "Unpaid",
        "Partially Paid" to "Partially Paid",
        "Paid" to "Paid",
        "Refunded" to "Refunded"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Search & Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search invoice #, customer, ref...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectFilter(key) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Showing ${invoices.size} of $totalCount billing records",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (invoices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No invoices found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(invoices, key = { it.id }) { item ->
                    InvoiceCard(
                        invoice = item,
                        onClick = { onSelectInvoice(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: InvoiceDto,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val (statusBg, statusFg) = getPaymentStatusColors(invoice.paymentStatus)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = invoice.paymentStatus.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = invoice.customerName.ifBlank { "Client" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${invoice.eventType} • Ref #${invoice.bookingReference}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "₹ %,.2f".format(Locale.getDefault(), invoice.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (invoice.balanceDue > 0) "BALANCE DUE" else "SETTLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (invoice.balanceDue > 0) Color(0xFFdc3232) else Color(0xFF2ecc71)
                    )
                    Text(
                        text = "₹ %,.2f".format(Locale.getDefault(), invoice.balanceDue),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.balanceDue > 0) Color(0xFFdc3232) else Color(0xFF2ecc71)
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailView(
    invoice: InvoiceDto,
    isActionInProgress: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onBack: () -> Unit,
    onRecordPayment: (Double, String, String) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onSharePdf: () -> Unit,
    onShareReceiptPdf: (PaymentRecordDto) -> Unit,
    onDismissMessages: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val receiptDateFormatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Top Back Row & Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Invoices",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onSharePdf,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Invoice", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success / Error Alerts
        if (successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2ecc71).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2ecc71), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = successMessage, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Invoice Header & Status Card
        val (statusBg, statusFg) = getPaymentStatusColors(invoice.paymentStatus)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INVOICE NUMBER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = invoice.invoiceNumber,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = invoice.paymentStatus.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Record Payment & Status Update
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isActionInProgress
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment")
                    }

                    OutlinedButton(
                        onClick = { showStatusDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isActionInProgress
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Status")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Billed To & Event Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Billed Customer & Event Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                InvoiceInfoRow(label = "Client Name", value = invoice.customerName)
                InvoiceInfoRow(label = "Contact Phone", value = invoice.customerPhone.ifBlank { "Not provided" })
                InvoiceInfoRow(label = "Email Address", value = invoice.customerEmail.ifBlank { "Not provided" })
                InvoiceInfoRow(label = "Booking Ref", value = invoice.bookingReference)
                InvoiceInfoRow(label = "Event Type", value = invoice.eventType)
                InvoiceInfoRow(
                    label = "Scheduled Date",
                    value = if (invoice.eventDate > 0) dateFormatter.format(Date(invoice.eventDate)) else "TBD"
                )
                InvoiceInfoRow(label = "Venue Location", value = invoice.eventLocation.ifBlank { "TBD" })
                InvoiceInfoRow(label = "Package / Bundle", value = invoice.selectedPackage.ifBlank { "Custom" })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Line Items & Cost Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Itemized Financial Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Line items
                if (invoice.lineItems.isNotEmpty()) {
                    invoice.lineItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = "₹ %,.2f".format(Locale.getDefault(), item.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.amount < 0) Color(0xFFdc3232) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                InvoiceInfoRow(label = "Subtotal", value = "₹ %,.2f".format(Locale.getDefault(), invoice.subtotal))
                if (invoice.additionalCharges > 0) {
                    InvoiceInfoRow(label = "Additional Charges", value = "₹ %,.2f".format(Locale.getDefault(), invoice.additionalCharges))
                }
                if (invoice.discount > 0) {
                    InvoiceInfoRow(
                        label = "Discount",
                        value = "- ₹ %,.2f".format(Locale.getDefault(), invoice.discount),
                        valueColor = Color(0xFFdc3232)
                    )
                }
                if (invoice.taxRate > 0) {
                    InvoiceInfoRow(
                        label = "Tax (${invoice.taxRate}%)",
                        value = "₹ %,.2f".format(Locale.getDefault(), invoice.taxAmount)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), thickness = 1.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Invoice Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "₹ %,.2f".format(Locale.getDefault(), invoice.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                InvoiceInfoRow(
                    label = "Amount Paid to Date",
                    value = "₹ %,.2f".format(Locale.getDefault(), invoice.amountPaid),
                    valueColor = Color(0xFF2ecc71)
                )
                InvoiceInfoRow(
                    label = "Remaining Balance Due",
                    value = "₹ %,.2f".format(Locale.getDefault(), invoice.balanceDue),
                    valueColor = if (invoice.balanceDue > 0) Color(0xFFdc3232) else Color(0xFF2ecc71)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Records & Receipts History
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Receipts (${invoice.paymentRecords.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (invoice.paymentRecords.isEmpty()) {
                    Text(
                        text = "No payments have been recorded for this invoice yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    invoice.paymentRecords.forEachIndexed { index, record ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = record.receiptId.ifBlank { "Receipt #${index + 1}" },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = receiptDateFormatter.format(Date(record.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = { onShareReceiptPdf(record) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Receipt PDF", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Method: ${record.method} (By: ${record.recordedBy.take(15)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "₹ %,.2f".format(Locale.getDefault(), record.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2ecc71)
                                    )
                                }

                                if (record.referenceNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Note: ${record.referenceNotes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        var paymentAmountStr by remember { mutableStateOf(invoice.balanceDue.toString()) }
        var selectedMethod by remember { mutableStateOf("Bank Transfer") }
        var paymentNotes by remember { mutableStateOf("") }
        val paymentMethods = listOf("Bank Transfer", "UPI", "Cash", "Card", "Cheque")

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Record Payment for ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Outstanding Balance Due: ₹ %,.2f".format(Locale.getDefault(), invoice.balanceDue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3232)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = paymentAmountStr,
                        onValueChange = { paymentAmountStr = it },
                        label = { Text("Payment Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(paymentMethods) { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = paymentNotes,
                        onValueChange = { paymentNotes = it },
                        label = { Text("Transaction Reference / Cheque / Notes") },
                        placeholder = { Text("e.g. UTR #9828114, Advance token") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentAmountStr.toDoubleOrNull() ?: 0.0
                        val safeAmt = amt.coerceIn(0.0, invoice.balanceDue)
                        val safeNotes = paymentNotes.trim().take(500)
                        if (safeAmt > 0) {
                            showPaymentDialog = false
                            onRecordPayment(safeAmt, selectedMethod, safeNotes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save & Generate Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Status Dialog
    if (showStatusDialog) {
        val statuses = listOf("Unpaid", "Partially Paid", "Paid", "Refunded")
        var chosenStatus by remember { mutableStateOf(invoice.paymentStatus) }

        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Update Payment Status", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    statuses.forEach { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chosenStatus = st }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = chosenStatus == st,
                                onClick = { chosenStatus = st },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStatusDialog = false
                        onUpdateStatus(chosenStatus)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InvoiceInfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

fun getPaymentStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "paid" -> Color(0xFF2ecc71).copy(alpha = 0.2f) to Color(0xFF27ae60)
        "partially paid" -> Color(0xFFe67e22).copy(alpha = 0.2f) to Color(0xFFd35400)
        "unpaid" -> Color(0xFFe74c3c).copy(alpha = 0.2f) to Color(0xFFc0392b)
        "refunded" -> Color(0xFF9b59b6).copy(alpha = 0.2f) to Color(0xFF8e44ad)
        else -> Color.Gray.copy(alpha = 0.2f) to Color.Gray
    }
}
