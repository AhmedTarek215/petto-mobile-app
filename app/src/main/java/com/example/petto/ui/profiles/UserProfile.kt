package com.example.petto.ui.profiles

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var btnMyPets: LinearLayout
    private lateinit var btnSaved: LinearLayout
    private lateinit var btnMyPosts: LinearLayout
    private lateinit var btnPrivacy: LinearLayout
    private lateinit var btnHelp: LinearLayout
    private lateinit var btnAboutUs: LinearLayout

    private lateinit var navHome: ImageView
    private lateinit var navCalendar: ImageView
    private lateinit var navNotifications: ImageView
    private lateinit var navProfile: ImageView
    private lateinit var fabAdd: ImageView

    private lateinit var firestore: FirebaseFirestore
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        userNameTextView = findViewById(R.id.userName)
        userEmailTextView = findViewById(R.id.userEmail)
        profileImageView = findViewById(R.id.profileImageView)

        btnMyPets = findViewById(R.id.btnMyPets)
        btnSaved = findViewById(R.id.btnSaved)
        btnMyPosts = findViewById(R.id.btnMyPosts)
        btnPrivacy = findViewById(R.id.btnPrivacy)
        btnHelp = findViewById(R.id.btnHelp)
        btnAboutUs = findViewById(R.id.btnAboutUs)

        navHome = findViewById(R.id.nav_home)
        navCalendar = findViewById(R.id.nav_calendar)
        navNotifications = findViewById(R.id.nav_notifications)
        navProfile = findViewById(R.id.nav_profile)
        fabAdd = findViewById(R.id.fab)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore = FirebaseFirestore.getInstance()

        if (userId.isNotEmpty()) {
            loadUserData()
        }

        profileImageView.setOnClickListener {
            showAvatarPicker()
        }

        btnMyPets.setOnClickListener {
            startActivity(Intent(this, PetProfile::class.java))
        }

        // Navigation placeholders
        btnSaved.setOnClickListener { }
        btnMyPosts.setOnClickListener { }
        btnPrivacy.setOnClickListener { }
        btnHelp.setOnClickListener { }
        btnAboutUs.setOnClickListener { }

        navHome.setOnClickListener { }
        navCalendar.setOnClickListener { }
        navNotifications.setOnClickListener { }
        navProfile.setOnClickListener { }
        fabAdd.setOnClickListener { }
    }

    private fun loadUserData() {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userNameTextView.text = document.getString("firstName") ?: ""
                    userEmailTextView.text = document.getString("email") ?: ""

                    val avatarId = document.getString("avatar")
                    if (!avatarId.isNullOrEmpty()) {
                        loadAvatarImage(avatarId)
                    } else {
                        profileImageView.setImageResource(R.drawable.profile)
                    }
                }
            }
    }

    private fun loadAvatarImage(avatarId: String) {
        firestore.collection("profile_images").document(avatarId).get()
            .addOnSuccessListener { avatarDoc ->
                val base64 = avatarDoc.getString("url")
                if (!base64.isNullOrEmpty()) {
                    try {
                        val pureBase64 = base64.substringAfter(",")
                        val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        profileImageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        profileImageView.setImageResource(R.drawable.profile)
                    }
                } else {
                    profileImageView.setImageResource(R.drawable.profile)
                }
            }
            .addOnFailureListener {
                profileImageView.setImageResource(R.drawable.profile)
            }
    }

    private fun showAvatarPicker() {
        firestore.collection("profile_images").get()
            .addOnSuccessListener { snapshot ->
                val avatarDocs = snapshot.documents
                if (avatarDocs.isEmpty()) return@addOnSuccessListener

                val avatarIds = avatarDocs.map { it.id }
                val avatarBase64s = avatarDocs.map { it.getString("url") ?: "" }
                val avatarBitmaps = avatarBase64s.map { base64 ->
                    try {
                        val clean = base64.substringAfter(",")
                        val bytes = Base64.decode(clean, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }

                val imageViews = avatarBitmaps.map { bmp ->
                    val img = ImageView(this)
                    img.setImageBitmap(bmp)
                    img.layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                        setMargins(20, 20, 20, 20)
                    }
                    img
                }

                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(20, 20, 20, 20)
                }

                imageViews.forEachIndexed { index, img ->
                    layout.addView(img)
                    img.setOnClickListener {
                        val selectedAvatarId = avatarIds[index]
                        firestore.collection("Users").document(userId)
                            .update("avatar", selectedAvatarId)
                            .addOnSuccessListener {
                                loadAvatarImage(selectedAvatarId)
                                Toast.makeText(this, "Avatar updated", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                AlertDialog.Builder(this)
                    .setTitle("Choose Your Avatar")
                    .setView(layout)
                    .setNegativeButton("Cancel", null)
                    .show()
            }
    }
}
