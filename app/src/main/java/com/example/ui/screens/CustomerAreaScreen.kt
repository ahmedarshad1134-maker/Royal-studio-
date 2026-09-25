package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.models.BookingStatus
import com.example.ui.models.CustomerBookingData
import com.example.ui.models.GalleryStatus
import com.example.ui.navigation.Screen
import com.example.ui.viewmodels.CustomerAreaViewModel
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.components.NotificationSheetContent
import com.example.ui.viewmodels.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerAreaScreen(
    navController: NavController,
    viewModel: CustomerAreaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isAuthenticated) {
        val booking = uiState.bookingData
        if (booking != null) {
            CustomerDashboardScreen(
                customerName = uiState.customerName,
                bookingData = booking,
                onLogout = { viewModel.logout() },
                onContactSupport = { navController.navigate(Screen.Contact.route) },
                onOpenGallery = { navController.navigate(Screen.PrivateGallery.createRoute(booking.id)) }
            )
        } else {
            CustomerNoBookingsScreen(
                customerName = uiState.customerName,
                onBookNow = { navController.navigate(Screen.Booking.route) },
                onLogout = { viewModel.logout() },
                onBack = { navController.popBackStack() }
            )
        }
    } else {
        CustomerLoginScreen(
            isAuthenticating = uiState.isAuthenticating,
            authError = uiState.authError,
            isSignUpMode = uiState.isSignUpMode,
            onLogin = { email, password, name -> viewModel.authenticate(email, password, name) },
            onToggleMode = { viewModel.toggleMode() },
            onBack = { navController.popBackStack() },
            onClearError = { viewModel.clearError() }
        )
    }
}

