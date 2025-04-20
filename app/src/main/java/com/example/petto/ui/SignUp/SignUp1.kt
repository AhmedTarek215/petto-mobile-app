package com.example.petto.ui.SignUp

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import android.text.Spanned
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.ui.Login.Login
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.google.android.material.textfield.TextInputLayout

class SignUp1 : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerCity: AutoCompleteTextView
    private lateinit var spinnerArea: AutoCompleteTextView
    private lateinit var spinnerGender: AutoCompleteTextView
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up1)

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etAge = findViewById(R.id.etAge)
        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerArea = findViewById(R.id.spinnerarea)
        spinnerGender = findViewById(R.id.spinnerGender)
        btnNext = findViewById(R.id.btnNext)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // **Set "Login" as clickable and colored text**
        val text = "Already have an account? Login"
        val spannableString = SpannableString(text)
        val pinkColor = ForegroundColorSpan(Color.parseColor("#D16B78"))
        spannableString.setSpan(pinkColor, text.indexOf("Login"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvLogin.text = spannableString

        // **Navigate to Login screen when "Login" is clicked**
        tvLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // **Progress bar setup**
        val progressBar = findViewById<SignUpProgressBar>(R.id.progressBar)
        progressBar.setProgress(1)

        btnNext.setOnClickListener {
            val intent = Intent(this, SignUp2::class.java)
            intent.putExtra("progress", 2)
            startActivity(intent)
        }

        // **Setup dropdown lists**
        setupCityDropdown()
        setupGenderDropdown()
    }

    // **Validation function**
    private fun validateFields(): Boolean {
        if (etFirstName.text.isEmpty()) {
            etFirstName.error = "First Name is required"
            return false
        }
        if (etLastName.text.isEmpty()) {
            etLastName.error = "Last Name is required"
            return false
        }
        if (etAge.text.isEmpty()) {
            etAge.error = "Age is required"
            return false
        }
        if (spinnerCity.text.isEmpty()) {
            spinnerCity.error = "City is required"
            return false
        }
        if (spinnerArea.text.isEmpty()) {
            spinnerArea.error = "Area is required"
            return false
        }
        if (spinnerGender.text.isEmpty()) {
            spinnerGender.error = "Gender is required"
            return false
        }
        return true
    }

    private fun setupCityDropdown() {
        val cityList = listOf("Cairo", "Alexandria", "Giza")
        val adapterCity = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityList)
        spinnerCity.setAdapter(adapterCity)

        spinnerCity.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cityList[position]
            updateAreaDropdown(selectedCity)

            // Remove floating label
            val cityInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout2)
            cityInputLayout.hint = ""
        }
    }

    private fun updateAreaDropdown(selectedCity: String) {
        val areaMap = mapOf(
            "Cairo" to listOf("Nasr City", "Heliopolis", "Maadi"),
            "Alexandria" to listOf("Sidi Gaber", "Smouha", "Gleem"),
            "Giza" to listOf("Mohandessin", "Dokki", "6th of October")
        )

        val areaList = areaMap[selectedCity] ?: emptyList()
        val adapterArea = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, areaList)
        spinnerArea.setAdapter(adapterArea)
        spinnerArea.setText("", false) // Clear previous selection

        spinnerArea.setOnItemClickListener { _, _, _, _ ->
            // Remove floating label
            val areaInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout3)
            areaInputLayout.hint = ""
        }
    }

    private fun setupGenderDropdown() {
        val genderList = listOf("Male", "Female", "Other")
        val adapterGender = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderList)
        spinnerGender.setAdapter(adapterGender)

        // Remove floating hint when a selection is made
        spinnerGender.setOnItemClickListener { _, _, _, _ ->
            val textInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout1)
            textInputLayout.hint = "" // Hide floating label
        }
    }
}
