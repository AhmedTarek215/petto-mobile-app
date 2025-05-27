package com.example.petto.ui.Services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
    private lateinit var backButton: ImageView

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
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var contentLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_profile)

        initializeViews()
        setupAdapters()

        selectedServiceId = intent.getStringExtra("service_id") ?: run {
            showError("No service ID provided")
            finish()
            return
        }

        loadServiceData(selectedServiceId)

        serviceWeb.setOnClickListener {
            val url = serviceWeb.text.toString()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(if (!url.startsWith("http")) "http://$url" else url)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
        backButton = findViewById(R.id.btnBack)

        rvServices = findViewById(R.id.rvServices)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        ratingBar = findViewById(R.id.ratingBar)
        averageRatingText = findViewById(R.id.ratingValue)
        addReviewIcon = findViewById(R.id.add)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        contentLayout = findViewById(R.id.contentLayout)
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
                loadingIndicator.visibility = View.VISIBLE
                contentLayout.visibility = View.GONE

                db.collection("services").document(serviceId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            serviceName.text = document.getString("name") ?: ""
                            serviceTime.text = document.getString("time") ?: ""
                            servicePhone.text = document.getString("phone") ?: ""
                            serviceWeb.text = document.getString("web") ?: ""
                            serviceLocation.text = document.getString("location") ?: ""

                            // ✅ Display stored average rating (even if there are no reviews)
                            val avgRating = document.getDouble("average_rating") ?: 0.0
                            ratingBar.rating = avgRating.toFloat()
                            averageRatingText.text = String.format("%.1f", avgRating)

                            val imageUrl = document.getString("imageUrl")
                            Glide.with(this@ServiceProfile)
                                .asBitmap()
                                .load(imageUrl)
                                .placeholder(R.drawable.service_image)
                                .error(R.drawable.service_image)
                                .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                                    override fun onResourceReady(
                                        resource: android.graphics.Bitmap,
                                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                                    ) {
                                        val resized = android.graphics.Bitmap.createScaledBitmap(resource, 600, 320, true)
                                        serviceImage.setImageBitmap(resized)
                                    }

                                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                                        serviceImage.setImageDrawable(placeholder)
                                    }
                                })

                            val serviceNames = document.get("service") as? List<String> ?: emptyList()
                            servicesList.clear()
                            servicesList.addAll(serviceNames.map { PetService(name = it) })
                            servicesAdapter.notifyDataSetChanged()
                        } else {
                            showError("Service not found")
                        }
                        loadingIndicator.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {
                        showError("Failed to load service details.")
                        loadingIndicator.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                    }

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
                                    profileImageUrl = userMap["profileImageUrl"] as? String ?: ""
                                )
                            } else {
                                ReviewUser()
                            }

                            val timestamp = (doc.getTimestamp("timestamp")?.seconds ?: 0L) * 1000L

                            Review(
                                review_id = doc.id,
                                date = date,
                                time = time,
                                rating = rating,
                                r_comment = text,
                                r_service_type = "",
                                user = user,
                                timestamp = timestamp
                            )
                        }

                        reviewsList.clear()
                        reviewsList.addAll(reviews)
                        reviewsAdapter.notifyDataSetChanged()

                        // ✅ Recalculate average rating if there are reviews
                        val totalRating = reviews.sumOf { it.rating.toDouble() }
                        val averageRating = if (reviews.isNotEmpty()) totalRating / reviews.size else null

                        if (averageRating != null) {
                            ratingBar.rating = averageRating.toFloat()
                            averageRatingText.text = String.format("%.1f", averageRating)

                            db.collection("services")
                                .document(serviceId)
                                .update("average_rating", averageRating)
                                .addOnSuccessListener {
                                    Log.d("ServiceProfile", "Average rating updated: $averageRating")
                                }
                                .addOnFailureListener {
                                    Log.e("ServiceProfile", "Failed to update rating: ${it.message}")
                                }
                        }
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

                loadingIndicator.visibility = View.VISIBLE
                contentLayout.visibility = View.GONE

                db.collection("Users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        val fname = userDoc.getString("firstName") ?: ""
                        val lname = userDoc.getString("lastName") ?: ""
                        val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""

                        val reviewData = hashMapOf(
                            "rating" to rating.toDouble(),
                            "text" to (text ?: ""),
                            "timestamp" to Timestamp.now(),
                            "user" to hashMapOf(
                                "user_id" to userId,
                                "fname" to fname,
                                "lname" to lname,
                                "profileImageUrl" to profileImageUrl
                            )
                        )

                        db.collection("services")
                            .document(selectedServiceId)
                            .collection("reviews")
                            .add(reviewData)
                            .addOnSuccessListener {
                                Toast.makeText(this@ServiceProfile, "Review submitted!", Toast.LENGTH_SHORT).show()
                                loadServiceData(selectedServiceId)
                                loadingIndicator.visibility = View.GONE
                                contentLayout.visibility = View.VISIBLE
                            }
                            .addOnFailureListener {
                                showError("Failed to submit review.")
                                loadingIndicator.visibility = View.GONE
                                contentLayout.visibility = View.VISIBLE
                            }
                    }
                    .addOnFailureListener {
                        showError("Failed to fetch user info.")
                        loadingIndicator.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                    }
            }
        })
        dialog.show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
