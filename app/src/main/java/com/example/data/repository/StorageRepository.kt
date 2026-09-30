package com.example.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Progress(val progress: Float) : UploadState()
    data class Success(val url: String) : UploadState()
    data class Error(val error: String) : UploadState()
    object Cancelled : UploadState()
}

class StorageRepository {
    private var storage: com.google.firebase.storage.FirebaseStorage? = null
    private var _isInitialized: Boolean = false

    val isInitialized: Boolean
        get() {
            if (_isInitialized && storage != null) return true
            return try {
                FirebaseApp.getInstance()
                storage = Firebase.storage
                _isInitialized = true
                true
            } catch (e: Throwable) {
                _isInitialized = false
                storage = null
                false
            }
        }

    init {
        // Initial evaluation
        isInitialized
    }

    /**
     * Uploads portfolio media.
     * Category could be wedding, prewedding, etc.
     */
    fun uploadPortfolioMedia(category: String, uri: Uri): Flow<UploadState> = callbackFlow {
        if (!isInitialized || storage == null) {
            trySend(UploadState.Error("Storage not initialized"))
            close()
            return@callbackFlow
        }

        trySend(UploadState.Uploading)

        val fileName = "${UUID.randomUUID()}_${uri.lastPathSegment}"
        val storageRef = storage!!.reference.child("portfolio/$category/$fileName")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            trySend(UploadState.Progress(progress.toFloat()))
        }.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                trySend(UploadState.Success(downloadUri.toString()))
                close()
            }.addOnFailureListener {
                trySend(UploadState.Error(it.message ?: "Failed to get download URL"))
                close()
            }
        }.addOnFailureListener {
            trySend(UploadState.Error(it.message ?: "Upload failed"))
            close()
        }.addOnCanceledListener {
            trySend(UploadState.Cancelled)
            close()
        }

        awaitClose { 
            // We can optionally cancel the upload if the flow collection stops.
            // uploadTask.cancel()
        }
    }

    /**
     * Uploads media for a specific customer's private gallery.
     */
    fun uploadPrivateGalleryMedia(customerId: String, bookingId: String, uri: Uri): Flow<UploadState> = callbackFlow {
        if (!isInitialized || storage == null) {
            trySend(UploadState.Error("Storage not initialized"))
            close()
            return@callbackFlow
        }

        trySend(UploadState.Uploading)

        val fileName = "${UUID.randomUUID()}_${uri.lastPathSegment}"
        val storageRef = storage!!.reference.child("private-galleries/$customerId/$bookingId/$fileName")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            trySend(UploadState.Progress(progress.toFloat()))
        }.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                trySend(UploadState.Success(downloadUri.toString()))
                close()
            }.addOnFailureListener {
                trySend(UploadState.Error(it.message ?: "Failed to get download URL"))
                close()
            }
        }.addOnFailureListener {
            trySend(UploadState.Error(it.message ?: "Upload failed"))
            close()
        }.addOnCanceledListener {
            trySend(UploadState.Cancelled)
            close()
        }

        awaitClose { }
    }

    suspend fun listPrivateGalleryFiles(customerId: String, bookingId: String): List<String> {
        if (!isInitialized || storage == null) return emptyList()
        return try {
            val storageRef = storage!!.reference.child("private-galleries/$customerId/$bookingId")
            val listResult = storageRef.listAll().await()
            val urls = mutableListOf<String>()
            for (item in listResult.items) {
                try {
                    val url = item.downloadUrl.await().toString()
                    urls.add(url)
                } catch (e: Exception) {
                    Log.w("StorageRepository", "Could not resolve URL for item ${item.name}", e)
                }
            }
            urls
        } catch (e: Exception) {
            Log.e("StorageRepository", "Failed to list gallery files", e)
            emptyList()
        }
    }
    suspend fun deleteMedia(url: String): Boolean {
        if (!isInitialized || storage == null) return false
        return try {
            val storageRef = storage!!.getReferenceFromUrl(url)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            Log.e("StorageRepository", "Failed to delete media", e)
            false
        }
    }
}
