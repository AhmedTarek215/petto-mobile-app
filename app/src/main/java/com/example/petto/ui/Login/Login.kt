package com.example.petto.ui.Login



import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.petto.R
import com.example.petto.ui.SignUp.SignUp1
import com.example.petto.ui.post.CreatePostActivity
import com.example.petto.ui.profiles.PetProfile
import com.example.petto.ui.profiles.UserProfile
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var passwordToggle: ImageView
    private lateinit var tvSignUP: TextView
    private lateinit var tvForgetPassword: TextView
    private var isPasswordVisible = false

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find Views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        passwordToggle = findViewById(R.id.passwordToggle)
        tvSignUP = findViewById(R.id.tvSignUp)
        tvForgetPassword = findViewById(R.id.tvForgetPassword)

        // Toggle password visibility
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.visibility)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.crossed_eye)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Highlight "Sign Up"
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)
        val startIndex = fullText.indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#D16B78")), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val typeface = ResourcesCompat.getFont(this, R.font.alexandriamedium)
        if (typeface != null) {
            spannableString.setSpan(TypefaceSpan(typeface), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tvSignUP.text = spannableString
        tvSignUP.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        // 🔐 Firebase Login Logic
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, UserProfile::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }




        // 🛠 Handle Forgot Password
        tvForgetPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_forgot_password, null)
        val etDialogEmail = dialogView.findViewById<EditText>(R.id.etDialogEmail)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog) // 💖 add custom style
            .setView(dialogView)
            .setPositiveButton("Send Reset Link", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setTextColor(resources.getColor(R.color.progress_active, theme)) // 🌸 make pink
            negativeButton.setTextColor(resources.getColor(R.color.black, theme)) // optional gray

            positiveButton.setOnClickListener {
                val email = etDialogEmail.text.toString().trim()
                if (email.isEmpty()) {
                    etDialogEmail.error = "Please enter your email"
                } else {
                    sendPasswordResetEmail(email)
                    dialog.dismiss()
                }
            }

            negativeButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
        val window = dialog.window
        window?.attributes?.windowAnimations = R.style.DialogAnimation

    }

    private fun showLoadingDialog(): AlertDialog {
        val loadingView = LinearLayout(this)
        loadingView.orientation = LinearLayout.VERTICAL
        loadingView.gravity = Gravity.CENTER
        loadingView.setPadding(30, 30, 30, 30)

        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        loadingView.addView(progressBar)

        val dialog = AlertDialog.Builder(this)
            .setView(loadingView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
        return dialog
    }

    private fun sendPasswordResetEmail(email: String) {
        val loadingDialog = showLoadingDialog() // show loading spinner

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                loadingDialog.dismiss() // hide spinner when done
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send reset link: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


}