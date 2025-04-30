package com.example.petto.data.model

data class Review(
    val review_id: String = "",
    val date: String = "",
    val time: String = "",
    val r_service_type: String = "",
    val rating: Float = 0f,
    val r_comment: String? = null,
    val user: ReviewUser = ReviewUser(),
    val timestamp: Long = 0L  // Add timestamp field as Long
)

data class ReviewUser(
    val user_id: String = "",
    val fname: String = "",
    val lname: String = "",
    val user_img: String = ""
)

