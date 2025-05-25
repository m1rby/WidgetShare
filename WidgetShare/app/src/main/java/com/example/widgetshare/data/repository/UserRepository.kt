package com.example.widgetshare.data.repository

import com.example.widgetshare.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val auth = FirebaseAuth.getInstance()

    suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return usersRef.child(userId).get().await().getValue(User::class.java)
    }

    suspend fun createUserProfile(userId: String, email: String, nickname: String) {
        val user = User(id = userId, email = email, nickname = nickname)
        usersRef.child(userId).setValue(user).await()
    }

    suspend fun findUserByNickname(nickname: String): User? {
        val snapshot = usersRef.orderByChild("nickname").equalTo(nickname).get().await()
        return snapshot.children.firstOrNull()?.getValue(User::class.java)
    }

    suspend fun sendFriendRequest(toNickname: String): Result<Unit> {
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in"))
        val targetUser = findUserByNickname(toNickname) ?: return Result.failure(Exception("User not found"))
        if (targetUser.friendRequests.contains(currentUser.id)) {
            return Result.failure(Exception("Request already sent"))
        }
        val updatedRequests = targetUser.friendRequests + currentUser.id
        usersRef.child(targetUser.id).child("friendRequests").setValue(updatedRequests).await()
        return Result.success(Unit)
    }

    suspend fun getFriendRequests(): List<User> {
        val currentUser = getCurrentUser() ?: return emptyList()
        return currentUser.friendRequests.mapNotNull { usersRef.child(it).get().await().getValue(User::class.java) }
    }

    suspend fun acceptFriendRequest(fromUserId: String): Result<Unit> {
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in"))
        val fromUser = usersRef.child(fromUserId).get().await().getValue(User::class.java) ?: return Result.failure(Exception("User not found"))
        // Add each other to friends
        val updatedFriends = currentUser.friends + fromUserId
        val updatedRequests = currentUser.friendRequests - fromUserId
        usersRef.child(currentUser.id).child("friends").setValue(updatedFriends).await()
        usersRef.child(currentUser.id).child("friendRequests").setValue(updatedRequests).await()
        // Update other user
        val fromUserFriends = fromUser.friends + currentUser.id
        usersRef.child(fromUserId).child("friends").setValue(fromUserFriends).await()
        return Result.success(Unit)
    }

    suspend fun rejectFriendRequest(fromUserId: String): Result<Unit> {
        val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in"))
        val updatedRequests = currentUser.friendRequests - fromUserId
        usersRef.child(currentUser.id).child("friendRequests").setValue(updatedRequests).await()
        return Result.success(Unit)
    }

    suspend fun getFriends(): List<User> {
        val currentUser = getCurrentUser() ?: return emptyList()
        return currentUser.friends.mapNotNull { usersRef.child(it).get().await().getValue(User::class.java) }
    }

    suspend fun logout() {
        auth.signOut()
    }
} 