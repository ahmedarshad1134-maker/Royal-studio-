package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.models.BookingItem
import com.example.ui.viewmodels.BookingUiState
import com.example.ui.viewmodels.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BookingScreen(
    navController: NavController,
    viewModel: BookingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.submittedBooking == null) {
                IconButton(
                    onClick = { 
                        if (uiState.currentStep > 1) viewModel.previousStep() 
                        else navController.popBackStack() 
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (uiState.submittedBooking == null) {
                Text(
                    text = "Step ${uiState.currentStep} of ${uiState.totalSteps}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Progress Bar
        if (uiState.submittedBooking == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
            ) {
                val progress = uiState.currentStep.toFloat() / uiState.totalSteps.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
            if (uiState.submittedBooking != null) {
                BookingConfirmationView(
                    booking = uiState.submittedBooking!!,
                    onTrackEnquiry = {
                        viewModel.resetBooking()
                        navController.navigate("customer_area")
                    },
                    onDone = { 
                        viewModel.resetBooking()
                        navController.navigate("home") {
                            popUpTo(0)
                        } 
                    }
                )
            } else if (uiState.isSubmitting) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Submitting your enquiry...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else {
                AnimatedContent(targetState = uiState.currentStep, label = "booking_steps") { step ->
                    when (step) {
                        1 -> Step1EventType(
                            selectedType = uiState.eventType,
                            error = uiState.errors["eventType"],
                            onSelect = { viewModel.updateEventType(it) },
                            onNext = { viewModel.nextStep() }
                        )
                        2 -> Step2EventDate(
                            selectedDateMillis = uiState.eventDateMillis,
                            error = uiState.errors["eventDate"],
                            onSelect = { viewModel.updateEventDate(it) },
                            onNext = { viewModel.nextStep() }
                        )
                        3 -> Step3Location(
                            location = uiState.location,
                            error = uiState.errors["location"],
                            onChange = { viewModel.updateLocation(it) },
                            onNext = { viewModel.nextStep() }
                        )
                        4 -> Step4Offering(
                            selectedOffering = uiState.selectedOffering,
                            error = uiState.errors["offering"],
                            onSelect = { viewModel.updateSelectedOffering(it) },
                            onNext = { viewModel.nextStep() }
                        )
                        5 -> Step5CustomerInfo(
                            name = uiState.customerName,
                            phone = uiState.phone,
                            email = uiState.email,
                            message = uiState.message,
                            errors = uiState.errors,
                            onChange = { n, p, e, m -> viewModel.updateCustomerInfo(n, p, e, m) },
                            onNext = { viewModel.nextStep() }
                        )
                        6 -> Step6Review(
                            uiState = uiState,
                            onSubmit = { viewModel.submitEnquiry() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Step1EventType(
    selectedType: String,
    error: String?,
    onSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    val eventTypes = listOf("Wedding", "Pre-Wedding", "Engagement", "Birthday", "Anniversary", "Baby/Family", "Corporate", "Other")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "What type of event are you planning?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(eventTypes) { type ->
                val isSelected = selectedType == type
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clickable { onSelect(type) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Next", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2EventDate(
    selectedDateMillis: Long?,
    error: String?,
    onSelect: (Long?) -> Unit,
    onNext: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    
    LaunchedEffect(datePickerState.selectedDateMillis) {
        onSelect(datePickerState.selectedDateMillis)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "When is your event?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Next", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun Step3Location(
    location: String,
    error: String?,
    onChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Where is the event taking place?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "City, Venue, or general area.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = location,
            onValueChange = onChange,
            label = { Text("Event Location") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            isError = error != null,
            supportingText = if (error != null) { { Text(error) } } else null
        )
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Next", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun Step4Offering(
    selectedOffering: String,
    error: String?,
    onSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    val offerings = listOf(
        "Basic Essentials Package",
        "Standard Classic Package",
        "Premium Royal Package",
        "Custom Package",
        "Photography Service Only",
        "Cinematography Service Only",
        "Not sure yet"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Which package or service are you interested in?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            offerings.forEach { offering ->
                val isSelected = selectedOffering == offering
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(offering) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = offering,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Next", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun Step5CustomerInfo(
    name: String,
    phone: String,
    email: String,
    message: String,
    errors: Map<String, String>,
    onChange: (String, String, String, String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Your Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { onChange(it, phone, email, message) },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = errors.containsKey("customerName"),
                supportingText = if (errors.containsKey("customerName")) { { Text(errors["customerName"]!!) } } else null
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { onChange(name, it, email, message) },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = errors.containsKey("phone"),
                supportingText = if (errors.containsKey("phone")) { { Text(errors["phone"]!!) } } else null
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { onChange(name, phone, it, message) },
                label = { Text("Email Address (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errors.containsKey("email"),
                supportingText = if (errors.containsKey("email")) { { Text(errors["email"]!!) } } else null
            )
            
            OutlinedTextField(
                value = message,
                onValueChange = { onChange(name, phone, email, it) },
                label = { Text("Additional Message / Questions") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Review Enquiry", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun Step6Review(
    uiState: BookingUiState,
    onSubmit: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val dateString = uiState.eventDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Not set"

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Review & Submit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ReviewRow("Event Type", uiState.eventType)
                    ReviewRow("Event Date", dateString)
                    ReviewRow("Location", uiState.location)
                    ReviewRow("Interested In", uiState.selectedOffering)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ReviewRow("Name", uiState.customerName)
                    ReviewRow("Phone", uiState.phone)
                    if (uiState.email.isNotBlank()) ReviewRow("Email", uiState.email)
                    if (uiState.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Message",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (uiState.submissionError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.submissionError,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Submit Enquiry", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BookingConfirmationView(
    booking: BookingItem,
    onTrackEnquiry: () -> Unit = {},
    onDone: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val dateString = dateFormatter.format(Date(booking.eventDate))

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enquiry Submitted!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thank you, ${booking.customerName}. We have received your request.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Enquiry Reference Number",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = booking.referenceId.ifBlank { booking.id },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Status: NEW (Enquiry Received)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${booking.eventType} on $dateString",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "What's next?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your enquiry has been securely registered with Royal Studio. Our team will review your requirements and reach out to discuss packages and availability. You can track progress anytime in the Customer Area.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onTrackEnquiry,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text("Track in Customer Area", modifier = Modifier.padding(vertical = 6.dp))
        }

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text("Back to Home", modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.primary)
        }
    }
}
