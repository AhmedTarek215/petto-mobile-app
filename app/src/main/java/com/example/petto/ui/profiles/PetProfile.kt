package com.example.petto.ui.profiles

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PetProfile : AppCompatActivity() {

    private lateinit var petSelector: Spinner
    private lateinit var profileImageView: CircleImageView
    private lateinit var nameEditText: EditText
    private lateinit var breedEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var colorEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText

    private lateinit var firestore: FirebaseFirestore
    private var userId: String = ""
    private var selectedImageUrl: String? = null

    private val petList = mutableListOf<Map<String, Any>>()
    private val petNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        profileImageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.name)
        breedEditText = findViewById(R.id.breed)
        genderEditText = findViewById(R.id.gender)
        ageEditText = findViewById(R.id.age)
        colorEditText = findViewById(R.id.color)
        weightEditText = findViewById(R.id.weight)
        heightEditText = findViewById(R.id.height)
        petSelector = findViewById(R.id.petSelector)

        ageEditText.isEnabled = false

        profileImageView.setOnClickListener {
            showPetImageSelectionDialog()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<FloatingActionButton>(R.id.addPetButton)?.setOnClickListener {
            startActivity(Intent(this, AddPetActivity::class.java))
        }

        findViewById<View>(R.id.rootLayout).setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        if (userId.isNotEmpty()) {
            loadPetList()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPetList() {
        firestore.collection("Users").document(userId)
            .collection("pets")
            .get()
            .addOnSuccessListener { result ->
                petList.clear()
                petNames.clear()

                for (doc in result) {
                    val data = doc.data
                    val name = data["name"]?.toString()?.trim() ?: "Unnamed"
                    petList.add(data)
                    petNames.add(name)
                }

                if (petList.isEmpty()) {
                    Toast.makeText(this, "No pets found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                petSelector.adapter = adapter

                petSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedName = petNames[position]
                        val selectedPet = petList.firstOrNull { it["name"]?.toString()?.trim() == selectedName }
                        selectedPet?.let { showPetData(it) }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                showPetData(petList[0])
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load pets", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPetData(pet: Map<String, Any>) {
        nameEditText.setText(pet["name"]?.toString() ?: "")
        breedEditText.setText(pet["breed"]?.toString() ?: "")
        genderEditText.setText(pet["gender"]?.toString() ?: "")
        colorEditText.setText(pet["color"]?.toString() ?: "")
        weightEditText.setText(pet["weight"]?.toString() ?: "")
        heightEditText.setText(pet["height"]?.toString() ?: "")

        val dobRaw = pet["dob"]?.toString()
        if (!dobRaw.isNullOrEmpty()) {
            ageEditText.setText(formatAge(dobRaw))
            ageEditText.tag = dobRaw
        } else {
            ageEditText.setText("")
        }

        val imageUrl = pet["imageUrl"]?.toString()
        if (!imageUrl.isNullOrEmpty()) {
            selectedImageUrl = imageUrl
            Glide.with(this).load(imageUrl).into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.pet_care)
        }
    }

    private fun formatAge(dob: String): String {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val birthDate = format.parse(dob) ?: return ""
            val now = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate }

            if (birthCal.after(now)) return "Not born yet"

            val diffInMillis = now.timeInMillis - birthCal.timeInMillis
            val totalMonths = (diffInMillis / (1000L * 60 * 60 * 24 * 30)).toInt()

            if (totalMonths >= 12) {
                val years = totalMonths / 12
                "$years year${if (years > 1) "s" else ""}"
            } else {
                "$totalMonths month${if (totalMonths != 1) "s" else ""}"
            }
        } catch (e: Exception) {
            "Invalid DOB"
        }
    }

    private fun showPetImageSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile_image_selector, null)
        val gridLayout = dialogView.findViewById<GridLayout>(R.id.imageGrid)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Pet Image")
            .setView(dialogView)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        firestore.collection("pet_images").get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val url = doc.getString("url") ?: continue
                    val imageView = ImageView(this).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 250
                            height = 250
                            setMargins(16, 16, 16, 16)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    Glide.with(this).load(url).into(imageView)

                    imageView.setOnClickListener {
                        selectedImageUrl = url
                        Glide.with(this).load(url).into(profileImageView)
                        dialog.dismiss()
                    }

                    gridLayout.addView(imageView)
                }
            }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }
}
