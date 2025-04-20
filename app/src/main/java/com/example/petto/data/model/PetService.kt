package com.example.petto.data.model

data class PetService(
    val service_id: String = "",
    val name: String = "",
    val service_img: String = "",
    val service_type: String = "",
    val social_media: String? = null,
    val services: List<String> = listOf(),
    val contact_info: String = "",
    val address: Address = Address()
)

data class Address(
    val street: String = "",
    val city: String = "",
    val area: String = ""
)


