package com.example.petto.data.viewModel

import com.example.petto.data.model.User


object SignUpViewModel {
    var firstName = ""
    var lastName = ""
    var age = 0
    var gender = ""
    var city = ""
    var area = ""
    var street = ""
    var email = ""
    var password = ""
    var profileImageUrl: String? = null
    //pet
    var petName: String = ""
    var petGender: String = ""
    var petBirthDate: String = ""
    var petType: String = ""
    var petBreed: String = ""
    var petWeight: String = ""
    var petHeight: String = ""
    var petColor: String = ""
    var petImageUrl: String? = null

    fun toUser(userId: String): User {
        return User(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            age = age,
            gender = gender,
            city = city,
            area = area,
            street = street,
            email = email
        )
    }

    fun clear() {
        firstName = ""
        lastName = ""
        age = 0
        gender = ""
        city = ""
        area = ""
        street = ""
        email = ""
        password = ""
    }

}