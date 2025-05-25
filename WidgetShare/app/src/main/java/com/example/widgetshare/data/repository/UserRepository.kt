// This file will be rewritten for REST API. All Firebase code removed. 

package com.example.widgetshare.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.widgetshare.data.remote.ApiClient
import com.example.widgetshare.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val context: Context) {
    private val api: ApiService = ApiClient.getApiService(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    suspend fun register(email: String, nickname: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            api.register(email, nickname, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp = api.login(email, password)
            prefs.edit().putString("jwt", resp.access_token).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        prefs.edit().remove("jwt").apply()
    }

    suspend fun getProfile() = api.getProfile(getToken())
    suspend fun searchUserByNickname(nickname: String) = api.searchUserByNickname(nickname, getToken())
    suspend fun sendFriendRequest(toNickname: String) = api.sendFriendRequest(toNickname, getToken())
    suspend fun getFriendRequests() = api.getFriendRequests(getToken())
    suspend fun acceptFriendRequest(requestId: Int) = api.acceptFriendRequest(requestId, getToken())
    suspend fun declineFriendRequest(requestId: Int) = api.declineFriendRequest(requestId, getToken())
    suspend fun getFriends() = api.getFriends(getToken())

    private fun getToken(): String = "Bearer ${prefs.getString("jwt", null) ?: ""}"
} 