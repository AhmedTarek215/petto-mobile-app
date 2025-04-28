package com.example.petto.ui.SignUp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petto.R
import com.example.petto.SignUpProgressBar
import com.example.petto.data.viewModel.SignUpViewModel
import com.example.petto.ui.Login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class SignUp4 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up4)

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

        val step = intent.getIntExtra("progress", 4)
        progressBar.setProgress(if (step > 4) 4 else step)

        val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit")
        val breedsMap = mapOf(
            "Dog" to listOf("Labrador", "Poodle", "Bulldog", "Beagle"),
            "Cat" to listOf("Siamese", "Persian", "Maine Coon", "Bengal"),
            "Bird" to listOf("Parrot", "Canary", "Cockatiel", "Finch"),
            "Rabbit" to listOf("Holland Lop", "Netherland Dwarf", "Mini Rex")
        )

        spinnerType.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, petTypes))

        spinnerType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = petTypes[position]
            textInputType.hint = ""
            spinnerBreed.setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    breedsMap[selectedType] ?: emptyList()
                )
            )
            spinnerBreed.setText("", false)
            textInputType.error = null
        }

        spinnerBreed.setOnItemClickListener { _, _, position, _ ->
            val selectedBreed = breedsMap[spinnerType.text.toString()]?.get(position) ?: ""
            textInputBreed.hint = ""
            textInputBreed.error = null
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, SignUp3::class.java)
            intent.putExtra("progress", 3)
            startActivity(intent)
            finish()
        }

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
            }

            if (breed.isEmpty()) {
                textInputBreed.error = "Please select a breed."
                isValid = false
            }

            if (weight.isEmpty()) {
                etWeight.error = "Please enter your pet's weight."
                isValid = false
            }

            if (height.isEmpty()) {
                etHeight.error = "Please enter your pet's height."
                isValid = false
            }

            if (color.isEmpty()) {
                etColor.error = "Please enter your pet's color."
                isValid = false
            }

            if (isValid) {
                SignUpViewModel.petType = petType
                SignUpViewModel.petBreed = breed
                SignUpViewModel.petWeight = weight
                SignUpViewModel.petHeight = height
                SignUpViewModel.petColor = color

                saveToFirestore()
            }
        }
    }

    private fun saveToFirestore() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val email = SignUpViewModel.email
        val password = SignUpViewModel.password

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                val userRef = firestore.collection("Users").document(uid)

                val userData = mapOf(
                    "uid" to uid,
                    "firstName" to SignUpViewModel.firstName,
                    "lastName" to SignUpViewModel.lastName,
                    "age" to SignUpViewModel.age,
                    "gender" to SignUpViewModel.gender,
                    "city" to SignUpViewModel.city,
                    "area" to SignUpViewModel.area,
                    "street" to SignUpViewModel.street,
                    "email" to email,
                    "pet" to mapOf(
                        "name" to SignUpViewModel.petName,
                        "gender" to SignUpViewModel.petGender,
                        "dob" to SignUpViewModel.petBirthDate,
                        "type" to SignUpViewModel.petType,
                        "breed" to SignUpViewModel.petBreed,
                        "weight" to SignUpViewModel.petWeight,
                        "height" to SignUpViewModel.petHeight,
                        "color" to SignUpViewModel.petColor
                    )
                )

                userRef.set(userData).addOnSuccessListener {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Success 🎉")
        builder.setMessage("Your account has been created successfully!")
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        builder.show()
    }

    private fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Signup Failed ❌")
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
