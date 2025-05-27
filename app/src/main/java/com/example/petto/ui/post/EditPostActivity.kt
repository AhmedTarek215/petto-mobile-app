package com.example.petto.ui.post

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.petto.R
import com.google.firebase.firestore.FirebaseFirestore

class EditPostActivity : AppCompatActivity() {

    private lateinit var editContent: EditText
    private lateinit var editImage: ImageView
    private lateinit var saveButton: Button

    private lateinit var postId: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        editContent = findViewById(R.id.editPostContent)
        editImage = findViewById(R.id.editPostImage)
        saveButton = findViewById(R.id.savePostButton)

        postId = intent.getStringExtra("postId") ?: ""

        if (postId.isNotEmpty()) {
            loadPost()
        }

        saveButton.setOnClickListener {
            val updatedContent = editContent.text.toString().trim()
            if (updatedContent.isNotEmpty()) {
                firestore.collection("posts").document(postId)
                    .update("content", updatedContent)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Content can't be empty", Toast.LENGTH_SHORT).show()
            }
        }

        val cancelButton = findViewById<Button>(R.id.btnCancel)
        cancelButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun loadPost() {
        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val content = document.getString("content") ?: ""
                    val imageUrl = document.getString("mediaUrl") ?: ""

                    editContent.setText(content)

                    if (imageUrl.isNotEmpty()) {
                        editImage.visibility = ImageView.VISIBLE
                        Glide.with(this).load(imageUrl).into(editImage)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load post", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
