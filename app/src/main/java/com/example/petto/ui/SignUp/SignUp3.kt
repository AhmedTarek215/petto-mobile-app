package com.example.petto.ui.SignUp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.example.petto.data.viewModel.SignUpViewModel
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class SignUp3 : AppCompatActivity() {

    private lateinit var progressBar: SignUpProgressBar
    private lateinit var etName: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var tvDateOfBirth: TextView
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var genderError: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var btnImportPhoto: ImageView

    private var selectedPetImageUrl: String? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up3)

        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar)
        etName = findViewById(R.id.etName)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
        genderError = findViewById(R.id.genderError)
        profileImage = findViewById(R.id.profileImage)
        btnImportPhoto = findViewById(R.id.btnImportPhoto)

        val step = intent.getIntExtra("progress", 3)
        progressBar.setProgress(if (step > 3) 3 else step)

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

        btnImportPhoto.setOnClickListener {
            showPetImageSelectionDialog()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, SignUp2::class.java)
            intent.putExtra("progress", step - 1)
            startActivity(intent)
            finish()
        }

        btnNext.setOnClickListener {
            validateAndProceed()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            tvDateOfBirth.text = formattedDate
            tvDateOfBirth.setTextColor(resources.getColor(R.color.black, theme))
            tvDateOfBirth.error = null
        }, year, month, day)

        datePickerDialog.show()
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

    private fun validateAndProceed() {
        val petName = etName.text.toString().trim()
        val gender = when {
            radioMale.isChecked -> "Male"
            radioFemale.isChecked -> "Female"
            else -> ""
        }
        val dob = tvDateOfBirth.text.toString().trim()

        var isValid = true

        if (petName.isEmpty()) {
            etName.error = "Name is required"
            isValid = false
        }

        if (gender.isEmpty()) {
            genderError.text = "Please select gender"
            genderError.visibility = TextView.VISIBLE
            isValid = false
        } else {
            genderError.visibility = TextView.GONE
        }

        if (dob == "Select Date" || dob.isEmpty()) {
            tvDateOfBirth.error = "Select date of birth"
            isValid = false
        }

        if (!isValid) return

        // Save to ViewModel
        SignUpViewModel.petName = petName
        SignUpViewModel.petGender = gender
        SignUpViewModel.petBirthDate = dob
        SignUpViewModel.petImageUrl = selectedPetImageUrl

        val intent = Intent(this, SignUp4::class.java)
        intent.putExtra("progress", 4)
        startActivity(intent)
        finish()
    }
}
