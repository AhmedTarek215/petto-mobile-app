package com.example.petto.ui.SignUp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.google.android.material.textfield.TextInputLayout

class SignUp4 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up4)

        // Initialize Views
        val progressBar = findViewById<SignUpProgressBar>(R.id.progressBar)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val spinnerType = findViewById<AutoCompleteTextView>(R.id.spinnerType)
        val spinnerBreed = findViewById<AutoCompleteTextView>(R.id.spinnerBreed)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etColor = findViewById<EditText>(R.id.etColor)

        val textInputType = findViewById<TextInputLayout>(R.id.textInputLayout1)
        val textInputBreed = findViewById<TextInputLayout>(R.id.textInputLayout2)

        // **Set Progress Bar for Step 4**
        val step = intent.getIntExtra("progress", 4) // Default to 4 if not provided
        Log.d("SignUp4", "Received progress: $step") // Debugging

        val fixedStep = if (step > 4) 4 else step // Prevent incorrect jumps
        progressBar.setProgress(fixedStep)

        // **Pet Type & Breed Handling**
        val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit")
        val breedsMap = mapOf(
            "Dog" to listOf("Labrador", "Poodle", "Bulldog", "Beagle"),
            "Cat" to listOf("Siamese", "Persian", "Maine Coon", "Bengal"),
            "Bird" to listOf("Parrot", "Canary", "Cockatiel", "Finch"),
            "Rabbit" to listOf("Holland Lop", "Netherland Dwarf", "Mini Rex")
        )

        val petTypeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, petTypes)
        spinnerType.setAdapter(petTypeAdapter)
        spinnerType.threshold = 100 // Prevent manual input by setting high threshold

        spinnerType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = petTypes[position]

            // Remove hint on selection
            spinnerType.setText(selectedType, false)
            textInputType.hint = "" // Hide floating label

            // Update breed dropdown based on selected pet type
            val breedAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, breedsMap[selectedType] ?: emptyList())
            spinnerBreed.setAdapter(breedAdapter)
            spinnerBreed.setText("", false) // Reset breed field when type changes

            // Clear error if user selects something
            textInputType.error = null
        }

        spinnerBreed.setOnItemClickListener { _, _, position, _ ->
            val selectedBreed = breedsMap[spinnerType.text.toString()]?.get(position) ?: ""

            // Remove hint on selection
            spinnerBreed.setText(selectedBreed, false)
            textInputBreed.hint = "" // Hide floating label

            // Clear error if user selects something
            textInputBreed.error = null
        }

        // Prevent users from typing random values
        spinnerType.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !petTypes.contains(spinnerType.text.toString())) {
                spinnerType.setText("")
            }
        }

        spinnerBreed.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && !breedsMap[spinnerType.text.toString()].orEmpty().contains(spinnerBreed.text.toString())) {
                spinnerBreed.setText("")
            }
        }

        // **Back Button to Previous Page**
        btnBack.setOnClickListener {
            val intent = Intent(this, SignUp3::class.java)
            intent.putExtra("progress", 3) // Ensure previous step is correct
            startActivity(intent)
            finish()
        }

        // **Next Button - Validation & Proceed**
        btnNext.setOnClickListener {
            val petType = spinnerType.text.toString().trim()
            val breed = spinnerBreed.text.toString().trim()
            val weight = etWeight.text.toString().trim()
            val height = etHeight.text.toString().trim()
            val color = etColor.text.toString().trim()

            var isValid = true

            if (petType.isEmpty()) {
                textInputType.error = "Please select a pet type."
                isValid = false
            } else {
                textInputType.error = null
            }

            if (breed.isEmpty()) {
                textInputBreed.error = "Please select a breed."
                isValid = false
            } else {
                textInputBreed.error = null
            }

            if (weight.isEmpty()) {
                etWeight.error = "Please enter your pet's weight."
                isValid = false
            } else {
                etWeight.error = null
            }

            if (height.isEmpty()) {
                etHeight.error = "Please enter your pet's height."
                isValid = false
            } else {
                etHeight.error = null
            }

            if (color.isEmpty()) {
                etColor.error = "Please enter your pet's color."
                isValid = false
            } else {
                etColor.error = null
            }

            if (isValid) {
                // Proceed to the next page
                //val intent = Intent(this, SignUpCompletionActivity::class.java) // Update with actual next activity
                //intent.putExtra("progress", 5) // Pass progress for next step
                startActivity(intent)
                finish()
            }
        }
    }
}
