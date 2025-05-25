package com.example.widgetshare.data.models

data class User(
    val id: String = "",
    val email: String = "",
    val nickname: String = "",
    val friends: List<String> = emptyList(), // List of user IDs
    val friendRequests: List<String> = emptyList() // List of user IDs who sent requests
) 