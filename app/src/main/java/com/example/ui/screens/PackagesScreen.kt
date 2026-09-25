package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.models.PackageItem
import com.example.ui.navigation.Screen
import com.example.ui.viewmodels.PackageViewModel

@Composable
fun PackagesScreen(
    navController: NavController,
    viewModel: PackageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Packages", "Compare")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            if (selectedTabIndex == 0) {
                PackagesListView(
                    packages = uiState.packages,
                    onPackageClick = { navController.navigate("package_detail/${it.id}") },
                    onBookClick = { navController.navigate(Screen.Booking.route) }
                )
            } else {
                PackagesCompareView(
                    packages = uiState.packages,
                    onBookClick = { navController.navigate(Screen.Booking.route) }
                )
            }
        }
    }
}

@Composable
private fun PackagesListView(
    packages: List<PackageItem>,
    onPackageClick: (PackageItem) -> Unit,
    onBookClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Investment Packages",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Transparent pricing for your unforgettable moments.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary, // Gold
                    textAlign = TextAlign.Center
                )
            }
        }

        items(packages) { pkg ->
            PackageCard(
                pkg = pkg,
                onClick = { onPackageClick(pkg) },
                onBookClick = onBookClick
            )
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun PackageCard(
    pkg: PackageItem,
    onClick: () -> Unit,
    onBookClick: () -> Unit
) {
    val borderColor = if (pkg.isRecommended) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (pkg.isRecommended) 2.dp else 0.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pkg.isRecommended) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (pkg.isRecommended) 8.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (pkg.isRecommended) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Recommended",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECOMMENDED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pkg.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${pkg.currency}${pkg.price}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Key Inclusions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                pkg.includedServices.take(4).forEach { service ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = service,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Details")
                    }
                    Button(
                        onClick = onBookClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Enquire")
                    }
                }
            }
        }
    }
}

@Composable
private fun PackagesCompareView(
    packages: List<PackageItem>,
    onBookClick: () -> Unit
) {
    val rowHeight = 64.dp
    val labelWidth = 140.dp
    val columnWidth = 180.dp
    
    // Labels for the rows
    val features = listOf(
        "Price",
        "Photography",
        "Cinematography",
        "Photographers",
        "Videographers",
        "Album",
        "Delivery"
    )

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            text = "Compare Packages",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth()) {
            // Fixed Left Column (Labels)
            Column(modifier = Modifier.width(labelWidth).background(MaterialTheme.colorScheme.background)) {
                // Empty header for alignment
                Box(modifier = Modifier.height(100.dp).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surface))
                
                features.forEach { label ->
                    Box(
                        modifier = Modifier.height(rowHeight).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surface).padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                
                // Bottom empty cell for CTA alignment
                Box(modifier = Modifier.height(80.dp).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surface))
            }

            // Horizontally Scrollable Packages
            LazyRow(modifier = Modifier.weight(1f)) {
                items(packages) { pkg ->
                    Column(modifier = Modifier.width(columnWidth)) {
                        // Header
                        Box(
                            modifier = Modifier.height(100.dp).fillMaxWidth()
                                .background(if (pkg.isRecommended) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (pkg.isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                if (pkg.isRecommended) {
                                    Text(
                                        text = "RECOMMENDED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = pkg.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Data Rows
                        CompareCell(text = "${pkg.currency}${pkg.price}", height = rowHeight)
                        CompareCell(text = pkg.photographyHours, height = rowHeight)
                        CompareCell(text = pkg.videographyHours, height = rowHeight)
                        CompareCell(text = pkg.numberOfPhotographers.toString(), height = rowHeight)
                        CompareCell(text = pkg.numberOfVideographers.toString(), height = rowHeight)
                        CompareCell(text = pkg.albumInformation, height = rowHeight)
                        CompareCell(text = pkg.deliveryInformation, height = rowHeight)

                        // CTA Row
                        Box(
                            modifier = Modifier.height(80.dp).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surface).padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = onBookClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Select")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareCell(text: String, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.height(height).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surface).padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
