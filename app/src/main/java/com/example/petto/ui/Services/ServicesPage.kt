package com.example.petto.ui.Services

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_page)

        // Back button behavior
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup RecyclerViews
        clinicsRecycler = findViewById(R.id.clinicsRecyclerView)
        hotelsRecycler = findViewById(R.id.hotelsRecyclerView)
        sheltersRecycler = findViewById(R.id.sheltersRecyclerView)

        clinicsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        hotelsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        sheltersRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        // Fetch services from Firestore
        FirebaseFirestore.getInstance().collection("services").get()
            .addOnSuccessListener { snapshot ->
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
                Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openServiceProfile(service: PetService) {
        val intent = Intent(this, ServiceProfile::class.java)
        intent.putExtra("service_id", service.documentId)
        startActivity(intent)
    }
}
