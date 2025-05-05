package com.example.petto.data.model

import com.google.firebase.Timestamp

data class Comment(
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

