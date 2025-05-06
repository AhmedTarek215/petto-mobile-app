package com.example.petto.data.model

data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val gender: String = "",
    val city: String = "",
    val area: String = "",
    val street: String = "",
    val email: String = "",
    var profileImageUrl: String? = null
    ,


    )
