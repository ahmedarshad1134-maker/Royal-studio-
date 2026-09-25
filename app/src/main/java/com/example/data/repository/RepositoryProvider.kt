package com.example.data.repository

object RepositoryProvider {
    val firebaseRepository by lazy { FirebaseRepository() }
    val authRepository by lazy { AuthRepository(firebaseRepository) }
    val storageRepository by lazy { StorageRepository() }
}
