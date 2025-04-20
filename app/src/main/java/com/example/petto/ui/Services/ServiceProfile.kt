package com.example.petto.ui.Services

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.PetService
import com.example.petto.data.model.Review
import com.example.petto.data.model.ReviewUser
import com.example.petto.data.repository.ServiceRepository
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.*
import android.widget.ImageView

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

        selectedServiceId = intent.getStringExtra("SERVICE_ID") ?: run {
            showError("No service ID provided")
            finish()
            return
        }

        loadServiceData(selectedServiceId)

        addReviewIcon.setOnClickListener {
            //showReviewDialog()
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
        addReviewIcon = findViewById(R.id.add)
    }

    private fun setupAdapters() {
        rvServices.layoutManager = GridLayoutManager(this, 2)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)

        //servicesAdapter = ServiceAdapter(servicesList)
        //rvServices.adapter = servicesAdapter

        reviewsAdapter = ReviewsAdapter(reviewsList)
        reviewsRecyclerView.adapter = reviewsAdapter
    }

    private fun loadServiceData(serviceId: String) {
        lifecycleScope.launch {
            try {
                db.collection("services").document(serviceId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        serviceName.text = document.getString("name") ?: ""
                        serviceTime.text = document.getString("time") ?: ""
                        servicePhone.text = document.getString("phone") ?: ""
                        serviceWeb.text = document.getString("web") ?: ""
                        serviceLocation.text = document.getString("location") ?: ""

                        val imageUrl = document.getString("imageUrl")
                        Glide.with(this@ServiceProfile).load(imageUrl).into(serviceImage)

                        val serviceNames = document.get("services") as? List<String> ?: emptyList()
                        servicesList.clear()
                        servicesList.addAll(serviceNames.map { PetService(it) })
                        servicesAdapter.notifyDataSetChanged()
                    } else {
                        showError("Service not found")
                    }
                }

                db.collection("services").document(serviceId)
                    .collection("reviews")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val reviews = querySnapshot.documents.mapNotNull { doc ->
                            val rating = (doc.getDouble("rating") ?: 0.0).toFloat()
                            val text = doc.getString("text")
                            val timestamp = doc.getString("timestamp") ?: ""

                            val date = doc.getString("date") ?: ""
                            val time = doc.getString("time") ?: ""

                            val userMap = doc.get("user") as? Map<*, *>
                            val user = if (userMap != null) {
                                ReviewUser(
                                    user_id = userMap["user_id"] as? String ?: "",
                                    fname = userMap["fname"] as? String ?: "",
                                    lname = userMap["lname"] as? String ?: "",
                                    user_img = userMap["user_img"] as? String ?: ""
                                )
                            } else {
                                ReviewUser()
                            }

                            Review(
                                review_id = doc.id,
                                date = date,
                                time = time,
                                rating = rating,
                                r_comment = text,
                                r_service_type = "", // Optionally fetch if stored
                                user = user
                            )
                        }

                        reviewsList.clear()
                        reviewsList.addAll(reviews)
                        reviewsAdapter.notifyDataSetChanged()
                    }

            } catch (e: Exception) {
                showError("Failed to load data: ${e.message}")
            }
        }
    }

    /*private fun showReviewDialog() {
        val dialog = ReviewDialog(this, object : ReviewDialog.ReviewSubmitListener {
            override fun onReviewSubmitted(rating: Float, text: String?) {
                val user = auth.currentUser ?: return
                val userId = user.uid

                db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                    val userName = userDoc.getString("name") ?: "Anonymous"
                    val profileImage = userDoc.getString("profileImageUrl") ?: ""

                    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    val date = timestamp.split(" ").getOrNull(0) ?: ""
                    val time = timestamp.split(" ").getOrNull(1) ?: ""

                    val reviewData = hashMapOf(
                        "rating" to rating.toDouble(),
                        "text" to (text ?: ""),
                        "timestamp" to timestamp,
                        "date" to date,
                        "time" to time,
                        "user" to hashMapOf(
                            "user_id" to userId,
                            "fname" to userName.split(" ").firstOrNull(),
                            "lname" to userName.split(" ").getOrNull(1),
                            "user_img" to profileImage
                        )
                    )

                    db.collection("services").document(selectedServiceId)
                        .collection("reviews")
                        .add(reviewData)
                        .addOnSuccessListener {
                            Toast.makeText(this@ServiceProfile, "Review submitted!", Toast.LENGTH_SHORT).show()
                            loadServiceData(selectedServiceId)
                        }
                }
            }
        })
        dialog.show()
    }*/

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
