package com.example.petto.ui.profiles

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPetActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var btnImportPhoto: ImageView
    private lateinit var etName: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var genderError: TextView
    private lateinit var tvDateOfBirth: TextView
    private lateinit var spinnerType: AutoCompleteTextView
    private lateinit var spinnerBreed: AutoCompleteTextView
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var etColor: EditText
    private lateinit var btnDone: Button
    private lateinit var btnCancel: Button

    private lateinit var inputLayoutType: TextInputLayout
    private lateinit var inputLayoutBreed: TextInputLayout

    private var selectedPetImageUrl: String? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addpet)

        profileImage = findViewById(R.id.profileImage)
        btnImportPhoto = findViewById(R.id.btnImportPhoto)
        etName = findViewById(R.id.etName)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        genderError = findViewById(R.id.genderError)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        spinnerType = findViewById(R.id.spinnerType)
        spinnerBreed = findViewById(R.id.spinnerBreed)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        etColor = findViewById(R.id.etColor)
        btnDone = findViewById(R.id.btnDone)
        btnCancel = findViewById(R.id.btnCancel)

        inputLayoutType = findViewById(R.id.textInputLayout1)
        inputLayoutBreed = findViewById(R.id.textInputLayout2)

        val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit")
        val breedsMap = mapOf(
            "Dog" to listOf("Labrador", "Poodle", "Bulldog", "Beagle"),
            "Cat" to listOf("Siamese", "Persian", "Maine Coon", "Bengal"),
            "Bird" to listOf("Parrot", "Canary", "Cockatiel", "Finch"),
            "Rabbit" to listOf("Holland Lop", "Netherland Dwarf", "Mini Rex")
        )

        spinnerType.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, petTypes))

        spinnerType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = petTypes[position]
            inputLayoutType.hint = ""
            inputLayoutType.error = null

            spinnerBreed.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, breedsMap[selectedType] ?: emptyList())
            )
            spinnerBreed.setText("", false)
        }

        spinnerBreed.setOnItemClickListener { _, _, position, _ ->
            inputLayoutBreed.hint = ""
            inputLayoutBreed.error = null
        }

        tvDateOfBirth.setOnClickListener { showDatePicker() }

        radioMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioFemale.isChecked = false
                genderError.visibility = TextView.GONE
            }
        }

        radioFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioMale.isChecked = false
                genderError.visibility = TextView.GONE
            }
        }

        btnImportPhoto.setOnClickListener { showPetImageSelectionDialog() }

        btnDone.setOnClickListener {
            validateAndSave()
        }

        btnCancel.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            tvDateOfBirth.text = formattedDate
            tvDateOfBirth.setTextColor(resources.getColor(R.color.black, theme))
            tvDateOfBirth.error = null
        }, year, month, day).show()
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

                    Glide.with(dialogView).load(url).into(imageView)

                    imageView.setOnClickListener {
                        selectedPetImageUrl = url
                        Glide.with(this).load(url).into(profileImage)
                        dialog.dismiss()
                    }

                    gridLayout.addView(imageView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load pet images", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateAndSave() {
        val petName = etName.text.toString().trim()
        val gender = when {
            radioMale.isChecked -> "Male"
            radioFemale.isChecked -> "Female"
            else -> ""
        }
        val dob = tvDateOfBirth.text.toString().trim()
        val type = spinnerType.text.toString().trim()
        val breed = spinnerBreed.text.toString().trim()
        val weight = etWeight.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val color = etColor.text.toString().trim()

        var isValid = true

        if (petName.isEmpty()) {
            etName.error = "Name is required"
            isValid = false
        }

        if (gender.isEmpty()) {
            genderError.text = "Please select gender"
            genderError.visibility = TextView.VISIBLE
            isValid = false
        }

        if (dob.isEmpty() || dob == "Select Date") {
            tvDateOfBirth.error = "Select date of birth"
            isValid = false
        }

        if (type.isEmpty()) {
            inputLayoutType.error = "Please select a pet type."
            isValid = false
        }

        if (breed.isEmpty()) {
            inputLayoutBreed.error = "Please select a breed."
            isValid = false
        }

        if (weight.isEmpty()) {
            etWeight.error = "Required"
            isValid = false
        }

        if (height.isEmpty()) {
            etHeight.error = "Required"
            isValid = false
        }

        if (color.isEmpty()) {
            etColor.error = "Required"
            isValid = false
        }

        if (!isValid) return

        btnDone.isEnabled = false // prevent multiple clicks

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val petData = mapOf(
            "name" to petName,
            "gender" to gender,
            "dob" to dob,
            "type" to type,
            "breed" to breed,
            "weight" to weight,
            "height" to height,
            "color" to color,
            "imageUrl" to selectedPetImageUrl
        )

        val petsRef = FirebaseFirestore.getInstance()
            .collection("Users").document(userId).collection("pets")

        Log.d("AddPet", "Saving pet to path: Users/$userId/pets")

        petsRef.add(petData)
            .addOnSuccessListener {
                Log.d("AddPet", "Pet added successfully!")
                Toast.makeText(this, "Pet added successfully!", Toast.LENGTH_SHORT).show()

                // Delay finish so toast can be seen
                profileImage.postDelayed({
                    val intent = Intent(this, com.example.petto.ui.profiles.PetProfile::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }, 10)

            }
            .addOnFailureListener { e ->
                btnDone.isEnabled = true
                Toast.makeText(this, "Failed to save pet.", Toast.LENGTH_SHORT).show()
                Log.e("AddPet", "Error: ${e.message}")
            }
    }
}
