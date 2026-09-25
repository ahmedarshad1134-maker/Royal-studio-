package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.viewmodels.ContactViewModel

@Composable
fun ContactScreen(
    navController: NavController,
    viewModel: ContactViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Contact Us",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val contact = uiState.contactInfo
            if (contact != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Get in Touch",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We would love to hear from you. Reach out to discuss your upcoming event.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // Contact Info Cards
                    ContactMethodCard(
                        icon = Icons.Default.Phone,
                        title = "Phone & WhatsApp",
                        value1 = contact.phone,
                        value2 = "WhatsApp: ${contact.whatsapp}",
                        onClick1 = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                            context.startActivity(intent)
                        },
                        onClick2 = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${contact.whatsapp.replace(Regex("[^0-9]"), "")}"))
                            context.startActivity(intent)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ContactMethodCard(
                        icon = Icons.Default.Email,
                        title = "Email",
                        value1 = contact.email,
                        onClick1 = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.email}"))
                            context.startActivity(intent)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ContactMethodCard(
                        icon = Icons.Default.LocationOn,
                        title = "Studio Location",
                        value1 = contact.address,
                        value2 = contact.businessHours,
                        onClick1 = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(contact.address)}"))
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                    Divider(color = MaterialTheme.colorScheme.surface)
                    Spacer(modifier = Modifier.height(48.dp))

                    // Contact Form
                    Text(
                        text = "Send a Message",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.submissionSuccess) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Message Sent Successfully!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "We will get back to you as soon as possible.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateForm(it, uiState.phone, uiState.email, uiState.message) },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.errors.containsKey("name"),
                            supportingText = if (uiState.errors.containsKey("name")) { { Text(uiState.errors["name"]!!) } } else null
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updateForm(uiState.name, it, uiState.email, uiState.message) },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.errors.containsKey("phone"),
                            supportingText = if (uiState.errors.containsKey("phone")) { { Text(uiState.errors["phone"]!!) } } else null
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateForm(uiState.name, uiState.phone, it, uiState.message) },
                            label = { Text("Email (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.errors.containsKey("email"),
                            supportingText = if (uiState.errors.containsKey("email")) { { Text(uiState.errors["email"]!!) } } else null
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.message,
                            onValueChange = { viewModel.updateForm(uiState.name, uiState.phone, uiState.email, it) },
                            label = { Text("Message") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            isError = uiState.errors.containsKey("message"),
                            maxLines = 4,
                            supportingText = if (uiState.errors.containsKey("message")) { { Text(uiState.errors["message"]!!) } } else null
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.submitContactForm() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !uiState.isSubmitting
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Send Message", modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun ContactMethodCard(
    icon: ImageVector,
    title: String,
    value1: String,
    value2: String? = null,
    onClick1: () -> Unit,
    onClick2: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value1,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onClick1() }.padding(vertical = 4.dp)
                )
                if (value2 != null) {
                    Text(
                        text = value2,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onClick2?.invoke() }.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
