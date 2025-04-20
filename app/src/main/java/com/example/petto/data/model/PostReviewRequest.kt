package com.example.petto.data.model

data class PostReviewRequest(
    val user_id: String = "",
    val service_id: String = "",
    val rating: Float = 0f,
    val r_comment: String? = null
)
