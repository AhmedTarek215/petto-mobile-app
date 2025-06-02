package com.example.petto.ui.SignUp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.example.petto.data.viewModel.SignUpViewModel
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class SignUp2 : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
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
    private var selectedImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)

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

        val step = intent.getIntExtra("progress", 2)
        progressBar.setProgress(step)

        etEmail.setText(SignUpViewModel.email)
        etPassword.setText(SignUpViewModel.password)
        etConfirmPassword.setText(SignUpViewModel.password)

        SignUpViewModel.profileImageUrl?.let { url ->
            selectedImageUrl = url
            SignUpViewModel.profileImageUrl?.let { url ->
                selectedImageUrl = url
                profileImage.post {
                    Glide.with(this)
                        .load(url)
                        .centerCrop()
                        .circleCrop()
                        .into(profileImage)
                }
            }

        }

        btnImportPhoto.setOnClickListener {
            showImageSelectionDialog()
        }

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etPassword, isPasswordVisible, passwordToggle)
        }

        confirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible, confirmPasswordToggle)
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, SignUp1::class.java)
            intent.putExtra("progress", step - 1)
            startActivity(intent)
            finish()
        }

        btnNext.setOnClickListener {
            if (validateFields()) {
                SignUpViewModel.email = etEmail.text.toString().trim()
                SignUpViewModel.password = etPassword.text.toString().trim()
                SignUpViewModel.profileImageUrl = selectedImageUrl

                val intent = Intent(this, SignUp3::class.java)
                intent.putExtra("progress", step + 1)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean, toggleIcon: ImageView) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT
            toggleIcon.setImageResource(R.drawable.visibility)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.crossed_eye)
        }
        editText.setSelection(editText.text.length)
    }

    private fun validateFields(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun showImageSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_profile_image_selector, null)
        val gridLayout = dialogView.findViewById<GridLayout>(R.id.imageGrid)

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Profile Image")
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()

        FirebaseFirestore.getInstance().collection("profile_images")
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
                        selectedImageUrl = url
                        Glide.with(this).load(url).centerCrop().circleCrop().into(profileImage)
                        dialog.dismiss()
                    }

                    gridLayout.addView(imageView)
                }
            }
            .addOnFailureListener {e ->
                Toast.makeText(this, "Could not load images: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }
}
