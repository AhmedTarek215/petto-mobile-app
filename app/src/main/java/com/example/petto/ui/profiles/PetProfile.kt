package com.example.petto.ui.profiles

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PetProfile : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var breedEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var colorEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var profileImageView: CircleImageView

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

        findViewById<View>(R.id.rootLayout).setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        savePetData()
    }

    private fun savePetData() {
        val petData = mutableMapOf<String, Any>(
            "name" to nameEditText.text.toString().trim(),
            "breed" to breedEditText.text.toString().trim(),
            "color" to colorEditText.text.toString().trim(),
            "weight" to weightEditText.text.toString().trim(),
            "height" to heightEditText.text.toString().trim(),
            "imageUrl" to (selectedImageUrl ?: "")
        )

        val currentDob = ageEditText.tag as? String
        if (!currentDob.isNullOrEmpty()) {
            petData["dob"] = currentDob
        }

        firestore.collection("Users").document(userId)
            .update("pet", petData)
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save pet data", Toast.LENGTH_SHORT).show()
            }

        firestore.collection("Users").document(userId)
            .update("gender", genderEditText.text.toString().trim())
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

                        colorEditText.setText(pet["color"]?.toString())
                        weightEditText.setText(pet["weight"]?.toString())
                        heightEditText.setText(pet["height"]?.toString())

                        val imageUrl = pet["imageUrl"]?.toString()
                        if (!imageUrl.isNullOrEmpty()) {
                            selectedImageUrl = imageUrl
                            Glide.with(this).load(imageUrl).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.pet_care)
                        }

                        val dobRaw = pet["dob"]
                        if (dobRaw != null) {
                            val dob = dobRaw.toString().trim().replace("\\s+".toRegex(), "")
                            ageEditText.setText(formatAge(dob))
                            ageEditText.tag = dob
                            Log.d("DOB_DEBUG", "DOB parsed successfully: $dob")
                        } else {
                            ageEditText.setText("")
                            ageEditText.tag = null
                            Log.d("DOB_DEBUG", "DOB is null in Firestore")
                        }
                    } else {
                        Toast.makeText(this, "No pet data found", Toast.LENGTH_SHORT).show()
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
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val birthDate = format.parse(dob) ?: return ""
            val now = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate }

            if (birthCal.after(now)) return "Not born yet"

            val diffInMillis = now.timeInMillis - birthCal.timeInMillis
            val totalMonths = (diffInMillis / (1000L * 60 * 60 * 24 * 30)).toInt()

            return if (totalMonths >= 12) {
                val years = totalMonths / 12
                "$years year${if (years > 1) "s" else ""}"
            } else {
                "$totalMonths month${if (totalMonths != 1) "s" else ""}"
            }
        } catch (e: Exception) {
            Log.e("DOB_FORMAT", "Error formatting DOB: ${e.message}")
            "Invalid DOB"
        }
    }

    private fun getResId(name: String): Int {
        return resources.getIdentifier(name, "id", packageName)
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
