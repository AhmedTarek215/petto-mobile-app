package com.example.petto.ui.profiles

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.example.petto.ui.HomeActivity
import com.example.petto.ui.calender.Calendar
import com.example.petto.ui.notification.NotificationActivity
import com.example.petto.ui.post.CreatePostActivity
import com.example.petto.ui.post.MyPostsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class UserProfile : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var profileImageView: CircleImageView

    private lateinit var btnMyPets: LinearLayout
    private lateinit var btnSaved: LinearLayout
    private lateinit var btnMyPosts: LinearLayout
    private lateinit var btnPrivacy: LinearLayout
    private lateinit var btnHelp: LinearLayout
    private lateinit var btnAboutUs: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var navHome: ImageView
    private lateinit var navCalendar: ImageView
    private lateinit var navNotifications: ImageView
    private lateinit var navProfile: ImageView
    private lateinit var fabAdd: ImageView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

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
        btnLogout = findViewById(R.id.btnLogout)

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

        navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        navCalendar.setOnClickListener {
            startActivity(Intent(this, Calendar::class.java))
            finish()
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        navNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
            finish()
        }

        navProfile.setOnClickListener {
            // already here
        }

        btnMyPets.setOnClickListener {
            startActivity(Intent(this, PetProfile::class.java))
        }

        profileImageView.setOnClickListener {
            showAvatarSelectionDialog()
        }

        btnMyPosts.setOnClickListener {
            startActivity(Intent(this, MyPostsActivity::class.java))
        }

        btnHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        btnAboutUs.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
        }

        btnPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, com.example.petto.ui.intro.IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadUserData() {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    userNameTextView.text = "$firstName $lastName"
                    userEmailTextView.text = document.getString("email") ?: ""

                    val avatarId = document.getString("avatarId")
                    if (!avatarId.isNullOrEmpty()) {
                        loadAvatarFromFirestore(avatarId)
                    } else {
                        profileImageView.setImageResource(R.drawable.profile)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                profileImageView.setImageResource(R.drawable.profile)
            }
    }

    private fun loadAvatarFromFirestore(avatarId: String) {
        firestore.collection("profile_images").document(avatarId).get()
            .addOnSuccessListener { doc ->
                val base64 = doc.getString("url")
                if (!base64.isNullOrEmpty()) {
                    try {
                        val imageData = base64.substringAfter("base64,", "")
                        val decoded = Base64.decode(imageData, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
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

    private fun showAvatarSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile_image_selector, null)
        val gridLayout = dialogView.findViewById<GridLayout>(R.id.imageGrid)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Profile Image")
            .setView(dialogView)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        FirebaseFirestore.getInstance().collection("profile_images")
            .get()
            .addOnSuccessListener { result ->
                var index = 0
                for (doc in result) {
                    val url = doc.getString("url") ?: continue
                    val imageView = ImageView(this)

                    val params = GridLayout.LayoutParams().apply {
                        width = 250
                        height = 250
                        rowSpec = GridLayout.spec(index / 3)
                        columnSpec = GridLayout.spec(index % 3)
                        setMargins(16, 16, 16, 16)
                    }

                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    Glide.with(this).load(url).into(imageView)

                    imageView.setOnClickListener {
                        Glide.with(this).load(url).into(profileImageView)
                        firestore.collection("Users").document(userId)
                            .update("avatarId", doc.id)
                        dialog.dismiss()
                    }

                    gridLayout.addView(imageView)
                    index++
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load avatars", Toast.LENGTH_SHORT).show()
            }
    }
}
