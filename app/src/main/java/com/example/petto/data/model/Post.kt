package com.example.petto.data.model

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    var userProfileImage: String? = null,
    val content: String = "",
    val mediaUrl: String? = null,
    val likes: Int = 0 ,
    val timestamp: Timestamp = Timestamp.now()
)
