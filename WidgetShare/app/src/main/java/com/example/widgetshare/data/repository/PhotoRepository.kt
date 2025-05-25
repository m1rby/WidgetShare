// This file will be rewritten for REST API. All Firebase code removed. 

package com.example.widgetshare.data.repository

import android.content.Context
import com.example.widgetshare.data.remote.ApiClient
import com.example.widgetshare.data.remote.ApiService
import okhttp3.MultipartBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson

class PhotoRepository(private val context: Context) {
    private val api: ApiService = ApiClient.getApiService(context)

    suspend fun uploadPhoto(file: MultipartBody.Part): String = withContext(Dispatchers.IO) {
        val resp = api.uploadPhoto(file)
        return@withContext resp.url
    }

    suspend fun sendPhotoToFriends(url: String, friendIds: List<Int>) = withContext(Dispatchers.IO) {
        val friendIdsJson = Gson().toJson(friendIds)
        api.sendPhotoToFriends(url, friendIdsJson, getToken())
    }

    suspend fun getPhotoHistory() = api.getPhotoHistory(getToken())

    private fun getToken(): String {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return "Bearer ${prefs.getString("jwt", null) ?: ""}"
    }
} 