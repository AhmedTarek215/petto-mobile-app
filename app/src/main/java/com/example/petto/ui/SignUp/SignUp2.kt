package com.example.petto.ui.SignUp



import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.example.petto.data.viewModel.SignUpViewModel

class SignUp2 : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)

        // Initialize views
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
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            isValid = false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirm your password"
            isValid = false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
}
