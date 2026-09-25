package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            HeroSection(
                onBookClick = { navController.navigate(Screen.Booking.route) },
                onPortfolioClick = { navController.navigate(Screen.Portfolio.route) }
            )
        }
        item {
            ServicesSection()
        }
        item {
            FeaturedPortfolioSection(
                onViewAllClick = { navController.navigate(Screen.Portfolio.route) }
            )
        }
        item {
            WhyChooseUsSection()
        }
        item {
            BookingCtaSection(
                onBookClick = { navController.navigate(Screen.Booking.route) }
            )
        }
        item {
            ReviewsPreviewSection(
                onViewReviewsClick = { navController.navigate(Screen.Reviews.route) }
            )
        }
        item {
            FooterSection(navController = navController)
        }
    }
}

@Composable
private fun HeroSection(onBookClick: () -> Unit, onPortfolioClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square on mobile, can adjust for tablet later
    ) {
        Image(
            painter = painterResource(id = R.drawable.hero_wedding_cinematic_1789061882938),
            contentDescription = "Royal Studio Hero Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Dark gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ROYAL STUDIO",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary, // Gold
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Capturing Your Timeless Moments",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onBookClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Book an Event")
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(
                    onClick = onPortfolioClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Portfolio")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ServicesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        SectionTitle(title = "Our Services", subtitle = "Premium Photography & Cinematography")
        Spacer(modifier = Modifier.height(24.dp))
        
        val services = listOf(
            ServiceItem("Wedding Photography", R.drawable.service_wedding_1789061903404),
            ServiceItem("Wedding Cinematography", R.drawable.service_wedding_1789061903404),
            ServiceItem("Pre-Wedding", R.drawable.portfolio_sample_1789061922601),
            ServiceItem("Birthday", R.drawable.service_event_1789061936720),
            ServiceItem("Engagement", R.drawable.service_wedding_1789061903404),
            ServiceItem("Other Events", R.drawable.service_event_1789061936720)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(services) { service ->
                ServiceCard(service)
            }
        }
    }
}

data class ServiceItem(val title: String, val imageRes: Int)

@Composable
private fun ServiceCard(item: ServiceItem) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .aspectRatio(0.8f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, // Gold
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun FeaturedPortfolioSection(onViewAllClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        SectionTitle(title = "Featured Work", subtitle = "Stories Told Through Our Lens")
        Spacer(modifier = Modifier.height(24.dp))
        
        // Placeholder Gallery
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.portfolio_sample_1789061922601),
                contentDescription = "Featured Work 1",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.service_wedding_1789061903404),
                    contentDescription = "Featured Work 2",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                )
                Image(
                    painter = painterResource(id = R.drawable.service_event_1789061936720),
                    contentDescription = "Featured Work 3",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OutlinedButton(
                onClick = onViewAllClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("View All Portfolio")
            }
        }
    }
}

@Composable
private fun WhyChooseUsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        SectionTitle(title = "Why Choose Us", subtitle = "The Royal Studio Difference")
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeatureItem(
                icon = Icons.Default.CameraAlt,
                title = "Creative Storytelling",
                description = "Every event is a unique story waiting to be told through our creative perspective."
            )
            FeatureItem(
                icon = Icons.Default.Favorite,
                title = "Professional Photography",
                description = "High-end equipment combined with years of artistic eye to capture perfection."
            )
            FeatureItem(
                icon = Icons.Default.Movie,
                title = "Cinematic Videos",
                description = "Breathtaking cinematography that turns your special moments into a movie."
            )
            FeatureItem(
                icon = Icons.Default.Star,
                title = "Personalized Service",
                description = "We cater to your specific vision, ensuring a royal treatment from start to finish."
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BookingCtaSection(onBookClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ready for Your Close-Up?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Let us turn your next event into an unforgettable masterpiece.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Enquire Now", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun ReviewsPreviewSection(onViewReviewsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        SectionTitle(title = "Client Love", subtitle = "What people are saying")
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "See what our clients are saying about their Royal Studio experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onViewReviewsClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Read Reviews")
                }
            }
        }
    }
}

@Composable
private fun FooterSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp)
    ) {
        Text(
            text = "ROYAL STUDIO",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Professional Photography & Cinematography Services.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Navigation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                FooterLink("Home") { navController.navigate(Screen.Home.route) }
                FooterLink("Portfolio") { navController.navigate(Screen.Portfolio.route) }
                FooterLink("About Us") { navController.navigate(Screen.About.route) }
                FooterLink("Contact") { navController.navigate(Screen.Contact.route) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                FooterLink("Weddings") { navController.navigate(Screen.Services.route) }
                FooterLink("Events") { navController.navigate(Screen.Services.route) }
                FooterLink("Portraits") { navController.navigate(Screen.Services.route) }
                FooterLink("Cinematography") { navController.navigate(Screen.Services.route) }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "© 2026 Royal Studio. All Rights Reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FooterLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary, // Gold
            textAlign = TextAlign.Center
        )
    }
}
