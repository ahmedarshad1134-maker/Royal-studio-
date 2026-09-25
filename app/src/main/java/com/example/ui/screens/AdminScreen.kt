package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import com.example.ui.viewmodels.AdminSection
import com.example.ui.viewmodels.AdminUiState
import com.example.ui.viewmodels.AdminViewModel

@Composable
fun AdminScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (!uiState.isAuthenticated) {
        AdminLoginScreen(
            isAuthenticating = uiState.isAuthenticating,
            authError = uiState.authError,
            onLogin = { email, password -> viewModel.login(email, password) },
            onBack = { navController.popBackStack() },
            onClearError = { viewModel.clearError() }
        )
    } else {
        AdminDashboardLayout(
            uiState = uiState,
            onNavigate = { viewModel.navigateTo(it) },
            onLogout = { viewModel.logout() }
        )
    }
}

@Composable
private fun AdminLoginScreen(
    isAuthenticating: Boolean,
    authError: String?,
    onLogin: (String, String) -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
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
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Secure Admin Portal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Restricted Access. Authorized Personnel Only.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onClearError() },
                label = { Text("Admin Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onClearError() },
                label = { Text("Admin Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            
            if (authError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onLogin(email, password) },
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
                    Text("Authenticate", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "If backend not initialized, use 'admin123' for demonstration",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardLayout(
    uiState: AdminUiState,
    onNavigate: (AdminSection) -> Unit,
    onLogout: () -> Unit,
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val notificationState by notificationViewModel.uiState.collectAsState()
    var showNotifications by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    // Load admin notifications
    androidx.compose.runtime.LaunchedEffect(Unit) {
        notificationViewModel.loadAdminNotifications()
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.currentSection != AdminSection.DASHBOARD) {
                    IconButton(
                        onClick = { onNavigate(AdminSection.DASHBOARD) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = uiState.currentSection.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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

        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.currentSection) {
                AdminSection.DASHBOARD -> DashboardContent(uiState, onNavigate)
                AdminSection.BOOKINGS -> AdminBookingsSection()
                AdminSection.INVOICES -> AdminInvoicesSection(onBackToDashboard = { onNavigate(AdminSection.DASHBOARD) })
                AdminSection.PORTFOLIO -> AdminPortfolioSection()
                AdminSection.SERVICES -> PlaceholderAdminView(uiState.currentSection.title, "Configure offered photography/videography services.")
                AdminSection.PACKAGES -> PlaceholderAdminView(uiState.currentSection.title, "Manage pricing and feature bundles.")
                AdminSection.REVIEWS -> PlaceholderAdminView(uiState.currentSection.title, "Moderate client testimonials.")
                AdminSection.SETTINGS -> PlaceholderAdminView(uiState.currentSection.title, "Update studio contact and public information.")
            }
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
                title = "Admin Alerts",
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
private fun DashboardContent(uiState: AdminUiState, onNavigate: (AdminSection) -> Unit) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Business Overview",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "New Enquiries",
                value = uiState.newEnquiriesCount.toString(),
                icon = Icons.Default.EventNote,
                color = Color(0xFF3498db)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Upcoming Events",
                value = uiState.upcomingEventsCount.toString(),
                icon = Icons.Default.CameraAlt,
                color = Color(0xFF9b59b6)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Confirmed",
                value = uiState.confirmedBookingsCount.toString(),
                icon = Icons.Default.BookOnline,
                color = Color(0xFF2ecc71)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Pending Reviews",
                value = uiState.pendingReviewsCount.toString(),
                icon = Icons.Default.RateReview,
                color = Color(0xFFe67e22)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Management Modules",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ModuleCard(title = AdminSection.BOOKINGS.title, icon = Icons.Default.BookOnline, onClick = { onNavigate(AdminSection.BOOKINGS) })
            ModuleCard(title = AdminSection.INVOICES.title, icon = Icons.Default.EventNote, onClick = { onNavigate(AdminSection.INVOICES) })
            ModuleCard(title = AdminSection.PORTFOLIO.title, icon = Icons.Default.PhotoLibrary, onClick = { onNavigate(AdminSection.PORTFOLIO) })
            ModuleCard(title = AdminSection.SERVICES.title, icon = Icons.Default.Build, onClick = { onNavigate(AdminSection.SERVICES) })
            ModuleCard(title = AdminSection.PACKAGES.title, icon = Icons.Default.LocalMall, onClick = { onNavigate(AdminSection.PACKAGES) })
            ModuleCard(title = AdminSection.REVIEWS.title, icon = Icons.Default.RateReview, onClick = { onNavigate(AdminSection.REVIEWS) })
            ModuleCard(title = AdminSection.SETTINGS.title, icon = Icons.Default.Settings, onClick = { onNavigate(AdminSection.SETTINGS) })
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModuleCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlaceholderAdminView(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "$title Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "This view connects securely to the backend CRM, ensuring data is validated server-side. UI scaffolding prepared for API integration.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
