package com.example.data.repository

import android.util.Log
import com.example.data.models.UserDto
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
    var isInitialized: Boolean = false
        private set

    init {
        try {
            FirebaseApp.getInstance()
            auth = Firebase.auth
            isInitialized = true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Firebase not initialized. Missing google-services.json?", e)
            isInitialized = false
        }
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun getAuthStateUpdates(): Flow<FirebaseUser?> = callbackFlow {
        if (!isInitialized) {
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
        if (!isInitialized) return Result.failure(Exception("Firebase not configured."))
        return try {
            val result = auth!!.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed, user is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String, phone: String = ""): Result<FirebaseUser> {
        if (!isInitialized) return Result.failure(Exception("Firebase not configured."))
        return try {
            val result = auth!!.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Signup failed, user is null")
            
            // Create user document in Firestore
            val userDto = UserDto(
                uid = user.uid,
                name = name,
                email = email,
                phone = phone,
                role = "customer",
                createdAt = Date(),
                updatedAt = Date()
            )
            dbRepository.createUserProfile(userDto)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        if (!isInitialized) return
        auth?.signOut()
    }
    
    suspend fun getUserRole(uid: String): String {
        if (!isInitialized) return "customer" // fallback
        return dbRepository.getUserProfile(uid)?.role ?: "customer"
    }
}
