package com.example.petto.data.repository

import com.example.petto.data.model.PetService
import com.example.petto.data.model.Review
import com.example.petto.data.model.ReviewUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ServiceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getServiceById(serviceId: String): PetService? {
        return try {
            val doc = db.collection("services").document(serviceId).get().await()
            doc.toObject(PetService::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getReviewsForService(serviceId: String): List<Review> {
        return try {
            val snapshot = db.collection("services")
                .document(serviceId)
                .collection("reviews")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun postReview(
        serviceId: String,
        rating: Float,
        comment: String?
    ): Boolean {
        return try {
            val user = auth.currentUser ?: return false

            val userData = db.collection("users").document(user.uid).get().await()
            val reviewUser = ReviewUser(
                user_id = user.uid,
                fname = userData.getString("fname") ?: "",
                lname = userData.getString("lname") ?: "",
                user_img = userData.getString("user_img") ?: ""
            )

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val review = Review(
                review_id = db.collection("services")
                    .document(serviceId)
                    .collection("reviews")
                    .document().id,
                date = currentDate,
                time = currentTime,
                r_service_type = "", // You can fill this based on context
                rating = rating,
                r_comment = comment,
                user = reviewUser
            )

            db.collection("services")
                .document(serviceId)
                .collection("reviews")
                .document(review.review_id)
                .set(review)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}
