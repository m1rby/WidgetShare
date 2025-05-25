package com.example.widgetshare.data.remote

import okhttp3.MultipartBody
import retrofit2.http.*

// --- AUTH ---
data class RegisterRequest(val email: String, val nickname: String, val password: String)
data class RegisterResponse(val id: Int, val email: String, val nickname: String)
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val access_token: String, val token_type: String)

data class UserDto(val id: Int, val email: String, val nickname: String)
data class FriendRequestDto(val id: Int, val from_user_id: Int, val to_user_id: Int, val status: String)
data class PhotoDto(val id: Int, val url: String, val sender_id: Int, val receiver_id: Int, val timestamp: Long)

interface ApiService {
    @POST("/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("email") email: String,
        @Field("nickname") nickname: String,
        @Field("password") password: String
    ): RegisterResponse

    @POST("/token")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("/profile")
    suspend fun getProfile(@Header("Authorization") token: String): UserDto

    @GET("/users/search")
    suspend fun searchUserByNickname(@Query("nickname") nickname: String, @Header("Authorization") token: String): UserDto?

    @POST("/friends/request")
    suspend fun sendFriendRequest(@Query("to_nickname") toNickname: String, @Header("Authorization") token: String): Unit

    @GET("/friends/requests")
    suspend fun getFriendRequests(@Header("Authorization") token: String): List<FriendRequestDto>

    @POST("/friends/accept")
    suspend fun acceptFriendRequest(@Query("request_id") requestId: Int, @Header("Authorization") token: String): Unit

    @POST("/friends/decline")
    suspend fun declineFriendRequest(@Query("request_id") requestId: Int, @Header("Authorization") token: String): Unit

    @GET("/friends")
    suspend fun getFriends(@Header("Authorization") token: String): List<UserDto>

    // --- PHOTOS ---
    @Multipart
    @POST("/upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): UploadPhotoResponse

    data class UploadPhotoResponse(val url: String)

    @FormUrlEncoded
    @POST("/photos/send")
    suspend fun sendPhotoToFriends(
        @Field("url") url: String,
        @Field("friend_ids") friendIds: String, // JSON-массив строкой
        @Header("Authorization") token: String
    ): Unit

    @GET("/photos/history")
    suspend fun getPhotoHistory(@Header("Authorization") token: String): List<PhotoDto>
} 