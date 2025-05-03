package com.example.petto.ui.profiles

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import com.bumptech.glide.Glide
import com.example.petto.ui.profiles.PetProfile

class UserProfile : AppCompatActivity() {

    private lateinit var profileImageView: CircleImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userPhone: TextView

    private lateinit var btnMyPets: LinearLayout
    private lateinit var btnSaved: LinearLayout
    private lateinit var btnMyPosts: LinearLayout
    private lateinit var btnPrivacy: LinearLayout
    private lateinit var btnHelp: LinearLayout
    private lateinit var btnAboutUs: LinearLayout

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutId = resources.getIdentifier("activity_user_profile", "layout", packageName)
        setContentView(layoutId)

        profileImageView = findViewById(getResId("profileImageView"))
        userName = findViewById(getResId("userName"))
        userEmail = findViewById(getResId("userEmail"))
        userPhone = findViewById(getResId("userPhone"))

        btnMyPets = findViewById(getResId("btnMyPets"))
        btnSaved = findViewById(getResId("btnSaved"))
        btnMyPosts = findViewById(getResId("btnMyPosts"))
        btnPrivacy = findViewById(getResId("btnPrivacy"))
        btnHelp = findViewById(getResId("btnHelp"))
        btnAboutUs = findViewById(getResId("btnAboutUs"))

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            loadUserData(userId)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        btnMyPets.setOnClickListener {
            startActivity(Intent(this, PetProfile::class.java))
        }

        btnSaved.setOnClickListener {
            Toast.makeText(this, "Saved clicked", Toast.LENGTH_SHORT).show()
        }

        btnMyPosts.setOnClickListener {
            Toast.makeText(this, "My Posts clicked", Toast.LENGTH_SHORT).show()
        }

        btnPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy clicked", Toast.LENGTH_SHORT).show()
        }

        btnHelp.setOnClickListener {
            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show()
        }

        btnAboutUs.setOnClickListener {
            Toast.makeText(this, "About Us clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData(userId: String) {
        firestore.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val profileUrl = document.getString("profileImageUrl") ?: ""

                    userName.text = "$firstName $lastName"
                    userEmail.text = email
                    userPhone.text = phone

                    if (profileUrl.isNotEmpty()) {
                        Glide.with(this).load(profileUrl).into(profileImageView)
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getResId(name: String): Int {
        return resources.getIdentifier(name, "id", packageName)
    }
}
