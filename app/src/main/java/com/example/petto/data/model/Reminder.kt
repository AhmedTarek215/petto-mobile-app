package com.example.petto.data.model

import com.google.firebase.Timestamp

data class Reminder(
    val pet_name: String = "",
    val reminder_name: String = "",
    val reminder_type: String = "",
    val r_time: String = "",
    val r_date: Timestamp? = null,
    val user_id: String = ""
)