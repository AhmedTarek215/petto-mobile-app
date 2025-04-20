package com.example.petto.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import com.example.petto.data.model.PetService
import com.example.petto.data.model.Review
import com.example.petto.data.model.PostReviewRequest

interface PettoApi {
    @GET("services/{serviceId}")
    suspend fun getServiceById(@Path("serviceId") serviceId: String): Response<PetService>

    @GET("services/{serviceId}/reviews")
    suspend fun getReviewsForService(@Path("serviceId") serviceId: String): Response<List<Review>>

    @POST("reviews")
    suspend fun postReview(@Body review: PostReviewRequest): Response<Unit>
}