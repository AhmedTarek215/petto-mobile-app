package com.example.petto.network

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: PettoApi by lazy {
        retrofit.create(PettoApi::class.java)
    }
}
