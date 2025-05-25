package com.example.widgetshare.data.repository

import com.example.widgetshare.data.models.Photo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PhotoRepository {
    private val database = FirebaseDatabase.getInstance()
    private val photosRef = database.getReference("photos")
    private val auth = FirebaseAuth.getInstance()

    suspend fun sendPhotoToFriends(photoUrl: String, friendIds: List<String>) {
        val senderId = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()
        for (friendId in friendIds) {
            val id = UUID.randomUUID().toString()
            val photo = Photo(
                id = id,
                url = photoUrl,
                senderId = senderId,
                receiverId = friendId,
                timestamp = timestamp
            )
            photosRef.child(friendId).child(id).setValue(photo).await()
        }
    }

    suspend fun getPhotoHistory(): List<Photo> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = photosRef.child(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(Photo::class.java) }.sortedByDescending { it.timestamp }
    }
} 