package com.example.ui.models

data class ServiceItem(
    val id: String,
    val name: String,
    val shortDescription: String,
    val description: String,
    val coverImageResId: Int,
    val features: List<String>,
    val startingPrice: String? = null, // e.g. "$500" or null if not applicable/configured
    val suitableEventTypes: List<String>
)
