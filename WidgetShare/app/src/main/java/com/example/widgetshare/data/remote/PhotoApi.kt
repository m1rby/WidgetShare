package com.example.widgetshare.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PhotoApi {
    @Multipart
    @POST("/upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part
    ): UploadResponse
}

data class UploadResponse(val url: String) 