package com.example.ui.models

data class PackageItem(
    val id: String,
    val name: String,
    val description: String,
    val price: String, // String to handle values like "$1,500" or "Custom"
    val currency: String,
    val includedServices: List<String>,
    val photographyHours: String,
    val videographyHours: String,
    val numberOfPhotographers: Int,
    val numberOfVideographers: Int,
    val albumInformation: String,
    val deliveryInformation: String,
    val optionalAddOns: List<String>,
    val isRecommended: Boolean
)
