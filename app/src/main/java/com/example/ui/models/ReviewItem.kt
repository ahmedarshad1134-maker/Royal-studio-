package com.example.ui.models

data class ReviewItem(
    val id: String,
    val customerName: String,
    val rating: Int, // 1 to 5
    val review: String,
    val eventType: String,
    val photoResId: Int? = null,
    val approved: Boolean = false,
    val featured: Boolean = false,
    val createdAt: Long
)
