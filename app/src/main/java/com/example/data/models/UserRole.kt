package com.example.data.models

enum class UserRole(val roleName: String) {
    CUSTOMER("customer"),
    ADMIN("admin");

    val isAdmin: Boolean get() = this == ADMIN
    val isCustomer: Boolean get() = this == CUSTOMER

    companion object {
        fun fromString(role: String?): UserRole = when (role?.trim()?.lowercase()) {
            "admin" -> ADMIN
            else -> CUSTOMER
        }
    }
}
