package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.BookingDto
import com.example.ui.viewmodels.AdminBookingsViewModel
import com.example.ui.viewmodels.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminBookingsSection(
    viewModel: AdminBookingsViewModel = viewModel(),
    invoiceViewModel: InvoiceViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.selectedBooking != null) {
        AdminBookingDetailView(
            booking = state.selectedBooking!!,
            invoiceViewModel = invoiceViewModel,
            isUpdating = state.isUpdating,
            conflictWarning = state.conflictWarning,
            statusMessage = state.statusMessage,
            errorMessage = state.errorMessage,
            onBack = { viewModel.selectBooking(null) },
            onUpdateStatus = { newStatus, note ->
                viewModel.updateStatus(state.selectedBooking!!.id, newStatus, note)
            },
            onSaveNotes = { notes ->
                viewModel.saveInternalNotes(state.selectedBooking!!.id, notes)
            },
            onUpdateDetails = { type, date, loc, pkg ->
                viewModel.updateEventDetails(state.selectedBooking!!.id, type, date, loc, pkg)
            },
            onDismissMessages = { viewModel.dismissMessages() }
        )
    } else {
        AdminBookingsListView(
            bookings = state.filteredBookings,
            totalCount = state.bookings.size,
            selectedFilter = state.selectedFilter,
            searchQuery = state.searchQuery,
            isLoading = state.isLoading,
            onSelectFilter = { viewModel.setFilter(it) },
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onSelectBooking = { viewModel.selectBooking(it) },
            onRefresh = { viewModel.loadBookings() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminBookingsListView(
    bookings: List<BookingDto>,
    totalCount: Int,
    selectedFilter: String,
    searchQuery: String,
    isLoading: Boolean,
    onSelectFilter: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectBooking: (BookingDto) -> Unit,
    onRefresh: () -> Unit
) {
    val filters = listOf(
        "ALL" to "All",
        "NEW" to "New Enquiries",
        "CONTACTED" to "Contacted",
        "QUOTATION_SENT" to "Quotes",
        "CONFIRMED" to "Confirmed",
        "COMPLETED" to "Completed",
        "CANCELLED" to "Cancelled"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Search & Refresh Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search by name, reference ID, phone...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
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

        // Status filter chips
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

        // Header Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${bookings.size} of $totalCount enquiries & events",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No records match criteria",
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
                items(bookings, key = { it.id }) { item ->
                    AdminBookingCard(
                        booking = item,
                        onClick = { onSelectBooking(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminBookingCard(
    booking: BookingDto,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = if (booking.eventDate > 0) dateFormatter.format(Date(booking.eventDate)) else "Date pending"

    val (badgeBg, badgeFg) = getStatusColors(booking.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Reference & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.referenceId.ifBlank { booking.id },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = booking.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Event Type
            Text(
                text = booking.customerName.ifBlank { "Anonymous Client" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${booking.eventType} • ${booking.packageId.ifBlank { "Standard Service" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            // Date & Location Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.eventLocation.ifBlank { "Location TBD" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminBookingDetailView(
    booking: BookingDto,
    invoiceViewModel: InvoiceViewModel,
    isUpdating: Boolean,
    conflictWarning: String?,
    statusMessage: String?,
    errorMessage: String?,
    onBack: () -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onSaveNotes: (String) -> Unit,
    onUpdateDetails: (String, Long, String, String) -> Unit,
    onDismissMessages: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val auditDateFormatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    var currentNotes by remember(booking.id) { mutableStateOf(booking.adminNotes) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedNewStatus by remember { mutableStateOf(booking.status) }
    var statusChangeNote by remember { mutableStateOf("") }
    var showEditDetailsDialog by remember { mutableStateOf(false) }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Back Button & Action Bar
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
                    text = "Enquiries & Bookings",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { showEditDetailsDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Event Details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification / Conflict Banners
        if (conflictWarning != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = conflictWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (statusMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2ecc71).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2ecc71),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Header Card with Primary Actions
        val (badgeBg, badgeFg) = getStatusColors(booking.status)
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
                            text = "REFERENCE NUMBER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = booking.referenceId.ifBlank { booking.id },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = booking.status,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change Status & Invoice Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            selectedNewStatus = booking.status
                            statusChangeNote = ""
                            showStatusDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Status")
                        }
                    }

                    OutlinedButton(
                        onClick = { showCreateInvoiceDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invoice")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact & Customer Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = booking.customerName.ifBlank { "Not provided" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = booking.phone.ifBlank { "No phone" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (booking.phone.isNotBlank()) {
                        FilledTonalButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.phone}"))
                                context.startActivity(dialIntent)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = booking.email.ifBlank { "No email" },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (booking.email.isNotBlank()) {
                        FilledTonalButton(
                            onClick = {
                                val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${booking.email}")
                                    putExtra(Intent.EXTRA_SUBJECT, "Royal Studio: Booking Reference ${booking.referenceId.ifBlank { booking.id }}")
                                }
                                context.startActivity(mailIntent)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Email", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Event & Service Requirements
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Event Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "Event Type", value = booking.eventType)
                DetailItem(label = "Scheduled Date", value = if (booking.eventDate > 0) dateFormatter.format(Date(booking.eventDate)) else "Not set")
                DetailItem(label = "Venue / Location", value = booking.eventLocation.ifBlank { "Not provided" })
                DetailItem(label = "Selected Package", value = booking.packageId.ifBlank { "Custom / Unassigned" })

                if (booking.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Client Message / Notes:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = booking.message,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Private Internal Admin Notes Card (CRITICAL REQUIREMENT)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confidential",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Internal Admin Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Confidential",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Internal remarks, crew assignments, and pricing notes are NEVER visible to the client.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentNotes,
                    onValueChange = { currentNotes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Add private staff notes, photographer assignments, payment status...") },
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSaveNotes(currentNotes) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isUpdating
                ) {
                    Text("Save Internal Notes")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audit Trail History
        if (booking.auditTrail.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Lifecycle Audit Trail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    booking.auditTrail.reversed().forEachIndexed { index, audit ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                if (index < booking.auditTrail.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(48.dp)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = audit.status,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = auditDateFormatter.format(Date(audit.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = "By: ${audit.updatedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                if (audit.note.isNotBlank()) {
                                    Text(
                                        text = audit.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Status Change Dialog
    if (showStatusDialog) {
        val statuses = listOf(
            "NEW" to "New Enquiry",
            "CONTACTED" to "Contacted Customer",
            "QUOTATION_SENT" to "Quotation Sent",
            "CONFIRMED" to "Confirmed Booking",
            "COMPLETED" to "Completed Event",
            "CANCELLED" to "Cancelled Booking"
        )

        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = {
                Text("Update Lifecycle Status", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Select the new lifecycle state for #${booking.referenceId.ifBlank { booking.id }}:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    statuses.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNewStatus = code }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedNewStatus == code,
                                onClick = { selectedNewStatus = code },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedNewStatus == code) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = statusChangeNote,
                        onValueChange = { statusChangeNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Audit Note (Optional)") },
                        placeholder = { Text("e.g. Deposit verified; date locked") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStatusDialog = false
                        onUpdateStatus(selectedNewStatus, statusChangeNote)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply Transition")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Event Details Dialog
    if (showEditDetailsDialog) {
        var editType by remember { mutableStateOf(booking.eventType) }
        var editLocation by remember { mutableStateOf(booking.eventLocation) }
        var editPackage by remember { mutableStateOf(booking.packageId) }

        AlertDialog(
            onDismissRequest = { showEditDetailsDialog = false },
            title = {
                Text("Edit Event Information", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = editType,
                        onValueChange = { editType = it },
                        label = { Text("Event Type") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Venue / Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPackage,
                        onValueChange = { editPackage = it },
                        label = { Text("Package / Service") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditDetailsDialog = false
                        onUpdateDetails(editType, booking.eventDate, editLocation, editPackage)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDetailsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Generate Invoice Dialog
    if (showCreateInvoiceDialog) {
        var subtotalStr by remember { mutableStateOf("150000") }
        var additionalChargesStr by remember { mutableStateOf("0") }
        var discountStr by remember { mutableStateOf("0") }
        var taxRateStr by remember { mutableStateOf("0") }
        var invoiceNotes by remember { mutableStateOf("Booking reference ${booking.referenceId.ifBlank { booking.id }}. Advance token due on invoice issuance.") }

        AlertDialog(
            onDismissRequest = { showCreateInvoiceDialog = false },
            title = { Text("Generate Studio Invoice", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Configurable billing for #${booking.referenceId.ifBlank { booking.id }} (${booking.customerName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subtotalStr,
                        onValueChange = { subtotalStr = it },
                        label = { Text("Base Package Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = additionalChargesStr,
                        onValueChange = { additionalChargesStr = it },
                        label = { Text("Additional Charges / Extra Services (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Discount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taxRateStr,
                        onValueChange = { taxRateStr = it },
                        label = { Text("Tax Rate % (0 for none, 18 for GST if applicable)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = invoiceNotes,
                        onValueChange = { invoiceNotes = it },
                        label = { Text("Terms & Payment Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Calculation preview
                    val sub = subtotalStr.toDoubleOrNull() ?: 0.0
                    val add = additionalChargesStr.toDoubleOrNull() ?: 0.0
                    val disc = discountStr.toDoubleOrNull() ?: 0.0
                    val rate = taxRateStr.toDoubleOrNull() ?: 0.0
                    val taxable = (sub + add - disc).coerceAtLeast(0.0)
                    val tax = if (rate > 0) (taxable * rate / 100.0) else 0.0
                    val total = taxable + tax

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Estimated Total: ₹ %,.2f".format(Locale.getDefault(), total),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (tax > 0) {
                                Text(
                                    text = "Includes ₹ %,.2f tax (${rate}%)".format(Locale.getDefault(), tax),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sub = (subtotalStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100_000_000.0)
                        val add = (additionalChargesStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100_000_000.0)
                        val disc = (discountStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100_000_000.0)
                        val rate = (taxRateStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
                        val notes = invoiceNotes.trim().take(1000)

                        showCreateInvoiceDialog = false
                        invoiceViewModel.generateInvoiceForBooking(
                            booking = booking,
                            subtotalAmount = sub,
                            additionalCharges = add,
                            discountAmount = disc,
                            taxRate = rate,
                            notes = notes
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Create Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateInvoiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getStatusColors(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "NEW" -> Color(0xFF3498db).copy(alpha = 0.2f) to Color(0xFF2980b9)
        "CONTACTED" -> Color(0xFF9b59b6).copy(alpha = 0.2f) to Color(0xFF8e44ad)
        "QUOTATION_SENT" -> Color(0xFFe67e22).copy(alpha = 0.2f) to Color(0xFFd35400)
        "CONFIRMED" -> Color(0xFF2ecc71).copy(alpha = 0.2f) to Color(0xFF27ae60)
        "COMPLETED" -> Color(0xFFD4AF37).copy(alpha = 0.25f) to Color(0xFFB8860B)
        "CANCELLED" -> Color(0xFFe74c3c).copy(alpha = 0.2f) to Color(0xFFc0392b)
        else -> Color.Gray.copy(alpha = 0.2f) to Color.Gray
    }
}
