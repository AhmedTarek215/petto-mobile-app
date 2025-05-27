package com.example.petto.ui.Services

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petto.R
import com.example.petto.data.model.PetService
import com.google.firebase.firestore.FirebaseFirestore

class ServicesPage : AppCompatActivity() {

    private lateinit var clinicsRecycler: RecyclerView
    private lateinit var hotelsRecycler: RecyclerView
    private lateinit var sheltersRecycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_page)

        // Back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        clinicsRecycler = findViewById(R.id.clinicsRecyclerView)
        hotelsRecycler = findViewById(R.id.hotelsRecyclerView)
        sheltersRecycler = findViewById(R.id.sheltersRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        contentLayout = findViewById(R.id.scrollView)

        // Initial visibility
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        // Set horizontal layout managers
        clinicsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        hotelsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        sheltersRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        //uploadHotelData()

        // Fetch services from Firestore
        FirebaseFirestore.getInstance().collection("services").get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE

                val services = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PetService::class.java)?.copy(
                        documentId = doc.id,
                        average_rating = doc.getDouble("average_rating") ?: 0.0
                    )
                }

                Log.d("FirestoreDebug", "Fetched services: ${services.size}")

                val clinics = services.filter { it.service_type.equals("clinic", true) }
                val hotels = services.filter { it.service_type.equals("hotel", true) }
                val shelters = services.filter { it.service_type.equals("shelter", true) }

                clinicsRecycler.adapter = ServicesAdapter(clinics) { openServiceProfile(it) }
                hotelsRecycler.adapter = ServicesAdapter(hotels) { openServiceProfile(it) }
                sheltersRecycler.adapter = ServicesAdapter(shelters) { openServiceProfile(it) }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show()
            }

    }

    private fun openServiceProfile(service: PetService) {
        val intent = Intent(this, ServiceProfile::class.java)
        intent.putExtra("service_id", service.documentId)
        startActivity(intent)
    }


}
