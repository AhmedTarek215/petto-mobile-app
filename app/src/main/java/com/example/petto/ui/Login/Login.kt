package com.example.petto.ui.Login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.petto.R
import com.example.petto.ui.SignUp.SignUp1

class Login : AppCompatActivity() {

    private var isPasswordVisible = false // Track password visibility state

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find Views
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val passwordToggle = findViewById<ImageView>(R.id.passwordToggle)
        val tvSignUP = findViewById<TextView>(R.id.tvSignUp)

        // **Toggle Password Visibility**
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible // Toggle state

            if (isPasswordVisible) {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.visibility) // Change to open eye icon
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.crossed_eye) // Change back to closed eye
            }
            etPassword.setSelection(etPassword.text.length) // Keep cursor at the end
        }

        // **Customize "Sign Up" TextView**
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length

        // Apply color to "Sign Up"
        val colorSpan = ForegroundColorSpan(Color.parseColor("#D16B78"))
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply custom font (if exists)
        val typeface = ResourcesCompat.getFont(this, R.font.alexandriamedium)
        if (typeface != null) {
            spannableString.setSpan(TypefaceSpan(typeface), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tvSignUP.text = spannableString

        // **Navigate to SignUp1 when clicking tvSignUP**
        tvSignUP.setOnClickListener {
            val intent = Intent(this, SignUp1::class.java)
            startActivity(intent)
        }
    }
}
