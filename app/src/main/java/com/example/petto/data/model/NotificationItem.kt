package com.example.petto.data.model

import com.google.firebase.Timestamp

data class NotificationItem(
    val type: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val senderId: String = "",
    val postId: String? = null,
    val profileImage: String? = null
)
