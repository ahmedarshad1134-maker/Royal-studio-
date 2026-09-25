package com.example.ui.models

enum class PortfolioCategory(val displayName: String) {
    ALL("All"),
    WEDDING("Wedding"),
    PRE_WEDDING("Pre-Wedding"),
    ENGAGEMENT("Engagement"),
    BIRTHDAY("Birthday"),
    ANNIVERSARY("Anniversary"),
    BABY_FAMILY("Baby/Family"),
    OTHER_EVENTS("Other Events")
}

enum class MediaType { IMAGE, VIDEO }

data class PortfolioItem(
    val id: String,
    val title: String,
    val category: PortfolioCategory,
    val type: MediaType,
    val imageUrl: String? = null,
    val imageResId: Int? = null, // Fallback for placeholders
    val featured: Boolean,
    val createdAt: Long,
    val isBroken: Boolean = false // Simulate broken media error state
)
