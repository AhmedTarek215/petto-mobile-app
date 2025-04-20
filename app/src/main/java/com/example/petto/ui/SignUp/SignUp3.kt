package com.example.petto.ui.SignUp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignUp3 : AppCompatActivity() {

    private lateinit var progressBar: SignUpProgressBar
    private lateinit var etName: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var tvDateOfBirth: TextView
    private lateinit var btnImportPhoto: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var genderError: TextView  // Error message for gender

    private var imageUri: Uri? = null
    private val calendar = Calendar.getInstance()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                resizeAndSetImage(it) // Resize and set image from gallery
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    profileImage.setImageBitmap(resizeBitmap(it)) // Resize and set image from camera
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up3)

        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar)
        etName = findViewById(R.id.etName)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        btnImportPhoto = findViewById(R.id.btnImportPhoto)
        profileImage = findViewById(R.id.profileImage)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
        genderError = findViewById(R.id.genderError) // Initialize gender error TextView

        // Set progress bar for the third step
        val step = intent.getIntExtra("progress", 3)
        Log.d("SignUp3", "Received progress: $step") // Debugging

        val fixedStep = if (step > 3) 3 else step // Prevent incorrect jumps
        progressBar.setProgress(fixedStep)

        // Handle Image Picker
        btnImportPhoto.setOnClickListener { showImagePickerDialog() }

        // Handle Date Picker
        tvDateOfBirth.setOnClickListener { showDatePicker() }

        // Navigation buttons
        btnBack.setOnClickListener { navigateTo(SignUp2::class.java) }
        btnNext.setOnClickListener { validateAndProceed() }

        // Handle Radio Button Selection
        radioMale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioFemale.isChecked = false // Uncheck the other radio button
                genderError.visibility = View.GONE // Hide error message when selected
            }
        }

        radioFemale.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioMale.isChecked = false // Uncheck the other radio button
                genderError.visibility = View.GONE // Hide error message when selected
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun resizeAndSetImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            profileImage.post {
                profileImage.setImageBitmap(resizeBitmap(bitmap))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        profileImage.post {
            val targetSize = minOf(profileImage.width, profileImage.height) // Get the smaller dimension
            if (targetSize > 0) {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
                profileImage.setImageBitmap(scaledBitmap)
            }
        }
        return bitmap
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvDateOfBirth.text = dateFormat.format(calendar.time)
            tvDateOfBirth.setTextColor(resources.getColor(R.color.black, theme)) // Change color after selection
            tvDateOfBirth.error = null // Clear error when user selects a date
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun validateAndProceed() {
        val name = etName.text.toString().trim()
        val genderSelected = radioMale.isChecked || radioFemale.isChecked
        val dobSelected = tvDateOfBirth.text != "Select Date"

        var isValid = true

        // Validate Name
        if (name.isEmpty()) {
            etName.error = "Name is required"
            isValid = false
        }

        // Validate Gender
        if (!genderSelected) {
            genderError.text = "Please select gender"
            genderError.visibility = View.VISIBLE
            isValid = false
        } else {
            genderError.visibility = View.GONE
        }

        // Validate Date of Birth
        if (!dobSelected) {
            tvDateOfBirth.error = "Select date of birth"
            isValid = false
        } else {
            tvDateOfBirth.error = null
        }

        if (!isValid) return // Stop execution if validation fails

        // Proceed to next screen (Sign Up Page 4)
        navigateTo(SignUp4::class.java)
    }

    private fun navigateTo(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
    }
}
