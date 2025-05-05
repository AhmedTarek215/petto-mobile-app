package com.example.petto.ui.profiles

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PetProfile : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var breedEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var colorEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var btnEdit: ImageView

    private lateinit var firestore: FirebaseFirestore
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutId = resources.getIdentifier("activity_pet_profile", "layout", packageName)
        setContentView(layoutId)

        firestore = FirebaseFirestore.getInstance()

        nameEditText = findViewById(getResId("name"))
        breedEditText = findViewById(getResId("breed"))
        genderEditText = findViewById(getResId("gender"))
        ageEditText = findViewById(getResId("age"))
        colorEditText = findViewById(getResId("color"))
        weightEditText = findViewById(getResId("weight"))
        heightEditText = findViewById(getResId("height"))
        profileImageView = findViewById(getResId("profileImageView"))
        btnEdit = findViewById(getResId("btnEdit"))

        ageEditText.isEnabled = false

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            loadPetData()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        btnEdit.setOnClickListener {
            updatePetData()
        }
    }

    private fun loadPetData() {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val pet = document.get("pet") as? Map<*, *>
                    if (pet != null) {
                        nameEditText.setText(document.getString("firstName"))
                        breedEditText.setText(pet["breed"]?.toString())
                        genderEditText.setText(document.getString("gender"))

                        val dob = pet["dob"]?.toString()
                        if (!dob.isNullOrEmpty()) {
                            ageEditText.setText(formatAge(dob))
                        } else {
                            ageEditText.setText("")
                        }

                        colorEditText.setText(pet["color"]?.toString())
                        weightEditText.setText(pet["weight"]?.toString())
                        heightEditText.setText(pet["height"]?.toString())
                    } else {
                        Toast.makeText(this, "No pet info found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load pet data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePetData() {
        val updatedPet = hashMapOf(
            "breed" to breedEditText.text.toString(),
            "color" to colorEditText.text.toString(),
            "dob" to "", // keep dob unchanged unless editable
            "weight" to weightEditText.text.toString(),
            "height" to heightEditText.text.toString()
        )

        firestore.collection("Users").document(userId)
            .update("pet", updatedPet)
            .addOnSuccessListener {
                Toast.makeText(this, "Pet info updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatAge(dob: String): String {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = format.parse(dob) ?: return ""
            val now = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate }

            val diffInMillis = now.timeInMillis - birthCal.timeInMillis
            val totalMonths = (diffInMillis / (1000L * 60 * 60 * 24 * 30)).toInt()

            if (totalMonths > 12) {
                val years = totalMonths / 12
                "$years year${if (years > 1) "s" else ""}"
            } else {
                "$totalMonths month${if (totalMonths != 1) "s" else ""}"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getResId(name: String): Int {
        return resources.getIdentifier(name, "id", packageName)
    }
}
