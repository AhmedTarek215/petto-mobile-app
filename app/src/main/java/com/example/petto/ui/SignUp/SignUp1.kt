package com.example.petto.ui.SignUp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.example.petto.data.viewModel.SignUpViewModel
import com.example.petto.ui.Login.Login
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

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etAge = findViewById(R.id.etAge)
        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerArea = findViewById(R.id.spinnerarea)
        spinnerGender = findViewById(R.id.spinnerGender)
        btnNext = findViewById(R.id.btnNext)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        val text = "Already have an account? Login"
        val spannableString = SpannableString(text)
        val pinkColor = ForegroundColorSpan(Color.parseColor("#D16B78"))
        spannableString.setSpan(pinkColor, text.indexOf("Login"), text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvLogin.text = spannableString

        tvLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        findViewById<SignUpProgressBar>(R.id.progressBar).setProgress(1)

        setupCityDropdown()
        setupGenderDropdown()

        etFirstName.setText(SignUpViewModel.firstName)
        etLastName.setText(SignUpViewModel.lastName)
        if (SignUpViewModel.age > 0) etAge.setText(SignUpViewModel.age.toString())
        spinnerCity.setText(SignUpViewModel.city, false)
        updateAreaDropdown(SignUpViewModel.city) // populate areas
        spinnerArea.setText(SignUpViewModel.area, false)
        spinnerGender.setText(SignUpViewModel.gender, false)

        btnNext.setOnClickListener {
            if (validateFields()) {
                saveUserPage1Data()
                val intent = Intent(this, SignUp2::class.java)
                intent.putExtra("progress", 2)
                startActivity(intent)
            }
        }
    }

    private fun saveUserPage1Data() {
        SignUpViewModel.firstName = etFirstName.text.toString().trim()
        SignUpViewModel.lastName = etLastName.text.toString().trim()
        SignUpViewModel.age = etAge.text.toString().toIntOrNull() ?: 0
        SignUpViewModel.gender = spinnerGender.text.toString().trim()
        SignUpViewModel.city = spinnerCity.text.toString().trim()
        SignUpViewModel.area = spinnerArea.text.toString().trim()
        SignUpViewModel.street = ""
    }

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
            updateAreaDropdown(cityList[position])
            findViewById<TextInputLayout>(R.id.textInputLayout2).hint = ""
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
        spinnerArea.setText("", false)

        spinnerArea.setOnItemClickListener { _, _, _, _ ->
            findViewById<TextInputLayout>(R.id.textInputLayout3).hint = ""
        }
    }

    private fun setupGenderDropdown() {
        val genderList = listOf("Male", "Female", "Other")
        val adapterGender = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderList)
        spinnerGender.setAdapter(adapterGender)

        spinnerGender.setOnItemClickListener { _, _, _, _ ->
            findViewById<TextInputLayout>(R.id.textInputLayout1).hint = ""
        }
    }
}
