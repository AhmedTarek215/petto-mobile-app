package com.example.petto.ui.Services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.data.model.PetService
import com.example.petto.data.model.Review
import com.example.petto.data.model.ReviewUser
import com.example.petto.data.repository.ServiceRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ServiceProfile : AppCompatActivity() {

    private lateinit var serviceImage: ImageView
    private lateinit var serviceName: TextView
    private lateinit var serviceTime: TextView
    private lateinit var servicePhone: TextView
    private lateinit var serviceWeb: TextView
    private lateinit var serviceLocation: TextView

    private lateinit var rvServices: RecyclerView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var ratingBar: RatingBar
    private lateinit var addReviewIcon: ImageView
    private lateinit var averageRatingText: TextView

    private lateinit var servicesAdapter: ServiceAdapter
    private lateinit var reviewsAdapter: ReviewsAdapter

    private var servicesList = mutableListOf<PetService>()
    private var reviewsList = mutableListOf<Review>()

    private val repository = ServiceRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedServiceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_profile)

        initializeViews()
        setupAdapters()

        selectedServiceId = "service1"

        /*selectedServiceId = intent.getStringExtra("SERVICE_ID") ?: run {
            showError("No service ID provided")
            finish()
            return
        }*/

        loadServiceData(selectedServiceId)

        serviceWeb.setOnClickListener {
            val url = serviceWeb.text.toString()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(if (!url.startsWith("http")) "http://$url" else url)
            startActivity(intent)
        }

        addReviewIcon.setOnClickListener {
            showReviewDialog()
        }

    }

    private fun initializeViews() {
        serviceImage = findViewById(R.id.imgService)
        serviceName = findViewById(R.id.ServiceName)
        serviceTime = findViewById(R.id.time)
        servicePhone = findViewById(R.id.phone)
        serviceWeb = findViewById(R.id.web)
        serviceLocation = findViewById(R.id.location)

        rvServices = findViewById(R.id.rvServices)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        ratingBar = findViewById(R.id.ratingBar)
        averageRatingText = findViewById(R.id.ratingValue)
        addReviewIcon = findViewById(R.id.add)
    }

    private fun setupAdapters() {
        rvServices.layoutManager = GridLayoutManager(this, 2)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)

        servicesAdapter = ServiceAdapter(servicesList)
        rvServices.adapter = servicesAdapter

        reviewsAdapter = ReviewsAdapter(reviewsList)
        reviewsRecyclerView.adapter = reviewsAdapter
    }

    private fun loadServiceData(serviceId: String) {
        lifecycleScope.launch {
            try {
                // Load service details
                db.collection("services").document(serviceId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            serviceName.text = document.getString("name") ?: ""
                            serviceTime.text = document.getString("time") ?: ""
                            servicePhone.text = document.getString("phone") ?: ""
                            serviceWeb.text = document.getString("web") ?: ""
                            serviceLocation.text = document.getString("location") ?: ""

                            val imageUrl = document.getString("imageUrl")
                            Glide.with(this@ServiceProfile)
                                .load(imageUrl)
                                .placeholder(R.drawable.service_image) // Optional
                                .error(R.drawable.service_image)           // Show fallback if image fails
                                .into(serviceImage)

                            Log.d("ServiceProfile", "Image URL: $imageUrl")


                            val serviceNames = document.get("service") as? List<String> ?: emptyList()
                            servicesList.clear()
                            servicesList.addAll(serviceNames.map { PetService(name = it) })
                            servicesAdapter.notifyDataSetChanged()
                        } else {
                            showError("Service not found")
                        }
                    }
                    .addOnFailureListener {
                        showError("Failed to load service details.")
                    }

                // Load reviews
                db.collection("services").document(serviceId)
                    .collection("reviews")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val reviews = querySnapshot.documents.mapNotNull { doc ->
                            val rating = (doc.getDouble("rating") ?: 0.0).toFloat()
                            val text = doc.getString("text")
                            val date = doc.getString("date") ?: ""
                            val time = doc.getString("time") ?: ""

                            val userMap = doc.get("user") as? Map<*, *>
                            val user = if (userMap != null) {
                                ReviewUser(
                                    user_id = userMap["user_id"] as? String ?: "",
                                    fname = userMap["fname"] as? String ?: "",
                                    lname = userMap["lname"] as? String ?: "",
                                    user_img = "" // No image for now
                                )
                            } else {
                                ReviewUser()
                            }

                            // Fetching timestamp as a Long (milliseconds)
                            val timestamp = (doc.getTimestamp("timestamp")?.seconds ?: 0L) * 1000L // Convert seconds to milliseconds

                            Review(
                                review_id = doc.id,
                                date = date,
                                time = time,
                                rating = rating,
                                r_comment = text,
                                r_service_type = "", // optional, leave empty
                                user = user,
                                timestamp = timestamp // Use the proper Long timestamp
                            )
                        }

                        reviewsList.clear()
                        reviewsList.addAll(reviews)
                        reviewsAdapter.notifyDataSetChanged()

                        // ✅ Calculate and display average rating
                        val totalRating = reviews.sumOf { it.rating.toDouble() }
                        val averageRating = if (reviews.isNotEmpty()) totalRating / reviews.size else 0.0
                        ratingBar.rating = averageRating.toFloat()
                        averageRatingText.text = String.format("%.1f", averageRating)


                    }
                    .addOnFailureListener {
                        showError("Failed to load reviews.")
                    }


            } catch (e: Exception) {
                showError("Failed to load data: ${e.message}")
            }
        }
    }

    private fun showReviewDialog() {
        val dialog = ReviewDialog(this, object : ReviewDialog.ReviewSubmitListener {
            override fun onReviewSubmitted(rating: Float, text: String?) {
                val user = auth.currentUser ?: return
                val userId = user.uid

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val fname = userDoc.getString("fname") ?: ""
                        val lname = userDoc.getString("lname") ?: ""

                        val reviewData = hashMapOf(
                            "rating" to rating.toDouble(),
                            "text" to (text ?: ""),
                            "timestamp" to Timestamp.now(), // Native Firestore Timestamp!
                            "user" to hashMapOf(
                                "user_id" to userId,
                                "fname" to fname,
                                "lname" to lname
                                // No user_img for now
                            )
                        )

                        db.collection("services")
                            .document(selectedServiceId)
                            .collection("reviews")
                            .add(reviewData)
                            .addOnSuccessListener {
                                Toast.makeText(this@ServiceProfile, "Review submitted!", Toast.LENGTH_SHORT).show()
                                loadServiceData(selectedServiceId) // Refresh reviews
                            }
                            .addOnFailureListener {
                                showError("Failed to submit review.")
                            }
                    }
                    .addOnFailureListener {
                        showError("Failed to fetch user info.")
                    }
            }
        })
        dialog.show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
