package com.example.petto.ui.SignUp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import java.io.IOException

class SignUp2 : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var btnImportPhoto: ImageView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var confirmPasswordToggle: ImageView
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: SignUpProgressBar

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

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
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up2)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        profileImage = findViewById(R.id.profileImage)
        btnImportPhoto = findViewById(R.id.btnImportPhoto)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        passwordToggle = findViewById(R.id.passwordToggle)
        confirmPasswordToggle = findViewById(R.id.ConfirmpasswordToggle)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBar)

        // Set progress bar step
        val step = intent.getIntExtra("progress", 2) // Default is step 1
        progressBar.setProgress(step) // Set the new progress

        // Image Import Functionality
        btnImportPhoto.setOnClickListener {
            showImagePickerDialog()
        }

        // Toggle password visibility
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etPassword, isPasswordVisible, passwordToggle)
        }

        confirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible, confirmPasswordToggle)
        }

        // Navigate back to SignUp1
        btnBack.setOnClickListener {
            val intent = Intent(this, SignUp1::class.java)
            intent.putExtra("progress", step - 1) // Update progress
            startActivity(intent)
            finish()
        }

        btnNext.setOnClickListener {
            if (validateFields()) {
                val currentStep = intent.getIntExtra("progress", 1) // Get current progress
                Log.d("SignUp2", "Current progress: $currentStep") // Debugging line

                val intent = Intent(this, SignUp3::class.java)
                intent.putExtra("progress", currentStep + 1) // Correctly increment progress
                startActivity(intent)
                finish()
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
            profileImage.setImageBitmap(resizeBitmap(bitmap))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val targetWidth = profileImage.width
        val targetHeight = profileImage.height

        return if (targetWidth > 0 && targetHeight > 0) {
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        } else {
            bitmap
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean, toggleIcon: ImageView) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT
            toggleIcon.setImageResource(R.drawable.visibility) // Show eye icon
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.crossed_eye) // Hide eye icon
        }
        editText.setSelection(editText.text.length) // Maintain cursor position
    }

    private fun validateFields(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        var isValid = true

        // Clear previous errors
        etEmail.error = null
        etPassword.error = null
        etConfirmPassword.error = null

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirm your password"
            isValid = false
        }

        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
}
