package com.example.petto.ui.Services

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
    private lateinit var shopsRecycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: View
    private lateinit var searchBar: EditText

    private var allServices = listOf<PetService>()
    private var clinics = listOf<PetService>()
    private var hotels = listOf<PetService>()
    private var shelters = listOf<PetService>()
    private var shops = listOf<PetService>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services_page)

        // Back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        searchBar = findViewById(R.id.searchBar)
        clinicsRecycler = findViewById(R.id.clinicsRecyclerView)
        hotelsRecycler = findViewById(R.id.hotelsRecyclerView)
        sheltersRecycler = findViewById(R.id.sheltersRecyclerView)
        shopsRecycler = findViewById(R.id.shopsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        contentLayout = findViewById(R.id.scrollView)

        // Recycler layout managers
        clinicsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        hotelsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        sheltersRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        shopsRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        // Initial visibility
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        // Fetch services
        FirebaseFirestore.getInstance().collection("services").get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE

                allServices = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PetService::class.java)?.copy(
                        documentId = doc.id,
                        average_rating = doc.getDouble("average_rating") ?: 0.0
                    )
                }

                applyServiceFilter("")

                searchBar.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        applyServiceFilter(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyServiceFilter(query: String) {
        val q = query.trim().lowercase()

        val filtered = allServices.filter {
            it.name?.lowercase()?.contains(q) == true ||
                    it.service_type?.lowercase()?.contains(q) == true ||
                    it.location?.lowercase()?.contains(q) == true ||
                    it.service?.any { s -> s.lowercase().contains(q) } == true
        }

        clinics = filtered.filter { it.service_type.equals("clinic", true) }
        hotels = filtered.filter { it.service_type.equals("hotel", true) }
        shelters = filtered.filter { it.service_type.equals("shelter", true) }
        shops = filtered.filter { it.service_type.equals("shops", true) }

        clinicsRecycler.adapter = ServicesAdapter(clinics) { openServiceProfile(it) }
        hotelsRecycler.adapter = ServicesAdapter(hotels) { openServiceProfile(it) }
        sheltersRecycler.adapter = ServicesAdapter(shelters) { openServiceProfile(it) }
        shopsRecycler.adapter = ServicesAdapter(shops) { openServiceProfile(it) }
    }

    private fun openServiceProfile(service: PetService) {
        val intent = Intent(this, ServiceProfile::class.java)
        intent.putExtra("service_id", service.documentId)
        startActivity(intent)
    }
}
