package com.example.petto.ui.profiles

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
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
    private lateinit var profileImageView: CircleImageView
//    private lateinit var btnImportPetPhoto: ImageView

    private lateinit var firestore: FirebaseFirestore
    private var userId: String = ""
    private var selectedImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

        firestore = FirebaseFirestore.getInstance()

        nameEditText = findViewById(getResId("name"))
        breedEditText = findViewById(getResId("breed"))
        genderEditText = findViewById(getResId("gender"))
        ageEditText = findViewById(getResId("age"))
        colorEditText = findViewById(getResId("color"))
        weightEditText = findViewById(getResId("weight"))
        heightEditText = findViewById(getResId("height"))
        profileImageView = findViewById(getResId("profileImageView"))
//        btnImportPetPhoto = findViewById(R.id.btnImportPetPhoto)

        ageEditText.isEnabled = false

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            loadPetData()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
        profileImageView.setOnClickListener {
            showPetImageSelectionDialog()
        }
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // closes the activity and returns to previous screen
        }

//        btnImportPetPhoto.setOnClickListener  {
//            showPetImageSelectionDialog()
//        }
        fun showPetImageSelectionDialog() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_profile_image_selector, null)
            val gridLayout = dialogView.findViewById<GridLayout>(R.id.imageGrid)

            val builder = AlertDialog.Builder(this)
                .setTitle("Select Pet Image")
                .setView(dialogView)
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            val dialog = builder.create()
            dialog.show()

            FirebaseFirestore.getInstance().collection("pet_images")
                .get()
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
                            // Update image view
                            Glide.with(this).load(url).into(profileImageView)

                            // Save imageUrl to Firestore
                            firestore.collection("Users").document(userId)
                                .update("pet.imageUrl", url)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Pet image updated!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to update image", Toast.LENGTH_SHORT).show()
                                }

                            dialog.dismiss()
                        }

                        gridLayout.addView(imageView)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not load pet images", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun loadPetData() {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val pet = document.get("pet") as? Map<*, *>
                    if (pet != null) {
                        nameEditText.setText(pet["name"]?.toString())
                        breedEditText.setText(pet["breed"]?.toString())
                        genderEditText.setText(document.getString("gender"))

                        val imageUrl = pet["imageUrl"]?.toString()
                        if (!imageUrl.isNullOrEmpty()) {
                            selectedImageUrl = imageUrl
                            Glide.with(this).load(imageUrl).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.profile)
                        }

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

    private fun showPetImageSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile_image_selector, null)
        val gridLayout = dialogView.findViewById<GridLayout>(R.id.imageGrid)

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Pet Image")
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()

        firestore.collection("pet_images")
            .get()
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

                        firestore.collection("Users").document(userId)
                            .update("pet.imageUrl", url)
                        dialog.dismiss()
                    }

                    gridLayout.addView(imageView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load images.", Toast.LENGTH_SHORT).show()
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
