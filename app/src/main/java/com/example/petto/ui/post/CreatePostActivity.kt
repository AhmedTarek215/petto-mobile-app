package com.example.petto.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class CreatePostActivity : AppCompatActivity() {

    private lateinit var editTextPost: EditText
    private lateinit var btnPost: Button
    private lateinit var btnCancel: Button
    private lateinit var backButton: ImageView
    private lateinit var profileImageView: CircleImageView
    private lateinit var usernameText: TextView

    private var selectedFileUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        editTextPost = findViewById(R.id.editTextPost)
        btnPost = findViewById(R.id.btnPost)
        btnCancel = findViewById(R.id.btnCancel)
        backButton = findViewById(R.id.backButton)
        profileImageView = findViewById(R.id.profileImageView)
        usernameText = findViewById(R.id.userName)

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestore.collection("Users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val fullName = "$firstName $lastName".trim()
                        usernameText.text = fullName

                        val avatarId = document.getString("avatarId")
                        if (!avatarId.isNullOrEmpty()) {
                            firestore.collection("profile_images").document(avatarId).get()
                                .addOnSuccessListener { avatarDoc ->
                                    val imageUrl = avatarDoc.getString("url")
                                    if (!imageUrl.isNullOrEmpty()) {
                                        Glide.with(this).load(imageUrl).into(profileImageView)
                                    } else {
                                        profileImageView.setImageResource(R.drawable.profile)
                                    }
                                }
                                .addOnFailureListener {
                                    profileImageView.setImageResource(R.drawable.profile)
                                }
                        } else {
                            profileImageView.setImageResource(R.drawable.profile)
                        }
                    } else {
                        Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
        }

        backButton.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        btnPost.setOnClickListener {
            val content = editTextPost.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (content.isEmpty()) {
                Toast.makeText(this, "Post cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedFileUri != null) {
                uploadMediaAndCreatePost(userId, content, selectedFileUri!!)
            } else {
                savePostToFirestore(userId, content, null)
            }
        }
    }

    private fun uploadMediaAndCreatePost(userId: String, content: String, fileUri: Uri) {
        val ref = storage.reference.child("posts/${System.currentTimeMillis()}_${fileUri.lastPathSegment}")
        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    savePostToFirestore(userId, content, downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePostToFirestore(userId: String, content: String, mediaUrl: String?) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val fullName = "$firstName $lastName".trim()
                    val avatarId = document.getString("avatarId")

                    if (!avatarId.isNullOrEmpty()) {
                        firestore.collection("profile_images").document(avatarId).get()
                            .addOnSuccessListener { avatarDoc ->
                                val profileImageUrl = avatarDoc.getString("url") ?: ""
                                createPost(userId, fullName, profileImageUrl, content, mediaUrl)
                            }
                            .addOnFailureListener {
                                // fallback if avatar couldn't be loaded
                                createPost(userId, fullName, "", content, mediaUrl)
                            }
                    } else {
                        // fallback if no avatar was set
                        createPost(userId, fullName, "", content, mediaUrl)
                    }
                } else {
                    Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createPost(
        userId: String,
        username: String,
        profileImageUrl: String,
        content: String,
        mediaUrl: String?
    ) {
        val post = hashMapOf(
            "userId" to userId,
            "username" to username,
            "userProfileImage" to profileImageUrl,
            "content" to content,
            "mediaUrl" to mediaUrl,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "likes" to 0
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Post created!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PostListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.data
            val type = contentResolver.getType(selectedFileUri!!)
            if (type?.startsWith("image/") == true) {
                Toast.makeText(this, "Image attached", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Video attached", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
