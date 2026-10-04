package com.example.data.repository

import android.util.Log
import com.example.data.models.UserDto
import com.example.data.models.UserRole
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class AuthRepository(private val dbRepository: FirebaseRepository) {
    private var auth: FirebaseAuth? = null
    private var _isInitialized: Boolean = false

    val isInitialized: Boolean
        get() {
            if (_isInitialized && auth != null) return true
            return try {
                FirebaseApp.getInstance()
                auth = Firebase.auth
                _isInitialized = true
                true
            } catch (e: Throwable) {
                _isInitialized = false
                auth = null
                false
            }
        }

    init {
        // Initial evaluation
        isInitialized
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun getAuthStateUpdates(): Flow<FirebaseUser?> = callbackFlow {
        if (!isInitialized || auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth?.addAuthStateListener(listener)
        awaitClose { auth?.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        if (!isInitialized || auth == null) {
            return Result.failure(IllegalStateException("Firebase is not initialized. Please ensure google-services.json is configured in the app/ folder."))
        }
        return try {
            val result = auth!!.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed, user is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String, phone: String = ""): Result<FirebaseUser> {
        if (!isInitialized || auth == null) {
            return Result.failure(IllegalStateException("Firebase is not initialized. Please ensure google-services.json is configured in the app/ folder."))
        }
        val cleanEmail = email.trim().lowercase()
        return try {
            val result = auth!!.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: throw Exception("Signup failed, user is null")
            
            // Create user document in Firestore - strictly enforce "customer" role
            val userDto = UserDto(
                uid = user.uid,
                name = name.trim(),
                email = cleanEmail,
                phone = phone.trim(),
                role = "customer",
                createdAt = Date(),
                updatedAt = Date()
            )

            // If createUserProfile fails after the account was created, retry once;
            // if it still fails, return success but log the error and let ensureCustomerProfile create it on next login.
            try {
                dbRepository.createUserProfile(userDto)
            } catch (e: Exception) {
                Log.w("AuthRepository", "First attempt to create user profile failed, retrying once...", e)
                try {
                    dbRepository.createUserProfile(userDto)
                } catch (retryEx: Exception) {
                    Log.e("AuthRepository", "Retry creating user profile failed. Returning success and deferring to ensureCustomerProfile on next login.", retryEx)
                }
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        if (!isInitialized || auth == null) {
            return Result.failure(IllegalStateException("Firebase is not initialized. Please ensure google-services.json is configured in the app/ folder."))
        }
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth!!.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign-in failed, user is null")
            ensureCustomerProfile(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithFacebook(accessToken: String): Result<FirebaseUser> {
        if (!isInitialized || auth == null) {
            return Result.failure(IllegalStateException("Firebase is not initialized. Please ensure google-services.json is configured in the app/ folder."))
        }
        return try {
            val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken)
            val result = auth!!.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Facebook sign-in failed, user is null")
            ensureCustomerProfile(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (!isInitialized || auth == null) {
            return Result.failure(IllegalStateException("Firebase is not initialized. Please ensure google-services.json is configured in the app/ folder."))
        }
        return try {
            auth!!.sendPasswordResetEmail(email.trim().lowercase()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureCustomerProfile(user: FirebaseUser) {
        try {
            val existsResult = dbRepository.userProfileExists(user.uid)
            if (existsResult.isSuccess) {
                val exists = existsResult.getOrNull() == true
                if (!exists) {
                    val cleanEmail = user.email?.trim()?.lowercase() ?: ""
                    val userDto = UserDto(
                        uid = user.uid,
                        name = user.displayName ?: "",
                        email = cleanEmail,
                        phone = user.phoneNumber ?: "",
                        role = "customer",
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    dbRepository.createUserProfile(userDto)
                }
            } else {
                Log.w("AuthRepository", "Failed to check if user profile exists (network/permission error): ${existsResult.exceptionOrNull()?.message}. Will not overwrite existing profile.")
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Error ensuring customer profile in Firestore", e)
        }
    }

    suspend fun signOut() {
        if (!isInitialized || auth == null) return
        auth?.signOut()
    }
    
    /**
     * Secure centralized user role resolution:
     * 1. Inspects Firebase Auth ID Token custom claims (role == 'admin' or admin == true)
     * 2. Inspects Firestore users/{uid} document role field
     * Never trusts role supplied from client UI.
     */
    suspend fun getUserRole(uid: String, forceRefresh: Boolean = false): UserRole {
        if (!isInitialized || uid.isBlank()) return UserRole.CUSTOMER

        // Check custom claim on current FirebaseUser token if uid matches
        val user = auth?.currentUser
        if (user != null && user.uid == uid) {
            try {
                val tokenResult = user.getIdToken(forceRefresh).await()
                val claims = tokenResult.claims
                val roleClaim = claims["role"] as? String
                val adminClaim = claims["admin"] as? Boolean
                if (roleClaim.equals("admin", ignoreCase = true) || adminClaim == true) {
                    return UserRole.ADMIN
                }
            } catch (e: Exception) {
                Log.w("AuthRepository", "Failed to retrieve custom claims from ID token", e)
            }
        }

        // Check Firestore user profile
        return try {
            val profile = dbRepository.getUserProfile(uid)
            UserRole.fromString(profile?.role)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to retrieve user profile from Firestore", e)
            UserRole.CUSTOMER
        }
    }

    suspend fun isCurrentUserAdmin(forceRefresh: Boolean = false): Boolean {
        val uid = currentUser?.uid ?: return false
        return getUserRole(uid, forceRefresh) == UserRole.ADMIN
    }

    suspend fun getUserProfile(uid: String): UserDto? {
        if (!isInitialized || uid.isBlank()) return null
        return dbRepository.getUserProfile(uid)
    }
}