@Composable
private fun CustomerNoBookingsScreen(
    customerName: String,
    onBookNow: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = onLogout,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome, $customerName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No active bookings or enquiries found for this account. When you submit an enquiry, your dates, timeline, invoices, and deliverables will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBookNow,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Book a Session / Enquire Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CustomerLoginScreen(
    isAuthenticating: Boolean,
    authError: String?,
    isSignUpMode: Boolean,
    onLogin: (String, String, String) -> Unit,
    onToggleMode: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isSignUpMode) "Create Account" else "Client Portal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSignUpMode) "Sign up to track your event and deliverables." else "Enter your details to view your event status, updates, and deliverables.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            if (isSignUpMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; onClearError() },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onClearError() },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onClearError() },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            
            if (authError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onLogin(email, password, name) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isAuthenticating
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isSignUpMode) "Sign Up" else "Access Dashboard", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.material3.TextButton(onClick = onToggleMode) {
                Text(
                    text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDashboardScreen(
    customerName: String,
    bookingData: CustomerBookingData,
    onLogout: () -> Unit,
    onContactSupport: () -> Unit,
    onOpenGallery: () -> Unit,
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    var showNotifications by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    // Load notifications for this booking
    androidx.compose.runtime.LaunchedEffect(bookingData.referenceId) {
        notificationViewModel.loadCustomerNotifications(bookingData.referenceId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showNotifications = true },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    BadgedBox(
                        badge = {
                            if (notificationState.unreadCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(notificationState.unreadCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Status Badge
            StatusBadge(status = bookingData.status)

            // Booking Summary Card
            BookingSummaryCard(bookingData)

            // Invoice & Billing (Only authorized to the customer's own booking)
            if (bookingData.invoice != null) {
                CustomerInvoiceCard(invoice = bookingData.invoice)
            }

            // Media / Deliverables Area
            MediaDeliveryCard(galleryStatus = bookingData.galleryStatus, onOpenGallery = onOpenGallery)

            // Updates Timeline
            UpdatesTimeline(updates = bookingData.updates)
            
            // Support Card
            SupportCard(onContactSupport = onContactSupport)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    if (showNotifications) {
        ModalBottomSheet(
            onDismissRequest = { showNotifications = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null
        ) {
            NotificationSheetContent(
                title = "Your Notifications",
                isLoading = notificationState.isLoading,
                notifications = notificationState.notifications,
                onMarkAsRead = { notificationViewModel.markAsRead(it) },
                onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showNotifications = false
                    }
                }
            )
        }
    }
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    val (statusText, statusColor) = when(status) {
        BookingStatus.NEW -> "Enquiry Received" to Color(0xFF3498db)
        BookingStatus.CONTACTED -> "Contacted" to Color(0xFF9b59b6)
        BookingStatus.QUOTATION_SENT -> "Quotation Sent" to Color(0xFFe67e22)
        BookingStatus.CONFIRMED -> "Confirmed" to Color(0xFF2ecc71)
        BookingStatus.COMPLETED -> "Completed" to MaterialTheme.colorScheme.primary
        BookingStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Status: $statusText",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

@Composable
private fun BookingSummaryCard(data: CustomerBookingData) {
    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Event Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "#${data.referenceId}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            SummaryRow(icon = Icons.Default.Event, label = "Event Type", value = data.eventType)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            SummaryRow(icon = Icons.Default.Event, label = "Date", value = dateFormatter.format(Date(data.eventDate)))
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            SummaryRow(icon = Icons.Default.LocationOn, label = "Location", value = data.location)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            SummaryRow(icon = Icons.Default.ShoppingBag, label = "Package", value = data.selectedPackage)
        }
    }
}

@Composable
private fun SummaryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MediaDeliveryCard(galleryStatus: GalleryStatus, onOpenGallery: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Private Gallery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val (icon, tint, message) = when(galleryStatus) {
                GalleryStatus.UNAVAILABLE -> Triple(Icons.Default.Lock, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), "Your gallery will appear here after the event.")
                GalleryStatus.PROCESSING -> Triple(Icons.Default.PhotoLibrary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), "We are currently processing and editing your media. Check back soon!")
                GalleryStatus.READY -> Triple(Icons.Default.PhotoLibrary, MaterialTheme.colorScheme.primary, "Your media is ready to view and download.")
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            if (galleryStatus == GalleryStatus.READY) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOpenGallery,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Secure Gallery")
                }
            }
        }
    }
}

@Composable
private fun UpdatesTimeline(updates: List<com.example.ui.models.BookingUpdate>) {
    val dateFormatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Updates & Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (updates.isEmpty()) {
                Text(
                    text = "No updates yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                updates.forEachIndexed { index, update ->
                    Row {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            if (index < updates.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(60.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.padding(bottom = if (index < updates.size - 1) 24.dp else 0.dp)) {
                            Text(
                                text = update.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dateFormatter.format(Date(update.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = update.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportCard(onContactSupport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Need Assistance?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Contact your dedicated event coordinator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            OutlinedButton(onClick = onContactSupport) {
                Text("Contact")
            }
        }
    }
}

@Composable
fun CustomerInvoiceCard(
    invoice: com.example.data.models.InvoiceDto
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val (statusBg, statusFg) = getPaymentStatusColors(invoice.paymentStatus)
    var showReceiptsSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Billing & Invoice",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = invoice.paymentStatus.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Package Cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "₹ %,.2f".format(Locale.getDefault(), invoice.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (invoice.balanceDue > 0) "Balance Due" else "Settled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (invoice.balanceDue > 0) Color(0xFFdc3232) else Color(0xFF2ecc71)
                    )
                    Text(
                        text = "₹ %,.2f".format(Locale.getDefault(), invoice.balanceDue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.balanceDue > 0) Color(0xFFdc3232) else Color(0xFF2ecc71)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Paid so far
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount Settled to Date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "₹ %,.2f".format(Locale.getDefault(), invoice.amountPaid),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2ecc71)
                )
            }

            if (invoice.dueDate > 0 && invoice.balanceDue > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Balance Due Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = dateFormatter.format(Date(invoice.dueDate)),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Download PDF / View Receipts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val pdfFile = com.example.util.InvoicePdfGenerator.generateInvoicePdf(context, invoice)
                        if (pdfFile != null && pdfFile.exists()) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                pdfFile
                            )
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Royal Studio Invoice #${invoice.invoiceNumber}")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Invoice PDF"))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invoice PDF")
                }

                if (invoice.paymentRecords.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showReceiptsSheet = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipts (${invoice.paymentRecords.size})")
                    }
                }
            }
        }
    }

    if (showReceiptsSheet) {
        val receiptDateFormatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { showReceiptsSheet = false },
            title = { Text("Payment Receipts", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    invoice.paymentRecords.forEach { rec ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = rec.receiptId,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = receiptDateFormatter.format(Date(rec.timestamp)),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val pdfFile = com.example.util.InvoicePdfGenerator.generateReceiptPdf(context, invoice, rec)
                                            if (pdfFile != null && pdfFile.exists()) {
                                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    pdfFile
                                                )
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Royal Studio Payment Receipt #${rec.receiptId}")
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Share Receipt PDF"))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share Receipt", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Via ${rec.method}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "₹ %,.2f".format(Locale.getDefault(), rec.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2ecc71)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReceiptsSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}
