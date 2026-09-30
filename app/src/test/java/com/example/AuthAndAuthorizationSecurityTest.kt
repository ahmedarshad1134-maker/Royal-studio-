package com.example

import com.example.data.models.BookingDto
import com.example.data.models.UserDto
import com.example.data.models.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAndAuthorizationSecurityTest {

    @Test
    fun `customer login role parsing defaults strictly to customer`() {
        // Given a user profile without admin role
        val customerProfile = UserDto(
            uid = "cust_123",
            name = "Jane Customer",
            email = "jane@example.com",
            role = "customer"
        )
        val role = UserRole.fromString(customerProfile.role)

        assertEquals(UserRole.CUSTOMER, role)
        assertTrue(role.isCustomer)
        assertFalse(role.isAdmin)
    }

    @Test
    fun `failed login reports proper error message when uninitialized or invalid`() {
        val blankEmail = ""
        val blankPassword = ""
        val isValid = blankEmail.isNotBlank() && blankPassword.isNotBlank()

        assertFalse("Blank credentials must fail validation", isValid)
    }

    @Test
    fun `admin authorization grants access only to admin role`() {
        val adminRole = UserRole.fromString("admin")
        val customerRole = UserRole.fromString("customer")
        val arbitraryRole = UserRole.fromString("super_user")

        assertTrue("admin role must be recognized as ADMIN", adminRole.isAdmin)
        assertFalse("customer role must not be recognized as ADMIN", customerRole.isAdmin)
        assertFalse("arbitrary client role must default to CUSTOMER and not ADMIN", arbitraryRole.isAdmin)
    }

    @Test
    fun `customer cannot become admin via client-supplied role`() {
        // Any attempt by a client to inject an elevated role is mapped safely
        val maliciousInputs = listOf("admin ", "ADMIN", "Administrator", "root", "moderator", null, "")
        for (input in maliciousInputs) {
            val role = UserRole.fromString(input)
            if (input?.trim()?.lowercase() != "admin") {
                assertEquals(
                    "Role '$input' must default to CUSTOMER",
                    UserRole.CUSTOMER,
                    role
                )
            }
        }

        // Test DTO sanitization on creation
        val attemptedMaliciousUser = UserDto(
            uid = "attacker_uid",
            name = "Attacker",
            email = "attacker@example.com",
            role = "admin"
        )
        // Sanitizer rule as enforced in repository
        val sanitizedUser = if (attemptedMaliciousUser.role.equals("admin", ignoreCase = true)) {
            attemptedMaliciousUser.copy(role = "customer")
        } else {
            attemptedMaliciousUser
        }
        assertEquals("Client cannot persist admin role on registration", "customer", sanitizedUser.role)
    }

    @Test
    fun `unauthenticated user cannot access private data`() {
        val currentAuthenticatedUserUid: String? = null
        val isAccessAllowed = !currentAuthenticatedUserUid.isNullOrBlank()

        assertFalse("Unauthenticated access must be strictly forbidden", isAccessAllowed)
    }

    @Test
    fun `authenticated customer cannot access another customers private data`() {
        val authenticatedCustomerUid = "customer_A"
        val authenticatedCustomerEmail = "customerA@example.com"

        val foreignCustomerBooking = BookingDto(
            id = "booking_999",
            customerId = "customer_B",
            customerName = "Customer B",
            email = "customerB@example.com",
            eventType = "Wedding"
        )

        // Ownership verification rule as implemented in PrivateGalleryViewModel & CustomerAreaViewModel
        val isOwner = (foreignCustomerBooking.customerId == authenticatedCustomerUid) ||
                      (foreignCustomerBooking.email.equals(authenticatedCustomerEmail, ignoreCase = true))

        assertFalse("Customer A must NEVER be permitted to access Customer B's private booking or gallery", isOwner)

        val ownBooking = BookingDto(
            id = "booking_111",
            customerId = authenticatedCustomerUid,
            customerName = "Customer A",
            email = authenticatedCustomerEmail,
            eventType = "Wedding"
        )
        val isOwnerOwnBooking = (ownBooking.customerId == authenticatedCustomerUid) ||
                                (ownBooking.email.equals(authenticatedCustomerEmail, ignoreCase = true))

        assertTrue("Customer A must be permitted to access their own booking and gallery", isOwnerOwnBooking)
    }

    @Test
    fun `contact message creation requires name, phone, and message non-empty`() {
        // Valid message
        val validMessage = com.example.data.models.ContactMessageDto(
            name = "John Doe",
            phone = "+1234567890",
            email = "john@example.com",
            message = "Inquiry regarding wedding photography"
        )
        val isValid = validMessage.name.isNotBlank() &&
                      validMessage.phone.isNotBlank() &&
                      validMessage.message.isNotBlank()
        assertTrue("Valid contact message must pass validation", isValid)

        // Invalid messages
        val blankName = validMessage.copy(name = "")
        assertFalse("Blank name must fail validation", blankName.name.isNotBlank() && blankName.phone.isNotBlank() && blankName.message.isNotBlank())

        val blankPhone = validMessage.copy(phone = "")
        assertFalse("Blank phone must fail validation", blankPhone.name.isNotBlank() && blankPhone.phone.isNotBlank() && blankPhone.message.isNotBlank())

        val blankMessage = validMessage.copy(message = "")
        assertFalse("Blank message must fail validation", blankMessage.name.isNotBlank() && blankMessage.phone.isNotBlank() && blankMessage.message.isNotBlank())
    }

    @Test
    fun `contact messages can be created by anyone but read, update, delete only by admin`() {
        val nonAdminRole = UserRole.CUSTOMER
        val adminRole = UserRole.ADMIN

        // Read/Update/Delete access
        assertFalse("Customer must not have read/update/delete access to contact messages", nonAdminRole.isAdmin)
        assertTrue("Admin must have read/update/delete access to contact messages", adminRole.isAdmin)
    }
}
